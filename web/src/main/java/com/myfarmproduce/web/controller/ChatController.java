package com.myfarmproduce.web.controller;

import com.myfarmproduce.application.service.ChatService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/chat")
public class ChatController {

    private final ChatService chat;

    public ChatController(ChatService chat) {
        this.chat = chat;
    }

    @GetMapping
    public String index(Model model) {
        model.addAttribute("messages", chat.getRecent(50));
        return "chat/index";
    }
}
