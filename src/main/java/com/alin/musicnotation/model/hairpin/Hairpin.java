package com.alin.musicnotation.model.hairpin;

import com.alin.musicnotation.model.Note;
import java.util.Objects;

/**
 * Represents a hairpin (crescendo or decrescendo) spanning between two notes.
 * Hairpins are dynamic markings that indicate a gradual change in volume.
 */
public final class Hairpin {
    private final Note startNote;
    private final Note endNote;
    private final HairpinType type;
    private final HairpinPlacement placement;

    public Hairpin(Note startNote, Note endNote, HairpinType type) {
        this(startNote, endNote, type, HairpinPlacement.BELOW);
    }

    public Hairpin(Note startNote, Note endNote, HairpinType type, HairpinPlacement placement) {
        this.startNote = Objects.requireNonNull(startNote, "startNote must not be null");
        this.endNote = Objects.requireNonNull(endNote, "endNote must not be null");
        this.type = Objects.requireNonNull(type, "type must not be null");
        this.placement = Objects.requireNonNull(placement, "placement must not be null");
    }

    public Note startNote() {
        return startNote;
    }

    public Note endNote() {
        return endNote;
    }

    public HairpinType type() {
        return type;
    }

    public HairpinPlacement placement() {
        return placement;
    }

    @Override
    public String toString() {
        return "Hairpin{" +
                "startNote=" + startNote +
                ", endNote=" + endNote +
                ", type=" + type +
                ", placement=" + placement +
                '}';
    }
}