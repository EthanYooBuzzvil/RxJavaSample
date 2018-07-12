package com.buzzvil.rxjava.sample.concurrent;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import lombok.AllArgsConstructor;
import lombok.Data;

public class ConcurrentTest {
    public static void main(String[] args) {
        System.out.println("Start");

        boolean async = true;

        ConcurrentTest concurrentTest = new ConcurrentTest();

        if (async) {
            concurrentTest.startAsyn();
        } else {
            concurrentTest.start();
        }

        System.out.println("Finish");

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        getToken()
                .subscribeOn(Schedulers.io())
                .flatMap(token -> Observable.zip(
                        getBigData(token).subscribeOn(Schedulers.io()),
                        getSmallData(token).subscribeOn(Schedulers.io()),
                        (bigData, smallData) -> bigData.toString() + " | " + smallData.toString()))
                .observeOn(Schedulers.single())
                .subscribe(System.out::println);
    }

    public void startAsyn() {
        getTokenAsyn()
                .flatMap(token -> Observable.zip(
                        getBigDataAsync(token),
                        getSmallDataAsync(token),
                        (bigData, smallData) -> bigData.toString() + " | " + smallData.toString()))
                .observeOn(Schedulers.single())
                .subscribe(System.out::println);
    }

    public Observable<Token> getToken() {
        return Observable.create(emitter -> {
            System.out.println("ConcurrentTest.getToken");
            Thread.sleep(1000);
            emitter.onNext(new Token("1234"));
            emitter.onComplete();
        });
    }

    public Observable<Token> getTokenAsyn() {
        return Observable
                .defer(this::getToken)
                .subscribeOn(Schedulers.io());
    }

    public Observable<BigData> getBigData(Token token) {
        return Observable.create(emitter -> {
            System.out.println("ConcurrentTest.getBigData");
            Thread.sleep(2000);
            emitter.onNext(new BigData("BigData.a." + token, 6));
            emitter.onComplete();
        });
    }

    public Observable<BigData> getBigDataAsync(Token token) {
        return Observable
                .defer(() -> getBigData(token))
                .subscribeOn(Schedulers.io());
    }

    public Observable<SmallData> getSmallData(Token token) {
        return Observable.create(emitter -> {
            System.out.println("ConcurrentTest.getSmallData");
            Thread.sleep(1000);
            emitter.onNext(new SmallData("SmallData.b." + token, 1));
            emitter.onComplete();
        });
    }

    public Observable<SmallData> getSmallDataAsync(Token token) {
        return Observable
                .defer(() -> getSmallData(token))
                .subscribeOn(Schedulers.io());
    }

    @Data
    @AllArgsConstructor
    public static class Token {
        private String key;
    }

    @Data
    @AllArgsConstructor
    public static class BigData {
        private String name;
        private int data;
    }

    @Data
    @AllArgsConstructor
    public static class SmallData {
        private String name;
        private int data;
    }
}