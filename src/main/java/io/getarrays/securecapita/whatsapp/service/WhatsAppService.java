package io.getarrays.securecapita.whatsapp.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import io.getarrays.securecapita.domain.User;
import io.getarrays.securecapita.repository.implementation.UserRepository1;
import io.getarrays.securecapita.whatsapp.dto.WhatsAppMessageDto;
import io.getarrays.securecapita.whatsapp.dto.WhatsAppResponseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class WhatsAppService {

    private static final Logger logger = LoggerFactory.getLogger(WhatsAppService.class);

    @Autowired
    private UserRepository1 userRepository;

    @Value("${twilio.account.sid:}")
    private String accountSid;

    @Value("${twilio.auth.token:}")
    private String authToken;

    @Value("${twilio.whatsapp.from:whatsapp:+14155238886}")
    private String fromNumber;

    /**
     * Send WhatsApp message to all users in the system
     */
    public WhatsAppResponseDto sendMessageToAllUsers(WhatsAppMessageDto messageDto) {
        // Initialize Twilio if credentials are provided
        if (accountSid != null && !accountSid.isEmpty() && authToken != null && !authToken.isEmpty()) {
            Twilio.init(accountSid, authToken);
        }

        List<User> allUsers = userRepository.findAll();
        List<String> successPhones = new ArrayList<>();
        List<String> failedPhones = new ArrayList<>();
        int successfulSends = 0;
        int failedSends = 0;

        for (User user : allUsers) {
            if (user.getPhone() != null && !user.getPhone().trim().isEmpty()) {
                try {
                    String phoneNumber = formatPhoneNumber(user.getPhone());
                    sendWhatsAppMessage(phoneNumber, messageDto.getMessage());
                    successPhones.add(user.getPhone());
                    successfulSends++;
                    logger.info("WhatsApp message sent successfully to: {}", user.getPhone());
                } catch (Exception e) {
                    failedPhones.add(user.getPhone() + " (" + e.getMessage() + ")");
                    failedSends++;
                    logger.error("Failed to send WhatsApp message to {}: {}", user.getPhone(), e.getMessage());
                }
            } else {
                failedPhones.add(user.getEmail() + " (no phone number)");
                failedSends++;
            }
        }

        return new WhatsAppResponseDto(
                allUsers.size(),
                successfulSends,
                failedSends,
                successPhones,
                failedPhones,
                messageDto.getMessage()
        );
    }

    /**
     * Send WhatsApp message to a specific user by phone number
     */
    public boolean sendMessageToUser(String phoneNumber, String message) {
        try {
            if (accountSid != null && !accountSid.isEmpty() && authToken != null && !authToken.isEmpty()) {
                Twilio.init(accountSid, authToken);
            }
            String formattedPhone = formatPhoneNumber(phoneNumber);
            sendWhatsAppMessage(formattedPhone, message);
            logger.info("WhatsApp message sent successfully to: {}", phoneNumber);
            return true;
        } catch (Exception e) {
            logger.error("Failed to send WhatsApp message to {}: {}", phoneNumber, e.getMessage());
            return false;
        }
    }

    /**
     * Send WhatsApp message using Twilio API
     */
    private void sendWhatsAppMessage(String toPhoneNumber, String messageBody) {
        if (accountSid == null || accountSid.isEmpty() || authToken == null || authToken.isEmpty()) {
            throw new IllegalStateException("Twilio credentials not configured. Please set twilio.account.sid and twilio.auth.token in application.yml");
        }

        try {
            Message message = Message.creator(
                    new PhoneNumber(toPhoneNumber),
                    new PhoneNumber(fromNumber),
                    messageBody
            ).create();

            logger.info("WhatsApp message sent. SID: {}", message.getSid());
        } catch (Exception e) {
            logger.error("Error sending WhatsApp message via Twilio: {}", e.getMessage());
            throw new RuntimeException("Failed to send WhatsApp message: " + e.getMessage(), e);
        }
    }

    /**
     * Format phone number to E.164 format (required by Twilio)
     * Example: +1234567890 or whatsapp:+1234567890
     */
    private String formatPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Phone number cannot be empty");
        }

        // Remove all non-digit characters except +
        String cleaned = phoneNumber.replaceAll("[^+\\d]", "");

        // If it doesn't start with +, add it (assuming country code)
        if (!cleaned.startsWith("+")) {
            // You may need to adjust this based on your country code
            // For example, if numbers start with 0, remove it and add country code
            if (cleaned.startsWith("0")) {
                cleaned = "+27" + cleaned.substring(1); // Example for South Africa
            } else {
                cleaned = "+" + cleaned; // Add + prefix
            }
        }

        // Add whatsapp: prefix if not already present
        if (!cleaned.startsWith("whatsapp:")) {
            cleaned = "whatsapp:" + cleaned;
        }

        return cleaned;
    }

    /**
     * Get all users with phone numbers
     */
    public List<User> getUsersWithPhoneNumbers() {
        return userRepository.findAll().stream()
                .filter(user -> user.getPhone() != null && !user.getPhone().trim().isEmpty())
                .collect(Collectors.toList());
    }
}


