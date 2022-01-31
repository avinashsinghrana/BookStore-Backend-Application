package com.bridgelabz.bookstore.utility;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class Utils {

    public static final Pattern VALID_EMAIL_ADDRESS_REGEX = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    public static String replaceParams(Map<String, String> hashMap, String template) {
        if (!StringUtils.isEmpty(template)) {
            String message = hashMap.entrySet().stream().filter(map -> map.getValue() != null).reduce(template, (s, e) -> s.replace("${" + e.getKey() + "}", e.getValue()), (s, s2) -> s);
            return message;
        } else
            return null;
    }

    public static boolean isValidEmail(String email) {
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(email);
        return matcher.find();
    }

}
