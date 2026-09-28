package com.example.contractwatch.service;

import com.example.contractwatch.model.Contract;
import com.example.contractwatch.model.RenewalDecision;
import com.example.contractwatch.repository.ContractRepository;
import com.example.contractwatch.repository.RenewalDecisionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RenewalDecisionService {

    private final RenewalDecisionRepository renewalDecisionRepository;
    private final ContractRepository contractRepository;

    public RenewalDecisionService(
            RenewalDecisionRepository renewalDecisionRepository,
            ContractRepository contractRepository) {

        this.renewalDecisionRepository = renewalDecisionRepository;
        this.contractRepository = contractRepository;
    }

    public List<RenewalDecision> getAllDecisions() {
        return renewalDecisionRepository.findAll();
    }

    public RenewalDecision makeDecision(RenewalDecision decision) {

        if (decision.getContract() == null ||
                decision.getContract().getId() == null) {
            throw new RuntimeException("Contract ID is required");
        }

        Contract contract = contractRepository
                .findById(decision.getContract().getId())
                .orElseThrow(() -> new RuntimeException("Contract not found"));

        if (!decision.getDecision().equalsIgnoreCase("RENEWED") &&
                !decision.getDecision().equalsIgnoreCase("TERMINATED")) {
            throw new RuntimeException(
                    "Decision must be RENEWED or TERMINATED");
        }

        if (decision.getDecision().equalsIgnoreCase("RENEWED")) {

            if (decision.getNewEndDate() == null) {
                throw new RuntimeException(
                        "New end date is required when contract is renewed");
            }

            contract.setStatus("RENEWED");
            contract.setNewEndDate(decision.getNewEndDate());
            contract.setEndDate(decision.getNewEndDate());

        } else {

            decision.setNewEndDate(null);
            contract.setStatus("TERMINATED");
        }

        contractRepository.save(contract);

        decision.setContract(contract);

        return renewalDecisionRepository.save(decision);
    }
}