package com.zeglabs.mohit.helloworld2.helper;

/**
 * Created by mohit on 31/8/16.
 */
public class ValidationHelper {
    /**
     * Regex to validate the mobile number
     * mobile number should be of 10 digits length
     *
     * @param mobile
     * @return
     */
    public static boolean isValidPhoneNumber(String mobile) {
        String regEx = "^[0-9]{10}$";
        return mobile.matches(regEx);
    }

    /**
     * Regex to validate the mobile number
     * mobile number should be of 10 digits length
     *
     * @param otp
     * @return
     */
    public static boolean isValidOtp(String otp) {
        String regEx = "^[0-9]{6}$";
        return otp.matches(regEx);
    }
}
