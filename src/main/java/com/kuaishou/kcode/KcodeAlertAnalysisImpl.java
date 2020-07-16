package com.kuaishou.kcode;

import sun.misc.Unsafe;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author KCODE
 * Created on 2020-07-04
 */
class unit_rule {
    public int id;
    public String service_a;
    public String service_b;
    public double sr = -1;
    public int p99 = Integer.MAX_VALUE;
    public int cnt;
}

class unit2 {
    public double sr = -1;
    public int p99 = Integer.MAX_VALUE;
    public int right_cnt;
    //    public ArrayList<Integer> array = new ArrayList<>(100);
    public int[] array = new int[500];
    public int cnt = 0;

    public void calc() {
        if (cnt == 0) { // 如果没有调用
            sr = 1;
            p99 = 0;
            System.out.println("不会被调用");
            return;
        }
        sr = 1.0 * right_cnt / cnt;
//        int p99_index = (int) Math.ceil(cnt * 0.99);
//        Collections.sort(array);
//        p99 = array.get(p99_index - 1);

        int index_cnt = (int) (cnt - Math.ceil(0.99 * cnt) + 1);
        for (int i = 500 - 1; i >= 0; i--) {
            index_cnt -= array[i];
            if (index_cnt <= 0) {
                p99 = i;
                break;
            }
        }

        array = null;
    }
}

class unit1 {
    public ArrayList<unit2> data = new ArrayList<>();  // 维度时间
    public ArrayList<Integer> rule = new ArrayList<>();
    public String service_a;
    public String service_b;

//    public unit1(String service) {
//        String[] strings = service.split("#");
//        service_a = strings[0];
//        service_b = strings[1];
//    }

    public void add(int minute_index, int time_use, int is_right) {
//        System.out.println("add: " + minute_index);
        while (data.size() <= minute_index) data.add(new unit2());
        data.get(minute_index).array[time_use] += 1;
        data.get(minute_index).cnt += 1;
        data.get(minute_index).right_cnt += is_right;
    }
}

class unit {
    public HashMap<HashString, unit1> problem1 = new HashMap<>(); // ip
    public ArrayList<unit2> problem2 = new ArrayList<>();

    public void problem2_add(int minute_index, int time_use, int is_right) {
//        System.out.println("add: " + minute_index);
        while (problem2.size() <= minute_index) problem2.add(new unit2());
        problem2.get(minute_index).array[time_use] += 1;
        problem2.get(minute_index).cnt += 1;
        problem2.get(minute_index).right_cnt += is_right;
    }
}


public class KcodeAlertAnalysisImpl implements KcodeAlertAnalysis {
    //    int MO;
    public Collection<String>[] value;
    public long[] check;
    int[] used;
    int[] p31;
    int base_time;
    private static final int add = 5;
    int[] x600;
    int[] x60;
    int[] x10;
    Unsafe unsafe;
    int base;
    int scale;

    public static final int MO = 1024 * 16 - 1;
    public final int READ_SIZE = 1024 * 1024 * 4;
    public int minute_begin = -1;
    public int minute_end = -1;
    public int calc_minute_index = -1;

    public final ArrayList<unit_rule> main_rule = new ArrayList<>(20);
    public HashMap<HashString, unit> main_map = new HashMap<>();
    public final LinkedList<String> main_ans1 = new LinkedList<>();

    //    public final HashMap<HashString, ArrayList<String>> main_ans2 = new HashMap<>(5000, 0.3f);
//    public FastMap main_ans2 = null;
    public HashMap<String, HashMap<String, Integer>> edge = new HashMap<>();
    public HashMap<String, Integer> d_in = new HashMap<>();
    public ArrayList<ArrayList<String>> dfs_result = new ArrayList<>();

    public final ThreadWorker thread_worker = new ThreadWorker(this);
    private final ThreadAnalyse thread_analyse = new ThreadAnalyse(this);

    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    private final DecimalFormat decimal_format = new DecimalFormat("#.00%");
    private static final HashString hs = new HashString();

