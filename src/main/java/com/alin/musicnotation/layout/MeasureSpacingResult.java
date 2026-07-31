package com.alin.musicnotation.layout;

import java.util.List;

public record MeasureSpacingResult(
        List<HorizontalLayout> layouts,
        double finalCursorX
) {
}