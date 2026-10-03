package net.rsprox.mcpbridge;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.awt.Point;
import java.util.ArrayDeque;
import java.util.Deque;
import net.rsprox.mcpbridge.ClickGuard.Outcome;
import net.rsprox.mcpbridge.ClickGuard.Verdict;
import net.rsprox.mcpbridge.GameAccess.Subscription;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.MenuOptionClicked;

/**
 * One interaction with a target, performed as a player performs it: the mouse is moved onto the
 * target, the client builds its menu, and the entry is clicked. The click is a left click when the
 * entry looks like the default one, and goes through the right-click menu otherwise, or once the
 * client has shown that a left click performs another entry. Each attempt is judged by the
 * {@link ClickGuard} and repeated until the client performs the intended action or the deadline
 * passes. The loop runs on the worker thread of the op. Every wait is counted in client cycles of
 * 20 ms, which pass at the same rate whatever the frame rate is.
 */
final class Interaction {
    /** The cycles an interaction with a target that is on screen may take: about three game ticks. */
    private static final int ON_SCREEN_CYCLES = 90;

    /** The cycles an interaction may take once the camera had to be turned, which takes several ticks. */
    private static final int CAMERA_CYCLES = 300;

    /** The longest wait for a game tick to start, in cycles: a little over one tick. */
    private static final int TICK_WAIT_CYCLES = 40;

    /** The cycles since the start of a game tick within which the wait for the next one is skipped. */
    private static final int RECENT_TICK_CYCLES = 5;

    /** The least cycles the client is given to report a click, which it handles on the cycle after the press. */
    private static final int CLICK_CYCLES = 5;

    /** The least cycles the right-click menu is given to open after the press. */
    private static final int MENU_OPEN_CYCLES = 8;

    /** The cycles an opened menu is given to fill with its entries. */
    private static final int MENU_FILL_CYCLES = 1;

    /** The cycles a menu that lacks the entry is given to catch up with the pointer before it is read again. */
    private static final int MENU_LAG_CYCLES = 1;

    /** The number of last attempts whose words are reported. */
    private static final int KEPT = 5;

    /** The message when an interface blocks the game view. */
    private static final String BLOCKED = "the client offers only Cancel; an open interface is blocking the game view";

    /** The words added to an attempt whose left click the client turned into another entry. */
    private static final String MENU_FROM_NOW = "; a left click performs another option here, so the menu is used next";

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

    /** The client cycle at which the interaction began. */
    private int began;

    /** The client cycle after which no further attempt is made. */
    private int deadline;

    /** Whether the client has shown that a left click on the target performs another entry than the intended one. */
    private boolean anotherPreferred;

    /** The canvas point of the press that the guard let through, or null until it let one through. */
    private Point pressed;

    /** Create the interaction that performs the target's option through the given mouse. */
    Interaction(GameAccess game, Mouse mouse, Target target) {
        this.game = game;
        this.mouse = mouse;
        this.target = target;
        this.guard = new ClickGuard(target);
    }

    /**
     * Perform the interaction and describe it: the target, the number of attempts, the tick on which
     * the client performed the action, the canvas point that was pressed, the words of the last
     * attempts and any turn of the camera. Throws {@code wrong_state} when the deadline passes first.
     */
    JsonObject run() throws BridgeException {
        try (Subscription clicks = game.subscribe(MenuOptionClicked.class, guard::on)) {
            game.onClientThread(client -> {
                Mouse.requireUnstretched(client);

                return null;
            });

            awaitTick();
            began = game.cycle();
            deadline = began + ON_SCREEN_CYCLES;

            while (true) {
                attempts++;
                Point aim = game.onClientThread(client -> target.aim(client, attempts));
                Verdict verdict = aim == null ? bringIntoView() : attempt(aim);
                remember(verdict.words);

                if (verdict.isPerformed()) return result(verdict);

                if (game.cycle() >= deadline) throw expired();
            }
        }
    }

    /**
     * Wait for the start of a game tick, so that positions are fresh and the action lands in a known
     * tick, unless one started a moment ago. The wait is bounded, since a client that is logged out
     * has no ticks.
     */
    private void awaitTick() throws BridgeException {
        int start = game.cycle();
        if (game.tickCycle() >= start - RECENT_TICK_CYCLES) return;

        while (game.tickCycle() < start && game.cycle() < start + TICK_WAIT_CYCLES) {
            game.nextFrame();
        }
    }

