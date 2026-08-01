package com.alin.musicnotation.renderer;

import com.alin.musicnotation.layout.PageLayout;
import com.alin.musicnotation.layout.SystemPlacement;
import javafx.scene.canvas.GraphicsContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class LayoutRenderer {

    private final SystemRenderer systemRenderer;

    public LayoutRenderer(SystemRenderer systemRenderer) {
        this.systemRenderer = systemRenderer;
    }

    public void render(GraphicsContext gc,
                       PageLayout pageLayout,
                       List<MeasureRenderData> renderDataList,
                       RenderingState initialState) {

        Map<Integer, MeasureRenderData> renderDataMap = new HashMap<>();
        for (MeasureRenderData data : renderDataList) {
            renderDataMap.put(data.measureIndex(), data);
        }

        RenderingState state = initialState;
        List<SystemPlacement> placements = pageLayout.systemPlacements();

        for (int i = 0; i < placements.size(); i++) {
            SystemPlacement placement = placements.get(i);
            boolean isFirstSystem = (i == 0);
            state = systemRenderer.render(gc, placement, renderDataMap, state, isFirstSystem);
        }
    }
}