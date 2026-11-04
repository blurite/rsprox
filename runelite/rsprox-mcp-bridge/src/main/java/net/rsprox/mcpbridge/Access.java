package net.rsprox.mcpbridge;

import com.google.gson.JsonObject;
import java.io.File;

/** What rsprox may do with this client: read it, or also drive it by sending it input. */
enum Access {
    /** Read the client. The ops that send input are never built for a connection with this access. */
    READ,

    /** Read the client and send it input. */
    DRIVE;

    /** The name of the home directory of the stock RuneLite client, which rsprox launches the official game with. */
    private static final String STOCK_HOME = ".runelite";

    /**
     * Decide the access from rsprox's welcome and the client's own home directory. Input is allowed
     * only when the welcome grants it and this is not the stock client. rsprox launches the official
     * game into the stock home and a custom server into {@code .rlcustom}, so the stock home is the
     * client's own evidence that it plays the live game, whatever rsprox said. A welcome without the
     * field grants nothing.
     */
    static Access of(JsonObject welcome, File clientHome) {
        boolean granted = welcome.has("access") && "drive".equals(welcome.get("access").getAsString());
        boolean stockClient = STOCK_HOME.equals(clientHome.getName());

        return granted && !stockClient ? DRIVE : READ;
    }
}
