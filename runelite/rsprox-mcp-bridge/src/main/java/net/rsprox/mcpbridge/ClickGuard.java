package net.rsprox.mcpbridge;

import net.runelite.api.Client;
import net.runelite.api.events.MenuOptionClicked;

/**
 * Decides, on the click the client is about to perform, whether it is the intended one. It is armed
 * just before a click of ours and disarmed on the first click the client reports, so that a click
 * that is not ours is never judged.
 */
final class ClickGuard {
    /** What became of one attempt. */
    static final class Verdict {
        /** Whether the client performed the intended action. */
        final boolean passed;

        /** What happened, in a few words. */
        final String words;

        /** The client's tick count when the action was let through, or -1 when it was not. */
        final int tick;

        /** Create a verdict. */
        private Verdict(boolean passed, String words, int tick) {
            this.passed = passed;
            this.words = words;
            this.tick = tick;
        }

        /** Build the verdict of an attempt that the client performed on the given tick. */
        static Verdict passed(String words, int tick) {
            return new Verdict(true, words, tick);
        }

        /** Build the verdict of an attempt that performed nothing. */
        static Verdict missed(String words) {
            return new Verdict(false, words, -1);
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
    synchronized boolean settled() {
        return verdict != null;
    }

    /** Stop expecting a click, and get the verdict, which is the given words when the client reported none. */
    synchronized Verdict disarm(String whenNone) {
        armed = false;

        return verdict != null ? verdict : Verdict.missed(whenNone);
    }

    /**
     * Judge the click the client is about to perform. The intended one is let through. Any other is
     * consumed, which makes the client send nothing for it.
     */
    synchronized void on(Client client, MenuOptionClicked event) {
        if (!armed) return;

        armed = false;
        String words = Offer.words(event.getMenuEntry());

        if (event.isConsumed()) {
            verdict = Verdict.missed("another plugin consumed the click on " + words);
        } else if (target.matches(event.getMenuEntry(), client)) {
            verdict = Verdict.passed("the client performed " + words, client.getTickCount());
        } else {
            event.consume();
            verdict = Verdict.missed("the click would have performed " + words + ", so it was cancelled");
        }
    }
}
