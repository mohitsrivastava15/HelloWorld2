package item;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by mohit on 6/11/16.
 */
public class ShowAllSensorReadingsItem {
    private String date;
    private String reading;

    public String getReading() {return reading;}
    public void setReading(String reading) {this.reading = reading;}

    public String getDate() {return date;}
    public void setDate(String date) {this.date = date;}

    public ShowAllSensorReadingsItem(String date, String reading) {
        this.date = date;
        this.reading = reading;
    }
    public ShowAllSensorReadingsItem(Date date, int reading) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM");
        this.date = dateFormat.format(date);
        this.reading = String.valueOf(reading);
    }
}
