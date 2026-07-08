package com.alin.musicnotation;

/**
 * Represents a musical pitch such as C4, D4, F#5 or Bb3.
 * <p>
 * A pitch is defined by:
 * - step: the letter name of the note (C, D, E, F, G, A, B);
 * - alter: the number of semitones applied by an accidental
 *   (negative for flats, positive for sharps, zero for natural);
 * - octave: the scientific pitch notation octave number.
 */
public record Pitch(Step step, int alter, int octave) {

    private static final int[] STEP_SEMITONES = {0, 2, 4, 5, 7, 9, 11};

    public enum Step {
        C, D, E, F, G, A, B
    }

    public Pitch {
        if (alter < -2 || alter > 2) {
            throw new IllegalArgumentException("Alter must be between -2 and 2");
        }
    }

    /**
     * Parses a pitch from scientific pitch notation, e.g. "C4", "F#5", "Bb3".
     */
    public static Pitch of(String name) {
        Step step = Step.valueOf(name.substring(0, 1).toUpperCase());

        int index = 1;
        int alter = 0;
        if (index < name.length() && name.charAt(index) == '#') {
            alter = 1;
            index++;
        } else if (index < name.length() && name.charAt(index) == 'b') {
            alter = -1;
            index++;
        }

        int octave = Integer.parseInt(name.substring(index));
        return new Pitch(step, alter, octave);
    }

    /**
     * Converts this pitch to its MIDI note number (C4 = 60).
     */
    public int toMidiNumber() {
        return (octave + 1) * 12 + STEP_SEMITONES[step.ordinal()] + alter;
    }

    @Override
    public String toString() {
        String accidental = alter > 0 ? "#".repeat(alter) : "b".repeat(-alter);
        return step.name() + accidental + octave;
    }
}