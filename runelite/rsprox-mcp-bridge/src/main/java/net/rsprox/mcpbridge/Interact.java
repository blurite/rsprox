package net.rsprox.mcpbridge;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Performs one option on one thing in the game as a player does: with the real mouse, through the
 * menu the client builds for it. The client then sends what it sends for any player's click.
 */
final class Interact implements Ops.Op {
    /** The gateway to the client and the threads it must be used on. */
    private final GameAccess game;

    /** The real mouse of the client. */
    private final Mouse mouse;

    /** Create the interactions for the given game, through the given mouse. */
    Interact(GameAccess game, Mouse mouse) {
        this.game = game;
        this.mouse = mouse;
    }

    /** Perform the option on the target, or walk to the tile, and describe what the client did. */
    @Override
    public JsonElement run(JsonObject args) throws BridgeException {
        Targets.requireArguments(args);
        Target target = game.onClientThread(client -> Targets.resolve(client, args));

        return new Interaction(game, mouse, target).run();
    }
}
