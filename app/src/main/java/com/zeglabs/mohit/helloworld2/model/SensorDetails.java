package com.zeglabs.mohit.helloworld2.model;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by mohit on 26/10/16.
 */
public class SensorDetails {

    String macAddress;


    String deviceName;

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    String itemName;
    int maxDistance;
    int currentReading;
    Date lastSyncDate;
    Date createDate;

    Map<Date, Integer> readings = new TreeMap();

    public SensorDetails(String macAddress, String deviceName, String itemName, int maxDistance, Date createDate) {
        this.macAddress = macAddress;
        this.deviceName = deviceName;
        this.itemName = itemName;
        this.maxDistance = maxDistance;
        this.createDate = createDate;
        this.setSensorReadings(this.readings);
    }

    public void setSensorReadings(Map<Date, Integer> reading) {
        this.readings = reading;
        if(reading.size()>0) {
            this.lastSyncDate = Collections.max(reading.keySet());
            this.currentReading = reading.get(this.lastSyncDate);
        } else {
            this.lastSyncDate = this.createDate;
        }
    }

    public Map getSensorReadings() {
        return this.readings;
    }

    public Map<String, Integer> getGraphFormattedSensorReadings() {
        Map<String, Integer> map = new LinkedHashMap<String, Integer>();
        for(Map.Entry<Date, Integer> entry : readings.entrySet()) {
            String date = new SimpleDateFormat("dd-MMM").format(entry.getKey());
            map.put(date, entry.getValue());
        }
        return map;
    }


    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public int getCurrentReading() {
        return currentReading;
    }

    public void setCurrentReading(int currentReading) {
        this.currentReading = currentReading;
    }

    public int getMaxDistance() {
        return maxDistance;
    }

    public void setMaxDistance(int maxDistance) {
        this.maxDistance = maxDistance;
    }

    public Date getLastSyncDate() {
        return lastSyncDate;
    }

    public void setLastSyncDate(Date lastSyncDate) {
        this.lastSyncDate = lastSyncDate;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public String getDeviceName() {return deviceName;}

    public void setDeviceName(String name) {this.deviceName = name;}

}
