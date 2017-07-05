package com.zeglabs.mohit.helloworld2.adapter;

/**
 * Created by mohit on 1/9/16.
 */
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.zeglabs.mohit.helloworld2.R;

import java.util.List;

import item.InventoryItem;
import github.nisrulz.stackedhorizontalprogressbar.StackedHorizontalProgressBar;
import jp.shts.android.library.TriangleLabelView;

public class InventoryAdapter extends RecyclerView.Adapter<InventoryAdapter.MyViewHolder> {

    private List<InventoryItem> inventoryList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView title, year, genre;
        public ImageView image;
        StackedHorizontalProgressBar dataBar;
        TriangleLabelView triangleAlert;

        public MyViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.itemName);
            genre = (TextView) view.findViewById(R.id.macAddress);
            year = (TextView) view.findViewById(R.id.lastUpdateTime);
            image = (ImageView) view.findViewById(R.id.grain_image);
            triangleAlert = (TriangleLabelView) view.findViewById(R.id.triangle_alert);

            this.dataBar = (StackedHorizontalProgressBar) view.findViewById(R.id.stackedhorizontalprogressbar);
        }
    }


    public InventoryAdapter(List<InventoryItem> inventoryList) {
        this.inventoryList = inventoryList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_inventory_list, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        InventoryItem inventoryItem = inventoryList.get(position);
        String title = Character.toTitleCase(inventoryItem.getItemName().charAt(0)) + inventoryItem.getItemName().substring(1);
        holder.title.setText(title);
        holder.genre.setText(inventoryItem.getMacAddress());
        holder.year.setText(inventoryItem.getLastUpdateTime());
        holder.image.setImageBitmap(inventoryItem.getImage());
        holder.triangleAlert.setVisibility(View.GONE);

        int primary_pts = 3;
        int secondary_pts = 7;
        int max = 10;

        holder.dataBar.setMax(inventoryItem.getMaxDistance());
        holder.dataBar.setProgress(inventoryItem.getCurrentReading());
        holder.dataBar.setSecondaryProgress(inventoryItem.getMaxDistance() - inventoryItem.getCurrentReading());
    }

    @Override
    public int getItemCount() {
        return inventoryList.size();
    }

    public InventoryItem removeItem (int position) {
        if(position > inventoryList.size())
            return null;
        InventoryItem item = this.inventoryList.get(position);
        this.inventoryList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, this.inventoryList.size());
        return item;
    }
}
