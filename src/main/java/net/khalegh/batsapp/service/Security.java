package net.khalegh.batsapp.service;

import net.bytebuddy.utility.RandomString;
import net.khalegh.batsapp.config.MemoryCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;

public class Security {
    private static final Logger log = LoggerFactory.getLogger(Security.class);

    private static String getCode(int len) {
        String code = RandomString.make(len);
        log.info("random otp -> " + code);
        return code;
    }

    public static void sendSMS(String userName) throws ExecutionException {
        if (MemoryCache.OTP.asMap().containsKey(userName) &&
                !MemoryCache.OTP.get(userName).isEmpty()) {
            log.info("code ->  " + MemoryCache.OTP.get(userName) +
                    " already sent to " + userName + ", return...");
            return;
        }
        String code = getCode(5);
        MemoryCache.OTP.put(userName, code);
        log.info(code + " send to userPhone by " + userName);
        //todo call third parties sms send API

    }
}
