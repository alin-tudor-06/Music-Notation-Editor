package com.alin.musicnotation.editor;

import com.alin.musicnotation.model.Note;
import com.alin.musicnotation.model.Pitch;
import com.alin.musicnotation.model.clef.ClefType;

public final class HitTester {

    private HitTester() {}

    /** Caută o notă sub cursor. Returnează null dacă nu e niciuna. */
    public static Note findNoteAt(RenderedLayout layout, double mouseX, double mouseY) {
        Note best = null;
        double bestDist = Double.MAX_VALUE;
        for (RenderedLayout.NoteHitBox nb : layout.notes()) {
            double dx = mouseX - nb.centerX();
            double dy = mouseY - nb.centerY();
            double d2 = dx * dx + dy * dy;
            if (d2 <= nb.radius() * nb.radius() && d2 < bestDist) {
                bestDist = d2;
                best = nb.note();
            }
        }
        return best;
    }

    /** Caută măsura sub cursor. */
    public static RenderedLayout.MeasureHitBox findMeasureAt(
            RenderedLayout layout, double mouseX, double mouseY) {
        for (RenderedLayout.MeasureHitBox mb : layout.measures()) {
            if (mouseX >= mb.x() && mouseX <= mb.x() + mb.width()
                    && mouseY >= mb.y() && mouseY <= mb.y() + mb.height()) {
                return mb;
            }
        }
        return null;
    }

    /** Mouse Y → poziție pe portativ (0 = linia de sus, 8 = linia de jos). */
    public static int staffPositionFromY(RenderedLayout.MeasureHitBox mb, double mouseY) {
        double halfSpacing = mb.staffLineSpacing() / 2.0;
        double rawPos = (mouseY - mb.staffFirstLineY()) / halfSpacing;
        return (int) Math.round(rawPos);
    }

    /** Poziție pe portativ → Pitch (inversa lui StaffPositionCalculator). */
    public static Pitch pitchFromStaffPosition(int position, ClefType clef) {
        int refIdx = diatonicIndex(clef.referencePitch());
        int pitchIdx = refIdx - position;
        int octave = Math.floorDiv(pitchIdx, 7);
        int stepOrd = Math.floorMod(pitchIdx, 7);
        return new Pitch(Pitch.Step.values()[stepOrd], octave);
    }

    private static int diatonicIndex(Pitch p) {
        return p.octave() * 7 + p.step().ordinal();
    }
}