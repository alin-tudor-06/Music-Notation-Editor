package com.alin.musicnotation.playback;

import com.alin.musicnotation.model.Note;

/**
 * Starea curentă a playback-ului.
 * Actualizat de controller în timpul redării.
 */
public class PlaybackCursor {
    private Note currentNote;
    private long currentTick;
    private int currentMeasureIndex;
    private boolean playing;

    public Note getCurrentNote() { return currentNote; }
    public void setCurrentNote(Note currentNote) { this.currentNote = currentNote; }

    public long getCurrentTick() { return currentTick; }
    public void setCurrentTick(long currentTick) { this.currentTick = currentTick; }

    public int getCurrentMeasureIndex() { return currentMeasureIndex; }
    public void setCurrentMeasureIndex(int currentMeasureIndex) { this.currentMeasureIndex = currentMeasureIndex; }

    public boolean isPlaying() { return playing; }
    public void setPlaying(boolean playing) { this.playing = playing; }

    public void reset() {
        currentNote = null;
        currentTick = 0;
        currentMeasureIndex = 0;
        playing = false;
    }
}