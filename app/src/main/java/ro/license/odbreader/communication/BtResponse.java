package ro.license.odbreader.communication;

import android.bluetooth.BluetoothSocket;


public class BtResponse {

    BluetoothSocket socket;
    int statusCode;
    String responseString;

    public BtResponse(int statusCode, BluetoothSocket socket, String responseString){
        this.statusCode = statusCode;
        this.socket = socket;
        this.responseString = responseString;
    }

    public BluetoothSocket getSocket() {
        return socket;
    }

    public void setSocket(BluetoothSocket socket) {
        this.socket = socket;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getResponseString() {
        return responseString;
    }

    public void setResponseString(String responseString) {
        this.responseString = responseString;
    }
}
