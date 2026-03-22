package io.getarrays.securecapita.ride.service;

import io.getarrays.securecapita.driver.Driver;
import io.getarrays.securecapita.driver.DriverRepository;
import io.getarrays.securecapita.driver.Vehicle;
import io.getarrays.securecapita.driver.VehicleRepository;
import io.getarrays.securecapita.driver.VehicleStatus;
import io.getarrays.securecapita.dto.UserDTO;
import io.getarrays.securecapita.exception.ResourceNotFoundException;
import io.getarrays.securecapita.ride.Ride;
import io.getarrays.securecapita.ride.RideRepository;
import io.getarrays.securecapita.ride.dto.RideDto;
import io.getarrays.securecapita.domain.PageResponseDto;
import io.getarrays.securecapita.roles.prerunner.ROLE_AUTH;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class RideService {

    @Autowired
    private RideRepository rideRepository;

    @Autowired
    private DriverRepository driverRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Transactional
    public RideDto createRide(RideDto rideDto) {
        Ride ride = dtoToEntity(null, rideDto);
        return RideDto.toDto(rideRepository.save(ride));
    }

    /**
     * Drivers with at least one approved vehicle see only their rides.
     * Users with {@link ROLE_AUTH#APPROVE_VEHICLE} (e.g. admins) see all rides.
     */
    @Transactional(readOnly = true)
    public PageResponseDto<RideDto> getRidesForViewer(UserDTO user, Authentication authentication, int page, int size) {
        if (canViewAllRides(authentication)) {
            return getAllRides(page, size);
        }
        Driver driver = driverRepository.findByUser_Id(user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "A driver profile is required to view rides."));
        if (!vehicleRepository.existsByDriver_IdAndStatus(driver.getId(), VehicleStatus.APPROVED)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You need at least one approved vehicle before you can view rides.");
        }
        Page<Ride> ridePage = rideRepository.findByDriver_Id(driver.getId(), PageRequest.of(page, size));
        return new PageResponseDto<>(
                ridePage.getContent().stream().map(RideDto::toDto).toList(),
                ridePage
        );
    }

    @Transactional(readOnly = true)
    public RideDto getRideByIdForViewer(UserDTO user, Authentication authentication, Long id) {
        Ride ride = findRideByIdOrThrow(id);
        if (canViewAllRides(authentication)) {
            return RideDto.toDto(ride);
        }
        Driver driver = driverRepository.findByUser_Id(user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "A driver profile is required to view rides."));
        if (!vehicleRepository.existsByDriver_IdAndStatus(driver.getId(), VehicleStatus.APPROVED)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You need at least one approved vehicle before you can view rides.");
        }
        if (!ride.getDriver().getId().equals(driver.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only view your own rides.");
        }
        return RideDto.toDto(ride);
    }

    private static boolean canViewAllRides(Authentication authentication) {
        if (authentication == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(ROLE_AUTH.APPROVE_VEHICLE.name()::equals);
    }

    public PageResponseDto<RideDto> getAllRides(int page, int size) {
        Page<Ride> ridePage = rideRepository.findAll(PageRequest.of(page, size));
        return new PageResponseDto<>(
                ridePage.getContent().stream().map(RideDto::toDto).toList(),
                ridePage
        );
    }

    public RideDto getRideById(Long id) {
        return RideDto.toDto(findRideByIdOrThrow(id));
    }

    @Transactional
    public RideDto updateRide(Long id, RideDto rideDto) {
        Ride ride = dtoToEntity(id, rideDto);
        return RideDto.toDto(rideRepository.save(ride));
    }

    @Transactional
    public void deleteRide(Long id) {
        if (rideRepository.existsById(id)) {
            rideRepository.deleteById(id);
        } else {
            throw new ResourceNotFoundException("Ride not found with id " + id);
        }
    }

    private Ride dtoToEntity(Long id, RideDto dto) {
        Ride ride = id != null ? findRideByIdOrThrow(id) : new Ride();
        Vehicle vehicle = vehicleRepository.findById(dto.getVehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id " + dto.getVehicleId()));
        if (vehicle.getStatus() != VehicleStatus.APPROVED) {
            throw new IllegalArgumentException("Only approved vehicles can be used for rides. Vehicle status: " + vehicle.getStatus());
        }
        Driver driver = driverRepository.findById(dto.getDriverId())
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found with id " + dto.getDriverId()));
        if (!vehicle.getDriver().getId().equals(driver.getId())) {
            throw new IllegalArgumentException("Vehicle does not belong to the specified driver");
        }
        ride.setCarName(dto.getCarName());
        ride.setYearOfManufacture(dto.getYearOfManufacture());
        ride.setRegistrationNumber(dto.getRegistrationNumber());
        ride.setDriver(driver);
        ride.setVehicle(vehicle);
        return ride;
    }

    private Ride findRideByIdOrThrow(Long id) {
        return rideRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ride not found with id " + id));
    }
}
