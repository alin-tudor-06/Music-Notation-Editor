package com.alin.musicnotation;

import java.util.Objects;

/**
 * A dynamic marking attached to a single note.
 * Immutable value object.
 *
 * <p><b>Current limitation:</b> Dynamics are attached to a note.
 * In actual music notation, dynamics belong to a position in time,
 * not to a specific note. Future versions should anchor dynamics
 * to time positions (like MusicXML directions) instead.</p>
 *
 * <p>This design is acceptable for the MVP stage.</p>
 */
public final class Dynamic {
    private final Note note;
    private final DynamicType type;
    private final DynamicPlacement placement;

    public Dynamic(Note note, DynamicType type) {
        this(note, type, DynamicPlacement.AUTO);
    }

    public Dynamic(Note note, DynamicType type, DynamicPlacement placement) {
        this.note = Objects.requireNonNull(note, "note must not be null");
        this.type = Objects.requireNonNull(type, "type must not be null");
        this.placement = Objects.requireNonNull(placement, "placement must not be null");
        if (note.isRest()) {
            throw new IllegalArgumentException("Dynamic cannot be attached to a rest: " + note);
        }
    }

    public Note note() {
        return note;
    }

    public DynamicType type() {
        return type;
    }

    public DynamicPlacement placement() {
        return placement;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Dynamic{");
        sb.append(type).append(" on ").append(note);
        if (placement != DynamicPlacement.AUTO) {
            sb.append(", placement=").append(placement);
        }
        sb.append('}');
        return sb.toString();
    }
}