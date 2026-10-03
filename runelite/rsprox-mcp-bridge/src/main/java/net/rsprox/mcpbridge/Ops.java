package net.rsprox.mcpbridge;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.MenuEntry;
import net.runelite.api.Player;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.widgets.Widget;

/** The operations rsprox can ask for, by name. Coordinates are canvas units everywhere. */
final class Ops {
    /** The shape of a widget reference: a group, a child and an optional dynamic child index. */
    private static final Pattern WIDGET_REF = Pattern.compile("(\\d+):(\\d+)(?:\\[(\\d+)])?");

    /** The message for a widget reference that does not have that shape. */
    private static final String WIDGET_REF_SHAPE = "widget must look like \"558:7\" or \"558:7[3]\"";

    /** The deepest level of nested widgets that a walk or a search descends to. */
    static final int WIDGET_DEPTH_LIMIT = 12;

    /** The pause between two reads of the game state while a login is awaited. */
    private static final int LOGIN_POLL_MS = 100;

    /** The message for a login that put the client back on the login screen. */
    private static final String LOGIN_REFUSED = "the login was refused; the client is back on the login screen";

    /** One operation that rsprox can ask for. */
    interface Op {
        /** Run the op with the arguments that rsprox forwarded. */
        JsonElement run(JsonObject args) throws BridgeException;
    }

    /** The gateway to the client and the threads it must be used on. */
    private final GameAccess game;

    /** The real mouse of the client. */
    private final Mouse mouse;

    /** The ops by the name rsprox calls them. */
    private final Map<String, Op> table = new HashMap<>();

    /** The lock that an op holds for as long as it drives the one mouse and keyboard of the client. */
    private final Object input = new Object();

    /** Create the op table for the given game. */
    Ops(GameAccess game) {
        this.game = game;
        this.mouse = new Mouse(game);
        reading("state", this::state);
        reading("widgets", this::widgets);
        reading("vars", this::vars);
        reading("screenshot", this::screenshot);
        reading("login", this::login);
        reading("entities", new Entities(game));
        driving("click", this::click);
        driving("type", this::type);
        driving("interact", new Interact(game, mouse));
        driving("camera", Camera::turns, new Camera(game));
    }

    /** Register an op that produces no input, which runs beside any other op. */
    private void reading(String name, Op op) {
        table.put(name, op);
    }

    /** Register an op that produces input, which runs alone among such ops from its start to its end. */
    private void driving(String name, Op op) {
        driving(name, args -> true, op);
    }

    /** Register an op that produces input for the arguments that pass the test, and only reads for the others. */
    private void driving(String name, Predicate<JsonObject> drives, Op op) {
        table.put(name, args -> drives.test(args) ? alone(op, args) : op.run(args));
    }

    /**
     * Run the op while no other op produces input. Two ops that move the pointer at once would each
     * click where the other left it, and the click guard of one would judge the click of the other.
     */
    private JsonElement alone(Op op, JsonObject args) throws BridgeException {
        synchronized (input) {
            return op.run(args);
        }
    }

    /** Run the named op. Throws {@code bad_args} for a name that is not in the table. */
    JsonElement run(String op, JsonObject args) throws BridgeException {
        Op handler = table.get(op);
        if (handler == null) throw new BridgeException("bad_args", "unknown op '" + op + "'");

        return handler.run(args);
    }

    /** Describe the game state, the canvas, the local player and the right-click menu. */
    private JsonElement state(JsonObject args) throws BridgeException {
        return game.onClientThread(client -> {
            JsonObject out = new JsonObject();
            out.addProperty("gameState", client.getGameState().name());
            out.addProperty("tick", client.getTickCount());
            Canvas canvas = client.getCanvas();
            out.add("canvas", ints(canvas.getWidth(), canvas.getHeight()));
            out.addProperty("world", client.getWorld());

            Player player = client.getLocalPlayer();
            if (player == null) {
                out.add("player", JsonNull.INSTANCE);
            } else {
                WorldPoint at = player.getWorldLocation();
                JsonObject described = new JsonObject();
                described.addProperty("name", player.getName());
                described.addProperty("x", at.getX());
                described.addProperty("y", at.getY());
                described.addProperty("plane", at.getPlane());
                out.add("player", described);
            }

            JsonObject menu = new JsonObject();
            menu.addProperty("open", client.isMenuOpen());
            JsonArray entries = new JsonArray();

            for (MenuEntry entry : client.getMenu().getMenuEntries()) {
                JsonObject described = new JsonObject();
                described.addProperty("option", entry.getOption());
                described.addProperty("target", entry.getTarget());
                described.addProperty("type", entry.getType().name());
                described.addProperty("id", entry.getIdentifier());
                described.addProperty("p0", entry.getParam0());
                described.addProperty("p1", entry.getParam1());
                entries.add(described);
            }

            menu.add("entries", entries);
            out.add("menu", menu);

            return out;
        });
    }

