package com.alin.musicnotation;

import java.util.Objects;

/**
 * An articulation attached to a single note.
 * Immutable value object.
 */
public final class Articulation {
    private final Note note;
    private final ArticulationType type;
    private final ArticulationPlacement placement;

    public Articulation(Note note, ArticulationType type) {
        this(note, type, ArticulationPlacement.AUTO);
    }

    public Articulation(Note note, ArticulationType type, ArticulationPlacement placement) {
        this.note = Objects.requireNonNull(note, "note must not be null");
        this.type = Objects.requireNonNull(type, "type must not be null");
        this.placement = Objects.requireNonNull(placement, "placement must not be null");
        if (note.isRest()) {
            throw new IllegalArgumentException("Articulation cannot be attached to a rest: " + note);
        }
    }

    public Note note() {
        return note;
    }

    public ArticulationType type() {
        return type;
    }

    public ArticulationPlacement placement() {
        return placement;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Articulation{");
        sb.append(type).append(" on ").append(note);
        if (placement != ArticulationPlacement.AUTO) {
            sb.append(", placement=").append(placement);
        }
        sb.append('}');
        return sb.toString();
    }
}