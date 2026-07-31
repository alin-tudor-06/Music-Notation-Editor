package com.alin.musicnotation.layout;

import java.util.ArrayList;
import java.util.List;

/**
 * Breaks a list of measure layouts into systems (lines) based on their natural widths
 * and the available width of each system.
 *
 * <p>This engine performs only one task: it decides which measures belong together
 * on the same system. It does NOT compute spacing, justify measures, or perform
 * page layout.
 *
 * <p>The algorithm is a simple greedy packing: measures are added to the current
 * system until the next measure would exceed the available width, then a new
 * system is started.
 *
 * <p><b>Usage:</b>
 * <pre>
 * List&lt;MeasureLayout&gt; measureLayouts = ...;
 * double availableWidth = 700.0;
 * List&lt;SystemLayout&gt; systems = SystemBreaker.computeSystems(measureLayouts, availableWidth);
 * </pre>
 */
public final class SystemBreaker {

    private SystemBreaker() {
        // Utility class - no instantiation
    }

    /**
     * Breaks the given measure layouts into systems.
     *
     * @param measureLayouts  list of measure layouts with their natural widths
     * @param availableWidth  the total width available for a system (in pixels)
     * @return a list of systems, each system containing a list of MeasureLayout objects
     * @throws IllegalArgumentException if any measure width is negative,
     *                                  or if availableWidth is not positive
     */
    public static List<SystemLayout> computeSystems(
            List<MeasureLayout> measureLayouts,
            double availableWidth) {

        if (measureLayouts == null || measureLayouts.isEmpty()) {
            return List.of();
        }
        if (availableWidth <= 0) {
            throw new IllegalArgumentException("Available width must be positive: " + availableWidth);
        }

        List<SystemLayout> systems = new ArrayList<>();
        List<MeasureLayout> currentSystem = new ArrayList<>();
        double currentWidth = 0.0;

        for (MeasureLayout ml : measureLayouts) {
            double width = ml.naturalWidth();
            if (width < 0) {
                throw new IllegalArgumentException("Measure width cannot be negative: " + width);
            }

            // If adding this measure would exceed the available width,
            // finish the current system and start a new one.
            if (!currentSystem.isEmpty() && currentWidth + width > availableWidth) {
                systems.add(new SystemLayout(List.copyOf(currentSystem)));
                currentSystem.clear();
                currentWidth = 0.0;
            }

            currentSystem.add(ml);
            currentWidth += width;
        }

        // Add the last system if it contains any measures.
        if (!currentSystem.isEmpty()) {
            systems.add(new SystemLayout(List.copyOf(currentSystem)));
        }

        return systems;
    }
}