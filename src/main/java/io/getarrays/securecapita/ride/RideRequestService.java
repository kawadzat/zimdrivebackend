package io.getarrays.securecapita.ride;

import io.getarrays.securecapita.domain.User;
import io.getarrays.securecapita.dto.UserDTO;
import io.getarrays.securecapita.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class RideRequestService {
    private final RideRequestRepository rideRequestRepository;
    private final UserRepository<User> userRepository;

    public RideRequest create(UserDTO currentUser, RideRequestDto dto) {
        RideRequest request = RideRequest.builder()
                .fromLocation(dto.getFrom())
                .toLocation(dto.getTo())
                .status(RideStatusEnum.REQUESTED)
                .requestedAt(new Date())
                .requestedBy(userRepository.get(currentUser.getId()))
                .build();
        return rideRequestRepository.save(request);
    }
}

