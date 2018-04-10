package ro.license.odbreader.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import ro.aptus.odbreader.R;
import ro.license.odbreader.services.TripsAdapter;
import ro.license.odbreader.services.TripsAdapterInterface;
import ro.license.odbreader.services.TripsWriter;

public class TripsHistoryActivity extends BaseActivity implements AdapterView.OnItemClickListener, TripsAdapterInterface {

    private final String TAG = TripsHistoryActivity.class.getName();
    private ListView listView;
    private TextView textNoItems;
    private TripsAdapter tripsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trips_history);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(getResources().getString(R.string.activity_trips_history));
        }

        Initialize();
    }

    private void Initialize(){
        try {
            textNoItems = (TextView) findViewById(R.id.text_no_items);

            if(MainActivity.tripsArray == null || MainActivity.tripsArray.isEmpty())
                textNoItems.setVisibility(View.VISIBLE);
            else
                textNoItems.setVisibility(View.GONE);

            listView = (ListView) findViewById(R.id.list_view_trips);
            listView.setOnItemClickListener(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(MainActivity.tripsArray != null && !MainActivity.tripsArray.isEmpty()){
            if(tripsAdapter == null){
                tripsAdapter = new TripsAdapter(this, MainActivity.tripsArray, this);
                listView.setAdapter(tripsAdapter);
            } else {
                tripsAdapter.notifyDataSetChanged();
            }
        }
    }

    private void storeTripsToStorage(){
        try {
            if(MainActivity.tripsArray == null){
                return;
            } else {
                TripsWriter tripsWriter = new TripsWriter(this);
                tripsWriter.storeTrips(MainActivity.tripsArray);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        try {
            Intent intent = new Intent(TripsHistoryActivity.this, TripDetailsActivity.class);
            intent.putExtra("tripPosition", position);

            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showDeleteAlert(final int position){
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setMessage(getResources().getString(R.string.trip_delete_alert_message) + String.valueOf(position + 1) + ") ?");
        dialog.setPositiveButton(getResources().getString(R.string.trip_delete_alert_yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                MainActivity.tripsArray.remove(position);
                tripsAdapter.notifyDataSetChanged();

                if(MainActivity.tripsArray == null || MainActivity.tripsArray.isEmpty())
                    textNoItems.setVisibility(View.VISIBLE);
                else
                    textNoItems.setVisibility(View.GONE);

                storeTripsToStorage();
            }
        });
        dialog.setNegativeButton(getResources().getString(R.string.location_alert_button_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {

            }
        });
        dialog.show();
    }

    @Override
    public void onTripDelete(int position) {
        showDeleteAlert(position);
    }
}
