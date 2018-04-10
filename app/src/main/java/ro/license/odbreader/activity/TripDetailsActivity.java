package ro.license.odbreader.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

import ro.aptus.odbreader.R;
import ro.license.odbreader.data.ObdResult;
import ro.license.odbreader.data.PolylineResult;
import ro.license.odbreader.services.CmdSpinnerAdapter;
import ro.license.odbreader.trip.CheckPoint;
import ro.license.odbreader.trip.Trip;

public class TripDetailsActivity extends BaseActivity implements OnMapReadyCallback, AdapterView.OnItemSelectedListener {

    private final String TAG = TripDetailsActivity.class.getName();
    private Trip trip = null;

    private GoogleMap mapView = null;

    private Spinner spinner;
    private CmdSpinnerAdapter spinnerAdapter;
    private TextView textAverageValue, textMaxValue;
    private RelativeLayout layoutTop;
    private ImageButton btnRecenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_details);

        try {
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Initialize();
    }

    private void Initialize() {
        try {
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setTitle(getResources().getString(R.string.activity_trip_details));
            }

            spinner = (Spinner) findViewById(R.id.maps_spinner);
            textAverageValue = (TextView) findViewById(R.id.text_average_value);
            textMaxValue = (TextView) findViewById(R.id.text_max_value);
            layoutTop = (RelativeLayout) findViewById(R.id.layout_top_details);
            btnRecenter = (ImageButton) findViewById(R.id.btn_recenter);
            btnRecenter.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    centerCamera();
                }
            });

            int tripPosition = getIntent().getIntExtra("tripPosition", -1);

            if (tripPosition != -1) {
                trip = MainActivity.tripsArray.get(tripPosition);
                spinnerAdapter = new CmdSpinnerAdapter(this, trip.getCheckPoints().get(0).getObdResults());
                spinner.setAdapter(spinnerAdapter);
                spinner.setOnItemSelectedListener(this);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        try {
            this.mapView = googleMap;

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mapView.setMyLocationEnabled(true);
            centerCamera();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void centerCamera(){
        double startLat = trip.getCheckPoints().get(0).getLatitude();
        double startLon = trip.getCheckPoints().get(0).getLongitude();

        double endLat = trip.getCheckPoints().get(trip.getCheckPoints().size() - 1).getLatitude();
        double endLon = trip.getCheckPoints().get(trip.getCheckPoints().size() - 1).getLongitude();

        LatLng startLatLng = new LatLng(startLat, startLon);
        LatLng endLatLng = new LatLng(endLat, endLon);

        zoomToSpan(startLatLng, endLatLng);

    }

    private void zoomToSpan(LatLng start, LatLng end){
        try {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(start);
            builder.include(end);
            LatLngBounds bounds = builder.build();
            bounds = adjustBoundsForMaxZoomLevel(bounds);

            Point size = new Point();
            getWindowManager().getDefaultDisplay().getSize(size);
            int screenWidth = size.x;
            int screenHeight = size.y;
            int padding = (int)(0.10 * screenWidth); // offset from edges of the map in pixels as 15% of screen width
            int height = screenHeight - layoutTop.getHeight();

            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, screenWidth, height, padding); //(bounds, padding)
            mapView.animateCamera(cu);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private LatLngBounds adjustBoundsForMaxZoomLevel(LatLngBounds bounds) {
        LatLng sw = bounds.southwest;
        LatLng ne = bounds.northeast;
        double deltaLat = Math.abs(sw.latitude - ne.latitude);
        double deltaLon = Math.abs(sw.longitude - ne.longitude);

        final double zoomN = 0.001; // minimum zoom coefficient
        if (deltaLat < zoomN) {
            sw = new LatLng(sw.latitude - (zoomN - deltaLat / 2), sw.longitude);
            ne = new LatLng(ne.latitude + (zoomN - deltaLat / 2), ne.longitude);
            bounds = new LatLngBounds(sw, ne);
        }
        else if (deltaLon < zoomN) {
            sw = new LatLng(sw.latitude, sw.longitude - (zoomN - deltaLon / 2));
            ne = new LatLng(ne.latitude, ne.longitude + (zoomN - deltaLon / 2));
            bounds = new LatLngBounds(sw, ne);
        }
        return bounds;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        ObdResult selectedResult = (ObdResult) parent.getSelectedItem();

        String averageResult = getAverageResult(selectedResult.getCmdName()) + selectedResult.getCmdUnit();
        String maxResult = getMaxResult(selectedResult.getCmdName()) + selectedResult.getCmdUnit();

        textAverageValue.setText(averageResult);
        textMaxValue.setText(maxResult);

        drawPolyline(selectedResult);
    }

    ArrayList<Polyline> usedPolyLines;

    private void drawPolyline(ObdResult obdResult){
        ArrayList<PolylineResult> polylineResultsArray = getPolylineResults(obdResult.getCmdName());

        if(usedPolyLines == null){
            usedPolyLines = new ArrayList<>();
        } else {
            for(Polyline polyline : usedPolyLines){
                polyline.remove();
            }
            usedPolyLines.clear();
        }

        if(polylineResultsArray != null && !polylineResultsArray.isEmpty()){

            int currentColor = -1;
            LatLng lastDrawnPoint = null;

            PolylineOptions polylineOptions = null;
            for(PolylineResult polyResult : polylineResultsArray){

                if(currentColor == -1){ // goes here only for the first point drawn
                    currentColor = polyResult.getColor();
                    polylineOptions = new PolylineOptions();
                } else {
                    if(currentColor != polyResult.getColor()){ // if incoming color is different from previous one
                        currentColor = polyResult.getColor();
                        polylineOptions = new PolylineOptions(); // create a new instance of polylineOptions for the new color
                        polylineOptions.add(lastDrawnPoint, polyResult.getLatLng()); // join the previous drawing to the new one, to not lose the gap between
                    } else {
                        polylineOptions.add(polyResult.getLatLng()); // color is the same, keep drawing
                    }
                }
                lastDrawnPoint = polyResult.getLatLng();
                polylineOptions.color(currentColor);
                Polyline polylineFinal = mapView.addPolyline(polylineOptions);
                usedPolyLines.add(polylineFinal);
            }
        }
    }

    private ArrayList<PolylineResult> getPolylineResults(String cmdName){
        ArrayList<PolylineResult> polylineResultsArray = new ArrayList<>();

        for(CheckPoint checkPoint : trip.getCheckPoints()){
            for(ObdResult obdResult : checkPoint.getObdResults()){
                if (obdResult.getCmdName().equals(cmdName)) {

                    double latitude = checkPoint.getLatitude();
                    double longitude = checkPoint.getLongitude();
                    int color = getColorByThreshold(obdResult);

                    PolylineResult polylineResult = new PolylineResult(latitude, longitude, color);
                    polylineResultsArray.add(polylineResult);

                }
            }
        }
        return polylineResultsArray;
    }

    private int getColorByThreshold(ObdResult obdResult){
        try {
            float calculatedResult = -1;

            switch (obdResult.getCmdName()){
                case "Vehicle Speed":
                    calculatedResult = Float.parseFloat(obdResult.getCmdCalculatedResult());

                    if(calculatedResult <= 50)
                        return Color.GREEN;
                    else if (calculatedResult > 50 && calculatedResult <= 80)
                        return Color.YELLOW;
                    else if (calculatedResult > 80)
                        return Color.RED;

                case "Engine RPM":
                    calculatedResult = Float.parseFloat(obdResult.getCmdCalculatedResult());

                    if(calculatedResult <= 2800)
                        return Color.GREEN;
                    else if (calculatedResult > 2800 && calculatedResult <= 4500)
                        return Color.YELLOW;
                    else if (calculatedResult > 4500)
                        return Color.RED;

                case "Throttle Position":
                    calculatedResult = Float.parseFloat(obdResult.getCmdCalculatedResult());

                    if(calculatedResult <= 60)
                        return Color.GREEN;
                    else if (calculatedResult > 60 && calculatedResult <= 80)
                        return Color.YELLOW;
                    else if (calculatedResult > 80)
                        return Color.RED;

                case "Engine Coolant Temperature":
                    calculatedResult = Float.parseFloat(obdResult.getCmdCalculatedResult());

                    if(calculatedResult <= 85)
                        return Color.GREEN;
                    else if (calculatedResult > 85 && calculatedResult <= 120)
                        return Color.YELLOW;
                    else if (calculatedResult > 120)
                        return Color.RED;

                case "Mass Air Flow":
                    calculatedResult = Float.parseFloat(obdResult.getCmdCalculatedResult());

                    if(calculatedResult <= 250)
                        return Color.GREEN;
                    else if (calculatedResult > 250 && calculatedResult <= 450)
                        return Color.YELLOW;
                    else if (calculatedResult > 450)
                        return Color.RED;

                default :
                    return getResources().getColor(R.color.polyline_light_blue);
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private String getAverageResult(String cmdName){
        long averageValue = 0;
        float total = 0;
        int howMany = 0;

        for(CheckPoint checkPoint : trip.getCheckPoints()){
            for(ObdResult obdResult : checkPoint.getObdResults()){
                if(obdResult.getCmdName().equals(cmdName)){
                    total = total + Float.parseFloat(obdResult.getCmdCalculatedResult());
                    howMany++;
                }
            }
        }
        averageValue = (long) (total / howMany);
        return String.valueOf(averageValue);
    }

    private String getMaxResult(String cmdName){
        float maxValue = 0;

        for(CheckPoint checkPoint : trip.getCheckPoints()){
            for(ObdResult obdResult : checkPoint.getObdResults()){
                if(obdResult.getCmdName().equals(cmdName)){
                    if(Float.parseFloat(obdResult.getCmdCalculatedResult()) > maxValue){
                        maxValue = Float.parseFloat(obdResult.getCmdCalculatedResult());
                    }
                }
            }
        }
        return String.valueOf((long) maxValue);
    }
}
