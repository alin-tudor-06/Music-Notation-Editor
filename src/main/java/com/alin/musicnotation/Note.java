package com.alin.musicnotation;

public class Note {
    private final Pitch pitch;
    private final Duration duration;
    private final boolean isRest;
    private final boolean tieStart;
    private final boolean tieEnd;

    // Constructor pentru note cu înălțime (fără tie)
    public Note(Pitch pitch, Duration duration) {
        this(pitch, duration, false, false);
    }

    // Constructor pentru note cu înălțime și tie
    public Note(Pitch pitch, Duration duration, boolean tieStart, boolean tieEnd) {
        this.pitch = pitch;
        this.duration = duration;
        this.isRest = false;
        this.tieStart = tieStart;
        this.tieEnd = tieEnd;
    }

    // Constructor pentru pauze (fără tie)
    public Note(Duration duration) {
        this.pitch = null;
        this.duration = duration;
        this.isRest = true;
        this.tieStart = false;
        this.tieEnd = false;
    }

    // Constructor pentru pauze (nu are sens tie, dar păstrăm consistența)
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

    @Override
    public String toString() {
        if (isRest) {
            return "Rest(" + duration + ")";
        }
        return "Note(" + pitch + ", " + duration +
                ", tieStart=" + tieStart + ", tieEnd=" + tieEnd + ")";
    }
}