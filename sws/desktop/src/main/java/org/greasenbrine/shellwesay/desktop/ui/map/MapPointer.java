package org.greasenbrine.shellwesay.desktop.ui.map;

import java.awt.Color;

/**
 * Immutable pointer marker positioned in map-space coordinates.
 */
public record MapPointer(
        float mapX,
        float mapY,
        Color color,
        float radius,
        String label
) {

    /**
     * Creates a pointer with sanitized values safe for drawing.
     */
    public MapPointer {
        mapX = clamp01(mapX);
        mapY = clamp01(mapY);
        color = color == null ? Color.WHITE : color;
        radius = Math.max(1.0f, radius);
        label = label == null ? "" : label;
    }

    private static float clamp01(float value) {
        return Math.max(0.0f, Math.min(1.0f, value));
    }
}
