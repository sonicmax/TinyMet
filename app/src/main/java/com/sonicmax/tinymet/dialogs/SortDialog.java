package com.sonicmax.tinymet.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.sonicmax.tinymet.R;

/**
 * Multiple-choice dialog which allows user to sort tempo dictionary results
 */
public abstract class SortDialog extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.sort_dict)
                .setItems(R.array.sort_options, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        String[] options = getResources().getStringArray(R.array.sort_options);
                        onChooseSort(options[which]);
                    }

                });

        return builder.create();
    }

    public abstract void onChooseSort(String option);
}
