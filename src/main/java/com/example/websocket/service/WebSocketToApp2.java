package com.example.websocket.service;

import com.alibaba.fastjson.JSON;
import com.example.websocket.controller.WebSocketClientToMt5;
import com.example.websocket.entity.Info;
import com.rabbitmq.client.Channel;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket是类似客户端服务端的形式(采用ws协议)，
 * 那么这里的WebSocketServer其实就相当于一个ws协议的Controller
 * websocket建立一次连接就会new一个WebSocketToApp2对象,所以这里是多例的
 */
@Data
@Component
@ServerEndpoint(value = "/websocket/c/{sid}")
public class WebSocketToApp2 {
    static Logger log = LoggerFactory.getLogger(WebSocketToApp2.class);
    //静态变量，用来记录当前在线连接数.应该把它设计成线程安全的.
    private static int onlineCount = 0;
    //concurrent包的线程安全Set,用来存放每个客户端对应的WebSocketToApp2
    private static ConcurrentHashMap<String, WebSocketToApp2> WebSocketToApp2S = new ConcurrentHashMap<>();
    //与某个客户端的连接会话，需要通过它来给客户端发送数据
    private  Session session;
    //用来存放请求传递过来的参数,判断当前拿到的mq消息要不要消费
    private  String sid;

    /**连接建立成功调用的方法
     * @param session
     * @param sid
     */
    @OnOpen
    public void onOpent(Session session, @PathParam("sid") String sid){
        this.session = session;
        this.sid = sid;
        WebSocketToApp2S.put(sid,this);//this是每次请求的时候，创建的WebSocketToApp2对象
        addOnlineCount();//连接数量+1
        log.info("前端连接后台WebSocket服务成功" + sid + "," + "当前连接的数量为" +WebSocketToApp2S.size() );
        try {
            sendMessage("连接成功");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
    * 连接关闭调用的方法*/
    @OnClose
    public void onClose(){
        WebSocketToApp2S.remove(this);//将当前连接的websocket对象从集合中删除，让连接数正确
        subOnlineCount();           //在线数减1
        log.info("有一连接关闭！当前连接数为" + getOnlineCount());
    }

    /**收到客户端消息后调用的方法
     * @param message 客户端发送过来的消息
     * @param session
     */
    @OnMessage
    public void onMessage(String message, Session session){
        /*方案一:
        *调用webSocketClient到mt5查询数据
        * 将前端传过来的sid 合并到message中，到那边再拆出来， 存放到RabbitMQ中，WebSocketToApp2监听MQ,
        * 只要MQ中有消息就遍历所有的连接,在找到sid相等的 WebSocketToApp2对象,然后推送数据给前端aqq
         */
        log.info("收到来着窗口" + sid + "的消息:" + message);
        this.sid = sid;
        WebSocketClientToMt5.sId = sid;
        WebSocketClientToMt5.webSocketClientToMt5.send(message);
        System.out.println("发送完成");
    }

    @OnError
    public void onError(Session session, Throwable error){
        log.error("发生错误");
        error.printStackTrace();
    }

    /**
     *  从mq获取到的消息
     *  需要解决的问题,拿到的消息要不要消费的问题:因为mq中的消息是多个客户端发送请求得到数据存到在mq中的，
     *            这时候监听拿到的消息可能是其他客户端的数据.要判断数据是否是当前客户端的数据，是就消费，不是将消息放回去
     * @param msg :解码后的消息
     *
     *
     */
    @RabbitListener(queues ="test_queue_1")
    public void getMessageFromMqToApp(byte[] msg){
        Info info = null;
        try {
            String s = new String(msg,"utf-8");
            info = JSON.parseObject(s, Info.class);
            //从mq中取出消息
            String uid = info.getSId();
            String message = info.getMessage();
            log.info("从mq中接受到"+uid+"的数据:" + message);
            //根据uid获取到对应的webSocketToApp2对象
            WebSocketToApp2 webSocketToApp2 = WebSocketToApp2S.get(uid);
            //将数据返回给前端
            webSocketToApp2.sendMessage(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**服务器向客户端主动推送消息
     * @param message 消息
     */
    public void sendMessage(String message) throws IOException {
        if (this.session != null){
            this.session.getBasicRemote().sendText(message);
        }

    }
    public static synchronized int getOnlineCount() {
        return onlineCount;
    }

    public static synchronized void addOnlineCount() {
        WebSocketToApp2.onlineCount++;
    }

    public static synchronized void subOnlineCount() {
        WebSocketToApp2.onlineCount--;
    }
}

