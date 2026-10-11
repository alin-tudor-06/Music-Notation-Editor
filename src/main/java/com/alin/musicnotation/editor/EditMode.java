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
    ADD_SIXTEENTH_REST,

    ACCIDENTAL_SHARP,
    ACCIDENTAL_FLAT,
    ACCIDENTAL_NATURAL,

    ARTICULATION_STACCATO,
    ARTICULATION_STACCATISSIMO,
    ARTICULATION_ACCENT,
    ARTICULATION_MARCATO,
    ARTICULATION_TENUTO,
    ARTICULATION_FERMATA,

    DYNAMIC_PPP,
    DYNAMIC_PP,
    DYNAMIC_P,
    DYNAMIC_MP,
    DYNAMIC_MF,
    DYNAMIC_F,
    DYNAMIC_FF,
    DYNAMIC_FFF,

    HAIRPIN_CRESCENDO,
    HAIRPIN_DECRESCENDO;

    public boolean isRestMode() {
        return this == ADD_WHOLE_REST || this == ADD_HALF_REST
                || this == ADD_QUARTER_REST || this == ADD_EIGHTH_REST
                || this == ADD_SIXTEENTH_REST;
    }

    public boolean isAddMode() {
        return this == ADD_WHOLE || this == ADD_HALF || this == ADD_QUARTER
                || this == ADD_EIGHTH || this == ADD_SIXTEENTH
                || this == ADD_WHOLE_REST || this == ADD_HALF_REST
                || this == ADD_QUARTER_REST || this == ADD_EIGHTH_REST
                || this == ADD_SIXTEENTH_REST;
    }

    public boolean isAccidentalMode() {
        return this == ACCIDENTAL_SHARP
                || this == ACCIDENTAL_FLAT
                || this == ACCIDENTAL_NATURAL;
    }

    public boolean isArticulationMode() {
        return this == ARTICULATION_STACCATO
                || this == ARTICULATION_STACCATISSIMO
                || this == ARTICULATION_ACCENT
                || this == ARTICULATION_MARCATO
                || this == ARTICULATION_TENUTO
                || this == ARTICULATION_FERMATA;
    }

    public boolean isDynamicMode() {
        return this == DYNAMIC_PPP || this == DYNAMIC_PP
                || this == DYNAMIC_P || this == DYNAMIC_MP
                || this == DYNAMIC_MF || this == DYNAMIC_F
                || this == DYNAMIC_FF || this == DYNAMIC_FFF;
    }

    public boolean isHairpinMode() {
        return this == HAIRPIN_CRESCENDO || this == HAIRPIN_DECRESCENDO;
    }
}