package net.khalegh.batsapp.service;

import lombok.Getter;
import lombok.Setter;
import net.khalegh.batsapp.config.MemoryCache;
import net.khalegh.batsapp.smspanel.SmsDotIR;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Configuration
public class Security {

    @Autowired
    Environment environment;

    private static final Logger log = LoggerFactory.getLogger(Security.class);
    private static final String Alphabet = "0123456789";
    @Getter
    @Setter
    private static boolean productionMode;

    public Security(Environment environment) {
        this.environment = environment;
        try {
            String productionModeStatus = environment.getProperty("service.productionMode");
            log.info("production mode from config file -> " + productionModeStatus);
            productionMode = Boolean.parseBoolean(productionModeStatus);
        } catch (Exception e) {
            log.error("can not get productionMode, set default value.");
            productionMode = false;
        }
    }

    private static String createRandomCode(int codeLength, String id) {
        List<Character> temp = id.chars()
                .mapToObj(i -> (char) i)
                .collect(Collectors.toList());
        Collections.shuffle(temp, new SecureRandom());
        return temp.stream()
                .map(Object::toString)
                .limit(codeLength)
                .collect(Collectors.joining());
    }

    private static String getCode() {
        String code = createRandomCode(5, Alphabet);
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
        if (productionMode) {
            if (SmsDotIR.sendVerificationCode(userName, code)) {
                log.info(code + " send to userPhone by " + userName + " done!");
            } else {
                log.info(code + " send to userPhone by " + userName + " failed!");
            }
        } else {
            log.info(code + " send to userPhone by " + userName + " done! (mock service)");
        }
    }

    public static void sendSMSForSignUp(String userName) throws ExecutionException, IOException {
        if (MemoryCache.signUpOTP.asMap().containsKey(userName) &&
                !MemoryCache.signupDoneByOTP.get(userName).isEmpty()) {
            log.info("code ->  " + MemoryCache.signUpOTP.get(userName) +
                    " already sent to " + userName + ", return...");
            return;
        }
        String code = getCode();
        MemoryCache.signUpOTP.put(userName, code);

        if (productionMode) {
            if (SmsDotIR.sendVerificationCode(userName, code)) {
                log.info(code + " send (signup) to userPhone by " + userName + " done!");
            } else {
                log.info(code + " send (signup) to userPhone by " + userName + " failed!");
            }
        } else {
            log.info(code + " send (signup) to userPhone by " + userName + " done! (mock service)");
        }
    }

}
