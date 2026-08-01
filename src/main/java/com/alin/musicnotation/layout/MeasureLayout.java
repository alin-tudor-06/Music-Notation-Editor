package com.alin.musicnotation.layout;

public record MeasureLayout(
        int measureIndex,
        double naturalWidth,
        double totalWidth,
        double justificationScale
) {
    // Constructor pentru compatibilitate cu codul vechi (scale = 1.0)
    public MeasureLayout(int measureIndex, double naturalWidth, double totalWidth) {
        this(measureIndex, naturalWidth, totalWidth, 1.0);
    }
}