    /** List the widgets that pass the group, text and hidden filters, up to the limit. */
    private JsonElement widgets(JsonObject args) throws BridgeException {
        Integer group = optionalInt(args, "group");
        String text = optionalString(args, "text");
        String needle = text == null ? null : text.toLowerCase(Locale.ROOT);
        boolean includeHidden = optionalBoolean(args, "hidden");
        int limit = args.get("limit").getAsInt();

        return game.onClientThread(client -> {
            WidgetWalk walk = new WidgetWalk(group, needle, includeHidden, limit);
            Set<Integer> roots = new LinkedHashSet<>();

            for (Widget root : client.getWidgetRoots()) {
                if (root == null) continue;

                roots.add(root.getId() >>> 16);
                walk.visit(root, 0);
            }

            JsonObject out = new JsonObject();
            JsonArray rootGroups = new JsonArray();

            for (int root : roots) {
                rootGroups.add(root);
            }

            out.add("roots", rootGroups);
            out.add("widgets", walk.found);
            out.addProperty("truncated", walk.truncated);

            return out;
        });
    }

    /** Lists the widgets that have something to read or click: text, a name or actions. */
    private static final class WidgetWalk {
        /** The widgets collected so far. */
        final JsonArray found = new JsonArray();

        /** Whether the limit cut the walk short. */
        boolean truncated;

        /** The interface group to keep, or null to keep every group. */
        private final Integer group;

        /** The lower-case text to look for, or null to keep every widget. */
        private final String needle;

        /** Whether hidden widgets are walked too. */
        private final boolean includeHidden;

        /** The most widgets to collect. */
        private final int limit;

        /** Create a walk with the given filters. */
        WidgetWalk(Integer group, String needle, boolean includeHidden, int limit) {
            this.group = group;
            this.needle = needle;
            this.includeHidden = includeHidden;
            this.limit = limit;
        }

        /** Collect the widget and walk its children, unless it is hidden from this walk or too deep. */
        void visit(Widget widget, int depth) {
            if (widget == null || truncated || depth > WIDGET_DEPTH_LIMIT) return;

            boolean hidden = widget.isHidden();
            if (hidden && !includeHidden) return;

            collect(widget, hidden);
            visitAll(widget.getStaticChildren(), depth);
            visitAll(widget.getDynamicChildren(), depth);
            visitAll(widget.getNestedChildren(), depth);
        }

        /** Add the widget when it passes the filters, or mark the walk truncated once the limit is reached. */
        private void collect(Widget widget, boolean hidden) {
            if (group != null && widget.getId() >>> 16 != group) return;

            JsonObject described = describe(widget, hidden);
            if (described == null) return;

            if (found.size() >= limit) {
                truncated = true;

                return;
            }

            found.add(described);
        }

        /** Visit each of the children, one level deeper. */
        private void visitAll(Widget[] children, int depth) {
            if (children == null) return;

            for (Widget child : children) {
                visit(child, depth + 1);
            }
        }

        /** Describe the widget. Returns null when it has nothing to read or click, or does not hold the needle. */
        private JsonObject describe(Widget widget, boolean hidden) {
            String text = nullToEmpty(widget.getText());
            String name = nullToEmpty(widget.getName());
            JsonArray actions = new JsonArray();
            String[] raw = widget.getActions();

            for (String action : raw == null ? new String[0] : raw) {
                if (action == null || action.isEmpty()) continue;

                actions.add(action);
            }

            if (text.isEmpty() && name.isEmpty() && actions.size() == 0) return null;

            if (needle != null && !contains(text) && !contains(name) && !contains(actions.toString())) return null;

            Rectangle bounds = widget.getBounds();
            JsonObject out = new JsonObject();
            out.addProperty("id", ref(widget));
            out.addProperty("text", text);
            out.addProperty("name", name);
            out.add("actions", actions);
            out.add("bounds", ints(bounds.x, bounds.y, bounds.width, bounds.height));
            out.add("click", ints(centre(bounds).x, centre(bounds).y));
            out.addProperty("type", widget.getType());
            out.addProperty("hidden", hidden);

            return out;
        }

