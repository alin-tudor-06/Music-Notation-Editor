package com.alin.musicnotation;

public enum Accidental {
    NONE(0),
    SHARP(1),
    FLAT(-1),
    NATURAL(0);

    private final int semitoneOffset;

    Accidental(int semitoneOffset) {
        this.semitoneOffset = semitoneOffset;
    }

    public int semitoneOffset() {
        return semitoneOffset;
    }

    public boolean isDisplayed() {
        return this != NONE;
    }

    public String unicodeSymbol() {
        return switch (this) {
            case SHARP -> "\u266F";
            case FLAT  -> "\u266D";
            case NATURAL -> "\u266E";
            default -> "";
        };
    }
}