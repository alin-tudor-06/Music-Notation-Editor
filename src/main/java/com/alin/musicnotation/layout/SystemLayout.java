package com.alin.musicnotation.layout;

import java.util.List;

public record SystemLayout(
        List<MeasureLayout> measureLayouts,
        double justificationScale      // adăugat
) {
    // Constructor pentru compatibilitate cu codul vechi (scale = 1.0)
    public SystemLayout(List<MeasureLayout> measureLayouts) {
        this(measureLayouts, 1.0);
    }
}