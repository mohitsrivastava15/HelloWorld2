package com.zeglabs.mohit.helloworld2.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zeglabs.mohit.helloworld2.R;

import java.util.List;

import item.ReportProblemDialogItem;

/**
 * Created by mohit on 4/9/16.
 */
public class ReportProblemAdapter extends RecyclerView.Adapter<ReportProblemAdapter.CustomViewHolder> {
    public static int position;
    private List<ReportProblemDialogItem> problemList;
    private AlertDialog alertDialog;
    private Context mContext;
    private int selectedPos = 0;

    public ReportProblemAdapter(Context ctx, List<ReportProblemDialogItem> list) {
        this.mContext = ctx;
        this.problemList = list;
    }
    public ReportProblemAdapter(Context ctx, List<ReportProblemDialogItem> list, AlertDialog alertDialog) {
        this.mContext = ctx;
        this.problemList = list;
        this.alertDialog = alertDialog;
    }


    public class CustomViewHolder extends RecyclerView.ViewHolder {
        protected TextView category;

        public CustomViewHolder(View view) {
            super(view);
            this.category = (TextView) view.findViewById(R.id.txt_category);
        }
    }
    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_report_problem, null);

        CustomViewHolder viewHolder = new CustomViewHolder(view);
        return viewHolder;
    }
    @Override
    public void onBindViewHolder(CustomViewHolder customViewHolder, final int i) {
        position = i;
        customViewHolder.itemView.setSelected(selectedPos == i);
        ReportProblemDialogItem feedItem = problemList.get(i);

        //Setting text quantity and price
        customViewHolder.category.setText(feedItem.getCategory());

        if(selectedPos == i){
            // Here I am just highlighting the background
            customViewHolder.itemView.setBackgroundColor(Color.rgb(230, 230, 230));
        }else{
            customViewHolder.itemView.setBackgroundColor(Color.TRANSPARENT);
        }

        customViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                notifyItemChanged(selectedPos);
                selectedPos = i;
                notifyItemChanged(selectedPos);
            }
        });
    }

    @Override
    public int getItemCount() {
        return (null != problemList ? problemList.size() : 0);
    }
}
