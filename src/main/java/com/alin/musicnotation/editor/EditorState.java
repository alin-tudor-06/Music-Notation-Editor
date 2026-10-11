package com.alin.musicnotation.editor;

import com.alin.musicnotation.model.Measure;
import com.alin.musicnotation.model.Note;
import com.alin.musicnotation.model.Pitch;
import com.alin.musicnotation.model.clef.ClefType;

public class EditorState {
    private EditMode mode = EditMode.SELECT;
    private Note selectedNote;
    private Measure selectedMeasure;

    // ─── Hover ghost ───
    private boolean hovering;
    private double hoverX;
    private Pitch hoverPitch;
    private ClefType hoverClef;
    private double hoverStaffFirstLineY;
    private double hoverLineSpacing;

    public EditMode getMode() { return mode; }
    public void setMode(EditMode mode) { this.mode = mode; }

    public Note getSelectedNote() { return selectedNote; }
    public void setSelectedNote(Note note) {
        this.selectedNote = note;
        if (note != null) this.selectedMeasure = null;
    }

    public Measure getSelectedMeasure() { return selectedMeasure; }
    public void setSelectedMeasure(Measure measure) {
        this.selectedMeasure = measure;
        if (measure != null) this.selectedNote = null;
    }

    public void clearSelection() {
        selectedNote = null;
        selectedMeasure = null;
    }

    public boolean isHovering() { return hovering; }
    public double getHoverX() { return hoverX; }
    public Pitch getHoverPitch() { return hoverPitch; }
    public ClefType getHoverClef() { return hoverClef; }
    public double getHoverStaffFirstLineY() { return hoverStaffFirstLineY; }
    public double getHoverLineSpacing() { return hoverLineSpacing; }

    public void setHover(double x, Pitch pitch, ClefType clef,
                         double staffFirstLineY, double lineSpacing) {
        this.hovering = true;
        this.hoverX = x;
        this.hoverPitch = pitch;
        this.hoverClef = clef;
        this.hoverStaffFirstLineY = staffFirstLineY;
        this.hoverLineSpacing = lineSpacing;
    }

    public void clearHover() { this.hovering = false; }
}