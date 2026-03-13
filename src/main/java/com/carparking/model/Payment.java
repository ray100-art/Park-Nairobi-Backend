package com.carparking.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "booking_id", nullable = false)
    private Long bookingId;

    @Column(name = "checkout_request_id", unique = true)
    private String checkoutRequestId;

    @Column(name = "mpesa_receipt_number")
    private String mpesaReceiptNumber;

    @Column(name = "phone")
    private String phone;

    @Column(name = "amount")
    private BigDecimal amount;               // ← was Double, now BigDecimal to match DB DECIMAL

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(name = "failure_reason")
    private String failureReason;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) this.createdAt = LocalDateTime.now();
    }

    // ── Getters & Setters ─────────────────────────────────────────────────
    public Long getId()                                 { return id; }
    public void setId(Long id)                         { this.id = id; }

    public Long getBookingId()                         { return bookingId; }
    public void setBookingId(Long bookingId)           { this.bookingId = bookingId; }

    public String getCheckoutRequestId()               { return checkoutRequestId; }
    public void setCheckoutRequestId(String v)         { this.checkoutRequestId = v; }

    public String getMpesaReceiptNumber()              { return mpesaReceiptNumber; }
    public void setMpesaReceiptNumber(String v)        { this.mpesaReceiptNumber = v; }

    public String getPhone()                           { return phone; }
    public void setPhone(String phone)                 { this.phone = phone; }

    public BigDecimal getAmount()                      { return amount; }
    public void setAmount(BigDecimal amount)           { this.amount = amount; }

    public PaymentStatus getStatus()                   { return status; }
    public void setStatus(PaymentStatus status)        { this.status = status; }

    public String getFailureReason()                   { return failureReason; }
    public void setFailureReason(String v)             { this.failureReason = v; }

    public LocalDateTime getCreatedAt()                { return createdAt; }
    public void setCreatedAt(LocalDateTime v)          { this.createdAt = v; }

    public LocalDateTime getCompletedAt()              { return completedAt; }
    public void setCompletedAt(LocalDateTime v)        { this.completedAt = v; }
}