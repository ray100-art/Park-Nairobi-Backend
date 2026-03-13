package com.carparking.config;

import com.carparking.model.ParkingSlot;
import com.carparking.service.ParkingSlotService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.util.logging.Logger;

@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = Logger.getLogger(DataSeeder.class.getName());

    private final ParkingSlotService slotService;

    public DataSeeder(ParkingSlotService slotService) {
        this.slotService = slotService;
    }

    @Override
    public void run(String... args) {
        register("LOT_A-01", "Lot A – Bay 01", "Nairobi CBD Parking",  -1.2864, 36.8172, 0);
        register("LOT_A-02", "Lot A – Bay 02", "Nairobi CBD Parking",  -1.2865, 36.8173, 0);
        register("LOT_A-03", "Lot A – Bay 03", "Nairobi CBD Parking",  -1.2866, 36.8174, 0);
        register("LOT_A-04", "Lot A – Bay 04", "Nairobi CBD Parking",  -1.2867, 36.8175, 0);
        register("LOT_A-05", "Lot A – Bay 05", "Nairobi CBD Parking",  -1.2868, 36.8176, 0);
        register("LOT_B-01", "Lot B – Bay 01", "Westlands Parking",    -1.2673, 36.8094, 1);
        register("LOT_B-02", "Lot B – Bay 02", "Westlands Parking",    -1.2674, 36.8095, 1);
        register("LOT_B-03", "Lot B – Bay 03", "Westlands Parking",    -1.2675, 36.8096, 1);
        register("LOT_C-01", "Lot C – Bay 01", "Upper Hill Parking",   -1.2967, 36.8219, 0);
        register("LOT_C-02", "Lot C – Bay 02", "Upper Hill Parking",   -1.2968, 36.8220, 0);
        log.info("=== " + slotService.getAllSlots().size() + " slots seeded ===");
    }

    private void register(String id, String label, String area,
                          double lat, double lon, int floor) {
        slotService.registerSlot(ParkingSlot.builder()
                .slotId(id)
                .label(label)
                .parkingAreaName(area)
                .latitude(lat)
                .longitude(lon)
                .floor(floor)
                .build());
    }
}