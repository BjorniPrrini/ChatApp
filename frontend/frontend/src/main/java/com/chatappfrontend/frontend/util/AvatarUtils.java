package com.chatappfrontend.frontend.util;

import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;

public class AvatarUtils {
    public static void applyAvatar(Label avatarLabel, String profilePicturePath, String initials, boolean onCached, int size){
        avatarLabel.setText(initials);
        avatarLabel.setGraphic(null);
        avatarLabel.getProperties().put("currentPicture", profilePicturePath);

        if(profilePicturePath == null){
            return;
        }

        if(onCached){
            ImageCache.load(profilePicturePath, size, image -> applyIfStillCurrent(avatarLabel, profilePicturePath, image, size));
        }else{
            ImageCache.loadUncached(profilePicturePath, size, image -> applyIfStillCurrent(avatarLabel, profilePicturePath, image, size));
        }
    }

    private static void applyIfStillCurrent(Label avatarLabel, String profilePicturePath, Image image, int size){
        if(!profilePicturePath.equals(avatarLabel.getProperties().get("currentPicture"))){
            return;
        }

        avatarLabel.setText(null);

        ImageView imageView = new ImageView(image);

        imageView.setFitHeight(size);
        imageView.setFitWidth(size);

        double clipSize = size / 2.0;

        Circle clip = new Circle(clipSize, clipSize, clipSize);

        imageView.setClip(clip);

        avatarLabel.setGraphic(imageView);
    }
}