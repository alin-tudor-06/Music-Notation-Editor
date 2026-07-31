package com.alin.musicnotation.renderer;

import com.alin.musicnotation.model.Measure;
import com.alin.musicnotation.model.Note;
import com.alin.musicnotation.model.repeat.BarlineType;
import com.alin.musicnotation.model.layout.NoteLayout;
import javafx.scene.canvas.GraphicsContext;

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
            RepeatRenderer repeatRenderer) {

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
    }

    public void paint(
            GraphicsContext gc,
            Measure measure,
            Map<Note, NoteLayout> layoutMap,
            Set<Note> beamedNotes,
            double firstLineY,
            double middleLineY,
            double startX,
            double measureEndX,
            RenderingState state,
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
        double naturalEndX = startX + measureEndX;

        // 1. Tempo
        tempoRenderer.draw(gc, measure, startX, firstLineY, staff.lineSpacing());

        // 2. Text expressions
        textExpressionRenderer.draw(gc, measure, startX, firstLineY, staffHeight, staff.lineSpacing());

        // 3. Notes (heads, stems, flags, rests, ledger lines, accidentals)
        noteRenderer.draw(gc, measure, layoutMap, firstLineY, middleLineY, staff.middleLine(),
                note.headWidth(), note.headHeight(), note.stemLength(),
                note.ledgerExtension(), note.outlineWidth(),
                note.flagWidth(), note.flagHeight(), note.flagSpacing(),
                note.restWidth(), note.restHeight(), note.eighthRestSlant(),
                staff.topLine(), staff.bottomLine(), staff.lineSpacing(),
                beamedNotes, state.currentKey());

        // 4. Beams
        beamRenderer.draw(gc, measure, layoutMap, middleLineY, staff.middleLine(),
                beam.thickness(), beam.spacing(), beam.hookLength(),
                beam.maxSlope(), note.stemLength(), note.headWidth());

        // 5. Tuplets
        tupletRenderer.draw(gc, measure, layoutMap, staff.middleLine(),
                tuplet.offset(), tuplet.bracketHeight(), tuplet.numberFontSize(),
                note.headWidth(), note.stemLength(), staff.lineSpacing());

        // 6. Slurs
        slurRenderer.draw(gc, measure, layoutMap, middleLineY, staff.middleLine(),
                slur.offset(), staff.lineSpacing(), note.headWidth());

        // 7. Articulations
        articulationRenderer.draw(gc, measure, layoutMap, middleLineY, staff.middleLine(),
                articulation.fontSizeFactor(), articulation.offset(),
                staff.lineSpacing(), note.headWidth());

        // 8. Dynamics
        dynamicRenderer.draw(gc, measure, layoutMap, middleLineY,
                dynamic.fontSizeFactor(), dynamic.offset(), staff.lineSpacing());

        // 9. Hairpins
        hairpinRenderer.draw(gc, measure, layoutMap, firstLineY, staffHeight,
                staff.lineSpacing(), note.headWidth(), note.headHeight());

        // 10. Barlines
        if (measure.hasStartRepeat()) {
            double leftBarX = startX - measureConfig.repeatGap();
            repeatRenderer.drawBarline(gc, leftBarX, firstLineY, staffHeight,
                    staff.lineSpacing(), BarlineType.START_REPEAT);
        }

        double barX = naturalEndX + measureConfig.endPadding();
        if (measure.hasEndRepeat()) {
            repeatRenderer.drawBarline(gc, barX, firstLineY, staffHeight,
                    staff.lineSpacing(), BarlineType.END_REPEAT);
        } else {
            repeatRenderer.drawBarline(gc, barX, firstLineY, staffHeight,
                    staff.lineSpacing(), BarlineType.SINGLE);
        }
    }
}