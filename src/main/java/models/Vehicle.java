package models;

import java.time.LocalDateTime;

public class Vehicle {
    private String vehicleNumber;
    private int slotNumber;
    private int floor;
    private LocalDateTime entryTime;
    
    public Vehicle(String vehicleNumber, int slotNumber, int floor) {
        this.vehicleNumber = vehicleNumber;
        this.slotNumber = slotNumber;
        this.floor = floor;
        this.entryTime = LocalDateTime.now();
    }
    
    public String getVehicleNumber() {
        return vehicleNumber;
    }
    
    public int getSlotNumber() {
        return slotNumber;
    }
    
    public int getFloor() {
        return floor;
    }
    
    public LocalDateTime getEntryTime() {
        return entryTime;
    }
    
    public void setEntryTime(LocalDateTime entryTime) {
        this.entryTime = entryTime;
    }
    
    @Override
    public String toString() {
        return vehicleNumber + " parked at Slot " + slotNumber + " (Floor " + floor + ")";
    }
}
