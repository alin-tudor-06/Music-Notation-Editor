package com.alin.musicnotation.renderer;

import com.alin.musicnotation.editor.RenderedLayout;
import com.alin.musicnotation.layout.PageLayout;
import com.alin.musicnotation.layout.SystemPlacement;
import com.alin.musicnotation.model.Note;
import javafx.scene.canvas.GraphicsContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class LayoutRenderer {

    private final SystemRenderer systemRenderer;
    private RenderedLayout lastLayout = RenderedLayout.empty();

    public LayoutRenderer(SystemRenderer systemRenderer) {
        this.systemRenderer = systemRenderer;
    }

    public void render(GraphicsContext gc,
                       PageLayout pageLayout,
                       List<MeasureRenderData> renderDataList,
                       RenderingState initialState,
                       Note highlightNote) {

        Map<Integer, MeasureRenderData> renderDataMap = new HashMap<>();
        for (MeasureRenderData data : renderDataList) {
            renderDataMap.put(data.measureIndex(), data);
        }

        RenderedLayout.Builder builder = new RenderedLayout.Builder();
        RenderingState state = initialState;
        List<SystemPlacement> placements = pageLayout.systemPlacements();

        for (int i = 0; i < placements.size(); i++) {
            SystemPlacement placement = placements.get(i);
            boolean isFirstSystem = (i == 0);
            state = systemRenderer.render(gc, placement, renderDataMap, state,
                    isFirstSystem, highlightNote, builder);
        }

        lastLayout = builder.build();
    }

    public RenderedLayout getLastLayout() {
        return lastLayout;
    }
}