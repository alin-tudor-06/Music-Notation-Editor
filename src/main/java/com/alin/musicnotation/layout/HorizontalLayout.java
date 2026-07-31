package com.alin.musicnotation.layout;

import com.alin.musicnotation.model.Note;

/**
 * Represents the horizontal position of a single note or rest within a measure.
 * This is the output of the spacing engine and contains no vertical information.
 */
public record HorizontalLayout(
        Note note,
        double x,
        int startSixteenths
) {
}