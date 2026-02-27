package com.resudex.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExperienceExtractor {

    public static int extractYears(String text) {
        Pattern pattern = Pattern.compile("(\\d+)\\+?\\s*(years|year|yrs|yr)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(text);

        int max = 0;
        while (matcher.find()) {
            int years = Integer.parseInt(matcher.group(1));
            max = Math.max(max, years);
        }
        return max;
    }
}