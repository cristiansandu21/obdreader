package ro.license.odbreader.trip;


import java.util.ArrayList;

public class Trip {

    ArrayList<CheckPoint> checkPoints;
    String date;
    String duration;

    public Trip (ArrayList<CheckPoint> checkPoints, String date, String duration){
        this.checkPoints = checkPoints;
        this.date = date;
        this.duration = duration;
    }

    public ArrayList<CheckPoint> getCheckPoints() {
        return checkPoints;
    }

    public void setCheckPoints(ArrayList<CheckPoint> checkPoints) {
        this.checkPoints = checkPoints;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

}
