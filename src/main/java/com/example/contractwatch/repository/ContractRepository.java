package com.example.contractwatch.repository;

import com.example.contractwatch.model.Contract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ContractRepository extends JpaRepository<Contract, Long> {

    // Method to find contracts whose endDate is between two dates
    List<Contract> findByEndDateBetween(LocalDate startDate, LocalDate endDate);
}
