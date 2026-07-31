package com.alin.musicnotation.layout;

import java.util.ArrayList;
import java.util.List;

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
            double width = ml.totalWidth();   // folosește lățimea totală (cu padding și schimbări)
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