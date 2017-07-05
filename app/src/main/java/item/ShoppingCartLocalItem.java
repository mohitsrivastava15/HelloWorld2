package item;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by mohit on 12/11/16.
 */
public class ShoppingCartLocalItem {
    private int orderItemId;

    private int qty;
    private String qtyPrice;
    private Date dateAdded;
    private String itemName;

    private String brandName;
    private String imageUrl;

    public ShoppingCartLocalItem(int orderItemId, String brandName, String itemName, String imgUrl, String qtyPrice, int qty) {
        this.orderItemId = orderItemId;
        this.qtyPrice = qtyPrice;
        this.dateAdded = Calendar.getInstance().getTime();
        this.brandName = brandName;
        this.itemName = itemName;
        this.imageUrl = imgUrl;
        this.qty = qty;
    }

    public String toString() {
        return "("+this.brandName+": "+this.itemName+", qtyPrice="+qtyPrice+", qty="+qty+")";
    }

    public Date getDateAdded() {return dateAdded;}
    public void setDateAdded(Date dateAdded) {this.dateAdded = dateAdded;}

    public int getOrderItemId() {return orderItemId;}
    public void setOrderItemId(int orderItemId) {this.orderItemId = orderItemId;}

    public String getItemName() {return itemName;}
    public void setItemName(String itemName) {this.itemName = itemName;}

    public String getBrandName() {return brandName;}
    public void setBrandName(String brandName) {this.brandName = brandName;}

    public String getImageUrl() {return imageUrl;}
    public void setImageUrl(String imageUrl) {this.imageUrl = imageUrl;}

    public String getQtyPrice() {return qtyPrice;}
    public void setQtyPrice(String qtyPrice) {this.qtyPrice = qtyPrice;}

    public void setQty(int qty) {this.qty = qty;}
    public int getQty() {return qty;}
}
