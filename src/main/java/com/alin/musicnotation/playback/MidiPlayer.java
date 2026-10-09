package com.alin.musicnotation.playback;

import com.alin.musicnotation.model.Score;

import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Synthesizer;
import javax.sound.midi.Transmitter;

public class MidiPlayer {

    private Sequencer sequencer;
    private Synthesizer synthesizer;

    public void load(Score score) throws Exception {
        close();
        var sequence = MidiSequenceBuilder.build(score);

        synthesizer = MidiSystem.getSynthesizer();
        synthesizer.open();
        setVolume(80);

        sequencer = MidiSystem.getSequencer(false);
        sequencer.open();
        Transmitter t = sequencer.getTransmitter();
        t.setReceiver(synthesizer.getReceiver());
        sequencer.setSequence(sequence);
    }

    public void play() {
        if (sequencer != null && !sequencer.isRunning()) {
            sequencer.start();
        }
    }

    public void pause() {
        if (sequencer != null && sequencer.isRunning()) {
            sequencer.stop();
        }
    }

    public void stop() {
        if (sequencer != null) {
            sequencer.stop();
            sequencer.setTickPosition(0);
        }
    }

    public boolean isRunning() {
        return sequencer != null && sequencer.isRunning();
    }

    public long getTickPosition() {
        return sequencer != null ? sequencer.getTickPosition() : 0;
    }

    public void setTempoFactor(float factor) {
        if (sequencer != null) sequencer.setTempoFactor(factor);
    }

    public void setVolume(int percent) {
        if (synthesizer != null) {
            int value = (int) (percent / 100.0 * 127);
            for (var channel : synthesizer.getChannels()) {
                if (channel != null) {
                    channel.controlChange(7, value);
                }
            }
        }
    }

    public void close() {
        if (sequencer != null && sequencer.isOpen()) {
            sequencer.stop();
            sequencer.close();
            sequencer = null;
        }
        if (synthesizer != null && synthesizer.isOpen()) {
            synthesizer.close();
            synthesizer = null;
        }
    }
}