package net.rsprox.mcpbridge;

import java.awt.Canvas;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.util.ArrayList;
import java.util.List;
import net.runelite.api.Client;
import net.runelite.api.Menu;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;

/**
 * What the client's menu offered at one moment, read on the client thread and kept as plain values:
 * the entries in the client's array order, where the last is the one a left click performs, whether
 * the aimed-at target was still under the mouse, and the row geometry of the menu when it is open.
 */
final class Offer {
    /**
     * The client's menu layout: a header of this height above the rows, then one row per entry, drawn
     * top to bottom in reverse array order. The row height is derived from the menu height and the
     * number of entries, and this value is used when that derivation gives nonsense.
     */
    private static final int HEADER = 19;

    /** The row height of the client's menu layout, as a fallback. */
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
    final List<String> offered = new ArrayList<>();

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

    /**
     * Read the menu the client has built, looking for the entry that performs the option on the
     * target, and whether the target still lies under the aimed-at point, if one is given.
     */
    Offer(Client client, Target target, Point aim) {
        Menu menu = client.getMenu();
        MenuEntry[] entries = menu.getMenuEntries();
        int found = -1;
        boolean cancels = true;

        for (int i = 0; i < entries.length; i++) {
            offered.add(words(entries[i]));
            cancels &= entries[i].getType() == MenuAction.CANCEL;

            if (target.matches(entries[i], client)) found = i;
        }

        Shape shape = aim == null ? null : target.shape(client);
        Canvas surface = client.getCanvas();
        this.open = client.isMenuOpen();
        this.match = found;
        this.onlyCancel = cancels;
        this.underMouse = aim == null || (shape != null && shape.contains(aim));
        this.bounds = new Rectangle(menu.getMenuX(), menu.getMenuY(), menu.getMenuWidth(), menu.getMenuHeight());
        this.scroll = client.getMenuScroll();
        this.canvas = new Rectangle(0, 0, surface.getWidth(), surface.getHeight());
    }

    /** Determine if the matching entry is the one a left click performs with the menu closed. */
    boolean isDefault() {
        return match >= 0 && match == offered.size() - 1;
    }

    /** Get the canvas point in the vertical middle of the matching entry's row of the open menu. */
    Point row() {
        int row = offered.size() - 1 - match - scroll;
        int height = rowHeight();

        return new Point(bounds.x + bounds.width / 2, bounds.y + HEADER + height * row + height / 2);
    }

    /** Get the row number of the matching entry, counted from the top of the open menu. */
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
