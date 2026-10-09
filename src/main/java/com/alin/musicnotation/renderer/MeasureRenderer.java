package com.alin.musicnotation.renderer;

import com.alin.musicnotation.layout.LayoutScaler;
import com.alin.musicnotation.layout.MeasureSpacingResult;
import com.alin.musicnotation.model.Measure;
import com.alin.musicnotation.model.Note;
import com.alin.musicnotation.model.layout.NoteLayout;
import javafx.scene.canvas.GraphicsContext;

import java.util.Map;

public final class MeasureRenderer {

    private final RenderConfig config;
    private final MeasurePainter painter;

    public MeasureRenderer(RenderConfig config, MeasurePainter painter) {
        this.config = config;
        this.painter = painter;
    }

    public RenderingState render(GraphicsContext gc,
                                 MeasureRenderData data,
                                 double startX,
                                 double firstLineY,
                                 RenderingState state,
                                 Note highlightNote) {

        Measure measure = data.measure();
        MeasureSpacingResult spacingResult = data.spacingResult();
        double scale = data.justificationScale();

        RenderingState measureState = RenderingStateMachine.advance(state, measure);

        Map<Note, NoteLayout> naturalLayouts = NoteLayoutFactory.buildNoteLayouts(
                spacingResult,
                0.0,
                firstLineY,
                config.staff(),
                config.note(),
                measureState.currentClef()
        );

        Map<Note, NoteLayout> justifiedLayouts = LayoutScaler.scaleLayouts(
                naturalLayouts,
                0.0,
                scale
        );

        painter.paint(gc,
                measure,
                justifiedLayouts,
                firstLineY,
                startX,
                data.extraWidth(),
                data.justifiedWidth(),
                state,
                measureState,
                config,
                data.isLastMeasure(),
                highlightNote
        );

        return measureState;
    }
}