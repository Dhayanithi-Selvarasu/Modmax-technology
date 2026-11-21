package com.novadigital.contact;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "contact_messages")
public class ContactMessage {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 80) private String name;
    @Column(nullable = false, length = 160) private String email;
    @Column(nullable = false, length = 140) private String subject;
    @Column(nullable = false, length = 2000) private String message;
    @Column(nullable = false, length = 20) private String status = "new";
    @Column(nullable = false) private LocalDateTime createdAt = LocalDateTime.now();

    protected ContactMessage() {}
    public ContactMessage(String name, String email, String subject, String message) {
        this.name=name; this.email=email; this.subject=subject; this.message=message;
    }
    public Long getId(){return id;}
    public String getName(){return name;}
    public String getEmail(){return email;}
    public String getSubject(){return subject;}
    public String getMessage(){return message;}
    public String getStatus(){return status;}
    public LocalDateTime getCreatedAt(){return createdAt;}
}
