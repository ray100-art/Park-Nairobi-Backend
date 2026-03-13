package com.carparking.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "slot_id", nullable = false)
    private String slotId;

    @Column(name = "parking_area_id")
    private Long parkingAreaId;

    @Column(name = "status", nullable = false)
    private String status = "PENDING";

    @Column(name = "payment_status")
    private String paymentStatus = "UNPAID";        // ← NEW

    @Column(name = "vehicle_plate", nullable = false)
    private String vehiclePlate;

    @Column(name = "check_in_time")
    private LocalDateTime checkInTime;

    @Column(name = "check_out_time")
    private LocalDateTime checkOutTime;

    @Column(name = "reserved_at")
    private LocalDateTime reservedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "total_amount")
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ── Auto timestamps ────────────────────────────
    @PrePersist
    public void prePersist() {
        this.createdAt  = LocalDateTime.now();
        this.updatedAt  = LocalDateTime.now();
        if (this.reservedAt == null)
            this.reservedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ── Constructors ───────────────────────────────
    public Booking() {}

    public Booking(Long userId, String slotId, Long parkingAreaId,
                   String vehiclePlate, LocalDateTime expiresAt) {
        this.userId        = userId;
        this.slotId        = slotId;
        this.parkingAreaId = parkingAreaId;
        this.vehiclePlate  = vehiclePlate;
        this.expiresAt     = expiresAt;
        this.status        = "PENDING";
        this.paymentStatus = "UNPAID";
        this.reservedAt    = LocalDateTime.now();
        this.totalAmount   = BigDecimal.ZERO;
    }

    // ── Getters ────────────────────────────────────
    public Long getId()                        { return id; }
    public Long getUserId()                    { return userId; }
    public String getSlotId()                  { return slotId; }
    public Long getParkingAreaId()             { return parkingAreaId; }
    public String getStatus()                  { return status; }
    public String getPaymentStatus()           { return paymentStatus; }   // ← NEW
    public String getVehiclePlate()            { return vehiclePlate; }
    public LocalDateTime getCheckInTime()      { return checkInTime; }
    public LocalDateTime getCheckOutTime()     { return checkOutTime; }
    public LocalDateTime getReservedAt()       { return reservedAt; }
    public LocalDateTime getExpiresAt()        { return expiresAt; }
    public BigDecimal getTotalAmount()         { return totalAmount; }
    public LocalDateTime getCreatedAt()        { return createdAt; }
    public LocalDateTime getUpdatedAt()        { return updatedAt; }

    // ── Setters ────────────────────────────────────
    public void setId(Long id)                               { this.id = id; }
    public void setUserId(Long userId)                       { this.userId = userId; }
    public void setSlotId(String slotId)                     { this.slotId = slotId; }
    public void setParkingAreaId(Long parkingAreaId)         { this.parkingAreaId = parkingAreaId; }
    public void setStatus(String status)                     { this.status = status; }
    public void setPaymentStatus(String paymentStatus)       { this.paymentStatus = paymentStatus; } // ← NEW
    public void setVehiclePlate(String vehiclePlate)         { this.vehiclePlate = vehiclePlate; }
    public void setCheckInTime(LocalDateTime checkInTime)    { this.checkInTime = checkInTime; }
    public void setCheckOutTime(LocalDateTime checkOutTime)  { this.checkOutTime = checkOutTime; }
    public void setReservedAt(LocalDateTime reservedAt)      { this.reservedAt = reservedAt; }
    public void setExpiresAt(LocalDateTime expiresAt)        { this.expiresAt = expiresAt; }
    public void setTotalAmount(BigDecimal totalAmount)       { this.totalAmount = totalAmount; }
    public void setCreatedAt(LocalDateTime createdAt)        { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt)        { this.updatedAt = updatedAt; }
}