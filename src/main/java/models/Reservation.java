package models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Reservation implements Comparable<Reservation> {
    private LocalDateTime reservationTime;
    private String vehicleNumber;
    private int slotNumber;
    private boolean isActive;
    
    // SIMPLIFIED: Removed customerName and contactNumber
    public Reservation(LocalDateTime time, String vehicle, int slot) {
        this.reservationTime = time;
        this.vehicleNumber = vehicle;
        this.slotNumber = slot;
        this.isActive = true;
    }
    
    @Override
    public int compareTo(Reservation other) {
        // Sort by reservation time (TreeSet uses this for automatic sorting)
        return this.reservationTime.compareTo(other.reservationTime);
    }
    
    // Getters
    public LocalDateTime getReservationTime() {
        return reservationTime;
    }
    
    public String getVehicleNumber() {
        return vehicleNumber;
    }
    
    public int getSlotNumber() {
        return slotNumber;
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public void setActive(boolean active) {
        this.isActive = active;
    }
    
    // Check if reservation time has arrived
    public boolean isTimeToArrive() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime windowStart = reservationTime.minusMinutes(15); // 15 min buffer before
        LocalDateTime windowEnd = reservationTime.plusMinutes(30);    // 30 min buffer after
        return now.isAfter(windowStart) && now.isBefore(windowEnd);
    }
    
    // Format for display
    public String getFormattedTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return reservationTime.format(formatter);
    }
    
    @Override
    public String toString() {
        return String.format("%s | Slot %d | %s", 
                           vehicleNumber, slotNumber, getFormattedTime());
    }
    
    // Detailed display
    public String getDetails() {
        return String.format(
            "Vehicle: %s\n" +
            "Slot: %d\n" +
            "Time: %s\n" +
            "Status: %s",
            vehicleNumber, slotNumber, 
            getFormattedTime(),
            isActive ? "Active" : "Cancelled"
        );
    }
}
