package com.example.location.util;

public class Util {
    public static String hideEmail(String email) {
        if (email == null || email.isEmpty()) {
            return "";
        }

        int atIndex = email.indexOf('@');
        if (atIndex <= 1) {
            return email;
        }

        String username = email.substring(0, atIndex);
        String domain = email.substring(atIndex + 1);

        if (username.length() <= 3) {
            return username + "......" + domain;
        } else {
            String hiddenUsername = username.substring(0, 3) + "......";
            return hiddenUsername + domain;
        }
    }
    public static boolean isValidLong(String str) {
        return str.matches("-?\\d+");
    }

    public enum ObjectType {
        USER, LOCATION, ACCESS
    }

    public enum ActionType {
        CREATED, UPDATED, DELETED
    }
}
