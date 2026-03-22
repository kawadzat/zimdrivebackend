package io.getarrays.securecapita.whatsapp.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WhatsAppResponseDto {
    private int totalUsers;
    private int successfulSends;
    private int failedSends;
    private List<String> successPhones;
    private List<String> failedPhones;
    private String message;
}


