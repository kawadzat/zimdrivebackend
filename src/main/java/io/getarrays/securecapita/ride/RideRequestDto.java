package io.getarrays.securecapita.ride;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RideRequestDto {
    @NotEmpty(message = "From location cannot be empty")
    private String from;

    @NotEmpty(message = "To location cannot be empty")
    private String to;
}

