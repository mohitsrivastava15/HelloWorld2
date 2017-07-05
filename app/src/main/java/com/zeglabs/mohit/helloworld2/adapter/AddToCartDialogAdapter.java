package com.zeglabs.mohit.helloworld2.adapter;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zeglabs.mohit.helloworld2.R;

import java.util.List;

import item.AddToCartDailogItem;

/**
 * Created by mohit on 4/9/16.
 */
public class AddToCartDialogAdapter extends RecyclerView.Adapter<AddToCartDialogAdapter.CustomViewHolder> {
    private List<AddToCartDailogItem> cartList;
    private AlertDialog alertDialog;
    private Context mContext;
    private int selectedPos = 0;

    public AddToCartDialogAdapter(Context ctx, List<AddToCartDailogItem> list) {
        this.mContext = ctx;
        this.cartList = list;
    }
    public AddToCartDialogAdapter(Context ctx, List<AddToCartDailogItem> list, AlertDialog alertDialog) {
        this.mContext = ctx;
        this.cartList = list;
        this.alertDialog = alertDialog;
    }


    public class CustomViewHolder extends RecyclerView.ViewHolder {
        protected TextView quantity;
        protected TextView price;

        public CustomViewHolder(View view) {
            super(view);
            this.quantity = (TextView) view.findViewById(R.id.txt_row_quantity);
            this.price = (TextView) view.findViewById(R.id.txt_row_price);
        }
    }
    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_add_to_cart, null);

        CustomViewHolder viewHolder = new CustomViewHolder(view);
        return viewHolder;
    }
    @Override
    public void onBindViewHolder(CustomViewHolder customViewHolder, final int i) {
        customViewHolder.itemView.setSelected(selectedPos == i);
        AddToCartDailogItem feedItem = cartList.get(i);

        //Setting text quantity and price
        customViewHolder.price.setText(feedItem.getPrice());
        customViewHolder.quantity.setText(feedItem.getQuantity());

        /*if(selectedPos == i){
            // Here I am just highlighting the background
            customViewHolder.itemView.setBackgroundColor(Color.rgb(230, 230, 230));
        }else{
            customViewHolder.itemView.setBackgroundColor(Color.TRANSPARENT);
        }*/

        customViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                notifyItemChanged(selectedPos);
                selectedPos = i;
                notifyItemChanged(selectedPos);

                /*
                TODO: Add code here to pass selected quantity to AddToCartActivity.java
                 */

                alertDialog.dismiss();
            }
        });
    }

    @Override
    public int getItemCount() {
        return (null != cartList ? cartList.size() : 0);
    }
}
