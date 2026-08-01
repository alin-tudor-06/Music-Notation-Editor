package com.alin.musicnotation.renderer;

import com.alin.musicnotation.model.layout.NoteLayout;
import com.alin.musicnotation.model.layout.StemDirection;

import java.util.ArrayList;
import java.util.List;

public final class RenderingHelpers {

    // Offset pentru a compensa diferența dintre elipse și glyph-urile Bravura
    // Valoarea exactă trebuie verificată vizual; aceasta este o estimare.
    private static final double STEM_ATTACH_OFFSET = 2.2;

    private RenderingHelpers() {
        // Utility class - no instantiation
    }

    public static StemDirection stemDirection(int position, int middleLinePosition) {
        return position > middleLinePosition ? StemDirection.UP : StemDirection.DOWN;
    }

    public static StemDirection stemDirection(List<NoteLayout> group, int middleLinePosition) {
        double avg = group.stream()
                .mapToInt(NoteLayout::position)
                .average()
                .orElse(middleLinePosition);
        return avg > middleLinePosition ? StemDirection.UP : StemDirection.DOWN;
    }

    /**
     * Calculates the x-coordinate where the stem should attach to the notehead.
     * Adjusted for Bravura SMuFL glyphs to ensure proper visual alignment.
     */
    public static double stemAttachX(NoteLayout layout, StemDirection direction, double noteHeadWidth) {
        double baseX = direction == StemDirection.UP
                ? layout.x() + noteHeadWidth / 2
                : layout.x() - noteHeadWidth / 2;

        // Ajustare pentru Bravura glyph-uri (compensează diferența față de elipse)
        // Pentru UP, stem-ul se atașează ușor spre interior
        // Pentru DOWN, stem-ul se atașează ușor spre interior
        return direction == StemDirection.UP
                ? baseX - STEM_ATTACH_OFFSET
                : baseX + STEM_ATTACH_OFFSET;
    }

    public static double computeStemTipY(NoteLayout layout, StemDirection direction,
                                         double stemLengthStandard, double middleLineY) {
        double y = layout.y();
        double standardTipY = y + direction.sign() * stemLengthStandard;
        if (direction == StemDirection.UP) {
            return Math.min(standardTipY, middleLineY);
        } else {
            return Math.max(standardTipY, middleLineY);
        }
    }

    public static List<List<NoteLayout>> groupBeamsByBeat(List<NoteLayout> layouts) {
        List<List<NoteLayout>> groups = new ArrayList<>();
        List<NoteLayout> currentGroup = null;
        int currentBeat = -1;
        for (NoteLayout layout : layouts) {
            if (!layout.note().duration().isBeamable()) {
                if (currentGroup != null && !currentGroup.isEmpty()) {
                    groups.add(currentGroup);
                    currentGroup = null;
                }
                continue;
            }
            int beat = layout.startSixteenths() / 4;
            if (currentGroup == null) {
                currentGroup = new ArrayList<>();
                currentGroup.add(layout);
                currentBeat = beat;
            } else if (beat == currentBeat) {
                currentGroup.add(layout);
            } else {
                groups.add(currentGroup);
                currentGroup = new ArrayList<>();
                currentGroup.add(layout);
                currentBeat = beat;
            }
        }
        if (currentGroup != null && !currentGroup.isEmpty()) {
            groups.add(currentGroup);
        }
        return groups;
    }
}