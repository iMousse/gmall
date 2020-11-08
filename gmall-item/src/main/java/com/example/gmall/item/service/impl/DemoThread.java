package com.example.gmall.item.service.impl;

import com.atguigu.core.bean.PageVo;

import java.util.concurrent.*;

public class DemoThread {
    public static void main(String[] args) {
/*        //1.继承thread基类
        new MyThread().start();


        //2.实现runnable接口
        new Thread(new MyRunnableTest()).start();
        new Thread(()->{
            System.out.println("thread start ...");
            System.out.println("================");
            System.out.println("thread end .....");
        },"匿名内部类线程").start();


        //3.Callable接口 + FutureTask,有返回值，可以处理异常
        FutureTask<String> futureTask = new FutureTask<>(new MyCallable());
        new Thread(futureTask).start();
        try {
            System.out.println(futureTask.get());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        //4.线程池
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(3, 5, 50, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(10),
                Executors.defaultThreadFactory(),
                (Runnable r, ThreadPoolExecutor executor) -> {
                    System.out.println("使用了拒绝策略");
                });

        for (int i = 0; i < 50; i++) {
            threadPoolExecutor.execute( ()->{
                System.out.println("thread start ..." + Thread.currentThread().getName());
                System.out.println("================");
                System.out.println("thread end .....");
            });
        }*/


        //5.
        CompletableFuture.runAsync(() -> {
            System.out.println("runAsync");
        }).whenComplete( (t,u) -> {
            System.out.println(t);
            System.out.println(u);
        });

        CompletableFuture.supplyAsync(() -> {
            System.out.println("runAsync");
            int i = 1 / 0;
            return "hello supplyAsync";
        }).whenComplete( (t,u) -> {
            System.out.println("t:" + t);
            System.out.println(u);
        });
    }
}

class MyCallable implements Callable<String> {

    @Override
    public String call() throws Exception {
        System.out.println("thread start ...");
        System.out.println("================");
        System.out.println("thread end .....");
        return "hello";
    }
}


class MyRunnableTest implements Runnable {

    @Override
    public void run() {
        System.out.println("thread start ..." + Thread.currentThread().getName());
        System.out.println("================");
        System.out.println("thread end .....");
    }
}


class MyThread extends Thread {
    @Override
    public void run() {
        System.out.println("thread start ...");
        System.out.println("================");
        System.out.println("thread end .....");
    }
}