package ro.license.odbreader.services;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import ro.aptus.odbreader.R;
import ro.license.odbreader.data.General;
import ro.license.odbreader.data.ObdResult;

public class ResultAdapter extends ArrayAdapter<ObdResult> {

    Context context;
    List<ObdResult> resultsList;

    public ResultAdapter(Context context, ArrayList<ObdResult> objects) {
        super(context, 0, objects);
        this.context = context;
        this.resultsList = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final ObdResult result = getItem(position);

        if(convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.obd_result_row, parent, false);
        }

        TextView textName = (TextView) convertView.findViewById(R.id.text_cmd_name);
        TextView textResult = (TextView) convertView.findViewById(R.id.text_cmd_result);

        String language = Locale.getDefault().getLanguage();
        if(language.equals("ro")){
            String cmdToRo = General.cmdToRomanian(result.getCmdName());
            textName.setText(cmdToRo);
        } else {
            textName.setText(result.getCmdName());
        }

        if(result.getCmdResult().equals("?"))
            textResult.setText("NODATA");
        else
            textResult.setText(result.getCmdResult());

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
    }
}
