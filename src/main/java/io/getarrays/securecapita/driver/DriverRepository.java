package io.getarrays.securecapita.driver;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DriverRepository extends JpaRepository<Driver, Long> {

    Optional<Driver> findByUser_Id(Long userId);

    Optional<Driver> findByEmail(String email);
}
