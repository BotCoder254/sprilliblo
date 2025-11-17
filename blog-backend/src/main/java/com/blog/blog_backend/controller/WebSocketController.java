package com.blog.blog_backend.controller;

import com.blog.blog_backend.dto.NotificationResponse;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.beans.factory.annotation.Autowired;

@Controller
public class WebSocketController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/hello")
    @SendTo("/topic/greetings")
    public String greeting(String message) throws Exception {
        Thread.sleep(1000); // simulated delay
        return "Hello, " + message + "!";
    }

    public void sendNotificationToUser(String userId, NotificationResponse notification) {
        messagingTemplate.convertAndSendToUser(
            userId, 
            "/queue/notifications", 
            notification
        );
    }

    public void sendDashboardUpdate(String tenantId, Object update) {
        messagingTemplate.convertAndSend(
            "/topic/dashboard/" + tenantId, 
            update
        );
    }

    public void sendCommentUpdate(String tenantId, Object update) {
        messagingTemplate.convertAndSend(
            "/topic/comments/" + tenantId, 
            update
        );
    }

    public void sendPostUpdate(String tenantId, Object update) {
        messagingTemplate.convertAndSend(
            "/topic/posts/" + tenantId, 
            update
        );
    }
}