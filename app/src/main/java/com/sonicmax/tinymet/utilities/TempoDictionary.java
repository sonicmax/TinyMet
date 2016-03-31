package com.sonicmax.tinymet.utilities;

import android.content.Context;
import android.database.Cursor;

import com.sonicmax.tinymet.database.TempoProvider;

/**
 * Class which allows us to convert tempo markings to their equivalent in BPM.
 */
public abstract class TempoDictionary {
    private Context mContext;

    public TempoDictionary(Context context) {
        mContext = context;
    }

    public void loadDatabase(String language) {
        TempoProvider mDbHelper = new TempoProvider(mContext);
        mDbHelper.createDatabase();
        mDbHelper.open();
        onLoad(mDbHelper.getTempoData(language));
        mDbHelper.close();
    }

    public void sortDatabase(String language, String sortType) {
        TempoProvider mDbHelper = new TempoProvider(mContext);
        mDbHelper.createDatabase();
        mDbHelper.open();
        onLoad(mDbHelper.sortTempoData(language, sortType));
        mDbHelper.close();
    }

    public abstract void onLoad(Cursor data);
}
