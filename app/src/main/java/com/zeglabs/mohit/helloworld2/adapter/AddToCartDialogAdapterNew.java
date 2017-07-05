package com.zeglabs.mohit.helloworld2.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.jaredrummler.materialspinner.MaterialSpinner;
import com.zeglabs.mohit.helloworld2.R;
import com.zeglabs.mohit.helloworld2.activity.InventoryActivity;
import com.zeglabs.mohit.helloworld2.helper.ComplexPreferences;
import com.zeglabs.mohit.helloworld2.helper.PrefManager;
import com.zeglabs.mohit.helloworld2.helper.ShoppingCartLocal;

import java.io.InputStream;
import java.io.Serializable;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import item.AddToCartDialogItemNew;
import item.ShoppingCartLocalItem;

/**
 * Created by mohit on 3/11/16.
 */
public class AddToCartDialogAdapterNew extends RecyclerView.Adapter<AddToCartDialogAdapterNew.CustomViewHolder> {
    private List<AddToCartDialogItemNew> cartList;
    private Context mContext;
    private int selectedPos = 0;
    private InventoryActivity activity;
    ComplexPreferences complexPreferences;
    ShoppingCartLocal shoppingCart = null;

    private int cartSum = 0;


    public AddToCartDialogAdapterNew(Context ctx, InventoryActivity invActivity, List<AddToCartDialogItemNew> cartItems) {
        this.mContext = ctx;
        this.cartList = cartItems;
        this.activity = invActivity;
        complexPreferences = ComplexPreferences.getComplexPreferences(ctx, "", 0);

        shoppingCart = complexPreferences.getObject(PrefManager.SHOPPING_CART, ShoppingCartLocal.class);
        if(shoppingCart == null) {
            shoppingCart = new ShoppingCartLocal();
        }
    }

    public class CustomViewHolder extends RecyclerView.ViewHolder {
        protected ImageView imgView;
        protected TextView txtBrandName;
        protected TextView txtProductName;
        protected MaterialSpinner spinnerQtyPrice;
        protected TextView txtCartQuantity;
        protected ImageView imgCartPlus;
        protected ImageView imgCartMinus;

        public CustomViewHolder(View view) {
            super(view);
            this.imgView = (ImageView) view.findViewById((R.id.img_orderItem));
            this.txtBrandName = (TextView) view.findViewById(R.id.txt_brand_name);
            this.txtProductName = (TextView) view.findViewById(R.id.txt_product_name);
            this.spinnerQtyPrice = (MaterialSpinner) view.findViewById(R.id.spinner_qtyprice);
            this.txtCartQuantity = (TextView) view.findViewById(R.id.txt_cart_qty);
            this.imgCartPlus = (ImageView) view.findViewById(R.id.img_cart_plus);
            this.imgCartMinus = (ImageView) view.findViewById(R.id.img_cart_minus);
        }
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_add_to_cart_new, null);

