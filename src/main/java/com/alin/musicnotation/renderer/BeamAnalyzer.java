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

        // Folosește aceeași metodă de grupare ca BeamRenderer
        List<List<NoteLayout>> groups = RenderingHelpers.groupBeamsByBeat(layouts);

        for (List<NoteLayout> group : groups) {
            if (group.size() >= 2) {
                for (NoteLayout layout : group) {
                    beamedNotes.add(layout.note());
                }
            }
        }

        return beamedNotes;
    }
}