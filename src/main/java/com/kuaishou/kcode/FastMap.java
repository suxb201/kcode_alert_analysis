//package com.kuaishou.kcode;
//
//import sun.misc.Unsafe;
//
//import java.lang.reflect.Field;
//import java.util.ArrayList;
//import java.util.Collection;
//
//public class FastMap {
//    int MO;
//    public Collection<String>[] value;
//    public long[] check;
//    int[] used;
//    int[] p31;
//    int base_time;
//    private static final int add = 5;
//    int[] x600;
//    int[] x60;
//    int[] x10;
//    Unsafe unsafe;
//    int base;
//    int scale;
//
//    public FastMap(int base_time) {
//        this.base_time = base_time;
//        this.MO = KcodeAlertAnalysisImpl.MO;
//        this.value = new ArrayList[this.MO * 256 + 256];
//        for (int i = 0; i < this.MO * 256 + 256; i++) this.value[i] = new ArrayList<>();
//        this.p31 = new int[100];
//        p31[0] = 1;
//        for (int i = 1; i < 100; i++) p31[i] = p31[i - 1] * 31;
//        x600 = new int[128];
//        x60 = new int[128];
//        x10 = new int[128];
//        for (int i = '0'; i <= '9'; i++) {
//            x600[i] = i * 600;
//            x60[i] = i * 60;
//            x10[i] = i * 10;
//        }
//
//        try {
//            Field field = Unsafe.class.getDeclaredField("theUnsafe");
//            field.setAccessible(true);
//            unsafe = (Unsafe) field.get(null);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        base = unsafe.arrayBaseOffset(this.value.getClass());
//        scale = unsafe.arrayIndexScale(this.value.getClass());
//        scale = 31 - Integer.numberOfLeadingZeros(scale);
//
//    }
//
//
//    static char[] aa = null;
//    static char[] bb = null;
//    static char[] cc = null;
//
//    int last_code_a = 0;
//    int last_code_b = 0;
//    ArrayList<String>[] last_value = null;
//
//    private Collection<String> get_object(int code) {
//        return (Collection<String>) unsafe.getObject(this.value, base + ((long) code << scale));
//    }
//
//    int code_time;
//    int code_a;
//    int code_b;
//    int code1;
//    int code2;
//    int code;
//
//
//    public final Collection<String> get(String caller, String responder, String time, String type) {
//
//        int code_a = caller.hashCode();
//        int code_b = responder.hashCode();
//        int code2 = (get_code_time(time) << 1) + (type.length() & 1);
//        int code1 = get_end_code(code_a, code_b);
//        int code = get_code(code1, code2);
//
//        return value[code];
//    }
//
////    private int string_hash(String s) {
////        int len = s.length();
////
////        if (len > 20) {
////            char[] ch = (char[]) unsafe.getObject(s, 12);
////            int code = 0;
////            for (int i = 0; i < 10; i++) code = code * 31 + ch[i];
////            for (int i = len - 10; i < len; i++) code = code * 31 + ch[i];
////            return code;
////        } else {
////            return s.hashCode();
////        }
////    }
//
//
//    private int get_code(int code1, int code2) {
//        return (code1 << 8) + code2;
//    }
//
//
//    private int get_end_code(int code_a, int code_b) {
//        return ((code_a << 5) - code_a + code_b) & MO;
//    }
//
//    private int get_code_time(String time) {
//        char[] ch = (char[]) unsafe.getObject(time, 12);
////        return time.charAt(11) * 600 + time.charAt(12) * 60 + time.charAt(14) * 10 + time.charAt(15) - base_time;
//        return ch[12] * 60 + ch[14] * 10 + ch[15] - base_time;
//    }
//
//    public final Collection<String> get_and_creat(String caller, String responder, String time, String type) {
//
//        int code_type = type.length() & 1;
//        int code_time = get_code_time(time);
//
//
//        int code_a = caller.hashCode();
//        int code_b = responder.hashCode();
//
//        int code1 = get_end_code(code_a, code_b);
//        int code2 = (code_time << 1) + code_type;
//
//        int code = get_code(code1, code2);
//        if (value[code] == null) {
//            value[code] = new ArrayList<>();
//        }
//        return value[code];
//    }
//}
