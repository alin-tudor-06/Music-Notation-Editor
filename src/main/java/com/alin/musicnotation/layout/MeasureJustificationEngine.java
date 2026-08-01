package com.alin.musicnotation.layout;

import java.util.ArrayList;
import java.util.List;

/**
 * Computes a justification scale for each system (except the last) so that
 * the natural music content stretches to fill the available system width.
 * <p>
 * This engine is purely mathematical – it knows nothing about notes, clefs, or rendering.
 * It only operates on {@link MeasureLayout} widths and the system's available width.
 */
public final class MeasureJustificationEngine {

    private MeasureJustificationEngine() {
        // Utility class
    }

    /**
     * Computes a scale factor for each system.
     *
     * @param systems        the systems with natural measure widths
     * @param availableWidth the horizontal space available for music content
     * @return a new list of systems with {@code justificationScale} set
     */
    public static List<SystemLayout> justifySystems(List<SystemLayout> systems, double availableWidth) {
        List<SystemLayout> justified = new ArrayList<>();

        for (int i = 0; i < systems.size(); i++) {
            SystemLayout system = systems.get(i);
            boolean isLastSystem = (i == systems.size() - 1);

            double totalNaturalWidth = 0.0;
            double totalFixedWidth = 0.0;

            for (MeasureLayout ml : system.measureLayouts()) {
                totalNaturalWidth += ml.naturalWidth();
                totalFixedWidth += (ml.totalWidth() - ml.naturalWidth());
            }

            double scale = 1.0;
            if (!isLastSystem && totalNaturalWidth > 0) {
                double availableForNatural = availableWidth - totalFixedWidth;
                scale = availableForNatural / totalNaturalWidth;
                // Safety: never shrink below 1.0 (should not happen if SystemBreaker did its job)
                if (scale < 1.0) scale = 1.0;
            }

            // Create a new SystemLayout with the computed scale
            justified.add(new SystemLayout(
                    system.measureLayouts(),
                    scale
            ));
        }

        return justified;
    }
}