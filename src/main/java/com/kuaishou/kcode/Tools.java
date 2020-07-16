package com.kuaishou.kcode;

import java.lang.reflect.Field;

public class Tools {
    public static Field f;

    public static void init() {
        try {
            f = String.class.getDeclaredField("value");
        } catch (Exception e) {
            e.printStackTrace();
        }
        f.setAccessible(true);
    }

    public static char[] get_ch(String s) {
        char[] ch = null;
        try {
            ch = (char[]) f.get(s);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return ch;
    }
}
