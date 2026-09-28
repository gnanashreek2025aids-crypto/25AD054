package com.example.contractwatch.controller;

import com.example.contractwatch.model.RenewalDecision;
import com.example.contractwatch.service.RenewalDecisionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/renewal-decisions")
@CrossOrigin
public class RenewalDecisionController {

    private final RenewalDecisionService renewalDecisionService;

    public RenewalDecisionController(RenewalDecisionService renewalDecisionService) {
        this.renewalDecisionService = renewalDecisionService;
    }

    @GetMapping
    public List<RenewalDecision> getAllDecisions() {
        return renewalDecisionService.getAllDecisions();
    }

    @PostMapping
    public RenewalDecision makeDecision(@RequestBody RenewalDecision decision) {
        return renewalDecisionService.makeDecision(decision);
    }
}