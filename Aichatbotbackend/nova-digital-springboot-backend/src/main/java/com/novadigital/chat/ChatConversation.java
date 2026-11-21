package com.novadigital.chat;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_conversations")
public class ChatConversation {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 80) private String sessionId;
    @Column(nullable = false, length = 1000) private String userMessage;
    @Column(nullable = false, columnDefinition = "TEXT") private String aiResponse;
    @Column(nullable = false) private LocalDateTime createdAt = LocalDateTime.now();

    protected ChatConversation() {}
    public ChatConversation(String sessionId, String userMessage, String aiResponse) {
        this.sessionId=sessionId; this.userMessage=userMessage; this.aiResponse=aiResponse;
    }
}
