package com.sonicmax.tinymet;

import android.os.Build;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.sonicmax.tinymet.utilities.SimpleMetronome;

/**
 * Main fragment containing basic metronome UI
 */
public class MainFragment extends Fragment {
    private final String LOG_TAG = MainFragment.class.getSimpleName();
    private final int DEFAULT_TEMPO = 120;
    private final int DEFAULT_PERCENT = 100;
    private final int DEFAULT_BEATS = 4;
    private final int TEMPO_LIMIT = 240;
    private final int REPEAT_DELAY = 50;

    private SimpleMetronome mMetronome;
    private Handler mRepeatHandler;
    private SeekBar mSeekBar;
    private View mRootView;
    private TextView mTempoView;
    private EditText mTempoEdit;
    private ViewSwitcher mTempoViewSwitcher;
    private TextView mPercentDisplay;
    private Button mDecreaseTempo;
    private Button mIncreaseTempo;
    private Button mDecreasePercent;
    private Button mIncreasePercent;
    private FloatingActionButton mFloatingButton;
    private TextView mBeatView;

    private int mCurrentTempo;
    private int mCurrentBeats;
    private boolean autoIncrement = false;
    private boolean autoDecrement = false;

    public MainFragment() {
    }

    ///////////////////////////////////////////////////////////////////////////
    // Lifecycle stuff
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mRootView = inflater.inflate(R.layout.fragment_main, container, false);
        getUiElements();
        setListeners();

        // Prepare UI to display data from metronome
        mCurrentBeats = DEFAULT_BEATS;
        mSeekBar.setMax(mCurrentBeats - 1);
        mPercentDisplay.setText(Integer.toString(DEFAULT_PERCENT) + "%");

        // Init metronome using default values
        mCurrentTempo = DEFAULT_TEMPO;
        mTempoView.setText(Integer.toString(mCurrentTempo));
        mMetronome = new SimpleMetronome(getContext(), DEFAULT_BEATS, mCurrentTempo) {

            @Override
            public void onTick(int currentBeat) {
                // Update seeker position to reflect current beat of metronome
                mSeekBar.setProgress(currentBeat);
                mSeekBar.animate();
            }
        };

