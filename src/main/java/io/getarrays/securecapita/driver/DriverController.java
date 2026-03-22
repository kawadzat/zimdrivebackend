package io.getarrays.securecapita.driver;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.getarrays.securecapita.domain.HttpResponse;
import io.getarrays.securecapita.domain.User;
import io.getarrays.securecapita.dto.UserDTO;
import io.getarrays.securecapita.driver.dto.VehicleDto;
import io.getarrays.securecapita.exception.BadRequestException;
import io.getarrays.securecapita.exception.ResourceNotFoundException;
import io.getarrays.securecapita.repository.implementation.UserRepository1;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import static io.getarrays.securecapita.utils.UserUtils.getAuthenticatedUser;
import static java.time.LocalDateTime.now;
import static java.util.Map.of;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

/**
 * Driver & vehicle REST API. All routes require authentication unless opened in {@code SecurityConfig}.
 */
@RestController
@RequestMapping("/driver")
@RequiredArgsConstructor
public class DriverController {

    private final VehicleRepository vehicleRepository;
    private final DriverRepository driverRepository;
    private final UserRepository1 userRepository1;
    private final ObjectMapper objectMapper;

    @PostMapping(value = {"/vehicles", "/vehicle/create"}, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    public ResponseEntity<HttpResponse> createVehicleJson(Authentication authentication,
                                                          @Valid @RequestBody VehicleDto dto) {
        return createVehicleInternal(authentication, dto);
    }

    @PostMapping(value = "/vehicles", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Transactional
    public ResponseEntity<HttpResponse> createVehicleMultipart(Authentication authentication,
                                                               @RequestPart("vehicle") String vehicleJson)
            throws Exception {
        VehicleDto dto = objectMapper.readValue(vehicleJson, VehicleDto.class);
        return createVehicleInternal(authentication, dto);
    }

    private ResponseEntity<HttpResponse> createVehicleInternal(Authentication authentication, VehicleDto dto) {
        UserDTO userDto = getAuthenticatedUser(authentication);
        Driver driver = resolveOrCreateDriver(userDto);
        Vehicle vehicle = new Vehicle();
        vehicle.setMake(dto.getMake().trim());
        vehicle.setModel(dto.getModel().trim());
        vehicle.setYearOfManufacture(dto.getYearOfManufacture());
        vehicle.setRegistrationNumber(dto.getRegistrationNumber().trim());
        vehicle.setColor(dto.getColor() != null ? dto.getColor().trim() : null);
        vehicle.setDriver(driver);
        vehicle.setStatus(VehicleStatus.PENDING_APPROVAL);
        Vehicle saved = vehicleRepository.save(vehicle);

        return ResponseEntity.status(CREATED).body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .statusCode(CREATED.value())
                        .status(CREATED)
                        .message("Vehicle submitted for approval")
                        .data(of("vehicle", toVehicleMap(saved)))
                        .build()
        );
    }

    @GetMapping("/vehicle/status")
    @Transactional(readOnly = true)
    public ResponseEntity<HttpResponse> getVehicleStatus(Authentication authentication) {
        UserDTO user = getAuthenticatedUser(authentication);
        List<Vehicle> vehicles = vehicleRepository.findByDriver_User_Id(user.getId());
        Map<String, Long> counts = new LinkedHashMap<>();
        counts.put("pending", countByEnum(vehicles, VehicleStatus.PENDING_APPROVAL));
        counts.put("approved", countByEnum(vehicles, VehicleStatus.APPROVED));
        counts.put("rejected", countByEnum(vehicles, VehicleStatus.REJECTED));

        return ResponseEntity.ok(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .statusCode(OK.value())
                        .status(OK)
                        .message("Vehicle status summary")
                        .data(of(
                                "counts", counts,
                                "total", (long) vehicles.size(),
                                "labels", Map.of(
                                        "pending", "pending_approval",
                                        "approved", "approved",
                                        "rejected", "rejected"
                                )
                        ))
                        .build()
        );
    }

    @GetMapping("/vehicles")
    @Transactional(readOnly = true)
    public ResponseEntity<HttpResponse> getMyVehicles(Authentication authentication,
                                                      @RequestParam(required = false) String status) {
        UserDTO user = getAuthenticatedUser(authentication);
        List<Vehicle> list;
        if (status == null || status.isBlank()) {
            list = vehicleRepository.findByDriver_User_Id(user.getId());
        } else {
            VehicleStatus vs = parseVehicleStatus(status);
            list = vehicleRepository.findByDriver_User_IdAndStatus(user.getId(), vs);
        }
        return ResponseEntity.ok(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .statusCode(OK.value())
                        .status(OK)
                        .message("Vehicles retrieved")
                        .data(of("vehicles", list.stream().map(this::toVehicleMap).collect(Collectors.toList())))
                        .build()
        );
    }

    @GetMapping("/vehicle/pending")
    @Transactional(readOnly = true)
    public ResponseEntity<HttpResponse> getPendingVehiclesForAdmin() {
        List<Vehicle> vehicles = vehicleRepository.findByStatus(VehicleStatus.PENDING_APPROVAL);
        return ResponseEntity.ok(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .statusCode(OK.value())
                        .status(OK)
                        .message("Pending vehicles")
                        .data(of("vehicles", vehicles.stream().map(this::toVehicleMapWithDriver).collect(Collectors.toList())))
                        .build()
        );
    }

    @PutMapping("/vehicle/{vehicleId}/approve")
    @Transactional
    public ResponseEntity<HttpResponse> approveVehicle(@PathVariable Long vehicleId) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id " + vehicleId));
        vehicle.setStatus(VehicleStatus.APPROVED);
        Vehicle saved = vehicleRepository.save(vehicle);
        return ResponseEntity.ok(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .statusCode(OK.value())
                        .status(OK)
                        .message("Vehicle approved")
                        .data(of("vehicle", toVehicleMap(saved)))
                        .build()
        );
    }

