package com.alin.musicnotation.model;

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

    public double toQuarterNoteFraction() {
        return 4.0 / denominator;
    }

    public int toMidiTicks(int ticksPerQuarterNote) {
        return (int) Math.round(ticksPerQuarterNote * toQuarterNoteFraction());
    }

    public boolean isBeamable() {
        return denominator >= EIGHTH.denominator;
    }

    public boolean requiresStem() {
        return this != WHOLE;
    }

    public boolean isFilled() {
        return denominator >= QUARTER.denominator;
    }

    public int flagCount() {
        return flagCount;
    }

    public int sixteenths() {
        return 16 / denominator;
    }
}