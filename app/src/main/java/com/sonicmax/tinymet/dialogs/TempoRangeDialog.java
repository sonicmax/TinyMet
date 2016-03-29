package com.sonicmax.tinymet.dialogs;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.sonicmax.tinymet.R;
import com.sonicmax.tinymet.utilities.Tempo;

/**
 * Dialog with seekbar which allows user to set tempo within a certain range
 */
public abstract class TempoRangeDialog extends DialogFragment {
    private int mMinTempo;
    private int mMaxTempo;
    private SeekBar mTempoBar;

    public TempoRangeDialog(Tempo tempo) {
        mMinTempo = tempo.getMinimum();
        mMaxTempo = tempo.getMaximum();
    }

    @SuppressLint("InflateParams")
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View layout = getActivity().getLayoutInflater().inflate(R.layout.tempo_range_dialog, null);

        TextView minTempo = (TextView) layout.findViewById(R.id.min_tempo);
        TextView maxTempo = (TextView) layout.findViewById(R.id.max_tempo);
        final TextView currentTempo = (TextView) layout.findViewById(R.id.current_tempo);

        minTempo.setText(Integer.toString(mMinTempo));
        maxTempo.setText(Integer.toString(mMaxTempo));

        mTempoBar = (SeekBar) layout.findViewById(R.id.tempo_range_seekbar);

        int tempoRange = mMaxTempo - mMinTempo;
        int halfway = tempoRange / 2;
        mTempoBar.setMax(tempoRange);
        mTempoBar.setProgress(halfway);
        currentTempo.setText(Integer.toString(mMinTempo + halfway));

        mTempoBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                currentTempo.setText(Integer.toString(mMinTempo + progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Do nothing
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Do nothing
            }
        });

        Button okButton = (Button) layout.findViewById(R.id.tempo_change_ok);
        Button cancelButton = (Button) layout.findViewById(R.id.tempo_change_cancel);

        okButton.setOnClickListener(buttonHandler);
        cancelButton.setOnClickListener(buttonHandler);

        builder.setView(layout);
        return builder.create();
    }

    View.OnClickListener buttonHandler = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {

                case R.id.tempo_change_ok:
                    onChooseTempo(mMinTempo + mTempoBar.getProgress());
                    break;

                case R.id.tempo_change_cancel:
                    onCancel();
                    break;
            }
        }
    };

    public abstract void onChooseTempo(int tempo);
    public abstract void onCancel();
}
