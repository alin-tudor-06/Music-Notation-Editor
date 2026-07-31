package com.alin.musicnotation.renderer;

import com.alin.musicnotation.model.Pitch;
import com.alin.musicnotation.model.clef.ClefType;

public final class StaffPositionCalculator {

    private StaffPositionCalculator() {
        // Utility class - no instantiation
    }

    public static int staffPosition(Pitch pitch, ClefType clef) {
        return diatonicIndex(clef.referencePitch()) - diatonicIndex(pitch);
    }

    private static int diatonicIndex(Pitch pitch) {
        return pitch.octave() * 7 + pitch.step().ordinal();
    }
}