package com.carparking.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class UpdateProfileRequest {

    @NotBlank(message = "Full name is required")
    @Size(max = 100, message = "Full name is too long")
    private String fullName;

    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^(\\+254|254|0)[17]\\d{8}$", message = "Invalid phone number")
    private String phone;

    public String getFullName() { return fullName; }
    public String getPhone()    { return phone; }
    public void setFullName(String v) { this.fullName = v; }
    public void setPhone(String v)    { this.phone = v; }
}
