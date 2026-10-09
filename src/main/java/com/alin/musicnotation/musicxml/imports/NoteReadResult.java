package com.alin.musicnotation.musicxml.imports;

import com.alin.musicnotation.model.Note;
import com.alin.musicnotation.model.articulation.ArticulationPlacement;
import com.alin.musicnotation.model.articulation.ArticulationType;

import java.util.List;

/**
 * Rezultatul citirii unei note:
 * - nota propriu-zisă
 * - info despre slurs (start/stop + number)
 * - info despre articulații
 */
public record NoteReadResult(
        Note note,
        List<SlurInfo> slurs,
        List<ArticulationInfo> articulations
) {

    public record SlurInfo(int number, boolean start, boolean stop) {}

    public record ArticulationInfo(ArticulationType type, ArticulationPlacement placement) {}
}