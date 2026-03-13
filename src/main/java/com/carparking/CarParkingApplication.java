package com.carparking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Car Parking Management System
 * Epic 01 – Parking Slot Algorithm Engine
 */
@SpringBootApplication
@EnableScheduling   // Required for reservation timeout (ScheduledExecutorService)
public class CarParkingApplication {

    public static void main(String[] args) {
        SpringApplication.run(CarParkingApplication.class, args);
    }
}
