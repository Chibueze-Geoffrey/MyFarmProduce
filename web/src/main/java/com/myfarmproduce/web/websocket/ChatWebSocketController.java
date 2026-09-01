package com.myfarmproduce.web.websocket;

import com.myfarmproduce.application.service.ChatService;
import com.myfarmproduce.web.security.AppPrincipal;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Real-time community chat. Persists each message and broadcasts to all clients. */
@Controller
public class ChatWebSocketController {

    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("d MMM yyyy, h:mm a", Locale.US).withZone(ZoneId.systemDefault());

    private final ChatService chat;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatWebSocketController(ChatService chat, SimpMessagingTemplate messagingTemplate) {
        this.chat = chat;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/chat.send")
    public void send(ChatOutgoingMessage payload, Principal principal) {
        String content = payload.content() == null ? "" : payload.content().trim();
        if (content.isEmpty()) return;
        if (content.length() > 2000) content = content.substring(0, 2000);

        AppPrincipal appPrincipal = principalOf(principal);
        var saved = chat.addMessage(appPrincipal.getId(), appPrincipal.getName(), content);

        messagingTemplate.convertAndSend("/topic/chat",
                new ChatBroadcastMessage(saved.getSenderName(), saved.getContent(), TIME_FORMAT.format(saved.getCreatedAt())));
    }

    private static AppPrincipal principalOf(Principal principal) {
        if (principal instanceof Authentication auth && auth.getPrincipal() instanceof AppPrincipal ap) return ap;
        throw new IllegalStateException("Not authenticated.");
    }

    public record ChatOutgoingMessage(String content) {}

    public record ChatBroadcastMessage(String senderName, String content, String time) {}
}
