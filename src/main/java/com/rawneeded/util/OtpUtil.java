package com.rawneeded.util;

import java.util.Random;

public  class OtpUtil {

    public static String generateOTP() {
            return String.format("%06d", new Random().nextInt(1000000));
    }

    public static String generateOrderNumber() {
        return String.format("%010d", new Random().nextInt(1000000000));
    }

}
