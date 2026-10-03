package net.rsprox.mcpbridge;

import net.runelite.api.Client;
import net.runelite.api.events.MenuOptionClicked;

/**
 * Decides, on the click the client is about to perform, whether it is the intended one. It is armed
 * just before a click of ours and stays armed until the client reports a click or the interaction
 * gives up on it, so that a click that is not ours is never judged.
 */
final class ClickGuard {
    /** What became of one attempt. */
    enum Outcome {
        /** The client performed the intended action. */
        PERFORMED,

        /** The client performed nothing. */
        MISSED,

        /** The client was stopped from performing another entry at a point where it also offered the intended one. */
        ANOTHER_PREFERRED,
    }

    /** The outcome of one attempt, with what happened in words. */
    static final class Verdict {
        /** What became of the attempt. */
        final Outcome outcome;

        /** What happened, in a few words. */
        final String words;

        /** The client's tick count when the action was let through, or -1 when it was not. */
        final int tick;

        /** Create a verdict. */
        private Verdict(Outcome outcome, String words, int tick) {
            this.outcome = outcome;
            this.words = words;
            this.tick = tick;
        }

        /** Build the verdict of an attempt that the client performed on the given tick. */
        private static Verdict performed(String words, int tick) {
            return new Verdict(Outcome.PERFORMED, words, tick);
        }

        /** Build the verdict of an attempt that performed nothing. */
        static Verdict missed(String words) {
            return new Verdict(Outcome.MISSED, words, -1);
        }

        /** Determine if the client performed the intended action. */
        boolean isPerformed() {
            return outcome == Outcome.PERFORMED;
        }

        /** Get the same verdict with other words. */
        Verdict worded(String words) {
            return new Verdict(outcome, words, tick);
        }
    }

    /** The target whose option the click must perform. */
    private final Target target;

    /** Whether a click of ours is pending. */
    private boolean armed;

    /** The verdict on the pending click, or null until the client reports one. */
    private Verdict verdict;

    /** Create the guard for the target. */
    ClickGuard(Target target) {
        this.target = target;
    }

    /** Expect a click of ours next. */
    synchronized void arm() {
        armed = true;
        verdict = null;
    }

    /** Determine if the client has reported the pending click. */
    synchronized boolean isSettled() {
        return verdict != null;
    }

    /**
     * Stop expecting a click, and get the verdict, or null when the client reported none. Called on
     * the client thread, where the client reports its clicks, so that no report can slip in between.
     */
    synchronized Verdict disarm() {
        armed = false;

        return verdict;
    }

    /**
     * Judge the click the client is about to perform. The intended one is let through. Any other is
     * consumed, which makes the client send nothing for it.
     */
    synchronized void on(Client client, MenuOptionClicked event) {
        if (!armed) return;

        armed = false;
        verdict = judge(client, event);
    }

    /** Let the intended click through and consume any other, and say which it was. */
    private Verdict judge(Client client, MenuOptionClicked event) {
        String words = Offer.words(event.getMenuEntry());

        if (event.isConsumed()) return Verdict.missed("another plugin consumed the click on " + words);

        if (target.matches(event.getMenuEntry(), client)) return performed(client, words);

        event.consume();

        return cancelled(client, words);
    }

    /** Build the verdict of the intended click, which the client performs on its current tick. */
    private static Verdict performed(Client client, String words) {
        return Verdict.performed("the client performed " + words, client.getTickCount());
    }

    /**
     * Build the verdict of a cancelled click. When the menu still holds the intended entry, the client
     * preferred another entry to it at this point, which a click through the open menu gets around.
     */
    private Verdict cancelled(Client client, String words) {
        boolean offered = Offer.read(client, target, null).match >= 0;
        Outcome outcome = offered ? Outcome.ANOTHER_PREFERRED : Outcome.MISSED;

        return new Verdict(outcome, "the click would have performed " + words + ", so it was cancelled", -1);
    }
}
