package com.alin.musicnotation.musicxml.imports;

import com.alin.musicnotation.model.Note;
import com.alin.musicnotation.model.articulation.ArticulationPlacement;
import com.alin.musicnotation.model.articulation.ArticulationType;

import java.util.List;

public record NoteReadResult(
        Note note,
        List<SlurInfo> slurs,
        List<ArticulationInfo> articulations,
        TupletInfo tuplet
) {
    public record SlurInfo(int number, boolean start, boolean stop) {}
    public record ArticulationInfo(ArticulationType type, ArticulationPlacement placement) {}

    /**
     * Info despre tuplet pentru o notă:
     * - numerator/denominator (ex: 3/2 pentru triplet)
     * - tupletNumber (numărul tupletului din MusicXML)
     * - isStart / isStop (nota începe sau termină tupletul)
     * Poate fi null dacă nota nu e într-un tuplet.
     */
    public record TupletInfo(int numerator, int denominator, int tupletNumber,
                             boolean isStart, boolean isStop) {}
}