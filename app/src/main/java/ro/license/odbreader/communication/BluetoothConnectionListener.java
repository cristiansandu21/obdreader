package ro.license.odbreader.communication;

public interface BluetoothConnectionListener {
    void onBluetoothSuccessConnection(BtResponse socket);
    void onBluetoothFailedConnection();
}