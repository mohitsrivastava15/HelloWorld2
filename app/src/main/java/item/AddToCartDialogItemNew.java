package item;

import android.content.Context;

import com.zeglabs.mohit.helloworld2.gson.AddToCartItemGSON;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by mohit on 3/11/16.
 */
public class AddToCartDialogItemNew {
    private List<String> qtyPrice;
    private String brandName;
    private String orderItemName;
    private int itemId;

    private String imageUrl;

    private Context appContext;

    public AddToCartDialogItemNew(Context ctx, AddToCartItemGSON gsonItem) {
        this.brandName = gsonItem.getBrandName();
        this.orderItemName = gsonItem.getOrderItemName();
        this.qtyPrice = new ArrayList<String>(Arrays.asList(gsonItem.getQtyPriceString().split("[|]")));
        this.appContext = ctx;
        this.imageUrl = gsonItem.getImageUrl();
        this.itemId = gsonItem.getId();
    }

    public String getImageUrl() {return imageUrl;}
    public void setImageUrl(String imageUrl) {this.imageUrl = imageUrl;}

    public List<String> getQtyPrice() {return qtyPrice;}
    public void setQtyPrice(List<String> qtyPrice) {this.qtyPrice = qtyPrice;}

    public String getBrandName() {return brandName;}
    public void setBrandName(String brandName) {this.brandName = brandName;}

    public String getOrderItemName() {return orderItemName;}
    public void setOrderItemName(String orderItemName) {this.orderItemName = orderItemName;}

    public List<String> getQtyPriceString() {return qtyPrice;}
    public void setQtyPriceString(List<String> qtyPriceString) {this.qtyPrice = qtyPriceString;}

    public int getItemId() {return itemId;}
    public void setItemId(int itemId) {this.itemId = itemId;}

}
