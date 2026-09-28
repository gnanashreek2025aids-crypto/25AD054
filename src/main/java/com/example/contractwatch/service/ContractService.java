package com.example.contractwatch.service;

import com.example.contractwatch.model.Contract;
import com.example.contractwatch.repository.ContractRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ContractService {

    @Autowired
    private ContractRepository contractRepository;

    // Helper method to enforce renewal reminder business rules
    private void applyRenewalRule(Contract contract) {
        if (contract == null || contract.getEndDate() == null) {
            return;
        }

        // Rule: A TERMINATED contract must never be changed to RENEWAL_DUE
        if ("TERMINATED".equalsIgnoreCase(contract.getStatus())) {
            return;
        }

        // Rule: Calculate reminderDate = endDate minus noticePeriod days
        LocalDate reminderDate = contract.getEndDate().minusDays(contract.getNoticePeriod());
        LocalDate today = LocalDate.now();

        // Rule: If today's date is on or after reminderDate and on or before endDate, change status to RENEWAL_DUE
        if (!today.isBefore(reminderDate) && !today.isAfter(contract.getEndDate())) {
            contract.setStatus("RENEWAL_DUE");
        } else if (contract.getStatus() == null || contract.getStatus().trim().isEmpty()) {
            contract.setStatus("ACTIVE");
        }
    }

    // Add contract
    public Contract addContract(Contract contract) {
        // Rule: New contracts should initially have status ACTIVE
        if (contract.getStatus() == null || contract.getStatus().trim().isEmpty()) {
            contract.setStatus("ACTIVE");
        }
        // Enforce renewal check before saving
        applyRenewalRule(contract);
        return contractRepository.save(contract);
    }

    // Get all contracts
    public List<Contract> getAllContracts() {
        List<Contract> contracts = contractRepository.findAll();
        for (Contract contract : contracts) {
            String oldStatus = contract.getStatus();
            applyRenewalRule(contract);
            if (contract.getStatus() != null && !contract.getStatus().equals(oldStatus)) {
                contractRepository.save(contract);
            }
        }
        return contracts;
    }

    // Get contract by ID
    public Contract getContractById(Long id) {
        Contract contract = contractRepository.findById(id).orElse(null);
        if (contract != null) {
            String oldStatus = contract.getStatus();
            applyRenewalRule(contract);
            if (contract.getStatus() != null && !contract.getStatus().equals(oldStatus)) {
                contractRepository.save(contract);
            }
        }
        return contract;
    }

    // Update contract
    public Contract updateContract(Long id, Contract contractDetails) {
        Contract existing = contractRepository.findById(id).orElse(null);
        if (existing != null) {
            existing.setVendor(contractDetails.getVendor());
            existing.setContractName(contractDetails.getContractName());
            existing.setStartDate(contractDetails.getStartDate());
            existing.setEndDate(contractDetails.getEndDate());
            existing.setNoticePeriod(contractDetails.getNoticePeriod());
            if (contractDetails.getStatus() != null && !contractDetails.getStatus().trim().isEmpty()) {
                existing.setStatus(contractDetails.getStatus());
            }
            applyRenewalRule(existing);
            return contractRepository.save(existing);
        }
        return null;
    }

    // Delete contract
    public void deleteContract(Long id) {
        contractRepository.deleteById(id);
    }

    // Renew contract
    public Contract renewContract(Long id, LocalDate newEndDate) {
        Contract contract = contractRepository.findById(id).orElse(null);
        if (contract != null) {
            // Rule: Accept a new end date, update endDate, store newEndDate and set status to RENEWED
            contract.setEndDate(newEndDate);
            contract.setNewEndDate(newEndDate);
            contract.setStatus("RENEWED");
            return contractRepository.save(contract);
        }
        return null;
    }

    // Terminate contract
    public Contract terminateContract(Long id) {
        Contract contract = contractRepository.findById(id).orElse(null);
        if (contract != null) {
            // Rule: When terminating a contract, set status to TERMINATED
            contract.setStatus("TERMINATED");
            return contractRepository.save(contract);
        }
        return null;
    }

    // Get contracts expiring within the next 30 days
    public List<Contract> getExpiringContracts() {
        LocalDate today = LocalDate.now();
        LocalDate thirtyDaysLater = today.plusDays(30);
        List<Contract> expiring = contractRepository.findByEndDateBetween(today, thirtyDaysLater);
        for (Contract contract : expiring) {
            String oldStatus = contract.getStatus();
            applyRenewalRule(contract);
            if (contract.getStatus() != null && !contract.getStatus().equals(oldStatus)) {
                contractRepository.save(contract);
            }
        }
        return expiring;
    }
}
