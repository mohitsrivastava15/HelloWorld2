package com.zeglabs.mohit.helloworld2.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by mohit on 30/10/16.
 */
public class SensorDetailsMap implements Parcelable{
    public Map<String, SensorDetails> getSensorCache() {return sensorCache;}

    public void setSensorCache(Map<String, SensorDetails> sensorCache) {this.sensorCache = sensorCache;}

    Map<String, SensorDetails> sensorCache = new HashMap<String, SensorDetails>();

    public SensorDetailsMap(List<SensorDetails> sensors) {
        if(sensorCache == null) {
            this.sensorCache = new HashMap<String, SensorDetails>();
        }
        for(int i=0; i<sensors.size(); i++) {
            sensorCache.put(sensors.get(i).getMacAddress(), sensors.get(i));
        }
    }

    public SensorDetailsMap() {
        if(sensorCache == null) {
            this.sensorCache = new HashMap<String, SensorDetails>();
        }
    }

    public void addToCache(String key, SensorDetails sensor) {
        sensorCache.put(key, sensor);
    }

    public SensorDetails getFromCache(String key) {
        return sensorCache.get(key);
    }

    /**
     * Setup the parcelable functions
     */
    private static final long serialVersionUID = 1L;

    @Override
    public int describeContents() {
        return 0;
    }
    // write your object's data to the passed-in Parcel
    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(sensorCache.size());
        for(Map.Entry<String, SensorDetails> entry : sensorCache.entrySet()) {
            out.writeString(entry.getKey());
            /**
             * Writing sensor values to the parcel
             */
            SensorDetails sensor = entry.getValue();

            out.writeSerializable(sensor.getCreateDate());
            out.writeInt(sensor.getCurrentReading());
            out.writeString(sensor.getItemName());
            out.writeSerializable(sensor.getLastSyncDate());
            out.writeString(sensor.getMacAddress());
            out.writeInt(sensor.getMaxDistance());
            out.writeString(sensor.getDeviceName());

            /**
             * Now write the readings of the sensor to the parcel
             */
            Map<Date, Integer> readings = sensor.getSensorReadings();
            out.writeInt(readings.size());
            for(Map.Entry<Date, Integer> datapoint : readings.entrySet()) {
                out.writeSerializable(datapoint.getKey());
                out.writeInt(datapoint.getValue());
            }

        }
    }

    // this is used to regenerate your object. All Parcelables must have a CREATOR that implements these two methods
    public static final Parcelable.Creator<SensorDetailsMap> CREATOR = new Parcelable.Creator<SensorDetailsMap>() {
        public SensorDetailsMap createFromParcel(Parcel in) {
            return new SensorDetailsMap(in);
        }

        public SensorDetailsMap[] newArray(int size) {
            return new SensorDetailsMap[size];
        }
    };
    // example constructor that takes a Parcel and gives you an object populated with it's values
    private SensorDetailsMap(Parcel in) {
        int size = in.readInt();
        for(int i=0; i<size; i++) {
            String keyMac = in.readString();
            Date createDate = (Date) in.readSerializable();
            int currentReading = in.readInt();
            String itemName = in.readString();
            Date lastSyncDate = (Date) in.readSerializable();
            String macAddress = in.readString();
            int maxDistance = in.readInt();
            String name = in.readString();

            int readingsSize = in.readInt();
            Map<Date, Integer> readings = new HashMap<Date, Integer>();
            for(int j=0; j<readingsSize; j++) {
                Date key = (Date) in.readSerializable();
                int value = in.readInt();
                readings.put(key, value);
            }
            SensorDetails sensor = new SensorDetails(macAddress, name, itemName, maxDistance, createDate);
            sensor.setCurrentReading(currentReading);
            sensor.setLastSyncDate(lastSyncDate);
            sensor.setSensorReadings(readings);
            sensorCache.put(keyMac, sensor);
        }

    }
}
