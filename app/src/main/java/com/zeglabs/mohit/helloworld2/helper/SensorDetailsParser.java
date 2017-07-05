package com.zeglabs.mohit.helloworld2.helper;

import com.zeglabs.mohit.helloworld2.model.SensorDetails;

import org.json.JSONArray;
import org.json.JSONException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by mohit on 26/10/16.
 */
public class SensorDetailsParser {

    public static List<SensorDetails> getSensorDetails(String sensorDetails, String sensorReadings) {
        try {
            JSONArray sensorDetailsArray = new JSONArray(sensorDetails);
            //JSONArray sensorDetailsArray = obj.getJSONArray("sensorDetails");
            JSONArray sensorReadingsArray = new JSONArray(sensorReadings);

            Map<Integer, Map> readings = getSensorReadingsSortedMap(sensorReadingsArray);

            List<SensorDetails> sensors = new ArrayList<SensorDetails>();

            for(int i=0; i<sensorDetailsArray.length(); i++) {
                int id = sensorDetailsArray.getJSONObject(i).getInt("id");
                String macAddress = sensorDetailsArray.getJSONObject(i).getString("mac_address");
                String sensorName = sensorDetailsArray.getJSONObject(i).getString("sensor_name");
                String itemName = sensorDetailsArray.getJSONObject(i).getString("item_name");
                int maxDistance = sensorDetailsArray.getJSONObject(i).getInt("max_distance");
                String dateCreatedString = sensorDetailsArray.getJSONObject(i).getString("date_created");

                Date dateCreated = getDateFromPHPString(dateCreatedString);

                SensorDetails sensor = new SensorDetails(macAddress, sensorName, itemName, maxDistance, dateCreated);
                /**
                 * Add the readings array to sensors
                 */
                sensor.setSensorReadings(readings.get(id));

                sensors.add(sensor);
            }
            return sensors;
        } catch (JSONException e) {
            return null;
        }

    }

    private static Map getSensorReadingsSortedMap(JSONArray sensorReadingsArray) {
        Map<Integer, Map> readings = new TreeMap();
        for(int i=0; i<sensorReadingsArray.length(); i++) {
            try {
                int id = sensorReadingsArray.getJSONObject(i).getInt("id");
                JSONArray readingsArray = sensorReadingsArray.getJSONObject(i).getJSONArray("sensor_readings");

                //Map timeSeries = new TreeMap(Collections.reverseOrder());
                Map timeSeries = new TreeMap();

                for(int j=0; j<readingsArray.length(); j++) {
                    String dateCreatedString = readingsArray.getJSONObject(j).getString("date_created");
                    int reading = readingsArray.getJSONObject(j).getInt("reading");

                    Date date = getDateFromPHPString(dateCreatedString);

                    timeSeries.put(date, reading);
                }
                readings.put(id, timeSeries);
            } catch (JSONException e) {

            }

        }
        return readings;
    }

    private static Date getDateFromPHPString(String dateCreatedString) {
        SimpleDateFormat dateParser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date dateCreated = null;
        try {
            return dateParser.parse(dateCreatedString);
        } catch (ParseException e1) {
            return null;
        }
    }
}
