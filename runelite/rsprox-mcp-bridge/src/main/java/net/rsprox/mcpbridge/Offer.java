package net.rsprox.mcpbridge;

import java.awt.Canvas;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import net.runelite.api.Client;
import net.runelite.api.Menu;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;

/**
 * What the client's menu offered at one moment, read on the client thread and kept as plain values:
 * the entries in the client's array order, whether the aimed-at target was still under the mouse, and
 * the row geometry of the menu when it is open. A left click usually performs the last entry, but the
 * client sorts the entries after this read and a plugin may swap them, so only the client's own
 * report of a click says what it performed.
 */
final class Offer {
    /** The height of the header that the client draws above the rows of its menu. */
    private static final int HEADER = 19;

    /**
     * The height of a row of the client's menu, one per entry, drawn top to bottom in reverse array
     * order. It is used when the height derived from the menu's height and its number of entries is
     * not a sane one.
     */
    private static final int ROW = 15;

    /** The least row height that counts as a sane derivation. */
    private static final int ROW_MIN = 10;

    /** The greatest row height that counts as a sane derivation. */
    private static final int ROW_MAX = 20;

    /** The distance from the edge of the canvas at which the mouse is parked to close a menu. */
    private static final int MARGIN = 2;

    /** Whether the menu was open. */
    final boolean open;

    /** The entries as "option target" with the colour tags stripped, in the client's array order. */
    final List<String> offered;

    /** The index of the entry that performs the intended action, or -1 when none does. */
    final int match;

    /** Whether every entry is a cancel entry, as while an interface blocks the game view. */
    final boolean onlyCancel;

    /** Whether the target's outline still held the aimed-at point when the menu was read. */
    final boolean underMouse;

    /** The bounds of the open menu on the canvas. */
    private final Rectangle bounds;

    /** The number of rows the open menu is scrolled by. */
    private final int scroll;

    /** The size of the canvas. */
    private final Rectangle canvas;

    /** Create the offer of the entries, of which the one at the index performs the intended action. */
    private Offer(Client client, MenuEntry[] entries, int match, boolean underMouse) {
        Menu menu = client.getMenu();
        Canvas surface = client.getCanvas();
        this.open = client.isMenuOpen();
        this.offered = words(entries);
        this.match = match;
        this.onlyCancel = Arrays.stream(entries).allMatch(entry -> entry.getType() == MenuAction.CANCEL);
        this.underMouse = underMouse;
        this.bounds = new Rectangle(menu.getMenuX(), menu.getMenuY(), menu.getMenuWidth(), menu.getMenuHeight());
        this.scroll = client.getMenuScroll();
        this.canvas = new Rectangle(0, 0, surface.getWidth(), surface.getHeight());
    }

    /**
     * Read the menu the client has built, looking for the entry that performs the option on the
     * target, and whether the target still lies under the aimed-at point, if one is given.
     */
    static Offer read(Client client, Target target, Point aim) {
        MenuEntry[] entries = client.getMenu().getMenuEntries();

        return new Offer(client, entries, lastMatch(entries, target, client), isUnder(client, target, aim));
    }

    /** Get the index of the last entry that performs the target's option on the target, or -1 when none does. */
    private static int lastMatch(MenuEntry[] entries, Target target, Client client) {
        for (int i = entries.length - 1; i >= 0; i--) {
            if (target.matches(entries[i], client)) return i;
        }

        return -1;
    }

    /** Determine if the target's outline holds the aimed-at point, which a read without an aim takes as given. */
    private static boolean isUnder(Client client, Target target, Point aim) {
        if (aim == null) return true;

        Shape shape = target.shape(client);

        return shape != null && shape.contains(aim);
    }

    /** Write each entry as its option and its target with the colour tags stripped, in the given order. */
    private static List<String> words(MenuEntry[] entries) {
        return Arrays.stream(entries).map(entry -> words(entry)).collect(Collectors.toList());
    }

    /** Determine if the matching entry is the last of the array, which is the one a left click usually performs. */
    boolean isLast() {
        return match >= 0 && match == offered.size() - 1;
    }

    /** Get the canvas point in the vertical middle of the matching entry's row of the open menu. */
    Point row() {
        int height = rowHeight();

        return new Point(bounds.x + bounds.width / 2, bounds.y + HEADER + height * rowNumber() + height / 2);
    }

    /** Get the row number of the matching entry, counted from the top of the open menu, where the first is 0. */
    int rowNumber() {
        return offered.size() - 1 - match - scroll;
    }

    /** Get the height of a row, derived from the menu's height when that gives a sane value. */
    private int rowHeight() {
        int derived = offered.isEmpty() ? 0 : (bounds.height - HEADER) / offered.size();

        return derived >= ROW_MIN && derived <= ROW_MAX ? derived : ROW;
    }

    /** Get the corner of the canvas farthest from the menu, where the mouse is out of the menu and it closes. */
    Point away() {
        boolean left = bounds.getCenterX() > canvas.getCenterX();
        boolean top = bounds.getCenterY() > canvas.getCenterY();

        return new Point(left ? MARGIN : canvas.width - MARGIN, top ? MARGIN : canvas.height - MARGIN);
    }

    /** Write the entries as a bracketed list, in the client's array order. */
    String words() {
        return offered.toString();
    }

    /** Write the entry as its option and its target with the colour tags stripped. */
    static String words(MenuEntry entry) {
        return (untagged(entry.getOption()) + " " + untagged(entry.getTarget())).trim();
    }

    /** Get the text without its colour and other tags, or an empty string for null. */
    static String untagged(String text) {
        return Ops.nullToEmpty(text).replaceAll("<[^>]*>", "");
    }
}