        CustomViewHolder viewHolder = new CustomViewHolder(view);
        return viewHolder;
    }
    @Override
    public void onBindViewHolder(final CustomViewHolder customViewHolder, final int i) {
        customViewHolder.itemView.setSelected(selectedPos == i);
        final AddToCartDialogItemNew feedItem = cartList.get(i);

        //Setting text quantity and price
        String downloadUrl = feedItem.getImageUrl();

        /**
         * Download image from the ec2 server
         */
        DownloadImage downloadImage = new DownloadImage(this.mContext, customViewHolder.imgView);
        downloadImage.execute(downloadUrl);

        //customViewHolder.imgView.setImageBitmap(feedItem.getImgOrderItem());
        customViewHolder.txtBrandName.setText(feedItem.getBrandName());
        customViewHolder.txtProductName.setText(feedItem.getOrderItemName());

        customViewHolder.spinnerQtyPrice.setItems(feedItem.getQtyPrice());
        customViewHolder.spinnerQtyPrice.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener<String>() {

            @Override public void onItemSelected(MaterialSpinner view, int position, long id, String item) {
                //Snackbar.make(view, "Clicked " + item, Snackbar.LENGTH_LONG).show();
            }
        });

        customViewHolder.imgCartPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String currentString = customViewHolder.txtCartQuantity.getText().toString();
                int currentReading = Integer.parseInt(currentString);
                currentReading++;
                cartSum++;
                customViewHolder.txtCartQuantity.setText(Integer.toString(currentReading));
                if(currentReading>0)
                    customViewHolder.txtCartQuantity.setTextColor(Color.BLACK);
                /**
                 * TODO: code to add items to local cart
                 */
                int id = feedItem.getItemId();
                String brandName = feedItem.getBrandName();
                String orderItemName = feedItem.getOrderItemName();
                String imageUrl = feedItem.getImageUrl();
                int qty = currentReading;
                String qtyPrice = (String) customViewHolder.spinnerQtyPrice.getItems().get(customViewHolder.spinnerQtyPrice.getSelectedIndex());
                adjustTempShoppingCart(id, brandName, orderItemName, imageUrl, qtyPrice, qty);
            }
        });

        customViewHolder.imgCartMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String currentString = customViewHolder.txtCartQuantity.getText().toString();
                int currentReading = Integer.parseInt(currentString);
                currentReading = currentReading >=1? currentReading-1 : currentReading;
                cartSum = cartSum >=1? cartSum-1 : cartSum;
                customViewHolder.txtCartQuantity.setText(Integer.toString(currentReading));
                if(currentReading==0)
                    customViewHolder.txtCartQuantity.setTextColor(Color.parseColor("#EEEEEE"));
                /**
                 * TODO: code to add items to local cart
                 */
                int id = feedItem.getItemId();
                String brandName = feedItem.getBrandName();
                String orderItemName = feedItem.getOrderItemName();
                String imageUrl = feedItem.getImageUrl();
                int qty = currentReading;
                String qtyPrice = (String) customViewHolder.spinnerQtyPrice.getItems().get(customViewHolder.spinnerQtyPrice.getSelectedIndex());
                adjustTempShoppingCart(id, brandName, orderItemName, imageUrl, qtyPrice, qty);
            }
        });

        if(shoppingCart != null) {
            if(shoppingCart.getShoppingCart().get(feedItem.getItemId())!=null) {
                customViewHolder.txtCartQuantity.setText(Integer.toString(shoppingCart.getShoppingCart().get(feedItem.getItemId()).getQty()));
                customViewHolder.txtCartQuantity.setTextColor(Color.BLACK);
            }
        }
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
                Deprecated: TODO: Add code here to pass selected quantity to AddToCartActivity.java
                 */
            }
        });
    }

    private void adjustTempShoppingCart(int itemId, String brandName, String orderItemName, String imgUrl, String qtyPrice, int qty) {
        ShoppingCartLocalItem item = new ShoppingCartLocalItem(itemId, brandName, orderItemName, imgUrl, qtyPrice, qty);
        if(shoppingCart.getShoppingCart().get(itemId) == null) {
            shoppingCart.getShoppingCart().put(itemId, item);
            complexPreferences.putObject(PrefManager.SHOPPING_CART, shoppingCart);
            complexPreferences.commit();
            return;
        }
        if(qty>0) {
            shoppingCart.getShoppingCart().get(itemId).setQty(qty);
        } else if (qty == 0) {
            shoppingCart.getShoppingCart().remove(itemId);
        }
        complexPreferences.putObject(PrefManager.SHOPPING_CART, shoppingCart);
        complexPreferences.commit();
    }

    @Override
    public int getItemCount() {
        return (null != cartList ? cartList.size() : 0);
    }

    class DownloadImage extends AsyncTask<String, Void, Bitmap> implements Serializable{
        private static final long serialVersionUID = 1L;
        Context context = null;
        ImageView imageView;
        Bitmap image = null;

        public DownloadImage(Context context, ImageView imageView) {
            this.context = context;
            this.imageView = imageView;
            //Toast.makeText(context, "Please wait, it may take a few minute...", Toast.LENGTH_SHORT).show();

        }

        public DownloadImage(Context context, CircleImageView imageView, Bitmap image) {
            this.context = context;
            this.imageView = imageView;
            this.image = image;
            imageView.setImageBitmap(this.image);
        }

        public Bitmap doInBackground(String... urls) {
            String imageURL = urls[0];
            Bitmap bimage = null;
            try {
                InputStream in = new java.net.URL(imageURL).openStream();
                bimage = BitmapFactory.decodeStream(in);

            } catch (Exception e) {
                Log.e("Error Message", e.getMessage());
                e.printStackTrace();
            }
            this.image = bimage;
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    imageView.setImageBitmap(image);
                }
            });
            return bimage;
        }

        public Bitmap getImage() {
            return this.image;
        }

        public void setImage(Bitmap result) {
            imageView.setImageBitmap(result);
        }
    }
    public int getCartSum() {return cartSum;}
}
