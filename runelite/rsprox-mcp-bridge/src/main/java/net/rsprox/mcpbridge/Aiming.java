package net.rsprox.mcpbridge;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;

/**
 * Picks the canvas point to aim at inside a clickable shape. The centre comes first, and later
 * attempts move to other interior points, because another entity or an interface may cover a part
 * of the shape.
 */
final class Aiming {
    /** The positions to try within the bounds of a shape, as fractions of its width and height. */
    private static final double[][] POSITIONS = {
        {0.5, 0.5}, {0.5, 0.25}, {0.5, 0.75}, {0.25, 0.5}, {0.75, 0.5},
        {0.25, 0.25}, {0.75, 0.75}, {0.25, 0.75}, {0.75, 0.25},
    };

    /** Prevent the creation of instances. */
    private Aiming() {
        //
    }

    /**
     * Get the point of the shape to aim at on the given attempt, or null when no tried point of the
     * shape lies within the area. The attempt chooses which position comes first, so attempts differ.
     */
    static Point point(Shape shape, Rectangle area, int attempt) {
        Rectangle bounds = shape.getBounds().intersection(area);
        if (bounds.isEmpty()) return null;

        for (int i = 0; i < POSITIONS.length; i++) {
            double[] position = POSITIONS[(attempt - 1 + i) % POSITIONS.length];
            int x = bounds.x + (int) (bounds.width * position[0]);
            int y = bounds.y + (int) (bounds.height * position[1]);

            if (shape.contains(x, y) && area.contains(x, y)) return new Point(x, y);
        }

        return null;
    }
}
