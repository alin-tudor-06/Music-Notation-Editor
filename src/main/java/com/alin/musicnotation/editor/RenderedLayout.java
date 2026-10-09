package com.alin.musicnotation.editor;

import com.alin.musicnotation.model.Measure;
import com.alin.musicnotation.model.Note;
import com.alin.musicnotation.model.clef.ClefType;

import java.util.ArrayList;
import java.util.List;

public record RenderedLayout(
        List<NoteHitBox> notes,
        List<MeasureHitBox> measures
) {
    public static RenderedLayout empty() {
        return new RenderedLayout(List.of(), List.of());
    }

    public record NoteHitBox(Note note, double centerX, double centerY, double radius) {}

    public record MeasureHitBox(
            Measure measure,
            double x, double y,
            double width, double height,
            double staffFirstLineY,
            double staffLineSpacing,
            ClefType clef
    ) {}

    public static class Builder {
        private final List<NoteHitBox> notes = new ArrayList<>();
        private final List<MeasureHitBox> measures = new ArrayList<>();

        public void addNote(Note note, double x, double y, double radius) {
            notes.add(new NoteHitBox(note, x, y, radius));
        }

        public void addMeasure(Measure measure, double x, double y,
                               double w, double h,
                               double staffFirstLineY, double staffLineSpacing,
                               ClefType clef) {
            measures.add(new MeasureHitBox(measure, x, y, w, h,
                    staffFirstLineY, staffLineSpacing, clef));
        }

        public RenderedLayout build() {
            return new RenderedLayout(List.copyOf(notes), List.copyOf(measures));
        }
    }
}