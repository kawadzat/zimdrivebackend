package io.getarrays.securecapita.ride.dto;

import io.getarrays.securecapita.ride.Ride;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RideDto {
    private Long id;

    @NotBlank(message = "Car name must not be empty")
    @Size(max = 255)
    private String carName;

    @NotNull(message = "Year of manufacture is required")
    @Min(value = 1900, message = "Year must be at least 1900")
    private Integer yearOfManufacture;

    @NotBlank(message = "Registration number must not be empty")
    @Size(max = 50)
    private String registrationNumber;

    @NotNull(message = "Driver is required")
    private Long driverId;

    @NotNull(message = "Vehicle is required - only approved vehicles can be used for rides")
    private Long vehicleId;

    public static RideDto toDto(Ride ride) {
        if (ride == null) {
            return null;
        }
        RideDto dto = new RideDto();
        dto.setId(ride.getId());
        dto.setCarName(ride.getCarName());
        dto.setYearOfManufacture(ride.getYearOfManufacture());
        dto.setRegistrationNumber(ride.getRegistrationNumber());
        if (ride.getDriver() != null) {
            dto.setDriverId(ride.getDriver().getId());
        }
        if (ride.getVehicle() != null) {
            dto.setVehicleId(ride.getVehicle().getId());
        }
        return dto;
    }
}
