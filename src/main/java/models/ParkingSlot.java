package models;

import java.time.LocalDateTime;

public class ParkingSlot {
    private int slotNumber;
    private int floor;
    private boolean isOccupied;
    private String vehicleNumber;
    private LocalDateTime entryTime;
    private boolean isEmergencySlot;
    private boolean hasEmergencyVehicle;
    
    // Constructor
    public ParkingSlot(int slotNumber, int floor) {
        this.slotNumber = slotNumber;
        this.floor = floor;
        this.isOccupied = false;
        this.vehicleNumber = "";
        this.entryTime = null;
        this.isEmergencySlot = false;
        this.hasEmergencyVehicle = false;
    }
    
    // ========== GETTERS ==========
    
    public int getSlotNumber() {
        return slotNumber;
    }
    
    public int getFloor() {
        return floor;
    }
    
    public boolean isOccupied() {
        return isOccupied;
    }
    
    public String getVehicleNumber() {
        return vehicleNumber;
    }
    
    public LocalDateTime getEntryTime() {
        return entryTime;
    }
    
    public boolean isEmergencySlot() {
        return isEmergencySlot;
    }
    
    public boolean hasEmergencyVehicle() {
        return hasEmergencyVehicle;
    }
    
    // NEW: Return slot type as String for UI display
    public String getSlotType() {
        if (hasEmergencyVehicle) {
            return "EMERGENCY_VEHICLE";
        } else if (isEmergencySlot && !isOccupied) {
            return "EMERGENCY";
        } else if (isOccupied) {
            return "OCCUPIED";
        } else {
            return "AVAILABLE";
        }
    }
    
    // ========== SETTERS ==========
    
    public void setOccupied(boolean occupied) {
        this.isOccupied = occupied;
    }
    
    public void setVehicleNumber(String vehicleNumber) {
        this.vehicleNumber = vehicleNumber;
        if (vehicleNumber != null && !vehicleNumber.isEmpty()) {
            this.entryTime = LocalDateTime.now();
        } else {
            this.entryTime = null;
        }
    }
    
    public void setEntryTime(LocalDateTime entryTime) {
        this.entryTime = entryTime;
    }
    
    public void setEmergencySlot(boolean isEmergencySlot) {
        this.isEmergencySlot = isEmergencySlot;
    }
    
    public void setHasEmergencyVehicle(boolean hasEmergencyVehicle) {
        this.hasEmergencyVehicle = hasEmergencyVehicle;
    }
    
    // ========== UTILITY METHODS ==========
    
    @Override
    public String toString() {
        if (isOccupied) {
            String type = hasEmergencyVehicle ? " [EMERGENCY]" : "";
            return "Slot " + slotNumber + " (Floor " + floor + "): " + 
                   vehicleNumber + type + " - Entry: " + entryTime;
        }
        
        if (isEmergencySlot) {
            return "Slot " + slotNumber + " (Floor " + floor + "): [EMERGENCY ONLY - Reserved]";
        }
        
        return "Slot " + slotNumber + " (Floor " + floor + "): [Available]";
    }
}
