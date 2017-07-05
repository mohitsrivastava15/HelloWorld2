package com.zeglabs.mohit.helloworld2.helper;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.zeglabs.mohit.helloworld2.R;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

/**
 * Created by mohit on 27/10/16.
 */
public class BluetoothCommunicator {
    String TAG = BluetoothCommunicator.class.getSimpleName();
    BluetoothAdapter bluetoothAdapter;
    BluetoothSocket mmSocket;
    BluetoothDevice mmDevice = null;
    OutputStream mmOutputStream;
    InputStream mmInputStream;
    volatile boolean stopWorker;

    Thread workerThread;
    byte[] readBuffer;
    int readBufferPosition;

    String macAddress;
    String reading;

    public String getDeviceName() {return deviceName;}
    public void setDeviceName(String deviceName) {this.deviceName = deviceName;}

    private String deviceName;

    private ProgressDialog mProgressDialog;
    private Activity activity;

    public BluetoothCommunicator(Activity activity, String macAddress, BluetoothAdapter adapter, ProgressDialog dialog) {
        this.activity = activity;
        this.macAddress = macAddress;
        this.bluetoothAdapter = adapter;
        this.mProgressDialog = dialog;
    }

    public BluetoothCommunicator(String macAddress, BluetoothAdapter adapter) {
        this.macAddress = macAddress;
        this.bluetoothAdapter = adapter;
        this.mProgressDialog = null;
    }

