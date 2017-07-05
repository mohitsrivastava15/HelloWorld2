package item;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by mohit on 1/9/16.
 */
public class InventoryItem implements Parcelable{
    private static final long serialVersionUID = 1L;

    public InventoryItem(String itemName, String macAddress, String lastUpdateTime) {
        this.itemName = itemName;
        this.macAddress = macAddress;
        this.lastUpdateTime = lastUpdateTime;
    }
    public InventoryItem(String itemName, String macAddress, String lastUpdateTime, int maxDistance) {
        this.itemName = itemName;
        this.macAddress = macAddress;
        this.lastUpdateTime = lastUpdateTime;
        this.maxDistance = maxDistance;
    }
    public InventoryItem(String itemName, String macAddress, String lastUpdateTime, int maxDistance, int currentReading) {
        this.itemName = itemName;
        this.macAddress = macAddress;
        this.lastUpdateTime = lastUpdateTime;
        this.maxDistance = maxDistance;
        this.currentReading = currentReading;
    }
    public InventoryItem(String itemName, String macAddress, String lastUpdateTime, Bitmap image) {
        this.itemName = itemName;
        this.macAddress = macAddress;
        this.lastUpdateTime = lastUpdateTime;
        this.image = image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }
    public int getCurrentReading() {
        return currentReading;
    }
    public void setCurrentReading(int currentReading) {
        this.currentReading = currentReading;
    }
    public int getMaxDistance() {
        return this.maxDistance;
    }
    public void setMaxDistance(int maxDistance) {
        this.maxDistance = maxDistance;
    }
    public InventoryItem() {
    }

    public String getItemName() {
        return itemName;
    }
    public void setItemName(String name) {
        this.itemName = name;
    }
    public String getLastUpdateTime() {
        return lastUpdateTime;
    }
    public void setLastUpdateTime(String lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }
    public String getMacAddress() {
        return macAddress;
    }
    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }
    public Bitmap getImage() {
        return image;
    }
    public Map<String, Integer> getReadings() { return readings; }
    public void setReadings(Map readings) { this.readings = readings; }

    @Override
    public int describeContents() {
        return 0;
    }

    private String itemName, macAddress, lastUpdateTime;
    private Bitmap image;
    private int maxDistance;
    private int currentReading;
    private Map<String, Integer> readings;

    // write your object's data to the passed-in Parcel
    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(itemName);
        out.writeString(macAddress);
        out.writeString(lastUpdateTime);
        out.writeInt(maxDistance);
        out.writeInt(currentReading);
        /**
         * Write the readings map to parcelable
         */
        out.writeInt(readings.size());
        for(Map.Entry<String, Integer> entry : readings.entrySet()) {
            out.writeString(entry.getKey());
            out.writeInt(entry.getValue());
        }
    }

    // this is used to regenerate your object. All Parcelables must have a CREATOR that implements these two methods
    public static final Parcelable.Creator<InventoryItem> CREATOR = new Parcelable.Creator<InventoryItem>() {
        public InventoryItem createFromParcel(Parcel in) {
            return new InventoryItem(in);
        }

        public InventoryItem[] newArray(int size) {
            return new InventoryItem[size];
        }
    };
    // example constructor that takes a Parcel and gives you an object populated with it's values
    private InventoryItem(Parcel in) {
        this.itemName = in.readString();
        this.macAddress = in.readString();
        this.lastUpdateTime = in.readString();
        this.maxDistance = in.readInt();
        this.currentReading = in.readInt();

        /**
         * Read values from the parcelable for the map
         */
        this.readings = new LinkedHashMap<String, Integer>();
        int size = in.readInt();
        for(int i=0; i<size; i++) {
            String key = in.readString();
            Integer value = in.readInt();
            this.readings.put(key, value);
        }
    }
}