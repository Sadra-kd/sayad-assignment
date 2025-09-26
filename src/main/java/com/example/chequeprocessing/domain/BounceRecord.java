package com.example.chequeprocessing.domain;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class BounceRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Cheque cheque;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private String reason;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Cheque getCheque() { return cheque; }
    public void setCheque(Cheque cheque) { this.cheque = cheque; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}


