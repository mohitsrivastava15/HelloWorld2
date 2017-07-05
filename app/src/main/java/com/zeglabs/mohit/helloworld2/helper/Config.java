package com.zeglabs.mohit.helloworld2.helper;

/**
 * Created by mohit on 28/8/16.
 */
public class Config {
    // server URL configuration
    public static final String URL_REQUEST_SMS = "http://ec2-52-66-79-192.ap-south-1.compute.amazonaws.com/request_sms.php";
    public static final String URL_VERIFY_OTP = "http://ec2-52-66-79-192.ap-south-1.compute.amazonaws.com/verify_otp.php";
    public static final String URL_CREATE_SENSOR = "http://ec2-52-66-79-192.ap-south-1.compute.amazonaws.com/create_device.php";
    public static final String URL_GET_ORDER_ITEMS = "http://ec2-52-66-79-192.ap-south-1.compute.amazonaws.com/get_orderitem_details.php";
    public static final String URL_GET_ALL_SENSOR_READINGS = "http://ec2-52-66-79-192.ap-south-1.compute.amazonaws.com/get_all_sensor_readings.php";
    public static final String URL_DELETE_SENSOR = "http://ec2-52-66-79-192.ap-south-1.compute.amazonaws.com/delete_device.php";
    public static final String URL_GET_USER_DETAILS = "http://ec2-52-66-79-192.ap-south-1.compute.amazonaws.com/get_user_details.php";

    // SMS provider identification
    // It should match with your SMS gateway origin
    // You can use  MSGIND, TESTER and ALERTS as sender ID
    // If you want custom sender Id, approve MSG91 to get one
    public static final String SMS_ORIGIN = "ZEGLAB";

    // special character to prefix the otp. Make sure this character appears only once in the sms
    public static final String OTP_DELIMITER = ":";

    public static final String DOWNLOAD_IMAGE = "Image";

    public static final double CONST_CRITICAL_STOCK_LEVEL = 0.2;
}
