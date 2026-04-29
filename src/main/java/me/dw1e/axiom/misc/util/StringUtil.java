package me.dw1e.axiom.misc.util;

import java.util.ArrayList;
import java.util.List;

public final class StringUtil {

    public static List<String> filterStartingWith(List<String> options, String input) {
        List<String> result = new ArrayList<>();
        String lowerInput = input.toLowerCase();

        for (String option : options) {
            if (option.toLowerCase().startsWith(lowerInput)) {
                result.add(option);
            }
        }

        return result;
    }
}
