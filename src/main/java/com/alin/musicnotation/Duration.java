package com.alin.musicnotation;

/**
 * Represents the duration of a note, expressed as a fraction of a whole note.
 * <p>
 * Each value stores the denominator of that fraction (e.g. a quarter note
 * is 1/4 of a whole note, so its denominator is 4).
 */
public enum Duration {

    WHOLE(1, 0),
    HALF(2, 0),
    QUARTER(4, 0),
    EIGHTH(8, 1),
    SIXTEENTH(16, 2);

    private final int denominator;
    private final int flagCount;

    Duration(int denominator, int flagCount) {
        this.denominator = denominator;
        this.flagCount = flagCount;
    }

    public int denominator() {
        return denominator;
    }

    /**
     * How many quarter notes this duration is equivalent to.
     * A whole note equals 4 quarter notes, a quarter note equals 1, etc.
     */
    public double toQuarterNoteFraction() {
        return 4.0 / denominator;
    }

    /**
     * Converts this duration into MIDI ticks, given the number of ticks
     * a single quarter note represents (the standard MIDI resolution unit).
     */
    public int toMidiTicks(int ticksPerQuarterNote) {
        return (int) Math.round(ticksPerQuarterNote * toQuarterNoteFraction());
    }

    /**
     * Whether notes of this duration are typically connected with a beam
     * instead of an individual flag (eighth notes and shorter).
     */
    public boolean isBeamable() {
        return denominator >= EIGHTH.denominator;
    }

    /**
     * Whether notes of this duration are notated with a stem.
     * A whole note has no stem; every shorter duration does.
     */
    public boolean requiresStem() {
        return this != WHOLE;
    }

    /**
     * Whether notes of this duration are notated with a filled (solid)
     * note head. Whole and half notes use an empty (hollow) note head.
     */
    public boolean isFilled() {
        return denominator >= QUARTER.denominator;
    }

    /**
     * How many flags this duration is notated with (0 for quarter notes
     * and longer, 1 for eighth notes, 2 for sixteenth notes).
     */
    public int flagCount() {
        return flagCount;
    }

    /**
     * Numărul de șaisprezecimi echivalent cu această durată.
     * De exemplu, o durată QUARTER = 4, EIGHTH = 2, SIXTEENTH = 1.
     */
    public int sixteenths() {
        return 16 / denominator;
    }
}