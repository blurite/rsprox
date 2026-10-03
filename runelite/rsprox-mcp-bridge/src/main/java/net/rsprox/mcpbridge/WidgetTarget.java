package net.rsprox.mcpbridge;

import com.google.gson.JsonObject;
import java.awt.Canvas;
import java.awt.Rectangle;
import java.awt.Shape;
import java.util.EnumSet;
import java.util.Set;
import net.runelite.api.Client;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.widgets.Widget;

/**
 * An interface component, named by its packed id and, for a dynamic child, its index. Its menu
 * entries carry the id as the second parameter and the child index as the first.
 */
final class WidgetTarget extends Target {
    /** The menu actions that perform an option on an interface component. */
    private static final Set<MenuAction> FAMILY = EnumSet.of(
            MenuAction.CC_OP,
            MenuAction.CC_OP_LOW_PRIORITY,
            MenuAction.WIDGET_CONTINUE,
            MenuAction.WIDGET_TYPE_1,
            MenuAction.WIDGET_TYPE_4,
            MenuAction.WIDGET_TYPE_5,
            MenuAction.WIDGET_CLOSE,
            MenuAction.WIDGET_FIRST_OPTION,
            MenuAction.WIDGET_SECOND_OPTION,
            MenuAction.WIDGET_THIRD_OPTION,
            MenuAction.WIDGET_FOURTH_OPTION,
            MenuAction.WIDGET_FIFTH_OPTION);

    /** The reference as the caller wrote it, or as the dialog was resolved to. */
    private final String ref;

    /** The packed id of the widget. */
    private final int id;

    /** The index of the dynamic child, or -1 for a static widget. */
    private final int index;

    /** Create the target for the widget with the packed id and child index, referred to by the ref. */
    WidgetTarget(String ref, int id, int index, String option) {
        super(option);
        this.ref = ref;
        this.id = id;
        this.index = index;
    }

    /** Get the words that name the widget. */
    @Override
    String label() {
        return "widget " + ref;
    }

    /** Describe the widget by its reference. */
    @Override
    JsonObject describe() {
        JsonObject out = described("IF_BUTTON", "widget");
        out.addProperty("widget", ref);
        out.addProperty("option", option);

        return out;
    }

    /** Get the bounds of the widget, or null while it is not visible. */
    @Override
    Shape shape(Client client) {
        Widget widget = Ops.widget(client, id >>> 16, id & 0xFFFF, index);

        return widget == null || !Ops.isVisible(widget) ? null : widget.getBounds();
    }

    /** Determine if the entry is this option on this widget, and on this child for a dynamic one. */
    @Override
    boolean matches(MenuEntry entry, Client client) {
        boolean sameWidget = entry.getParam1() == id && (index < 0 || entry.getParam0() == index);

        return FAMILY.contains(entry.getType()) && sameWidget && sameOption(entry);
    }

    /** Get no tile: an interface is not in the world, so the camera cannot bring it into view. */
    @Override
    WorldPoint tile(Client client) {
        return null;
    }

    /** Get the whole canvas, since an interface lies outside the 3D view. */
    @Override
    Rectangle area(Client client) {
        Canvas canvas = client.getCanvas();

        return new Rectangle(0, 0, canvas.getWidth(), canvas.getHeight());
    }
}
