package trees;

import models.Reservation;
import java.time.LocalDateTime;
import java.util.*;

public class ReservationTree {
    // TreeSet: Auto-sorted by reservation time (Red-Black Tree internally)
    private TreeSet<Reservation> reservations;
    
    // TreeMap: Quick lookup - slotNumber → Reservation time
    private TreeMap<Integer, LocalDateTime> slotReservations;
    
    // TreeMap: Quick lookup - vehicleNumber → Reservation
    private TreeMap<String, Reservation> vehicleReservations;
    
    public ReservationTree() {
        reservations = new TreeSet<>();
        slotReservations = new TreeMap<>();
        vehicleReservations = new TreeMap<>();
    }
    
    // SIMPLIFIED: Add new reservation (only 3 parameters now)
    public String addReservation(LocalDateTime time, String vehicle, int slot) {
        // Validate: Check if slot already reserved around that time
        if (isSlotReservedAt(slot, time)) {
            return "ERROR: Slot " + slot + " already reserved near that time!";
        }
        
        // Validate: Check if vehicle already has reservation
        if (vehicleReservations.containsKey(vehicle)) {
            return "ERROR: Vehicle " + vehicle + " already has a reservation!";
        }
        
        // Validate: Time must be in future
        if (time.isBefore(LocalDateTime.now())) {
            return "ERROR: Cannot reserve for past time!";
        }
        
        // Make reservation ----
        Reservation reservation = new Reservation(time, vehicle, slot);
        reservations.add(reservation);
        slotReservations.put(slot, time);
        vehicleReservations.put(vehicle, reservation);
        
        return "SUCCESS: Reservation created for vehicle " + vehicle + " at " + 
               reservation.getFormattedTime();
    }
    
    // Check if slot is reserved (with 2-hour buffer)
    private boolean isSlotReservedAt(int slot, LocalDateTime time) {
        if (!slotReservations.containsKey(slot)) {
            return false;
        }
        
        LocalDateTime existingTime = slotReservations.get(slot);
        long hoursDiff = Math.abs(java.time.Duration.between(time, existingTime).toHours());
        
        return hoursDiff < 2; 
    }
    
    // Get all reservations sorted by time (TreeSet maintains order)
    public List<Reservation> getAllReservations() {
        return new ArrayList<>(reservations);
    }
    
    // Get upcoming reservations (next N hours)
    public List<Reservation> getUpcomingReservations(int hours) {
        List<Reservation> upcoming = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime future = now.plusHours(hours);
        
        for (Reservation res : reservations) {
            if (res.isActive() && 
                res.getReservationTime().isAfter(now) && 
                res.getReservationTime().isBefore(future)) {
                upcoming.add(res);
            }
        }
        
        return upcoming;
    }
    
    // Get reservations that are ready to arrive (time window)
    public List<Reservation> getReadyToArriveReservations() {
        List<Reservation> ready = new ArrayList<>();
        
        for (Reservation res : reservations) {
            if (res.isActive() && res.isTimeToArrive()) {
                ready.add(res);
            }
        }
        
        return ready;
    }
    
    // Find reservation by vehicle number
    public Reservation findByVehicle(String vehicleNumber) {
        return vehicleReservations.get(vehicleNumber);
    }
    
    // Find reservations by slot number
    public List<Reservation> findBySlot(int slotNumber) {
        List<Reservation> slotReservations = new ArrayList<>();
        
        for (Reservation res : reservations) {
            if (res.getSlotNumber() == slotNumber && res.isActive()) {
                slotReservations.add(res);
            }
        }
        
        return slotReservations;
    }
    
    // Cancel reservation
    public String cancelReservation(String vehicleNumber) {
        Reservation res = vehicleReservations.get(vehicleNumber);
        
        if (res == null) {
            return "ERROR: No reservation found for vehicle " + vehicleNumber;
        }
        
        if (!res.isActive()) {
            return "ERROR: Reservation already cancelled!";
        }
        
        // Mark as cancelled
        res.setActive(false);
        
        // Remove from lookups
        slotReservations.remove(res.getSlotNumber());
        vehicleReservations.remove(vehicleNumber);
        
        return "SUCCESS: Reservation cancelled for vehicle " + vehicleNumber;
    }
    
    // Complete reservation (when vehicle arrives and parks)
    public String completeReservation(String vehicleNumber) {
        Reservation res = vehicleReservations.get(vehicleNumber);
        
        if (res == null) {
            return "ERROR: No reservation found!";
        }
        
        // Remove from all structures
        reservations.remove(res);
        slotReservations.remove(res.getSlotNumber());
        vehicleReservations.remove(vehicleNumber);
        
        return "Reservation completed for vehicle " + vehicleNumber;
    }
    
    // Clean expired reservations (older than 2 hours past scheduled time)
    public int cleanExpiredReservations() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(2);
        int count = 0;
        
        Iterator<Reservation> iterator = reservations.iterator();
        while (iterator.hasNext()) {
            Reservation res = iterator.next();
            if (res.getReservationTime().isBefore(cutoff)) {
                iterator.remove();
                slotReservations.remove(res.getSlotNumber());
                vehicleReservations.remove(res.getVehicleNumber());
                count++;
            }
        }
        
        return count;
    }
    
    // Get total active reservations
    public int getTotalReservations() {
        return vehicleReservations.size();
    }
    
    // Check if slot is available for reservation at given time
    public boolean isSlotAvailable(int slot, LocalDateTime time) {
        return !isSlotReservedAt(slot, time);
    }
}
