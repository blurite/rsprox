package net.rsprox.mcpbridge;

import com.google.gson.JsonObject;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import net.runelite.api.Client;
import net.runelite.api.MenuEntry;
import net.runelite.api.coords.WorldPoint;

/**
 * One thing in the client that an option is performed on, named by its identity so that it can be
 * found again in the client's own state on every attempt. All reads happen on the client thread.
 */
abstract class Target {
    /** The option to perform, as the caller wrote it. */
    final String option;

    /** Create a target on which the option is to be performed. */
    Target(String option) {
        this.option = option;
    }

    /** Get the words that name the target in a message. */
    abstract String label();

    /** Describe the target for the result: the prefix of the packet to expect, its kind, identity and option. */
    abstract JsonObject describe();

    /** Get the clickable outline of the target on the canvas, or null while no part of it can be clicked. */
    abstract Shape shape(Client client);

    /** Determine if the menu entry performs this target's option on this very target. */
    abstract boolean matches(MenuEntry entry, Client client);

    /** Get the world tile to turn the camera toward, or null for a target that is not in the world. */
    abstract WorldPoint tile(Client client);

    /** Get the part of the canvas within which the target can be clicked: the 3D view unless overridden. */
    Rectangle area(Client client) {
        return Scenes.viewport(client);
    }

    /** Get the canvas point to aim at on the given attempt, or null while no part of the target is on screen. */
    Point aim(Client client, int attempt) {
        Shape shape = shape(client);
        if (shape == null) return null;

        return Aiming.point(shape, area(client), attempt);
    }

    /** Determine if the entry's option is exactly the option, ignoring case and colour tags. */
    boolean sameOption(MenuEntry entry) {
        return option.equalsIgnoreCase(Offer.untagged(entry.getOption()));
    }

    /** Start the description with the packet prefix, the kind and, for the result's order, nothing else yet. */
    static JsonObject described(String expect, String kind) {
        JsonObject out = new JsonObject();
        out.addProperty("expect", expect);
        out.addProperty("target", kind);

        return out;
    }
}
