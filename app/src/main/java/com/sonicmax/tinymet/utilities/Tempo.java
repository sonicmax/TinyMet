package com.sonicmax.tinymet.utilities;

import android.os.Parcel;
import android.os.Parcelable;

public class Tempo implements Parcelable {
    private int mMinimum;
    private int mMaximum;

    public Tempo(int min, int max) {
        mMinimum = min;
        mMaximum = max;
    }

    public int getMinimum() {
        return mMinimum;
    }

    public int getMaximum() {
        return mMaximum;
    }

    public int getAverage() {
        return (mMinimum + mMaximum) / 2;
    }


    protected Tempo(Parcel in) {
        mMinimum = in.readInt();
        mMaximum = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mMinimum);
        dest.writeInt(mMaximum);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Tempo> CREATOR = new Parcelable.Creator<Tempo>() {
        @Override
        public Tempo createFromParcel(Parcel in) {
            return new Tempo(in);
        }

        @Override
        public Tempo[] newArray(int size) {
            return new Tempo[size];
        }
    };
}