package com.zeglabs.mohit.helloworld2.activity;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuInflater;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.graphics.Color;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.zeglabs.mohit.helloworld2.R;
import com.zeglabs.mohit.helloworld2.adapter.AddToCartDialogAdapterNew;
import com.zeglabs.mohit.helloworld2.adapter.ReportProblemAdapter;
import com.zeglabs.mohit.helloworld2.adapter.ShowAllSensorReadingsAdapter;
import com.zeglabs.mohit.helloworld2.app.MyApplication;
import com.zeglabs.mohit.helloworld2.decorators.DividerItemDecoration;
import com.zeglabs.mohit.helloworld2.gson.SensorReadingsGSON;
import com.zeglabs.mohit.helloworld2.helper.BluetoothCommunicator;
import com.zeglabs.mohit.helloworld2.helper.ComplexPreferences;
import com.zeglabs.mohit.helloworld2.helper.Config;
import com.zeglabs.mohit.helloworld2.helper.PrefManager;

import de.hdodenhof.circleimageview.CircleImageView;
import github.nisrulz.stackedhorizontalprogressbar.StackedHorizontalProgressBar;
import item.AddToCartDialogItemNew;
import com.zeglabs.mohit.helloworld2.gson.AddToCartItemGSON;
import item.InventoryItem;
import item.ReportProblemDialogItem;
import item.ShoppingCartLocalItem;
import item.ShowAllSensorReadingsItem;

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.charts.CombinedChart.DrawOrder;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.XAxis.XAxisPosition;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.BubbleData;
import com.github.mikephil.charting.data.BubbleDataSet;
import com.github.mikephil.charting.data.BubbleEntry;
import com.github.mikephil.charting.data.CandleData;
import com.github.mikephil.charting.data.CandleDataSet;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.ScatterData;
import com.github.mikephil.charting.data.ScatterDataSet;
import com.github.mikephil.charting.formatter.AxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.zeglabs.mohit.helloworld2.helper.ShoppingCartLocal;
import com.zeglabs.mohit.helloworld2.login.LoginActivity;
import com.zeglabs.mohit.helloworld2.model.SensorDetailsMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class InventoryActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private InventoryItem sensor;
    private InventoryActivity activity = this;

    private Context appContext;
    private String TAG = "InventoryActivity";
    private ViewGroup viewGroup;
    private NavigationView navigationView = null;

    public TextView title, year, genre, suppliesExpiryDate;
    public ImageView image;

    private RecyclerView mRecyclerView_AddToCart;
    private RecyclerView mRecyclerView_ReportProblem;
    //private AddToCartDialogAdapter adapterAddToCart;
    private AddToCartDialogAdapterNew adapterAddToCart;
    private ReportProblemAdapter adapterReportProblem;
    private ShowAllSensorReadingsAdapter adapterShowAllSensorReadings;

    private CombinedChart mChart;
    private final int itemcount = 7;

    //private List<AddToCartDailogItem> cartList = new ArrayList<AddToCartDailogItem>();
    private List<AddToCartDialogItemNew> cartList = new ArrayList<AddToCartDialogItemNew>();
    private List<ReportProblemDialogItem> problemList = new ArrayList<ReportProblemDialogItem>();
    private List<AddToCartItemGSON> readingList = new ArrayList<AddToCartItemGSON>();

    private SensorDetailsMap sensorCache;
    ComplexPreferences complexPreferences;

    private RecyclerView mRecyclerView_AllSensorReadings;
    private LinearLayoutManager mLinearLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        this.sensor = getIntent().getParcelableExtra("sensor");


        //sensorCache = getIntent().getParcelableExtra(PrefManager.SENSOR_CACHE);
        complexPreferences = ComplexPreferences.getComplexPreferences(getApplicationContext(), "", 0);
        sensorCache = complexPreferences.getObject(PrefManager.SENSOR_CACHE, SensorDetailsMap.class);

        appContext = getApplicationContext();
        viewGroup = (ViewGroup) ((ViewGroup) this.findViewById(android.R.id.content));

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view_main);
        navigationView.setNavigationItemSelectedListener(this);

        mProgressDialog = new ProgressDialog(this);

        setupNavdrawer();
        getInventoryTopLayout(viewGroup);
        setupInventoryChart();

        setupShowAllDataRelativeLayout();
    }

    String[] values = new String[] {
            "Device not giving correct reading",
            "Device stopped working",
            "Device not syncing reading",
            "App not detecting device",
            "Other"
    };

    ShoppingCartLocal shoppingCart = null;


    public void setupInventoryViewButtons() {

        shoppingCart = complexPreferences.getObject(PrefManager.SHOPPING_CART, ShoppingCartLocal.class);
        int hotCount = 0;

        if(shoppingCart != null) {
            for(Map.Entry<Integer, ShoppingCartLocalItem> entry : shoppingCart.getShoppingCart().entrySet()) {
                hotCount += entry.getValue().getQty();
            }
        }

        if(hotCount>0) {
            updateHotCount(hotCount);
        }

        Button addToCart = (Button) findViewById(R.id.btn_addToCart);
        addToCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //updateHotCount(1);
                AlertDialog.Builder builder = new AlertDialog.Builder(InventoryActivity.this);
                LayoutInflater inflater = InventoryActivity.this.getLayoutInflater();
                // Inflate and set the layout for the dialog
                // Pass null as the parent view because its going in the dialog layout
                final View dialogLayout = inflater.inflate(R.layout.dialog_add_to_cart_new, null);



                builder.setView(dialogLayout);

                mRecyclerView_AddToCart = (RecyclerView) dialogLayout.findViewById(R.id.recyclerview_add_to_cart);
                mRecyclerView_AddToCart.setLayoutManager(new LinearLayoutManager(InventoryActivity.this));
                mRecyclerView_AddToCart.setItemAnimator(new DefaultItemAnimator());
                mRecyclerView_AddToCart.addItemDecoration(new DividerItemDecoration(appContext));
                mRecyclerView_AddToCart.setPadding(0, 0, -100, 0);

                /*
                Create addToCart list
                 */
                /*cartList = new ArrayList<AddToCartDailogItem>();
                cartList.add(new AddToCartDailogItem("1 kg - pouch", "₹ 60"));
                cartList.add(new AddToCartDailogItem("5 kg - pouch", "₹ 299"));
                cartList.add(new AddToCartDailogItem("10 kg - pouch", "₹ 576"));*/
                final AlertDialog alertDialog = builder.create();
                if(cartList == null || cartList.isEmpty()) {
                    showProgressDialog("Please wait! Fetching order items from server!");
                    sendRequestOrderItems(alertDialog);
                } else {
                    /*
                    Set the adapterAddToCart with context=activity.this and itemList=addToCartList
                    */
                    alertDialog.setCanceledOnTouchOutside(false);
                    adapterAddToCart = new AddToCartDialogAdapterNew(InventoryActivity.this, activity, cartList);
                    mRecyclerView_AddToCart.setAdapter(adapterAddToCart);

                    alertDialog.show();


                }
                Button btnX = (Button) dialogLayout.findViewById(R.id.btn_closeX);
                btnX.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        alertDialog.dismiss();
                    }
                });
                Button addToCartButton = (Button) dialogLayout.findViewById(R.id.btn_addToCart_new);
                addToCartButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        alertDialog.dismiss();
                        int hotCount = adapterAddToCart.getCartSum();
                        hotCount = 0;

                        shoppingCart = complexPreferences.getObject(PrefManager.SHOPPING_CART, ShoppingCartLocal.class);
                        if(shoppingCart != null) {
                            for(Map.Entry<Integer, ShoppingCartLocalItem> entry : shoppingCart.getShoppingCart().entrySet()) {
                                hotCount += entry.getValue().getQty();
                            }
                        }

                        if(hotCount>=0) {
                            updateHotCount(hotCount);
                        }

                        /**
                         * TODO: Write code to send the cart details to the backend server
                         */
                        int temp = 1;
                    }
                });
            }
        });

        final Button reportProblem = (Button) findViewById(R.id.btn_reportProblem);
        reportProblem.setOnClickListener(new View.OnClickListener() {
            private int item;
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(InventoryActivity.this);
                LayoutInflater inflater = InventoryActivity.this.getLayoutInflater();
                final View dialogLayout = inflater.inflate(R.layout.dialog_report_problem, null);
                //dialogLayout.setPadding(0, 0, -130, 0);
                builder.setView(dialogLayout);

                mRecyclerView_ReportProblem = (RecyclerView) dialogLayout.findViewById(R.id.recyclerview_report_problem);
                mRecyclerView_ReportProblem.setLayoutManager(new LinearLayoutManager(InventoryActivity.this));
                mRecyclerView_ReportProblem.setItemAnimator(new DefaultItemAnimator());
                mRecyclerView_ReportProblem.addItemDecoration(new DividerItemDecoration(appContext));
                mRecyclerView_ReportProblem.setPadding(0, 0, -130, 0);

                /*
                Create reportProblem list
                 */
                problemList = new ArrayList<ReportProblemDialogItem>();
                for(int i=0; i<values.length; i++) {
                    problemList.add(new ReportProblemDialogItem(values[i]));
                }

                /*
                Set the adapterReportProblem with context=activity.this and itemList=problemList
                 */
                final AlertDialog alertDialog = builder.create();
                alertDialog.setCanceledOnTouchOutside(false);
                adapterReportProblem = new ReportProblemAdapter(InventoryActivity.this, problemList, alertDialog);
                mRecyclerView_ReportProblem.setAdapter(adapterReportProblem);

                alertDialog.show();

                Button btnsubmit = (Button) dialogLayout.findViewById(R.id.btn_reportProblem);
                TextView txtOther = (TextView) dialogLayout.findViewById(R.id.txt_problem);
                btnsubmit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String problemId = "Problem "+ReportProblemAdapter.position+" selected";
                        Toast.makeText(getApplicationContext(),problemId,Toast.LENGTH_LONG).show();
                        alertDialog.dismiss();
                        /*
                        TODO: Add code to email the problem specified by the user to our support
                         */
                    }
                });
                Button btnX = (Button) dialogLayout.findViewById(R.id.btn_closeX);
                btnX.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        alertDialog.dismiss();
                    }
                });
            }
        });
    }

    public void setupNavdrawer() {
        this.navigationView = (NavigationView) findViewById(R.id.nav_view_main);
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

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private int hot_number = 1;
    private TextView ui_hot = null;

    private Menu optionsMenu;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.optionsMenu = menu;

        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.inventory, menu);
        final View menu_hotlist = menu.findItem(R.id.menu_shopping_cart).getActionView();
        ui_hot = (TextView) menu_hotlist.findViewById(R.id.hotlist_hot);
        ui_hot.setVisibility(View.GONE);
        //updateHotCount(hot_number);
        new MyMenuItemStuffListener(menu_hotlist, "Show hot message") {
            @Override
            public void onClick(View v) {
                //onHotlistSelected();
                //updateHotCount(hot_number++);
                Intent intent = new Intent(viewGroup.getContext(), ShoppingCartActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                viewGroup.getContext().startActivity(intent);
            }
        };

        /**
         * Setting count badge on shopping carts
         */
        setupInventoryViewButtons();
        return true;
    }
    // call the updating code on the main thread,
