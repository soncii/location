package com.example.location.util;

import java.util.Optional;

public class Util {
    public static Long saveParseLong (String l) {
        try {
            return Long.parseLong(l);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
