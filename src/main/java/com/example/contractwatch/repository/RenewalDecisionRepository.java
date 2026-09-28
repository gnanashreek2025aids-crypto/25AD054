package com.example.contractwatch.repository;

import com.example.contractwatch.model.RenewalDecision;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RenewalDecisionRepository extends JpaRepository<RenewalDecision, Long> {
}