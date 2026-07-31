package com.alin.musicnotation.renderer;

import com.alin.musicnotation.model.Measure;
import com.alin.musicnotation.model.Note;
import com.alin.musicnotation.model.repeat.BarlineType;
import com.alin.musicnotation.model.layout.NoteLayout;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.Map;
import java.util.Set;

public final class MeasurePainter {

    private final TempoRenderer tempoRenderer;
    private final TextExpressionRenderer textExpressionRenderer;
    private final NoteRenderer noteRenderer;
    private final BeamRenderer beamRenderer;
    private final TupletRenderer tupletRenderer;
    private final SlurRenderer slurRenderer;
    private final ArticulationRenderer articulationRenderer;
    private final DynamicRenderer dynamicRenderer;
    private final HairpinRenderer hairpinRenderer;
    private final RepeatRenderer repeatRenderer;
    private final ClefRenderer clefRenderer;
    private final KeySignatureRenderer keySignatureRenderer;
    private final TimeSignatureRenderer timeSignatureRenderer;

    public MeasurePainter(
            TempoRenderer tempoRenderer,
            TextExpressionRenderer textExpressionRenderer,
            NoteRenderer noteRenderer,
            BeamRenderer beamRenderer,
            TupletRenderer tupletRenderer,
            SlurRenderer slurRenderer,
            ArticulationRenderer articulationRenderer,
            DynamicRenderer dynamicRenderer,
            HairpinRenderer hairpinRenderer,
            RepeatRenderer repeatRenderer,
            ClefRenderer clefRenderer,
            KeySignatureRenderer keySignatureRenderer,
            TimeSignatureRenderer timeSignatureRenderer) {

        this.tempoRenderer = tempoRenderer;
        this.textExpressionRenderer = textExpressionRenderer;
        this.noteRenderer = noteRenderer;
        this.beamRenderer = beamRenderer;
        this.tupletRenderer = tupletRenderer;
        this.slurRenderer = slurRenderer;
        this.articulationRenderer = articulationRenderer;
        this.dynamicRenderer = dynamicRenderer;
        this.hairpinRenderer = hairpinRenderer;
        this.repeatRenderer = repeatRenderer;
        this.clefRenderer = clefRenderer;
        this.keySignatureRenderer = keySignatureRenderer;
        this.timeSignatureRenderer = timeSignatureRenderer;
    }

    public void paint(
            GraphicsContext gc,
            Measure measure,
            Map<Note, NoteLayout> layoutMap,
            Set<Note> beamedNotes,
            double firstLineY,
            double middleLineY,
            double startX,
            double startPadding,
            double extraShiftX,
            double contentWidth,
            RenderingState oldState,
            RenderingState newState,
            RenderConfig config) {

        var staff = config.staff();
        var note = config.note();
        var beam = config.beam();
        var tuplet = config.tuplet();
        var slur = config.slur();
        var articulation = config.articulation();
        var dynamic = config.dynamic();
        var measureConfig = config.measure();

        double staffHeight = StaffGeometry.getStaffHeight(staff.lineSpacing());
        double lineSpacing = staff.lineSpacing();

        // 1. Tempo și text expressions (se desenează la începutul măsurii, fără padding)
        tempoRenderer.draw(gc, measure, startX, firstLineY, lineSpacing);
        textExpressionRenderer.draw(gc, measure, startX, firstLineY, staffHeight, lineSpacing);

        // 2. Desenează schimbările de clef, key, time la poziția startX + startPadding
        double currentX = startX + startPadding;

        // Clef change (doar dacă este diferit)
        if (measure.clef() != null && measure.clef() != oldState.currentClef()) {
            clefRenderer.drawClef(gc, currentX + 6, firstLineY, staffHeight, lineSpacing, measure.clef());
            currentX += 44 + 6;
        }

        // Key signature change (doar dacă este diferită)
        if (measure.keySignature() != null && !measure.keySignature().equals(oldState.currentKey())) {
            double keyWidth = keySignatureRenderer.drawChange(gc, currentX, firstLineY,
                    oldState.currentKey(), measure.keySignature(), newState.currentClef(), lineSpacing);
            currentX += keyWidth;
        }

        // Time signature change (doar dacă este diferit)
        if (measure.timeSignature() != null && !measure.timeSignature().equals(oldState.currentTime())) {
            Font font = Font.font("Serif", FontWeight.BOLD, lineSpacing * 2.2);
            double timeWidth = timeSignatureRenderer.drawChange(gc, currentX, firstLineY,
                    staffHeight, lineSpacing, measure.timeSignature());
            currentX += timeWidth + 10;
        }

        // 3. Notele și elementele asociate (layoutMap conține deja offset-ul corect)
        noteRenderer.draw(gc, measure, layoutMap, firstLineY, middleLineY, staff.middleLine(),
                note.headWidth(), note.headHeight(), note.stemLength(),
                note.ledgerExtension(), note.outlineWidth(),
                note.flagWidth(), note.flagHeight(), note.flagSpacing(),
                note.restWidth(), note.restHeight(), note.eighthRestSlant(),
                staff.topLine(), staff.bottomLine(), staff.lineSpacing(),
                beamedNotes, newState.currentKey());

        beamRenderer.draw(gc, measure, layoutMap, middleLineY, staff.middleLine(),
                beam.thickness(), beam.spacing(), beam.hookLength(),
                beam.maxSlope(), note.stemLength(), note.headWidth());

        tupletRenderer.draw(gc, measure, layoutMap, staff.middleLine(),
                tuplet.offset(), tuplet.bracketHeight(), tuplet.numberFontSize(),
                note.headWidth(), note.stemLength(), staff.lineSpacing());

        slurRenderer.draw(gc, measure, layoutMap, middleLineY, staff.middleLine(),
                slur.offset(), staff.lineSpacing(), note.headWidth());

        articulationRenderer.draw(gc, measure, layoutMap, middleLineY, staff.middleLine(),
                articulation.fontSizeFactor(), articulation.offset(),
                staff.lineSpacing(), note.headWidth());

        dynamicRenderer.draw(gc, measure, layoutMap, middleLineY,
                dynamic.fontSizeFactor(), dynamic.offset(), staff.lineSpacing());

        hairpinRenderer.draw(gc, measure, layoutMap, firstLineY, staffHeight,
                staff.lineSpacing(), note.headWidth(), note.headHeight());

        // 4. Barlines
        double endPadding = measureConfig.endPadding();
        double measureEndX = startX + startPadding + extraShiftX + contentWidth;
        double barX = measureEndX + endPadding;

        if (measure.hasStartRepeat()) {
            double leftBarX = startX - measureConfig.repeatGap();
            repeatRenderer.drawBarline(gc, leftBarX, firstLineY, staffHeight,
                    staff.lineSpacing(), BarlineType.START_REPEAT);
        }

        if (measure.hasEndRepeat()) {
            repeatRenderer.drawBarline(gc, barX, firstLineY, staffHeight,
                    staff.lineSpacing(), BarlineType.END_REPEAT);
        } else {
            repeatRenderer.drawBarline(gc, barX, firstLineY, staffHeight,
                    staff.lineSpacing(), BarlineType.SINGLE);
        }
    }
}