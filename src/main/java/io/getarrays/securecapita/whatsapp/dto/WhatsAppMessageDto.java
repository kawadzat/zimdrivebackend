package io.getarrays.securecapita.whatsapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WhatsAppMessageDto {
    
    @NotBlank(message = "Message content is required")
    @Size(max = 1600, message = "Message must not exceed 1600 characters")
    private String message;
    
    private String phoneNumber; // Optional: for sending to specific user
}


