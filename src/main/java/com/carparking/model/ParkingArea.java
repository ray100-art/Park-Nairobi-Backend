package com.carparking.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "parking_areas")
public class ParkingArea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "address", nullable = false)
    private String address;

    @Column(name = "latitude", nullable = false, columnDefinition = "DECIMAL(10,7)")
    private double latitude;

    @Column(name = "longitude", nullable = false, columnDefinition = "DECIMAL(10,7)")
    private double longitude;

    @Column(name = "total_slots")
    private int totalSlots;

    @Column(name = "active")
    private boolean active = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ── Constructors ──────────────────────────────────
    public ParkingArea() {}

    public ParkingArea(String name, String address,
                       double latitude, double longitude, int totalSlots) {
        this.name       = name;
        this.address    = address;
        this.latitude   = latitude;
        this.longitude  = longitude;
        this.totalSlots = totalSlots;
        this.active     = true;
    }

    // ── Getters ───────────────────────────────────────
    public Long getId()                  { return id; }
    public String getName()              { return name; }
    public String getAddress()           { return address; }
    public double getLatitude()          { return latitude; }
    public double getLongitude()         { return longitude; }
    public int getTotalSlots()           { return totalSlots; }
    public boolean isActive()            { return active; }
    public LocalDateTime getCreatedAt()  { return createdAt; }
    public LocalDateTime getUpdatedAt()  { return updatedAt; }

    // ── Setters ───────────────────────────────────────
    public void setId(Long id)                         { this.id = id; }
    public void setName(String name)                   { this.name = name; }
    public void setAddress(String address)             { this.address = address; }
    public void setLatitude(double latitude)           { this.latitude = latitude; }
    public void setLongitude(double longitude)         { this.longitude = longitude; }
    public void setTotalSlots(int totalSlots)          { this.totalSlots = totalSlots; }
    public void setActive(boolean active)              { this.active = active; }
    public void setCreatedAt(LocalDateTime createdAt)  { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt)  { this.updatedAt = updatedAt; }
}