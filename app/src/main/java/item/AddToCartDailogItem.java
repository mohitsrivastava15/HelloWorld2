package item;

/**
 * Created by mohit on 6/9/16.
 */
public class AddToCartDailogItem {
    private String quantity;
    private String price;

    public AddToCartDailogItem(String qty, String price) {
        this.quantity=qty;
        this.price=price;
    }

    public String getPrice() {
        return price;
    }
    public void setPrice(String price) {
        this.price = price;
    }

    public String getQuantity() {
        return quantity;
    }
    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }
}
