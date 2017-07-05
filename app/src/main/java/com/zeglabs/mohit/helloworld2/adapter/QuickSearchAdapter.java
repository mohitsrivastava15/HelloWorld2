package com.zeglabs.mohit.helloworld2.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
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
import com.zeglabs.mohit.helloworld2.helper.PrefManager;

import java.util.ArrayList;
import java.util.List;

import item.QuickSearchItem;

/**
 * Created by mohit on 8/9/16.
 */
public class QuickSearchAdapter extends RecyclerView.Adapter<QuickSearchAdapter.ViewHolder> {
    private List<QuickSearchItem> quickSearchItemList = new ArrayList<QuickSearchItem>();
    private Context context;
    private static int selectedPos = -1;

    public QuickSearchAdapter(Context context, List<QuickSearchItem> list) {
        this.context = context;
        this.quickSearchItemList = list;
    }
    @Override
    public QuickSearchAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_search_quick, viewGroup, false);
        return new ViewHolder(view);
    }
    @Override
    public void onBindViewHolder(QuickSearchAdapter.ViewHolder viewHolder, final int i) {
        viewHolder.txt_grain.setText(quickSearchItemList.get(i).getTxtGrain());
        viewHolder.ic_grain.setImageBitmap(quickSearchItemList.get(i).getImgGrain());

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                notifyItemChanged(selectedPos);
                selectedPos = i;
                notifyItemChanged(selectedPos);
                String itemName = quickSearchItemList.get(i).getTxtGrain();
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
        return quickSearchItemList.size();
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
