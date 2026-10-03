package net.rsprox.mcpbridge;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.awt.Point;
import java.util.ArrayDeque;
import java.util.Deque;
import net.rsprox.mcpbridge.ClickGuard.Verdict;
import net.rsprox.mcpbridge.GameAccess.Subscription;
import net.runelite.api.Client;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.MenuOptionClicked;

/**
 * One interaction with a target, performed as a player performs it: the mouse is moved onto the
 * target, the client builds its menu, and the entry is clicked, through the right-click menu when it
 * is not the default. Each attempt is judged by the {@link ClickGuard} and repeated until the client
 * performs the intended action or the deadline passes. The loop runs on the worker thread of the op.
 */
final class Interaction {
    /** The time the client gives a game cycle. */
    private static final int CYCLE_MS = 20;

    /** The deadline for a target that is on screen: about three game ticks. */
    private static final int ON_SCREEN_DEADLINE_MS = 1_800;

    /** The deadline once the camera had to be turned, which takes the camera several ticks. */
    private static final int CAMERA_DEADLINE_MS = 6_000;

    /** The longest wait for a game tick to start, which is a little over one tick. */
    private static final int TICK_WAIT_MS = 800;

    /** How recently a game tick must have started for the wait for one to be skipped. */
    private static final int RECENT_TICK_MS = 100;

    /** The number of frames the client is given to report a click. */
    private static final int CLICK_FRAMES = 3;

    /** The frames to wait for the right-click menu to open after the press. */
    private static final int MENU_OPEN_FRAMES = 8;

    /** The number of attempts that the result and the failure keep the words of. */
    private static final int KEPT = 5;

    /** The message when an interface blocks the game view. */
    private static final String BLOCKED = "the client offers only Cancel; an open interface is blocking the game view";

    /** The gateway to the client and the threads it must be used on. */
    private final GameAccess game;

    /** The real mouse of the client. */
    private final Mouse mouse;

    /** The target and the option to perform on it. */
    private final Target target;

    /** The judge of each click. */
    private final ClickGuard guard;

    /** The turn of the camera, when the target is out of view. */
    private final CameraTurn camera = new CameraTurn();

    /** The words of the last attempts, oldest first. */
    private final Deque<String> tried = new ArrayDeque<>();

    /** The number of attempts so far. */
    private int attempts;

    /** The number of reads of the menu. */
    private int reads;

    /** The number of reads at which the client offered more than Cancel. */
    private int offeredMore;

    /** The game cycle at which the interaction began. */
    private int began;

    /** The game cycle after which no further attempt is made. */
    private int deadline;

    /** Create the interaction that performs the target's option through the given mouse. */
    Interaction(GameAccess game, Mouse mouse, Target target) {
        this.game = game;
        this.mouse = mouse;
        this.target = target;
        this.guard = new ClickGuard(target);
    }

    /**
     * Perform the interaction and describe it: the target, the number of attempts, the tick on which
     * the client performed the action, the words of the last attempts and any turn of the camera.
     * Throws {@code wrong_state} when the deadline passes first.
     */
    JsonObject run() throws BridgeException {
        try (Subscription clicks = game.subscribe(MenuOptionClicked.class, guard::on)) {
            game.onClientThread(client -> {
                Mouse.requireUnstretched(client);

                return null;
            });

            awaitTick();
            began = cycle();
            deadline = began + ON_SCREEN_DEADLINE_MS / CYCLE_MS;

            while (true) {
                attempts++;
                Point aim = game.onClientThread(client -> target.aim(client, attempts));
                Verdict verdict = aim == null ? bringIntoView() : attempt(aim);
                remember(verdict.words);

                if (verdict.passed) return result(verdict);

                if (cycle() >= deadline) throw expired();
            }
        }
    }

    /**
     * Wait for the start of a game tick, so that positions are fresh and the action lands in a known
     * tick, unless one started a moment ago. The wait is bounded, since a client that is logged out
     * has no ticks.
     */
    private void awaitTick() throws BridgeException {
        int start = cycle();
        if (game.tickCycle() >= start - RECENT_TICK_MS / CYCLE_MS) return;

        for (int i = 0; i < TICK_WAIT_MS / CYCLE_MS && game.tickCycle() < start; i++) {
            game.nextFrame();
        }
    }

    /** Get the client's game cycle. */
    private int cycle() throws BridgeException {
        return game.onClientThread(Client::getGameCycle);
    }

    /**
     * Move the mouse onto the aim point, read the menu and click the intended entry. The menu may lag
     * the pointer by a frame, so a missing entry is read once more before the attempt counts as missed.
     */
    private Verdict attempt(Point aim) throws BridgeException {
        mouse.hover(aim.x, aim.y);
        Offer offer = read(aim);

        if (offer.match < 0) {
            game.nextFrame();
            offer = read(aim);
        }

        String at = " at " + Scenes.words(aim.x, aim.y);
        if (offer.match < 0) return missed(offer, offerWords(offer) + at);

        if (!offer.underMouse) return missed(offer, target.label() + " moved out from under the mouse" + at);

        if (offer.isDefault() && !offer.open) return click(aim, "left-clicked " + offer.offered.get(offer.match) + at);

        return throughMenu(aim, offer);
    }

