package ro.license.odbreader.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.pires.obd.commands.ObdCommand;
import com.github.pires.obd.enums.AvailableCommandNames;

import java.util.ArrayList;

import ro.aptus.odbreader.R;
import ro.license.odbreader.communication.BluetoothConnectionListener;
import ro.license.odbreader.communication.BluetoothConnector;
import ro.license.odbreader.communication.BtResponse;
import ro.license.odbreader.data.General;
import ro.license.odbreader.data.ObdResult;
import ro.license.odbreader.data.TempStorage;
import ro.license.odbreader.obd.ObdCommandJob;
import ro.license.odbreader.obd.ObdGatewayService;
import ro.license.odbreader.obd.ObdProgressListener;
import ro.license.odbreader.services.ResultAdapter;
import ro.license.odbreader.services.TripsWriter;
import ro.license.odbreader.trip.Trip;
import ro.license.odbreader.trip.TripThread;

import static com.github.pires.obd.enums.AvailableCommandNames.values;
import static ro.license.odbreader.obd.ObdCommandJob.ObdCommandJobState;

public class MainActivity extends BaseActivity implements View.OnClickListener, BluetoothConnectionListener, ObdProgressListener {

    // Elements
    private TextView textBluetoothStatus, textObdStatus;
    private Button btnConnect, btnData;
    private final int BUTTON_MODE_START = 10;
    private final int BUTTON_MODE_STOP = 11;
    private int BUTTON_MODE = BUTTON_MODE_START;
    private ListView listViewResults;
    private ResultAdapter resultAdapter = null;

    // Bluetooth
    private BluetoothAdapter btAdapter;
    private BluetoothDevice btDevice = null;
    private BluetoothSocket btSocket = null;
    private BluetoothConnector mBluetoothConnector = null;
    private final int REQUEST_ENABLE_BT = 1;

    // General
    private static final String TAG = MainActivity.class.getName();
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 2;
    private ProgressDialog dialog;
    private SharedPreferences prefs;

    // OBD
    private ObdGatewayService gatewayService = null;
    private static final int MENU_SETTINGS = 21;
    private static final int MENU_TRIPS_HISTORY = 22;
    private int jobsPosition = 0;

