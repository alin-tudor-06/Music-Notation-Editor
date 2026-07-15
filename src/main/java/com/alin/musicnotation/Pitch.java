package com.alin.musicnotation;

public record Pitch(Step step, Accidental accidental, int octave) {

    private static final int[] STEP_SEMITONES = {0, 2, 4, 5, 7, 9, 11};

    public enum Step {
        C, D, E, F, G, A, B
    }

    /**
     * Constructor simplu pentru note fără alterație (NONE).
     */
    public Pitch(Step step, int octave) {
        this(step, Accidental.NONE, octave);
    }

    /**
     * Parsează un pitch din notația științifică, e.g. "C4", "F#5", "Bb3", "Fn4".
     */
    public static Pitch of(String name) {
        Step step = Step.valueOf(name.substring(0, 1).toUpperCase());

        int index = 1;
        Accidental accidental = Accidental.NONE;
        if (index < name.length()) {
            char c = name.charAt(index);
            if (c == '#') {
                accidental = Accidental.SHARP;
                index++;
            } else if (c == 'b') {
                accidental = Accidental.FLAT;
                index++;
            } else if (c == 'n') {
                accidental = Accidental.NATURAL;
                index++;
            }
        }

        int octave = Integer.parseInt(name.substring(index));
        return new Pitch(step, accidental, octave);
    }

    /**
     * Numărul MIDI al notei (C4 = 60).
     */
    public int toMidiNumber() {
        return (octave + 1) * 12 + STEP_SEMITONES[step.ordinal()] + accidental.semitoneOffset();
    }

    @Override
    public String toString() {
        String accidentalStr = accidental == Accidental.SHARP ? "#"
                : accidental == Accidental.FLAT ? "b"
                : accidental == Accidental.NATURAL ? "n"
                : "";
        return step.name() + accidentalStr + octave;
    }
}