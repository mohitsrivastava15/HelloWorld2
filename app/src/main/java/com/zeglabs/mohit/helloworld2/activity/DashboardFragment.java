package com.zeglabs.mohit.helloworld2.activity;


import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.zeglabs.mohit.helloworld2.R;
import com.zeglabs.mohit.helloworld2.adapter.InventoryAdapter;
import com.zeglabs.mohit.helloworld2.adapter.ShowAllSensorReadingsAdapter;
import com.zeglabs.mohit.helloworld2.app.MyApplication;
import com.zeglabs.mohit.helloworld2.decorators.DividerItemDecoration;
import com.zeglabs.mohit.helloworld2.gson.SensorReadingsGSON;
import com.zeglabs.mohit.helloworld2.helper.ComplexPreferences;
import com.zeglabs.mohit.helloworld2.helper.Config;
import com.zeglabs.mohit.helloworld2.helper.PrefManager;
import com.zeglabs.mohit.helloworld2.helper.SensorDetailsParser;
import com.zeglabs.mohit.helloworld2.login.LoginActivity;
import com.zeglabs.mohit.helloworld2.model.SensorDetails;
import com.zeglabs.mohit.helloworld2.model.SensorDetailsMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import item.InventoryItem;
import item.ShowAllSensorReadingsItem;

/**
 * A simple {@link Fragment} subclass.
 */
public class DashboardFragment extends Fragment {
    BluetoothAdapter bluetoothAdapter;
    BluetoothSocket mmSocket;
    BluetoothDevice mmDevice;
    OutputStream mmOutputStream;
    InputStream mmInputStream;
    volatile boolean stopWorker;

    private View myFragmentView;
    TextView myLabel;

    Thread workerThread;
    byte[] readBuffer;
    int readBufferPosition;

    private List<InventoryItem> inventoryItemList = new ArrayList<>();
    private RecyclerView recyclerView;
    private InventoryAdapter mAdapter;
    private SensorDetailsMap sensorCache;

    private String TAG = "DashboardFragment";

    private ComplexPreferences complexPreferences;

