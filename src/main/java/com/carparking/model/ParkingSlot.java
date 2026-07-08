package com.carparking.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "slots")
public class ParkingSlot {

    @Id
    @Column(name = "slot_id")
    private String slotId;

    @Column(name = "label")
    private String label;

    @Column(name = "parking_area_name")
    private String parkingAreaName;

    @Column(name = "parking_area_id")
    private Long parkingAreaId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private SlotStatus status;

    @Column(name = "latitude", columnDefinition = "DECIMAL(10,7)")
    private double latitude;

    @Column(name = "longitude", columnDefinition = "DECIMAL(10,7)")
    private double longitude;

    @Column(name = "floor")
    private int floor;

    @Column(name = "reservation_expires_at")
    private LocalDateTime reservationExpiresAt;

    @Column(name = "reserved_by_driver_id")
    private String reservedByDriverId;

    @Transient
    private double distanceKm;

    // ── Constructors ──────────────────────────────────
    public ParkingSlot() {}

    public ParkingSlot(String slotId, String label, String parkingAreaName,
                       Long parkingAreaId, SlotStatus status,
                       double latitude, double longitude, int floor,
                       LocalDateTime reservationExpiresAt,
                       String reservedByDriverId, double distanceKm) {
        this.slotId               = slotId;
        this.label                = label;
        this.parkingAreaName      = parkingAreaName;
        this.parkingAreaId        = parkingAreaId;
        this.status               = status;
        this.latitude             = latitude;
        this.longitude            = longitude;
        this.floor                = floor;
        this.reservationExpiresAt = reservationExpiresAt;
        this.reservedByDriverId   = reservedByDriverId;
        this.distanceKm           = distanceKm;
    }

    // ── Getters ───────────────────────────────────────
    public String getSlotId()                       { return slotId; }
    public String getLabel()                        { return label; }
    public String getParkingAreaName()              { return parkingAreaName; }
    public Long getParkingAreaId()                  { return parkingAreaId; }
    public SlotStatus getStatus()                   { return status; }
    public double getLatitude()                     { return latitude; }
    public double getLongitude()                    { return longitude; }
    public int getFloor()                           { return floor; }
    public LocalDateTime getReservationExpiresAt()  { return reservationExpiresAt; }
    @JsonIgnore
    public String getReservedByDriverId()           { return reservedByDriverId; }
    public double getDistanceKm()                   { return distanceKm; }

    // ── Setters ───────────────────────────────────────
    public void setSlotId(String slotId)                             { this.slotId = slotId; }
    public void setLabel(String label)                               { this.label = label; }
    public void setParkingAreaName(String parkingAreaName)           { this.parkingAreaName = parkingAreaName; }
    public void setParkingAreaId(Long parkingAreaId)                 { this.parkingAreaId = parkingAreaId; }
    public void setStatus(SlotStatus status)                         { this.status = status; }
    public void setLatitude(double latitude)                         { this.latitude = latitude; }
    public void setLongitude(double longitude)                       { this.longitude = longitude; }
    public void setFloor(int floor)                                  { this.floor = floor; }
    public void setReservationExpiresAt(LocalDateTime t)             { this.reservationExpiresAt = t; }
    public void setReservedByDriverId(String reservedByDriverId)     { this.reservedByDriverId = reservedByDriverId; }
    public void setDistanceKm(double distanceKm)                     { this.distanceKm = distanceKm; }

    // ── Builder ───────────────────────────────────────
    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String slotId;
        private String label;
        private String parkingAreaName;
        private Long parkingAreaId;
        private SlotStatus status;
        private double latitude;
        private double longitude;
        private int floor;
        private LocalDateTime reservationExpiresAt;
        private String reservedByDriverId;
        private double distanceKm;

        public Builder slotId(String slotId)                   { this.slotId = slotId; return this; }
        public Builder label(String label)                     { this.label = label; return this; }
        public Builder parkingAreaName(String parkingAreaName) { this.parkingAreaName = parkingAreaName; return this; }
        public Builder parkingAreaId(Long parkingAreaId)       { this.parkingAreaId = parkingAreaId; return this; }
        public Builder status(SlotStatus status)               { this.status = status; return this; }
        public Builder latitude(double latitude)               { this.latitude = latitude; return this; }
        public Builder longitude(double longitude)             { this.longitude = longitude; return this; }
        public Builder floor(int floor)                        { this.floor = floor; return this; }

        public ParkingSlot build() {
            return new ParkingSlot(slotId, label, parkingAreaName,
                    parkingAreaId, status, latitude, longitude, floor,
                    reservationExpiresAt, reservedByDriverId, distanceKm);
        }
    }

    // ── Helper ────────────────────────────────────────
    public boolean isFree() {
        return SlotStatus.FREE.equals(this.status);
    }
}