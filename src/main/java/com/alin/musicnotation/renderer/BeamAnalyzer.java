package com.alin.musicnotation.renderer;

import com.alin.musicnotation.model.Note;
import com.alin.musicnotation.model.layout.NoteLayout;

import java.util.*;

public final class BeamAnalyzer {

    private BeamAnalyzer() {
        // Utility class - no instantiation
    }

    public static Set<Note> computeBeamedNotes(List<NoteLayout> layouts) {
        Set<Note> beamedNotes = new HashSet<>();
        List<NoteLayout> beamableLayouts = new ArrayList<>();
        for (NoteLayout layout : layouts) {
            if (!layout.note().isRest() && layout.note().duration().isBeamable()) {
                beamableLayouts.add(layout);
            }
        }
        beamableLayouts.sort(Comparator.comparingDouble(NoteLayout::x));
        List<List<NoteLayout>> beamGroups = RenderingHelpers.groupBeamsByBeat(beamableLayouts);
        for (List<NoteLayout> group : beamGroups) {
            if (group.size() >= 2) {
                for (NoteLayout layout : group) {
                    beamedNotes.add(layout.note());
                }
            }
        }
        return beamedNotes;
    }
}