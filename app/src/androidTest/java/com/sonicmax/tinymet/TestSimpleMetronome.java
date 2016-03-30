package com.sonicmax.tinymet;

import android.test.AndroidTestCase;

import com.sonicmax.tinymet.utilities.SimpleMetronome;

public class TestSimpleMetronome extends AndroidTestCase {
    private final String LOG_TAG = TestSimpleMetronome.class.getSimpleName();

    // Test 4/4 beat at tempo of 151
    private final int TEST_PAUSE_BEAT = 3;
    private final int TEST_END_BEAT = 4;
    private final int TEST_TEMPO = 151;

    private final int TEST_MS = 397; // This is the expected return value for getTickInMs() method

    private int mTotal = 0;
    private boolean mPaused = false;

    private final SimpleMetronome metronome = new SimpleMetronome(getContext(), TEST_END_BEAT, TEST_TEMPO) {

        @Override
        public void onTick(int currentBeat) {
            mTotal++;
            if (currentBeat == TEST_PAUSE_BEAT) {
                metronome.stop();
                mPaused = true;

                // Perform a couple of simple tests to make sure it worked correctly

                if (mTotal != TEST_PAUSE_BEAT) {
                    throw new AssertionError();
                }

                if (metronome.getTickInMs() != TEST_MS) {
                    throw new AssertionError();
                }

                // Now check that we can resume metronome without any issues
                metronome.start();
            }

            else if (currentBeat == TEST_END_BEAT) {
                metronome.stop();

                if (mPaused && mTotal != TEST_END_BEAT) {
                    throw new AssertionError();
                }
            }
        }
    };
}
