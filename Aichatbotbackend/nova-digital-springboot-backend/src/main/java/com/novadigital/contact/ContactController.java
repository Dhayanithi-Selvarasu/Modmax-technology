package com.novadigital.contact;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contact")
public class ContactController {
    private final ContactRepository repository;
    public ContactController(ContactRepository repository) { this.repository = repository; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ContactResponse submit(@Valid @RequestBody ContactRequest request) {
        ContactMessage saved = repository.save(new ContactMessage(
            request.name().trim(), request.email().trim(), request.subject().trim(), request.message().trim()
        ));
        return new ContactResponse(true, saved.getId(), "Your message has been received.");
    }

    public record ContactResponse(boolean ok, Long id, String message) {}
}
