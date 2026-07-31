package com.alin.musicnotation.layout;

import com.alin.musicnotation.model.Note;

public record HorizontalLayout(
        Note note,
        double x,
        int startSixteenths
) {
}