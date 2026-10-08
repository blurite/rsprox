package net.rsprox.mcpbridge;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.OptionalInt;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import net.runelite.client.RuneLiteProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The socket to rsprox: one JSON object per line in each direction. A reader thread hands each request
 * to a small worker pool, so a request that waits on the game never holds up the next one.
 */
final class BridgeConnection implements AutoCloseable {
    /** The logger of the connection. */
    private static final Logger log = LoggerFactory.getLogger(BridgeConnection.class);

    /** The version of the wire protocol that this plugin speaks. */
    private static final int PROTOCOL = 1;

    /** The longest wait for rsprox to accept the socket. */
    private static final int CONNECT_TIMEOUT_MS = 2_000;

    /** The longest wait for rsprox to answer the hello. */
    private static final int HELLO_TIMEOUT_MS = 5_000;

    /** The number of requests that can run at the same time. */
    private static final int WORKERS = 4;

    /** The start of the argument in which rsprox hands a client its jav_config, up to the port. */
    private static final String JAV_CONFIG_PREFIX = "--jav_config=http://127.0.0.1:";

    /** The socket to rsprox. */
    private final Socket socket;

    /** The source of the requests from rsprox. */
    private final BufferedReader reader;

    /** The sink of the replies, which each writer locks for the length of one line. */
    private final BufferedWriter writer;

    /** The JSON serializer, which writes nulls. */
    private final Gson gson;

    /** The ops that requests run. */
    private final Ops ops;

    /** Whether rsprox asked in its welcome for a client without the GPU plugin. */
    final boolean softwareRendering;

    /** Whether the connection has been closed. */
    private final AtomicBoolean closed = new AtomicBoolean();

    /** The pool that runs the requests. */
    private ExecutorService workers;

    /** Create a connection over a socket that rsprox has welcomed. */
    private BridgeConnection(
        Socket socket, BufferedReader reader, BufferedWriter writer, Gson gson, Ops ops, boolean softwareRendering) {
        this.softwareRendering = softwareRendering;
        this.socket = socket;
        this.reader = reader;
        this.writer = writer;
        this.gson = gson;
        this.ops = ops;
    }

    /**
     * Connect to the rsprox that launched this client on {@code httpPort}. Returns null, having started
     * no thread, when there is no rsprox to talk to or it does not expect this client.
     */
    static BridgeConnection dial(Path rendezvous, int httpPort, Gson clientGson, Ops ops) {
        if (!Files.isRegularFile(rendezvous)) return null;

        // The protocol spells out an absent player as null, which the client's Gson would drop.
        Gson gson = clientGson.newBuilder().serializeNulls().create();
        Socket socket = new Socket();

        try {
            JsonObject file = gson.fromJson(Files.readString(rendezvous), JsonObject.class);
            socket.connect(
                new InetSocketAddress(InetAddress.getLoopbackAddress(), file.get("port").getAsInt()),
                CONNECT_TIMEOUT_MS);

            socket.setSoTimeout(HELLO_TIMEOUT_MS);
            BufferedReader reader =
                new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));

            BufferedWriter writer =
                new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));

            JsonObject hello = new JsonObject();
            hello.addProperty("hello", PROTOCOL);
            hello.addProperty("httpPort", httpPort);
            hello.add("token", file.get("token"));
            hello.addProperty("pid", ProcessHandle.current().pid());
            hello.addProperty("runelite", RuneLiteProperties.getVersion());
            hello.addProperty("plugin", BridgeConnection.class.getPackage().getImplementationVersion());
            writer.write(gson.toJson(hello));
            writer.write('\n');
            writer.flush();

            String line = reader.readLine();
            JsonObject answer = line == null ? null : gson.fromJson(line, JsonObject.class);

            if (answer == null || !answer.has("welcome")) {
                log.info("rsprox MCP bridge stays dormant: {}", answer == null ? "no answer" : answer.get("reject"));
                socket.close();

                return null;
            }

            socket.setSoTimeout(0);
            boolean software = answer.has("softwareRendering") && answer.get("softwareRendering").getAsBoolean();
            BridgeConnection connection = new BridgeConnection(socket, reader, writer, gson, ops, software);
            connection.start();
            log.info("rsprox MCP bridge connected as session {}", answer.get("session"));

            return connection;
        } catch (IOException | RuntimeException e) {
            log.info("rsprox MCP bridge stays dormant: {}", e.toString());

            try {
                socket.close();
            } catch (IOException ignored) {
                // Nothing was established, so there is nothing to report.
            }

            return null;
        }
    }

    /**
     * Get the port in the jav_config argument of a client's command line, which is how rsprox tells its
     * clients apart. Empty when rsprox did not launch the client.
     */
    static OptionalInt httpPort(String commandLine) {
        int start = commandLine.indexOf(JAV_CONFIG_PREFIX);
        if (start < 0) return OptionalInt.empty();

        start += JAV_CONFIG_PREFIX.length();

        int end = start;
        while (end < commandLine.length() && Character.isDigit(commandLine.charAt(end))) {
            end++;
        }

        try {
            return OptionalInt.of(Integer.parseInt(commandLine.substring(start, end)));
        } catch (NumberFormatException e) {
            return OptionalInt.empty();
        }
    }

    /** Start the worker pool and the thread that reads requests. */
    private void start() {
        workers = Executors.newFixedThreadPool(WORKERS, runnable -> daemon(runnable, "rsprox-mcp-bridge-worker"));
        daemon(this::readLoop, "rsprox-mcp-bridge-reader").start();
    }

    /** Create a daemon thread, which never keeps the client alive. */
    private static Thread daemon(Runnable runnable, String name) {
        Thread thread = new Thread(runnable, name);
        thread.setDaemon(true);

        return thread;
    }

    /** Hand each request to the workers until the connection ends, then close it. */
    private void readLoop() {
        try {
            String line;

            while ((line = reader.readLine()) != null) {
                JsonObject request = gson.fromJson(line, JsonObject.class);
                long id = request.get("id").getAsLong();
                String op = request.get("op").getAsString();
                JsonObject args = request.has("args") ? request.getAsJsonObject("args") : new JsonObject();
                workers.execute(() -> answer(id, op, args));
            }
        } catch (IOException | RuntimeException e) {
            if (!closed.get()) {
                log.info("rsprox MCP bridge lost its connection: {}", e.toString());
            }
        } finally {
            close();
        }
    }

    /** Run one request and write its reply, which carries {@code err} when the op failed. */
    private void answer(long id, String op, JsonObject args) {
        JsonObject reply = new JsonObject();
        reply.addProperty("id", id);

        try {
            reply.add("ok", ops.run(op, args));
        } catch (BridgeException e) {
            reply.add("err", error(e.code, e.getMessage()));
        } catch (Throwable t) {
            log.warn("rsprox MCP bridge op {} failed", op, t);
            reply.add("err", error("internal", t.toString()));
        }

        try {
            synchronized (writer) {
                writer.write(gson.toJson(reply));
                writer.write('\n');
                writer.flush();
            }
        } catch (IOException e) {
            close();
        }
    }

    /** Build the {@code err} member of a reply. */
    private static JsonObject error(String code, String message) {
        JsonObject error = new JsonObject();
        error.addProperty("code", code);
        error.addProperty("message", message);

        return error;
    }

    /** Close the socket and stop the workers. Idempotent. */
    @Override
    public void close() {
        if (!closed.compareAndSet(false, true)) return;

        try {
            socket.close();
        } catch (IOException ignored) {
            // The connection is being dropped either way.
        }

        workers.shutdownNow();
    }
}
