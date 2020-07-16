package com.kuaishou.kcode;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.concurrent.RecursiveAction;

public class ThreadAnalyse extends RecursiveAction {

    boolean running = false;
    ByteBuffer buffer = null;
    KcodeAlertAnalysisImpl main_class;
    HashMap<HashString, unit> main_map;

    public ThreadAnalyse(KcodeAlertAnalysisImpl main_class) {
        this.main_class = main_class;
        this.main_map = main_class.main_map;
    }

    public void start(ByteBuffer buf) {
        this.buffer = buf;
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

    private final char[] ch = new char[200];

    public void compute() {

        int i = 0, end = this.buffer.limit();
        while (i < end) {
            HashString hs_service = new HashString();
            HashString hs_ip = new HashString();

            // service_a
            int index = 0;
            while ((ch[index] = (char) buffer.get(i)) != ',') {
                index += 1;
                i += 1;
            }
            i += 1;
            hs_service.append(ch, 0, index).append('#');

            // ip_a
            index = 0;
            while ((ch[index] = (char) buffer.get(i)) != ',') {
                index += 1;
                i += 1;
            }
            i += 1;
            hs_ip.append(ch, 0, index).append('#');

            // service_b
            index = 0;
            while ((ch[index] = (char) buffer.get(i)) != ',') {
                index += 1;
                i += 1;
            }
            i += 1;
            hs_service.append(ch, 0, index);

            // ip_b
            index = 0;
            while ((ch[index] = (char) buffer.get(i)) != ',') {
                index += 1;
                i += 1;
            }
            i += 1;
            hs_ip.append(ch, 0, index);

            // 开始读 true false
            int is_right;
            if (buffer.get(i) == 't') {
                is_right = 1;
                i += 5;
            } else {
                is_right = 0;
                i += 6;
            }

            // 开始读 time use
            int time_use = 0;
            byte b;
            while ((b = buffer.get(i)) != ',') {
                time_use = time_use * 10 + b - '0';
                i += 1;
            }
            i += 1; // 逗号下一个

            // 开始读 timestamp 秒级
            int minute = 0;
            for (int j = 0; j < 10; j++) {
                b = buffer.get(i + j);
                minute = minute * 10 + b - '0';
            }
            minute /= 60;
            i += 14;

            // 更新 main class
            main_class.minute_begin = main_class.minute_begin == -1 ? minute - 1 : main_class.minute_begin;
            main_class.minute_end = Math.max(main_class.minute_end, minute);

            int minute_index = minute - main_class.minute_begin;


            unit tmp_unit = main_map.computeIfAbsent(hs_service, k -> new unit());
            unit1 tmp_unit1 = tmp_unit.problem1.computeIfAbsent(hs_ip, k -> new unit1());
            tmp_unit1.add(minute_index, time_use, is_right);
            tmp_unit.problem2_add(minute_index, time_use, is_right);

            if (minute_index > main_class.calc_minute_index + 3) {
                main_class.calc_minute_index += 1;
                main_class.thread_worker.start(main_class.calc_minute_index);
                main_class.thread_worker.stop(); // map 添加元素导致冲突

            }
//            System.out.println(service + " " + ip + " " + minute + " " + minute_index);
        }
        this.running = false;
    }
}
