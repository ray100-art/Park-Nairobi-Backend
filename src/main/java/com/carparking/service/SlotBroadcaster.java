package com.carparking.service;

import com.carparking.model.ParkingSlot;
import com.carparking.model.SlotUpdateMessage;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class SlotBroadcaster {

    private final SimpMessagingTemplate messagingTemplate;

    public SlotBroadcaster(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    // Call this whenever a slot status changes
    public void broadcastSlotUpdate(ParkingSlot slot) {
        SlotUpdateMessage msg = new SlotUpdateMessage(
                slot.getSlotId(),
                slot.getStatus().name(),
                slot.getParkingAreaName(),
                slot.getLatitude(),
                slot.getLongitude()
        );
        messagingTemplate.convertAndSend("/topic/slots", msg);
    }
}