package com.alin.musicnotation.editor;

public enum EditMode {
    SELECT,
    ADD_WHOLE,
    ADD_HALF,
    ADD_QUARTER,
    ADD_EIGHTH,
    ADD_SIXTEENTH,
    ADD_WHOLE_REST,
    ADD_HALF_REST,
    ADD_QUARTER_REST,
    ADD_EIGHTH_REST,
    ADD_SIXTEENTH_REST;

    public boolean isRestMode() {
        return this == ADD_WHOLE_REST || this == ADD_HALF_REST
                || this == ADD_QUARTER_REST || this == ADD_EIGHTH_REST
                || this == ADD_SIXTEENTH_REST;
    }

    public boolean isAddMode() {
        return this != SELECT;
    }
}