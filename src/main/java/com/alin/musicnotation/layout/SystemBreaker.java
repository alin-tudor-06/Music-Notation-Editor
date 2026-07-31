package com.alin.musicnotation.layout;

import java.util.ArrayList;
import java.util.List;

/**
 * Computes system breaks by packing measures into systems (lines)
 * based on their natural widths and the available width.
 *
 * <p>This utility uses a simple greedy packing algorithm:
 * measures are added to the current system until the next measure
 * would exceed the available width, then a new system is started.
 */
public final class SystemBreaker {

    private SystemBreaker() {
        // Utility class - no instantiation
    }

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

            if (!currentSystem.isEmpty() && currentWidth + width > availableWidth) {
                systems.add(new SystemLayout(List.copyOf(currentSystem)));
                currentSystem.clear();
                currentWidth = 0.0;
            }

            currentSystem.add(ml);
            currentWidth += width;
        }

        if (!currentSystem.isEmpty()) {
            systems.add(new SystemLayout(List.copyOf(currentSystem)));
        }

        return systems;
    }
}