        /** Determine if the haystack holds the needle, ignoring case. */
        private boolean contains(String haystack) {
            return haystack.toLowerCase(Locale.ROOT).contains(needle);
        }
    }

    /** Read the requested player variables, varbits and client variables. */
    private JsonElement vars(JsonObject args) throws BridgeException {
        int[] varps = optionalInts(args, "varps");
        int[] varbits = optionalInts(args, "varbits");
        int[] varcInts = optionalInts(args, "varcInts");
        int[] varcStrs = optionalInts(args, "varcStrs");

        return game.onClientThread(client -> {
            JsonObject out = new JsonObject();
            out.add("varps", readVars("varp", varps, id -> number(client.getVarpValue(id))));
            out.add("varbits", readVars("varbit", varbits, id -> number(client.getVarbitValue(id))));
            out.add("varcInts", readVars("varcInt", varcInts, id -> number(client.getVarcIntValue(id))));
            out.add("varcStrs", readVars("varcStr", varcStrs, id -> string(client.getVarcStrValue(id))));

            return out;
        });
    }

    /** A read of one kind of client variable. */
    private interface VarReader {
        /** Read the variable with the given id. */
        JsonElement read(int id);
    }

    /** Read each id through the reader, keyed by id. Throws {@code not_found} for an id the client does not have. */
    private static JsonObject readVars(String kind, int[] ids, VarReader reader) throws BridgeException {
        JsonObject out = new JsonObject();

        for (int id : ids) {
            try {
                out.add(Integer.toString(id), reader.read(id));
            } catch (RuntimeException e) {
                // The client indexes its tables with the id and throws for one that does not exist.
                throw new BridgeException("not_found", kind + " " + id + " could not be read: " + e);
            }
        }

        return out;
    }

    /** Capture the next frame as a PNG that has the size of the canvas. */
    private JsonElement screenshot(JsonObject args) throws BridgeException {
        BufferedImage frame = game.frame();
        Dimension canvas = game.onClientThread(client -> client.getCanvas().getSize());
        int width = canvas.width;
        int height = canvas.height;

        if (width <= 0 || height <= 0) throw new BridgeException("wrong_state", "the canvas has no size yet");

        // A high-density display renders more pixels than the canvas has units. Clicks are in canvas
        // units, so the image is brought to canvas size to make a pixel and a click coordinate the same thing.
        BufferedImage sized = frame;

        if (frame.getWidth() != width || frame.getHeight() != height) {
            sized = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = sized.createGraphics();
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            graphics.drawImage(frame, 0, 0, width, height, null);
            graphics.dispose();
        }

        ByteArrayOutputStream png = new ByteArrayOutputStream();

        try {
            ImageIO.write(sized, "png", png);
        } catch (IOException e) {
            throw new BridgeException("internal", "could not encode the frame: " + e);
        }

        JsonObject out = new JsonObject();
        out.addProperty("png", Base64.getEncoder().encodeToString(png.toByteArray()));
        out.addProperty("width", width);
        out.addProperty("height", height);

        return out;
    }

    /** Click the canvas at the given coordinates, or at the centre of the given widget. */
    private JsonElement click(JsonObject args) throws BridgeException {
        boolean right = "right".equals(optionalString(args, "button"));
        Point point = clickPoint(args);
        mouse.hover(point.x, point.y);
        mouse.click(point.x, point.y, right ? Mouse.Button.RIGHT : Mouse.Button.LEFT);

        JsonObject out = new JsonObject();
        out.addProperty("x", point.x);
        out.addProperty("y", point.y);

        return out;
    }

