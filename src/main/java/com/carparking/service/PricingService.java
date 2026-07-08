package com.carparking.service;

import com.carparking.model.Booking;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class PricingService {

    @Value("${parking.fee.amount:50}")
    private int parkingFeeAmount;

    public BigDecimal getParkingFee() {
        return BigDecimal.valueOf(parkingFeeAmount);
    }

    public BigDecimal expectedAmount(Booking booking) {
        if (booking.getTotalAmount() != null
                && booking.getTotalAmount().compareTo(BigDecimal.ZERO) > 0) {
            return booking.getTotalAmount();
        }
        return getParkingFee();
    }
}
