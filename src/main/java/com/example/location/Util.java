package com.example.location;

import java.util.Optional;

public class Util {
    public static Long saveParseLong (String l) {
        return  Optional.ofNullable(l)
                .map(Long::parseLong)
                .orElse(null);
    }
}
