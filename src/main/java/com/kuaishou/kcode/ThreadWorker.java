package com.kuaishou.kcode;

import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.RecursiveAction;

public class ThreadWorker extends RecursiveAction {

    boolean running = false;
    KcodeAlertAnalysisImpl main_class;
    LinkedList<String> main_ans1;
    HashMap<HashString, unit> main_map;
    ArrayList<unit_rule> main_rule;
    int minute_index;
    int minute;
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    DecimalFormat decimal_format = new DecimalFormat("#.00%");

    public ThreadWorker(KcodeAlertAnalysisImpl main_class) {
        this.main_class = main_class;
        this.main_ans1 = main_class.main_ans1;
        this.main_map = main_class.main_map;
        this.main_rule = main_class.main_rule;
    }

    public void start(int minute_index) {
//        System.out.println(formatter.format(1594483652000L));
        this.minute_index = minute_index;
        this.minute = main_class.minute_begin + minute_index;
        this.running = true;
        this.fork();
    }

    public void stop() {
        if (this.running) {
            this.join();
            this.running = false;
        }
        this.reinitialize();
    }


    public void compute() {
//        System.out.println("q: " + minute_index + " minute: " + this.minute);
        for (Map.Entry<HashString, unit> entry_service : main_map.entrySet()) {

            // ----------- 更新 problem2
            if (entry_service.getValue().problem2.size() <= this.minute_index) continue;
            if (entry_service.getValue().problem2.get(this.minute_index).cnt == 0) continue;
            entry_service.getValue().problem2.get(this.minute_index).calc();
//            System.out.println("更新 problem2： " + entry_service.getKey() + " " + formatter.format(this.minute * 60 * 1000L));

            // ----------- 更新 problem1
            for (Map.Entry<HashString, unit1> entry_ip : entry_service.getValue().problem1.entrySet()) {
                unit1 tmp = entry_ip.getValue();
                if (tmp.data.size() <= this.minute_index) continue; //  这一聚合对还没有这个时间点的数据
                if (tmp.data.get(this.minute_index).cnt == 0) continue;
//                System.out.println(tmp.service_a + " " + tmp.service_b + " " + this.minute_index);
                // ---------------- 存边，方便第二问 --------------------
                if (tmp.service_a == null) {
                    String[] a_b = entry_service.getKey().split("#");
                    tmp.service_a = a_b[0];
                    tmp.service_b = a_b[1];
                }
                {
                    HashMap<String, Integer> tmp_map = this.main_class.edge.computeIfAbsent(tmp.service_a, k -> new HashMap<>());
                    if (!tmp_map.containsKey(tmp.service_b)) {
                        tmp_map.put(tmp.service_b, 0);

                        this.main_class.d_in.putIfAbsent(tmp.service_a, 0);
                        this.main_class.d_in.putIfAbsent(tmp.service_b, 0);

                        int value = this.main_class.d_in.get(tmp.service_b);
                        this.main_class.d_in.put(tmp.service_b, value + 1);
//                        System.out.println("add " + tmp.service_a + "----" + tmp.service_b);
                    }

                }
                // -------------- 存边结束

                unit2 tmp2 = tmp.data.get(this.minute_index);
                tmp2.calc();
                if (tmp.rule.size() == 0) for (int i = 0; i < this.main_rule.size(); i++) tmp.rule.add(0);

                for (int i = 0; i < this.main_rule.size(); i++) {

                    unit_rule rule = this.main_rule.get(i);
//                    System.out.println(rule.id + " " + rule.service_a + " " + rule.service_b);
                    if (!rule.service_a.equals(tmp.service_a) && !rule.service_a.equals("ALL")) continue;
                    if (!rule.service_b.equals(tmp.service_b) && !rule.service_b.equals("ALL")) continue;

//                    System.out.println(rule.id + " " + rule.service_a + " " + rule.service_b + " " + rule.p99 + " " + rule.sr);
//                    System.out.println(tmp2.p99 + " " + tmp2.sr);

                    if (tmp2.sr < rule.sr || tmp2.p99 > rule.p99) {
                        tmp.rule.set(i, tmp.rule.get(i) + 1);
                    } else {
                        tmp.rule.set(i, 0);
                    }

                    if (tmp.rule.get(i) >= rule.cnt) {
                        String[] strings = entry_ip.getKey().split("#");

                        this.main_ans1.add(rule.id
                                + ","
                                + formatter.format(this.minute * 60 * 1000L)
                                + ","

                                + tmp.service_a
                                + ","
                                + strings[0]
                                + ","
                                + tmp.service_b
                                + ","
                                + strings[1]
                                + ","
                                + (rule.sr == -1 ? tmp2.p99 : decimal_format.format(tmp2.sr - 0.00005))
                                + (rule.sr == -1 ? "ms" : "%")
                        );
                    }
                }

            }
        }

        this.running = false;
    }
}
