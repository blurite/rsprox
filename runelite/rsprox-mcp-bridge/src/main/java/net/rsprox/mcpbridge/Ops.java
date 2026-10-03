package net.rsprox.mcpbridge;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.awt.Canvas;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
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
    private static final Pattern WIDGET_REF = Pattern.compile("(\\d+):(\\d+)(?:\\[(\\d+)])?");
    private static final int WIDGET_DEPTH_LIMIT = 12;
    private static final int DEFAULT_WIDGET_LIMIT = 200;
    private static final int DEFAULT_LOGIN_WAIT_MS = 15_000;
    private static final int LOGIN_POLL_MS = 100;

    interface Op {
        JsonElement run(JsonObject args) throws BridgeException;
    }

    private final GameAccess game;
    private final Map<String, Op> table = new HashMap<>();

    Ops(GameAccess game) {
        this.game = game;
        table.put("state", this::state);
        table.put("widgets", this::widgets);
        table.put("vars", this::vars);
        table.put("screenshot", this::screenshot);
        table.put("click", this::click);
        table.put("type", this::type);
        table.put("login", this::login);
    }

    JsonElement run(String op, JsonObject args) throws BridgeException {
        Op handler = table.get(op);
        if (handler == null) {
            throw new BridgeException("bad_args", "unknown op '" + op + "'");
        }

        return handler.run(args);
    }

    private JsonElement state(JsonObject args) throws BridgeException {
        return game.read(client -> {
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

    private JsonElement widgets(JsonObject args) throws BridgeException {
        Integer group = optionalInt(args, "group");
        String text = optionalString(args, "text");
        String needle = text == null ? null : text.toLowerCase(Locale.ROOT);
        boolean includeHidden = optionalBoolean(args, "hidden");
        Integer limit = optionalInt(args, "limit");

        return game.read(client -> {
            WidgetWalk walk =
                new WidgetWalk(group, needle, includeHidden, limit == null ? DEFAULT_WIDGET_LIMIT : limit);

            Set<Integer> roots = new LinkedHashSet<>();

            for (Widget root : client.getWidgetRoots()) {
                if (root != null) {
                    roots.add(root.getId() >>> 16);
                    walk.visit(root, 0);
                }
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
        final JsonArray found = new JsonArray();
        boolean truncated;
        private final Integer group;
        private final String needle;
        private final boolean includeHidden;
        private final int limit;

        WidgetWalk(Integer group, String needle, boolean includeHidden, int limit) {
            this.group = group;
            this.needle = needle;
            this.includeHidden = includeHidden;
            this.limit = limit;
        }

        void visit(Widget widget, int depth) {
            if (widget == null || truncated || depth > WIDGET_DEPTH_LIMIT) {
                return;
            }

            boolean hidden = widget.isHidden();
            if (hidden && !includeHidden) {
                return;
            }

            if (group == null || widget.getId() >>> 16 == group) {
                JsonObject described = describe(widget, hidden);
                if (described != null) {
                    if (found.size() >= limit) {
                        truncated = true;

                        return;
                    }

                    found.add(described);
                }
            }

            visitAll(widget.getStaticChildren(), depth);
            visitAll(widget.getDynamicChildren(), depth);
            visitAll(widget.getNestedChildren(), depth);
        }

        private void visitAll(Widget[] children, int depth) {
            if (children != null) {
                for (Widget child : children) {
                    visit(child, depth + 1);
                }
            }
        }

        private JsonObject describe(Widget widget, boolean hidden) {
            String text = nullToEmpty(widget.getText());
            String name = nullToEmpty(widget.getName());
            JsonArray actions = new JsonArray();
            String[] raw = widget.getActions();

            if (raw != null) {
                for (String action : raw) {
                    if (action != null && !action.isEmpty()) {
                        actions.add(action);
                    }
                }
            }

            if (text.isEmpty() && name.isEmpty() && actions.size() == 0) {
                return null;
            }

            if (needle != null && !contains(text) && !contains(name) && !contains(actions.toString())) {
                return null;
            }

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

        private boolean contains(String haystack) {
            return haystack.toLowerCase(Locale.ROOT).contains(needle);
        }
    }

    private JsonElement vars(JsonObject args) throws BridgeException {
        int[] varps = optionalInts(args, "varps");
        int[] varbits = optionalInts(args, "varbits");
        int[] varcInts = optionalInts(args, "varcInts");
        int[] varcStrs = optionalInts(args, "varcStrs");

        return game.read(client -> {
            JsonObject out = new JsonObject();
            out.add("varps", readVars("varp", varps, id -> number(client.getVarpValue(id))));
            out.add("varbits", readVars("varbit", varbits, id -> number(client.getVarbitValue(id))));
            out.add("varcInts", readVars("varcInt", varcInts, id -> number(client.getVarcIntValue(id))));
            out.add("varcStrs", readVars("varcStr", varcStrs, id -> string(client.getVarcStrValue(id))));

            return out;
        });
    }

    private interface VarReader {
        JsonElement read(int id);
    }

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

    private JsonElement screenshot(JsonObject args) throws BridgeException {
        BufferedImage frame = game.frame();
        int[] canvas = game.read(client -> new int[] {client.getCanvas().getWidth(), client.getCanvas().getHeight()});
        int width = canvas[0];
        int height = canvas[1];

        if (width <= 0 || height <= 0) {
            throw new BridgeException("wrong_state", "the canvas has no size yet");
        }

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

    private JsonElement click(JsonObject args) throws BridgeException {
        String button = optionalString(args, "button");
        boolean right = "right".equals(button);

        if (button != null && !right && !"left".equals(button)) {
            throw new BridgeException("bad_args", "button must be \"left\" or \"right\"");
        }

        String widget = optionalString(args, "widget");
        Integer x = optionalInt(args, "x");
        Integer y = optionalInt(args, "y");
        Point point;

        if (widget != null) {
            point = game.read(client -> centre(visibleBounds(client, widget)));
        } else if (x != null && y != null) {
            point = new Point(x, y);
        } else {
            throw new BridgeException("bad_args", "pass either x and y, or widget");
        }

        int awtButton = right ? MouseEvent.BUTTON3 : MouseEvent.BUTTON1;
        int downMask = right ? InputEvent.BUTTON3_DOWN_MASK : InputEvent.BUTTON1_DOWN_MASK;
        game.input(canvas -> {
            long now = System.currentTimeMillis();
            canvas.dispatchEvent(
                new MouseEvent(canvas, MouseEvent.MOUSE_MOVED, now, 0, point.x, point.y, 0, false));

            canvas.dispatchEvent(
                new MouseEvent(canvas, MouseEvent.MOUSE_PRESSED, now, downMask, point.x, point.y, 1, false, awtButton));

            canvas.dispatchEvent(
                new MouseEvent(canvas, MouseEvent.MOUSE_RELEASED, now + 30, 0, point.x, point.y, 1, false, awtButton));

            canvas.dispatchEvent(
                new MouseEvent(canvas, MouseEvent.MOUSE_CLICKED, now + 30, 0, point.x, point.y, 1, false, awtButton));
        });

        JsonObject out = new JsonObject();
        out.addProperty("x", point.x);
        out.addProperty("y", point.y);

        return out;
    }

    private static Rectangle visibleBounds(Client client, String ref) throws BridgeException {
        Matcher matcher = WIDGET_REF.matcher(ref);
        if (!matcher.matches()) {
            throw new BridgeException("bad_args", "widget must look like \"558:7\" or \"558:7[3]\"");
        }

        Widget widget = client.getWidget(Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)));
        if (widget != null && matcher.group(3) != null) {
            widget = widget.getChild(Integer.parseInt(matcher.group(3)));
        }

        if (widget == null) {
            throw new BridgeException("not_found", "widget " + ref + " does not exist");
        }

        Rectangle bounds = widget.getBounds();
        if (widget.isHidden() || bounds == null || bounds.isEmpty()) {
            throw new BridgeException("not_found", "widget " + ref + " is not visible");
        }

        return bounds;
    }

    private JsonElement type(JsonObject args) throws BridgeException {
        String text = optionalString(args, "text");
        if (text == null) {
            throw new BridgeException("bad_args", "text is required");
        }

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

    private static void key(Canvas canvas, int code, char ch) {
        long now = System.currentTimeMillis();
        canvas.dispatchEvent(new KeyEvent(canvas, KeyEvent.KEY_PRESSED, now, 0, code, ch));
        canvas.dispatchEvent(new KeyEvent(canvas, KeyEvent.KEY_TYPED, now, 0, KeyEvent.VK_UNDEFINED, ch));
        canvas.dispatchEvent(new KeyEvent(canvas, KeyEvent.KEY_RELEASED, now, 0, code, ch));
    }

    private JsonElement login(JsonObject args) throws BridgeException {
        String username = optionalString(args, "username");
        if (username == null) {
            throw new BridgeException("bad_args", "username is required");
        }

        String password = optionalString(args, "password");
        if (password == null || password.isEmpty()) {
            throw new BridgeException("bad_args", "password is required and must not be empty");
        }

        Integer wait = optionalInt(args, "wait_ms");
        long deadline = System.currentTimeMillis() + (wait == null ? DEFAULT_LOGIN_WAIT_MS : wait);
        awaitLoginScreen(deadline);
        game.read(client -> {
            GameState state = client.getGameState();
            if (state != GameState.LOGIN_SCREEN) {
                throw new BridgeException("wrong_state", "the client is not on the login screen: " + state);
            }

            client.setUsername(username);
            client.setPassword(password);
            client.setGameState(GameState.LOGGING_IN);

            return null;
        });

        while (true) {
            GameState state = game.read(Client::getGameState);
            if (state == GameState.LOGGED_IN) {
                JsonObject out = new JsonObject();
                out.addProperty("gameState", state.name());

                return out;
            }

            if (state == GameState.LOGIN_SCREEN) {
                throw new BridgeException("wrong_state", "the login was refused; the client is back on the login screen");
            }

            if (System.currentTimeMillis() >= deadline) {
                throw new BridgeException("timeout", "not logged in before the wait elapsed; gameState is " + state);
            }

            try {
                Thread.sleep(LOGIN_POLL_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new BridgeException("internal", "interrupted while waiting for the login");
            }
        }
    }

    /** The bridge connects while the client is still loading, and a login set before the login screen is lost. */
    private void awaitLoginScreen(long deadline) throws BridgeException {
        while (true) {
            GameState state = game.read(Client::getGameState);
            if (state != GameState.STARTING && state != GameState.UNKNOWN) {
                return;
            }

            if (System.currentTimeMillis() >= deadline) {
                throw new BridgeException(
                    "timeout",
                    "the client did not reach the login screen; gameState is " + state
                        + ". A client that cannot reach the game server stays in this state.");
            }

            try {
                Thread.sleep(LOGIN_POLL_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new BridgeException("internal", "interrupted while waiting for the login screen");
            }
        }
    }

    private static Point centre(Rectangle bounds) {
        return new Point(bounds.x + bounds.width / 2, bounds.y + bounds.height / 2);
    }

    private static String ref(Widget widget) {
        String ref = (widget.getId() >>> 16) + ":" + (widget.getId() & 0xFFFF);

        return widget.getIndex() >= 0 ? ref + "[" + widget.getIndex() + "]" : ref;
    }

    private static JsonArray ints(int... values) {
        JsonArray out = new JsonArray();

        for (int value : values) {
            out.add(value);
        }

        return out;
    }

    private static JsonElement number(int value) {
        return new JsonPrimitive(value);
    }

    private static JsonElement string(String value) {
        return value == null ? JsonNull.INSTANCE : new JsonPrimitive(value);
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private static JsonElement present(JsonObject args, String name) {
        JsonElement value = args.get(name);

        return value == null || value.isJsonNull() ? null : value;
    }

    private static Integer optionalInt(JsonObject args, String name) throws BridgeException {
        JsonElement value = present(args, name);
        if (value == null) {
            return null;
        }

        if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isNumber()) {
            throw new BridgeException("bad_args", name + " must be an integer");
        }

        return value.getAsInt();
    }

    private static String optionalString(JsonObject args, String name) throws BridgeException {
        JsonElement value = present(args, name);
        if (value == null) {
            return null;
        }

        if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString()) {
            throw new BridgeException("bad_args", name + " must be a string");
        }

        return value.getAsString();
    }

    private static boolean optionalBoolean(JsonObject args, String name) throws BridgeException {
        JsonElement value = present(args, name);
        if (value == null) {
            return false;
        }

        if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isBoolean()) {
            throw new BridgeException("bad_args", name + " must be a boolean");
        }

        return value.getAsBoolean();
    }

    private static int[] optionalInts(JsonObject args, String name) throws BridgeException {
        JsonElement value = present(args, name);
        if (value == null) {
            return new int[0];
        }

        if (!value.isJsonArray()) {
            throw new BridgeException("bad_args", name + " must be an array of integers");
        }

        JsonArray array = value.getAsJsonArray();
        int[] out = new int[array.size()];

        for (int i = 0; i < out.length; i++) {
            JsonElement item = array.get(i);
            if (!item.isJsonPrimitive() || !item.getAsJsonPrimitive().isNumber()) {
                throw new BridgeException("bad_args", name + " must be an array of integers");
            }

            out[i] = item.getAsInt();
        }

        return out;
    }
}
