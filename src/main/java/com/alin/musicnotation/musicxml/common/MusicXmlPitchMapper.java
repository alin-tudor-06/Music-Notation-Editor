package com.alin.musicnotation.musicxml.common;

import com.alin.musicnotation.model.Pitch;
import com.alin.musicnotation.model.Accidental;

public final class MusicXmlPitchMapper {

    private MusicXmlPitchMapper() {}

    public static String getStep(Pitch pitch) {
        return pitch.step().name();
    }

    public static Integer getAlter(Pitch pitch) {
        Accidental acc = pitch.accidental();
        if (acc == Accidental.NONE) return null;
        return switch (acc) {
            case SHARP -> 1;
            case FLAT -> -1;
            case NATURAL -> 0;
            default -> null;
        };
    }

    public static int getOctave(Pitch pitch) {
        return pitch.octave();
    }

    public static Pitch toPitch(String step, Integer alter, int octave) {
        Accidental accidental = (alter == null) ? Accidental.NONE
                : switch (alter) {
            case 0 -> Accidental.NATURAL;
            case 1 -> Accidental.SHARP;
            case -1 -> Accidental.FLAT;
            default -> throw new IllegalArgumentException("Invalid alter: " + alter);
        };
        return new Pitch(Pitch.Step.valueOf(step), accidental, octave);
    }
}