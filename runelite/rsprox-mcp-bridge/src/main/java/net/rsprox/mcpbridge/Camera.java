package net.rsprox.mcpbridge;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;

/**
 * Reads and turns the camera. Reading is the op {@code camera}; the turn, which is input, is the op
 * {@code camera_turn}. A turn waits until the camera has settled, within a bound, because the client
 * moves the camera toward its target over several ticks.
 */
final class Camera implements Ops.Op {
    /** The cycles over which neither the yaw nor the pitch may change for the camera to count as settled. */
    private static final int SETTLED_CYCLES = 10;

    /** The most cycles a turn is given to settle, which is six seconds. */
    private static final int TURN_CYCLES = 300;

    /** The gateway to the client and the threads it must be used on. */
    private final GameAccess game;

    /** Create the camera op for the given game. */
    Camera(GameAccess game) {
        this.game = game;
    }

    /** Report the camera's yaw and pitch without touching it. */
    JsonElement read(JsonObject args) throws BridgeException {
        return game.onClientThread(client -> describe(client, null));
    }

    /**
     * Turn the camera as asked, wait for it to settle, and report its yaw and pitch, with whether the
     * looked-at target is on screen.
     */
    @Override
    public JsonElement run(JsonObject args) throws BridgeException {
        String lookAt = Ops.optionalString(args, "look_at");
        Target target = lookAt == null ? null : locate(lookAt, args);
        turn(args, target);
        settle();

        return game.onClientThread(client -> describe(client, target));
    }

    /** Find the target of the kind that look_at names, from the same fields as an interaction takes. */
    private Target locate(String kind, JsonObject args) throws BridgeException {
        JsonObject named = args.deepCopy();
        named.addProperty("target", kind);
        Targets.requireIdentity(named);

        return game.onClientThread(client -> Targets.locate(client, named));
    }

    /** Set the camera's targets: the yaw that faces the target, then the yaw and the pitch that are given. */
    private void turn(JsonObject args, Target target) throws BridgeException {
        Integer yaw = Ops.optionalInt(args, "yaw");
        Integer pitch = Ops.optionalInt(args, "pitch");

        game.onClientThread(client -> {
            if (target != null) client.setCameraYawTarget(yawToward(client, target));

            if (yaw != null) client.setCameraYawTarget(yaw);

            if (pitch != null) client.setCameraPitchTarget(pitch);

            return null;
        });
    }

    /** Get the yaw that faces the target from the local player. Throws {@code wrong_state} while there is no player. */
    private static int yawToward(Client client, Target target) throws BridgeException {
        Player local = client.getLocalPlayer();
        if (local == null) throw new BridgeException("wrong_state", "the client is not in the game");

        WorldPoint tile = target.tile(client);
        if (tile == null) throw new BridgeException("not_found", target.label() + " is not in the world");

        LocalPoint there = LocalPoint.fromWorld(client.getTopLevelWorldView(), tile);
        if (there == null) throw new BridgeException("not_found", target.label() + " is outside the loaded scene");

        return CameraTurn.yawToward(local.getLocalLocation(), there);
    }

    /** Wait until the yaw and the pitch have not changed for the settled number of cycles, within the bound. */
    private void settle() throws BridgeException {
        int start = game.cycle();
        int stillSince = start;
        int now = start;
        long last = game.onClientThread(Camera::angles);

        while (now - stillSince < SETTLED_CYCLES && now - start < TURN_CYCLES) {
            game.nextFrame();
            now = game.cycle();
            long angles = game.onClientThread(Camera::angles);

            if (angles != last) stillSince = now;

            last = angles;
        }
    }

    /** Get the yaw and the pitch packed into one value, so that a change in either shows. */
    private static long angles(Client client) {
        return ((long) client.getCameraYaw() << Integer.SIZE) | client.getCameraPitch();
    }

    /** Describe the camera's yaw and pitch, and whether the target is on screen when one was looked at. */
    private static JsonObject describe(Client client, Target target) {
        JsonObject out = new JsonObject();
        out.addProperty("yaw", client.getCameraYaw());
        out.addProperty("pitch", client.getCameraPitch());

        if (target != null) out.addProperty("onScreen", target.aim(client, 1) != null);

        return out;
    }
}
