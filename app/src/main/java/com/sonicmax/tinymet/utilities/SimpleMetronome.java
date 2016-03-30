package com.sonicmax.tinymet.utilities;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

import com.sonicmax.tinymet.R;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Creates a simple metronome using bpm as set by constructor.
 * Override onTick() method in Fragment/etc to handle UI interactions/changes.
 */
public class SimpleMetronome {
    private final int TEMPO_LIMIT = 240;

    private SoundPool mSoundPool;
    private AudioManager mAudioManager;
    private ScheduledExecutorService mBeatScheduler;
    private ScheduledFuture mFutureBeat;

    // Values for SoundPool
    private boolean mSoundPoolLoaded = false;
    private int mTickId;
    private int mTockId;
    private float mVolume;
    // Metronome stuff
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
        mBeatScheduler = Executors.newSingleThreadScheduledExecutor();
        initAudio(context);
    }

    public void initAudio(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            mSoundPool = new SoundPool.Builder().build();
        }
        else {
            // SoundPool.Builder doesn't work with API < 21, so we have to use deprecated SoundPool constructor
            mSoundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        }

        // Make sure that SoundPool uses correct volume level
        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        float actVolume = (float) mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        float maxVolume = (float) mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        mVolume = actVolume / maxVolume;

        mSoundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {

            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                if (status == 0) {
                    mSoundPoolLoaded = true;
                }
            }

        });

        mTickId = mSoundPool.load(context, R.raw.high_seiko, 1);
        mTockId = mSoundPool.load(context, R.raw.low_seiko, 1);
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
            if (mCurrentBeat == 0 || mCurrentBeat == mBeatsPerBar) {
                mCurrentBeat = 0;
                playSound(mTickId);
            }
            else {
                playSound(mTockId);
            }

            onTick(mCurrentBeat++);
            mFutureBeat = mBeatScheduler.schedule(mTick, mTempoInMs, TimeUnit.MILLISECONDS);
        }

    };

    private void playSound(int id) {
        mSoundPool.play(id, mVolume, mVolume, 0, 0, 1);
    }

    /**
     * Called after each metronome tick has completed
     * @param currentBeat
     */
    public void onTick(int currentBeat) {}

    public void start() {
        // Schedule first beat to start immediately.
        mBeatScheduler.schedule(mTick, 0, TimeUnit.MILLISECONDS);
        mIsRunning = true;
    }

    public void stop() {
        if (mIsRunning) {
            mFutureBeat.cancel(true);
            mIsRunning = false;
        }
    }

    public void stopAndReleaseResources() {
        if (mIsRunning) {
            mFutureBeat.cancel(true);
            mIsRunning = false;
        }
        if (mSoundPoolLoaded) {
            mSoundPool.release();
        }
    }

    public void setTempo(int value) {
        if (value > 0 && value <= TEMPO_LIMIT) {
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
