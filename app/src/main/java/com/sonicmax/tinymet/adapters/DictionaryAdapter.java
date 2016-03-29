package com.sonicmax.tinymet.adapters;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.sonicmax.tinymet.R;
import com.sonicmax.tinymet.utilities.Tempo;

import java.util.ArrayList;
import java.util.List;

public class DictionaryAdapter extends BaseAdapter {

    private List<String> mDictionaryNames = new ArrayList<>();
    private List<Tempo> mDictionaryValues = new ArrayList<>();

    private final Context context;

    public DictionaryAdapter(Context context) {
        this.context = context;
    }

    public void updateDictionary(List<String> names, List<Tempo> values) {
        mDictionaryNames = names;
        mDictionaryValues = values;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        // We can assume that mDictionaryNames and mDictionaryValues will have same length
        return mDictionaryNames.size();
    }

    public void clear() {
        mDictionaryNames.clear();
        mDictionaryValues.clear();
        notifyDataSetChanged();
    }

    @Override
    public Bundle getItem(int position) {
        Bundle bundle = new Bundle(2);
        bundle.putString("name", mDictionaryNames.get(position));
        bundle.putParcelable("value", mDictionaryValues.get(position));
        return bundle;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private static class ViewHolder {
        public final TextView nameView;
        public final TextView tempoView;

        public ViewHolder(TextView nameView, TextView tempoView) {
            this.nameView = nameView;
            this.tempoView = tempoView;
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        TextView nameView;
        TextView tempoView;

        if (convertView == null) {

            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.dictionary_entry, parent, false);
            nameView = (TextView) convertView.findViewById(R.id.tempo_name);
            tempoView = (TextView) convertView.findViewById(R.id.tempo_value);
            convertView.setTag(new ViewHolder(nameView, tempoView));

        } else {

            ViewHolder viewHolder = (ViewHolder) convertView.getTag();
            nameView = viewHolder.nameView;
            tempoView = viewHolder.tempoView;

        }

        Bundle dictionaryData = getItem(position);
        Tempo tempo = dictionaryData.getParcelable("value");
        nameView.setText(dictionaryData.getString("name"));
        tempoView.setText(formatTempo(tempo));

        return convertView;
    }

    private String formatTempo(Tempo tempo) {
        return "♩ = " + Integer.toString(tempo.getMinimum()) + "-" + Integer.toString(tempo.getMaximum());
    }

}

