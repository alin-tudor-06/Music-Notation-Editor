package com.alin.musicnotation.renderer;

import com.alin.musicnotation.layout.MeasureSpacingResult;
import com.alin.musicnotation.model.Measure;

public record MeasureRenderData(
        int measureIndex,
        Measure measure,
        MeasureSpacingResult spacingResult,
        double extraWidth,
        double justifiedWidth,
        double justificationScale
) {
}