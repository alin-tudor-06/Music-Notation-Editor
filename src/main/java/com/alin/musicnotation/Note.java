package com.alin.musicnotation;

import java.util.Objects;

public final class Note {
    private final Pitch pitch;
    private final Duration duration;
    private final boolean isRest;
    private final boolean tieStart;
    private final boolean tieEnd;
    private Tuplet tuplet;

    public Note(Pitch pitch, Duration duration) {
        this(pitch, duration, false, false);
    }

    public Note(Pitch pitch, Duration duration, boolean tieStart, boolean tieEnd) {
        this.pitch = pitch;
        this.duration = duration;
        this.isRest = false;
        this.tieStart = tieStart;
        this.tieEnd = tieEnd;
    }

    public Note(Duration duration) {
        this.pitch = null;
        this.duration = duration;
        this.isRest = true;
        this.tieStart = false;
        this.tieEnd = false;
    }

    public Pitch pitch() {
        return pitch;
    }

    public Duration duration() {
        return duration;
    }

    public boolean isRest() {
        return isRest;
    }

    public boolean tieStart() {
        return tieStart;
    }

    public boolean tieEnd() {
        return tieEnd;
    }

    public Tuplet tuplet() {
        return tuplet;
    }

    void setTuplet(Tuplet tuplet) {
        Objects.requireNonNull(tuplet, "tuplet must not be null");
        if (this.tuplet != null && this.tuplet != tuplet) {
            throw new IllegalStateException("Note already belongs to another tuplet: " + this.tuplet);
        }
        this.tuplet = tuplet;
    }

    @Override
    public String toString() {
        if (isRest) {
            return "Rest(" + duration + ")";
        }
        StringBuilder sb = new StringBuilder("Note(" + pitch + ", " + duration);
        if (tieStart) sb.append(", tieStart");
        if (tieEnd) sb.append(", tieEnd");
        if (tuplet != null) sb.append(", tuplet=").append(tuplet.numerator()).append(":").append(tuplet.denominator());
        sb.append(")");
        return sb.toString();
    }
}