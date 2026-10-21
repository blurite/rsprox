package net.rsprox.mcpbridge;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * One attempt to reach rsprox, and the connection it makes. Dialling blocks for seconds, so it runs on a
 * thread of its own. Whoever closes the dial also closes a connection that the attempt makes later.
 */
final class BridgeDial implements AutoCloseable {
    /** Whether the dial has been closed. Guarded by this. */
    private boolean closed;

    /** The connection the attempt made, or null while there is none. Guarded by this. */
    private BridgeConnection connection;

    /** Create a dial that has not been attempted yet. */
    private BridgeDial() {
        //
    }

    /**
     * Run {@code dial} on a daemon thread, and hand the connection it returns to {@code onConnected} on
     * that thread. A dial that returns null, or that was closed in the meantime, hands over nothing.
     */
    static BridgeDial start(Supplier<BridgeConnection> dial, Consumer<BridgeConnection> onConnected) {
        BridgeDial attempt = new BridgeDial();
        Thread thread = new Thread(() -> attempt.run(dial, onConnected), "rsprox-mcp-bridge-dial");
        thread.setDaemon(true);
        thread.start();

        return attempt;
    }

    /** Dial unless the dial was closed first, and keep the connection unless it was closed meanwhile. */
    private void run(Supplier<BridgeConnection> dial, Consumer<BridgeConnection> onConnected) {
        if (isClosed()) return;

        BridgeConnection made = dial.get();
        if (made == null) return;

        if (!keep(made)) {
            made.close();

            return;
        }

        onConnected.accept(made);
    }

    /** Determine if the dial has been closed. */
    private synchronized boolean isClosed() {
        return closed;
    }

    /** Take ownership of the connection, and determine if the dial was still open to take it. */
    private synchronized boolean keep(BridgeConnection made) {
        if (closed) return false;

        connection = made;

        return true;
    }

    /** Close the connection, if there is one, and any that the attempt still makes. Idempotent. */
    @Override
    public synchronized void close() {
        closed = true;

        if (connection != null) {
            connection.close();
        }
    }
}
