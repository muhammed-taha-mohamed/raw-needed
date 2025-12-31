package com.rawneeded.util;

import java.util.Random;

public  class OtpUtil {

    public static String generateOTP() {
            return String.format("%06d", new Random().nextInt(1000000));
    }


}
