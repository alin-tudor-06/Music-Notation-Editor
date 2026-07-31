package com.alin.musicnotation.renderer;

import com.alin.musicnotation.layout.MeasureSpacingResult;
import com.alin.musicnotation.model.Measure;
import com.alin.musicnotation.model.Note;
import com.alin.musicnotation.model.layout.NoteLayout;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.*;

public final class MeasureRenderer {

    private final RenderConfig config;
    private final MeasurePainter painter;
    private final KeySignatureRenderer keySignatureRenderer;
    private final TimeSignatureRenderer timeSignatureRenderer;

    public MeasureRenderer(RenderConfig config,
                           MeasurePainter painter,
                           KeySignatureRenderer keySignatureRenderer,
                           TimeSignatureRenderer timeSignatureRenderer) {
        this.config = config;
        this.painter = painter;
        this.keySignatureRenderer = keySignatureRenderer;
        this.timeSignatureRenderer = timeSignatureRenderer;
    }

    public MeasureRenderResult render(GraphicsContext gc,
                                      MeasureRenderData data,
                                      double startX,
                                      double firstLineY,
                                      RenderingState state) {
        Measure measure = data.measure();
        MeasureSpacingResult spacingResult = data.spacingResult();

        RenderingState newState = RenderingStateMachine.advance(state, measure);

        double extraWidth = 0;
        double lineSpacing = config.staff().lineSpacing();

        // Clef change
        if (measure.clef() != null && measure.clef() != state.currentClef()) {
            extraWidth += 44 + 6;
        }

        // Key signature change
        if (measure.keySignature() != null && !measure.keySignature().equals(state.currentKey())) {
            extraWidth += keySignatureRenderer.calculateChangeWidth(state.currentKey(), measure.keySignature(), lineSpacing);
        }

        // Time signature change
        if (measure.timeSignature() != null && !measure.timeSignature().equals(state.currentTime())) {
            Font font = Font.font("Serif", FontWeight.BOLD, lineSpacing * 2.2);
            extraWidth += timeSignatureRenderer.calculateWidth(font, measure.timeSignature()) + 10;
        }

        double startPadding = config.measure().startPadding();
        double noteStartX = startX + startPadding + extraWidth;

        Map<Note, NoteLayout> layoutMap = NoteLayoutFactory.buildNoteLayouts(
                spacingResult, noteStartX, firstLineY,
                config.staff(), config.note(), newState.currentClef()
        );

        List<NoteLayout> layouts = new ArrayList<>(layoutMap.values());
        layouts.sort(Comparator.comparingDouble(NoteLayout::x));
        Set<Note> beamedNotes = BeamAnalyzer.computeBeamedNotes(layouts);

        double middleLineY = firstLineY + config.staff().middleLine() * (config.staff().lineSpacing() / 2);

        painter.paint(gc, measure, layoutMap, beamedNotes,
                firstLineY, middleLineY, startX, startPadding, extraWidth,
                spacingResult.finalCursorX(), state, newState, config);

        double totalWidth = startPadding + extraWidth + spacingResult.finalCursorX() + config.measure().endPadding();
        return new MeasureRenderResult(newState, totalWidth);
    }
}