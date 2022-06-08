package com.jay.demo;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * Copyright(c),2018-2021,合肥市鼎足空间技术有限公司
 *
 * @author jing.fang
 * @date 2022/6/7
 * @description Future 类API demo
 * history
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
public class FutureDemo {

    private static ExecutorService pool = Executors.newFixedThreadPool(3);

    public static void main(String[] args) throws Exception {
//        test01();
//        test02();
//        testCompletableFuture01();
//        testCompletableFuture02();
        testCompletableFuture03();
        System.out.println("主线程结束运行");
    }

    /**
     * 基本使用方法1
     */
    private static void test01(){
        FutureTask<String> task = new FutureTask<String>(() -> {
            System.out.println("线程开始运行");
            TimeUnit.SECONDS.sleep(3);
            return "线程已完成";
        });
        pool.submit(task);
        try {
            // 调用get方法时会阻塞线程
            System.out.println(task.get());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        } finally {
            // 线程池使用完毕需要关闭，不然会造成资源浪费现象
            pool.shutdown();
        }
    }

    /**
     * 基本使用方法2
     */
    private static void test02(){
        FutureTask<String> task = new FutureTask<String>(() -> {
            System.out.println("线程开始运行");
            TimeUnit.SECONDS.sleep(3);
            return "线程已完成";
        });
        pool.submit(task);
        try {
            while (true){
                if(task.isDone()){
                    // 调用get方法时会阻塞线程
                    System.out.println(task.get());
                    break;
                }else {
                    TimeUnit.MILLISECONDS.sleep(500);
                    System.out.println("====正在执行线程任务====");
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        } finally {
            // 线程池使用完毕需要关闭，不然会造成资源浪费现象
            pool.shutdown();
        }
    }

    private static void testCompletableFuture01() throws ExecutionException, InterruptedException {
        // 未使用线程池参数时，默认使用的是ForkJoinPool线程池
        CompletableFuture<Void> completableFuture = CompletableFuture.runAsync(() -> {
            System.out.println("线程开始运行");
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("线程执行完成");
        });
        System.out.println("获取执行结果：" + completableFuture.get());
    }

    private static void testCompletableFuture02() throws ExecutionException, InterruptedException {
        // 未使用线程池参数时，默认使用的是ForkJoinPool线程池(该线程池创建的线程类似守护线程)
        CompletableFuture<Integer> completableFuture = CompletableFuture.supplyAsync(() -> {
            System.out.println("线程开始运行");
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("线程执行完成");
            return ThreadLocalRandom.current().nextInt(10);
        }, pool).whenComplete((r ,e) -> {
            if (e == null){
                System.out.println("执行结果：" + r);
            }
            if (r > 2){
                int a = 10 / 0;
            }
            pool.shutdown();
        }).exceptionally((e) -> {
            System.out.println(e.getMessage());
            pool.shutdown();
            return null;
        });
    }

    static List<MallData> mallList = Arrays.asList(
            new MallData("JD"),
            new MallData("YaMaSon"),
            new MallData("Pdd"),
            new MallData("DangDang"),
            new MallData("TaoBao"));
    private static void getPriceList1(List<MallData> list, String bookName){
        long start = System.currentTimeMillis();
        List<String> collect = list.stream()
                .map(mallData ->
                        String.format("%s in %s price is %.2f",
                                bookName,
                                mallData.getName(),
                                mallData.getPrice(bookName)))
                .collect(Collectors.toList());
        for (String s : collect) {
            System.out.println(s);
        }
        long end = System.currentTimeMillis();
        System.out.println("总用时为：" + (end - start) + "毫秒");
    }

    /**
     * 使用CompletableFuture优化程序
     */
    private static void getPriceList2(List<MallData> list, String bookName){
        try {
            long start = System.currentTimeMillis();
            List<String> collect = list.stream().map(mallData ->
                    CompletableFuture.supplyAsync(() ->
                            String.format("%s in %s price is %.2f", bookName, mallData.getName(), mallData.getPrice(bookName))
                            , pool))
                    .collect(Collectors.toList()).stream().map(CompletableFuture::join).collect(Collectors.toList());
            for (String s : collect) {
                System.out.println(s);
            }
            long end = System.currentTimeMillis();
            System.out.println("总用时为：" + (end - start) + "毫秒");
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            pool.shutdown();
        }
    }

    private static void testCompletableFuture03() throws ExecutionException, InterruptedException {
        getPriceList1(mallList, "mysql");
        getPriceList2(mallList, "mysql");
    }

}
