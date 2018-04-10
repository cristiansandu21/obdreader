package ro.license.odbreader.data;

import com.google.android.gms.maps.model.LatLng;

public class PolylineResult {

    LatLng latLng;
    int color;

    public PolylineResult(double lat, double lon, int color){
        this.latLng = new LatLng(lat, lon);
        this.color = color;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }
}
