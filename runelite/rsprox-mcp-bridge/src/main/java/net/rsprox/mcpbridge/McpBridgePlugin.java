package net.rsprox.mcpbridge;

import com.google.gson.Gson;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.client.callback.ClientThread;
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
    private static final Logger log = LoggerFactory.getLogger(McpBridgePlugin.class);
    private static final String GPU_PLUGIN = "GpuPlugin";
    private static final long GPU_START_WAIT_MS = 60_000;
    private static final long GPU_POLL_MS = 100;

    @Inject private Client client;
    @Inject private ClientThread clientThread;
    @Inject private DrawManager drawManager;
    @Inject private Gson gson;
    @Inject private PluginManager pluginManager;

    private BridgeConnection connection;

    @Override
    protected void startUp() {
        Path rendezvous = Paths.get(System.getProperty("user.home"), ".rsprox", "mcp", "bridge.json");
        Ops ops = new Ops(new GameAccess(client, clientThread, drawManager));

        // The protocol spells out an absent player as null, which the client's Gson would drop.
        connection = BridgeConnection.dial(rendezvous, gson.newBuilder().serializeNulls().create(), ops);
        if (connection != null && connection.softwareRendering()) {
            Thread thread = new Thread(this::stopGpuPlugin, "mcp-bridge-gpu-off");
            thread.setDaemon(true);
            thread.start();
        }
    }

    /**
     * Frames read back from the GPU plugin are black on a virtual display. Stopping the plugin, instead
     * of disabling it, leaves the profile untouched for clients that share it.
     */
    private void stopGpuPlugin() {
        Plugin gpu = null;

        for (Plugin plugin : pluginManager.getPlugins()) {
            if (plugin.getClass().getSimpleName().equals(GPU_PLUGIN)) {
                gpu = plugin;
            }
        }

        if (gpu == null || !pluginManager.isPluginEnabled(gpu)) {
            return;
        }

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

    @Override
    protected void shutDown() {
        if (connection != null) {
            connection.close();
            connection = null;
        }
    }
}