    @PutMapping("/vehicle/{vehicleId}/reject")
    @Transactional
    public ResponseEntity<HttpResponse> rejectVehicle(@PathVariable Long vehicleId) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id " + vehicleId));
        vehicle.setStatus(VehicleStatus.REJECTED);
        Vehicle saved = vehicleRepository.save(vehicle);
        return ResponseEntity.ok(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .statusCode(OK.value())
                        .status(OK)
                        .message("Vehicle rejected")
                        .data(of("vehicle", toVehicleMap(saved)))
                        .build()
        );
    }

    private Driver resolveOrCreateDriver(UserDTO userDTO) {
        return driverRepository.findByUser_Id(userDTO.getId())
                .orElseGet(() -> {
                    User user = userRepository1.findById(userDTO.getId())
                            .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + userDTO.getId()));
                    Driver driver = new Driver();
                    driver.setEmail(user.getEmail());
                    driver.setUser(user);
                    return driverRepository.save(driver);
                });
    }

    private long countByEnum(List<Vehicle> vehicles, VehicleStatus status) {
        return vehicles.stream().filter(v -> v.getStatus() == status).count();
    }

    private VehicleStatus parseVehicleStatus(String raw) {
        if (raw == null) {
            throw new BadRequestException("Status cannot be null");
        }
        String s = raw.trim().toLowerCase(Locale.ROOT);
        return switch (s) {
            case "pending", "pending_approval" -> VehicleStatus.PENDING_APPROVAL;
            case "approved" -> VehicleStatus.APPROVED;
            case "rejected" -> VehicleStatus.REJECTED;
            default -> throw new BadRequestException("Invalid status filter: " + raw);
        };
    }

    private Map<String, Object> toVehicleMap(Vehicle v) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", v.getId());
        m.put("make", v.getMake());
        m.put("model", v.getModel());
        m.put("yearOfManufacture", v.getYearOfManufacture());
        m.put("registrationNumber", v.getRegistrationNumber());
        m.put("color", v.getColor());
        m.put("status", v.getStatus());
        return m;
    }

    private Map<String, Object> toVehicleMapWithDriver(Vehicle v) {
        Map<String, Object> m = toVehicleMap(v);
        if (v.getDriver() != null) {
            m.put("driverId", v.getDriver().getId());
            if (v.getDriver().getUser() != null) {
                m.put("userId", v.getDriver().getUser().getId());
                m.put("driverEmail", v.getDriver().getEmail());
            }
        }
        return m;
    }
}
