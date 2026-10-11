package com.alin.musicnotation.renderer;

import com.alin.musicnotation.editor.RenderedLayout;
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
                                 Note highlightNote,
                                 RenderedLayout.Builder builder,
                                 boolean isFirstMeasureOfSystem) {

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

        // Populăm hit-box-urile notelor
        double startPadding = config.measure().startPadding();
        double noteOffsetX = startX + startPadding + data.extraWidth();
        for (NoteLayout nl : justifiedLayouts.values()) {
            if (!nl.note().isRest()) {
                builder.addNote(
                        nl.note(),
                        noteOffsetX + nl.x(),
                        nl.y(),
                        14
                );
            }
        }

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
                highlightNote,
                isFirstMeasureOfSystem
        );

        return measureState;
    }
}