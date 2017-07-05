package com.zeglabs.mohit.helloworld2.activity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
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
import android.widget.TextView;
import android.widget.Toast;

import com.github.javiersantos.bottomdialogs.BottomDialog;
import com.zeglabs.mohit.helloworld2.R;
import com.zeglabs.mohit.helloworld2.helper.ComplexPreferences;
import com.zeglabs.mohit.helloworld2.helper.Config;
import com.zeglabs.mohit.helloworld2.helper.DownloadImageFromInternet;
import com.zeglabs.mohit.helloworld2.helper.PrefManager;
import com.zeglabs.mohit.helloworld2.model.SensorDetails;
import com.zeglabs.mohit.helloworld2.model.SensorDetailsMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private Activity act;

    NavigationView navigationView = null;
    private ViewGroup viewGroup;
    Toolbar toolbar = null;

    private BluetoothAdapter bluetoothAdapter;

    private SensorDetailsMap sensorCache;
    ComplexPreferences complexPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*CircleImageView image = (CircleImageView) findViewById(R.id.profile_image);
        image.setImageBitmap(PrefManager.getBitmapFromMemCache(PrefManager.BITMAP_PHOTO_KEY));*/

        setupClassDefaults();
        //viewGroup = (ViewGroup) (this.findViewById(android.R.id.content));

        this.toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*
                TODO: Call the AddDeviceActivity.java with an intent
                 */
                //Intent intent = new Intent(view.getContext(), AddDeviceActivity.class);
                Intent intent = new Intent(view.getContext(), AddDeviceActivityBLE.class);
                intent.putExtra(PrefManager.SENSOR_CACHE, sensorCache);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                view.getContext().startActivity(intent);
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        this.setupNavdrawer();
        if(!checkPermission())
            requestPermission();

        //sensorCache = getIntent().getParcelableExtra(PrefManager.SENSOR_CACHE);

        //set the fragment initially
        //complexPreferences = ComplexPreferences.getComplexPreferences(getApplicationContext(), "", 0);
        //sensorCache = complexPreferences.getObject(PrefManager.SENSOR_CACHE, SensorDetailsMap.class);

        DashboardFragment fDashboard = new DashboardFragment();
        /*Bundle bundle = new Bundle();
        bundle.putParcelable(PrefManager.SENSOR_CACHE, sensorCache);

        fDashboard.setArguments(bundle);*/

        android.support.v4.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fDashboard);
        fragmentTransaction.commit();

        /*BottomBar bottomBar = (BottomBar) findViewById(R.id.bottomBar);
        final ViewGroup viewGroup = (ViewGroup) this
                .findViewById(android.R.id.content);
        bottomBar.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelected(@IdRes int tabId) {
                if (tabId == R.id.tab_dashboard) {
                    Snackbar.make(viewGroup, "Dashboard requested", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        });*/
        setupBottomDrawer();
    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(), ACCESS_COARSE_LOCATION);

        return result == PackageManager.PERMISSION_GRANTED;
    }

    private static final int PERMISSION_REQUEST_CODE = 200;
    private void requestPermission() {

        ActivityCompat.requestPermissions(this, new String[]{ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_CODE);
    }

    private void setupClassDefaults() {
        act = MainActivity.this;

        viewGroup = (ViewGroup) (this.findViewById(android.R.id.content));

        complexPreferences = ComplexPreferences.getComplexPreferences(getApplicationContext(), "", 0);
        sensorCache = complexPreferences.getObject(PrefManager.SENSOR_CACHE, SensorDetailsMap.class);
    }

    public List<String> getCriticalItemsFromSensorCache() {
        Map<String, SensorDetails> cache = this.sensorCache.getSensorCache();
        List<String> items = new ArrayList<String>();

        for(Map.Entry<String, SensorDetails> entry : cache.entrySet()) {
            int reading = entry.getValue().getCurrentReading();
            int max = entry.getValue().getMaxDistance();

            double readingPercentage = (reading*1.0)/max;

            if(readingPercentage < Config.CONST_CRITICAL_STOCK_LEVEL) {
                String itemName = Character.toTitleCase(entry.getValue().getItemName().charAt(0)) + entry.getValue().getItemName().substring(1);
                items.add(itemName);
            }
        }

        return items;
    }

    public void setupBottomDrawer() {
        if( !PrefManager.getFromPrefs(getApplicationContext(), PrefManager.APP_FIRST_RUN, true) ) {
            return;
        }
        PrefManager.saveToPrefs(getApplicationContext(), PrefManager.APP_FIRST_RUN, false);

        List<String> criticalItems = getCriticalItemsFromSensorCache();

        String str = "";
        int i = 1;

        for (String item : criticalItems) {
            if(i == criticalItems.size())
                str += "\u2022 "+ item;
            else
                str += "\u2022 "+ item + System.getProperty("line.separator");
            i++;
        }

        new BottomDialog.Builder(this)
                .setTitle("Stock Alert!")
                .setContent("The following items are below critical level"+
                        System.getProperty("line.separator")+System.getProperty("line.separator")+
                        str)
                .setIcon(R.drawable.ic_zeglabs_small)
                //.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_launcher))
                .show();
    }

    public void setupNavdrawer() {
        this.navigationView = (NavigationView) findViewById(R.id.nav_view_main);
        View hView =  this.navigationView.getHeaderView(0);
        navigationView.setItemIconTintList(null);

        DownloadImageFromInternet downloadImage;
        if (PrefManager.getBitmapFromMemCache(PrefManager.BITMAP_PHOTO_KEY) == null){
            downloadImage = new DownloadImageFromInternet(getApplicationContext(), (CircleImageView) hView.findViewById(R.id.nav_user_image));
            downloadImage.execute(PrefManager.getFromPrefs(getApplicationContext(), PrefManager.PHOTO_KEY, ""));
        }

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



    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
            //drawer.closeDrawer(this.navigationView);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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
            // Handle the camera action
            //set the fragment initially
            DashboardFragment fDashboard = new DashboardFragment();

            Bundle bundle = new Bundle();
            bundle.putParcelable(PrefManager.SENSOR_CACHE, sensorCache);
            fDashboard.setArguments(bundle);

            android.support.v4.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, fDashboard);
            fragmentTransaction.commit();
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


}
