package com.sonicmax.tinymet.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.sonicmax.tinymet.R;

/**
 * Multiple-choice dialog which allows user to filter tempo markings by language
 */
public abstract class LanguageDialog extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.pick_language)
                .setItems(R.array.languages, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        String[] languages = getResources().getStringArray(R.array.languages);
                        onChooseLanguage(languages[which]);
                    }

                });

        return builder.create();
    }

    public abstract void onChooseLanguage(String language);
}
