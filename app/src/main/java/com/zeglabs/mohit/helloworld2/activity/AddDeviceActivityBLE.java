package com.zeglabs.mohit.helloworld2.activity;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothSocket;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.zeglabs.mohit.helloworld2.Manifest;
import com.zeglabs.mohit.helloworld2.R;
import com.zeglabs.mohit.helloworld2.adapter.PopularSearchAdapter;
import com.zeglabs.mohit.helloworld2.adapter.QuickSearchAdapter;
import com.zeglabs.mohit.helloworld2.decorators.DividerItemDecoration;
import com.zeglabs.mohit.helloworld2.helper.ComplexPreferences;
import com.zeglabs.mohit.helloworld2.helper.PrefManager;
import com.zeglabs.mohit.helloworld2.model.SensorDetailsMap;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;
import item.PopularSearchItem;
import item.QuickSearchItem;

public class AddDeviceActivityBLE extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private String TAG = AddDeviceActivityBLE.class.getSimpleName();
    NavigationView navigationView;
    private Context appContext;
    private ViewGroup viewGroup;
    BluetoothAdapter mBluetoothAdapter;

    private SensorDetailsMap sensorCache;
    ComplexPreferences complexPreferences;

    ProgressDialog dialog;

    AlertDialog.Builder builder;

    //private SensorDetailsMap cache;

    /*
    For the nested class
     */
    public ArrayList<BluetoothDevice> mDeviceList = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_device);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        appContext = getApplicationContext();
        viewGroup = (ViewGroup) ((ViewGroup) this.findViewById(android.R.id.content));

        dialog = ProgressDialog.show(AddDeviceActivityBLE.this, "", "Searching for ZegPods in the vicinity. Please wait...", true);

        //cache = getIntent().getParcelableExtra(PrefManager.SENSOR_CACHE);
        complexPreferences = ComplexPreferences.getComplexPreferences(getApplicationContext(), "", 0);
        sensorCache = complexPreferences.getObject(PrefManager.SENSOR_CACHE, SensorDetailsMap.class);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        /*ContextThemeWrapper themedContext;
        if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ) {
            themedContext = new ContextThemeWrapper( AddDeviceActivityBLE.this, android.R.style.Theme_Light_NoTitleBar );
        }
        else {
            themedContext = new ContextThemeWrapper( AddDeviceActivityBLE.this, android.R.style.Theme_Light_NoTitleBar );
        }
        builder = new AlertDialog.Builder(themedContext);*/


        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        setupNavdrawer();
        setupAutoComplete();
        setupQuickSearch();
        setupPopularSearch();
        pairSensorProgrammatically();
    }



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
        getMenuInflater().inflate(R.menu.add_device, menu);
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


    String[] inventory = { "Rice", "Wheat", "Pulses", "Tea", "Coffee", "Sugar", "Salt", "Spices"};

    public void setupAutoComplete() {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.myautocompleteview, inventory);
        AutoCompleteTextView actv = (AutoCompleteTextView) findViewById(R.id.search_autoCompleteTextView);
        actv.setThreshold(1);//will start working from first character
        actv.setAdapter(adapter);//setting the adapter data into the AutoCompleteTextView
        actv.setTextColor(ContextCompat.getColor(appContext, R.color.colorPrimary));

        TextView clear = (TextView) findViewById(R.id.txt_clear);
        //clear.getBackground().clearColorFilter();
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AutoCompleteTextView actv = (AutoCompleteTextView) findViewById(R.id.search_autoCompleteTextView);
                actv.setText("");
            }
        });
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
    public void setupQuickSearch() {
        final String[] grains = {"Rice", "Wheat", "Pulses"};
        Bitmap[] icons = {
                BitmapFactory.decodeResource(getResources(), R.drawable.ic_rice_search),
                BitmapFactory.decodeResource(getResources(), R.drawable.ic_wheat_search),
                BitmapFactory.decodeResource(getResources(), R.drawable.ic_pulses_search),
        };
        RecyclerView recyclerView = (RecyclerView)findViewById(R.id.search_quick_gridView);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(appContext));

        initQuickSearchView(recyclerView);
        List<QuickSearchItem> quickSearchItemList = prepareQuickSearchData(grains, icons);
        QuickSearchAdapter adapter = new QuickSearchAdapter(getApplicationContext(), quickSearchItemList);
        recyclerView.setAdapter(adapter);
    }
    private void initQuickSearchView(RecyclerView view) {
        view.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getApplicationContext(),3);
        view.setLayoutManager(layoutManager);
    }
    private List<QuickSearchItem> prepareQuickSearchData(String[] grains, Bitmap[] icons) {
        List<QuickSearchItem> list = new ArrayList<>();
        for(int i=0; i<grains.length; i++) {
            QuickSearchItem item = new QuickSearchItem(grains[i], icons[i]);
            list.add(item);
        }
        return list;
    }
    public void setupPopularSearch() {
        final String[] grains = {"Coffee", "Sugar", "Salt", "Tea", "Spices"};
        Bitmap[] icons = {
                BitmapFactory.decodeResource(getResources(), R.drawable.ic_coffee),
                BitmapFactory.decodeResource(getResources(), R.drawable.ic_sugar),
                BitmapFactory.decodeResource(getResources(), R.drawable.ic_salt),
                BitmapFactory.decodeResource(getResources(), R.drawable.ic_tea),
                BitmapFactory.decodeResource(getResources(), R.drawable.ic_spicies),
        };
        RecyclerView recyclerView = (RecyclerView)findViewById(R.id.search_popular_listView);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(appContext));

        initPopularSearchView(recyclerView);
        List<PopularSearchItem> quickSearchItemList = preparePopularSearchData(grains, icons);
        PopularSearchAdapter adapter = new PopularSearchAdapter(getApplicationContext(), quickSearchItemList);
        recyclerView.setAdapter(adapter);
    }
    private void initPopularSearchView(RecyclerView view) {
        view.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(AddDeviceActivityBLE.this);
        view.setLayoutManager(layoutManager);
    }
    private List<PopularSearchItem> preparePopularSearchData(String[] grains, Bitmap[] icons) {
        List<PopularSearchItem> list = new ArrayList<>();
        for(int i=0; i<grains.length; i++) {
            PopularSearchItem item = new PopularSearchItem(grains[i], icons[i]);
            list.add(item);
        }
        return list;
    }

    public boolean pairSensorProgrammatically() {
        IntentFilter filter = new IntentFilter();

        /*
        filter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);*/

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            showDeviceNotFoundAlertDialogMainActivity("Non-BLE Device", "A new ZegPod cannot be added as the device does not support Bluetooth Low Energy! Redirecting you to Home Screen!.", "OK");
        }

        scanForBLEDevices();

        return true;
    }

    private boolean isDeviceExistsInCache(String mac) {
        return !(this.sensorCache.getSensorCache().get(mac) == null);
    }


    public void showDeviceNotFoundAlertDialogMainActivity(String title, String message, String btnText) {

        AlertDialog.Builder builder = new AlertDialog.Builder(AddDeviceActivityBLE.this);
        LayoutInflater inflater = AddDeviceActivityBLE.this.getLayoutInflater();
        final View dialogLayout = inflater.inflate(R.layout.dialog_general_message, null);
        builder.setView(dialogLayout);

        TextView txtTitle = (TextView) dialogLayout.findViewById(R.id.txt_generalTitle);
        TextView txtMessage = (TextView) dialogLayout.findViewById(R.id.txt_generalMessage);
        Button btnLayout = (Button) dialogLayout.findViewById(R.id.btn_generalButton);

        txtTitle.setText(title);
        txtMessage.setText(message);
        btnLayout.setText(btnText);

        final AlertDialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(false);

        btnLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                alertDialog.hide();
                Intent intent1 = new Intent(viewGroup.getContext(), MainActivity.class);
                //intent1.putExtra(PrefManager.SENSOR_CACHE, cache);
                intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                viewGroup.getContext().startActivity(intent1);
            }
        });

        Button btnX = (Button) dialogLayout.findViewById(R.id.btn_closeX);
        btnX.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.hide();
                Intent intent1 = new Intent(viewGroup.getContext(), MainActivity.class);
                //intent1.putExtra(PrefManager.SENSOR_CACHE, cache);
                intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                viewGroup.getContext().startActivity(intent1);
            }
        });

        alertDialog.show();

    }

    HashMap<String, BluetoothDevice> mDevices = null;
    private static final long SCAN_PERIOD = 10000;
    private BluetoothLeScanner mLEScanner;
    private ScanSettings settings;
    private List<ScanFilter> filters;
    private BluetoothGatt mGatt;
    private Handler mHandler;
    public BluetoothGatt mBluetoothGatt = null;


    public void scanForBLEDevices() {
        if (mDevices == null) {
            mDevices = new HashMap<String, BluetoothDevice>();
        }
        if(mBluetoothAdapter == null) {
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        }
        mHandler = new Handler();

        mLEScanner = mBluetoothAdapter.getBluetoothLeScanner();
        settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build();
        filters = new ArrayList<ScanFilter>();
        scanLeDevice(true);
    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (Build.VERSION.SDK_INT < 21) {
                        mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    } else {
                        mLEScanner.stopScan(mScanCallback);

                    }
                    Log.d(TAG, "No BLE Device found!");

                    if(mDevices.isEmpty()) {
                        //Add code to redirect to the Main Activity!
                        showDeviceNotFoundAlertDialogMainActivity("No BLE device found!",
                                "Could not find a sensor in the vicinity. Perhaps sensor bluetooth is not on OR Sensor is more than 30 feet away! OR Sensor has already been added!", "OK");
                    }
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    dialog.dismiss();
                }
            }, SCAN_PERIOD);
            if (Build.VERSION.SDK_INT < 21) {
                mBluetoothAdapter.startLeScan(mLeScanCallback);
            } else {
                //mLEScanner.startScan(filters, settings, mScanCallback);
                mLEScanner.startScan(mScanCallback);
            }
        } else {
            if (Build.VERSION.SDK_INT < 21) {
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
            } else {
                mLEScanner.stopScan(mScanCallback);
            }
        }
    }
    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            Log.d(TAG, "Scan Result: "+String.valueOf(callbackType));
            Log.d(TAG, "Scan Result: "+result.toString());

            BluetoothDevice btDevice = result.getDevice();

            if(btDevice.getName().contains("BlendMicro") && !isDeviceExistsInCache(btDevice.getAddress())) {

                mDevices.put(btDevice.getAddress(), btDevice);
                connectToDevice(btDevice);
                dialog.dismiss();
                Toast.makeText(appContext, "ZegPod with mac=" + btDevice.getAddress() + " found! " +
                        "Discovery finished. Please select an item to proceed!", Toast.LENGTH_LONG).show();

                PrefManager.saveToPrefs(viewGroup.getContext(), PrefManager.SENSOR_FOUND_MAC, btDevice.getAddress());
                PrefManager.saveToPrefs(viewGroup.getContext(), PrefManager.SENSORCREATE_DEVICENAME, btDevice.getName());
                mBluetoothAdapter.stopLeScan(mLeScanCallback);

            }
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for (ScanResult sr : results) {
                Log.d(TAG, sr.toString());
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.d(TAG, "Error Code: " + errorCode);
            dialog.dismiss();
        }
    };

    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi,
                                     byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.i("---- onLeScan", device.toString());
                            dialog.dismiss();
                            Toast.makeText(appContext, "Yo yo yo, mac="+device.getAddress(), Toast.LENGTH_LONG).show();
                            mDevices.put(device.getAddress(), device);
                            connectToDevice(device);
                        }
                    });
                }
            };
    public static BluetoothDevice mDevice;
    public void connectToDevice(BluetoothDevice device) {
        if (mGatt == null) {
            //mGatt = device.connectGatt(this, false, gattCallback);
            //scanLeDevice(false);// will stop after first device detection
            mDevice = device;
        }
    }

    public static final UUID RBL_SERVICE = UUID.fromString("713D0000-503E-4C75-BA94-3148F18D941E");

    public static final UUID RBL_DEVICE_RX_UUID = UUID.fromString("713D0002-503E-4C75-BA94-3148F18D941E");

    public static final UUID RBL_DEVICE_TX_UUID = UUID.fromString("713D0003-503E-4C75-BA94-3148F18D941E");

    private BluetoothGattCharacteristic txCharc = null;
    private BluetoothGattCharacteristic rxCharc = null;

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.i("onConnectionStateChange", "Status: " + status);
            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:
                    Log.i("gattCallback", "STATE_CONNECTED");
                    gatt.discoverServices();
                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    Log.e("gattCallback", "STATE_DISCONNECTED");
                    break;
                default:
                    Log.e("gattCallback", "STATE_OTHER");
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

            txCharc = rblService.getCharacteristic(RBL_DEVICE_TX_UUID);
            if (txCharc == null) {
                Log.e(TAG, "RBL RX Characteristic not found!");
                return;
            }

            //gatt.readCharacteristic(services.get(0).getCharacteristics().get(0));
            //gatt.readCharacteristic(services.get(0).getCharacteristics().get(1));
            gatt.readCharacteristic(services.get(0).getCharacteristics().get(2));
            gatt.readCharacteristic(services.get(1).getCharacteristics().get(0));
            gatt.readCharacteristic(services.get(2).getCharacteristics().get(0));
            gatt.readCharacteristic(services.get(2).getCharacteristics().get(1));
            gatt.readCharacteristic(services.get(3).getCharacteristics().get(0));
            gatt.readCharacteristic(rxCharc);
            gatt.readCharacteristic(txCharc);

            gatt.setCharacteristicNotification(rxCharc, true);
            gatt.setCharacteristicNotification(txCharc, true);

        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {

            byte[] value=characteristic.getValue();
            String v = new String(value, StandardCharsets.UTF_8);
            Log.i("onCharRead --> ", v);
            gatt.disconnect();

        }
    };

}