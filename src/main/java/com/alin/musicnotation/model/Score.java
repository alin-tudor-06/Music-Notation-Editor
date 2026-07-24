package com.alin.musicnotation.model;

import com.alin.musicnotation.model.clef.ClefType;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class Score {

    private final KeySignature keySignature;
    private final ClefType initialClef;
    private final List<Measure> measures = new ArrayList<>();

    public Score(KeySignature keySignature) {
        this(keySignature, ClefType.TREBLE);
    }

    public Score(KeySignature keySignature, ClefType initialClef) {
        this.keySignature = Objects.requireNonNull(keySignature, "keySignature must not be null");
        this.initialClef = Objects.requireNonNull(initialClef, "initialClef must not be null");
    }

    public Score() {
        this(KeySignature.C_MAJOR, ClefType.TREBLE);
    }

    public void addMeasure(Measure measure) {
        Objects.requireNonNull(measure, "measure must not be null");
        measures.add(measure);
    }

    public void validate() {
        for (Measure measure : measures) {
            measure.validate();
        }
    }

    public List<Measure> measures() {
        return List.copyOf(measures);
    }

    public KeySignature keySignature() {
        return keySignature;
    }

    public ClefType initialClef() {
        return initialClef;
    }

    @Override
    public String toString() {
        return "Score{keySignature=" + keySignature +
                ", initialClef=" + initialClef +
                ", measures=" + measures +
                "}";
    }
}