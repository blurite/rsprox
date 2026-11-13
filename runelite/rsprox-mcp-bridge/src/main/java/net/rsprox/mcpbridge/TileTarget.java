package net.rsprox.mcpbridge;

import com.google.gson.JsonObject;
import java.awt.Shape;
import net.runelite.api.Client;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.Point;
import net.runelite.api.Tile;
import net.runelite.api.WorldView;
import net.runelite.api.coords.WorldPoint;

/**
 * A world tile to walk to. The client's walk entry names no tile: it walks to the tile the mouse
 * hovers, so the entry matches only while that is this tile.
 */
final class TileTarget extends Target {
    /** The option the client offers on a tile. */
    static final String WALK_HERE = "Walk here";

    /** The world tile. */
    private final WorldPoint at;

    /** Create the target for walking to the world tile. */
    TileTarget(WorldPoint at) {
        super(WALK_HERE);
        this.at = at;
    }

    /** Get the words that name the tile. */
    @Override
    String label() {
        return "tile " + Scenes.words(at.getX(), at.getY());
    }

    /** Describe the tile with its world coordinates. */
    @Override
    JsonObject describe() {
        JsonObject out = described("MOVE_GAMECLICK", "tile");
        out.addProperty("x", at.getX());
        out.addProperty("y", at.getY());

        return out;
    }

    /** Get the outline of the tile, or null while it is not in front of the camera. */
    @Override
    Shape shape(Client client) {
        WorldView view = client.getTopLevelWorldView();
        if (!Scenes.isLoaded(view, at.getX(), at.getY())) return null;

        return Scenes.tilePolygon(client, view, at.getX() - view.getBaseX(), at.getY() - view.getBaseY());
    }

    /** Determine if the entry is the walk entry while the client's hovered tile is this tile. */
    @Override
    boolean matches(MenuEntry entry, Client client) {
        if (entry.getType() != MenuAction.WALK) return false;

        WorldView view = client.getTopLevelWorldView();
        Tile hovered = view.getSelectedSceneTile();
        Point wanted = new Point(at.getX() - view.getBaseX(), at.getY() - view.getBaseY());

        return hovered != null && hovered.getSceneLocation().equals(wanted);
    }

    /** Get the world tile. */
    @Override
    WorldPoint tile(Client client) {
        return at;
    }
}
