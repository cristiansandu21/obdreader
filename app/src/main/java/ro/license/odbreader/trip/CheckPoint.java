package ro.license.odbreader.trip;

import java.util.ArrayList;

import ro.license.odbreader.data.ObdResult;


public class CheckPoint {

    ArrayList<ObdResult> obdResults;
    double latitude;
    double longitude;

    public CheckPoint(ArrayList<ObdResult> obdResults, double latitude, double longitude){
        this.obdResults = obdResults;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public ArrayList<ObdResult> getObdResults() {
        return obdResults;
    }

    public void setObdResults(ArrayList<ObdResult> obdResults) {
        this.obdResults = obdResults;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
