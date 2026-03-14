package models;

import java.time.LocalDateTime;

public class ParkingLog {
    private String vehicleNumber;
    private int slotNumber;
    private LocalDateTime entryTime;
    private LocalDateTime exitTime;
    private double charges;

    public ParkingLog(String vehicleNumber, int slotNumber,
                      LocalDateTime entryTime, LocalDateTime exitTime, double charges) {
        this.vehicleNumber = vehicleNumber;
        this.slotNumber = slotNumber;
        this.entryTime = entryTime;
        this.exitTime = exitTime;
        this.charges = charges;
    }
    // Getters for GUI
    public String getVehicleNumber() { return vehicleNumber; }
    public int getSlotNumber() { return slotNumber; }
    public LocalDateTime getEntryTime() { return entryTime; }
    public LocalDateTime getExitTime() { return exitTime; }
    public double getCharges() { return charges; }

    @Override
    public String toString() {
        return "Vehicle: " + vehicleNumber + ", Slot: " + slotNumber
                + " | IN: " + entryTime + " | OUT: " + exitTime
                + " | Charges: Rs." + charges;
    }
}