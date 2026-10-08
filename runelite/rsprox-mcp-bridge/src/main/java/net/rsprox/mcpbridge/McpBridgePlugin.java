package net.rsprox.mcpbridge;

import com.google.gson.Gson;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.OptionalInt;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginInstantiationException;
import net.runelite.client.plugins.PluginManager;
import net.runelite.client.ui.DrawManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The in-client half of the rsprox MCP server. The sideload directory is shared with clients that
 * rsprox did not launch for an agent, so the plugin does nothing unless a running MCP server left a
 * rendezvous file and welcomes this client.
 */
@PluginDescriptor(
    name = "RSProx MCP Bridge",
    description = "Lets the rsprox MCP server observe and drive this client",
    hidden = true
)
public class McpBridgePlugin extends Plugin {
    /** The logger of the plugin. */
    private static final Logger log = LoggerFactory.getLogger(McpBridgePlugin.class);

    /** The class name of the client's GPU plugin. */
    private static final String GPU_PLUGIN = "GpuPlugin";

    /** The longest wait for the GPU plugin to take over rendering. */
    private static final long GPU_START_WAIT_MS = 60_000;

    /** The pause between two checks of whether the GPU plugin renders. */
    private static final long GPU_POLL_MS = 100;

    /** The game client. */
    @Inject private Client client;

    /** The runner of tasks on the client thread. */
    @Inject private ClientThread clientThread;

    /** The source of rendered frames. */
    @Inject private DrawManager drawManager;

    /** The bus that the client posts its events on. */
    @Inject private EventBus eventBus;

    /** The client's JSON serializer. */
    @Inject private Gson gson;

    /** The manager that starts and stops the client's plugins. */
    @Inject private PluginManager pluginManager;

    /** The access to the game that the ops share, or null while the plugin is dormant. */
    private GameAccess game;

    /** The dial to rsprox and the connection it made, or null while the plugin is dormant. */
    private BridgeDial dial;

    /**
     * Connect to the rsprox that launched this client, and stay dormant when there is none.
     * The client calls this on its UI thread, so the dial runs on a thread of its own.
     */
    @Override
    protected void startUp() {
        OptionalInt httpPort = BridgeConnection.httpPort(System.getProperty("sun.java.command", ""));
        if (httpPort.isEmpty()) return;

        // The server writes this path in McpMain.kt.
        Path rendezvous = Paths.get(System.getProperty("user.home"), ".rsprox", "mcp", "bridge.json");
        game = new GameAccess(client, clientThread::invoke, drawManager, eventBus);
        Ops ops = new Ops(game);
        int port = httpPort.getAsInt();

        dial = BridgeDial.start(() -> BridgeConnection.dial(rendezvous, port, gson, ops), this::onConnected);
    }

    /** Stop the GPU plugin when rsprox asked for software rendering. Runs on the thread of the dial. */
    private void onConnected(BridgeConnection connection) {
        if (connection.rendering() != Rendering.SOFTWARE) return;

        stopGpuPlugin();
    }

    /**
     * Stop the GPU plugin once it renders, so that screenshots show the game.
     * Frames read back from the GPU plugin are black on a virtual display. Stopping the plugin, instead
     * of disabling it, leaves the profile untouched for clients that share it.
     */
    private void stopGpuPlugin() {
        Plugin gpu = pluginManager.getPlugins().stream()
            .filter(plugin -> plugin.getClass().getSimpleName().equals(GPU_PLUGIN))
            .reduce((first, last) -> last)
            .orElse(null);

        if (gpu == null || !pluginManager.isPluginEnabled(gpu)) return;

        // The GPU plugin finishes starting on the client thread, some time after it is marked active.
        // Stopping it before then lets that late start win, so wait until the client renders through it.
        long deadline = System.currentTimeMillis() + GPU_START_WAIT_MS;

        try {
            while (!(pluginManager.isPluginActive(gpu) && client.isGpu())) {
                if (System.currentTimeMillis() >= deadline) {
                    log.info("The GPU plugin did not take over rendering, so there is nothing to stop");

                    return;
                }

                Thread.sleep(GPU_POLL_MS);
            }

            pluginManager.stopPlugin(gpu);
            log.info("Stopped the GPU plugin for software rendering");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (PluginInstantiationException e) {
            log.warn("Unable to stop the GPU plugin", e);
        }
    }

    /** Close the connection to rsprox, if there is one or the dial still makes one, and let go of the game. */
    @Override
    protected void shutDown() {
        if (dial == null) return;

        dial.close();
        dial = null;
        game.close();
        game = null;
    }
}
