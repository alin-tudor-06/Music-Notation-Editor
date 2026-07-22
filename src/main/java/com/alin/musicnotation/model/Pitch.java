package com.alin.musicnotation.model;

public record Pitch(Step step, Accidental accidental, int octave) {

    private static final int[] STEP_SEMITONES = {0, 2, 4, 5, 7, 9, 11};

    public enum Step { C, D, E, F, G, A, B }

    public Pitch(Step step, int octave) {
        this(step, Accidental.NONE, octave);
    }

    public static Pitch of(String name) {
        Step step = Step.valueOf(name.substring(0, 1).toUpperCase());
        int index = 1;
        Accidental accidental = Accidental.NONE;
        if (index < name.length()) {
            char c = name.charAt(index);
            if (c == '#') { accidental = Accidental.SHARP; index++; }
            else if (c == 'b') { accidental = Accidental.FLAT; index++; }
            else if (c == 'n') { accidental = Accidental.NATURAL; index++; }
        }
        int octave = Integer.parseInt(name.substring(index));
        return new Pitch(step, accidental, octave);
    }

    public int toMidiNumber() {
        return (octave + 1) * 12 + STEP_SEMITONES[step.ordinal()] + accidental.semitoneOffset();
    }

    @Override
    public String toString() {
        String accidentalStr = switch (accidental) {
            case SHARP -> "#";
            case FLAT -> "b";
            case NATURAL -> "n";
            default -> "";
        };
        return step.name() + accidentalStr + octave;
    }
}