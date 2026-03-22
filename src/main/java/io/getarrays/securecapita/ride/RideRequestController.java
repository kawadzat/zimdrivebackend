package io.getarrays.securecapita.ride;

import io.getarrays.securecapita.domain.HttpResponse;
import io.getarrays.securecapita.dto.UserDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static io.getarrays.securecapita.utils.UserUtils.getAuthenticatedUser;
import static java.time.LocalDateTime.now;
import static java.util.Map.of;
import static org.springframework.http.HttpStatus.CREATED;

/**
 * Passenger ride requests (distinct from {@code /rides} CRUD for driver ride records).
 */
@RestController
@RequestMapping("/ride")
@RequiredArgsConstructor
public class RideRequestController {

    private final RideRequestService rideRequestService;

    @PostMapping("/request")
    public ResponseEntity<HttpResponse> createRideRequest(Authentication authentication,
                                                          @Valid @RequestBody RideRequestDto dto) {
        UserDTO user = getAuthenticatedUser(authentication);
        RideRequest created = rideRequestService.create(user, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .statusCode(CREATED.value())
                        .status(CREATED)
                        .message("Ride request created")
                        .data(of(
                                "id", created.getId(),
                                "status", created.getStatus(),
                                "from", created.getFromLocation(),
                                "to", created.getToLocation(),
                                "requestedAt", created.getRequestedAt()
                        ))
                        .build()
        );
    }
}
