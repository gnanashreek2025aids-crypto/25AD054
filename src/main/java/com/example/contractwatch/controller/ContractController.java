package com.example.contractwatch.controller;

import com.example.contractwatch.model.Contract;
import com.example.contractwatch.service.ContractService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/contracts")
@CrossOrigin(origins = "*")
public class ContractController {

    @Autowired
    private ContractService contractService;

    // POST /api/contracts – add contract
    @PostMapping
    public ResponseEntity<Contract> addContract(@RequestBody Contract contract) {
        Contract created = contractService.addContract(contract);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    // GET /api/contracts – get all contracts
    @GetMapping
    public ResponseEntity<List<Contract>> getAllContracts() {
        List<Contract> contracts = contractService.getAllContracts();
        return ResponseEntity.ok(contracts);
    }

    // GET /api/contracts/expiring – show contracts expiring within next 30 days
    // Placed before /{id} to ensure exact route matching
    @GetMapping("/expiring")
    public ResponseEntity<List<Contract>> getExpiringContracts() {
        List<Contract> expiring = contractService.getExpiringContracts();
        return ResponseEntity.ok(expiring);
    }

    // GET /api/contracts/{id} – get one contract
    @GetMapping("/{id}")
    public ResponseEntity<?> getContractById(@PathVariable Long id) {
        Contract contract = contractService.getContractById(id);
        if (contract == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Contract not found with ID: " + id);
        }
        return ResponseEntity.ok(contract);
    }

    // PUT /api/contracts/{id} – update contract
    @PutMapping("/{id}")
    public ResponseEntity<?> updateContract(@PathVariable Long id, @RequestBody Contract contract) {
        Contract updated = contractService.updateContract(id, contract);
        if (updated == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Contract not found with ID: " + id);
        }
        return ResponseEntity.ok(updated);
    }

    // DELETE /api/contracts/{id} – delete contract
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteContract(@PathVariable Long id) {
        Contract existing = contractService.getContractById(id);
        if (existing == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Contract not found with ID: " + id);
        }
        contractService.deleteContract(id);
        return ResponseEntity.ok("Contract deleted successfully with ID: " + id);
    }

    // PUT /api/contracts/{id}/renew?newEndDate=YYYY-MM-DD – renew contract
    @PutMapping("/{id}/renew")
    public ResponseEntity<?> renewContract(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate newEndDate) {
        Contract renewed = contractService.renewContract(id, newEndDate);
        if (renewed == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Contract not found with ID: " + id);
        }
        return ResponseEntity.ok(renewed);
    }

    // PUT /api/contracts/{id}/terminate – terminate contract
    @PutMapping("/{id}/terminate")
    public ResponseEntity<?> terminateContract(@PathVariable Long id) {
        Contract terminated = contractService.terminateContract(id);
        if (terminated == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Contract not found with ID: " + id);
        }
        return ResponseEntity.ok(terminated);
    }
}
