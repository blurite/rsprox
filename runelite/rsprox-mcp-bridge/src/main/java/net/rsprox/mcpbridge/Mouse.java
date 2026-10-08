package net.rsprox.mcpbridge;

import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import net.runelite.api.Client;
import net.runelite.api.Point;

/** The real mouse of the client: synthetic events on its canvas, which the client treats as a player's input. */
final class Mouse {
    /** The milliseconds by which the timestamp of a release follows that of its press. No time passes between them. */
    private static final int CLICK_HOLD_MS = 30;

    /** The cycles the client is given to take a new pointer position and build its menu for it. */
    private static final int FOLLOW_CYCLES = 2;

    /** The refusal of input in stretched mode. */
    private static final String STRETCHED = "stretched mode is on, in which a mouse position is not a canvas position";

    /** A button of the mouse, with the names that AWT gives it. */
    enum Button {
        /** The left button. */
        LEFT(MouseEvent.BUTTON1, InputEvent.BUTTON1_DOWN_MASK),

        /** The right button. */
        RIGHT(MouseEvent.BUTTON3, InputEvent.BUTTON3_DOWN_MASK);

        /** The button as a mouse event names it. */
        final int awt;

        /** The modifier that a press of the button holds. */
        final int downMask;

        /** Create the button with its event names. */
        Button(int awt, int downMask) {
            this.awt = awt;
            this.downMask = downMask;
        }
    }

    /** The gateway to the canvas and the thread it must be used on. */
    private final GameAccess game;

    /** Create the mouse of the given game. */
    Mouse(GameAccess game) {
        this.game = game;
    }

    /**
     * Move the mouse to the canvas position. While the client tracks the pointer as off the canvas, it
     * is first brought onto it, as happens when a pointer enters a window.
     */
    private void move(int x, int y) throws BridgeException {
        boolean outside = game.onClientThread(Mouse::pointerOutside);
        game.input(canvas -> {
            long now = System.currentTimeMillis();

            if (outside) canvas.dispatchEvent(new MouseEvent(canvas, MouseEvent.MOUSE_ENTERED, now, 0, x, y, 0, false));

            canvas.dispatchEvent(new MouseEvent(canvas, MouseEvent.MOUSE_MOVED, now, 0, x, y, 0, false));
        });
    }

    /**
     * Move the mouse to the canvas position and wait until the client has built its menu for it. The
     * client takes the position on its next cycle and builds the menu while it draws the frame after.
     */
    void hover(int x, int y) throws BridgeException {
        move(x, y);
        game.awaitCycles(FOLLOW_CYCLES);
    }

    /** Press and release the button at the canvas position, where the mouse must already be. */
    void click(int x, int y, Button button) throws BridgeException {
        game.input(canvas -> {
            long now = System.currentTimeMillis();
            long released = now + CLICK_HOLD_MS;
            canvas.dispatchEvent(
                new MouseEvent(canvas, MouseEvent.MOUSE_PRESSED, now, button.downMask, x, y, 1, false, button.awt));

            canvas.dispatchEvent(
                new MouseEvent(canvas, MouseEvent.MOUSE_RELEASED, released, 0, x, y, 1, false, button.awt));

            canvas.dispatchEvent(
                new MouseEvent(canvas, MouseEvent.MOUSE_CLICKED, released, 0, x, y, 1, false, button.awt));
        });
    }

    /**
     * Check that canvas units are what the client's mouse handling expects. Throws {@code wrong_state}
     * in stretched mode, where the canvas is drawn at another size than its coordinates.
     */
    static void requireUnstretched(Client client) throws BridgeException {
        if (client.isStretchedEnabled()) throw new BridgeException("wrong_state", STRETCHED);
    }

    /** Determine if the client tracks the pointer as off the canvas, which it reports as a negative position. */
    private static boolean pointerOutside(Client client) {
        Point tracked = client.getMouseCanvasPosition();

        return tracked == null || tracked.getX() < 0 || tracked.getY() < 0;
    }
}