    /**
     * Get the canvas point to click: the centre of the widget when one is named, and the x and y
     * otherwise. Throws {@code bad_args} when neither is given.
     */
    private Point clickPoint(JsonObject args) throws BridgeException {
        String widget = optionalString(args, "widget");
        if (widget != null) return game.onClientThread(client -> centre(visibleWidget(client, widget).getBounds()));

        Integer x = optionalInt(args, "x");
        Integer y = optionalInt(args, "y");

        if (x == null || y == null) throw new BridgeException("bad_args", "pass either x and y, or widget");

        return new Point(x, y);
    }

    /**
     * Get the referenced widget. Throws {@code bad_args} for a malformed reference and
     * {@code not_found} for a widget that does not exist or is not visible.
     */
    static Widget visibleWidget(Client client, String ref) throws BridgeException {
        Matcher matcher = WIDGET_REF.matcher(ref);
        if (!matcher.matches()) throw new BridgeException("bad_args", WIDGET_REF_SHAPE);

        int index = matcher.group(3) == null ? -1 : Integer.parseInt(matcher.group(3));
        Widget widget = widget(client, Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)), index);

        if (widget == null) throw new BridgeException("not_found", "widget " + ref + " does not exist");

        if (!isVisible(widget)) throw new BridgeException("not_found", "widget " + ref + " is not visible");

        return widget;
    }

    /** Get the widget of the group and child, or its dynamic child at the index unless it is -1. Null when absent. */
    static Widget widget(Client client, int group, int child, int index) {
        Widget widget = client.getWidget(group, child);

        return widget == null || index < 0 ? widget : widget.getChild(index);
    }

    /** Determine if the widget is shown with some size. */
    static boolean isVisible(Widget widget) {
        Rectangle bounds = widget.getBounds();

        return !widget.isHidden() && bounds != null && !bounds.isEmpty();
    }

    /** Type the text as key events, followed by Enter when asked. */
    private JsonElement type(JsonObject args) throws BridgeException {
        String text = args.get("text").getAsString();
        boolean enter = optionalBoolean(args, "enter");
        game.input(canvas -> {
            for (char ch : text.toCharArray()) {
                key(canvas, KeyEvent.getExtendedKeyCodeForChar(ch), ch);
            }

            if (enter) {
                key(canvas, KeyEvent.VK_ENTER, '\n');
            }
        });

        JsonObject out = new JsonObject();
        out.addProperty("typed", text.length());

        return out;
    }

    /** Dispatch the press, the typed character and the release of one key. */
    private static void key(Canvas canvas, int code, char ch) {
        long now = System.currentTimeMillis();
        canvas.dispatchEvent(new KeyEvent(canvas, KeyEvent.KEY_PRESSED, now, 0, code, ch));
        canvas.dispatchEvent(new KeyEvent(canvas, KeyEvent.KEY_TYPED, now, 0, KeyEvent.VK_UNDEFINED, ch));
        canvas.dispatchEvent(new KeyEvent(canvas, KeyEvent.KEY_RELEASED, now, 0, code, ch));
    }

    /** Log in with the given credentials and wait until the client is in the game. */
    private JsonElement login(JsonObject args) throws BridgeException {
        String username = args.get("username").getAsString();
        String password = args.get("password").getAsString();

        if (password.isEmpty()) throw new BridgeException("bad_args", "password is required and must not be empty");

        long deadline = System.currentTimeMillis() + args.get("wait_ms").getAsInt();
        awaitLoaded(deadline);
        submitCredentials(username, password);

        return awaitLoggedIn(deadline);
    }

    /**
     * Wait until the client has finished starting. Throws {@code timeout} when the deadline passes first.
     * The bridge connects while the client is still loading, and a login set before the login screen is lost.
     */
    private void awaitLoaded(long deadline) throws BridgeException {
        while (true) {
            GameState state = game.onClientThread(Client::getGameState);
            if (state != GameState.STARTING && state != GameState.UNKNOWN) return;

            if (System.currentTimeMillis() >= deadline) throw neverLoaded(state);

            pollAgain("the login screen");
        }
    }

    /** Build the timeout for a client that is still in the given state when the wait for the login screen ends. */
    private static BridgeException neverLoaded(GameState state) {
        String reached = "the client did not reach the login screen; gameState is " + state + ". ";
        String hint = "A client that cannot reach the game server stays in this state.";

        return new BridgeException("timeout", reached + hint);
    }

    /** Set the credentials and start the login. Throws {@code wrong_state} unless the client is on the login screen. */
    private void submitCredentials(String username, String password) throws BridgeException {
        game.onClientThread(client -> {
            GameState state = client.getGameState();
            if (state != GameState.LOGIN_SCREEN) throw notOnLoginScreen(state);

            client.setUsername(username);
            client.setPassword(password);
            client.setGameState(GameState.LOGGING_IN);

            return null;
        });
    }

    /** Build the refusal of a login for a client that is in the given state and not on the login screen. */
    private static BridgeException notOnLoginScreen(GameState state) {
        return new BridgeException("wrong_state", "the client is not on the login screen: " + state);
    }

    /**
     * Wait until the client is in the game. Throws {@code wrong_state} when the login is refused and
     * {@code timeout} when the deadline passes first.
     */
    private JsonElement awaitLoggedIn(long deadline) throws BridgeException {
        while (true) {
            GameState state = game.onClientThread(Client::getGameState);
            if (state == GameState.LOGGED_IN) {
                JsonObject out = new JsonObject();
                out.addProperty("gameState", state.name());

                return out;
            }

            if (state == GameState.LOGIN_SCREEN) throw new BridgeException("wrong_state", LOGIN_REFUSED);

            if (System.currentTimeMillis() >= deadline) throw neverLoggedIn(state);

            pollAgain("the login");
        }
    }

    /** Build the timeout for a login that is still in the given state when the wait ends. */
    private static BridgeException neverLoggedIn(GameState state) {
        return new BridgeException("timeout", "not logged in before the wait elapsed; gameState is " + state);
    }

    /** Sleep until the next read of the game state is due. */
    private static void pollAgain(String awaited) throws BridgeException {
        try {
            Thread.sleep(LOGIN_POLL_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BridgeException("internal", "interrupted while waiting for " + awaited);
        }
    }

    /** Get the point at the centre of the bounds. */
    private static Point centre(Rectangle bounds) {
        return new Point(bounds.x + bounds.width / 2, bounds.y + bounds.height / 2);
    }

    /** Get the reference that names the widget, in the shape that a click accepts. */
    static String ref(Widget widget) {
        String ref = (widget.getId() >>> 16) + ":" + (widget.getId() & 0xFFFF);

        return widget.getIndex() >= 0 ? ref + "[" + widget.getIndex() + "]" : ref;
    }

    /** Build a JSON array of the values. */
    private static JsonArray ints(int... values) {
        JsonArray out = new JsonArray();

        for (int value : values) {
            out.add(value);
        }

        return out;
    }

    /** Wrap the value as a JSON number. */
    private static JsonElement number(int value) {
        return new JsonPrimitive(value);
    }

    /** Wrap the value as a JSON string, or as JSON null when there is none. */
    private static JsonElement string(String value) {
        return value == null ? JsonNull.INSTANCE : new JsonPrimitive(value);
    }

    /** Get the value, or an empty string in place of null. */
    static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    /** Get the named argument, or null when it is absent or JSON null. */
    private static JsonElement present(JsonObject args, String name) {
        JsonElement value = args.get(name);

        return value == null || value.isJsonNull() ? null : value;
    }

    /** Get the named integer argument, or null when it is absent. */
    static Integer optionalInt(JsonObject args, String name) {
        JsonElement value = present(args, name);

        return value == null ? null : value.getAsInt();
    }

    /** Get the named string argument, or null when it is absent. */
    static String optionalString(JsonObject args, String name) {
        JsonElement value = present(args, name);

        return value == null ? null : value.getAsString();
    }

    /** Get the named boolean argument, or false when it is absent. */
    private static boolean optionalBoolean(JsonObject args, String name) {
        JsonElement value = present(args, name);

        return value != null && value.getAsBoolean();
    }

    /** Get the named array of integers, or an empty array when it is absent. */
    private static int[] optionalInts(JsonObject args, String name) {
        JsonElement value = present(args, name);
        if (value == null) return new int[0];

        JsonArray array = value.getAsJsonArray();
        int[] out = new int[array.size()];

        for (int i = 0; i < out.length; i++) {
            out[i] = array.get(i).getAsInt();
        }

        return out;
    }
}
