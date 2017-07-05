package com.zeglabs.mohit.helloworld2.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zeglabs.mohit.helloworld2.R;
import com.zeglabs.mohit.helloworld2.activity.InventoryActivity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import item.AddToCartDailogItem;
import item.ShowAllSensorReadingsItem;

/**
 * Created by mohit on 6/11/16.
 */
public class ShowAllSensorReadingsAdapter extends RecyclerView.Adapter<ShowAllSensorReadingsAdapter.CustomViewHolder> {
    List<ShowAllSensorReadingsItem> readingsList = new ArrayList<ShowAllSensorReadingsItem>();
    private Context mContext;
    private InventoryActivity activity;
    private int selectedPos = 0;

    public ShowAllSensorReadingsAdapter(Context ctx, InventoryActivity act, List<ShowAllSensorReadingsItem> readingsList) {
        this.readingsList = readingsList;
        this.mContext = ctx;
        this.activity = act;
    }

    public class CustomViewHolder extends RecyclerView.ViewHolder {
        protected TextView date;
        protected TextView reading;

        public CustomViewHolder(View view) {
            super(view);
            this.date = (TextView) view.findViewById(R.id.txt_date);
            this.reading = (TextView) view.findViewById(R.id.txt_reading);
        }
    }
    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_show_all_sensor_readings, null);

        CustomViewHolder viewHolder = new CustomViewHolder(view);
        return viewHolder;
    }
    @Override
    public void onBindViewHolder(CustomViewHolder customViewHolder, final int i) {
        customViewHolder.itemView.setSelected(selectedPos == i);
        ShowAllSensorReadingsItem readingItem = readingsList.get(i);

        //Setting text quantity and price
        customViewHolder.date.setText(readingItem.getDate());
        customViewHolder.reading.setText(readingItem.getReading());

        customViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                notifyItemChanged(selectedPos);
                selectedPos = i;
                //notifyItemChanged(selectedPos);

                /*
                TODO: Add code here to pass selected quantity to AddToCartActivity.java
                 */
            }
        });
    }

    @Override
    public int getItemCount() {
        return (null != readingsList ? readingsList.size() : 0);
    }
}