    public DashboardFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!bluetoothAdapter.isEnabled()) {
            Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnOn, 0);
        }
        myFragmentView = inflater.inflate(R.layout.fragment_dashboard, container, false);
        complexPreferences = ComplexPreferences.getComplexPreferences(myFragmentView.getContext().getApplicationContext(), "", 0);
        this.sensorCache = complexPreferences.getObject(PrefManager.SENSOR_CACHE, SensorDetailsMap.class);
        //final SensorDetailsMap sensorCache = (SensorDetailsMap) getArguments().getParcelable(PrefManager.SENSOR_CACHE);

        recyclerView = (RecyclerView) myFragmentView.findViewById(R.id.recycler_view);

        if(mAdapter == null)
            mAdapter = new InventoryAdapter(inventoryItemList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(myFragmentView.getContext().getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(myFragmentView.getContext()));
        recyclerView.setAdapter(mAdapter);

        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(myFragmentView.getContext().getApplicationContext(), recyclerView, new ClickListener() {
            @Override
            public void onClick(View view, int position) {
                InventoryItem inventoryItem = inventoryItemList.get(position);
                Toast.makeText(myFragmentView.getContext().getApplicationContext(), inventoryItem.getItemName() + " is selected!", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(view.getContext(), InventoryActivity.class);
                /*
                Pass your inventory item here.
                 */
                intent.putExtra("sensor", inventoryItem);
                //intent.putExtra(PrefManager.SENSOR_CACHE, sensorCache);

                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                view.getContext().startActivity(intent);
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));

        //List<SensorDetails> sensors = SensorDetailsParser.getSensorDetails(sensorDetails, sensorReadings);
        List<SensorDetails> sensors = new ArrayList<SensorDetails>(sensorCache.getSensorCache().values());

        //prepareMovieData();
        prepareSensorData(sensors);
        initSwipe();

        //Setup to refresh dashboard data on pull down
        setupSwipeRefreshLayout();

        return myFragmentView;
    }

    private void prepareSensorData(List<SensorDetails> sensors) {

        for (int i=0; i<sensors.size(); i++) {
            SensorDetails sensor = sensors.get(i);
            String item = sensors.get(i).getItemName().toLowerCase();

            Date lastSyncDate = sensor.getLastSyncDate();
            String lastSyncDateString = new SimpleDateFormat("HH:mm, MMM dd").format(lastSyncDate);

            InventoryItem invItem = new InventoryItem(sensor.getItemName(), sensor.getMacAddress(), lastSyncDateString);
            invItem.setMaxDistance(sensor.getMaxDistance());
            invItem.setCurrentReading(sensor.getCurrentReading());
            invItem.setReadings(sensor.getGraphFormattedSensorReadings());

            switch (item) {
                case "rice":
                    invItem.setImage(BitmapFactory.decodeResource(getResources(), R.drawable.ic_rice));
                    break;
                case "wheat":
                    invItem.setImage(BitmapFactory.decodeResource(getResources(), R.drawable.ic_wheat));
                    break;
                case "daal":
                    invItem.setImage(BitmapFactory.decodeResource(getResources(), R.drawable.ic_daal));
                    break;
                case "coffee":
                    invItem.setImage(BitmapFactory.decodeResource(getResources(), R.drawable.ic_coffee));
                    break;
                case "sugar":
                    invItem.setImage(BitmapFactory.decodeResource(getResources(), R.drawable.ic_sugar));
                    break;
                case "salt":
                    invItem.setImage(BitmapFactory.decodeResource(getResources(), R.drawable.ic_salt));
                    break;
            }
            inventoryItemList.add(invItem);
        }
        if(mAdapter == null) {
            mAdapter = new InventoryAdapter(inventoryItemList);
        }
        mAdapter.notifyDataSetChanged();
    }

    /*private void prepareMovieData() {
        InventoryItem inventoryItem = new InventoryItem("Rice", "AD:AG:BF:9F:23:QA:BH", "12:30, Aug 15", BitmapFactory.decodeResource(getResources(), R.drawable.ic_wheat));
        inventoryItemList.add(inventoryItem);

        inventoryItem = new InventoryItem("Wheat", "AD:AG:BF:9F:23:QA:BH", "08:27, Aug 15", BitmapFactory.decodeResource(getResources(), R.drawable.ic_rice));
        inventoryItemList.add(inventoryItem);

        inventoryItem = new InventoryItem("Toor Daal", "AD:AG:BF:9F:23:QA:BH", "12:30, Aug 15", BitmapFactory.decodeResource(getResources(), R.drawable.ic_daal));
        inventoryItemList.add(inventoryItem);

        inventoryItem = new InventoryItem("Coffee", "AD:AG:BF:9F:23:QA:BH", "12:30, Aug 15", BitmapFactory.decodeResource(getResources(), R.drawable.ic_coffee));
        inventoryItemList.add(inventoryItem);

        inventoryItem = new InventoryItem("Sugar", "AD:AG:BF:9F:23:QA:BH", "12:30, Aug 15", BitmapFactory.decodeResource(getResources(), R.drawable.ic_sugar));
        inventoryItemList.add(inventoryItem);

        inventoryItem = new InventoryItem("Salt", "AD:AG:BF:9F:23:QA:BH", "12:30, Aug 15", BitmapFactory.decodeResource(getResources(), R.drawable.ic_salt));
        inventoryItemList.add(inventoryItem);

        mAdapter.notifyDataSetChanged();
    }*/

    public interface ClickListener {
        void onClick(View view, int position);

        void onLongClick(View view, int position);
    }

    public static class RecyclerTouchListener implements RecyclerView.OnItemTouchListener {

        private GestureDetector gestureDetector;
        private DashboardFragment.ClickListener clickListener;

        public RecyclerTouchListener(Context context, final RecyclerView recyclerView, final DashboardFragment.ClickListener clickListener) {
            this.clickListener = clickListener;
            gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
                    if (child != null && clickListener != null) {
                        clickListener.onLongClick(child, recyclerView.getChildPosition(child));
                    }
                }
            });
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {

            View child = rv.findChildViewUnder(e.getX(), e.getY());
            if (child != null && clickListener != null && gestureDetector.onTouchEvent(e)) {
                clickListener.onClick(child, rv.getChildPosition(child));
            }
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {
        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }
    }

    private Paint p = new Paint();

    private void initSwipe(){
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public int getMovementFlags(RecyclerView recyclerView,
                                        RecyclerView.ViewHolder viewHolder) {
                int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
                int swipeFlags = ItemTouchHelper.LEFT;
                return makeMovementFlags(dragFlags, swipeFlags);
            }

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();

                //TODO: show custom dialog :- Are you sure you want to delete the sensor A:B:C:D:E:F:G:H
                /**if (direction == ItemTouchHelper.LEFT){
                    mAdapter.removeItem(position);
                }*/
                showGeneralDialogBox("Delete Sensor?",
                        "Are you sure you want to delete the "+inventoryItemList.get(position).getItemName()+" sensor with MacAddress="+inventoryItemList.get(position).getMacAddress(),
                        "OK", position);
                /*
                TODO: 1) call the delete sensor function on the server 2) Delete the sensor from the cache 3) show the progress dialog while sending request to the server 4) remove iem from the adapter
                 */
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {

                Bitmap icon;
                if(actionState == ItemTouchHelper.ACTION_STATE_SWIPE){

                    View itemView = viewHolder.itemView;
                    float height = (float) itemView.getBottom() - (float) itemView.getTop();
                    float width = height / 3;

                    if(dX > 0){
                        /*p.setColor(Color.parseColor("#388E3C"));
                        RectF background = new RectF((float) itemView.getLeft(), (float) itemView.getTop(), dX,(float) itemView.getBottom());
                        c.drawRect(background,p);
                        icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_edit_white);
                        RectF icon_dest = new RectF((float) itemView.getLeft() + width ,(float) itemView.getTop() + width,(float) itemView.getLeft()+ 2*width,(float)itemView.getBottom() - width);
                        c.drawBitmap(icon,null,icon_dest,p);*/
                    } else {
                        p.setColor(Color.parseColor("#D32F2F"));
                        RectF background = new RectF((float) itemView.getRight() + dX, (float) itemView.getTop(),(float) itemView.getRight(), (float) itemView.getBottom());
                        c.drawRect(background,p);
                        icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_delete_white);
                        RectF icon_dest = new RectF((float) itemView.getRight() - 2*width ,(float) itemView.getTop() + width,(float) itemView.getRight() - width,(float)itemView.getBottom() - width);
                        c.drawBitmap(icon,null,icon_dest,p);
                    }
                }
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    private void showGeneralDialogBox(String title, String message, String btnText, final int position) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
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
                showProgressDialog("Deleting sensor. Please wait!");
                sendRequestDeleteSensor(alertDialog, position);
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
    private void sendRequestDeleteSensor(final AlertDialog alertDialog, final int position) {
        StringRequest strReq = new StringRequest(Request.Method.POST,
                Config.URL_DELETE_SENSOR, new Response.Listener<String>() {

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
                         * Success, sensor deleted successfully.
                         */

                        InventoryItem item = mAdapter.removeItem(position);
//                        inventoryItemList.remove(position);
                        alertDialog.hide();
                        mAdapter.notifyDataSetChanged();


                        /**
                         * Get cache details from Sensor and remove them
                         */

                        complexPreferences = ComplexPreferences.getComplexPreferences(getContext(), "", 0);
                        sensorCache = complexPreferences.getObject(PrefManager.SENSOR_CACHE, SensorDetailsMap.class);

                        sensorCache.getSensorCache().remove(item.getMacAddress());
                        complexPreferences.putObject(PrefManager.SENSOR_CACHE, sensorCache);
                        complexPreferences.commit();

                        /**
                         * Hide progress view and show the custom dialog
                         */

                        hideProgressDialog();
                        Toast.makeText(getContext(), "Successfully deleted sensor with item="+item.getItemName()+" and mac="+item.getMacAddress()+"\n", Toast.LENGTH_LONG).show();


                    } else if (errorCode == 1) {
                        Toast.makeText(getContext(), "No sensor exists in the DB with mac="+inventoryItemList.get(position).getMacAddress()+"\n" + message, Toast.LENGTH_LONG).show();

                    } else if (errorCode == 2) {
                        Toast.makeText(getContext(), "No item user exists with email="+PrefManager.getFromPrefs(getContext(), PrefManager.EMAIL_KEY, "")+"\n" + message, Toast.LENGTH_LONG).show();

                        Intent intent = new Intent(getContext(), LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        getContext().startActivity(intent);
                    } else if (errorCode == 3){
                        Toast.makeText(getContext(), "DB Failure for sensorMac="+inventoryItemList.get(position).getMacAddress()+". Please check the error=" + message, Toast.LENGTH_LONG).show();
                    } else if (errorCode == 4) {
                        Toast.makeText(getContext(), "Sensor with mac="+inventoryItemList.get(position).getMacAddress()+" associated with another email id"+ message, Toast.LENGTH_LONG).show();

                        Intent intent = new Intent(getContext(), LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        getContext().startActivity(intent);
                    }
                } catch (JSONException e) {
                    Toast.makeText(getContext(),
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
                Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
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
                params.put("email", PrefManager.getFromPrefs(getContext(), PrefManager.EMAIL_KEY, ""));
                params.put("mac", inventoryItemList.get(position).getMacAddress());

                Log.e(TAG, "Posting params: " + params.toString());

                return params;
            }

        };

        // Adding request to request queue
        MyApplication.getInstance().addToRequestQueue(strReq);
    }

    SwipeRefreshLayout mSwipeRefreshLayout;
    private void setupSwipeRefreshLayout() {
        mSwipeRefreshLayout = (SwipeRefreshLayout) myFragmentView.findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Refresh items
                sendRequestGetUserDetails();
            }
        });
    }

    private void sendRequestGetUserDetails() {
        StringRequest strReq = new StringRequest(Request.Method.POST,
                Config.URL_GET_USER_DETAILS, new Response.Listener<String>() {

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
                         * Success, sensor deleted successfully.
                         */
                        String mobile = responseObj.getString("mobile");
                        String sensorDetails = responseObj.getString("sensorDetails");
                        String sensorReadings = responseObj.getString("sensorReadings");

                        PrefManager.saveToPrefs(getContext(), PrefManager.PHONE_KEY, mobile);
                        PrefManager.saveToPrefs(getContext(), PrefManager.SENSOR_DETAILS, sensorDetails);
                        PrefManager.saveToPrefs(getContext(), PrefManager.SENSOR_READINGS, sensorReadings);
                        PrefManager.saveToPrefs(getContext(), PrefManager.APP_FIRST_RUN, true);

                        /**
                         * Save the cache in the intent variables
                         */
                        List<SensorDetails> sensors = SensorDetailsParser.getSensorDetails(sensorDetails, sensorReadings);
                        SensorDetailsMap cache = new SensorDetailsMap(sensors);

                        ComplexPreferences complexPreferences = ComplexPreferences.getComplexPreferences(getContext(), "", 0);
                        complexPreferences.putObject(PrefManager.SENSOR_CACHE, cache);
                        complexPreferences.commit();

                        sensorCache = complexPreferences.getObject(PrefManager.SENSOR_CACHE, SensorDetailsMap.class);
                        List<SensorDetails> sensorsDataNew = new ArrayList<SensorDetails>(sensorCache.getSensorCache().values());

                        //prepareMovieData();
                        inventoryItemList = new ArrayList<>();
                        prepareSensorData(sensorsDataNew);
                        mAdapter = new InventoryAdapter(inventoryItemList);
                        recyclerView.setAdapter(mAdapter);
                        mAdapter.notifyDataSetChanged();

                        /**
                         * Hide progress view and show the custom dialog
                         */

                        mSwipeRefreshLayout.setRefreshing(false);

                    } else if (errorCode == 1) {
                        Toast.makeText(getContext(), "No item user exists with email="+PrefManager.getFromPrefs(getContext(), PrefManager.EMAIL_KEY, "")+"\n" + message, Toast.LENGTH_LONG).show();

                        Intent intent = new Intent(getContext(), LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        getContext().startActivity(intent);
                    } else if (errorCode == 2){
                        Toast.makeText(getContext(), "DB Failure for email="+PrefManager.getFromPrefs(getContext(), PrefManager.EMAIL_KEY, "")+". Please check the error=" + message, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    Toast.makeText(getContext(),
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
                Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
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
                params.put("email", PrefManager.getFromPrefs(getContext(), PrefManager.EMAIL_KEY, ""));
                Log.e(TAG, "Posting params: " + params.toString());

                return params;
            }

        };

        // Adding request to request queue
        MyApplication.getInstance().addToRequestQueue(strReq);
    }

    private ProgressDialog mProgressDialog;
    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(getActivity());
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
            mProgressDialog = new ProgressDialog(getActivity());
            mProgressDialog.setIndeterminate(true);
        }
        mProgressDialog.setMessage(message);

        mProgressDialog.show();
        //timerDelayRemoveDialog(10000, mProgressDialog);
    }
}
