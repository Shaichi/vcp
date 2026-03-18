package org.example.swd392_vneidcivicpoint.repository;

import org.example.swd392_vneidcivicpoint.entity.Citizen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CitizenRepository extends JpaRepository<Citizen, Long> {
    Optional<Citizen> findByCccdNumber(String cccdNumber);
}