    private void showProgressDialog() {

        mProgressDialog.setMessage("Loading..");
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setMessage("Reading from sensor! Please wait...");
        mProgressDialog.show();
    }

    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.hide();
        }
    }

    public String getSensorReading() {
        stopWorker = false;

        try {
            findBT();
            if(mmDevice == null) {
                return null;
            }
            openBT();
        } catch (IOException e) {
            return null;
        }

        return reading;
    }

    private void findBT() {
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if(pairedDevices.size() > 0)
        {
            for(BluetoothDevice device : pairedDevices)
            {
                if(device.getAddress().contains(this.macAddress))
                {
                    mmDevice = device;
                    deviceName = mmDevice.getName();

                    //myLabel.setText("HC-05 device found");
                    break;
                }
            }
        }
        //myLabel.setText("HC-05 not found");
    }

    void openBT() throws IOException
    {
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); //Standard SerialPortService ID


        try {
            mmSocket = (BluetoothSocket) mmDevice.getClass().getMethod("createRfcommSocket", new Class[] {int.class}).invoke(mmDevice,1);
            mmSocket.connect();
        } catch (Exception e) {
            try
            {
                Log.i(TAG, "Trying fallback...");
                mmSocket = (BluetoothSocket) mmDevice.getClass().getMethod("createRfcommSocket", new Class[] {int.class}).invoke(mmDevice,1);
                mmSocket.connect();

            }
            catch (Exception e1)
            {
                Log.i(TAG, "Couldn't connect even through fallback! "+e1.getMessage());
                return;
            }
        }
        mmOutputStream = mmSocket.getOutputStream();
        mmInputStream = mmSocket.getInputStream();

        readBuffer = new byte[1024];


        beginListenForDataThread2();
    }

    void beginListenForDataThread2()
    {
        final byte delimiter = 10; //This is the ASCII code for a newline character

        stopWorker = false;
        readBufferPosition = 0;
        readBuffer = new byte[1024];
        workerThread = new Thread(new Runnable()
        {
            public void run()
            {
                while(!Thread.currentThread().isInterrupted() && !stopWorker)
                {
                    try
                    {
                        int bytesAvailable = mmInputStream.available();
                        if(bytesAvailable > 0)
                        {
                            byte[] packetBytes = new byte[bytesAvailable];
                            mmInputStream.read(packetBytes);
                            for(int i=0;i<bytesAvailable;i++)
                            {
                                byte b = packetBytes[i];
                                if(b == delimiter)
                                {
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    final String data = new String(encodedBytes, "US-ASCII");
                                    readBufferPosition = 0;

                                    reading = data;
                                    stopWorker = true;
                                    Log.i(TAG, "---------------hurray-------------------");

                                    break;

                                }
                                else
                                {
                                    readBuffer[readBufferPosition++] = b;
                                }
                            }
                        }
                    }
                    catch (IOException ex)
                    {
                        stopWorker = true;
                    } finally {

                    }
                }
            }
        });

        workerThread.start();

        try {
            workerThread.join();
            resetConnection();
        } catch (InterruptedException e) {
            Log.i(TAG, e.getMessage());
        }
    }

    void beginListenForDataThread()
    {
        final byte delimiter = 10; //This is the ASCII code for a newline character

        stopWorker = false;
        readBufferPosition = 0;
        readBuffer = new byte[1024];
        workerThread = new Thread(new Runnable()
        {
            public void run()
            {
                while(!Thread.currentThread().isInterrupted() && !stopWorker)
                {
                    try
                    {
                        int bytesAvailable = mmInputStream.available();
                        if(bytesAvailable > 0)
                        {
                            byte[] packetBytes = new byte[bytesAvailable];
                            mmInputStream.read(packetBytes);
                            for(int i=0;i<bytesAvailable;i++)
                            {
                                byte b = packetBytes[i];
                                if(b == delimiter)
                                {
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    final String data = new String(encodedBytes, "US-ASCII");
                                    readBufferPosition = 0;

                                    reading = data;
                                    stopWorker = true;
                                    Log.i(TAG, "---------------hurray-------------------");
                                    activity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            hideProgressDialog();
                                            showSyncConfirmAlertDialog();

                                        }
                                    });
                                    resetConnection();

                                    break;

                                }
                                else
                                {
                                    readBuffer[readBufferPosition++] = b;
                                }
                            }
                        }
                    }
                    catch (IOException ex)
                    {
                        stopWorker = true;
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                hideProgressDialog();
                                showDeviceNotFoundAlertDialog();

                            }
                        });
                        resetConnection();
                    } finally {
                        resetConnection();
                    }
                }
            }
        });
        workerThread.start();
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showProgressDialog();
            }
        });


    }

    /**
     * Reset input and output streams and make sure socket is closed.
     * This method will be used during shutdown() to ensure that the connection is properly closed during a shutdown.
     * @return
     */
    private void resetConnection() {
        if (mmInputStream != null) {
            try {mmInputStream.close();} catch (Exception e) {}

        }
        if (mmOutputStream != null) {
            try {mmOutputStream.close();} catch (Exception e) {}

        }

        if (mmSocket != null) {
            try {mmSocket.close();} catch (Exception e) {}

        }

    }

    private void showDeviceNotFoundAlertDialog() {
        String error = "--------- Sorry! Device with macAddress "+this.macAddress+" could not be found! Please ensure that the sensor is on, the phone is within a distance of 20m and its bluetooth is working!";

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LayoutInflater inflater = activity.getLayoutInflater();
        final View dialogLayout = inflater.inflate(R.layout.dialog_device_not_found, null);
        builder.setView(dialogLayout);

        final AlertDialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(false);

        Button btnOk = (Button) dialogLayout.findViewById(R.id.btn_deviceNotFoundOK);
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                alertDialog.hide();
            }
        });

        Button btnX = (Button) dialogLayout.findViewById(R.id.btn_closeX);
        btnX.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.hide();
            }
        });

        TextView txtError = (TextView) dialogLayout.findViewById(R.id.txt_errorDeviceNotFound);
        txtError.setText(error);

        alertDialog.show();

    }

    private void showSyncConfirmAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LayoutInflater inflater = activity.getLayoutInflater();
        final View dialogLayout = inflater.inflate(R.layout.dialog_sync_reading, null);
        builder.setView(dialogLayout);

        final AlertDialog alertDialog = builder.create();


        Button btnSync = (Button) dialogLayout.findViewById(R.id.btn_syncReading);
        btnSync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                /**
                 * TODO: Write code to connect with Volley and send data to the server
                 */

                //Toast.makeText(appContext, "Sending reading="+sensorReading+" to the server!",Toast.LENGTH_LONG).show();
                alertDialog.hide();
            }
        });

        EditText txtReading = (EditText) dialogLayout.findViewById(R.id.txt_reading);
        txtReading.setText(reading);

        alertDialog.show();
    }
}
