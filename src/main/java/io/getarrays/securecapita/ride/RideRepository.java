package io.getarrays.securecapita.ride;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RideRepository extends JpaRepository<Ride, Long> {

    List<Ride> findByDriver_Id(Long driverId);

    Page<Ride> findByDriver_Id(Long driverId, Pageable pageable);
}
