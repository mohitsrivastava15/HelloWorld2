package com.zeglabs.mohit.helloworld2.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.zeglabs.mohit.helloworld2.R;
import com.zeglabs.mohit.helloworld2.activity.AddDeviceActivityBLE;
import com.zeglabs.mohit.helloworld2.activity.CalibrateDeviceActivity;
import com.zeglabs.mohit.helloworld2.activity.CalibrateDeviceActivityBLE;
import com.zeglabs.mohit.helloworld2.helper.Config;
import com.zeglabs.mohit.helloworld2.helper.PrefManager;

import java.util.ArrayList;
import java.util.List;

import item.PopularSearchItem;
import item.QuickSearchItem;

/**
 * Created by mohit on 8/9/16.
 */
public class PopularSearchAdapter extends RecyclerView.Adapter<PopularSearchAdapter.ViewHolder> {
    private List<PopularSearchItem> popularSearchItemList = new ArrayList<PopularSearchItem>();
    private Context context;
    private static int selectedPos = -1;

    public PopularSearchAdapter(Context context, List<PopularSearchItem> list) {
        this.context = context;
        this.popularSearchItemList = list;
    }
    @Override
    public PopularSearchAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_search_popular, viewGroup, false);
        return new ViewHolder(view);
    }
    @Override
    public void onBindViewHolder(final PopularSearchAdapter.ViewHolder viewHolder, final int i) {
        viewHolder.txt_grain.setText(popularSearchItemList.get(i).getTxtGrain());
        viewHolder.ic_grain.setImageBitmap(popularSearchItemList.get(i).getImgGrain());

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                notifyItemChanged(selectedPos);
                selectedPos = i;
                notifyItemChanged(selectedPos);
                String itemName = popularSearchItemList.get(i).getTxtGrain();
                Snackbar.make(view, "Item "+itemName+" selected", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                /*
                TODO: Add code here to pass selected grain category to the server for adding to DB
                 */
                Intent intent2 = new Intent(view.getContext(), CalibrateDeviceActivityBLE.class);
                intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent2.putExtra(PrefManager.SENSORCREATE_ITEM, itemName);
                intent2.putExtra(PrefManager.BLUETOOTH_DEVICE, AddDeviceActivityBLE.mDevice);
                view.getContext().startActivity(intent2);
            }
        });
    }
    @Override
    public int getItemCount() {
        return popularSearchItemList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private TextView txt_grain;
        private ImageView ic_grain;

        public ViewHolder(View view) {
            super(view);

            txt_grain = (TextView) view.findViewById(R.id.txt_grain);
            ic_grain = (ImageView) view.findViewById(R.id.ic_grain);
        }
    }
}
