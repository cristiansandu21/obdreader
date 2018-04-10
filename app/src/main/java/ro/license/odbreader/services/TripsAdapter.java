package ro.license.odbreader.services;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ro.aptus.odbreader.R;
import ro.license.odbreader.trip.Trip;

public class TripsAdapter extends ArrayAdapter<Trip> {

    Context context;
    List<Trip> resultsList;
    TripsAdapterInterface delegate = null;

    public TripsAdapter(Context context, ArrayList<Trip> objects, TripsAdapterInterface delegate) {
        super(context, 0, objects);
        this.context = context;
        this.resultsList = objects;
        this.delegate = delegate;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        final Trip result = getItem(position);

        if(convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.trip_row, parent, false);
        }

        TextView textId = (TextView) convertView.findViewById(R.id.trip_id);
        TextView textDetail = (TextView) convertView.findViewById(R.id.trip_detail);
        TextView textDuration = (TextView) convertView.findViewById(R.id.trip_duration);
        ImageButton btnDelete = (ImageButton) convertView.findViewById(R.id.btn_delete_trip);
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                delegate.onTripDelete(position);
            }
        });

        textId.setText(String.valueOf(position + 1)); // +1 cuz first pozition is 0
        textDetail.setText(result.getDate());
        textDuration.setText(result.getDuration());

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
