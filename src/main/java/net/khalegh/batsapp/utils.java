package net.khalegh.batsapp;


import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class utils {
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
