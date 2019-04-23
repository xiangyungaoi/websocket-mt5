package com.example.websocket.config;

import com.example.websocket.controller.WebSocketClientToMt5;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

import java.net.URI;

@Configuration
@Component
public class WebSocketConfig {
    static Logger log = LoggerFactory.getLogger(WebSocketConfig.class);

    /**
     * 开启WebSocket支持
     * ServerEndpointExporter作用
     * 这个Bean会自动注册使用@ServerEndpoint注解声明的websocket endpoint
     *
     * @return
     */
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }


    /**连接mt5服务的客户端
     * 使用该客户端想mt5服务器发送消息
     * @return
     */
   /* @Bean
    public WebSocketClientToMt5 webSocketClientToMt5() {
        WebSocketClientToMt5 webSocketClientToMt5 = new WebSocketClientToMt5();   //连接mt5服务器的websocket
        try {
            WebSocketClient webSocketClient = new WebSocketClient(new URI("ws://api.digiexclub.com:8090"),new Draft_6455()) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    log.info("[websocket] 连接mt5服务器成功");
                }

                @Override
                public void onMessage(String message) {
                    log.info("[websocket] 收到消息={}",message);
                    System.out.println(message);
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    System.out.println(code);
                    System.out.println(reason);
                    log.info("[websocket] 退出mt5服务器连接");
                }

                @Override
                public void onError(Exception ex) {
                    log.info("[websocket] 连接错误={}",ex.getMessage());
                }
            };
            webSocketClient.connect();
            webSocketClientToMt5.setWebSocketClientToMt5(webSocketClient);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return webSocketClientToMt5;
    }*/





}