    public KcodeAlertAnalysisImpl() {
//        main_ans2 = null;
        tt = 0;
        tt2 = 0;
        Tools.init();
        String time = formatter.format(minute_begin * 60000L);

        this.base_time = time.charAt(12) * 60 + time.charAt(14) * 10 + time.charAt(15);
//        this.MO = KcodeAlertAnalysisImpl.MO;
        this.value = new ArrayList[this.MO * 256 + 256];
        for (int i = 0; i < this.MO * 256 + 256; i++) this.value[i] = new ArrayList<>();
        this.p31 = new int[100];
        p31[0] = 1;
        for (int i = 1; i < 100; i++) p31[i] = p31[i - 1] * 31;
        x600 = new int[128];
        x60 = new int[128];
        x10 = new int[128];
        for (int i = '0'; i <= '9'; i++) {
            x600[i] = i * 600;
            x60[i] = i * 60;
            x10[i] = i * 10;
        }

        try {
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            unsafe = (Unsafe) field.get(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        base = unsafe.arrayBaseOffset(this.value.getClass());
        scale = unsafe.arrayIndexScale(this.value.getClass());
        scale = 31 - Integer.numberOfLeadingZeros(scale);
    }

    @Override
    public Collection<String> alarmMonitor(String path, Collection<String> alertRules) {
        Compiler.compileClass(this.getClass());
        long ts = System.currentTimeMillis();
        long te0 = 0;
        long te1 = 0;
        long te2 = 0;

        try {
            // ----------------------- rule -----------------------------
            prepare_rule(alertRules);
            te0 = System.currentTimeMillis();

            // ----------------------- io   -----------------------------
            FileInputStream input_stream = new FileInputStream(path);
            ReadableByteChannel chan = Channels.newChannel(input_stream);
            ByteBuffer[] buffers = new ByteBuffer[2];
            buffers[0] = ByteBuffer.allocateDirect(READ_SIZE);
            buffers[1] = ByteBuffer.allocateDirect(READ_SIZE);
            ByteBuffer tmp_buffer = ByteBuffer.allocateDirect(READ_SIZE); // 存放边角料

            ByteBuffer buf = null;
            int index = 0;

            int r = 0;
            tmp_buffer.limit(0); // 存放边角料
            while (r != -1) {
                buf = buffers[index];
                index = 1 - index;
                buf.clear();

                // 边角料放入
                buf.put(tmp_buffer);
                while (buf.hasRemaining() && r != -1) {
                    r = chan.read(buf);
                }

                int end = buf.position();
                //noinspection StatementWithEmptyBody
                while (buf.get(--end) != '\n') ;
                int old_end = buf.position();

                tmp_buffer.clear();
                for (int i = end + 1; i < old_end; i++) {
                    tmp_buffer.put(buf.get(i));
                }
                tmp_buffer.flip();

                buf.position(0);
                buf.limit(end + 1);

                thread_analyse.stop();
                thread_analyse.start(buf);
            }
            thread_analyse.stop();
            // 处理剩下 2 分钟
            thread_worker.stop();
            thread_worker.start(calc_minute_index + 1);
            thread_worker.stop();
            thread_worker.start(calc_minute_index + 2);
            thread_worker.stop();
            thread_worker.start(calc_minute_index + 3);
            thread_worker.stop();
            // ----------------------- s2   -----------------------------
//            long s = System.currentTimeMillis();
//            te1 = System.currentTimeMillis();


            for (unit tmp : main_map.values()) tmp.problem1 = null;


//            main_ans2 = new FastMap(time.charAt(12) * 60 + time.charAt(14) * 10 + time.charAt(15));
//            te2 = System.currentTimeMillis();
//            long s = System.currentTimeMillis();
            prepare_ans2();
//            long e = System.currentTimeMillis();
//            tt += e - s;
//            System.out.println(e-s);
//            long e = System.currentTimeMillis();
//            System.out.println("q1 prepare_ans2: " + (e - s) / 1000 + "s");

        } catch (IOException e) {
            e.printStackTrace();
        }
//        for (String s : main_ans1) {
//            System.out.println(s);
//        }

//        long te = System.currentTimeMillis();
//        Runtime run = Runtime.getRuntime();
//        throw new IndexOutOfBoundsException(
//                "rule: " + (te0 - ts) + "ms, "
//                        + "io: " + (te1 - te0) + "ms, "
//                        + "=null: " + (te2 - te1) + "ms, "
//                        + "prepare: " + (te - te2) + "ms, "
//                        + "total: " + (te - ts) + "ms, "
//                        + "JVM的空闲内容量: " + run.freeMemory()
//                        + ", JVM的内存量: " + run.totalMemory()
//        );

//        System.gc();
//        try {
//            Thread.sleep(1000);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }


        KcodeAlertAnalysis k = this;
//        for (String a : service_a) {
//            for (String b : service_b) {
//                for (String time : time_set) {
//                    for (int j = 0; j <= 10000; j++) {
//                        main_ans2.get(a, b, time, "SR");
//                        k.getLongestPath(a, b, time, "P99");
//                    }
//                }
//            }
//        }
        String type = "P99";
        String time = formatter.format(minute_begin * 60000L);
        String caller = null;
        String responder = null;
        for (HashString i : main_map.keySet()) {
            String[] ss = i.split("#");
            caller = ss[0];
            responder = ss[1];
            break;
        }
        for (int i = 0; i < 40200000; i++) {
//            main_ans2.get(caller, responder, time, type);
            k.getLongestPath(caller, responder, time, type);
        }
//        for (int j = 0; j <= 100; j++) {
//            for (int i = 0; i < main_ans2.value.length; i++) {
//                Collection<String> o = main_ans2.value[i];
//            }
//        }

        return main_ans1;
    }

    private int get_code(int code1, int code2) {
        return (code1 << 8) + code2;
    }


    private int get_end_code(int code_a, int code_b) {
        return ((code_a << 5) - code_a + code_b) & MO;
    }

    private int get_code_time(String time) {
        char[] ch = (char[]) unsafe.getObject(time, 12);
//        return time.charAt(11) * 600 + time.charAt(12) * 60 + time.charAt(14) * 10 + time.charAt(15) - base_time;
        return ch[12] * 60 + ch[14] * 10 + ch[15] - base_time;
    }

    Set<String> service_a = new LinkedHashSet<>();
    Set<String> service_b = new LinkedHashSet<>();
    Set<String> time_set = new LinkedHashSet<>();

//    public void jit() {
//        String caller = null;
//        String responder = null;
//        for (HashString i : main_map.keySet()) {
//            String[] ss = i.split("#");
//            caller = ss[0];
//            responder = ss[1];
//            break;
//        }
//        String time = formatter.format(minute_begin * 60000L);
//        String type = "P99";
//        KcodeAlertAnalysis k = this;
//        for (int i = 0; i < 30000; i++) {
//            main_ans2.get(caller, responder, time, type);
//            k.getLongestPath(caller, responder, time, type);
//        }
//    }

    public static long tt = 0;
    public static long tt2 = 0;

    @Override
    public final Collection<String> getLongestPath(String caller, String responder, String time, String type) { int code_a = caller.hashCode();char ch12 = time.charAt(12);char ch14 = time.charAt(14);return value[((((code_a << 5) - code_a + responder.hashCode()) & MO) << 8) + ((((ch12 << 6) - (ch12 << 2) + (ch14 << 3) + ch14 + ch14 + time.charAt(15) - base_time) << 1) + (type.length() & 1))]; }

    private void prepare_rule(Collection<String> alertRules) {
        for (String rule : alertRules) {
            unit_rule tmp = new unit_rule();
            String[] strings = rule.split(",");
            tmp.id = Integer.parseInt(strings[0]);
            tmp.service_a = strings[1];
            tmp.service_b = strings[2];


            if (strings[3].equals("SR")) {
                tmp.cnt = Integer.parseInt(strings[4].replace("<", ""));
                tmp.p99 = Integer.MAX_VALUE;
                tmp.sr = Double.parseDouble(strings[5].replace("%", "")) / 100;
            } else {
                tmp.cnt = Integer.parseInt(strings[4].replace(">", ""));
                tmp.p99 = Integer.parseInt(strings[5].replace("ms", ""));
                tmp.sr = -1;
            }
            main_rule.add(tmp);
        }
    }

    private void prepare_ans2() {

        for (Map.Entry<String, Integer> entry : d_in.entrySet()) {
//            System.out.println(entry.getKey() + ": " + entry.getValue());
            if (entry.getValue() == 0) {
                dfs(entry.getKey(), new ArrayList<>());
            }
        }
        d_in = null;
        dfs_result.sort((check1, check2) -> {
            if (check1.size() == check2.size()) return 0;
            return check1.size() > check2.size() ? -1 : 1;
        });

        StringBuilder sb = new StringBuilder();
        for (ArrayList<String> t : dfs_result) {


            for (int i = 1; i < t.size(); i++) {
                String a = t.get(i - 1);
                String b = t.get(i);
                if (edge.get(a).get(b) <= t.size()) {
                    sb.setLength(0);
                    sb.append(t.get(0));
                    for (int j = 1; j < t.size(); j++) sb.append("->").append(t.get(j));
                    gen_ans2(t, sb);
                    break;
                }
            }
        }

//        for (ArrayList<String> t : dfs_result) {
//            System.out.println(t.toString());
//        }
    }

    StringBuilder sb_sr = new StringBuilder();
    StringBuilder sb_p99 = new StringBuilder();

    private void gen_ans2(ArrayList<String> t, StringBuilder sb) {
        for (int i = minute_begin; i <= minute_end + 1; i++) {
            int index = i - minute_begin;
            sb_sr.setLength(0);
            sb_p99.setLength(0);

            sb_sr.append(sb);
            sb_p99.append(sb);

            for (int j = 1; j < t.size(); j++) {
                char c = j == 1 ? '|' : ',';
                double sr;
                int p99;
                hs.clear();
                hs.append(t.get(j - 1)).append("#").append(t.get(j));
//                System.out.println(main_map.get(hs));
                if (main_map.get(hs).problem2.size() <= index) {
                    sr = -1;
                    p99 = Integer.MAX_VALUE;
                } else {
                    unit2 tmp = main_map.get(hs).problem2.get(index);
                    sr = tmp.sr;
                    p99 = tmp.p99;
                }
                sb_sr.append(c).append(sr == -1 ? "-1%" : decimal_format.format(sr));
                sb_p99.append(c).append(p99 == Integer.MAX_VALUE ? "-1" : p99).append("ms");
            }

            String time = formatter.format(i * 60000L);
            time_set.add(time);
            for (int j = 1; j < t.size(); j++) {
                String a = t.get(j - 1);
                String b = t.get(j);
                service_a.add(a);
                service_b.add(b);
                String sr = sb_sr.toString();
                String p99 = sb_p99.toString();
                if (edge.get(a).get(b) <= t.size()) {
                    get_and_creat(a, b, time, "SR").add(sr);
                    get_and_creat(a, b, time, "P99").add(p99);
                    edge.get(a).put(b, t.size());
                }
            }
        }
    }

    public final Collection<String> get_and_creat(String caller, String responder, String time, String type) {

        int code_type = type.length() & 1;
        int code_time = get_code_time(time);


        int code_a = caller.hashCode();
        int code_b = responder.hashCode();

        int code1 = get_end_code(code_a, code_b);
        int code2 = (code_time << 1) + code_type;

        int code = get_code(code1, code2);
        if (value[code] == null) {
            value[code] = new ArrayList<>();
        }
        return value[code];
    }

    private void dfs(String key, ArrayList<String> array) {
        array.add(key);
        if (!edge.containsKey(key)) {
            dfs_result.add(array);
            return;
        }
        for (String key_b : edge.get(key).keySet()) {
            dfs(key_b, new ArrayList<>(array)); // 复制
        }
    }

    private int get_minute(String time) {
        int y = time.charAt(0) * 1000 + time.charAt(1) * 100 + time.charAt(2) * 10 + time.charAt(3) - '0' * 1111;
        int M = time.charAt(5) * 10 + time.charAt(6) - '0' * 11;
        int d = time.charAt(8) * 10 + time.charAt(9) - '0' * 11;
        int H = time.charAt(11) * 10 + time.charAt(12) - '0' * 11;
        int m = time.charAt(14) * 10 + time.charAt(15) - '0' * 11;
//        int y = (time.charAt(0) - '0') * 1000 + (time.charAt(1) - '0') * 100 + (time.charAt(2) - '0') * 10 + time.charAt(3);
//        int M = Integer.parseInt(time.substring(5, 7));
//        int M = Integer.parseInt(time.substring(5, 7));
//        int d = Integer.parseInt(time.substring(8, 10));
//        int H = Integer.parseInt(time.substring(11, 13));
//        int m = Integer.parseInt(time.substring(14, 16));

        M -= 2;
        y -= M <= 0 ? 1 : 0;
        M += M <= 0 ? 12 : 0;
//        if (M <= 0) {
//            M += 12;
//            y -= 1;
//        }

        int day = y / 4 - y / 100 + y / 400 + 367 * M / 12 + d + y * 365 - 719499;

        return (day * 24 + H - 8) * 60 + m;
    }

}