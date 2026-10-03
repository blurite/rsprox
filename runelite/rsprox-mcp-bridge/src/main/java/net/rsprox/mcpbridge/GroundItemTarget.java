package net.rsprox.mcpbridge;

import com.google.gson.JsonObject;
import java.awt.Shape;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import net.runelite.api.Client;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.Tile;
import net.runelite.api.TileItem;
import net.runelite.api.WorldView;
import net.runelite.api.coords.WorldPoint;

/**
 * An item on the ground, named by its id and its world tile. Its menu entries carry the id as the
 * identifier and the scene tile as the parameters.
 */
final class GroundItemTarget extends Target {
    /** The menu actions that perform an option on a ground item. */
    private static final Set<MenuAction> FAMILY = EnumSet.of(
        MenuAction.GROUND_ITEM_FIRST_OPTION,
        MenuAction.GROUND_ITEM_SECOND_OPTION,
        MenuAction.GROUND_ITEM_THIRD_OPTION,
        MenuAction.GROUND_ITEM_FOURTH_OPTION,
        MenuAction.GROUND_ITEM_FIFTH_OPTION,
        MenuAction.EXAMINE_ITEM_GROUND);

    /** The id of the item. */
    private final int id;

    /** The name of the item. */
    private final String name;

    /** The world tile the item lies on. */
    private final WorldPoint at;

    /** Create the target for the item with the id on the world tile, named as the client names it. */
    GroundItemTarget(int id, String name, WorldPoint at, String option) {
        super(option);
        this.id = id;
        this.name = name;
        this.at = at;
    }

    /** Get the words that name the item. */
    @Override
    String label() {
        return "ground item " + id + " (" + name + ")";
    }

    /** Describe the item with its id, name and world tile. */
    @Override
    JsonObject describe() {
        JsonObject out = described("OPOBJ", "ground_item");
        out.addProperty("id", id);
        out.addProperty("name", name);
        out.addProperty("x", at.getX());
        out.addProperty("y", at.getY());
        out.addProperty("option", option);

        return out;
    }

    /** Get the outline of the item's tile, or null while the item is gone or the tile is not in front of the camera. */
    @Override
    Shape shape(Client client) {
        WorldView view = client.getTopLevelWorldView();
        Tile tile = Scenes.tile(view, at.getX(), at.getY());

        if (tile == null) return null;

        List<TileItem> items = tile.getGroundItems();
        if (items == null || items.stream().noneMatch(item -> item.getId() == id)) return null;

        return Scenes.tilePolygon(client, view, at.getX() - view.getBaseX(), at.getY() - view.getBaseY());
    }

    /** Determine if the entry is this option on the item with this id on this tile. */
    @Override
    boolean matches(MenuEntry entry, Client client) {
        WorldView view = client.getTopLevelWorldView();
        int sceneX = at.getX() - view.getBaseX();
        int sceneY = at.getY() - view.getBaseY();
        boolean sameTile = entry.getParam0() == sceneX && entry.getParam1() == sceneY;

        return FAMILY.contains(entry.getType()) && entry.getIdentifier() == id && sameTile && sameOption(entry);
    }

    /** Get the world tile the item lies on. */
    @Override
    WorldPoint tile(Client client) {
        return at;
    }
}
