package com.zeglabs.mohit.helloworld2.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import android.widget.Button;
import android.widget.TextView;

import com.zeglabs.mohit.helloworld2.R;
import com.zeglabs.mohit.helloworld2.adapter.ShoppingCartAdapter;
import com.zeglabs.mohit.helloworld2.decorators.DividerItemDecoration;
import com.zeglabs.mohit.helloworld2.helper.ComplexPreferences;
import com.zeglabs.mohit.helloworld2.helper.PrefManager;
import com.zeglabs.mohit.helloworld2.helper.ShoppingCartLocal;
import com.zeglabs.mohit.helloworld2.model.SensorDetailsMap;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;
import item.ShoppingCartLocalItem;

public class ShoppingCartActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private Activity act;

    private SensorDetailsMap sensorCache;
    ComplexPreferences complexPreferences;

    private Context appContext;
    private String TAG = AccountActivity.class.getSimpleName();
    private ViewGroup viewGroup;

    NavigationView navigationView;
    ShoppingCartLocal shoppingCart = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_cart);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        complexPreferences = ComplexPreferences.getComplexPreferences(getApplicationContext(), "", 0);
        sensorCache = complexPreferences.getObject(PrefManager.SENSOR_CACHE, SensorDetailsMap.class);


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        setupClassDefaults();
        setupNavdrawer();
        setupActivityElements();
    }

    RecyclerView recyclerView;
    TextView txtShoppingCartSummary;
    ShoppingCartAdapter mAdapter;
    Button btnCheckout;

    private void setupActivityElements() {
        recyclerView = (RecyclerView) viewGroup.findViewById(R.id.recyclerview_shopping_cart);
        txtShoppingCartSummary = (TextView) viewGroup.findViewById(R.id.txt_shopping_cart_summary);
        btnCheckout = (Button) viewGroup.findViewById(R.id.btn_checkout);

        txtShoppingCartSummary.setText(getSummaryText());

        setupActivityAdapters();

        btnCheckout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showGeneralDialogBox("Coming Soon!", "We're working hard to roll this out to you. Please revisit us soon. Thank you for your patience!", "OK");
            }
        });
    }

    private String getSummaryText() {
        complexPreferences = ComplexPreferences.getComplexPreferences(appContext, "", 0);

        shoppingCart = complexPreferences.getObject(PrefManager.SHOPPING_CART, ShoppingCartLocal.class);
        if(shoppingCart == null) {
            shoppingCart = new ShoppingCartLocal();
        }
        int countItems = shoppingCart.getShoppingCart().size();
        int total = 0;
        for(Map.Entry<Integer, ShoppingCartLocalItem> entry : shoppingCart.getShoppingCart().entrySet()) {
            int qty = entry.getValue().getQty();
            int lotSize = 0;
            int price = 0;
            String qtyPrice = entry.getValue().getQtyPrice();

            Pattern p = Pattern.compile("\\s*(\\d+)\\s+kg\\s*-\\s*Rs[.]?\\s*(\\d+)");
            Matcher m = p.matcher(qtyPrice);
            if(m.matches()) {
                lotSize = Integer.parseInt(m.group(1));
                price = Integer.parseInt(m.group(2));
            }
            total += qty*lotSize*price;
        }
        return countItems+" Items - Rs "+total;
    }

    private void setupActivityAdapters() {
        mAdapter = new ShoppingCartAdapter(appContext, act);

        recyclerView.setLayoutManager(new LinearLayoutManager(act));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(appContext));
        //recyclerView.setPadding(0, 0, -100, 0);

        recyclerView.setAdapter(mAdapter);
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
        getMenuInflater().inflate(R.menu.shopping_cart, menu);
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
            Intent intent = new Intent(viewGroup.getContext(), ComingSoonActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            viewGroup.getContext().startActivity(intent);

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

    private void setupClassDefaults() {
        act = ShoppingCartActivity.this;

        appContext = getApplicationContext();
        viewGroup = (ViewGroup) (this.findViewById(android.R.id.content));

        complexPreferences = ComplexPreferences.getComplexPreferences(getApplicationContext(), "", 0);
        sensorCache = complexPreferences.getObject(PrefManager.SENSOR_CACHE, SensorDetailsMap.class);
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

    private void showGeneralDialogBox(String title, String message, String btnText) {

        AlertDialog.Builder builder = new AlertDialog.Builder(act);
        LayoutInflater inflater = act.getLayoutInflater();
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
            }
        });

        Button btnX = (Button) dialogLayout.findViewById(R.id.btn_closeX);
        btnX.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.hide();
            }
        });

        alertDialog.show();

    }
}
