package ro.license.odbreader.services;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import ro.aptus.odbreader.R;
import ro.license.odbreader.data.General;
import ro.license.odbreader.data.ObdResult;

public class CmdSpinnerAdapter extends ArrayAdapter<ObdResult> {

    Context context;
    List<ObdResult> resultsList;

    public CmdSpinnerAdapter(Context context, ArrayList<ObdResult> objects) {
        super(context, 0, objects);
        this.context = context;
        this.resultsList = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        try {
            final ObdResult result = getItem(position);

            if(convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.spinner_row, parent, false);
            }

            TextView textName = (TextView) convertView.findViewById(R.id.text_spinner_name);
            String cmdName = result.getCmdName();

            String language = Locale.getDefault().getLanguage();

            if (language.equals("ro")) {
                String cmdToRo = General.cmdToRomanian(cmdName);
                textName.setText(cmdToRo);
            } else {
                textName.setText(cmdName);
            }

            return convertView;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        try {
            final ObdResult result = getItem(position);

            if(convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.spinner_row, parent, false);
            }

            TextView textName = (TextView) convertView.findViewById(R.id.text_spinner_name);
            String cmdName = result.getCmdName();

            String language = Locale.getDefault().getLanguage();

            if (language.equals("ro")) {
                String cmdToRo = General.cmdToRomanian(cmdName);
                textName.setText(cmdToRo);
            } else {
                textName.setText(cmdName);
            }

            if(resultsList.size() == 1){
                convertView.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.round_border_all));
            }else{
                if(position == 0){
                    convertView.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.round_border_top));
                }else if(position > 0 && position == resultsList.size()-1){
                    convertView.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.round_border_bottom));
                }else{
                    convertView.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.round_border_none));
                }
            }


            return convertView;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
