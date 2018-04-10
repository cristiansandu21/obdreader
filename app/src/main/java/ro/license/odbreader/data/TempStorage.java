package ro.license.odbreader.data;


import java.util.ArrayList;

public class TempStorage {

    private ArrayList<ObdResult> obdResults;

    public TempStorage (){

    }

    public void setObdResults(ArrayList<ObdResult> obdResults){
        this.obdResults = obdResults;
    }

    public ArrayList<ObdResult> getObdResults(){
        return this.obdResults;
    }

}
