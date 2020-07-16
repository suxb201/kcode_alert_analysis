package com.kuaishou.kcode.check.demo;

import static com.kuaishou.kcode.check.demo.Utils.createQ1CheckResult;
import static com.kuaishou.kcode.check.demo.Utils.createQ2Result;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import com.kuaishou.kcode.KcodeAlertAnalysis;
import com.kuaishou.kcode.KcodeAlertAnalysisImpl;

/**
 * @author KCODE
 * Created on 2020-07-01
 */
public class KcodeAlertAnalysisTest {

    public static void main(String[] args) throws Exception {
        // demo 数据集
        String sourceFilePathDemo = "data/demo/demo.data";
        String ruleFilePathDemo = "data/demo/rule.txt";
        String q1ResultFilePathDemo = "data/demo/q1ans.txt";
        String q2ResultFilePathDemo = "data/demo/q2ans.txt";
        // test 数据集
        String sourceFilePathTest = "data/data_test/kcodeAlertForStudent-test.data";
        String ruleFilePathTest = "data/data_test/ruleForStudent-test.txt";
        String q1ResultFilePathTest = "data/data_test/Q1Result-test.data";
        String q2ResultFilePathTest = "data/data_test/Q2Answer-test.data";

        // 第一套数据集
        //kcodeAlertForStudent-1.data，原始监控数据
        String sourceFilePath1 = "data/data1/kcodeAlertForStudent-1.data";
        // ruleForStudent-1，报警规则
        String ruleFilePath1 = "data/data1/ruleForStudent-1.txt";
        // Q1Result-1.txt，第一问结果
        String q1ResultFilePath1 = "data/data1/Q1Result-1.txt";
        // Q2Result-1.txt，第二问输出和结果
        String q2ResultFilePath1 = "data/data1/Q2Result-1.txt";

        // 第二套数据集
        //kcodeAlertForStudent-2.data，原始监控数据
        String sourceFilePath2 = "data/data2/kcodeAlertForStudent-2.data";
        // ruleForStudent-2，报警规则
        String ruleFilePath2 = "data/data2/ruleForStudent-2.txt";
        // Q1Result-2.txt，第一问结果
        String q1ResultFilePath2 = "data/data2/Q1Result-2.txt";
        // Q2Result-2.txt，第二问输出和结果
        String q2ResultFilePath2 = "data/data2/Q2Result-2.txt";
        System.out.println("--------------- demo  -----------------------");
        testQuestion12(sourceFilePathDemo, ruleFilePathDemo, q1ResultFilePathDemo, q2ResultFilePathDemo);

//
        System.out.println("--------------- data1  -----------------------");
        testQuestion12(sourceFilePath1, ruleFilePath1, q1ResultFilePath1, q2ResultFilePath1);
        System.out.println("--------------- test  ）-----------------------");
        testQuestion12(sourceFilePathTest, ruleFilePathTest, q1ResultFilePathTest, q2ResultFilePathTest);
        System.out.println("--------------- data2  -----------------------");
        testQuestion12(sourceFilePath2, ruleFilePath2, q1ResultFilePath2, q2ResultFilePath2);

    }

    public static void testQuestion12(String sourceFilePath, String ruleFilePath, String q1ResultFilePath, String q2ResultFilePath) throws Exception {
        int ans = 0;
        // Q1
        Set<Q1Result> q1CheckResult = createQ1CheckResult(q1ResultFilePath);
        KcodeAlertAnalysis instance = new KcodeAlertAnalysisImpl();
        List<String> alertRules = Files.lines(Paths.get(ruleFilePath)).collect(Collectors.toList());
        long start = System.nanoTime();
        Collection<String> alertResult = instance.alarmMonitor(sourceFilePath, alertRules);
        long finish = System.nanoTime();
        if (Objects.isNull(alertResult) || alertResult.size() != q1CheckResult.size()) {
            System.out.println("Q1 Error Size:" + q1CheckResult + "," + alertResult.size());
            return;
        }
        Set<Q1Result> resultSet = alertResult.stream().map(line -> new Q1Result(line)).collect(Collectors.toSet());
        if (!resultSet.containsAll(q1CheckResult)) {
            System.out.println("Q1 Error Value: ");

            System.out.println("right: " + Arrays.toString(q1CheckResult.toArray()));
            System.out.println("wrong: " + Arrays.toString(resultSet.toArray()));
            return;
        }
        double t1 = (double) ((finish - start) / 1000000) / 1000;
        System.out.println("Q1 cast:" + t1 + "s");

//        return;
        // Q2
        Map<Q2Input, Set<Q2Result>> q2Result = createQ2Result(q2ResultFilePath);
        long cast = 0L;
        for (Map.Entry<Q2Input, Set<Q2Result>> entry : q2Result.entrySet()) {
            start = System.nanoTime();
            Q2Input q2Input = entry.getKey();
            Collection<String> longestPaths = instance.getLongestPath(q2Input.getCaller(), q2Input.getResponder(), q2Input.getTime(), q2Input.getType());
            finish = System.nanoTime();
            Set<Q2Result> checkResult = entry.getValue();

            if (Objects.isNull(longestPaths) || longestPaths.size() != checkResult.size()) {
                System.out.println("Q2 Error Size:" + q2Input + "," + checkResult.size() + longestPaths.size());
                return;
            }
            Set<Q2Result> results = longestPaths.stream().map(line -> new Q2Result(line)).collect(Collectors.toSet());
            if (!results.containsAll(checkResult)) {
                System.out.println("Q2 Error Result:" + q2Input);
                return;
            }
            cast += (finish - start);
        }
        int n2 = q2Result.entrySet().size();
        double t2 = ((double) cast / 1000000);
        System.out.println("time use: " + ((double) KcodeAlertAnalysisImpl.tt / 1000000) + "ms");
        System.out.println("time use2: " + ((double) KcodeAlertAnalysisImpl.tt2 / 1000000) + "ms");

        System.out.println("Q2 cast:" + t2 + "ms");
        System.out.println("s2 score: " + n2 / t2);
    }
}