// so we can call this asynchronously
    public void updateHotCount(final int new_hot_number) {
        hot_number = new_hot_number;
        if (ui_hot == null) return;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (new_hot_number == 0)
                    ui_hot.setVisibility(View.INVISIBLE);
                else {
                    ui_hot.setVisibility(View.VISIBLE);
                    ui_hot.setText(Integer.toString(new_hot_number));
                }
            }
        });
    }

    public void setRefreshActionButtonState(final boolean refreshing) {
        if (optionsMenu != null) {
            final MenuItem refreshItem = optionsMenu.findItem(R.id.menu_sensorSyncReading);
            if (refreshItem != null) {
                if (refreshing) {
                    refreshItem.setActionView(R.layout.actionbar_indeterminate_progress);
                } else {
                    refreshItem.setActionView(null);
                }
            }
        }
    }

    boolean refreshClicked = false;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        } else if(id == R.id.menu_sensorSyncReading) {
            setRefreshActionButtonState(!refreshClicked);
            refreshClicked = !refreshClicked;
            showProgressDialog();

            sensorReading = findSensorReadingLocal();

            if(sensorReading == null) {
                showDeviceNotFoundAlertDialog();
            } else {
                showSyncConfirmAlertDialog();

                Toast.makeText(appContext, "Got the reading=" + sensorReading + " from the device=" + sensor.getMacAddress() + "!", Toast.LENGTH_LONG).show();
                setRefreshActionButtonState(false);
            }
            hideProgressDialog();
            return true;
        } else if(id == R.id.menu_shopping_cart) {
            Intent intent = new Intent(viewGroup.getContext(), ShoppingCartActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            viewGroup.getContext().startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
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
        //timerDelayRemoveDialog(10000, mProgressDialog);
    }

    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.hide();
        }
    }

    private void showProgressDialog(String message) {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setIndeterminate(true);
        }
        mProgressDialog.setMessage(message);

        mProgressDialog.show();
        //timerDelayRemoveDialog(10000, mProgressDialog);
    }

    private void showDeviceNotFoundAlertDialog() {
        String error = "Sorry! Device with macAddress "+this.sensor.getMacAddress()+" could not be found! Please ensure that the sensor is on, the phone is within a distance of 20m and its bluetooth is working!";

        AlertDialog.Builder builder = new AlertDialog.Builder(InventoryActivity.this);
        LayoutInflater inflater = InventoryActivity.this.getLayoutInflater();
        final View dialogLayout = inflater.inflate(R.layout.dialog_device_not_found, null);
        builder.setView(dialogLayout);

        final AlertDialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(false);

        Button btnOk = (Button) dialogLayout.findViewById(R.id.btn_deviceNotFoundOK);
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                setRefreshActionButtonState(false);
                alertDialog.hide();
            }
        });

        Button btnX = (Button) dialogLayout.findViewById(R.id.btn_closeX);
        btnX.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setRefreshActionButtonState(false);
                alertDialog.hide();
            }
        });

        TextView txtError = (TextView) dialogLayout.findViewById(R.id.txt_errorDeviceNotFound);
        txtError.setText(error);

        alertDialog.show();

    }

    private void showSyncConfirmAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(InventoryActivity.this);
        LayoutInflater inflater = InventoryActivity.this.getLayoutInflater();
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

                setRefreshActionButtonState(false);
                Toast.makeText(appContext, "Sending reading="+sensorReading+" to the server!",Toast.LENGTH_LONG).show();
                alertDialog.hide();
            }
        });

        EditText txtReading = (EditText) dialogLayout.findViewById(R.id.txt_reading);
        txtReading.setText(sensorReading);

        alertDialog.show();
    }

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothCommunicator bc;
    private String sensorReading;

    private String findSensorReadingLocal() {

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!bluetoothAdapter.isEnabled()) {
            Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnOn, 0);
        }
        if(this.bc == null) {
            if(this.mProgressDialog == null) {
                mProgressDialog = new ProgressDialog(this);
            }
            this.bc = new BluetoothCommunicator(InventoryActivity.this, sensor.getMacAddress(), bluetoothAdapter, mProgressDialog);
        }
        this.sensorReading = this.bc.getSensorReading();

        return this.sensorReading;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_dashboard) {
            Intent intent = new Intent(viewGroup.getContext(), MainActivity.class);
            //intent.putExtra(PrefManager.SENSOR_CACHE, this.sensorCache);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
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

    protected String[] mMonths = new String[] {
            "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Okt", "Nov", "Dec"
    };

    public void setupInventoryChart() {


        mChart = (CombinedChart) findViewById(R.id.chart1);
        mChart.setDescription("");
        mChart.setBackgroundColor(Color.WHITE);
        mChart.setDrawGridBackground(false);
        mChart.setDrawBarShadow(false);
        mChart.setHighlightFullBarEnabled(false);

        // draw bars behind lines
        mChart.setDrawOrder(new DrawOrder[]{
                DrawOrder.BAR, DrawOrder.BUBBLE, DrawOrder.LINE
        });

        Legend l = mChart.getLegend();
        l.setWordWrapEnabled(true);
        l.setPosition(Legend.LegendPosition.BELOW_CHART_CENTER);

        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setDrawGridLines(false);
        rightAxis.setAxisMinValue(0f); // this replaces setStartAtZero(true)
        //rightAxis.setAxisMaxValue(this.sensor.getMaxDistance());
        rightAxis.setAxisMaxValue(100f);

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setAxisMinValue(0f); // this replaces setStartAtZero(true)
        //leftAxis.setAxisMaxValue(this.sensor.getMaxDistance());
        leftAxis.setAxisMaxValue(100f);

        XAxis xAxis = mChart.getXAxis();
        xAxis.setPosition(XAxisPosition.BOTTOM);
        xAxis.setAxisMinValue(0f);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);
        xAxis.setValueFormatter(new AxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                //return mMonths[(int) value % mMonths.length];
                String[] dates = sensor.getReadings().keySet().toArray(new String[sensor.getReadings().keySet().size()]);
                return dates[(int) value % dates.length];
            }

            @Override
            public int getDecimalDigits() {
                return 0;
            }
        });

        CombinedData data = new CombinedData();

        data.setData(generateLineData());
        data.setData(generateBarData());
        //data.setData(generateBubbleData());
        //data.setData(generateScatterData());
        //data.setData(generateCandleData());
        //Typeface mTfLight = Typeface.createFromAsset(getAssets(), "OpenSans-Light.ttf");
        //data.setValueTypeface(mTfLight);

        xAxis.setAxisMaxValue(data.getXMax() + 0.25f);

        mChart.setData(data);
        mChart.invalidate();
    }

    protected float getRandom(float range, float startsfrom) {
        return (float) (Math.random() * range) + startsfrom;
    }

    double dailyAvgConsumption = 0;
    String suppliesExpectedToLastUnitl = "";

    private LineData generateLineData() {

        LineData d = new LineData();

        ArrayList<Entry> entries = new ArrayList<Entry>();

        //for (int index = 0; index < this.sensor.getReadings().size(); index++)
        //    entries.add(new Entry(index + 0.5f, getRandom(80, 5)));

        int index = 0;
        int maxDistance = this.sensor.getMaxDistance();

        float diff = 0;
        float prev = 0;
        String lastSyncDateString = this.sensor.getLastUpdateTime()+", "+ Calendar.getInstance().get(Calendar.YEAR);

        for (Integer value : this.sensor.getReadings().values()) {
            entries.add(new Entry(index + 0.5f, (float) (value*100.0)/maxDistance));
            if(index != 0) {
                diff += prev - value;
            }
            prev = value;
            index++;
        }

        this.dailyAvgConsumption = index == 0? 0: diff/index;
        this.dailyAvgConsumption /= this.sensor.getMaxDistance();

        Double daysToExpiry = Math.ceil(((float)this.sensor.getCurrentReading()/this.sensor.getMaxDistance())/this.dailyAvgConsumption);

        Date lastSyncDate = null;
        try {
            lastSyncDate = new SimpleDateFormat("hh:mm, MMM dd, yyyy", Locale.ENGLISH).parse(lastSyncDateString);
            Calendar cal = Calendar.getInstance();
            cal.setTime(lastSyncDate);
            int days = daysToExpiry.intValue();
            cal.add(Calendar.DATE, days);
            this.suppliesExpectedToLastUnitl = new SimpleDateFormat("dd-MMM").format(cal.getTime());
        } catch (ParseException e) {

        }
        suppliesExpiryDate.setText(suppliesExpectedToLastUnitl);

        LineDataSet set = new LineDataSet(entries, "Line DataSet");
        set.setColor(Color.rgb(240, 238, 70));
        set.setLineWidth(2.5f);
        set.setCircleColor(Color.rgb(240, 238, 70));
        set.setCircleRadius(5f);
        set.setFillColor(Color.rgb(240, 238, 70));
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        set.setDrawValues(true);
        set.setValueTextSize(10f);
        set.setValueTextColor(Color.rgb(226, 76, 96));

        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        d.addDataSet(set);

        return d;
    }

    private BarData generateBarData() {

        ArrayList<BarEntry> entries1 = new ArrayList<BarEntry>();
        ArrayList<BarEntry> entries2 = new ArrayList<BarEntry>();

        for (int index = 0; index < this.sensor.getReadings().size(); index++) {
            //entries1.add(new BarEntry(index, this.sensor.getMaxDistance()*0.2f));
            entries1.add(new BarEntry(index, (float) Config.CONST_CRITICAL_STOCK_LEVEL * 100.0f));

            // stacked
            entries2.add(new BarEntry(0, new float[]{getRandom(13, 12), getRandom(13, 12)}));
        }

        BarDataSet set1 = new BarDataSet(entries1, "Bar 1");
        set1.setColor(Color.rgb(226, 76, 96));

        //set1.setValueTextColor(Color.rgb(60, 220, 78));
        //set1.setValueTextSize(10f);
        set1.setAxisDependency(YAxis.AxisDependency.LEFT);

        float barWidth = 1f; // x2 dataset

        BarData d = new BarData(set1);
        d.setBarWidth(barWidth);
        d.setValueTextSize(0f);

        // make this BarData object grouped
        //d.groupBars(0, groupSpace, barSpace); // start at x = 0

        return d;
    }

    public void getInventoryTopLayout (View view) {
        title = (TextView) view.findViewById(R.id.itemName);
        //genre = (TextView) view.findViewById(R.id.genre);
        year = (TextView) view.findViewById(R.id.lastUpdateTime);
        image = (ImageView) view.findViewById(R.id.grain_image);
        suppliesExpiryDate = (TextView) findViewById(R.id.txt_suppliesExpectedToLastUntil);
        TextView mac = (TextView) view.findViewById(R.id.mac);

        String txtTitle = Character.toTitleCase(this.sensor.getItemName().charAt(0)) + this.sensor.getItemName().substring(1);
        title.setText(txtTitle);

        //title.setText(this.sensor.getItemName());
        mac.setText("Device Mac Address: "+this.sensor.getMacAddress());
        year.setText("Last synced at: "+this.sensor.getLastUpdateTime());

        switch (this.sensor.getItemName().toLowerCase()) {
            case "rice":
                image.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ic_rice));
                break;
            case "wheat":
                image.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ic_wheat));
                break;
            case "daal":
                image.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ic_daal));
                break;
            case "coffee":
                image.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ic_coffee));
                break;
            case "sugar":
                image.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ic_sugar));
                break;
            case "salt":
                image.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ic_salt));
                break;
        }

        StackedHorizontalProgressBar stackedHorizontalProgressBar;
        stackedHorizontalProgressBar = (StackedHorizontalProgressBar) view.findViewById(R.id.stackedhorizontalprogressbar);

        stackedHorizontalProgressBar.setMax(this.sensor.getMaxDistance());
        stackedHorizontalProgressBar.setProgress(this.sensor.getCurrentReading());
        stackedHorizontalProgressBar.setSecondaryProgress(this.sensor.getMaxDistance() - this.sensor.getCurrentReading());

        /*int primary_pts = 3;
        int secondary_pts = 7;
        int max = 10;

        stackedHorizontalProgressBar.setMax(max);
        stackedHorizontalProgressBar.setProgress(primary_pts);
        stackedHorizontalProgressBar.setSecondaryProgress(secondary_pts);*/
    }

    protected ScatterData generateScatterData() {

        ScatterData d = new ScatterData();

        ArrayList<Entry> entries = new ArrayList<Entry>();

        for (float index = 0; index < itemcount; index += 0.5f)
            entries.add(new Entry(index + 0.25f, getRandom(10, 55)));

        ScatterDataSet set = new ScatterDataSet(entries, "Scatter DataSet");
        set.setColors(ColorTemplate.MATERIAL_COLORS);
        set.setScatterShapeSize(7.5f);
        set.setDrawValues(false);
        set.setValueTextSize(10f);
        d.addDataSet(set);

        return d;
    }

    protected CandleData generateCandleData() {

        CandleData d = new CandleData();

        ArrayList<CandleEntry> entries = new ArrayList<CandleEntry>();

        for (int index = 0; index < itemcount; index += 2)
            entries.add(new CandleEntry(index + 1f, 90, 70, 85, 75f));

        CandleDataSet set = new CandleDataSet(entries, "Candle DataSet");
        set.setDecreasingColor(Color.rgb(142, 150, 175));
        set.setShadowColor(Color.DKGRAY);
        set.setBarSpace(0.3f);
        set.setValueTextSize(10f);
        set.setDrawValues(false);
        d.addDataSet(set);

        return d;
    }

    protected BubbleData generateBubbleData() {

        BubbleData bd = new BubbleData();

        ArrayList<BubbleEntry> entries = new ArrayList<BubbleEntry>();

        for (int index = 0; index < itemcount; index++) {
            float y = getRandom(10, 105);
            float size = getRandom(100, 105);
            entries.add(new BubbleEntry(index + 0.5f, y, size));
        }

        BubbleDataSet set = new BubbleDataSet(entries, "Bubble DataSet");
        set.setColors(ColorTemplate.VORDIPLOM_COLORS);
        set.setValueTextSize(10f);
        set.setValueTextColor(Color.WHITE);
        set.setHighlightCircleWidth(1.5f);
        set.setDrawValues(true);
        bd.addDataSet(set);

        return bd;
    }

    static abstract class MyMenuItemStuffListener implements View.OnClickListener, View.OnLongClickListener {
        private String hint;
        private View view;

        MyMenuItemStuffListener(View view, String hint) {
            this.view = view;
            this.hint = hint;
            view.setOnClickListener(this);
            view.setOnLongClickListener(this);
        }

        @Override abstract public void onClick(View v);

        @Override public boolean onLongClick(View v) {
            final int[] screenPos = new int[2];
            final Rect displayFrame = new Rect();
            view.getLocationOnScreen(screenPos);
            view.getWindowVisibleDisplayFrame(displayFrame);
            final Context context = view.getContext();
            final int width = view.getWidth();
            final int height = view.getHeight();
            final int midy = screenPos[1] + height / 2;
            final int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
            Toast cheatSheet = Toast.makeText(context, hint, Toast.LENGTH_SHORT);
            if (midy < displayFrame.height()) {
                cheatSheet.setGravity(Gravity.TOP | Gravity.RIGHT,
                        screenWidth - screenPos[0] - width / 2, height);
            } else {
                cheatSheet.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, height);
            }
            cheatSheet.show();
            return true;
        }
    }
    Gson gson = new GsonBuilder().create();
    private void sendRequestOrderItems(final AlertDialog alertDialog) {
        StringRequest strReq = new StringRequest(Request.Method.POST,
                Config.URL_GET_ORDER_ITEMS, new Response.Listener<String>() {

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
                        /**
                         * Success, show alertDialog.
                         */
                        JSONArray orderItemsJsonArray = new JSONArray(responseObj.getString("orderItems"));

                        for(int i=0; i<orderItemsJsonArray.length(); i++) {
                            AddToCartItemGSON itemTemp = gson.fromJson(orderItemsJsonArray.getString(i), AddToCartItemGSON.class);
                            AddToCartDialogItemNew item = new AddToCartDialogItemNew(appContext, itemTemp);
                            cartList.add(item);
                        }

                        /**
                         * Hide progress view and show the custom dialog
                         */

                        hideProgressDialog();

                        alertDialog.setCanceledOnTouchOutside(false);
                        adapterAddToCart = new AddToCartDialogAdapterNew(InventoryActivity.this, activity, cartList);
                        mRecyclerView_AddToCart.setAdapter(adapterAddToCart);

                        alertDialog.show();

                    } else if (errorCode == 1) {
                        Toast.makeText(appContext, "No user exists with email="+PrefManager.getFromPrefs(appContext, PrefManager.EMAIL_KEY, "")+"\n" + message, Toast.LENGTH_LONG).show();

                        Intent intent = new Intent(appContext, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        appContext.startActivity(intent);

                    } else if (errorCode == 2) {
                        Toast.makeText(appContext, "No item exists with name="+sensor.getItemName().toLowerCase()+"\n" + message, Toast.LENGTH_LONG).show();

                        Intent intent = new Intent(appContext, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        appContext.startActivity(intent);
                    } else {
                        Toast.makeText(appContext, "Inconsistent Database. Please check the error=" + message, Toast.LENGTH_LONG).show();
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
                params.put("item", sensor.getItemName().toLowerCase());

                Log.e(TAG, "Posting params: " + params.toString());

                return params;
            }

        };

        // Adding request to request queue
        MyApplication.getInstance().addToRequestQueue(strReq);
    }

    private void setupShowAllDataRelativeLayout() {
        RelativeLayout layout_showAllData = (RelativeLayout)viewGroup.findViewById(R.id.layout_show_all_data);
        layout_showAllData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                LayoutInflater inflater = InventoryActivity.this.getLayoutInflater();
                final View dialogLayout = inflater.inflate(R.layout.dialog_show_all_data, null);

                builder.setView(dialogLayout);

                mRecyclerView_AllSensorReadings = (RecyclerView) dialogLayout.findViewById(R.id.recyclerview_showAllSensorReadings);
                mRecyclerView_AllSensorReadings.setLayoutManager(new LinearLayoutManager(InventoryActivity.this));
                mRecyclerView_AllSensorReadings.setItemAnimator(new DefaultItemAnimator());
                //mRecyclerView_AllSensorReadings.addItemDecoration(new DividerItemDecoration(appContext));
                mRecyclerView_AllSensorReadings.setPadding(0, 0, -100, 0);

                final AlertDialog alertDialog = builder.create();
                if(readingsList == null || readingsList.isEmpty()) {
                    showProgressDialog("Please wait! Fetching sensor readings from server!");
                    sendRequestAllSensorReadings(alertDialog);
                } else {
                    /*
                    Set the adapterAddToCart with context=activity.this and itemList=addToCartList
                    */
                    alertDialog.setCanceledOnTouchOutside(false);
                    adapterShowAllSensorReadings = new ShowAllSensorReadingsAdapter(appContext, activity, readingsList);
                    mRecyclerView_AllSensorReadings.setAdapter(adapterShowAllSensorReadings);
                    alertDialog.show();
                }

                Button btnX = (Button) dialogLayout.findViewById(R.id.btn_closeX);
                btnX.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        alertDialog.dismiss();
                    }
                });
                Button btnOK = (Button) dialogLayout.findViewById(R.id.btn_generalButton);
                btnOK.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        alertDialog.dismiss();
                    }
                });
            }
        });

    }

    List<ShowAllSensorReadingsItem> readingsList = new ArrayList<ShowAllSensorReadingsItem>();
    Gson gsonDate = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();

    private void sendRequestAllSensorReadings(final AlertDialog alertDialog) {
        StringRequest strReq = new StringRequest(Request.Method.POST,
                Config.URL_GET_ALL_SENSOR_READINGS, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, response.toString());

                try {
                    JSONObject responseObj = new JSONObject(response);

                    boolean error = responseObj.getBoolean("error");
                    String message = responseObj.getString("message");
                    int errorCode = responseObj.getInt("code");

                    if (errorCode == 0) {
                        /**
                         * Success, show alertDialog.
                         */
                        JSONArray sensorReadingsJsonArray = new JSONArray(responseObj.getString("readings"));

                        Map<Date, Integer> readingsMap = new TreeMap<Date, Integer>();
                        for(int i=0; i<sensorReadingsJsonArray.length(); i++) {
                            SensorReadingsGSON readingTemp = gsonDate.fromJson(sensorReadingsJsonArray.getString(i), SensorReadingsGSON.class);
                            readingsMap.put(readingTemp.getDateCreated(), readingTemp.getReading());
                        }

                        for(Map.Entry<Date, Integer> entry : readingsMap.entrySet()) {
                            readingsList.add(new ShowAllSensorReadingsItem(entry.getKey(), entry.getValue()));
                        }

                        /**
                         * Hide progress view and show the custom dialog
                         */

                        hideProgressDialog();

                        alertDialog.setCanceledOnTouchOutside(false);
                        adapterShowAllSensorReadings = new ShowAllSensorReadingsAdapter(appContext, activity, readingsList);
                        mRecyclerView_AllSensorReadings.setAdapter(adapterShowAllSensorReadings);

                        alertDialog.show();

                    } else if (errorCode == 1) {
                        Toast.makeText(appContext, "No sensor exists in the DB with mac="+sensor.getMacAddress()+"\n" + message, Toast.LENGTH_LONG).show();

                    } else if (errorCode == 2) {
                        Toast.makeText(appContext, "No item user exists with email="+PrefManager.getFromPrefs(appContext, PrefManager.EMAIL_KEY, "")+"\n" + message, Toast.LENGTH_LONG).show();

                        Intent intent = new Intent(appContext, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        appContext.startActivity(intent);
                    } else if (errorCode == 3){
                        Toast.makeText(appContext, "DB Failure OR No readings found for sensor with mac="+sensor.getMacAddress()+". Please check the error=" + message, Toast.LENGTH_LONG).show();
                    } else if (errorCode == 4) {
                        Toast.makeText(appContext, "Sensor with mac="+sensor.getMacAddress()+" associated with another email id"+ message, Toast.LENGTH_LONG).show();

                        Intent intent = new Intent(appContext, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        appContext.startActivity(intent);
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
                params.put("mac", sensor.getMacAddress());

                Log.e(TAG, "Posting params: " + params.toString());

                return params;
            }

        };

        // Adding request to request queue
        MyApplication.getInstance().addToRequestQueue(strReq);
    }
}
