package com.carparking.service;

import com.carparking.model.Payment;
import com.carparking.model.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByCheckoutRequestId(String checkoutRequestId);

    List<Payment> findByBookingId(Long bookingId);

    boolean existsByBookingIdAndStatus(Long bookingId, PaymentStatus status);
}