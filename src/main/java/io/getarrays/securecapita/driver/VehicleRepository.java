package io.getarrays.securecapita.driver;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    boolean existsByDriver_IdAndStatus(Long driverId, VehicleStatus status);

    List<Vehicle> findByDriver_Id(Long driverId);

    List<Vehicle> findByDriver_IdAndStatus(Long driverId, VehicleStatus status);

    List<Vehicle> findByDriver_User_Id(Long userId);

    List<Vehicle> findByDriver_User_IdAndStatus(Long userId, VehicleStatus status);

    List<Vehicle> findByStatus(VehicleStatus status);
}
