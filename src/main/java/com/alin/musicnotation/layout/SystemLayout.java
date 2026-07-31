package com.alin.musicnotation.layout;

import java.util.List;

/**
 * Layout information for a system (staff line).
 * This is a pure layout object – it does not reference the musical model.
 *
 * <p>A system contains one or more measures that fit on a single staff line.
 * Future milestones may add:
 * <ul>
 *   <li>y position on the page</li>
 *   <li>system height</li>
 *   <li>justified width</li>
 *   <li>page index</li>
 * </ul>
 */
public record SystemLayout(
        List<MeasureLayout> measureLayouts
) {
    /**
     * Creates a SystemLayout with the given measure layouts.
     *
     * @param measureLayouts the layouts of the measures in this system
     * @throws IllegalArgumentException if measureLayouts is null or empty
     */
    public SystemLayout {
        if (measureLayouts == null || measureLayouts.isEmpty()) {
            throw new IllegalArgumentException("System must contain at least one measure layout");
        }
        // Create a defensive copy
        measureLayouts = List.copyOf(measureLayouts);
    }
}