package com.alin.musicnotation.layout;

import com.alin.musicnotation.model.Note;

/**
 * Immutable DTO representing the horizontal position of a note or rest
 * within a measure. This is the output of the spacing engine and does
 * not contain any vertical information.
 */
public record HorizontalLayout(
        Note note,
        double x,
        int startSixteenths
) {
}