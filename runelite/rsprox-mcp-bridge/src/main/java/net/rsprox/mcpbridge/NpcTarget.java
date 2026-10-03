package net.rsprox.mcpbridge;

import com.google.gson.JsonObject;
import java.awt.Shape;
import java.util.EnumSet;
import java.util.Set;
import net.runelite.api.Client;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.NPC;
import net.runelite.api.coords.WorldPoint;

/** An NPC, named by its index in the client's list. Its menu entries carry that index as the identifier. */
final class NpcTarget extends Target {
    /** The menu actions that perform an option on an NPC. */
    private static final Set<MenuAction> FAMILY = EnumSet.of(
            MenuAction.NPC_FIRST_OPTION,
            MenuAction.NPC_SECOND_OPTION,
            MenuAction.NPC_THIRD_OPTION,
            MenuAction.NPC_FOURTH_OPTION,
            MenuAction.NPC_FIFTH_OPTION,
            MenuAction.EXAMINE_NPC);

    /** The index of the NPC. */
    private final int index;

    /** The id of the NPC when it was found. */
    private final int id;

    /** The name of the NPC when it was found. */
    private final String name;

    /** Create the target for the NPC with the index, as found with the id and name. */
    NpcTarget(int index, int id, String name, String option) {
        super(option);
        this.index = index;
        this.id = id;
        this.name = name;
    }

    /** Get the words that name the NPC. */
    @Override
    String label() {
        return "npc " + index + " (" + name + ")";
    }

    /** Describe the NPC with its index, id and name. */
    @Override
    JsonObject describe() {
        JsonObject out = described("OPNPC", "npc");
        out.addProperty("index", index);
        out.addProperty("id", id);
        out.addProperty("name", name);
        out.addProperty("option", option);

        return out;
    }

    /** Get the convex hull of the NPC's model, or null while the client does not show it. */
    @Override
    Shape shape(Client client) {
        NPC npc = find(client);

        return npc == null ? null : npc.getConvexHull();
    }

    /** Determine if the entry is this option on the NPC with this index. */
    @Override
    boolean matches(MenuEntry entry, Client client) {
        return FAMILY.contains(entry.getType()) && entry.getIdentifier() == index && sameOption(entry);
    }

    /** Get the tile the NPC stands on, or null while the client does not have it. */
    @Override
    WorldPoint tile(Client client) {
        NPC npc = find(client);

        return npc == null ? null : npc.getWorldLocation();
    }

    /** Find the NPC in the client's list, or null while it is not there. */
    private NPC find(Client client) {
        return client.getTopLevelWorldView().npcs().byIndex(index);
    }
}
