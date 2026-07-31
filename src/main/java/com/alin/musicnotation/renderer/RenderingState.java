package com.alin.musicnotation.renderer;

import com.alin.musicnotation.model.KeySignature;
import com.alin.musicnotation.model.TimeSignature;
import com.alin.musicnotation.model.clef.ClefType;

public record RenderingState(
        ClefType currentClef,
        KeySignature currentKey,
        TimeSignature currentTime
) {
    public RenderingState withClef(ClefType clef) {
        return new RenderingState(clef, currentKey, currentTime);
    }

    public RenderingState withKey(KeySignature key) {
        return new RenderingState(currentClef, key, currentTime);
    }

    public RenderingState withTime(TimeSignature time) {
        return new RenderingState(currentClef, currentKey, time);
    }
}