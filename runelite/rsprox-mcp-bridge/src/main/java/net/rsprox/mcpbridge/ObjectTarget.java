package net.rsprox.mcpbridge;

import com.google.gson.JsonObject;
import java.awt.Shape;
import java.util.EnumSet;
import java.util.Set;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.Point;
import net.runelite.api.Tile;
import net.runelite.api.TileObject;
import net.runelite.api.coords.WorldPoint;

/**
 * An object, named by its own id and its scene tile of origin. Its menu entries carry the id as the
 * identifier and the tile as the parameters.
 */
final class ObjectTarget extends Target {
    /** The menu actions that perform an option on an object. */
    private static final Set<MenuAction> FAMILY = EnumSet.of(
        MenuAction.GAME_OBJECT_FIRST_OPTION,
        MenuAction.GAME_OBJECT_SECOND_OPTION,
        MenuAction.GAME_OBJECT_THIRD_OPTION,
        MenuAction.GAME_OBJECT_FOURTH_OPTION,
        MenuAction.GAME_OBJECT_FIFTH_OPTION,
        MenuAction.EXAMINE_OBJECT);

    /** The object's own id. */
    private final int id;

    /** The name the client shows the object as. */
    private final String name;

    /** The world tile of origin. */
    private final WorldPoint origin;

    /** Create the target for the object with the id at its world tile of origin, shown with the name. */
    ObjectTarget(int id, String name, WorldPoint origin, String option) {
        super(option);
        this.id = id;
        this.name = name;
        this.origin = origin;
    }

    /** Get the words that name the object. */
    @Override
    String label() {
        return "object " + id + " (" + name + ")";
    }

    /** Describe the object with its id, name and world tile of origin. */
    @Override
    JsonObject describe() {
        JsonObject out = described("OPLOC", "object");
        out.addProperty("id", id);
        out.addProperty("name", name);
        out.addProperty("x", origin.getX());
        out.addProperty("y", origin.getY());
        out.addProperty("option", option);

        return out;
    }

    /**
     * Get the clickbox of the object, falling back to the hull of a game object and then to its tile,
     * or null while the object is gone or none of them is in front of the camera.
     */
    @Override
    Shape shape(Client client) {
        TileObject object = find(client);
        if (object == null) return null;

        Shape clickbox = object.getClickbox();
        if (clickbox != null) return clickbox;

        Shape hull = object instanceof GameObject ? ((GameObject) object).getConvexHull() : null;

        return hull != null ? hull : object.getCanvasTilePoly();
    }

    /** Determine if the entry is this option on the object with this id at this tile. */
    @Override
    boolean matches(MenuEntry entry, Client client) {
        boolean sameTile = Scenes.isOnTile(entry, client, origin);

        return FAMILY.contains(entry.getType()) && entry.getIdentifier() == id && sameTile && sameOption(entry);
    }

    /** Get the world tile of origin. */
    @Override
    WorldPoint tile(Client client) {
        return origin;
    }

    /** Find the object on its tile of origin, or null while it is not there. */
    private TileObject find(Client client) {
        Tile tile = Scenes.tile(client.getTopLevelWorldView(), origin.getX(), origin.getY());
        if (tile == null) return null;

        Point at = tile.getSceneLocation();

        for (TileObject object : Scenes.objects(tile)) {
            if (object.getId() == id && Scenes.origin(object, tile).equals(at)) return object;
        }

        return null;
    }
}
