package com.zeglabs.mohit.helloworld2.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.zeglabs.mohit.helloworld2.R;
import com.zeglabs.mohit.helloworld2.helper.ComplexPreferences;
import com.zeglabs.mohit.helloworld2.helper.PrefManager;
import com.zeglabs.mohit.helloworld2.model.SensorDetails;
import com.zeglabs.mohit.helloworld2.model.SensorDetailsMap;

import java.util.Calendar;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;
import github.nisrulz.stackedhorizontalprogressbar.StackedHorizontalProgressBar;

public class DeviceAddedActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private Activity act;
    private String TAG = DeviceAddedActivity.class.getSimpleName();

    NavigationView navigationView;
    private ViewGroup viewGroup;
    private Context appContext;

    private String mac;
    private String itemName;
    private String reading;
    private String deviceName;

    private TextView txtMacAddress;
    private TextView txtGrain;
    private TextView txtContainerHeight;
    private ImageView imgGrain;
    private TextView txtSuccessMessage;
    private StackedHorizontalProgressBar dataBar;

    private Button btnAddAnother;
    private Button btnOk;

    //private SensorDetailsMap cache;
    private SensorDetailsMap sensorCache;
    ComplexPreferences complexPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        act = DeviceAddedActivity.this;

        appContext = getApplicationContext();
        viewGroup = (ViewGroup) this.findViewById(android.R.id.content);

        setContentView(R.layout.activity_device_added);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //cache = getIntent().getParcelableExtra(PrefManager.SENSOR_CACHE);
        complexPreferences = ComplexPreferences.getComplexPreferences(getApplicationContext(), "", 0);
        sensorCache = complexPreferences.getObject(PrefManager.SENSOR_CACHE, SensorDetailsMap.class);

        /**
         * Extract variables from the intent
         */
        this.mac = getIntent().getStringExtra(PrefManager.SENSORCREATE_MAC);
        this.itemName = getIntent().getStringExtra(PrefManager.SENSORCREATE_ITEM);
        this.reading = getIntent().getStringExtra(PrefManager.SENSORCREATE_MAXDISTANCE);
        this.deviceName = getIntent().getStringExtra(PrefManager.SENSORCREATE_DEVICENAME);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        setupNavdrawer();
        setupViewElements();
        addSensorToCache();
    }

    private void addSensorToCache() {
        Calendar calendar = Calendar.getInstance();
        Date today = calendar.getTime();

        String[] parts = null;
        if(this.reading.contains("cm")) {
            parts = this.reading.split("\\s*cm\\s*");
        }
        String reading = "";
        if(parts != null) {
            for (String str : parts)
                reading += str.trim();
        } else {
            reading = this.reading;
        }

        int intReading = Integer.parseInt(reading);

        SensorDetails sensor = new SensorDetails(this.mac, this.deviceName, this.itemName, intReading, today);
        this.sensorCache.getSensorCache().put(this.mac, sensor);

        complexPreferences.putObject(PrefManager.SENSOR_CACHE, sensorCache);
        complexPreferences.commit();

    }

    private void setupViewElements() {
        this.txtMacAddress = (TextView) findViewById(R.id.txt_macAddress);
        this.txtGrain = (TextView) findViewById(R.id.txt_grain);
        this.txtContainerHeight = (TextView) findViewById(R.id.txt_containerHeight);
        this.txtSuccessMessage = (TextView) findViewById(R.id.txt_successMessage);
        this.imgGrain = (ImageView) findViewById(R.id.img_grain);
        this.dataBar = (StackedHorizontalProgressBar) findViewById(R.id.stackedhorizontalprogressbar);

        this.btnAddAnother = (Button) findViewById(R.id.btn_addAnother);
        this.btnOk = (Button) findViewById(R.id.btn_dashboard);

        this.btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(viewGroup.getContext(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                //intent.putExtra(PrefManager.SENSOR_CACHE, cache);
                viewGroup.getContext().startActivity(intent);
            }
        });

        this.btnAddAnother.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), AddDeviceActivity.class);
                //intent.putExtra(PrefManager.SENSOR_CACHE, cache);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                view.getContext().startActivity(intent);
            }
        });

        String message = "Success!"+System.getProperty("line.separator")+System.getProperty("line.separator")+"The device to track "+this.itemName+" container has been added successfully. Please press OK to proceed to Home or press Add Another to continue adding another device";


        /**
         * Setup the chart
         */
        int primary_pts = 0;
        int secondary_pts = 10;
        int max = 10;

        this.dataBar.setMax(max);
        this.dataBar.setProgress(primary_pts);
        this.dataBar.setSecondaryProgress(secondary_pts);

        /**
         * Setup the TextView elements
         */
        this.txtMacAddress.setText(this.mac);
        this.txtGrain.setText(this.itemName);
        if(!this.reading.contains("cm")) {
            this.reading += " cm";
        }
        this.txtContainerHeight.setText(this.reading);

        SpannableStringBuilder str = new SpannableStringBuilder(message);
        str.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), 0, 8, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        this.txtSuccessMessage.setText(str);

        /**
         * Setup imageview element
         */
        switch (this.itemName) {
            case "rice":
                imgGrain.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ic_rice));
                break;
            case "wheat":
                imgGrain.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ic_wheat));
                break;
            case "daal":
                imgGrain.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ic_daal));
                break;
            case "coffee":
                imgGrain.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ic_coffee));
                break;
            case "sugar":
                imgGrain.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ic_sugar));
                break;
            case "salt":
                imgGrain.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ic_salt));
                break;
        }
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
        getMenuInflater().inflate(R.menu.device_added, menu);
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
    public void setupNavdrawer() {
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
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
}
