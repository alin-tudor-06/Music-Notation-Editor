package com.alin.musicnotation.model;

import java.util.Objects;

public final class TimeSignature {

    public enum DisplayStyle {
        NUMBERS,
        COMMON,  // C
        CUT      // C|
    }

    private final int numerator;
    private final int denominator;
    private final DisplayStyle displayStyle;

    public static final TimeSignature COMMON_TIME = new TimeSignature(4, 4, DisplayStyle.COMMON);
    public static final TimeSignature CUT_TIME = new TimeSignature(2, 2, DisplayStyle.CUT);
    public static final TimeSignature THREE_FOUR = new TimeSignature(3, 4);
    public static final TimeSignature TWO_FOUR = new TimeSignature(2, 4);
    public static final TimeSignature SIX_EIGHT = new TimeSignature(6, 8);
    public static final TimeSignature NINE_EIGHT = new TimeSignature(9, 8);
    public static final TimeSignature TWELVE_EIGHT = new TimeSignature(12, 8);

    public TimeSignature(int numerator, int denominator) {
        this(numerator, denominator, DisplayStyle.NUMBERS);
    }

    public TimeSignature(int numerator, int denominator, DisplayStyle displayStyle) {
        if (numerator <= 0) {
            throw new IllegalArgumentException("Numerator must be positive: " + numerator);
        }
        if (denominator <= 0) {
            throw new IllegalArgumentException("Denominator must be positive: " + denominator);
        }
        int d = denominator;
        while (d % 2 == 0) {
            d /= 2;
        }
        if (d != 1) {
            throw new IllegalArgumentException("Denominator must be a power of two: " + denominator);
        }
        this.numerator = numerator;
        this.denominator = denominator;
        this.displayStyle = Objects.requireNonNull(displayStyle, "displayStyle must not be null");
    }

    public int numerator() {
        return numerator;
    }

    public int denominator() {
        return denominator;
    }

    public DisplayStyle displayStyle() {
        return displayStyle;
    }

    public int measureCapacityInSixteenths() {
        return numerator * (16 / denominator);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TimeSignature that)) return false;
        return numerator == that.numerator && denominator == that.denominator && displayStyle == that.displayStyle;
    }

    @Override
    public int hashCode() {
        return Objects.hash(numerator, denominator, displayStyle);
    }

    @Override
    public String toString() {
        return numerator + "/" + denominator + (displayStyle != DisplayStyle.NUMBERS ? " (" + displayStyle + ")" : "");
    }
}