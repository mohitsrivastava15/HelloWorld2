package com.zeglabs.mohit.helloworld2.helper;

import java.util.HashMap;
import java.util.Map;

import item.ShoppingCartLocalItem;

/**
 * Created by mohit on 12/11/16.
 */
public class ShoppingCartLocal {
    Map<Integer, ShoppingCartLocalItem> shoppingCart;

    public ShoppingCartLocal() {
        shoppingCart = new HashMap<>();
    }

    public void addToShoppingCart(int itemId, ShoppingCartLocalItem item) {
        shoppingCart.put(itemId, item);
    }

    public ShoppingCartLocalItem removeFromShoppingCart(int itemId) {
        return shoppingCart.remove(itemId);
    }

    public Map<Integer, ShoppingCartLocalItem> getShoppingCart() {return shoppingCart;}
    public void setShoppingCart(Map<Integer, ShoppingCartLocalItem> shoppingCart) {this.shoppingCart = shoppingCart;}
}
