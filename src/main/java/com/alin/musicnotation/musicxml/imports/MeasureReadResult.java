package com.alin.musicnotation.musicxml.imports;

import com.alin.musicnotation.model.Measure;
import com.alin.musicnotation.model.clef.ClefType;
import com.alin.musicnotation.model.KeySignature;
import com.alin.musicnotation.model.TimeSignature;

/**
 * Rezultatul citirii unei măsuri.
 * Conține măsura + valorile de attributes (dacă au fost prezente).
 */
public record MeasureReadResult(
        Measure measure,
        ClefType clef,
        KeySignature key,
        TimeSignature time,
        Integer divisions
) {
}