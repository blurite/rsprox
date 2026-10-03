package net.rsprox.mcpbridge;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.NPCComposition;
import net.runelite.api.ObjectComposition;
import net.runelite.api.Player;
import net.runelite.api.Point;
import net.runelite.api.Tile;
import net.runelite.api.TileItem;
import net.runelite.api.TileObject;
import net.runelite.api.WorldView;
import net.runelite.api.coords.WorldPoint;

/** Lists what is near the local player, with the options each thing offers. */
final class Entities implements Ops.Op {
    /** The kinds of entity, in the order the result lists them. */
    private static final List<String> KINDS = List.of("npc", "object", "ground_item", "player");

    /** The gateway to the client and the threads it must be used on. */
    private final GameAccess game;

    /** Create the listing for the given game. */
    Entities(GameAccess game) {
        this.game = game;
    }

    /** List the entities that pass the kind, radius and name filters, nearest first, up to the limit. */
    @Override
    public JsonElement run(JsonObject args) throws BridgeException {
        Set<String> kinds = kinds(args);
        int radius = args.get("radius").getAsInt();
        String name = Ops.optionalString(args, "name");
        String needle = name == null ? null : name.toLowerCase(Locale.ROOT);
        int limit = args.get("limit").getAsInt();

        return game.onClientThread(client -> {
            Player local = client.getLocalPlayer();
            if (local == null) throw new BridgeException("wrong_state", "the client is not in the game");

            Survey survey = new Survey(client, local, radius, needle);

            if (kinds.contains("npc")) survey.addNpcs();

            if (kinds.contains("player")) survey.addPlayers();

            if (kinds.contains("object") || kinds.contains("ground_item")) survey.addTiles(kinds);

            return survey.result(limit);
        });
    }

    /** Get the kinds to list, which are all of them unless the arguments name some. */
    private static Set<String> kinds(JsonObject args) {
        Set<String> kinds = new LinkedHashSet<>();

        for (JsonElement kind : args.has("kinds") ? args.getAsJsonArray("kinds") : new JsonArray()) {
            kinds.add(kind.getAsString());
        }

        return kinds.isEmpty() ? new LinkedHashSet<>(KINDS) : kinds;
    }

    /** One entity that passed the filters. */
    private static final class Entry implements Comparable<Entry> {
        /** The kind of the entity. */
        final String kind;

        /** The distance from the local player in tiles, where a diagonal step counts as one. */
        final int distance;

        /** The index or id that orders entities at the same distance. */
        final int key;

        /** The world x of the entity. */
        final int x;

        /** The world y of the entity. */
        final int y;

        /** The entity as the result lists it. */
        final JsonObject described;

        /** Create the record of one entity. */
        Entry(String kind, int distance, int key, int x, int y, JsonObject described) {
            this.kind = kind;
            this.distance = distance;
            this.key = key;
            this.x = x;
            this.y = y;
            this.described = described;
        }

        /** Order the nearest first, then by index or id, then by tile, so a listing is stable. */
        @Override
        public int compareTo(Entry other) {
            if (distance != other.distance) return Integer.compare(distance, other.distance);

            if (key != other.key) return Integer.compare(key, other.key);

            if (x != other.x) return Integer.compare(x, other.x);

            return Integer.compare(y, other.y);
        }
    }

    /** Collects the entities around the local player on its plane, in the top-level world view. */
    private static final class Survey {
        /** The entities collected so far. */
        private final List<Entry> found = new ArrayList<>();

        /** The game client. */
        private final Client client;

        /** The top-level world view. */
        private final WorldView view;

        /** The local player. */
        private final Player local;

        /** The tile of the local player. */
        private final WorldPoint origin;

        /** The greatest distance from the local player to keep. */
        private final int radius;

        /** The lower-case text a name must hold, or null to keep every name. */
        private final String needle;

        /** Create a survey around the local player with the given filters. */
        Survey(Client client, Player local, int radius, String needle) {
            this.client = client;
            this.view = client.getTopLevelWorldView();
            this.local = local;
            this.origin = local.getWorldLocation();
            this.radius = radius;
            this.needle = needle;
        }

        /** Collect the NPCs that the client shows. */
        void addNpcs() {
            for (NPC npc : view.npcs()) {
                NPCComposition composition = npc.getTransformedComposition();
                if (composition == null) continue;

                WorldPoint at = npc.getWorldLocation();
                JsonObject described = new JsonObject();
                described.addProperty("index", npc.getIndex());
                described.addProperty("id", npc.getId());
                described.addProperty("name", Ops.nullToEmpty(composition.getName()));
                described.addProperty("x", at.getX());
                described.addProperty("y", at.getY());
                described.add("options", strings(Scenes.offered(composition.getActions())));
                add("npc", npc.getIndex(), at.getX(), at.getY(), described);
            }
        }

