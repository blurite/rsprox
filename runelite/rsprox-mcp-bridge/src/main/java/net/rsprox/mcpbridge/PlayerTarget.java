package net.rsprox.mcpbridge;

import com.google.gson.JsonObject;
import java.awt.Shape;
import java.util.EnumSet;
import java.util.Set;
import net.runelite.api.Client;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.Player;
import net.runelite.api.coords.WorldPoint;

/** Another player, named by its index in the client's list. Its menu entries carry that index as the identifier. */
final class PlayerTarget extends Target {
    /** The menu actions that perform an option on a player. */
    private static final Set<MenuAction> FAMILY = EnumSet.of(
            MenuAction.PLAYER_FIRST_OPTION,
            MenuAction.PLAYER_SECOND_OPTION,
            MenuAction.PLAYER_THIRD_OPTION,
            MenuAction.PLAYER_FOURTH_OPTION,
            MenuAction.PLAYER_FIFTH_OPTION,
            MenuAction.PLAYER_SIXTH_OPTION,
            MenuAction.PLAYER_SEVENTH_OPTION,
            MenuAction.PLAYER_EIGHTH_OPTION);

    /** The index of the player. */
    private final int index;

    /** The name of the player when it was found. */
    private final String name;

    /** Create the target for the player with the index, as found with the name. */
    PlayerTarget(int index, String name, String option) {
        super(option);
        this.index = index;
        this.name = name;
    }

    /** Get the words that name the player. */
    @Override
    String label() {
        return "player " + index + " (" + name + ")";
    }

    /** Describe the player with its index and name. */
    @Override
    JsonObject describe() {
        JsonObject out = described("OPPLAYER", "player");
        out.addProperty("index", index);
        out.addProperty("name", name);
        out.addProperty("option", option);

        return out;
    }

    /** Get the convex hull of the player's model, or null while the client does not show it. */
    @Override
    Shape shape(Client client) {
        Player player = find(client);

        return player == null ? null : player.getConvexHull();
    }

    /** Determine if the entry is this option on the player with this index. */
    @Override
    boolean matches(MenuEntry entry, Client client) {
        return FAMILY.contains(entry.getType()) && entry.getIdentifier() == index && sameOption(entry);
    }

    /** Get the tile the player stands on, or null while the client does not have it. */
    @Override
    WorldPoint tile(Client client) {
        Player player = find(client);

        return player == null ? null : player.getWorldLocation();
    }

    /** Find the player in the client's list, or null while it is not there. */
    private Player find(Client client) {
        return client.getTopLevelWorldView().players().byIndex(index);
    }
}
