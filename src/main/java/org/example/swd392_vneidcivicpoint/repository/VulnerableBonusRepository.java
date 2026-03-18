package org.example.swd392_vneidcivicpoint.repository;

import org.example.swd392_vneidcivicpoint.entity.Citizen;
import org.example.swd392_vneidcivicpoint.entity.VulnerableBonus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VulnerableBonusRepository extends JpaRepository<VulnerableBonus, Long> {
    boolean existsByCitizenAndFiscalYear(Citizen citizen, int fiscalYear);
}
