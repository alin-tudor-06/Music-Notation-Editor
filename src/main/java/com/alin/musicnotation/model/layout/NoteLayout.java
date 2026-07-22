package com.alin.musicnotation.model.layout;

import com.alin.musicnotation.model.Note;

public record NoteLayout(double x, double y, Note note, int position, int startSixteenths) {
}