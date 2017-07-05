package com.zeglabs.mohit.helloworld2.activity;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.zeglabs.mohit.helloworld2.R;
import com.zeglabs.mohit.helloworld2.adapter.PopularSearchAdapter;
import com.zeglabs.mohit.helloworld2.adapter.QuickSearchAdapter;
import com.zeglabs.mohit.helloworld2.decorators.DividerItemDecoration;
import com.zeglabs.mohit.helloworld2.helper.ComplexPreferences;
import com.zeglabs.mohit.helloworld2.helper.PrefManager;
import com.zeglabs.mohit.helloworld2.model.SensorDetailsMap;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;
import item.PopularSearchItem;
import item.QuickSearchItem;

public class AddDeviceActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    NavigationView navigationView;
    private Context appContext;
    private ViewGroup viewGroup;
    BluetoothAdapter mBluetoothAdapter;

    private SensorDetailsMap sensorCache;
    ComplexPreferences complexPreferences;

    ProgressDialog dialog;
    PairingRequest receiver = new PairingRequest();

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

        dialog = ProgressDialog.show(AddDeviceActivity.this, "", "Searching for ZegPods in the vicinity. Please wait...", true);

        //cache = getIntent().getParcelableExtra(PrefManager.SENSOR_CACHE);
        complexPreferences = ComplexPreferences.getComplexPreferences(getApplicationContext(), "", 0);
        sensorCache = complexPreferences.getObject(PrefManager.SENSOR_CACHE, SensorDetailsMap.class);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        ContextThemeWrapper themedContext;
        if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ) {
            themedContext = new ContextThemeWrapper( AddDeviceActivity.this, android.R.style.Theme_Light_NoTitleBar );
        }
        else {
            themedContext = new ContextThemeWrapper( AddDeviceActivity.this, android.R.style.Theme_Light_NoTitleBar );
        }
        builder = new AlertDialog.Builder(themedContext);

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
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(AddDeviceActivity.this);
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


        filter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);


        AddDeviceActivity.this.registerReceiver(receiver, filter);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBluetoothAdapter.startDiscovery();

        return true;
    }
    private boolean findBT() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothSocket mmSocket;
        BluetoothDevice mmDevice;

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if(pairedDevices.size() > 0)
        {
            for(BluetoothDevice device : pairedDevices)
            {
                if(device.getName().contains("HC-05"))
                {
                    mmDevice = device;
                }
            }

            return false;
        }
        return false;
    }

    private boolean isDeviceExistsInCache(String mac) {
        return !(this.sensorCache.getSensorCache().get(mac) == null);
    }

    public class PairingRequest extends BroadcastReceiver {

        public PairingRequest() {
            super();
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (intent.getAction().equals(BluetoothDevice.ACTION_PAIRING_REQUEST)) {
                try {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    int pin= intent.getIntExtra("android.bluetooth.device.extra.PAIRING_KEY", 1234);
                    //the pin in case you need to accept for an specific pin
                    Log.d("PIN", " " + intent.getIntExtra("android.com.zeglabs.mohit.helloworld2.bluetooth.device.extra.PAIRING_KEY",1234));
                    //maybe you look for a name or address
                    Log.d("Bonded", device.getName());
                    byte[] pinBytes;
                    pinBytes = (""+pin).getBytes("UTF-8");
                    device.setPin(pinBytes);
                    //setPairing confirmation if neeeded
                    device.setPairingConfirmation(true);
                    //device.getClass().getMethod("cancelPairingUserInput", boolean.class).invoke(device);
                    dialog.dismiss();
                    unregisterReceiver(receiver);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if(intent.getAction().equals(BluetoothAdapter.ACTION_DISCOVERY_STARTED)) {
                if(mDeviceList == null)
                    mDeviceList = new ArrayList<BluetoothDevice>();
            } else if(intent.getAction().equals(BluetoothDevice.ACTION_FOUND)) {
                BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if(device.getName().contains("HC-05") && !isDeviceExistsInCache(device.getAddress())) {
                    mDeviceList.add(device);
                    try {
                        int pin = intent.getIntExtra("android.com.zeglabs.mohit.helloworld2.bluetooth.device.extra.PAIRING_KEY", 0);
                        pin = 1234;
                        //the pin in case you need to accept for an specific pin
                        Log.d("PIN", " " + intent.getIntExtra("android.com.zeglabs.mohit.helloworld2.bluetooth.device.extra.PAIRING_KEY", 0));
                        //maybe you look for a name or address
                        Log.d("Bonded", device.getName());
                        byte[] pinBytes;
                        pinBytes = ("" + pin).getBytes("UTF-8");
                        device.setPin(pinBytes);

                        //setPairing confirmation if neeeded
                        boolean createBond = device.createBond();
                        if(device.getBondState() == BluetoothDevice.BOND_BONDED) {
                        /*
                        TODO: Device already bonded. Show appropriate status message.
                         */
                            //Snackbar.make(viewGroup, "Device  "+device.getDeviceName()+" already bonded", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                            Toast.makeText(appContext, "Already bonded with "+device.getName(), Toast.LENGTH_LONG).show();

                        }
                        if(createBond){
                            Toast.makeText(appContext, "Successfully paired with "+device.getName(), Toast.LENGTH_LONG).show();
                        }

                    }catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                } else if(device.getName().contains("HC-05") && isDeviceExistsInCache(device.getAddress())) {
                    Toast.makeText(appContext, "Device with mac="+device.getAddress()+" already exists! Doing nothing!", Toast.LENGTH_LONG).show();
                }

            } else if (intent.getAction().equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {

                if(mDeviceList == null || mDeviceList.isEmpty()) {
                    dialog.dismiss();
                    /**
                     * TODO: Add code here to show a custom dialog, which has an OK button to redirect to home screen
                     */
                    showDeviceNotFoundAlertDialogMainActivity("Sensor not found!",
                            "Could not find a sensor in the vicinity. Perhaps sensor bluetooth is not on OR Sensor is more than 30 feet away! OR Sensor has already been added!", "OK");
                } else {
                    Toast.makeText(appContext, "Discovery finished. Please select an item to proceed!", Toast.LENGTH_LONG).show();
                    PrefManager.saveToPrefs(viewGroup.getContext(), PrefManager.SENSOR_FOUND_MAC, mDeviceList.get(0).getAddress());
                    PrefManager.saveToPrefs(viewGroup.getContext(), PrefManager.SENSORCREATE_DEVICENAME, mDeviceList.get(0).getName());

                    dialog.dismiss();
                }


            }


        }
        private void showDeviceNotFoundAlertDialogMainActivity(String title, String message, String btnText) {

            AlertDialog.Builder builder = new AlertDialog.Builder(AddDeviceActivity.this);
            LayoutInflater inflater = AddDeviceActivity.this.getLayoutInflater();
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
    }
}