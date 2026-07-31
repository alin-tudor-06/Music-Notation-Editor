package com.alin.musicnotation.renderer;

import com.alin.musicnotation.model.Measure;

public final class RenderingStateMachine {

    private RenderingStateMachine() {
        // Utility class - no instantiation
    }

    public static RenderingState advance(RenderingState state, Measure measure) {
        RenderingState newState = state;
        if (measure.clef() != null) {
            newState = newState.withClef(measure.clef());
        }
        if (measure.keySignature() != null) {
            newState = newState.withKey(measure.keySignature());
        }
        if (measure.timeSignature() != null) {
            newState = newState.withTime(measure.timeSignature());
        }
        return newState;
    }
}