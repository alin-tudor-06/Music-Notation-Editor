package com.alin.musicnotation.layout;

import com.alin.musicnotation.renderer.MeasureRenderData;

import java.util.List;

public record PageLayoutResult(
        PageLayout pageLayout,
        List<MeasureRenderData> renderDataList
) {
}