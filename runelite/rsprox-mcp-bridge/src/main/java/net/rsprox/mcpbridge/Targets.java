package net.rsprox.mcpbridge;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
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
import net.runelite.api.widgets.ComponentID;
import net.runelite.api.widgets.Widget;

/**
 * Finds the target that the arguments of an interaction name in the client's own state, and checks
 * that it offers the option. The client would send a packet for a target or an option it does not
 * have, so both are refused before anything is clicked.
 */
final class Targets {
    /** The arguments that each kind of target needs. */
    private static final Map<String, List<String>> REQUIRED = Map.of(
        "npc", List.of("index", "option"),
        "object", List.of("id", "x", "y", "option"),
        "ground_item", List.of("id", "x", "y", "option"),
        "player", List.of("index", "option"),
        "widget", List.of("widget", "option"),
        "dialog", List.of("option"),
        "tile", List.of("x", "y"));

    /** The option that every NPC, object and ground item has after its own five. */
    private static final String EXAMINE = "Examine";

    /** The position of the examine option, after the five options of the target. */
    private static final int EXAMINE_POSITION = 5;

    /** The number of options a player can have. */
    private static final int PLAYER_OPTIONS = 8;

    /** The option the client offers on a widget that continues a dialog. */
    private static final String CONTINUE = "Continue";

    /** The text of the widget that continues a dialog. */
    private static final String CONTINUE_TEXT = "Click here to continue";

    /** The deepest level of nested widgets that a search descends to. */
    private static final int WIDGET_DEPTH_LIMIT = 12;

    /** Not for instances. */
    private Targets() {
        //
    }

    /** A target as found, with the options the client says it has. */
    private static final class Found {
        /** The target. */
        final Target target;

        /** The options the target offers, in the client's order. */
        final List<String> options;

        /** Create the record of a found target. */
        Found(Target target, List<String> options) {
            this.target = target;
            this.options = options;
        }
    }

    /** Check that the kind of target has its arguments. Throws {@code bad_args} for the first that is missing. */
    static void requireArguments(JsonObject args) throws BridgeException {
        require(args, name -> true);
    }

    /** Check that the kind of target has the arguments that name it, whatever the option. Throws {@code bad_args}. */
    static void requireIdentity(JsonObject args) throws BridgeException {
        require(args, name -> !name.equals("option"));
    }

    /** Check that the kind of target has those of its arguments that pass the test. Throws {@code bad_args}. */
    private static void require(JsonObject args, Predicate<String> wanted) throws BridgeException {
        String kind = args.get("target").getAsString();
        List<String> required = REQUIRED.get(kind);

        if (required == null) throw new BridgeException("bad_args", "unknown target '" + kind + "'");

        for (String name : required) {
            JsonElement value = args.get(name);
            boolean missing = wanted.test(name) && (value == null || value.isJsonNull());

            if (missing) throw new BridgeException("bad_args", "target " + kind + " needs '" + name + "'");
        }
    }

    /**
     * Find the target that the arguments name and check that it offers the option. Throws
     * {@code not_found} for a target the client does not have or an option it does not offer.
     */
    static Target resolve(Client client, JsonObject args) throws BridgeException {
        Found found = find(client, args);
        Target target = found.target;
        String refusal = target.label() + " has no option '" + target.option + "'. It offers: " + found.options;

        if (found.options.stream().noneMatch(target.option::equalsIgnoreCase)) throw notFound(refusal);

        return target;
    }

    /**
     * Find the target that the arguments name, whatever its options. Throws {@code not_found} when the
     * client does not have it.
     */
    static Target locate(Client client, JsonObject args) throws BridgeException {
        return find(client, args).target;
    }

    /** Find the target of the kind the arguments name, with the options the client says it has. */
    private static Found find(Client client, JsonObject args) throws BridgeException {
        String option = Ops.nullToEmpty(Ops.optionalString(args, "option"));

        switch (args.get("target").getAsString()) {
            case "npc":
                return npc(client, args.get("index").getAsInt(), option);
            case "player":
                return player(client, args.get("index").getAsInt(), option);
            case "widget":
                return widget(client, args.get("widget").getAsString(), option);
            case "dialog":
                return dialog(client, option);
            case "object":
                return object(client, args.get("id").getAsInt(), tile(client, args), option);
            case "ground_item":
                return groundItem(client, args.get("id").getAsInt(), tile(client, args), option);
            default:
                return new Found(new TileTarget(tile(client, args)), List.of(TileTarget.WALK_HERE));
        }
    }

    /** Get the world tile that the arguments name. Throws {@code not_found} for one outside the loaded scene. */
    private static WorldPoint tile(Client client, JsonObject args) throws BridgeException {
        int x = args.get("x").getAsInt();
        int y = args.get("y").getAsInt();
        WorldView view = client.getTopLevelWorldView();
        String refusal = "tile " + Scenes.words(x, y) + " is not in the loaded scene";

        if (!Scenes.isLoaded(view, x, y)) throw notFound(refusal);

        return new WorldPoint(x, y, view.getPlane());
    }

    /** Find the NPC with the index in the client's list of NPCs. */
    private static Found npc(Client client, int index, String option) throws BridgeException {
        NPC npc = client.getTopLevelWorldView().npcs().byIndex(index);
        NPCComposition composition = npc == null ? null : npc.getTransformedComposition();

        if (composition == null) throw notFound("no npc with index " + index + " is in the client's view");

        String name = Ops.nullToEmpty(composition.getName());

        return new Found(new NpcTarget(index, npc.getId(), name, option), withExamine(composition.getActions()));
    }

