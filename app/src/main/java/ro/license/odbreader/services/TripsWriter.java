package ro.license.odbreader.services;


import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import ro.license.odbreader.trip.Trip;


public class TripsWriter {

    private final String TAG = TripsWriter.class.getName();
    public final static String TRIPS_FILE_NAME = "trips_history";
    private Context context;

    public TripsWriter(Context context){
        this.context = context;
    }

    public void storeTrips(ArrayList<Trip> trips) {
        try {
            File file = new File(context.getFilesDir(), TripsWriter.TRIPS_FILE_NAME);
            file.delete();

            FileWriter writer = new FileWriter (file, true);


            if(trips.isEmpty()){
                writer.write("");
                writer.close();
            } else {
                for(Trip t : trips){
                    Gson gson = new Gson();
                    String jsonTrip = gson.toJson(t);

                    writer.write(jsonTrip);
                    writer.write("\n");
                }
                writer.close();
            }

            Log.v(TAG, "Trips successfully stored : " + trips.size());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean tripAlreadyStored(String token){
        try {
            File file = new File(context.getFilesDir(), TripsWriter.TRIPS_FILE_NAME);
            if(!file.exists()){
                Log.v(TAG, "File does not exist.");
                return false;
            }

            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    if(line.equals(token))
                        return true;
                }
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public ArrayList<Trip> getStoredTripsArray(){
        try {

            ArrayList<Trip> tripsArray = new ArrayList<>();

            File file = new File(context.getFilesDir(), TRIPS_FILE_NAME);
            if(!file.exists()){
                Log.v(TAG, "Couldn't get trips array. File does not exist.");
                return tripsArray;
            }

            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    Gson gson = new Gson();
                    Trip trip = gson.fromJson(line, Trip.class);
                    tripsArray.add(trip);
                }
                Log.v(TAG, "Trips read from storage : " + tripsArray.size());
                return tripsArray;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
