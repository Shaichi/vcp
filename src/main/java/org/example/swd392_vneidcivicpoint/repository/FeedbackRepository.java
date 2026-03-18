package org.example.swd392_vneidcivicpoint.repository;

import org.example.swd392_vneidcivicpoint.constants.FeedbackStatus;
import org.example.swd392_vneidcivicpoint.entity.Citizen;
import org.example.swd392_vneidcivicpoint.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    Optional<Feedback> findByTrackingId(String trackingId);
    long countByCitizenAndStatus(Citizen citizen, FeedbackStatus status);
    List<Feedback> findByStatusOrderByCreatedAtAsc(FeedbackStatus status);
}
