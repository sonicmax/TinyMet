package com.sonicmax.tinymet.database;

import java.io.IOException;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class TempoProvider {
    protected static final String LOG_TAG = TempoProvider.class.getSimpleName();

    private final Context mContext;
    private SQLiteDatabase mDb;
    private DatabaseHelper mDbHelper;

    public TempoProvider(Context context) {
        this.mContext = context;
        mDbHelper = new DatabaseHelper(mContext);
    }

    public TempoProvider createDatabase() throws SQLException {
        try {
            mDbHelper.createDataBase();
        }
        catch (IOException e) {
            Log.e(LOG_TAG, "Unable to create database: ", e);
        }
        return this;
    }

    public TempoProvider open() throws SQLException {
        try {
            mDbHelper.openDataBase();
            mDbHelper.close();
            mDb = mDbHelper.getReadableDatabase();
        }
        catch (SQLException e) {
            Log.e(LOG_TAG, "Error opening database:", e);
        }
        return this;
    }

    public void close() {
        mDbHelper.close();
    }

    public Cursor getTempoData(String language) {
        try {
            String sql = "SELECT * FROM TEMPO_DICTIONARY";

            if (!language.equals("ALL")) {
                sql += " WHERE LANGUAGE = '" + language + "'";
            } else {
                sql += " WHERE LANGUAGE = *";
            }

            sql += " ORDER BY NAME";

            Cursor mCur = mDb.rawQuery(sql, null);
            if (mCur != null) {
                mCur.moveToNext();
            }
            return mCur;
        }
        catch (SQLException e) {
            Log.e(LOG_TAG, "Error querying database: ", e);
            throw e;
        }
    }

    public Cursor sortTempoData(String language, String sortType) {
        try {
            String sql = "SELECT * FROM TEMPO_DICTIONARY";

            if (!language.equals("ALL")) {
                sql += " WHERE LANGUAGE = '" + language + "'";
            }

            switch (sortType) {
                case "By name (ascending)":
                    sql += " ORDER BY NAME";
                    break;
                case "By name (descending)":
                    sql += " ORDER BY NAME DESC";
                    break;
                case "By tempo (ascending)":
                    sql += " ORDER BY MIN";
                    break;
                case "By tempo (descending)":
                    sql += " ORDER BY MIN DESC";
                    break;
            }

            Cursor mCur = mDb.rawQuery(sql, null);
            if (mCur != null) {
                mCur.moveToNext();
            }
            return mCur;
        }
        catch (SQLException e) {
            Log.e(LOG_TAG, "Error querying database: ", e);
            throw e;
        }
    }
}
