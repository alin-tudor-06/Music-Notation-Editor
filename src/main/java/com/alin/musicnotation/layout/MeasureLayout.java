package com.alin.musicnotation.layout;

public record MeasureLayout(
        int measureIndex,
        double naturalWidth,
        double totalWidth   // include startPadding + extraWidth + naturalWidth + endPadding
) {
}