    private TripThread tripThread = null;
    private TempStorage tempStorage = null;
    public static ArrayList<Trip> tripsArray = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(getResources().getString(R.string.main_title));
        }

        Initialize();

    }

    private void Initialize(){
        textBluetoothStatus = (TextView) findViewById(R.id.text_bluetooth_status);
        textObdStatus = (TextView) findViewById(R.id.text_obd_status);
        textObdStatus.setText(getResources().getString(R.string.status_obd_disconnected));

        listViewResults = (ListView) findViewById(R.id.list_view_results);

        btnConnect = (Button) findViewById(R.id.btn_connect);
        btnConnect.setOnClickListener(this);

        btnData = (Button) findViewById(R.id.btn_data);
        btnData.setOnClickListener(this);
        btnData.setEnabled(false);
        btnData.setAlpha(0.5f);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        checkLocationPermission();

        readTripsFromStorage();

    }

    @Override
    protected void onResume() {
        super.onResume();
        bluetoothSetup();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(gatewayService != null && gatewayService.isRunning())
            gatewayService.stopService("");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, MENU_SETTINGS, 0, getResources().getString(R.string.menu_settings));
        menu.add(0, MENU_TRIPS_HISTORY, 0 , getResources().getString(R.string.menu_trips_history));
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_SETTINGS:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;

            case MENU_TRIPS_HISTORY:
                startActivity(new Intent(this, TripsHistoryActivity.class));
                return true;
        }
        return false;
    }

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission. ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission. ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle(getResources().getString(R.string.location_permission_title))
                        .setMessage(getResources().getString(R.string.location_permission_content))
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission. ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    private boolean isLocationEnabled(){
        LocationManager lm = (LocationManager)getSystemService(LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch(Exception ex) {}

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch(Exception ex) {}

        if(!gps_enabled && !network_enabled) {
            // notify user
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setMessage(getResources().getString(R.string.location_alert_title));
            dialog.setPositiveButton(getResources().getString(R.string.location_alert_button_ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(myIntent);
                    //get gps
                }
            });
            dialog.setNegativeButton(getResources().getString(R.string.location_alert_button_cancel), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    // TODO Auto-generated method stub

                }
            });
            dialog.show();
            return false;
        }
        return true;
    }

    private boolean bluetoothSetup(){
        // Check if the device supports Bluetooth
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        if(btAdapter == null){
            // Device does not support Bluetooth
        }

        if(btSocket != null && btSocket.isConnected()){
            textBluetoothStatus.setText(btDevice.getName() + getResources().getString(R.string.bluetooth_device_connected));
            textBluetoothStatus.setTextColor(getResources().getColor(R.color.point_grey_dark_font));
            return true;
        } else {
            // Check if Bluetooth is enabled
            if(btAdapter.isEnabled()){
                textBluetoothStatus.setText(getResources().getString(R.string.status_bluetooth_enabled));
                textBluetoothStatus.setTextColor(getResources().getColor(R.color.point_grey_dark_font));
                return true;
            } else {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }

        return false;
    }

    private void connectToDevice(){
        try {

            final String remoteDevice = prefs.getString(SettingsActivity.BLUETOOTH_LIST_KEY, null);

            if(remoteDevice == null || remoteDevice.isEmpty()){
                Toast.makeText(this, getResources().getString(R.string.no_bluetooth_selected), Toast.LENGTH_LONG).show();

                // log error
                Log.e(TAG, "No Bluetooth device has been selected.");
            } else {

                if(btAdapter == null)
                    btAdapter = BluetoothAdapter.getDefaultAdapter();

                btDevice = btAdapter.getRemoteDevice(remoteDevice);

                if(btSocket != null){
                    if(btSocket.isConnected())
                        btSocket.close();
                }

                btSocket = null;

                dialog = ProgressDialog.show(MainActivity.this,
                        "",
                        getResources().getString(R.string.connecting_dialog),
                        true,
                        false);

                mBluetoothConnector = new BluetoothConnector(btDevice);
                mBluetoothConnector.delegate = this;
                mBluetoothConnector.execute();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void stopLiveData(){
        if(gatewayService != null && gatewayService.isRunning()){
            gatewayService.stopService("");
        }
    }

    private void startLiveData(){
        if(isLocationEnabled()){
            if(btSocket != null){
                General.resultsList = null;
                this.tempStorage = null;
                resultAdapter = null;
                jobsPosition = 0;

                tripThread = null;

                btnData.setEnabled(false);
                btnData.setAlpha(0.5f);

                gatewayService = new ObdGatewayService(this.btSocket, MainActivity.this);
                gatewayService.delegate = this;
                gatewayService.start();
            } else {
                Toast.makeText(MainActivity.this, getResources().getString(R.string.bluetooth_no_socket), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_ENABLE_BT){
            if(resultCode == RESULT_OK)
                textBluetoothStatus.setText(getResources().getString(R.string.status_bluetooth_enabled));
            else
                textBluetoothStatus.setText(getResources().getString(R.string.status_bluetooth_disabled));
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        switch(id){
            case R.id.btn_connect:
                connectToDevice();
                break;
            case R.id.btn_data:
                switch (BUTTON_MODE){
                    case BUTTON_MODE_START:
                        startLiveData();
                        break;

                    case BUTTON_MODE_STOP:
                        stopLiveData();
                        break;
                }
                break;
        }
    }

    private void storeTripsToStorage(){
        try {
            if(this.tripsArray == null || this.tripsArray.isEmpty()){
                return;
            } else {
                TripsWriter tripsWriter = new TripsWriter(this);
                tripsWriter.storeTrips(this.tripsArray);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void readTripsFromStorage(){
        TripsWriter tripsWriter = new TripsWriter(this);
        this.tripsArray = tripsWriter.getStoredTripsArray();
    }

    @Override
    public void onBluetoothSuccessConnection(BtResponse response) {
        try {
            if(response == null)
                return;

            switch(response.getStatusCode()){
                case 200:
                    this.btSocket = response.getSocket();
                    Log.v(TAG, "Socket CONNECTED : " + btSocket.toString());
                    textBluetoothStatus.setText(btDevice.getName() + getResources().getString(R.string.bluetooth_device_connected));
                    textBluetoothStatus.setTextColor(getResources().getColor(R.color.point_grey_dark_font));
                    btnData.setEnabled(true);
                    btnData.setAlpha(1f);
                    break;

                case 303:
                    this.btSocket = null;
                    Log.v(TAG, "Socket NOT Connected : " + response.getResponseString());
                    textBluetoothStatus.setText(getResources().getString(R.string.bluetooth_failed_to_connect));
                    textBluetoothStatus.setTextColor(getResources().getColor(R.color.bt_offline));
                    Toast.makeText(this, getResources().getString(R.string.bluetooth_303), Toast.LENGTH_LONG).show();
                    break;
            }
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        } finally {
            dialog.dismiss();
        }
    }

    @Override
    public void onBluetoothFailedConnection() {
        Log.v(TAG, "Socket CANCELED");
    }

    public static String LookUpCommand(String txt) {
        for (AvailableCommandNames item : values()) {
            if (item.getValue().equals(txt)) return item.name();
        }
        return txt;
    }

    @Override
    public void stateUpdate(final ObdCommandJob job) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    String cmdName = job.getCommand().getName();
                    String cmdResult = "";
                    String cmdID = LookUpCommand(cmdName);

                    // added recently
                    String cmdCalculatedResult = "";
                    String cmdResultUnit = "";

                    if(job.getState().equals(ObdCommandJobState.EXECUTION_ERROR)){
                        cmdResult = job.getCommand().getResult();
                    } else if (job.getState().equals(ObdCommandJobState.BROKEN_PIPE)){
                        if(gatewayService != null && gatewayService.isRunning()){
                            gatewayService.stopService(getResources().getString(R.string.bluetooth_broken_pipe));
                        }
                    } else if (job.getState().equals(ObdCommandJobState.NOT_SUPPORTED)){
                        cmdResult = "NA";
                    } else {
                        cmdResult = job.getCommand().getFormattedResult();

                        cmdResultUnit = job.getCommand().getResultUnit();
                        cmdCalculatedResult = job.getCommand().getCalculatedResult();

                        if(!gatewayService.isInterrupted())
                            textObdStatus.setText(getResources().getString(R.string.obd_receiving_data));
                    }

                    ObdResult obdResult = new ObdResult(cmdName, cmdResult, jobsPosition);
                    obdResult.setCmdCalculatedResult(cmdCalculatedResult);
                    obdResult.setCmdUnit(cmdResultUnit);

                    if(General.resultsList == null)
                        General.resultsList = new ArrayList<ObdResult>();

                    if (General.workingObdCommands == null)
                        General.workingObdCommands = new ArrayList<ObdCommand>();

                    if(isResultOk(obdResult) && !isJobStored(job)){
                        General.workingObdCommands.add(job.getCommand());
                    }

                    int oldResultId = isResultStored(obdResult);

                    if (oldResultId == -1) {
                        General.resultsList.add(obdResult);
                        if (isResultOk(obdResult)) {
                            if(tempStorage == null)
                                tempStorage = new TempStorage();

                            if(tempStorage.getObdResults() == null)
                                tempStorage.setObdResults(new ArrayList<ObdResult>());

                            tempStorage.getObdResults().add(obdResult);
                        }
                        jobsPosition++;
                    } else {

                        if(tripThread == null){
                            tripThread = new TripThread(MainActivity.this, tempStorage);
                            tripThread.start();
                        }

                        btnData.setEnabled(true);
                        btnData.setAlpha(1f);

                        obdResult.setPosition(oldResultId);
                        General.resultsList.set(oldResultId, obdResult);

                        if (isResultOk(obdResult)){
                            int position = getGoodResultPosition(obdResult, tempStorage);

                            tempStorage.getObdResults().set(position, obdResult);
                        }
                    }

                    // Refresh the view
                    if(resultAdapter == null){
                        resultAdapter = new ResultAdapter(MainActivity.this, General.resultsList);
                        listViewResults.setAdapter(resultAdapter);
                    } else {
                        resultAdapter.notifyDataSetChanged();
                    }
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
    }

    private int isResultStored(ObdResult obdResult){
        if(General.resultsList != null && !General.resultsList.isEmpty()){
            for(ObdResult r : General.resultsList){
                if(r.getCmdName().equals(obdResult.getCmdName()))
                    return r.getPosition();
            }
        }
        return -1;
    }

    private int getGoodResultPosition(ObdResult obdResult, TempStorage tempStorage){
        int position = -1;
        if(tempStorage.getObdResults() != null && !tempStorage.getObdResults().isEmpty()){
            for(ObdResult r : tempStorage.getObdResults()){
                position++;
                if(r.getCmdName().equals(obdResult.getCmdName())){
                    return position;
                }
            }
        }
        return -1;
    }

    private boolean isResultOk(ObdResult obdResult){
        for (String s : General.badResults){
            if(obdResult.getCmdResult().contains(s))
                return false;
        }
        return true;
    }

    private boolean isJobStored(ObdCommandJob job){
        for(ObdCommand command : General.workingObdCommands){
            if(command.getName().equals(job.getCommand().getName()))
                return true;
        }
        return false;
    }

    @Override
    public void onServiceStarted() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                BUTTON_MODE = BUTTON_MODE_STOP;
                btnData.setText(getResources().getString(R.string.main_button_data_stop));
                btnConnect.setEnabled(false);
                btnConnect.setAlpha(0.5f);
                textObdStatus.setText(getResources().getString(R.string.obd_initializing));
            }
        });
    }

    @Override
    public void onServiceStopped(final String reason) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(reason.toLowerCase().contains("bluetooth")){
                    textBluetoothStatus.setText(btDevice.getName() + getResources().getString(R.string.bluetooth_device_disconnected));
                    textBluetoothStatus.setTextColor(getResources().getColor(R.color.bt_offline));
                    Toast.makeText(MainActivity.this, reason + "\n" + getResources().getString(R.string.service_stopped), Toast.LENGTH_SHORT).show();
                    btnData.setEnabled(false);
                    btnData.setAlpha(0.5f);
                }

                BUTTON_MODE = BUTTON_MODE_START;
                btnData.setText(getResources().getString(R.string.main_button_data_start));
                btnConnect.setEnabled(true);
                btnConnect.setAlpha(1f);


                if(tripThread != null)
                    if(tripThread.isRunning())
                        tripThread.stopService();

                storeTripsToStorage();

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        textObdStatus.setText(getResources().getString(R.string.obd_stopped));
                    }
                }, 500);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission. ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                    }

                } else {


                }
                return;
            }

        }
    }
}
