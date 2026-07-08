package com.carparking.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class MpesaPayRequest {

    @NotNull(message = "Booking ID is required")
    private Long bookingId;

    @NotBlank(message = "Phone number is required")
    private String phone;

    public Long getBookingId() { return bookingId; }
    public String getPhone()   { return phone; }
    public void setBookingId(Long v) { this.bookingId = v; }
    public void setPhone(String v)   { this.phone = v; }
}
