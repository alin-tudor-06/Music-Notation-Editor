package com.alin.musicnotation.layout;

import com.alin.musicnotation.model.Note;
import com.alin.musicnotation.model.layout.NoteLayout;

import java.util.HashMap;
import java.util.Map;

/**
 * Immutable utility that scales the x‑coordinates of all {@link NoteLayout}s
 * relative to a given origin.
 * <p>
 * This is used after natural layouts are built, to apply horizontal justification.
 * The original layout map is never modified.
 */
public final class LayoutScaler {

    private static final double EPSILON = 1e-9;

    private LayoutScaler() {
        // Utility class
    }

    /**
     * Returns a new map of layouts with x‑coordinates scaled by {@code scale}
     * relative to {@code originX}.
     * <p>
     * If {@code scale} is approximately 1.0, the original map is returned directly
     * (no copy is made).
     *
     * @param original the natural layouts (unscaled)
     * @param originX  the reference x‑coordinate (typically 0)
     * @param scale    the scale factor (> 0)
     * @return a new map with scaled layouts, or the original if scale ≈ 1
     */
    public static Map<Note, NoteLayout> scaleLayouts(Map<Note, NoteLayout> original,
                                                     double originX,
                                                     double scale) {
        if (Math.abs(scale - 1.0) < EPSILON) {
            return original;
        }

        Map<Note, NoteLayout> scaled = new HashMap<>(original.size());
        for (Map.Entry<Note, NoteLayout> entry : original.entrySet()) {
            NoteLayout layout = entry.getValue();
            double newX = originX + (layout.x() - originX) * scale;
            NoteLayout newLayout = new NoteLayout(
                    newX,
                    layout.y(),
                    layout.note(),
                    layout.position(),
                    layout.startSixteenths()
            );
            scaled.put(entry.getKey(), newLayout);
        }
        return scaled;
    }
}