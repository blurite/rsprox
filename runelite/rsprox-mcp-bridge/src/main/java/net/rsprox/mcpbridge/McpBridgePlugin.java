package net.rsprox.mcpbridge;

import com.google.gson.Gson;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.DrawManager;

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
    @Inject private Client client;
    @Inject private ClientThread clientThread;
    @Inject private DrawManager drawManager;
    @Inject private Gson gson;

    private BridgeConnection connection;

    @Override
    protected void startUp() {
        Path rendezvous = Paths.get(System.getProperty("user.home"), ".rsprox", "mcp", "bridge.json");
        Ops ops = new Ops(new GameAccess(client, clientThread, drawManager));
        // The protocol spells out an absent player as null, which the client's Gson would drop.
        connection = BridgeConnection.dial(rendezvous, gson.newBuilder().serializeNulls().create(), ops);
    }

    @Override
    protected void shutDown() {
        if (connection != null) {
            connection.close();
            connection = null;
        }
    }
}
