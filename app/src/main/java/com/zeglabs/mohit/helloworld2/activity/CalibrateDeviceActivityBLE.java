package com.zeglabs.mohit.helloworld2.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.zeglabs.mohit.helloworld2.R;
import com.zeglabs.mohit.helloworld2.app.MyApplication;
import com.zeglabs.mohit.helloworld2.helper.BluetoothCommunicator;
import com.zeglabs.mohit.helloworld2.helper.ComplexPreferences;
import com.zeglabs.mohit.helloworld2.helper.Config;
import com.zeglabs.mohit.helloworld2.helper.PrefManager;
import com.zeglabs.mohit.helloworld2.model.SensorDetailsMap;
import com.zeglabs.mohit.helloworld2.service.RBLService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.R.attr.data;

public class CalibrateDeviceActivityBLE extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private Activity act;
    private String TAG = CalibrateDeviceActivityBLE.class.getSimpleName();

    NavigationView navigationView;
    private ViewGroup viewGroup;
    private Context appContext;

    private String sensorMac;
    private String sensorReading;
    private String itemName;

    private BluetoothAdapter bluetoothAdapter;

    private EditText editTxtContainerHeightInput;
    private Button txtClear;
    private Button txtOK;
    private View bottomPanel;
    private Button btnCalibrate;

    private BluetoothCommunicator bc;
    private boolean hideBottomPanel = true;

    ProgressDialog dialog;

    private SensorDetailsMap sensorCache;
    ComplexPreferences complexPreferences;

    //private SensorDetailsMap cache;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        act = CalibrateDeviceActivityBLE.this;

        appContext = getApplicationContext();
        viewGroup = (ViewGroup) (this.findViewById(android.R.id.content));

        sensorMac = PrefManager.getFromPrefs(getApplicationContext(), PrefManager.SENSOR_FOUND_MAC, "");
        itemName = getIntent().getStringExtra(PrefManager.SENSORCREATE_ITEM);
        mDevice = getIntent().getParcelableExtra(PrefManager.BLUETOOTH_DEVICE);

        //cache = getIntent().getParcelableExtra(PrefManager.SENSOR_CACHE);
        complexPreferences = ComplexPreferences.getComplexPreferences(getApplicationContext(), "", 0);
        sensorCache = complexPreferences.getObject(PrefManager.SENSOR_CACHE, SensorDetailsMap.class);

        setContentView(R.layout.activity_calibrate_device);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setEditTextContainerHeight();
        setupCalibrateButton();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        setupNavdrawer();

        setupGattService();
    }

    private BluetoothGattCharacteristic characteristicTx = null;
    private RBLService mBluetoothLeService;
    private BluetoothDevice mDevice = null;
    private String mDeviceAddress;

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName,
                                       IBinder service) {
            mBluetoothLeService = ((RBLService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            mBluetoothLeService.connect(mDevice.getAddress());
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    private String data = "";

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (RBLService.ACTION_GATT_DISCONNECTED.equals(action)) {
                Toast.makeText(getApplicationContext(), "Disconnected",
                        Toast.LENGTH_SHORT).show();
                //setButtonDisable();
            } else if (RBLService.ACTION_GATT_SERVICES_DISCOVERED
                    .equals(action)) {
                Toast.makeText(getApplicationContext(), "Connected",
                        Toast.LENGTH_SHORT).show();

                getGattService(mBluetoothLeService.getSupportedGattService());
            } else if (RBLService.ACTION_DATA_AVAILABLE.equals(action)) {
                data = intent.getStringExtra(RBLService.EXTRA_DATA);

                //readAnalogInValue(data);
            } else if (RBLService.ACTION_GATT_RSSI.equals(action)) {
                Toast.makeText(getApplicationContext(), intent.getStringExtra(RBLService.EXTRA_DATA),
                        Toast.LENGTH_SHORT).show();

            }
        }
    };

    private void getGattService(BluetoothGattService gattService) {
        if (gattService == null)
            return;

        //startReadRssi();

        characteristicTx = gattService.getCharacteristic(RBLService.UUID_BLE_SHIELD_TX);

        BluetoothGattCharacteristic characteristicRx = gattService.getCharacteristic(RBLService.UUID_BLE_SHIELD_RX);
        mBluetoothLeService.setCharacteristicNotification(characteristicRx, true);
        mBluetoothLeService.readCharacteristic(characteristicRx);
    }

    private boolean flag = true;
    private boolean connState = false;
    private boolean scanFlag = false;

    private void startReadRssi() {
        new Thread() {
            public void run() {

                while (flag) {
                    mBluetoothLeService.readRssi();
                    try {
                        sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            };
        }.start();
    }


    private void setupGattService() {
        Intent gattServiceIntent = new Intent(CalibrateDeviceActivityBLE.this, RBLService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);



    }

    private ProgressDialog mProgressDialog;
    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(R.string.loading));
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setMessage("Reading from sensor! Please wait...");
        }

        mProgressDialog.show();
        timerDelayRemoveDialog(20000, mProgressDialog);
    }

    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.hide();
        }
    }

    private void timerDelayRemoveDialog(long time, final ProgressDialog d){
        new Handler().postDelayed(new Runnable() {
            public void run() {
                if(d.isShowing()) {
                    d.hide();
                    Toast.makeText(getApplicationContext(), "Calibration timed out! Please re-calibrate!", Toast.LENGTH_SHORT).show();
                }
            }
        }, time);
    }

    private void setupCalibrateButton () {
        btnCalibrate = (Button) findViewById(R.id.btn_calibrate);
        btnCalibrate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //dialog = ProgressDialog.show(CalibrateDeviceActivity.this, "", "Reading from sensor! Please wait...", true);
                showProgressDialog();

                String reading = findSensorReadingLocal();
                editTxtContainerHeightInput.requestFocus();
                editTxtContainerHeightInput.setText(reading);

                bottomPanel.setVisibility(View.VISIBLE);

                btnCalibrate.setText("CALIBRATE AGAIN!");
                hideBottomPanel = false;
                hideProgressDialog();
            }
        });


    }

    private void setEditTextContainerHeight() {

        /**
         * Find all the important UI elements in the view
         */

        txtClear = (Button) findViewById(R.id.btn_clear);
        bottomPanel = (View) findViewById(R.id.transparentOverlay);
        txtOK = (Button) findViewById(R.id.btn_ok);

        editTxtContainerHeightInput = (EditText) findViewById(R.id.txt_containerHeightInput);
        editTxtContainerHeightInput.clearFocus();
        editTxtContainerHeightInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //editTxtContainerHeightInput.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(editTxtContainerHeightInput, InputMethodManager.SHOW_IMPLICIT);

                bottomPanel.setVisibility(View.VISIBLE);
            }
        });

        txtOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "Sending request to server..!", Toast.LENGTH_SHORT).show();
                if(sensorReading == null) {
                    sensorReading = editTxtContainerHeightInput.getText().toString();
                }
                sendRequest();
            }
        });
        //clear.getBackground().clearColorFilter();
        txtClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editTxtContainerHeightInput.setText("");
                editTxtContainerHeightInput.clearFocus();
                bottomPanel = (View) findViewById(R.id.transparentOverlay);
                bottomPanel.setVisibility(View.GONE);
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(viewGroup.getWindowToken(), 0);
            }
        });

        bottomPanel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                bottomPanel.setVisibility(View.GONE);
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(viewGroup.getWindowToken(), 0);

            }
        });

        /*editTxtContainerHeightInput.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // TODO Auto-generated method stub

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                // TODO Auto-generated method stub

            }

            @Override
            public void afterTextChanged(Editable s) {
                String input = editTxtContainerHeightInput.getText().toString();
                if(!s.toString().contains(" cm")){

                    editTxtContainerHeightInput.setText(input+" cm");
                    Selection.setSelection(editTxtContainerHeightInput.getText(), editTxtContainerHeightInput.getText().length()-3);

                }

            }
        });*/
    }
    private BluetoothGatt mBluetoothGatt;

    private String findSensorReadingLocal() {

        mBluetoothGatt = mDevice.connectGatt(appContext, false, mGattCallback);
        return "";
    }

    public static final UUID RBL_SERVICE = UUID.fromString("713D0000-503E-4C75-BA94-3148F18D941E");

    public static final UUID RBL_DEVICE_RX_UUID = UUID.fromString("713D0002-503E-4C75-BA94-3148F18D941E");

    public static final UUID RBL_DEVICE_TX_UUID = UUID.fromString("713D0003-503E-4C75-BA94-3148F18D941E");

    private BluetoothGattCharacteristic txCharc = null;
    private BluetoothGattCharacteristic rxCharc = null;

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.i("onConnectionStateChange", "Status: " + status);
            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:
                    Log.i(TAG, "Connected to GATT server.");
                    Log.i(TAG, "Attempting to start service discovery:"
                            + mBluetoothGatt.discoverServices());
                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    Log.e("mGattCallback", "STATE_DISCONNECTED");
                    break;
                default:
                    Log.e("mGattCallback", "STATE_OTHER");
            }

        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            List<BluetoothGattService> services = gatt.getServices();
            Log.i("onServicesDiscovered", services.toString());


            BluetoothGattService rblService = gatt.getService(RBL_SERVICE);

            if (rblService == null) {
                Log.e(TAG, "RBL service not found!");
                return;
            }

            List<BluetoothGattCharacteristic> Characteristic = rblService
                    .getCharacteristics();

            for (BluetoothGattCharacteristic a : Characteristic) {
                Log.e(TAG, " a =  uuid : " + a.getUuid() + "");
            }

            rxCharc = rblService.getCharacteristic(RBL_DEVICE_RX_UUID);
            if (rxCharc == null) {
                Log.e(TAG, "RBL RX Characteristic not found!");
                return;
            }

            /*txCharc = rblService.getCharacteristic(RBL_DEVICE_TX_UUID);
            if (txCharc == null) {
                Log.e(TAG, "RBL TX Characteristic not found!");
                return;
            }*/

            //gatt.readCharacteristic(services.get(0).getCharacteristics().get(0));
            //gatt.readCharacteristic(services.get(0).getCharacteristics().get(1));
            //gatt.readCharacteristic(services.get(0).getCharacteristics().get(2));
            //gatt.readCharacteristic(services.get(1).getCharacteristics().get(0));
            //gatt.readCharacteristic(services.get(2).getCharacteristics().get(0));
            //gatt.readCharacteristic(services.get(2).getCharacteristics().get(1));
            //gatt.readCharacteristic(services.get(3).getCharacteristics().get(0));
            gatt.readCharacteristic(rxCharc);
            //gatt.readCharacteristic(txCharc);

            gatt.setCharacteristicNotification(rxCharc, true);
            //gatt.setCharacteristicNotification(txCharc, true);

        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {

            byte[] value=characteristic.getValue();
            //byte[] value2 = rxCharc.getValue();
            String v = new String(value, StandardCharsets.US_ASCII);
            Log.i("onCharRead --> ", v);
            //String v2 = new String(value2, StandardCharsets.US_ASCII);
            //Log.i("v2 rxCharc --> ", v2);
            //String test1Value = readAnalogInValue(value);
            //String test2Value = bytesToHex(value);
            String test3Value = testReadValue(value);

            //String test1Value2 = readAnalogInValue(value2);
            //String test2Value2 = bytesToHex(value2);
            //String test3Value2 = testReadValue(value2);



            gatt.disconnect();

        }
        final private char[] hexArray = { '0', '1', '2', '3', '4', '5', '6',
                '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

        private String bytesToHex(byte[] bytes) {
            char[] hexChars = new char[bytes.length * 2];
            int v;
            for (int j = 0; j < bytes.length; j++) {
                v = bytes[j] & 0xFF;
                hexChars[j * 2] = hexArray[v >>> 4];
                hexChars[j * 2 + 1] = hexArray[v & 0x0F];
            }
            return new String(hexChars);
        }
        private String readAnalogInValue(byte[] data) {
            String str = "";
            for (int i = 0; i < data.length; i += 3) {
                if (data[i] == 0x0A) {

                } else if (data[i] == 0x0B) {
                    int value;

                    value = ((data[i + 1] << 8) & 0x0000ff00)
                            | (data[i + 2] & 0x000000ff);

                    str += String.valueOf(value);
                }
            }
            return str;
        }

        private String testReadValue(byte[] data) {
            if (data != null && data.length > 0) {
                final StringBuilder stringBuilder = new StringBuilder(data.length);
                for (byte byteChar : data)
                    stringBuilder.append(String.format("%02X ", byteChar));
                return stringBuilder.toString();
            }
            return "";
        }
    };

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.calibrate_device, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_dashboard) {
            Intent intent = new Intent(viewGroup.getContext(), MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //intent.putExtra(PrefManager.SENSOR_CACHE, cache);
            viewGroup.getContext().startActivity(intent);
            // Handle the camera action
        } else if (id == R.id.nav_account) {
            Intent intent = new Intent(viewGroup.getContext(), AccountActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //intent.putExtra(PrefManager.SENSOR_CACHE, cache);
            viewGroup.getContext().startActivity(intent);

        } else if (id == R.id.nav_notifications) {

        } else if (id == R.id.nav_settings) {
            Intent intent = new Intent(viewGroup.getContext(), ComingSoonActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            viewGroup.getContext().startActivity(intent);

        } else if (id == R.id.nav_customer_support) {
            Intent intent = new Intent(viewGroup.getContext(), ComingSoonActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            viewGroup.getContext().startActivity(intent);

        } else if (id == R.id.nav_about) {
            Intent intent = new Intent(viewGroup.getContext(), ComingSoonActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            viewGroup.getContext().startActivity(intent);

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void setupNavdrawer() {
        View hView =  this.navigationView.getHeaderView(0);
        navigationView.setItemIconTintList(null);

        //Set user name
        TextView nav_user = (TextView)hView.findViewById(R.id.nav_user_name);
        nav_user.setText(PrefManager.getFromPrefs(getApplicationContext(), PrefManager.NAME_KEY, ""));

        //Set user email
        TextView nav_email = (TextView)hView.findViewById(R.id.nav_user_email);
        nav_email.setText(PrefManager.getFromPrefs(getApplicationContext(), PrefManager.EMAIL_KEY, ""));

        //Set user image
        CircleImageView nav_image = (CircleImageView)hView.findViewById(R.id.nav_user_image);
        nav_image.setImageBitmap(PrefManager.getBitmapFromMemCache(PrefManager.BITMAP_PHOTO_KEY));

        navigationView.setNavigationItemSelectedListener(this);
    }

    final Handler handler = new Handler();
    private String deviceName;

    private void sendRequest() {
        StringRequest strReq = new StringRequest(Request.Method.POST,
                Config.URL_CREATE_SENSOR, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, response.toString());

                try {
                    JSONObject responseObj = new JSONObject(response);

                    // Parsing json object response
                    // response will be a json object
                    boolean error = responseObj.getBoolean("error");
                    String message = responseObj.getString("message");
                    int errorCode = responseObj.getInt("code");

                    // checking for error, if not error SMS is initiated
                    // device should receive it shortly
                    if (errorCode == 0) {
                        Toast.makeText(appContext, "Sensor added successfully. Redirecting!", Toast.LENGTH_SHORT).show();

                        /**
                         * Redirect to the successful screen.
                         */
                        Intent intent = new Intent(appContext, DeviceAddedActivity.class);
                        //intent.putExtra(PrefManager.SENSOR_CACHE, cache);
                        intent.putExtra(PrefManager.SENSORCREATE_MAC, sensorMac);
                        intent.putExtra(PrefManager.SENSORCREATE_ITEM, itemName);
                        intent.putExtra(PrefManager.SENSORCREATE_MAXDISTANCE, sensorReading);
                        intent.putExtra(PrefManager.SENSORCREATE_DEVICENAME, deviceName);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        appContext.startActivity(intent);
                    } else if (errorCode == 1) {
                        Toast.makeText(appContext, "Sensor="+sensorMac+" already existed. Doing nothing!", Toast.LENGTH_LONG).show();

                        Intent intent = new Intent(appContext, MainActivity.class);
                        //intent.putExtra(PrefManager.SENSOR_CACHE, cache);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        appContext.startActivity(intent);

                    } else if (errorCode == 3) {
                        Toast.makeText(appContext, "Inconsistent Database. Please check the error=" + message, Toast.LENGTH_LONG).show();
                        //mInputMobile.setVisibility(View.VISIBLE);
                        //btnMobileNext.setVisibility(View.VISIBLE);
                        Intent intent = new Intent(viewGroup.getContext(), MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        //intent.putExtra(PrefManager.SENSOR_CACHE, cache);
                        viewGroup.getContext().startActivity(intent);
                    } else if (errorCode == 4){
                        Toast.makeText(appContext, "Sensor="+sensorMac+" already existed and associated with a different email address!" + message, Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(viewGroup.getContext(), MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        //intent.putExtra(PrefManager.SENSOR_CACHE, cache);
                        viewGroup.getContext().startActivity(intent);
                    }
                } catch (JSONException e) {
                    Toast.makeText(appContext,
                            "Error: " + response.toString(),
                            Toast.LENGTH_LONG).show();
                } finally {
                    hideProgressDialog();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Error: " + error.getMessage());
                Toast.makeText(appContext, error.getMessage(), Toast.LENGTH_SHORT).show();
                hideProgressDialog();
            }
        }) {

            /**
             * Passing user parameters to our server
             * @return
             */
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("email", PrefManager.getFromPrefs(appContext, PrefManager.EMAIL_KEY, ""));
                params.put("item", itemName);
                if(deviceName == null) {
                    deviceName = "HC-05";
                }
                params.put("sensorname", deviceName);
                params.put("mac", sensorMac);

                /**
                 * Processing the calibration reading
                 */
                String[] parts = null;
                if(sensorReading.contains("cm")) {
                    parts = sensorReading.split("\\s*cm\\s*");
                }
                String reading = "";
                if(parts != null) {
                    for (String str : parts)
                        reading += str.trim();
                } else {
                    reading = sensorReading;
                }
                params.put("maxdistance", reading);
                Log.e(TAG, "Posting params: " + params.toString());
                return params;
            }

        };

        // Adding request to request queue
        MyApplication.getInstance().addToRequestQueue(strReq);
    }

    class BluetoothCommunicatorLocal {
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

        public BluetoothCommunicatorLocal(String macAddress, BluetoothAdapter adapter) {
            this.macAddress = macAddress;
            this.bluetoothAdapter = adapter;
        }

        public void getSensorReading() {
            stopWorker = false;

            workerThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Log.i(TAG, "Inside getSensorReading");
                        findBT();
                        Log.i(TAG, "Inside getSensorReading: returned after findBT");
                        if(mmDevice == null) {
                            return;
                        }
                        Log.i(TAG, "Inside getSensorReading: trying openBT");
                        openBT();
                        beginListenForDataThread();
                        Log.i(TAG, "Inside getSensorReading: after open BT and read successful");
                    } catch (IOException e) {
                        return;
                    }
                }
            });
            workerThread.start();
        }


        public String getSensorReading2() {
            stopWorker = false;

            try {
                Log.i(TAG, "Inside getSensorReading");
                findBT();
                Log.i(TAG, "Inside getSensorReading: returned after findBT");
                if(mmDevice == null) {
                    return null;
                }
                Log.i(TAG, "Inside getSensorReading: trying openBT");
                openBT();
                beginListenForDataThread2();
                Log.i(TAG, "Inside getSensorReading: after open BT and read successful");
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

            Log.i(TAG, "Inside openBT");
            try {
                Log.i(TAG, "Inside openBT: try block");
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



        }

        void beginListenForDataThread2()
        {
            final byte delimiter = 10; //This is the ASCII code for a newline character

            Log.i(TAG, "Inside beginListenForDataThread");

            stopWorker = false;
            readBufferPosition = 0;
            readBuffer = new byte[1024];
            workerThread = new Thread(new Runnable()
            {
                public void run()
                {
                    Log.i(TAG, "Inside beginListenForDataThread: Runnable");
                    try
                    {
                        while(!Thread.currentThread().isInterrupted() && !stopWorker)
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

                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Log.i(TAG, "Successfully retrieved "+reading+" Updating UI elements now!");
                                                editTxtContainerHeightInput.requestFocus();
                                                editTxtContainerHeightInput.setText(reading);

                                                bottomPanel.setVisibility(View.VISIBLE);

                                                btnCalibrate.setText("CALIBRATE AGAIN!");
                                                hideBottomPanel = false;
                                                hideProgressDialog();
                                                stopWorker = true;
                                                resetConnection();
                                            }
                                        });

                                        break;

                                    }
                                    else
                                    {
                                        readBuffer[readBufferPosition++] = b;
                                    }
                                }
                            }
                        }

                    }
                    catch (IOException ex)
                    {
                        Log.i(TAG, "Encountered IO exception. "+ex.getMessage()+"\nClosing the thread and resetting connection");
                        stopWorker = true;
                        resetConnection();
                    } finally {
                        Log.i(TAG, "Inside beginListenForDataThread: Finally block");
                        stopWorker = true;
                        resetConnection();
                    }

                }
            });

            workerThread.start();
            Log.i(TAG, "Inside beginListenForDataThread: Thread started");
        }

        void beginListenForDataThread()
        {
            final byte delimiter = 10; //This is the ASCII code for a newline character

            Log.i(TAG, "Inside beginListenForDataThread");

            stopWorker = false;
            readBufferPosition = 0;
            readBuffer = new byte[1024];
            Log.i(TAG, "Inside beginListenForDataThread: Runnable");
            try
            {
                while(!Thread.currentThread().isInterrupted() && !stopWorker)
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

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Log.i(TAG, "Successfully retrieved "+reading+" Updating UI elements now!");
                                        editTxtContainerHeightInput.requestFocus();
                                        editTxtContainerHeightInput.setText(reading);

                                        bottomPanel.setVisibility(View.VISIBLE);

                                        btnCalibrate.setText("CALIBRATE AGAIN!");
                                        hideBottomPanel = false;
                                        hideProgressDialog();
                                        stopWorker = true;
                                        resetConnection();
                                    }
                                });

                                break;

                            }
                            else
                            {
                                readBuffer[readBufferPosition++] = b;
                            }
                        }
                    }
                }

            }
            catch (IOException ex)
            {
                Log.i(TAG, "Encountered IO exception. "+ex.getMessage()+"\nClosing the thread and resetting connection");
                stopWorker = true;
                resetConnection();
            } finally {
                Log.i(TAG, "Inside beginListenForDataThread: Finally block");
                stopWorker = true;
                resetConnection();
            }
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
}
