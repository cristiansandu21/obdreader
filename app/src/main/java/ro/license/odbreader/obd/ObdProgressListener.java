package ro.license.odbreader.obd;

public interface ObdProgressListener {

    void stateUpdate(final ObdCommandJob job);
    void onServiceStarted();
    void onServiceStopped(String reason);

}