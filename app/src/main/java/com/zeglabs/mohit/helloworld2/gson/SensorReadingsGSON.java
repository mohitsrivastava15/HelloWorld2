package com.zeglabs.mohit.helloworld2.gson;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

/**
 * Created by mohit on 5/11/16.
 */
public class SensorReadingsGSON {

    @SerializedName("date_created")
    private Date dateCreated;

    @SerializedName("reading")
    private int reading;

    public Date getDateCreated() {return dateCreated;}
    public void setDateCreated(Date dateCreated) {this.dateCreated = dateCreated;}

    public int getReading() {return reading;}
    public void setReading(int reading) {this.reading = reading;}

    public String toString() {
        return "{"+this.dateCreated+", "+this.reading+"}";
    }
}
