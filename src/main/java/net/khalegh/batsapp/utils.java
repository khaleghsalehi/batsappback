package net.khalegh.batsapp;


import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class utils {
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
