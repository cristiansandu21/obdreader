package ro.license.odbreader.data;


import com.github.pires.obd.commands.ObdCommand;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class General {

    public static ArrayList<ObdResult> resultsList = null;
    public static ArrayList<ObdCommand> workingObdCommands = null;

    public static boolean commandListFinished = false;
    public static final String[] badResults = {"NODATA", "NA" , "OFF", "?"};


    public static String getFormattedTime(){
        String currentDateTimeString = new SimpleDateFormat("dd-MM HH:mm").format(new Date());

        return currentDateTimeString;
    }

    public static String getAccurateTime(){
        String currentDateTimeString = new SimpleDateFormat("dd-MM HH-mm-ss").format(new Date());

        return currentDateTimeString;
    }

    public static String cmdToRomanian(String cmdName){

        switch (cmdName){
            case "Engine Load":
                return "Sarcina motorului";

            case "Engine RPM":
                return "RPM Motor";

            case "Mass Air Flow":
                return "Debimetru de aer";

            case "Throttle Position":
                return "Poziţia clapetei de accelerație";

            case "Engine Coolant Temperature":
                return "Temperatura motorului";

            case "Vehicle Speed":
                return "Viteza vehiculului";

            case "Control Module Power Supply ":
                return "Alimentarea modulului de comandă";

            case "Command Equivalence Ratio":
                return "Rata echivalentă a comenzii";

            case "Distance traveled with MIL on":
                return "Distanță parcursă cu MIL aprins";

            case "Timing Advance":
                return "Sincronizare în avans";

            case "Vehicle Identification Number (VIN)":
                return "Număr de identificare al vehiculului";

            case "Engine Runtime":
                return "Timpul de funcționare a motorului";

            case "Fuel Type":
                return "Tipul combustibilului";

            case "Fuel Consumption Rate":
                return "Rata de consum de combustibil";

            case "Fuel Level":
                return "Nivelul combustibilului";

            case "Air/Fuel Ratio":
                return "Raport aer / combustibil";

            case "Wideband Air/Fuel Ratio":
                return "Raport aer / combustibil cu bandă largă";

            case "Engine oil temperature":
                return "Temperatura uleiului de motor";

            case "Barometric Pressure":
                return "Presiune barometrică";

            case "Fuel Pressure":
                return "Presiunea combustibilului";

            case "Fuel Rail Pressure":
                return "Presiunea din conducta de alimentare";

            case "Intake Manifold Pressure":
                return "Presiunea colectorului de admisie";

            case "Air Intake Temperature":
                return "Temperatura de admisie a aerului";

            case "Ambient Air Temperature":
                return "Temperatura aerului ambiant";

        }
        return "";
    }
}
