package com.example.chequeprocessing.web;

import com.example.chequeprocessing.domain.Cheque;
import com.example.chequeprocessing.service.ChequeService;
import com.example.chequeprocessing.web.dto.IssueChequeRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;


@RestController
@RequestMapping("/api/cheques")
public class ChequeController {
    private final ChequeService chequeService;

    public ChequeController(ChequeService chequeService) {
        this.chequeService = chequeService;
    }

    @PostMapping
    @PreAuthorize("hasRole('TELLER')")
    public ResponseEntity<Cheque> issue(@RequestBody IssueChequeRequest request) {
        try {
            Cheque issuedCheque = chequeService.issueCheque(request.getDrawerId(), request.getNumber(), request.getAmount(), java.time.LocalDate.now());
            return new ResponseEntity<>(issuedCheque, HttpStatus.CREATED);
        } catch (IllegalArgumentException | IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PostMapping("/{id}/present")
    @PreAuthorize("hasRole('TELLER')")
    public ResponseEntity<Cheque> present(@PathVariable("id") Long id) {
        try {
            Cheque presentedCheque = chequeService.presentCheque(id, java.time.LocalDate.now());
            return ResponseEntity.ok(presentedCheque);
        } catch (ChequeService.ChequeBounceException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        } catch (IllegalArgumentException | IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }
}


