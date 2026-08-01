package com.chatappfrontend.frontend.service;

import com.chatappfrontend.frontend.model.MessageEventDTO;
import com.chatappfrontend.frontend.model.UserStatusEventDTO;
import com.chatappfrontend.frontend.util.AppConfig;
import com.chatappfrontend.frontend.util.JsonMapper;
import com.chatappfrontend.frontend.util.SessionManager;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;

public class WebSocketService {
    private WebSocket webSocket;
    private final ObjectMapper objectMapper = JsonMapper.get();
    private Consumer<MessageEventDTO> conversationHandler;
    private String conversationDestination;
    private Consumer<MessageEventDTO> userHandler;
    private String userDestination;
    private Consumer<UserStatusEventDTO> statusHandler;
    private String statusDestination;

    public void connect() throws Exception{
        String token = SessionManager.getInstance().getToken();
        String url = AppConfig.get("api.base.url").replace("http", "ws") + "/ws";
        HttpClient client = HttpClient.newHttpClient();

        webSocket = client.newWebSocketBuilder()
                .header("Authorization", "Bearer " + token)
                .buildAsync(URI.create(url), new WebSocket.Listener(){
                    private StringBuilder buffer = new StringBuilder();

                    @Override
                    public void onOpen(WebSocket ws){
                        String connectFrame = "CONNECT\naccept-version:1.2\nheart-beat:0,0\nAuthorization:Bearer " + token + "\n\n\0";

                        ws.sendText(connectFrame, true);
                        ws.request(1);
                    }

                    @Override
                    public CompletionStage<?> onText(WebSocket ws, CharSequence data, boolean last){
                        buffer.append(data);

                        if(last){
                            String frame = buffer.toString();

                            buffer = new StringBuilder();

                            handleFrame(frame);
                        }

                        ws.request(1);

                        return CompletableFuture.completedFuture(null);
                    }

                    @Override
                    public CompletionStage<?> onClose(WebSocket ws, int statusCode, String reason){
                        return CompletableFuture.completedFuture(null);
                    }

                    @Override
                    public void onError(WebSocket ws, Throwable error){
                        error.printStackTrace();
                    }
                }).get();
    }

    private void handleFrame(String frame){
        if(frame.startsWith("CONNECTED")){
            if(conversationDestination != null){
                sendSubscribeFrame(conversationDestination, "sub-conversation");
            }

            if(userDestination != null){
                sendSubscribeFrame(userDestination, "sub-user");
            }

            if(statusDestination != null){
                sendSubscribeFrame(statusDestination, "sub-status");
            }
        }else if(frame.startsWith("MESSAGE")){
            int bodyStart = frame.indexOf("\n\n") + 2;

            if(bodyStart > 1){
                String body = frame.substring(bodyStart).replace("\0", "");
                String subscriptionId = extractSubscriptionId(frame);

                try {
                    if(subscriptionId.equals("sub-conversation") && conversationHandler != null){
                        MessageEventDTO event = objectMapper.readValue(body, MessageEventDTO.class);

                        conversationHandler.accept(event);
                    }else if(subscriptionId.equals("sub-user") && userHandler != null){
                        MessageEventDTO event = objectMapper.readValue(body, MessageEventDTO.class);

                        userHandler.accept(event);
                    }else if(subscriptionId.equals("sub-status") && statusHandler != null){
                        UserStatusEventDTO event = objectMapper.readValue(body, UserStatusEventDTO.class);

                        statusHandler.accept(event);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }else if(frame.startsWith("ERROR")){
            System.err.println("STOMP ERROR frame received:\n" + frame);
        }
    }

    public void subscribe(Long conversationId, Consumer<MessageEventDTO> onMessage){
        this.conversationHandler = onMessage;
        this.conversationDestination = "/topic/conversation." + conversationId;

        if(webSocket != null){
            sendSubscribeFrame(conversationDestination, "sub-conversation");
        }
    }

    public void subscribeToUser(Long userId, Consumer<MessageEventDTO> onMessage){
        this.userHandler = onMessage;
        this.userDestination = "/queue/user." + userId;

        if(webSocket != null){
            sendSubscribeFrame(userDestination, "sub-user");
        }
    }

    public void unsubscribe(){
        if(webSocket != null && conversationDestination != null){
            String unsubscribeFrame = "UNSUBSCRIBE\nid:sub-conversation\n\n\0";

            webSocket.sendText(unsubscribeFrame, true);

            conversationDestination = null;
            conversationHandler = null;
        }
    }

    public void disconnect(){
        if(webSocket != null){
            String disconnectFrame = "DISCONNECT\n\n\0";

            webSocket.sendText(disconnectFrame, true);
            webSocket.abort();
            webSocket = null;
        }
    }

    public void sendReadReceipt(Long conversationId){
        if(webSocket != null){
            String destination = "/app/chat.read";
            String body = String.valueOf(conversationId);
            String sendFrame = "SEND\ndestination:" + destination + "\ncontent-type:application/json\n\n" + body + "\0";

            webSocket.sendText(sendFrame, true);
        }
    }

    public void sendDeliveredReceipt(Long messageId){
        if(webSocket != null){
            String destination = "/app/chat.delivered";
            String body = String.valueOf(messageId);
            String sendFrame = "SEND\ndestination:" + destination + "\ncontent-type:application/json\n\n" + body + "\0";

            webSocket.sendText(sendFrame, true);
        }
    }

    public void sendMarkAllDeliveredRequest(){
        if(webSocket != null){
            String destination = "/app/chat.markAllDelivered";
            String sendFrame = "SEND\ndestination:" + destination + "\ncontent-type:application/json\n\n\0";

            webSocket.sendText(sendFrame, true);
        }
    }

    public void sendOnlineStatusRequest(){
        if(webSocket != null){
            String destination = "/app/user.online";
            String sendFrame = "SEND\ndestination:" + destination + "\ncontent-type:application/json\n\n\0";

            webSocket.sendText(sendFrame, true);
        }
    }

    public void subscribeToStatus(Long userId, Consumer<UserStatusEventDTO> onStatusChange){
        this.statusHandler = onStatusChange;
        this.statusDestination = "/queue/user." + userId + ".status";

        if(webSocket != null){
            sendSubscribeFrame(statusDestination, "sub-status");
        }
    }

    private void sendSubscribeFrame(String destination, String id){
        String subscribeFrame = "SUBSCRIBE\nid:" + id + "\ndestination:" + destination + "\n\n\0";

        webSocket.sendText(subscribeFrame, true);
    }

    private String extractSubscriptionId(String frame){
        int startIndex = frame.indexOf("subscription:") + "subscription:".length();
        int endIndex = frame.indexOf("\n", startIndex);

        return frame.substring(startIndex, endIndex);
    }
}