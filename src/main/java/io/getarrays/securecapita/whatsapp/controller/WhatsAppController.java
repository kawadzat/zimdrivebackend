package io.getarrays.securecapita.whatsapp.controller;

import io.getarrays.securecapita.domain.User;
import io.getarrays.securecapita.exception.CustomMessage;
import io.getarrays.securecapita.whatsapp.dto.WhatsAppMessageDto;
import io.getarrays.securecapita.whatsapp.dto.WhatsAppResponseDto;
import io.getarrays.securecapita.whatsapp.service.WhatsAppService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/whatsapp")
public class WhatsAppController {

    @Autowired
    private WhatsAppService whatsAppService;

    /**
     * Send WhatsApp message to all users in the system
     * POST /whatsapp/send-to-all
     */
    @PostMapping("/send-to-all")
    public ResponseEntity<?> sendMessageToAllUsers(@Valid @RequestBody WhatsAppMessageDto messageDto) {
        try {
            WhatsAppResponseDto response = whatsAppService.sendMessageToAllUsers(messageDto);
            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new CustomMessage("Configuration error: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new CustomMessage("Error sending messages: " + e.getMessage()));
        }
    }

    /**
     * Send WhatsApp message to a specific user by phone number
     * POST /whatsapp/send
     */
    @PostMapping("/send")
    public ResponseEntity<?> sendMessageToUser(@Valid @RequestBody WhatsAppMessageDto messageDto) {
        if (messageDto.getPhoneNumber() == null || messageDto.getPhoneNumber().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new CustomMessage("Phone number is required"));
        }

        try {
            boolean success = whatsAppService.sendMessageToUser(
                    messageDto.getPhoneNumber(),
                    messageDto.getMessage()
            );

            if (success) {
                return ResponseEntity.ok(new CustomMessage("WhatsApp message sent successfully"));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new CustomMessage("Failed to send WhatsApp message"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new CustomMessage("Error sending message: " + e.getMessage()));
        }
    }

    /**
     * Get all users with phone numbers
     * GET /whatsapp/users-with-phones
     */
    @GetMapping("/users-with-phones")
    public ResponseEntity<List<User>> getUsersWithPhoneNumbers() {
        List<User> users = whatsAppService.getUsersWithPhoneNumbers();
        return ResponseEntity.ok(users);
    }
}


