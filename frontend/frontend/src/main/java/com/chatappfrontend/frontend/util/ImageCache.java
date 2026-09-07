package com.chatappfrontend.frontend.util;

import javafx.concurrent.Task;
import javafx.scene.image.Image;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class ImageCache {
    private static final Map<String, Image> cache = new ConcurrentHashMap<>();
    private static final Map<String, List<Consumer<Image>>> inFlight = new ConcurrentHashMap<>();
    private static final String BASE_URL = AppConfig.get("api.base.url");

    public static void load(String path, int size, Consumer<Image> onLoaded){
        if(path == null){
            return;
        }

        String key = path + '@' + size;

        Image cached = cache.get(key);

        if(cached != null){
            onLoaded.accept(cached);

            return;
        }

        if(registerWaiter(key, onLoaded)){
            return;
        }

        fetch(path, size, image -> {
            cache.put(key, image);

            notifyWaiters(key, image);
        });
    }

    public static void loadUncached(String path, int size, Consumer<Image> onLoad){
        if(path == null){
            return;
        }

        String key = path + '@' + size;

        Image cached = cache.get(key);

        if(cached != null){
            onLoad.accept(cached);

            return;
        }

        if(registerWaiter(key, onLoad)){
            return;
        }

        fetch(path, size, image -> notifyWaiters(key, image));
    }

    private static boolean registerWaiter(String key, Consumer<Image> onLoaded){
        synchronized (inFlight) {
            List<Consumer<Image>> waiters = inFlight.get(key);

            if(waiters != null){
                waiters.add(onLoaded);

                return true;
            }

            List<Consumer<Image>> newWaiters = new ArrayList<>();

            newWaiters.add(onLoaded);

            inFlight.put(key, newWaiters);

            return false;
        }
    }

    private static void notifyWaiters(String key, Image image){
        List<Consumer<Image>> waiters;

        synchronized (inFlight) {
            waiters = inFlight.remove(key);
        }

        if(waiters != null){
            waiters.forEach(w -> w.accept(image));
        }
    }

    private static void fetch(String path, int size, Consumer<Image> onLoaded){
        Task<Image> task = new Task<>() {
            @Override
            protected Image call() {
                return new Image(BASE_URL + "/" + path, size, size, false, false);
            }
        };

        task.setOnSucceeded(_ -> onLoaded.accept(task.getValue()));

        AppExecutor.run(task);
    }
}