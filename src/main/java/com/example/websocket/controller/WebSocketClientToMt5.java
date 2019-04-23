package com.example.websocket.controller;

import com.alibaba.fastjson.JSON;
import com.example.websocket.config.RabbitMqConfig;
import com.example.websocket.entity.Info;
import com.example.websocket.service.WebSocketToApp2;
import lombok.Data;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static org.apache.logging.log4j.message.MapMessage.MapFormat.JSON;

@Data
@Component
public class WebSocketClientToMt5 {

    static Logger log = LoggerFactory.getLogger(WebSocketClientToMt5.class);
    public static String sId; //用来区分是哪个客户端存到的数据

    //引入连接mt5服务器的websocket客户端,使用它来想mt5服务器发送请求获取数据
    public static WebSocketClient webSocketClientToMt5;

    public static RabbitTemplate rabbitTemplate;
    @Autowired
    public WebSocketClientToMt5(RabbitTemplate rabbitTemplate){
        this.rabbitTemplate = rabbitTemplate;
    }
    public WebSocketClientToMt5() { }

    static {
        try {
            webSocketClientToMt5 = new WebSocketClient(new URI("ws://api.digiexclub.com:8090"), new Draft_6455()) {
                @Override
                public void onOpen(ServerHandshake serverHandshake) {
                    log.info("连接Mt5服务器成功");
                }

                @Override
                public void onMessage(String message) {
                    log.info("收到mt5返回的数据:" + message);
                    //将消息存到mq中
                    Info info = new Info(sId,message);
                    //将消息对象info转换成字节码数据，存到mq中
                    byte[] bytes = com.alibaba.fastjson.JSON.toJSONString(info).getBytes();
                    rabbitTemplate.convertAndSend(RabbitMqConfig.EXCHANGE, RabbitMqConfig.ROUTINGKEY, bytes, new MessagePostProcessor() {
                        @Override
                        public Message postProcessMessage(Message message) throws AmqpException {
                            return message;
                        }
                    });
                }

                @Override
                public void onClose(int code, String reson, boolean b) {
                    log.info("连接Mt5服务器关闭:" + code + reson );
                }

                @Override
                public void onError(Exception e) {
                    log.error("连接mt5服务器发生错误");
                    e.printStackTrace();
                }
            };
            webSocketClientToMt5.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }






}
