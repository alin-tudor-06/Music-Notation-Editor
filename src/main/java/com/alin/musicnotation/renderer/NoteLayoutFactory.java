package com.alin.musicnotation.renderer;

import com.alin.musicnotation.layout.HorizontalLayout;
import com.alin.musicnotation.layout.MeasureSpacingResult;
import com.alin.musicnotation.model.Note;
import com.alin.musicnotation.model.clef.ClefType;
import com.alin.musicnotation.model.layout.NoteLayout;

import java.util.HashMap;
import java.util.Map;

public final class NoteLayoutFactory {

    private NoteLayoutFactory() {
        // Utility class - no instantiation
    }

    public static Map<Note, NoteLayout> buildNoteLayouts(
            MeasureSpacingResult spacingResult,
            double startX,
            double firstLineY,
            RenderConfig.StaffConfig staff,
            RenderConfig.NoteConfig note,
            ClefType clef) {

        Map<Note, NoteLayout> layoutMap = new HashMap<>();
        for (HorizontalLayout h : spacingResult.layouts()) {
            Note noteObj = h.note();
            int position = 0;
            double y;
            if (noteObj.isRest()) {
                y = StaffGeometry.getRestY(firstLineY, staff.lineSpacing(),
                        note.restHeight(), noteObj.duration());
            } else {
                position = StaffPositionCalculator.staffPosition(noteObj.pitch(), clef);
                y = firstLineY + position * (staff.lineSpacing() / 2.0);
            }
            NoteLayout layout = new NoteLayout(startX + h.x(), y, noteObj, position, h.startSixteenths());
            layoutMap.put(noteObj, layout);
        }
        return layoutMap;
    }
}