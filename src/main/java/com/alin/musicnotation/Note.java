package com.alin.musicnotation;

public class Note {
    private final Pitch pitch;
    private final Duration duration;
    private final boolean isRest;

    // Constructor pentru note cu înălțime (sunete)
    public Note(Pitch pitch, Duration duration) {
        this.pitch = pitch;
        this.duration = duration;
        this.isRest = false;
    }

    // Constructor pentru pauze (resturi)
    public Note(Duration duration) {
        this.pitch = null;
        this.duration = duration;
        this.isRest = true;
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

    @Override
    public String toString() {
        if (isRest) {
            return "Rest(" + duration + ")";
        }
        return "Note(" + pitch + ", " + duration + ")";
    }
}