        return mRootView;
    }

    @Override
    public void onStop() {
        mMetronome.stopAndReleaseResources();
        super.onStop();
    }

    @Override
    public void onResume() {
        mMetronome.initAudio(getContext());
        super.onResume();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Handlers for UI interactions
    ///////////////////////////////////////////////////////////////////////////

    private void getUiElements() {
        mTempoView = (TextView) mRootView.findViewById(R.id.bpm);
        mTempoEdit = (EditText) mRootView.findViewById(R.id.bpm_edit);
        mBeatView = (TextView) mRootView.findViewById(R.id.beat);
        mTempoViewSwitcher = (ViewSwitcher) mRootView.findViewById(R.id.bpm_switcher);
        mDecreaseTempo = (Button) mRootView.findViewById(R.id.minus_button);
        mIncreaseTempo = (Button) mRootView.findViewById(R.id.plus_button);
        mDecreasePercent = (Button) mRootView.findViewById(R.id.minus_percent);
        mIncreasePercent = (Button) mRootView.findViewById(R.id.plus_percent);
        mTapper = (Button) mRootView.findViewById(R.id.metronome_tap);
        mPercentDisplay = (TextView) mRootView.findViewById(R.id.percent);
        mFloatingButton = (FloatingActionButton) mRootView.findViewById(R.id.fab);
        mSeekBar = (SeekBar) mRootView.findViewById(R.id.ticker);
    }

    private void setListeners() {
        mTempoViewSwitcher.setOnClickListener(clickHandler);
        mDecreaseTempo.setOnClickListener(clickHandler);
        mIncreaseTempo.setOnClickListener(clickHandler);
        mDecreasePercent.setOnClickListener(clickHandler);
        mIncreasePercent.setOnClickListener(clickHandler);
        mTapper.setOnClickListener(clickHandler);
        mDecreaseTempo.setOnLongClickListener(longClickHandler);
        mIncreaseTempo.setOnLongClickListener(longClickHandler);
        mDecreaseTempo.setOnTouchListener(touchHandler);
        mIncreaseTempo.setOnTouchListener(touchHandler);
        mSeekBar.setOnSeekBarChangeListener(beatChanger);

        if (mFloatingButton != null) {
            mFloatingButton.setOnClickListener(clickHandler);
            mFloatingButton.setOnLongClickListener(longClickHandler);
        }

        mRepeatHandler = new Handler();
    }

    private View.OnClickListener clickHandler = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            // Route UI clicks to their appropriate handler
            switch (v.getId()) {
                case R.id.minus_button:
                    decreaseTempo();
                    break;

                case R.id.plus_button:
                    increaseTempo();
                    break;

                case R.id.fab:
                    triggerMetronome();
                    break;

                case R.id.bpm_switcher:
                    mTempoViewSwitcher.showNext(); // Switch TextView containing tempo to EditText
                    mTempoEdit.requestFocus();
                    mTempoEdit.setText("");
                    mTempoEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                        public void onFocusChange(View v, boolean hasFocus) {
                            // Parse input of mTempoEdit after losing focus
                            if (!hasFocus) {
                                try {
                                    int newTempo = Integer.parseInt(mTempoEdit.getText().toString());
                                    if (newTempo > 1 && newTempo < TEMPO_LIMIT) {
                                        mCurrentTempo = newTempo;
                                        pushTempoChange(mCurrentTempo);
                                    }

                                } catch (NumberFormatException e) {
                                    // We can just keep using existing tempo
                                    Log.e(LOG_TAG, "Couldn't parse input", e);
                                }

                                mTempoViewSwitcher.showPrevious(); // Switch back to TextView
                            }
                        }
                    });

                    break;
            }
        }
    };

    private View.OnLongClickListener longClickHandler = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            switch(v.getId()) {
                case R.id.fab:
                    // Change FAB icon so user knows that they triggered this action
                    changeFloatingButtonIcon(R.drawable.ic_undo_white_24dp);
                    resetTempo();

                    // Reset icon after 500ms
                    new Handler().postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            int id = (mMetronome.isRunning()) ? R.drawable.ic_media_pause : R.drawable.ic_media_play;
                            changeFloatingButtonIcon(id);
                        }

                    }, 500);

                    return true;

                case R.id.minus_button:
                    autoDecrement = true;
                    mRepeatHandler.post(new LongPressRepeater());
                    return false;

                case R.id.plus_button:
                    autoIncrement = true;
                    mRepeatHandler.post(new LongPressRepeater());
                    return false;

                default:
                    return false;
            }
        }
    };

    SeekBar.OnSeekBarChangeListener beatChanger = new SeekBar.OnSeekBarChangeListener() {
        private boolean wasRunning;

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            if (wasRunning) {
                // Restart metronome
                mMetronome.start();
                changeFloatingButtonIcon(R.drawable.ic_media_pause);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            // Stop metronome while user is adjusting seekbar.
            wasRunning = mMetronome.isRunning();
            mMetronome.stop();
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            mBeatView.setText(Integer.toString(progress + 1));
            // Allow user to change current beat by manipulating seekbar.
            if (fromUser) {
                mMetronome.setCurrentBeat(progress);
            }
        }
    };

    /**
     * Allows repeated increase of value while holding long press
     */
    class LongPressRepeater implements Runnable {

        @Override
        public void run() {

            if (autoIncrement) {
                if (mCurrentTempo < TEMPO_LIMIT) {
                    mCurrentTempo++;
                }
                mRepeatHandler.postDelayed(new LongPressRepeater(), REPEAT_DELAY);
            }

            else if (autoDecrement) {
                if (mCurrentTempo > 1) {
                    mCurrentTempo--;
                }
                mRepeatHandler.postDelayed(new LongPressRepeater(), REPEAT_DELAY);
            }

            pushTempoChange(mCurrentTempo);
        }
    }

    /**
     * Used in combination with LongPressRepeater so we know when to stop repeating
     */
    View.OnTouchListener touchHandler = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {

            if (event.getAction() == MotionEvent.ACTION_UP) {

                switch (v.getId()) {
                    case R.id.minus_button:
                        if (autoDecrement) {
                            autoDecrement = false;
                        }
                        break;

                    case R.id.plus_button:
                        if (autoIncrement) {
                            autoIncrement = false;
                        }
                        break;
                }
            }

            return false;
        }
    };

    ///////////////////////////////////////////////////////////////////////////
    // Methods which modify UI
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Changes icon of Floating Action Button
     * @param id ID of drawable to use (eg R.id.mydrawable)
     */
    private void changeFloatingButtonIcon(int id) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mFloatingButton.setImageDrawable(getResources().getDrawable(id, getContext().getTheme()));
        } else {
            mFloatingButton.setImageDrawable(getResources().getDrawable(id)); // oioioi
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Helper methods for metronome
    ///////////////////////////////////////////////////////////////////////////

    public void setTempo(int newTempo) {
        mCurrentTempo = newTempo;
        pushTempoChange(mCurrentTempo);
    }

    private void increaseTempo() {
        if (mCurrentTempo < TEMPO_LIMIT) { // 240
            mCurrentTempo++;
            pushTempoChange(mCurrentTempo);
        }
    }

    private void decreaseTempo() {
        if (mCurrentTempo > 1) { // (tempo of 0 would not be very practical...)
            mCurrentTempo--;
            pushTempoChange(mCurrentTempo);
        }
    }

    private void resetTempo() {
        if (mCurrentTempo != DEFAULT_TEMPO) {
            mCurrentTempo = DEFAULT_TEMPO;
            pushTempoChange(mCurrentTempo);
        }
    }

    /**
     * Updates UI and metronome with new tempo
     * @param value Tempo as int
     */
    private void pushTempoChange(int value) {
        mTempoView.setText(Integer.toString(value));
        if (mMetronome != null) {
            mMetronome.setTempo(value);
        }
    }

    private void triggerMetronome() {
        int id = (mMetronome.isRunning()) ? R.drawable.ic_media_play : R.drawable.ic_media_pause;
        changeFloatingButtonIcon(id);
        mMetronome.trigger();
    }
}