        /** Collect the players other than the local one. */
        void addPlayers() {
            JsonArray options = strings(Scenes.offered(client.getPlayerOptions()));

            for (Player player : view.players()) {
                if (player == local) continue;

                WorldPoint at = player.getWorldLocation();
                JsonObject described = new JsonObject();
                described.addProperty("index", player.getId());
                described.addProperty("name", Ops.nullToEmpty(player.getName()));
                described.addProperty("x", at.getX());
                described.addProperty("y", at.getY());
                described.add("options", options);
                add("player", player.getId(), at.getX(), at.getY(), described);
            }
        }

        /** Collect the objects and the ground items of the asked kinds on every tile within the radius. */
        void addTiles(Set<String> kinds) {
            int centreX = origin.getX() - view.getBaseX();
            int centreY = origin.getY() - view.getBaseY();
            Tile[][] plane = view.getScene().getTiles()[view.getPlane()];

            for (int x = Math.max(0, centreX - radius); x <= Math.min(view.getSizeX() - 1, centreX + radius); x++) {
                for (int y = Math.max(0, centreY - radius); y <= Math.min(view.getSizeY() - 1, centreY + radius); y++) {
                    Tile tile = plane[x][y];
                    if (tile == null) continue;

                    if (kinds.contains("object")) addObjects(tile);

                    if (kinds.contains("ground_item")) addGroundItems(tile);
                }
            }
        }

        /** Collect the objects that have options and whose tile of origin is this tile, so each is listed once. */
        private void addObjects(Tile tile) {
            Point at = tile.getSceneLocation();

            for (TileObject object : Scenes.objects(tile)) {
                if (!Scenes.origin(object, tile).equals(at)) continue;

                ObjectComposition shown = Scenes.shown(client, object.getId());
                List<String> options = shown == null ? List.of() : Scenes.offered(shown.getActions());

                if (options.isEmpty()) continue;

                JsonObject described = new JsonObject();
                described.addProperty("id", object.getId());
                described.addProperty("name", Ops.nullToEmpty(shown.getName()));
                described.addProperty("x", worldX(at));
                described.addProperty("y", worldY(at));
                described.add("options", strings(options));
                add("object", object.getId(), worldX(at), worldY(at), described);
            }
        }

        /** Collect the items that lie on the tile. */
        private void addGroundItems(Tile tile) {
            List<TileItem> items = tile.getGroundItems();
            if (items == null) return;

            Point at = tile.getSceneLocation();

            for (TileItem item : items) {
                JsonObject described = new JsonObject();
                described.addProperty("id", item.getId());
                described.addProperty("name", Ops.nullToEmpty(client.getItemDefinition(item.getId()).getName()));
                described.addProperty("quantity", item.getQuantity());
                described.addProperty("x", worldX(at));
                described.addProperty("y", worldY(at));
                described.add("options", strings(Scenes.offered(Scenes.GROUND_ITEM_OPTIONS)));
                add("ground_item", item.getId(), worldX(at), worldY(at), described);
            }
        }

        /** Keep the entity when its tile is within the radius and its name holds the needle. */
        private void add(String kind, int key, int x, int y, JsonObject described) {
            int distance = Math.max(Math.abs(x - origin.getX()), Math.abs(y - origin.getY()));
            if (distance > radius) return;

            String name = described.get("name").getAsString().toLowerCase(Locale.ROOT);
            if (needle != null && !name.contains(needle)) return;

            found.add(new Entry(kind, distance, key, x, y, described));
        }

        /** Get the world x of the scene tile. */
        private int worldX(Point scene) {
            return view.getBaseX() + scene.getX();
        }

        /** Get the world y of the scene tile. */
        private int worldY(Point scene) {
            return view.getBaseY() + scene.getY();
        }

        /** Build the result from the nearest entities, up to the limit, with one list per kind. */
        JsonObject result(int limit) {
            Collections.sort(found);
            JsonObject out = new JsonObject();
            JsonObject at = new JsonObject();
            at.addProperty("x", origin.getX());
            at.addProperty("y", origin.getY());
            at.addProperty("plane", origin.getPlane());
            out.add("origin", at);

            for (String kind : KINDS) {
                out.add(kind + "s", new JsonArray());
            }

            for (Entry entity : found.subList(0, Math.min(limit, found.size()))) {
                out.getAsJsonArray(entity.kind + "s").add(entity.described);
            }

            out.addProperty("truncated", found.size() > limit);

            return out;
        }
    }

    /** Build a JSON array of the values. */
    private static JsonArray strings(List<String> values) {
        JsonArray out = new JsonArray();

        for (String value : values) {
            out.add(value);
        }

        return out;
    }
}
