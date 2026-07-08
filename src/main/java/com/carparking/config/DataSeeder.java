package com.carparking.config;

import com.carparking.service.ParkingSlotService;
import com.carparking.service.SlotRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.util.logging.Logger;

@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = Logger.getLogger(DataSeeder.class.getName());

    private final ParkingSlotService slotService;
    private final SlotRepository     slotRepository;

    public DataSeeder(ParkingSlotService slotService, SlotRepository slotRepository) {
        this.slotService    = slotService;
        this.slotRepository = slotRepository;
    }

    @Override
    public void run(String... args) {
        slotRepository.findAll().forEach(slotService::syncSlot);
        log.info("=== " + slotService.getAllSlots().size() + " slots loaded from DB ===");
    }
}
