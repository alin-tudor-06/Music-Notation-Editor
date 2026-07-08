package com.alin.musicnotation;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a complete musical score as an ordered sequence of measures.
 */
public final class Score {

    private final List<Measure> measures = new ArrayList<>();

    public void addMeasure(Measure measure) {
        Objects.requireNonNull(measure, "measure must not be null");
        measures.add(measure);
    }

    public List<Measure> measures() {
        return List.copyOf(measures);
    }
}