    /**
     * Move the mouse onto the aim point, read the menu and click the intended entry. The menu may lag
     * the pointer, so a missing entry is read once more before the attempt counts as missed.
     */
    private Verdict attempt(Point aim) throws BridgeException {
        mouse.hover(aim.x, aim.y);
        Offer offer = read(aim);

        if (offer.match < 0) {
            game.awaitCycles(MENU_LAG_CYCLES);
            offer = read(aim);
        }

        String at = " at " + Scenes.words(aim.x, aim.y);

        if (offer.match < 0) return missed(offer, offerWords(offer) + at);

        if (!offer.underMouse) return missed(offer, target.label() + " moved out from under the mouse" + at);

        if (offer.isLast() && !offer.open && !anotherPreferred) return leftClick(aim, offer.offered.get(offer.match));

        return throughMenu(aim, offer);
    }

    /**
     * Left-click the aim point, where the entry looks like the default one. Only the client's report
     * says what a left click performs: the client sorts its entries after the menu is read, and a
     * plugin may swap them. When it performs another entry while it offers the intended one, the
     * following attempts go through the right-click menu.
     */
    private Verdict leftClick(Point aim, String entry) throws BridgeException {
        Verdict verdict = click(aim, "left-clicked " + entry + " at " + Scenes.words(aim.x, aim.y));

        if (verdict.outcome != Outcome.ANOTHER_PREFERRED) return verdict;

        anotherPreferred = true;

        return verdict.worded(verdict.words + MENU_FROM_NOW);
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
     * Wait for the right-click menu to open and read it once it has filled. The client opens the menu
     * on a cycle after the press and fills it on the one after that.
     */
    private Offer awaitOpenMenu() throws BridgeException {
        int until = game.cycle() + MENU_OPEN_CYCLES;
        Offer offer = read(null);

        while (!offer.open && game.cycle() < until) {
            game.nextFrame();
            offer = read(null);
        }

        if (!offer.open) return offer;

        game.awaitCycles(MENU_FILL_CYCLES);

        return read(null);
    }

    /**
     * Left-click the point under the guard, and give the client its cycles to report the click. The
     * guard stays armed for all of them, so a click that the client handles late is still judged.
     */
    private Verdict click(Point at, String words) throws BridgeException {
        guard.arm();
        mouse.click(at.x, at.y, Mouse.Button.LEFT);

        int until = game.cycle() + CLICK_CYCLES;
        while (!guard.isSettled() && game.cycle() < until) {
            game.nextFrame();
        }

        Verdict verdict = game.onClientThread(client -> guard.disarm());
        if (verdict == null) return unreported(words);

        if (verdict.isPerformed()) pressed = at;

        return verdict.worded(words + "; " + verdict.words);
    }

    /**
     * Build the verdict of a click that the client did not report, once the cycle in which the guard
     * was disarmed has passed, so that the next click is never made in that same cycle.
     */
    private Verdict unreported(String words) throws BridgeException {
        game.awaitCycles(1);

        return Verdict.missed(words + "; the client reported no click within " + CLICK_CYCLES + " cycles");
    }

    /** Build the verdict of a missed attempt, after moving the mouse off an open menu so that it closes. */
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
        Offer offer = game.onClientThread(client -> Offer.read(client, target, aim));
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
        game.awaitCycles(1);

        if (tile == null) return Verdict.missed(words);

        deadline = Math.max(deadline, began + CAMERA_CYCLES);

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
        out.add("pressed", point(pressed));
        JsonArray words = new JsonArray();
        tried.forEach(words::add);
        out.add("tried", words);

        if (camera.describe() != null) out.add("camera", camera.describe());

        return out;
    }

    /** Build the JSON array of the x and the y of the canvas point. */
    private static JsonArray point(Point point) {
        JsonArray out = new JsonArray();
        out.add(point.x);
        out.add(point.y);

        return out;
    }

    /** Build the failure for a deadline that passed, with what happened at the last attempt. */
    private BridgeException expired() {
        String last = reads > 0 && offeredMore == 0 ? BLOCKED : tried.peekLast();
        String message = "aimed at " + target.label() + "; " + last + " at the last of " + attempts + " attempts";

        return new BridgeException("wrong_state", message);
    }
}
