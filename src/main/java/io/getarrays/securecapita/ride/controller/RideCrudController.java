package io.getarrays.securecapita.ride.controller;

import io.getarrays.securecapita.domain.PageResponseDto;
import io.getarrays.securecapita.dto.UserDTO;
import io.getarrays.securecapita.exception.CustomMessage;
import io.getarrays.securecapita.ride.dto.RideDto;
import io.getarrays.securecapita.ride.service.RideService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import static io.getarrays.securecapita.utils.UserUtils.getAuthenticatedUser;

@RestController
@RequestMapping("/rides")
public class RideCrudController {

    @Autowired
    private RideService rideService;

    @PostMapping
    public ResponseEntity<RideDto> createRide(@Valid @RequestBody RideDto rideDto) {
        RideDto createdRide = rideService.createRide(rideDto);
        return new ResponseEntity<>(createdRide, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<PageResponseDto<RideDto>> getAllRides(
            Authentication authentication,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        UserDTO user = getAuthenticatedUser(authentication);
        PageResponseDto<RideDto> rides = rideService.getRidesForViewer(user, authentication, page, size);
        return new ResponseEntity<>(rides, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RideDto> getRideById(Authentication authentication, @PathVariable Long id) {
        UserDTO user = getAuthenticatedUser(authentication);
        return new ResponseEntity<>(rideService.getRideByIdForViewer(user, authentication, id), HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RideDto> updateRide(@PathVariable Long id,
                                              @Valid @RequestBody RideDto rideDto) {
        RideDto updatedRide = rideService.updateRide(id, rideDto);
        return new ResponseEntity<>(updatedRide, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRide(@PathVariable Long id) {
        rideService.deleteRide(id);
        return new ResponseEntity<>(new CustomMessage("Ride deleted successfully"), HttpStatus.OK);
    }
}
