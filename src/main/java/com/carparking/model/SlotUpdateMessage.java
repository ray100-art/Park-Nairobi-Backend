package com.carparking.model;

public class SlotUpdateMessage {

    private String slotId;
    private String status;
    private String parkingAreaName;
    private double latitude;
    private double longitude;
    private String reservedByDriverId;
    private String timestamp;

    public SlotUpdateMessage() {}

    public SlotUpdateMessage(String slotId, String status,
                             String parkingAreaName,
                             double latitude, double longitude,
                             String reservedByDriverId) {
        this.slotId             = slotId;
        this.status             = status;
        this.parkingAreaName    = parkingAreaName;
        this.latitude           = latitude;
        this.longitude          = longitude;
        this.reservedByDriverId = reservedByDriverId;
        this.timestamp          = java.time.LocalDateTime.now().toString();
    }

    public String getSlotId()              { return slotId; }
    public String getStatus()              { return status; }
    public String getParkingAreaName()     { return parkingAreaName; }
    public double getLatitude()            { return latitude; }
    public double getLongitude()           { return longitude; }
    public String getReservedByDriverId()  { return reservedByDriverId; }
    public String getTimestamp()           { return timestamp; }

    public void setSlotId(String slotId)                       { this.slotId = slotId; }
    public void setStatus(String status)                       { this.status = status; }
    public void setParkingAreaName(String parkingAreaName)     { this.parkingAreaName = parkingAreaName; }
    public void setLatitude(double latitude)                   { this.latitude = latitude; }
    public void setLongitude(double longitude)                 { this.longitude = longitude; }
    public void setReservedByDriverId(String reservedByDriverId) { this.reservedByDriverId = reservedByDriverId; }
    public void setTimestamp(String timestamp)                 { this.timestamp = timestamp; }
}