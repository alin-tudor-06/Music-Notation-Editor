package com.alin.musicnotation.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class Score {

    private final KeySignature keySignature;
    private final List<Measure> measures = new ArrayList<>();

    public Score(KeySignature keySignature) {
        this.keySignature = Objects.requireNonNull(keySignature, "keySignature must not be null");
    }

    public Score() {
        this(KeySignature.C_MAJOR);
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

    @Override
    public String toString() {
        return "Score{keySignature=" + keySignature + ", measures=" + measures + "}";
    }
}