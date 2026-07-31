package com.alin.musicnotation.renderer;

import com.alin.musicnotation.layout.MeasureSpacingResult;
import com.alin.musicnotation.model.Measure;
import com.alin.musicnotation.model.Note;
import com.alin.musicnotation.model.layout.NoteLayout;
import javafx.scene.canvas.GraphicsContext;

import java.util.*;

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
                                 RenderingState state) {
        Measure measure = data.measure();
        MeasureSpacingResult spacingResult = data.spacingResult();

        Map<Note, NoteLayout> layoutMap = NoteLayoutFactory.buildNoteLayouts(
                spacingResult, startX, firstLineY,
                config.staff(), config.note(), state.currentClef()
        );

        List<NoteLayout> layouts = new ArrayList<>(layoutMap.values());
        layouts.sort(Comparator.comparingDouble(NoteLayout::x));
        Set<Note> beamedNotes = BeamAnalyzer.computeBeamedNotes(layouts);

        double middleLineY = firstLineY + config.staff().middleLine() * (config.staff().lineSpacing() / 2);

        painter.paint(gc, measure, layoutMap, beamedNotes,
                firstLineY, middleLineY, startX, spacingResult.finalCursorX(),
                state, config);

        return RenderingStateMachine.advance(state, measure);
    }
}