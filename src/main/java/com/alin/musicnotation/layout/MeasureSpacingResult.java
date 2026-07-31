package com.alin.musicnotation.layout;

import java.util.List;

/**
 * Result of a measure spacing computation.
 * Contains the horizontal layouts and the final cursor X position.
 */
public record MeasureSpacingResult(
        List<HorizontalLayout> layouts,
        double finalCursorX
) {
}