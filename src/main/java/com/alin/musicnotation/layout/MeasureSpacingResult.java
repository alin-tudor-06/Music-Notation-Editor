package com.alin.musicnotation.layout;

import java.util.List;

/**
 * Result of a measure spacing computation.
 * Contains the horizontal layouts for all notes/rests and the final cursor X.
 */
public record MeasureSpacingResult(
        List<HorizontalLayout> layouts,
        double finalCursorX
) {
}