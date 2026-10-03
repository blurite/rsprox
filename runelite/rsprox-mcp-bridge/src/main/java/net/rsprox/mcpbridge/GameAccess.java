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

    /** The game cycle at which the last game tick started, or the lowest value before the first. */
    private volatile int tickCycle = Integer.MIN_VALUE;

    /** Create the access to the given client, its thread, its renderer and its event bus. */
    GameAccess(Client client, Executor clientThread, DrawManager drawManager, EventBus eventBus) {
        this.client = client;
        this.clientThread = clientThread;
        this.drawManager = drawManager;
        this.eventBus = eventBus;
        this.ticks = subscribe(GameTick.class, (game, tick) -> tickCycle = game.getGameCycle());
    }

    interface ClientCall<T> {
        /** Read from or act on the client, on the client thread. */
        T call(Client client) throws BridgeException;
    }

    interface ClientEvent<T> {
        /** Handle an event that the client posted, on the thread it posted it from. */
        void on(Client client, T event);
    }

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

    /** Receive every event of the type until the subscription is closed. */
    <T> Subscription subscribe(Class<T> type, ClientEvent<T> handler) {
        EventBus.Subscriber subscriber = eventBus.register(type, event -> handler.on(client, event), PRIORITY);

        return () -> eventBus.unregister(subscriber);
    }

    /** Get the game cycle at which the last game tick started, or the lowest value before the first. */
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

    /** Wait until the client has drawn its next frame. The image is not kept. */
    void nextFrame() throws BridgeException {
        CompletableFuture<Void> drawn = new CompletableFuture<>();
        drawManager.requestNextFrameListener(image -> drawn.complete(null));

        await(drawn, "a rendered frame");
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
