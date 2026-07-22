package com.alin.musicnotation;

public class Note {
    private final Pitch pitch;
    private final Duration duration;
    private final boolean isRest;
    private final boolean tieStart;
    private final boolean tieEnd;
    private Tuplet tuplet;  // câmpul nou pentru tuplet

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

    public Note(Duration duration, boolean tieStart, boolean tieEnd) {
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

    public Tuplet getTuplet() {
        return tuplet;
    }

    public void setTuplet(Tuplet tuplet) {
        this.tuplet = tuplet;
    }

    @Override
    public String toString() {
        if (isRest) {
            return "Rest(" + duration + ")";
        }
        return "Note(" + pitch + ", " + duration +
                ", tieStart=" + tieStart + ", tieEnd=" + tieEnd +
                (tuplet != null ? ", tuplet=" + tuplet.getNumerator() + ":" + tuplet.getDenominator() : "") +
                ")";
    }
}