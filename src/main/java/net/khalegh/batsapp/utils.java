package net.khalegh.batsapp;


import com.google.common.hash.Hashing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class utils {
    public static final String USER_DIR_SALT = "f1lHYyfDAg65kaBHW0XZ";
    private static final Logger log = LoggerFactory.getLogger(utils.class);


    private static final String extendedArabic = "\u06f0\u06f1\u06f2\u06f3\u06f4\u06f5\u06f6\u06f7\u06f8\u06f9";
    private static final String arabic = "\u0660\u0661\u0662\u0663\u0664\u0665\u0666\u0667\u0668\u0669";

    public static String persianToDecimal(String number) {
        char[] chars = new char[number.length()];
        for (int i = 0; i < number.length(); i++) {
            char ch = number.charAt(i);
            if (ch >= 0x0660 && ch <= 0x0669)
                ch -= 0x0660 - '0';
            else if (ch >= 0x06f0 && ch <= 0x06F9)
                ch -= 0x06f0 - '0';
            chars[i] = ch;
        }
        return new String(chars);
    }


    public static String getUuidHash(String uuid) {
        String dirName = reverseString(USER_DIR_SALT) + uuid + USER_DIR_SALT;
        String hash = Hashing.sha256()
                .hashString(dirName, StandardCharsets.UTF_8)
                .toString();
        log.info("uuid sha256 with salt " + hash);
        return hash;
    }

    private static String reverseString(String str) {
        char[] ch = str.toCharArray();
        StringBuilder rev = new StringBuilder();
        for (int i = ch.length - 1; i >= 0; i--) {
            rev.append(ch[i]);
        }
        return rev.toString();
    }

    public static int getTimeDiff(String now, String before) {
        String[] fractions1 = now.split(":");
        String[] fractions2 = before.split(":");
        Integer hours1 = Integer.parseInt(fractions1[0]);
        Integer hours2 = Integer.parseInt(fractions2[0]);

        Integer minutes1 = Integer.parseInt(fractions1[1]);
        Integer minutes2 = Integer.parseInt(fractions2[1]);

        Integer second1 = Integer.parseInt(fractions1[2]);
        Integer second2 = Integer.parseInt(fractions2[2]);
        int hourDiff = hours1 - hours2;
        int minutesDiff = minutes1 - minutes2;
        int secondDiff = second1 - second2;
        if (secondDiff < 0) {
            secondDiff = 60 + secondDiff;
            minutesDiff--;
        }
        if (minutesDiff < 0) {
            minutesDiff = 60 + minutesDiff;
            hourDiff--;
        }
        if (hourDiff < 0) {
            hourDiff = 24 + hourDiff;
        }

        return (hourDiff * 360) + (minutesDiff * 60) + (secondDiff);
    }

    public static String extractFileNumber(String str) {
        String baseStr = str.substring(str.indexOf("_") + 1);
        String[] tmp = baseStr.split(("_"));
        baseStr = tmp[0];
        return baseStr;
    }

    public static List<String> getTags(String tweetText) {
        List<String> allMatches = new ArrayList<String>();
        Matcher m = Pattern.compile("[#]+([ا-یA-Za-z0-9-_]+)")
                .matcher(tweetText);
        while (m.find()) {
            allMatches.add(m.group().replaceAll("#", ""));
        }
        return allMatches;
    }


}
