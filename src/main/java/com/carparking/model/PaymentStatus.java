package com.carparking.model;

public enum PaymentStatus {
    PENDING,     // STK push sent, waiting for PIN
    COMPLETED,   // Payment confirmed by Safaricom
    FAILED,      // User cancelled or timed out
    CANCELLED    // Cancelled by system
}