package com.sonicmax.tinymet.utilities;

import android.content.Context;
import android.media.MediaPlayer;

import com.sonicmax.tinymet.R;

import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Creates a simple metronome using bpm as set by constructor.
 * Override onTick() method in Fragment/etc to handle UI interactions/changes.
 */
public class SimpleMetronome {
    private final int TEMPO_LIMIT = 241;

    private final MediaPlayer mTickPlayer;
    private final MediaPlayer mTockPlayer;
    private ScheduledExecutorService mBeatService;
    private int mBeatsPerBar;
    private int mTempo;
    private int mTempoInMs;
    private int mCurrentBeat;

    private boolean mIsRunning = false;

    public SimpleMetronome(Context context, int beatsPerBar, int tempo) {
        mBeatsPerBar = beatsPerBar;
        mTempo = tempo;
        mTempoInMs = getTickInMs();
        mCurrentBeat = 0;
        mTickPlayer = MediaPlayer.create(context, R.raw.high_seiko);
        mTockPlayer = MediaPlayer.create(context, R.raw.low_seiko);
    }

    public void trigger() {
        if (!mIsRunning) {
            start();
        } else {
            stop();
        }
    }

    private Tick mTick = new Tick() {

        @Override
        public void run() {
            // Main logic for metronome.
            if (mCurrentBeat == 0 || mCurrentBeat == mBeatsPerBar) {
                mCurrentBeat = 0;
                mTickPlayer.start();
            }
            else {
                mTockPlayer.start();
            }

            onTick(mCurrentBeat++);
            mBeatService.schedule(mTick, mTempoInMs, TimeUnit.MILLISECONDS);
        }

    };

    /**
     * Called after each metronome tick has completed
     * @param currentBeat
     */
    public void onTick(int currentBeat) {}

    public void start() {
        mBeatService = Executors.newSingleThreadScheduledExecutor();
        mBeatService.schedule(mTick, mTempoInMs, TimeUnit.MILLISECONDS);
        mIsRunning = true;
    }

    public void stop() {
        if (mIsRunning) {
            mBeatService.shutdown();
            mIsRunning = false;
        }
    }

    public void setTempo(int value) {
        if (value > 0 && value < TEMPO_LIMIT) {
            mTempo = value;
            mTempoInMs = getTickInMs();
        }
    }

    public boolean isRunning() {
        return mIsRunning;
    }

    public void setCurrentBeat(int beat) {
        mCurrentBeat = beat;
    }

    public int getTickInMs() {
        return 1000 * 60 / mTempo;
    }

}
