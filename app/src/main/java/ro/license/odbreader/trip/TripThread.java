package ro.license.odbreader.trip;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import ro.aptus.odbreader.R;
import ro.license.odbreader.activity.MainActivity;
import ro.license.odbreader.data.General;
import ro.license.odbreader.data.ObdResult;
import ro.license.odbreader.data.TempStorage;

public class TripThread extends Thread implements LocationListener {

    private final String TAG = TripThread.class.getName();
    private boolean isRunning = false;
    private long SLEEP_TIME = 3000;
    private Context context;
    private LocationManager locationManager;
    private Location location;

    private ArrayList<CheckPoint> checkPointsArray = null;
    private TempStorage tempStorage = null;

    long startTime = 0;
    long endTime = 0;

    private String FILE_NAME = "";

    public TripThread(Context context, TempStorage tempStorage) {
        try {
            startTime = System.currentTimeMillis();

            this.context = context;
            this.checkPointsArray = new ArrayList<>();
            this.tempStorage = tempStorage;

            this.FILE_NAME = "temp_" + General.getAccurateTime();

            locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 10, this);

            location = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);

            isRunning = true;

        } catch (SecurityException se){
            se.printStackTrace();
            Toast.makeText(context, "Location permission not granted.", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void run() {
        try {
            while (isRunning){

                if(location != null){
                    double lat = location.getLatitude();
                    double lon = location.getLongitude();

                    ArrayList<ObdResult> resultsToStore = tempStorage.getObdResults();
                    CheckPoint cp = new CheckPoint(resultsToStore, lat, lon);
                    storeCheckPoint(cp);
                }
                Thread.sleep(SLEEP_TIME); // default 3sec
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
    }


    public void stopService(){
        try {
            this.isRunning = false;

            endTime = System.currentTimeMillis();
            long seconds = (endTime - startTime) / 1000;

            String duration = getFormattedDuration(seconds);

            ArrayList<CheckPoint> tempCpArray = getCpArray();

            Trip trip = new Trip(tempCpArray, General.getFormattedTime(), duration);
            if(MainActivity.tripsArray == null)
                MainActivity.tripsArray = new ArrayList<>();

            MainActivity.tripsArray.add(trip);
            Toast.makeText(context, context.getResources().getString(R.string.trip_stored_success), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getFormattedDuration(long duration){

        int min = 0;
        int sec = 0;

        if(duration > 60){
            min = (int) duration / 60;
            sec = (int) duration - (60 * min);
        } else {
            sec = (int) duration;
        }

        String stringMin = "";
        String stringSec = "";

        if(min > 9)
            stringMin = String.valueOf(min);
        else
            stringMin = "0" + String.valueOf(min);

        if(sec > 9)
            stringSec = String.valueOf(sec);
        else
            stringSec = "0" + String.valueOf(sec);

        return stringMin + ":" + stringSec;
    }

    private boolean fileDeleted = false;

    public boolean isRunning(){
        return isRunning;
    }

    private void storeCheckPoint(CheckPoint checkPoint) {
        try {
            File file = new File(context.getFilesDir(), this.FILE_NAME);

            if(!fileDeleted){
                file.delete();
                fileDeleted = true;
            }

            FileWriter writer = new FileWriter (file, true);
            Gson gson = new Gson();
            String jsonCheckPoint = gson.toJson(checkPoint);

            writer.write(jsonCheckPoint);
            writer.write("\n");
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<CheckPoint> getCpArray(){
        try {

            ArrayList<CheckPoint> cpArray = new ArrayList<>();

            File file = new File(context.getFilesDir(), this.FILE_NAME);
            if(!file.exists()){
                Log.v(TAG, "Couldn't get checkpoints array. File does not exist.");
                return cpArray;
            }

            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    Gson gson = new Gson();
                    CheckPoint cp = gson.fromJson(line, CheckPoint.class);
                    cpArray.add(cp);
                }
                Log.v(TAG, "Checkpoints successfully read from file");
                return cpArray;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void onLocationChanged(Location location) {
        if(location != null)
            this.location = location;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
