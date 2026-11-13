package net.rsprox.mcpbridge;

import java.awt.Canvas;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import javax.swing.SwingUtilities;
import net.runelite.api.Client;
import net.runelite.api.events.GameTick;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.ui.DrawManager;

/**
 * The only holder of the client, its canvas, the draw manager and the event bus. Each is handed out
 * solely inside a callback that runs on the thread it belongs to, so an op cannot touch game state
 * from a worker thread.
 */
final class GameAccess implements AutoCloseable {
    /** The longest wait for the client thread or the renderer to answer. */
    private static final long TIMEOUT_MS = 5_000;

    /** The priority of the subscriptions, which is the default of the client's own plugins. */
    private static final float PRIORITY = 0;

    /** The game client, which is only handed out on its own thread. */
    private final Client client;

    /** The executor that runs a task on the client thread. */
    private final Executor clientThread;

    /** The source of rendered frames. */
    private final DrawManager drawManager;

    /** The bus that the client posts its events on. */
    private final EventBus eventBus;

    /** The subscription that keeps {@link #tickCycle} current, for as long as this access is open. */
    private final Subscription ticks;

    /** The client cycle at which the last game tick started, or the lowest value before the first. */
    private volatile int tickCycle = Integer.MIN_VALUE;

    /** Create the access to the given client, its thread, its renderer and its event bus. */
    GameAccess(Client client, Executor clientThread, DrawManager drawManager, EventBus eventBus) {
        this.client = client;
        this.clientThread = clientThread;
        this.drawManager = drawManager;
        this.eventBus = eventBus;
        this.ticks = subscribe(GameTick.class, (game, tick) -> tickCycle = game.getGameCycle());
    }

    /** A read of the client, or an act on it, that runs on the client thread. */
    interface ClientCall<T> {
        /** Read from or act on the client, on the client thread. */
        T call(Client client) throws BridgeException;
    }

    /** A registration for events, which lasts until it is closed. */
    interface Subscription extends AutoCloseable {
        /** Stop receiving the events. Idempotent. */
        @Override
        void close();
    }

    /** Run {@code body} on the client thread and wait for its result. */
    <T> T onClientThread(ClientCall<T> body) throws BridgeException {
        CompletableFuture<T> result = new CompletableFuture<>();
        clientThread.execute(() -> {
            try {
                result.complete(body.call(client));
            } catch (Throwable t) {
                result.completeExceptionally(t);
            }
        });

        return await(result, "the client thread");
    }

    /** Run {@code body} on the AWT event thread, where synthetic input events must be dispatched. */
    void input(Consumer<Canvas> body) throws BridgeException {
        try {
            SwingUtilities.invokeAndWait(() -> body.accept(client.getCanvas()));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BridgeException("internal", "interrupted while dispatching input");
        } catch (InvocationTargetException e) {
            throw new BridgeException("internal", String.valueOf(e.getCause()));
        }
    }

    /** Receive every event of the type, on the thread the client posts it from, until the subscription is closed. */
    <T> Subscription subscribe(Class<T> type, BiConsumer<Client, T> handler) {
        EventBus.Subscriber subscriber = eventBus.register(type, event -> handler.accept(client, event), PRIORITY);

        return () -> eventBus.unregister(subscriber);
    }

    /** Get the client cycle at which the last game tick started, or the lowest value before the first. */
    int tickCycle() {
        return tickCycle;
    }

    /** Get a copy of the next rendered frame, at whatever size the renderer produced it. */
    BufferedImage frame() throws BridgeException {
        CompletableFuture<BufferedImage> result = new CompletableFuture<>();
        drawManager.requestNextFrameListener(image -> {
            try {
                // Copied on the render thread: the renderer may reuse the image for the next frame.
                result.complete(copy(image));
            } catch (Throwable t) {
                result.completeExceptionally(t);
            }
        });

        return await(result, "a rendered frame");
    }

    /**
     * Wait until the client has drawn its next frame. The listener takes no image, so the GPU plugin
     * does not read the framebuffer back for it.
     */
    void nextFrame() throws BridgeException {
        CompletableFuture<Void> drawn = new CompletableFuture<>();
        Runnable listener = () -> drawn.complete(null);
        drawManager.registerEveryFrameListener(listener);

        try {
            await(drawn, "a rendered frame");
        } finally {
            drawManager.unregisterEveryFrameListener(listener);
        }
    }

    /**
     * Get the client's own cycle counter. It advances once per client cycle of 20 ms, whatever the
     * frame rate, and the client handles the input of a cycle within that cycle.
     */
    int cycle() throws BridgeException {
        return onClientThread(Client::getGameCycle);
    }

    /**
     * Wait until the client's cycle counter has advanced by the given number, looking once per frame,
     * and then for the frame that the client draws next. A frame is not a unit of time: with an
     * unlocked frame rate several frames pass within one cycle.
     */
    void awaitCycles(int cycles) throws BridgeException {
        int until = cycle() + cycles;

        while (cycle() < until) {
            nextFrame();
        }

        nextFrame();
    }

    /** Stop following the game ticks. */
    @Override
    public void close() {
        ticks.close();
    }

    /** Draw the image into a new one that the renderer does not own. */
    private static BufferedImage copy(Image image) {
        BufferedImage copy =
            new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_RGB);

        Graphics2D graphics = copy.createGraphics();
        graphics.drawImage(image, 0, 0, null);
        graphics.dispose();

        return copy;
    }

    /**
     * Wait for the result. Throws {@code timeout} when {@code what} does not answer in time, and the
     * failure of the awaited work when it failed.
     */
    private static <T> T await(CompletableFuture<T> result, String what) throws BridgeException {
        try {
            return result.get(TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            throw new BridgeException("timeout", "no answer from " + what + " within " + TIMEOUT_MS + " ms");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BridgeException("internal", "interrupted while waiting for " + what);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof BridgeException) throw (BridgeException) cause;

            throw new BridgeException("internal", String.valueOf(cause));
        }
    }
}
