package com.alin.musicnotation.renderer;

import com.alin.musicnotation.model.layout.NoteLayout;
import com.alin.musicnotation.model.layout.StemDirection;

import java.util.ArrayList;
import java.util.List;

public final class RenderingHelpers {

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

    public static double stemAttachX(NoteLayout layout, StemDirection direction, double noteHeadWidth) {
        return direction == StemDirection.UP
                ? layout.x() + noteHeadWidth / 2
                : layout.x() - noteHeadWidth / 2;
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