package net.rsprox.mcpbridge;

import com.google.gson.JsonObject;
import java.awt.Shape;
import java.util.EnumSet;
import java.util.Set;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.WorldView;
import net.runelite.api.coords.WorldPoint;

/**
 * An NPC or another player, named by its index in the client's list of its kind. Its menu entries
 * carry that index as the identifier.
 */
final class ActorTarget extends Target {
    /** The menu actions that perform an option on an NPC. */
    private static final Set<MenuAction> NPC_FAMILY = EnumSet.of(
        MenuAction.NPC_FIRST_OPTION,
        MenuAction.NPC_SECOND_OPTION,
        MenuAction.NPC_THIRD_OPTION,
        MenuAction.NPC_FOURTH_OPTION,
        MenuAction.NPC_FIFTH_OPTION,
        MenuAction.EXAMINE_NPC);

    /** The menu actions that perform an option on a player. */
    private static final Set<MenuAction> PLAYER_FAMILY = EnumSet.of(
        MenuAction.PLAYER_FIRST_OPTION,
        MenuAction.PLAYER_SECOND_OPTION,
        MenuAction.PLAYER_THIRD_OPTION,
        MenuAction.PLAYER_FOURTH_OPTION,
        MenuAction.PLAYER_FIFTH_OPTION,
        MenuAction.PLAYER_SIXTH_OPTION,
        MenuAction.PLAYER_SEVENTH_OPTION,
        MenuAction.PLAYER_EIGHTH_OPTION);

    /** Whether the actor is an NPC, and another player otherwise. */
    private final boolean npc;

    /** The index of the actor. */
    private final int index;

    /** The id of the NPC when it was found, which is unused for a player. */
    private final int id;

    /** The name of the actor when it was found. */
    private final String name;

    /** Create the target for the actor of the kind with the index, as found with the id and name. */
    private ActorTarget(boolean npc, int index, int id, String name, String option) {
        super(option);
        this.npc = npc;
        this.index = index;
        this.id = id;
        this.name = name;
    }

    /** Create the target for the NPC with the index, as found with the id and name. */
    static ActorTarget npc(int index, int id, String name, String option) {
        return new ActorTarget(true, index, id, name, option);
    }

    /** Create the target for the player with the index, as found with the name. */
    static ActorTarget player(int index, String name, String option) {
        return new ActorTarget(false, index, -1, name, option);
    }

    /** Get the words that name the actor. */
    @Override
    String label() {
        return (npc ? "npc " : "player ") + index + " (" + name + ")";
    }

    /** Describe the actor with its index, its id when it is an NPC, and its name. */
    @Override
    JsonObject describe() {
        JsonObject out = npc ? described("OPNPC", "npc") : described("OPPLAYER", "player");
        out.addProperty("index", index);

        if (npc) out.addProperty("id", id);

        out.addProperty("name", name);
        out.addProperty("option", option);

        return out;
    }

    /** Get the convex hull of the actor's model, or null while the client does not show it. */
    @Override
    Shape shape(Client client) {
        Actor actor = find(client);

        return actor == null ? null : actor.getConvexHull();
    }

    /** Determine if the entry is this option on the actor of this kind with this index. */
    @Override
    boolean matches(MenuEntry entry, Client client) {
        Set<MenuAction> family = npc ? NPC_FAMILY : PLAYER_FAMILY;

        return family.contains(entry.getType()) && entry.getIdentifier() == index && sameOption(entry);
    }

    /** Get the tile the actor stands on, or null while the client does not have it. */
    @Override
    WorldPoint tile(Client client) {
        Actor actor = find(client);

        return actor == null ? null : actor.getWorldLocation();
    }

    /** Find the actor in the client's list of its kind, or null while it is not there. */
    private Actor find(Client client) {
        WorldView view = client.getTopLevelWorldView();

        return npc ? view.npcs().byIndex(index) : view.players().byIndex(index);
    }
}
