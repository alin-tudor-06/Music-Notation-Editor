package com.alin.musicnotation.musicxml.export;

import com.alin.musicnotation.model.KeySignature;
import com.alin.musicnotation.model.TimeSignature;
import com.alin.musicnotation.model.clef.ClefType;

public class MusicXmlContext {

    private ClefType currentClef;
    private KeySignature currentKey;
    private TimeSignature currentTime;

    public MusicXmlContext(ClefType initialClef, KeySignature initialKey, TimeSignature initialTime) {
        this.currentClef = initialClef;
        this.currentKey = initialKey;
        this.currentTime = initialTime;
    }

    public ClefType getCurrentClef() {
        return currentClef;
    }

    public void setCurrentClef(ClefType currentClef) {
        this.currentClef = currentClef;
    }

    public KeySignature getCurrentKey() {
        return currentKey;
    }

    public void setCurrentKey(KeySignature currentKey) {
        this.currentKey = currentKey;
    }

    public TimeSignature getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(TimeSignature currentTime) {
        this.currentTime = currentTime;
    }
}