package ro.license.odbreader.communication;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.AsyncTask;

import java.util.UUID;


public class BluetoothConnector extends AsyncTask<BluetoothDevice, Void, BtResponse> {

    public BluetoothConnectionListener delegate = null;

    private BluetoothDevice bluetoothDevice = null;

    public BluetoothConnector(BluetoothDevice bluetoothDevice){
        this.bluetoothDevice = bluetoothDevice;
    }

    @Override
    protected BtResponse doInBackground(BluetoothDevice... params) {
        try {
            UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

            BluetoothSocket socket = this.bluetoothDevice.createInsecureRfcommSocketToServiceRecord(uuid);

            socket.connect();

            return new BtResponse(200, socket, "");

        } catch (Exception e) {
            e.printStackTrace();
            return new BtResponse(303, null, e.getLocalizedMessage());
        }
    }

    @Override
    protected void onPostExecute(BtResponse response) {
        delegate.onBluetoothSuccessConnection(response);
    }

    @Override
    protected void onCancelled() {
        delegate.onBluetoothFailedConnection();
    }

}