    /** Find the player with the index in the client's list of players. */
    private static Found player(Client client, int index, String option) throws BridgeException {
        Player player = client.getTopLevelWorldView().players().byIndex(index);
        if (player == null) throw notFound("no player with index " + index + " is in the client's view");

        String[] options = Arrays.copyOf(client.getPlayerOptions(), PLAYER_OPTIONS);

        return new Found(new PlayerTarget(index, Ops.nullToEmpty(player.getName()), option), Scenes.offered(options));
    }

    /**
     * Find the visible widget with the reference. Besides its own ops, the client may offer to continue
     * a dialog on it.
     */
    private static Found widget(Client client, String ref, String option) throws BridgeException {
        Widget widget = Ops.visibleWidget(client, ref);
        List<String> options = new ArrayList<>(Scenes.offered(widget.getActions()));
        options.add(CONTINUE);

        return new Found(new WidgetTarget(ref, widget.getId(), widget.getIndex(), option), options);
    }

    /**
     * Find the widget of the open dialog that the option names: the one that continues it for
     * "continue", or the numbered or worded choice of a dialog with options.
     */
    private static Found dialog(Client client, String option) throws BridgeException {
        if (option.equalsIgnoreCase(CONTINUE)) return continueWidget(client);

        Widget options = client.getWidget(ComponentID.DIALOG_OPTION_OPTIONS);
        Widget[] choices = options == null || options.isHidden() ? null : options.getDynamicChildren();
        List<String> texts = new ArrayList<>();

        for (Widget choice : choices == null ? new Widget[0] : choices) {
            String text = Ops.nullToEmpty(choice.getText());
            boolean chosen = text.equalsIgnoreCase(option) || Integer.toString(choice.getIndex()).equals(option);

            if (chosen && Ops.isVisible(choice)) return found(choice);

            if (!text.isEmpty() && choice.getIndex() > 0) texts.add(choice.getIndex() + ". " + text);
        }

        throw notFound("no dialog option '" + option + "' is shown. It offers: " + texts);
    }

    /** Find the visible widget that continues a dialog. Throws {@code not_found} while no dialog waits. */
    private static Found continueWidget(Client client) throws BridgeException {
        for (Widget root : client.getWidgetRoots()) {
            Widget widget = firstVisible(root, Targets::continues, 0);
            if (widget != null) return found(widget);
        }

        throw notFound("no dialog is waiting to be continued");
    }

    /** Determine if the widget is the one that continues a dialog, by its text. */
    private static boolean continues(Widget widget) {
        return CONTINUE_TEXT.equalsIgnoreCase(Ops.nullToEmpty(widget.getText()).trim());
    }

    /** Build the found record of a dialog widget, which the client offers to continue. */
    private static Found found(Widget widget) {
        WidgetTarget target = new WidgetTarget(Ops.ref(widget), widget.getId(), widget.getIndex(), CONTINUE);

        return new Found(target, List.of(CONTINUE));
    }

    /** Find the first visible widget under the root, itself included, that passes the test. */
    private static Widget firstVisible(Widget root, Predicate<Widget> test, int depth) {
        if (root == null || root.isHidden() || depth > WIDGET_DEPTH_LIMIT) return null;

        if (test.test(root)) return root;

        Widget[] nested = root.getNestedChildren();

        for (Widget[] children : Arrays.asList(root.getStaticChildren(), root.getDynamicChildren(), nested)) {
            for (Widget child : children == null ? new Widget[0] : children) {
                Widget found = firstVisible(child, test, depth + 1);
                if (found != null) return found;
            }
        }

        return null;
    }

    /** Find the object that has the id, or shows as it, on the world tile. */
    private static Found object(Client client, int id, WorldPoint at, String option) throws BridgeException {
        WorldView view = client.getTopLevelWorldView();
        Tile tile = Scenes.tile(view, at.getX(), at.getY());

        for (TileObject object : tile == null ? List.<TileObject>of() : Scenes.objects(tile)) {
            ObjectComposition shown = Scenes.shown(client, object.getId());
            if (shown == null || (object.getId() != id && shown.getId() != id)) continue;

            Point origin = Scenes.origin(object, tile);
            int x = view.getBaseX() + origin.getX();
            int y = view.getBaseY() + origin.getY();
            WorldPoint world = new WorldPoint(x, y, at.getPlane());
            ObjectTarget target = new ObjectTarget(object.getId(), Ops.nullToEmpty(shown.getName()), world, option);

            return new Found(target, withExamine(shown.getActions()));
        }

        throw notFound("no object " + id + " is on tile " + Scenes.words(at.getX(), at.getY()));
    }

    /** Find the item with the id that lies on the world tile. */
    private static Found groundItem(Client client, int id, WorldPoint at, String option) throws BridgeException {
        Tile tile = Scenes.tile(client.getTopLevelWorldView(), at.getX(), at.getY());
        List<TileItem> items = tile == null ? null : tile.getGroundItems();

        for (TileItem item : items == null ? List.<TileItem>of() : items) {
            if (item.getId() != id) continue;

            String name = Ops.nullToEmpty(client.getItemDefinition(id).getName());

            return new Found(new GroundItemTarget(id, name, at, option), withExamine(Scenes.GROUND_ITEM_OPTIONS));
        }

        throw notFound("no ground item " + id + " is on tile " + Scenes.words(at.getX(), at.getY()));
    }

    /** Get the options offered among the five of a target, followed by the examine option that each such target has. */
    private static List<String> withExamine(String[] options) {
        String[] out = Arrays.copyOf(options == null ? new String[0] : options, EXAMINE_POSITION + 1);
        out[EXAMINE_POSITION] = EXAMINE;

        return Scenes.offered(out);
    }

    /** Build the failure for something the client does not have. */
    private static BridgeException notFound(String message) {
        return new BridgeException("not_found", message);
    }
}
