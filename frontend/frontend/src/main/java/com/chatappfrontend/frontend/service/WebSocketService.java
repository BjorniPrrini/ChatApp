package com.chatappfrontend.frontend.service;

import com.chatappfrontend.frontend.model.MessageEventDTO;
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
    private Consumer<MessageEventDTO> messageHandler;
    private String currentSubscription;

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
                        System.out.println("WebSocket closed — status: " + statusCode + ", reason: " + reason);

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
            if(currentSubscription != null){
                sendSubscribeFrame(currentSubscription);
            }
        }else if(frame.startsWith("MESSAGE")){
            int bodyStart = frame.indexOf("\n\n") + 2;

            if(bodyStart > 1){
                String body = frame.substring(bodyStart).replace("\0", "");

                try {
                    MessageEventDTO event = objectMapper.readValue(body, MessageEventDTO.class);

                    if(messageHandler != null){
                        messageHandler.accept(event);
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
        this.messageHandler = onMessage;
        this.currentSubscription = "/topic/conversation." + conversationId;

        if(webSocket != null){
            sendSubscribeFrame(currentSubscription);
        }
    }

    private void sendSubscribeFrame(String destination){
        String subscribeFrame = "SUBSCRIBE\nid:sub-0\ndestination:" + destination + "\n\n\0";

        webSocket.sendText(subscribeFrame, true);
    }

    public void unsubscribe(){
        if(webSocket != null && currentSubscription != null){
            String unsubscribeFrame = "UNSUBSCRIBE\nid:sub-0\n\n\0";

            webSocket.sendText(unsubscribeFrame, true);
            currentSubscription = null;
            messageHandler = null;
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
}