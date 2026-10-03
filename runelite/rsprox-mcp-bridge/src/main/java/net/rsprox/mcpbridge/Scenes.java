package net.rsprox.mcpbridge;

import java.awt.Polygon;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.ObjectComposition;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.Tile;
import net.runelite.api.TileObject;
import net.runelite.api.WorldView;
import net.runelite.api.coords.LocalPoint;

/**
 * Reads of the top-level world view that the entity listing and the interactions share. World
 * coordinates are the scene coordinates plus the base of the view, which is what the packets carry.
 */
final class Scenes {
    /** The options of a ground item by position. The RuneLite API exposes none, and the client takes with the third. */
    static final String[] GROUND_ITEM_OPTIONS = {null, null, "Take", null, null};

    /** Not for instances. */
    private Scenes() {
        //
    }

    /** Determine if the world coordinates lie in the scene that the view has loaded. */
    static boolean isLoaded(WorldView view, int x, int y) {
        int sceneX = x - view.getBaseX();
        int sceneY = y - view.getBaseY();

        return sceneX >= 0 && sceneY >= 0 && sceneX < view.getSizeX() && sceneY < view.getSizeY();
    }

    /** Get the tile at the world coordinates on the plane in view, or null when the scene has none there. */
    static Tile tile(WorldView view, int x, int y) {
        if (!isLoaded(view, x, y)) return null;

        return view.getScene().getTiles()[view.getPlane()][x - view.getBaseX()][y - view.getBaseY()];
    }

    /** Get the objects on the tile that can have options: its game, wall, ground and decorative objects. */
    static List<TileObject> objects(Tile tile) {
        List<TileObject> out = new ArrayList<>();
        GameObject[] gameObjects = tile.getGameObjects();

        for (GameObject object : gameObjects == null ? new GameObject[0] : gameObjects) {
            if (object != null) out.add(object);
        }

        if (tile.getWallObject() != null) out.add(tile.getWallObject());

        if (tile.getGroundObject() != null) out.add(tile.getGroundObject());

        if (tile.getDecorativeObject() != null) out.add(tile.getDecorativeObject());

        return out;
    }

    /**
     * Get the scene tile that an option on the object names: the minimum tile of a game object, which
     * may cover several, and the tile it stands on for any other object.
     */
    static Point origin(TileObject object, Tile tile) {
        return object instanceof GameObject ? ((GameObject) object).getSceneMinLocation() : tile.getSceneLocation();
    }

    /** Get the definition that the client shows for the object id, or null while it shows none. */
    static ObjectComposition shown(Client client, int id) {
        ObjectComposition composition = client.getObjectDefinition(id);

        return composition.getImpostorIds() == null ? composition : composition.getImpostor();
    }

    /** Get the names of the options that are offered, in order. */
    static List<String> offered(String[] options) {
        List<String> out = new ArrayList<>();

        for (String option : options == null ? new String[0] : options) {
            if (option != null && !option.isEmpty()) out.add(option);
        }

        return out;
    }

    /** Get the part of the canvas that shows the 3D scene. */
    static Rectangle viewport(Client client) {
        return new Rectangle(
            client.getViewportXOffset(),
            client.getViewportYOffset(),
            client.getViewportWidth(),
            client.getViewportHeight());
    }

    /** Get the outline of the scene tile on the canvas, or null while the tile is not in front of the camera. */
    static Polygon tilePolygon(Client client, WorldView view, int sceneX, int sceneY) {
        return Perspective.getCanvasTilePoly(client, LocalPoint.fromScene(sceneX, sceneY, view));
    }

    /** Write the world tile as its coordinates in brackets. */
    static String words(int x, int y) {
        return "(" + x + ", " + y + ")";
    }
}
