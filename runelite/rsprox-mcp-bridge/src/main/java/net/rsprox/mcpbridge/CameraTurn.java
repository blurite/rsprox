package net.rsprox.mcpbridge;

import com.google.gson.JsonObject;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;

/**
 * Turns the camera toward a world tile: first the yaw that puts the tile in front of the camera,
 * then, once the yaw has arrived and the tile is still not on screen, a middle pitch. Angles are in
 * the client's unit, which has {@link #FULL_TURN} to a full turn.
 */
final class CameraTurn {
    /** The number of angle units in a full turn. */
    private static final int FULL_TURN = 16384;

    /** The pitch that shows a good stretch of ground in front of the player. */
    private static final int PITCH_MIDDLE = 2048;

    /** The distance within which the yaw counts as having arrived at its target. */
    private static final int ARRIVED_WITHIN = 64;

    /** The yaw before the turn, or null while the camera has not been turned. */
    private Integer yawBefore;

    /** The yaw the camera was sent to, or null while it has not been turned. */
    private Integer yawTarget;

    /** The pitch before the camera was pitched, or null while it has not been. */
    private Integer pitchBefore;

    /** Get the yaw that puts the tile in front of the local player's camera. */
    static int yawToward(LocalPoint from, LocalPoint to) {
        double radians = Math.atan2(from.getX() - to.getX(), to.getY() - from.getY());
        int units = (int) Math.round(radians / (2 * Math.PI) * FULL_TURN);

        return Math.floorMod(units, FULL_TURN);
    }

    /**
     * Turn the camera one step further toward the tile and say what was done. The first call sends
     * the yaw; a later call, once the yaw has arrived, sends the pitch. Throws {@code wrong_state}
     * while the client has no local player.
     */
    String toward(Client client, WorldPoint tile) throws BridgeException {
        Player local = client.getLocalPlayer();
        if (local == null) throw new BridgeException("wrong_state", "the client is not in the game");

        if (yawTarget == null) return turnYaw(client, local, tile);

        if (pitchBefore == null && arrived(client)) return pitch(client);

        return "waiting for the camera, yaw " + client.getCameraYaw() + " of " + yawTarget;
    }

    /** Send the camera to the yaw that faces the tile. */
    private String turnYaw(Client client, Player local, WorldPoint tile) {
        LocalPoint there = LocalPoint.fromWorld(client.getTopLevelWorldView(), tile);
        yawBefore = client.getCameraYaw();
        yawTarget = there == null ? yawBefore : yawToward(local.getLocalLocation(), there);
        client.setCameraYawTarget(yawTarget);

        return "turned the camera from yaw " + yawBefore + " toward " + yawTarget;
    }

    /** Send the camera to the middle pitch. */
    private String pitch(Client client) {
        pitchBefore = client.getCameraPitch();
        client.setCameraPitchTarget(PITCH_MIDDLE);

        return "pitched the camera from " + pitchBefore + " toward " + PITCH_MIDDLE;
    }

    /** Determine if the camera's yaw is at its target, give or take a little. */
    private boolean arrived(Client client) {
        int away = Math.floorMod(client.getCameraYaw() - yawTarget, FULL_TURN);

        return Math.min(away, FULL_TURN - away) <= ARRIVED_WITHIN;
    }

    /** Describe the turn as its yaw and pitch, each "before -> target", or null while the camera was not turned. */
    JsonObject describe() {
        if (yawTarget == null) return null;

        JsonObject out = new JsonObject();
        out.addProperty("yaw", yawBefore + " -> " + yawTarget);

        if (pitchBefore != null) out.addProperty("pitch", pitchBefore + " -> " + PITCH_MIDDLE);

        return out;
    }
}
