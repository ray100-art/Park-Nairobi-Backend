package com.carparking.model;

import java.time.LocalDateTime;

public class SensorEvent {

    private String slotId;
    private String sensorId;
    private LocalDateTime timestamp;

    public SensorEvent() {}

    public SensorEvent(String slotId, String sensorId) {
        this.slotId    = slotId;
        this.sensorId  = sensorId;
        this.timestamp = LocalDateTime.now();
    }

    public String getSlotId()                      { return slotId; }
    public String getSensorId()                    { return sensorId; }
    public LocalDateTime getTimestamp()            { return timestamp; }
    public void setSlotId(String slotId)           { this.slotId = slotId; }
    public void setSensorId(String sensorId)       { this.sensorId = sensorId; }
    public void setTimestamp(LocalDateTime ts)     { this.timestamp = ts; }
}