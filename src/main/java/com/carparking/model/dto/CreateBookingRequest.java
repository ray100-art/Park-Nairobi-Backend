package com.carparking.model.dto;

import jakarta.validation.constraints.NotBlank;

public class CreateBookingRequest {

    @NotBlank(message = "Slot ID is required")
    private String slotId;

    @NotBlank(message = "Vehicle plate is required")
    private String vehiclePlate;

    public String getSlotId()       { return slotId; }
    public String getVehiclePlate() { return vehiclePlate; }
    public void setSlotId(String v)       { this.slotId = v; }
    public void setVehiclePlate(String v) { this.vehiclePlate = v; }
}
