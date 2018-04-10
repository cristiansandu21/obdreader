package ro.license.odbreader.obd;

import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.github.pires.obd.commands.ObdCommand;
import com.github.pires.obd.commands.protocol.EchoOffCommand;
import com.github.pires.obd.commands.protocol.LineFeedOffCommand;
import com.github.pires.obd.commands.protocol.SelectProtocolCommand;
import com.github.pires.obd.commands.protocol.TimeoutCommand;
import com.github.pires.obd.enums.ObdProtocols;
import com.github.pires.obd.exceptions.UnsupportedCommandException;

import java.io.IOException;
import java.util.ArrayList;

import ro.license.odbreader.activity.SettingsActivity;
import ro.license.odbreader.data.General;


public class ObdGatewayService extends Thread {

    private final String TAG = ObdGatewayService.class.getName();
    private BluetoothSocket btSocket = null;
    public ObdProgressListener delegate = null;

    boolean isRunning = false;
    boolean connSetup = false;
    private ArrayList<ObdCommandJob> jobs = null;
    SharedPreferences prefs;
    Context context;

    public ObdGatewayService(BluetoothSocket btSocket, Context context){
        this.btSocket = btSocket;
        isRunning = true;
        jobs = new ArrayList<>();

        // Reset the working obd commands list
        General.workingObdCommands = null;
        General.commandListFinished = false;

        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        this.context = context;
    }

    @Override
    public void run() {
        delegate.onServiceStarted();

        while(isRunning){
            try {
                if(btSocket.isConnected()){
                    if(!connSetup){
                        setupConnection();
                        connSetup = true;
                    }

                    if(jobs.isEmpty()){
                        queueCommands();
                    }

                    executeJobs();

                } else {
                    Log.d(TAG, "Bluetooth socket closed");
                    Thread.currentThread().interrupt();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void queueCommands(){
        if(!General.commandListFinished){
            for (ObdCommand command : ObdConfig.getCommands()){
                if (prefs.getBoolean(command.getName(), true))
                    jobs.add(new ObdCommandJob(command));
            }
            General.commandListFinished = true;

            // There are no new commands to come, therefore start trip thread here

        } else {
            for(ObdCommand command2 : General.workingObdCommands){
                jobs.add(new ObdCommandJob(command2));
            }
        }
    }

    private void executeJobs(){
        for(ObdCommandJob job : jobs){
            sendJobRequest(job);
        }

        jobs.removeAll(jobs);

        try {
            Thread.currentThread().sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void sendJobRequest(ObdCommandJob job) {
        try {
            if (job.getState().equals(ObdCommandJob.ObdCommandJobState.NEW)) {
                job.setState(ObdCommandJob.ObdCommandJobState.RUNNING);
                if (btSocket.isConnected()) {
                    job.getCommand().run(btSocket.getInputStream(), btSocket.getOutputStream());
                } else {
                    job.setState(ObdCommandJob.ObdCommandJobState.EXECUTION_ERROR);
                    Log.e(TAG, "Can't run command on a closed socket");
                }
            } else {
                Log.e(TAG, "Job state was not new, so it shouldn't be in queue. BUG ALERT!");
            }
        } catch (InterruptedException i) {
            Thread.currentThread().interrupt();
        } catch (UnsupportedCommandException u) {
            if (job != null) {
                job.setState(ObdCommandJob.ObdCommandJobState.NOT_SUPPORTED);
            }
            Log.d(TAG, "Command not supported. ->" + u.getMessage());
        } catch (IOException io) {
            if (job != null) {
                if (io.getMessage().contains("Broken pipe"))
                    job.setState(ObdCommandJob.ObdCommandJobState.BROKEN_PIPE);
                else
                    job.setState(ObdCommandJob.ObdCommandJobState.EXECUTION_ERROR);
            }
            Log.e(TAG, "IO Error. ->" + io.getMessage());
        } catch (Exception e) {
            if (job != null) {
                job.setState(ObdCommandJob.ObdCommandJobState.EXECUTION_ERROR);
            }
            Log.e(TAG, "Failed to run command. ->" + e.getMessage());
        }

        if (job != null) {
            delegate.stateUpdate(job);
        }
    }

    private void setupConnection(){
        try {
            synchronized (this){
                new EchoOffCommand().run(btSocket.getInputStream(), btSocket.getOutputStream());
                wait(100);
                new LineFeedOffCommand().run(btSocket.getInputStream(), btSocket.getOutputStream());
                wait(100);
                new TimeoutCommand(110).run(btSocket.getInputStream(), btSocket.getOutputStream());
                wait(100);
                // Get protocol from preferences, default "AUTO"
                final String protocol = prefs.getString(SettingsActivity.PROTOCOLS_LIST_KEY, "AUTO");
                new SelectProtocolCommand(ObdProtocols.valueOf(protocol)).run(btSocket.getInputStream(), btSocket.getOutputStream());
                wait(100);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public void stopService(String reason){
        isRunning = false;
        delegate.onServiceStopped(reason);
    }

    public boolean isRunning(){
        return isRunning;
    }
}
