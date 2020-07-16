package com.kuaishou.kcode;

import java.lang.reflect.Field;


public class HashString {

    static Field f = null;
    int hashcode1;
    int hashcode2;
    int len;
    char[] value = new char[256];

    public HashString() {
        if (f == null) {
            try {
                f = String.class.getDeclaredField("value");
            } catch (Exception e) {
                e.printStackTrace();
            }
            f.setAccessible(true);
        }

        hashcode1 = 0;
        hashcode2 = 0;
        len = 0;
    }

    public void clear() {
        hashcode1 = 0;
        hashcode2 = 0;
        len = 0;
    }

    public HashString append(String s) {
        try {
            int len_ = s.length();
            char[] ch = (char[]) f.get(s);
            for (int i = 0; i < len_; i++) {
                value[len++] = ch[i];
                hashcode1 = (hashcode1 << 8) + hashcode1 + ch[i];
                hashcode2 = (hashcode2 << 8) - hashcode2 + ch[i];
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this;
    }

    public HashString append(char[] ch, int start, int end) {
        for (int i = start; i < end; i++) {
            value[len++] = ch[i];
            hashcode1 = (hashcode1 << 8) + hashcode1 + ch[i];
            hashcode2 = (hashcode2 << 8) - hashcode2 + ch[i];
        }
        return this;
    }

    public HashString append(char c) {
        value[len++] = c;
        hashcode1 = (hashcode1 << 8) + hashcode1 + c;
        hashcode2 = (hashcode2 << 8) - hashcode2 + c;
        return this;
    }

    public String[] split(String s) {
        return new String(value, 0, len).split(s);
    }


    @Override
    public int hashCode() {
        return (int) (hashcode1 % 1000000007);
    }

    @Override
    public boolean equals(Object obj) {
        return hashcode2 == ((HashString) obj).hashcode2 && hashcode1 == ((HashString) obj).hashcode1;
    }

    public final void add4(String a, String b, String type, String c) {

        try {
            int len_a = a.length();
            int len_b = b.length();
            int len_c = c.length();
            char[] aa = (char[]) f.get(a);
            char[] bb = (char[]) f.get(b);
            char[] cc = (char[]) f.get(c);

            hashcode1 = type.charAt(0);
            hashcode2 = type.charAt(0);
            for (int i = 0; i < len_a; i++) {
                hashcode1 = (hashcode1 << 8) + hashcode1 + aa[i];
                hashcode2 = (hashcode2 << 8) - hashcode2 + aa[i];
            }
            for (int i = 0; i < len_b; i++) {
                hashcode1 = (hashcode1 << 8) + hashcode1 + bb[i];
                hashcode2 = (hashcode2 << 8) - hashcode2 + bb[i];
            }
            for (int i = 0; i < len_c; i++) {
//                hashcode1 = (hashcode1 << 8) + hashcode1 + cc[i];
                hashcode2 = (hashcode2 << 8) - hashcode2 + cc[i];
            }
            hashcode1 = c.charAt(11) + (hashcode1 << 8) + hashcode1;
            hashcode1 = c.charAt(12) + (hashcode1 << 8) + hashcode1;
            hashcode1 = c.charAt(14) + (hashcode1 << 8) + hashcode1;
            hashcode1 = c.charAt(15) + (hashcode1 << 8) + hashcode1;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
