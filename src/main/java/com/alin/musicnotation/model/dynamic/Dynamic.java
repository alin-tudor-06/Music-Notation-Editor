package com.alin.musicnotation.model.dynamic;

import com.alin.musicnotation.model.Note;
import java.util.Objects;

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

    public Note note() { return note; }
    public DynamicType type() { return type; }
    public DynamicPlacement placement() { return placement; }

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