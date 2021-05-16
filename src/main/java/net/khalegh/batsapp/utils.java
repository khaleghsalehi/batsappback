package net.khalegh.batsapp;


import net.khalegh.batsapp.contorl.WebView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class utils {
    private static final Logger log = LoggerFactory.getLogger(utils.class);

    public static void notifyParent(String uuid) {
        //todo implement
        log.error("=======  notify parent implementation ===============");
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
