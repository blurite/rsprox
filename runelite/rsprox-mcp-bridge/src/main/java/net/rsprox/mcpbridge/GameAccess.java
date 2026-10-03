package net.rsprox.mcpbridge;

import java.awt.Canvas;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import javax.swing.SwingUtilities;
import net.runelite.api.Client;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.ui.DrawManager;

/**
 * The only holder of the client, its canvas and the draw manager. Each is handed out solely inside a
 * callback that runs on the thread it belongs to, so an op cannot touch game state from a worker thread.
 */
final class GameAccess {
    private static final long TIMEOUT_MS = 5_000;

    private final Client client;
    private final ClientThread clientThread;
    private final DrawManager drawManager;

    GameAccess(Client client, ClientThread clientThread, DrawManager drawManager) {
        this.client = client;
        this.clientThread = clientThread;
        this.drawManager = drawManager;
    }

    interface ClientCall<T> {
        T call(Client client) throws BridgeException;
    }

    /** Runs {@code body} on the client thread and waits for its result. */
    <T> T read(ClientCall<T> body) throws BridgeException {
        CompletableFuture<T> result = new CompletableFuture<>();
        clientThread.invoke(() -> {
            try {
                result.complete(body.call(client));
            } catch (Throwable t) {
                result.completeExceptionally(t);
            }
        });
        return await(result, "the client thread");
    }

    /** Runs {@code body} on the AWT event thread, where synthetic input events must be dispatched. */
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

    /** A copy of the next rendered frame, at whatever size the renderer produced it. */
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

    private static BufferedImage copy(Image image) {
        BufferedImage copy =
            new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = copy.createGraphics();
        graphics.drawImage(image, 0, 0, null);
        graphics.dispose();
        return copy;
    }

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
            if (cause instanceof BridgeException) {
                throw (BridgeException) cause;
            }
            throw new BridgeException("internal", String.valueOf(cause));
        }
    }
}
