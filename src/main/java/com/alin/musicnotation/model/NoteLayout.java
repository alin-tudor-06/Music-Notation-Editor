package com.alin.musicnotation.model;

import com.alin.musicnotation.Note;

public record NoteLayout(double x, double y, Note note, int position, int startSixteenths) {
}