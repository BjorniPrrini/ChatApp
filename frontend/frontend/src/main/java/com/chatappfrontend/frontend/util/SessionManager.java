package com.chatappfrontend.frontend.util;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SessionManager {
    private static SessionManager instance;
    private String token;
    private Long userId;
    private String nickname;
    private String profilePicture;

    private SessionManager(){}

    public static SessionManager getInstance(){
        if(instance == null){
            instance = new SessionManager();
        }

        return instance;
    }

    public void clear(){
        token = null;
        userId = null;
        nickname = null;
        profilePicture = null;
    }
}