    /**
     * Open the right-click menu on the aim point, or use the one that is open, and click the intended
     * row. The row is found again in the menu as the client reports it after opening, since the
     * entries may be reordered or added to while it opens.
     */
    private Verdict throughMenu(Point aim, Offer offer) throws BridgeException {
        if (!offer.open) {
            mouse.click(aim.x, aim.y, Mouse.Button.RIGHT);
            offer = awaitOpenMenu();
        }

        if (!offer.open) return missed(offer, "the menu did not open on " + offer.words());

        if (offer.match < 0) return missed(offer, "the open menu offers " + offer.words());

        Point row = offer.row();
        mouse.hover(row.x, row.y);
        Offer hovered = read(null);
        boolean same = hovered.open && hovered.match == offer.match;

        if (!same) return missed(hovered, "the menu changed to " + hovered.words() + " while the row was hovered");

        String words = "clicked row " + offer.rowNumber() + ", " + offer.offered.get(offer.match);

        return click(row, words + " at " + Scenes.words(row.x, row.y));
    }

    /**
     * Wait for the right-click menu to open and read it once it has settled. The client opens the menu
     * on its next game cycle, which can be several frames after the press, and fills it a frame later.
     */
    private Offer awaitOpenMenu() throws BridgeException {
        Offer offer = read(null);

        for (int i = 0; i < MENU_OPEN_FRAMES && !offer.open; i++) {
            game.nextFrame();
            offer = read(null);
        }

        if (!offer.open) return offer;

        game.nextFrame();

        return read(null);
    }

    /** Left-click the point under the guard, and wait a few frames for the client to report the click. */
    private Verdict click(Point at, String words) throws BridgeException {
        guard.arm();
        mouse.click(at.x, at.y, Mouse.Button.LEFT);

        for (int i = 0; i < CLICK_FRAMES && !guard.settled(); i++) {
            game.nextFrame();
        }

        Verdict verdict = guard.disarm("the client reported no click within " + CLICK_FRAMES + " frames");
        String outcome = words + "; " + verdict.words;

        return verdict.passed ? Verdict.passed(outcome, verdict.tick) : Verdict.missed(outcome);
    }

    /** Record a missed attempt, closing the menu when it is open so that the next attempt starts afresh. */
    private Verdict missed(Offer offer, String words) throws BridgeException {
        if (offer.open) {
            Point away = offer.away();
            mouse.hover(away.x, away.y);
        }

        return Verdict.missed(words);
    }

    /**
     * Read the menu the client has built, and whether the target still lies under the aim when one is
     * given, and count the reads and those that offered more than Cancel.
     */
    private Offer read(Point aim) throws BridgeException {
        Offer offer = game.onClientThread(client -> new Offer(client, target, aim));
        reads++;

        if (!offer.onlyCancel) offeredMore++;

        return offer;
    }

    /** Write what the client offered, naming the blocking interface when it offered only Cancel. */
    private static String offerWords(Offer offer) {
        return offer.onlyCancel ? BLOCKED : "the client offered " + offer.words();
    }

    /** Turn the camera toward the target, which is not on screen, and extend the deadline for the turn. */
    private Verdict bringIntoView() throws BridgeException {
        WorldPoint tile = game.onClientThread(target::tile);
        String words = "no part of " + target.label() + " is on screen";
        game.nextFrame();

        if (tile == null) return Verdict.missed(words);

        deadline = Math.max(deadline, began + CAMERA_DEADLINE_MS / CYCLE_MS);

        return Verdict.missed(words + "; " + game.onClientThread(client -> camera.toward(client, tile)));
    }

    /** Keep the words of the attempt among the last few. */
    private void remember(String words) {
        tried.addLast(words);

        if (tried.size() > KEPT) tried.removeFirst();
    }

    /** Build the result of the interaction that the client performed. */
    private JsonObject result(Verdict verdict) {
        JsonObject out = target.describe();
        out.addProperty("attempts", attempts);
        out.addProperty("tick", verdict.tick);
        JsonArray words = new JsonArray();
        tried.forEach(words::add);
        out.add("tried", words);

        if (camera.describe() != null) out.add("camera", camera.describe());

        return out;
    }

    /** Build the failure for a deadline that passed, with what happened at the last attempt. */
    private BridgeException expired() {
        String last = reads > 0 && offeredMore == 0 ? BLOCKED : tried.peekLast();
        String message = "aimed at " + target.label() + "; " + last + " at the last of " + attempts + " attempts";

        return new BridgeException("wrong_state", message);
    }
}
