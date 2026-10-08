package net.rsprox.mcpbridge;

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
    /** The kinds of target by name, each with the arguments that name it and the way to find it. */
    private static final Map<String, Kind> KINDS = Map.of(
        "npc", new Kind(Targets::npc, "index", "option"),
        "object", new Kind(Targets::object, "id", "x", "y", "option"),
        "ground_item", new Kind(Targets::groundItem, "id", "x", "y", "option"),
        "player", new Kind(Targets::player, "index", "option"),
        "widget", new Kind(Targets::widget, "widget", "option"),
        "dialog", new Kind(Targets::dialog, "option"),
        "tile", new Kind(Targets::walk, "x", "y"));

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

    /** Prevent the creation of instances. */
    private Targets() {
        //
    }

    /** The way to find the target of one kind. */
    private interface Finder {
        /** Find the target that the arguments name, with the options the client says it has. */
        Candidate find(Client client, JsonObject args, String option) throws BridgeException;
    }

    /** One kind of target: the way to find it and the arguments it needs. */
    private static final class Kind {
        /** The way to find a target of the kind. */
        final Finder finder;

        /** The names of the arguments that a target of the kind needs. */
        final List<String> required;

        /** Create the kind that the finder finds from the required arguments. */
        Kind(Finder finder, String... required) {
            this.finder = finder;
            this.required = List.of(required);
        }
    }

    /** A target as found, with the options the client says it has. */
    private static final class Candidate {
        /** The target. */
        final Target target;

        /** The options the target offers, in the client's order. */
        final List<String> options;

        /** Create the record of a found target. */
        Candidate(Target target, List<String> options) {
            this.target = target;
            this.options = options;
        }

        /** Determine if the target offers the option that is to be performed on it. */
        boolean offersOption() {
            return options.stream().anyMatch(offered -> Target.isSameOption(offered, target.option));
        }

        /** Build the failure for a target that does not offer the option. */
        BridgeException noOption() {
            return notFound(target.label() + " has no option '" + target.option + "'. It offers: " + options);
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

        for (String name : KINDS.get(kind).required) {
            boolean missing = wanted.test(name) && !args.has(name);

            if (missing) throw new BridgeException("bad_args", "target " + kind + " needs '" + name + "'");
        }
    }

    /**
     * Find the target that the arguments name and check that it offers the option. Throws
     * {@code not_found} for a target the client does not have or an option it does not offer.
     */
    static Target resolve(Client client, JsonObject args) throws BridgeException {
        Candidate found = find(client, args);
        if (!found.offersOption()) throw found.noOption();

        return found.target;
    }

    /**
     * Find the target that the arguments name, whatever its options. Throws {@code not_found} when the
     * client does not have it.
     */
    static Target locate(Client client, JsonObject args) throws BridgeException {
        return find(client, args).target;
    }

    /** Find the target of the kind the arguments name, with the options the client says it has. */
    private static Candidate find(Client client, JsonObject args) throws BridgeException {
        String option = Ops.nullToEmpty(Ops.optionalString(args, "option"));

        return KINDS.get(args.get("target").getAsString()).finder.find(client, args, option);
    }

    /** Find the tile to walk to, on which the client offers to walk. */
    private static Candidate walk(Client client, JsonObject args, String option) throws BridgeException {
        return new Candidate(new TileTarget(tile(client, args)), List.of(TileTarget.WALK_HERE));
    }

    /** Get the world tile that the arguments name. Throws {@code not_found} for one outside the loaded scene. */
    private static WorldPoint tile(Client client, JsonObject args) throws BridgeException {
        int x = args.get("x").getAsInt();
        int y = args.get("y").getAsInt();
        WorldView view = client.getTopLevelWorldView();

        if (!Scenes.isLoaded(view, x, y)) throw notFound("tile " + Scenes.words(x, y) + " is not in the loaded scene");

        return new WorldPoint(x, y, view.getPlane());
    }

    /** Find the NPC with the index in the client's list of NPCs. */
    private static Candidate npc(Client client, JsonObject args, String option) throws BridgeException {
        int index = args.get("index").getAsInt();
        NPC npc = client.getTopLevelWorldView().npcs().byIndex(index);
        NPCComposition composition = npc == null ? null : npc.getTransformedComposition();

        if (composition == null) throw notFound("no npc with index " + index + " is in the client's view");

        String name = Ops.nullToEmpty(composition.getName());

        return new Candidate(ActorTarget.npc(index, npc.getId(), name, option), withExamine(composition.getActions()));
    }

    /** Find the player with the index in the client's list of players. */
    private static Candidate player(Client client, JsonObject args, String option) throws BridgeException {
        int index = args.get("index").getAsInt();
        Player player = client.getTopLevelWorldView().players().byIndex(index);

        if (player == null) throw notFound("no player with index " + index + " is in the client's view");

        String[] options = Arrays.copyOf(client.getPlayerOptions(), PLAYER_OPTIONS);
        ActorTarget target = ActorTarget.player(index, Ops.nullToEmpty(player.getName()), option);

        return new Candidate(target, Scenes.offered(options));
    }

    /**
     * Find the visible widget with the reference. Besides its own ops, the client may offer to continue
     * a dialog on it.
     */
    private static Candidate widget(Client client, JsonObject args, String option) throws BridgeException {
        String ref = args.get("widget").getAsString();
        Widget widget = Ops.visibleWidget(client, ref);
        List<String> options = new ArrayList<>(Scenes.offered(widget.getActions()));
        options.add(CONTINUE);

        return new Candidate(new WidgetTarget(ref, widget.getId(), widget.getIndex(), option), options);
    }

    /**
     * Find the widget of the open dialog that the option names: the one that continues it for
     * "continue", or the numbered or worded choice of a dialog with options.
     */
    private static Candidate dialog(Client client, JsonObject args, String option) throws BridgeException {
        if (Target.isSameOption(option, CONTINUE)) return continueWidget(client);

        List<Widget> choices = choices(client);

        for (Widget choice : choices) {
            if (names(option, choice) && Ops.isVisible(choice)) return found(choice);
        }

        throw notFound("no dialog option '" + option + "' is shown. It offers: " + numbered(choices));
    }

    /** Get the widgets of the choices of the open dialog, or none while no dialog with options is open. */
    private static List<Widget> choices(Client client) {
        Widget options = client.getWidget(ComponentID.DIALOG_OPTION_OPTIONS);
        if (options == null || options.isHidden()) return List.of();

        Widget[] choices = options.getDynamicChildren();

        return choices == null ? List.of() : Arrays.asList(choices);
    }

    /** Determine if the option names the choice, by its text or by its number. */
    private static boolean names(String option, Widget choice) {
        return Target.isSameOption(option, choice.getText()) || Integer.toString(choice.getIndex()).equals(option);
    }

    /** Write each choice that has a text as its number and its text. The first child is the title of the dialog. */
    private static List<String> numbered(List<Widget> choices) {
        List<String> out = new ArrayList<>();

        for (Widget choice : choices) {
            String text = Offer.untagged(choice.getText());

            if (!text.isEmpty() && choice.getIndex() > 0) out.add(choice.getIndex() + ". " + text);
        }

        return out;
    }

    /** Find the visible widget that continues a dialog. Throws {@code not_found} while no dialog waits. */
    private static Candidate continueWidget(Client client) throws BridgeException {
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
    private static Candidate found(Widget widget) {
        WidgetTarget target = new WidgetTarget(Ops.ref(widget), widget.getId(), widget.getIndex(), CONTINUE);

        return new Candidate(target, List.of(CONTINUE));
    }

    /** Find the first visible widget under the root, itself included, that passes the test. */
    private static Widget firstVisible(Widget root, Predicate<Widget> test, int depth) {
        if (root == null || root.isHidden() || depth > Ops.WIDGET_DEPTH_LIMIT) return null;

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
    private static Candidate object(Client client, JsonObject args, String option) throws BridgeException {
        int id = args.get("id").getAsInt();
        WorldPoint at = tile(client, args);
        Tile tile = Scenes.tile(client.getTopLevelWorldView(), at.getX(), at.getY());

        if (tile == null) throw noObject(id, at);

        for (TileObject object : Scenes.objects(tile)) {
            ObjectComposition shown = Scenes.shown(client, object.getId());
            if (shown == null || (object.getId() != id && shown.getId() != id)) continue;

            String name = Ops.nullToEmpty(shown.getName());
            ObjectTarget target = new ObjectTarget(object.getId(), name, origin(client, object, tile), option);

            return new Candidate(target, withExamine(shown.getActions()));
        }

        throw noObject(id, at);
    }

    /** Get the world tile of origin of the object on the tile, on the plane in view. */
    private static WorldPoint origin(Client client, TileObject object, Tile tile) {
        WorldView view = client.getTopLevelWorldView();
        Point origin = Scenes.origin(object, tile);

        return new WorldPoint(view.getBaseX() + origin.getX(), view.getBaseY() + origin.getY(), view.getPlane());
    }

    /** Build the failure for an object that is not on the world tile. */
    private static BridgeException noObject(int id, WorldPoint at) {
        return notFound("no object " + id + " is on tile " + Scenes.words(at.getX(), at.getY()));
    }

    /** Find the item with the id that lies on the world tile. */
    private static Candidate groundItem(Client client, JsonObject args, String option) throws BridgeException {
        int id = args.get("id").getAsInt();
        WorldPoint at = tile(client, args);
        Tile tile = Scenes.tile(client.getTopLevelWorldView(), at.getX(), at.getY());

        if (tile == null || tile.getGroundItems() == null) throw noGroundItem(id, at);

        if (tile.getGroundItems().stream().noneMatch(item -> item.getId() == id)) throw noGroundItem(id, at);

        String name = Ops.nullToEmpty(client.getItemDefinition(id).getName());

        return new Candidate(new GroundItemTarget(id, name, at, option), withExamine(Scenes.GROUND_ITEM_OPTIONS));
    }

    /** Build the failure for a ground item that is not on the world tile. */
    private static BridgeException noGroundItem(int id, WorldPoint at) {
        return notFound("no ground item " + id + " is on tile " + Scenes.words(at.getX(), at.getY()));
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
