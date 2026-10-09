package com.alin.musicnotation.musicxml.imports;

import com.alin.musicnotation.model.Note;

import java.util.List;

/**
 * Rezultatul citirii unei note:
 * - nota propriu-zisă
 * - info despre slurs (start/stop + number)
 */
public record NoteReadResult(Note note, List<SlurInfo> slurs) {

    public record SlurInfo(int number, boolean start, boolean stop) {}
}