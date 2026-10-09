package com.alin.musicnotation.editor;

import com.alin.musicnotation.model.Note;

public class EditorState {
    private EditMode mode = EditMode.SELECT;
    private Note selectedNote;

    public EditMode getMode() { return mode; }
    public void setMode(EditMode mode) { this.mode = mode; }

    public Note getSelectedNote() { return selectedNote; }
    public void setSelectedNote(Note note) { this.selectedNote = note; }

    public void clearSelection() { selectedNote = null; }
}