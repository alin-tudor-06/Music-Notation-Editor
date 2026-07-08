package com.alin.musicnotation;

import java.util.Objects;

/**
 * Represents a single musical note: a pitch played for a specific duration.
 */
public record Note(Pitch pitch, Duration duration) {

    public Note {
        Objects.requireNonNull(pitch, "pitch must not be null");
        Objects.requireNonNull(duration, "duration must not be null");
    }

    @Override
    public String toString() {
        return pitch + " " + duration;
    }
}