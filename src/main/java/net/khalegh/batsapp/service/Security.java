package net.khalegh.batsapp.service;

import net.khalegh.batsapp.config.MemoryCache;
import net.khalegh.batsapp.smspanel.SmsDotIR;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class Security {
    private static final Logger log = LoggerFactory.getLogger(Security.class);
    private static final String Alphabet="0123456789";


    private static String createRandomCode(int codeLength, String id) {
        List<Character> temp = id.chars()
                .mapToObj(i -> (char)i)
                .collect(Collectors.toList());
        Collections.shuffle(temp, new SecureRandom());
        return temp.stream()
                .map(Object::toString)
                .limit(codeLength)
                .collect(Collectors.joining());
    }
    private static String getCode() {
        String code =createRandomCode(5,Alphabet);
        log.info("random otp -> " + code);
        return code;
    }

    public static void sendSMS(String userName) throws ExecutionException, IOException {
        if (MemoryCache.OTP.asMap().containsKey(userName) &&
                !MemoryCache.OTP.get(userName).isEmpty()) {
            log.info("code ->  " + MemoryCache.OTP.get(userName) +
                    " already sent to " + userName + ", return...");
            return;
        }
        String code = getCode();
        MemoryCache.OTP.put(userName, code);
        if (SmsDotIR.sendVerificationCode(userName, code)) {
            log.info(code + " send to userPhone by " + userName + " done!");
        } else {
            log.info(code + " send to userPhone by " + userName + " failed!");
        }
    }
}
