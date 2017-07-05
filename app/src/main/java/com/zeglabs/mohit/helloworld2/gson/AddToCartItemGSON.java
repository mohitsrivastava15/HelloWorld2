package com.zeglabs.mohit.helloworld2.gson;

import android.graphics.Bitmap;

import com.google.gson.annotations.SerializedName;

/**
 * Created by mohit on 3/11/16.
 */
public class AddToCartItemGSON {
    @SerializedName("brand_name")
    private String brandName;

    @SerializedName("product_name")
    private String orderItemName;

    @SerializedName("qty_price")
    private String qtyPriceString;

    @SerializedName("image_url")
    private String imageUrl;

    @SerializedName("id")
    private int id;

    public String getImageUrl() {return imageUrl;}
    public void setImageUrl(String imageUrl) {this.imageUrl = imageUrl;}

    public String getQtyPriceString() {return qtyPriceString;}
    public void setQtyPriceString(String qtyPriceString) {this.qtyPriceString = qtyPriceString;}

    public String getOrderItemName() {return orderItemName;}
    public void setOrderItemName(String orderItemName) {this.orderItemName = orderItemName;}

    public String getBrandName() {return brandName;}
    public void setBrandName(String brandName) {this.brandName = brandName;}

    public int getId() {return id;}
    public void setId(int id) {this.id = id;}

}
