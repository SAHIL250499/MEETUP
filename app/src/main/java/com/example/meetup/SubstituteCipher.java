package com.example.meetup;

import android.app.Application;

public class SubstituteCipher extends Application {
    static int extractNumbers(String id) {
        char[] list = id.toCharArray();
        int result = 0;
        for (char x : list) {
            if (Character.isDigit(x)) {
                result += Integer.parseInt(String.valueOf(x));
            }
        }
        return result;
    }

    public String encode(String s, String id) {
        StringBuilder sb = new StringBuilder(s.length());
        int shifter = extractNumbers(id);
        for (char c : s.toCharArray()) {
            sb.append((char) ((int) c - shifter));
        }
        return sb.toString();
    }

    public String decode(String s, String id) {
        StringBuilder sb = new StringBuilder(s.length());
        int shifter = extractNumbers(id);
        for (char c : s.toCharArray()) {
            sb.append((char) ((int) c + shifter));
        }
        return sb.toString();
    }
}
