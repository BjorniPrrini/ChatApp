package com.chatappfrontend.frontend.util;

import javafx.concurrent.Task;
import javafx.scene.image.Image;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class ImageCache {
    private static final Map<String, Image> cache = new ConcurrentHashMap<>();
    private static final String BASE_URL = AppConfig.get("api.base.url");

    public static void load(String path, Consumer<Image> onLoaded){
        if(path == null){
            return;
        }

        Image cached = cache.get(path);

        if(cached != null){
            onLoaded.accept(cached);

            return;
        }

        Task<Image> task = new Task<>() {
            @Override
            protected Image call() {
                return new Image(BASE_URL + "/" + path, true);
            }
        };

        task.setOnSucceeded(_ -> {
            Image image = task.getValue();

            cache.put(path, image);

            onLoaded.accept(image);
        });

        AppExecutor.run(task);
    }
}