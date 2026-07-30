package com.alin.musicnotation.model;

import com.alin.musicnotation.model.clef.ClefType;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class Score {

    private final KeySignature keySignature;
    private final ClefType initialClef;
    private final TimeSignature initialTimeSignature;
    private final List<Measure> measures = new ArrayList<>();

    public Score(KeySignature keySignature) {
        this(keySignature, ClefType.TREBLE, TimeSignature.COMMON_TIME);
    }

    public Score(KeySignature keySignature, ClefType initialClef) {
        this(keySignature, initialClef, TimeSignature.COMMON_TIME);
    }

    public Score(KeySignature keySignature, ClefType initialClef, TimeSignature initialTimeSignature) {
        this.keySignature = Objects.requireNonNull(keySignature, "keySignature must not be null");
        this.initialClef = Objects.requireNonNull(initialClef, "initialClef must not be null");
        this.initialTimeSignature = Objects.requireNonNull(initialTimeSignature, "initialTimeSignature must not be null");
    }

    public Score() {
        this(KeySignature.C_MAJOR, ClefType.TREBLE, TimeSignature.COMMON_TIME);
    }

    public void addMeasure(Measure measure) {
        Objects.requireNonNull(measure, "measure must not be null");
        measures.add(measure);
    }

    public void validate() {
        TimeSignature currentTime = initialTimeSignature;
        for (Measure measure : measures) {
            TimeSignature measureTime = measure.timeSignature();
            if (measureTime != null) {
                currentTime = measureTime;
            }
            measure.validate(currentTime);
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

    public TimeSignature initialTimeSignature() {
        return initialTimeSignature;
    }

    @Override
    public String toString() {
        return "Score{keySignature=" + keySignature +
                ", initialClef=" + initialClef +
                ", initialTimeSignature=" + initialTimeSignature +
                ", measures=" + measures +
                "}";
    }
}