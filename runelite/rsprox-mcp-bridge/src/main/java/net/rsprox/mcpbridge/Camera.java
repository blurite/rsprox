package net.rsprox.mcpbridge;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;

/**
 * Reads and turns the camera. A turn waits until the camera has settled, within a bound, because the
 * client moves the camera toward its target over several ticks.
 */
final class Camera implements Ops.Op {
    /** The number of frames over which the camera must not move to count as settled. */
    private static final int SETTLED_FRAMES = 3;

    /** The most frames a turn is given to settle. */
    private static final int TURN_FRAMES = 300;

    /** The gateway to the client and the threads it must be used on. */
    private final GameAccess game;

    /** Create the camera op for the given game. */
    Camera(GameAccess game) {
        this.game = game;
    }

    /**
     * Turn the camera as asked, or read it, and report its yaw and pitch, with whether the looked-at
     * target is on screen.
     */
    @Override
    public JsonElement run(JsonObject args) throws BridgeException {
        Integer yaw = Ops.optionalInt(args, "yaw");
        Integer pitch = Ops.optionalInt(args, "pitch");
        String lookAt = Ops.optionalString(args, "look_at");
        requireRanges(yaw, pitch);
        Target target = lookAt == null ? null : locate(lookAt, args);
        boolean turned = game.onClientThread(client -> turn(client, yaw, pitch, target));

        if (turned) settle();

        return game.onClientThread(client -> describe(client, target));
    }

    /** Check that the angles are within the client's ranges. Throws {@code bad_args} when one is not. */
    private static void requireRanges(Integer yaw, Integer pitch) throws BridgeException {
        boolean badYaw = yaw != null && (yaw < 0 || yaw >= CameraTurn.FULL_TURN);
        if (badYaw) throw new BridgeException("bad_args", "yaw must be from 0 to " + (CameraTurn.FULL_TURN - 1));

        boolean badPitch = pitch != null && (pitch < CameraTurn.PITCH_MIN || pitch > CameraTurn.PITCH_MAX);
        String range = "pitch must be from " + CameraTurn.PITCH_MIN + " to " + CameraTurn.PITCH_MAX;

        if (badPitch) throw new BridgeException("bad_args", range);
    }

    /** Find the target of the kind that look_at names, from the same fields as an interaction takes. */
    private Target locate(String kind, JsonObject args) throws BridgeException {
        JsonObject named = args.deepCopy();
        named.addProperty("target", kind);
        Targets.requireIdentity(named);

        return game.onClientThread(client -> Targets.locate(client, named));
    }

    /** Set the camera's targets from the arguments, and report whether any was set. */
    private static boolean turn(Client client, Integer yaw, Integer pitch, Target target) throws BridgeException {
        boolean turned = false;

        if (target != null) {
            client.setCameraYawTarget(yawToward(client, target));
            turned = true;
        }

        if (yaw != null) {
            client.setCameraYawTarget(yaw);
            turned = true;
        }

        if (pitch != null) {
            client.setCameraPitchTarget(pitch);
            turned = true;
        }

        return turned;
    }

    /** Get the yaw that faces the target from the local player. Throws {@code wrong_state} while there is no player. */
    private static int yawToward(Client client, Target target) throws BridgeException {
        Player local = client.getLocalPlayer();
        if (local == null) throw new BridgeException("wrong_state", "the client is not in the game");

        WorldPoint tile = target.tile(client);
        String refusal = target.label() + " is not in the world, so the camera cannot look at it";

        if (tile == null) throw new BridgeException("not_found", refusal);

        LocalPoint there = LocalPoint.fromWorld(client.getTopLevelWorldView(), tile);
        if (there == null) throw new BridgeException("not_found", target.label() + " is outside the loaded scene");

        return CameraTurn.yawToward(local.getLocalLocation(), there);
    }

    /** Wait until the yaw and the pitch have stopped changing for a few frames, within the bound. */
    private void settle() throws BridgeException {
        long last = -1;
        int still = 0;

        for (int frame = 0; frame < TURN_FRAMES && still < SETTLED_FRAMES; frame++) {
            game.nextFrame();
            long now = game.onClientThread(Camera::angles);
            still = now == last ? still + 1 : 0;
            last = now;
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
