package io.getarrays.securecapita.client.dto;

import io.getarrays.securecapita.client.Client;
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
public class ClientDto {
    private Long id;

    @NotBlank(message = "Name must not be empty")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    @NotBlank(message = "Surname must not be empty")
    @Size(max = 100, message = "Surname must not exceed 100 characters")
    private String surname;

    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    private String phoneNumber;

    public static ClientDto toDto(Client client) {
        if (client == null) {
            return null;
        }
        return new ClientDto(
                client.getId(),
                client.getName(),
                client.getSurname(),
                client.getPhoneNumber()
        );
    }
}


