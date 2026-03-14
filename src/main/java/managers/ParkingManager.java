package managers;

import graphs.ParkingGraph;
import models.ParkingSlot;
import models.ParkingLog;
import models.Reservation;
import trees.ReservationTree;
import managers.OperationHistory;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.*;

public class ParkingManager {
    // Data Structures
    private ArrayList<ParkingSlot> allSlots;
    private PriorityQueue<Integer> availableSlots;
    private HashMap<String, Integer> vehicleToSlot;
    private HashMap<String, LocalDateTime> entryTimes;
    private Queue<String> entryQueue;
    private LinkedList<ParkingLog> parkingLogs;
    private int[][] floorLayout;
    
    private ReservationTree reservationTree;
    private OperationHistory operationHistory;
    private ParkingGraph parkingGraph;
    
    private Set<String> emergencyVehicles;
    private Set<Integer> emergencySlots;
    private Set<Integer> reservedSlots;

    private int totalFloors;
    private int slotsPerFloor;
    private int totalSlots;
    private int freeSlots;
    private int occupiedSlots;

    public ParkingManager(int totalFloors, int slotsPerFloor) {
        this.totalFloors = totalFloors;
        this.slotsPerFloor = slotsPerFloor;
        this.totalSlots = totalFloors * slotsPerFloor;
        this.freeSlots = totalSlots - 3; // Exclude emergency slots
        this.occupiedSlots = 0;

        allSlots = new ArrayList<>();
        availableSlots = new PriorityQueue<>();
        vehicleToSlot = new HashMap<>();
        entryTimes = new HashMap<>();
        entryQueue = new LinkedList<>();
        parkingLogs = new LinkedList<>();
        floorLayout = new int[totalFloors][slotsPerFloor];
        
        reservationTree = new ReservationTree();
        operationHistory = new OperationHistory(50);
        parkingGraph = new ParkingGraph(totalSlots);
        
        emergencyVehicles = new HashSet<>();
        emergencySlots = new HashSet<>();
        reservedSlots = new HashSet<>();

        int slotNumber = 1;
        for (int floor = 0; floor < totalFloors; floor++) {
            for (int slot = 0; slot < slotsPerFloor; slot++) {
                ParkingSlot parkingSlot = new ParkingSlot(slotNumber, floor);
                allSlots.add(parkingSlot);
                
                // FIXED: Slots 1-3 are STRICTLY emergency slots
                if (slotNumber >= 1 && slotNumber <= 3) {
                    emergencySlots.add(slotNumber);
                    parkingSlot.setEmergencySlot(true);
                    // DO NOT add to availableSlots
                } else {
                    // Only slots 4-30 available for normal vehicles
                    availableSlots.offer(slotNumber);
                }
                
                floorLayout[floor][slot] = 0;
                slotNumber++;
            }
        }
    }

    // ========== UPDATED: PARK VEHICLE WITH VALIDATION ==========
    
    public String parkVehicle(String vehicleNumber) {
        return parkVehicle(vehicleNumber, false);
    }
    
    private String parkVehicle(String vehicleNumber, boolean isEmergency) {
        if (vehicleToSlot.containsKey(vehicleNumber)) {
            return "ERROR: Vehicle " + vehicleNumber + " is already parked!";
        }

        Integer slotNumber = findAvailableSlot(isEmergency);
        
        if (slotNumber == null) {
            if (!isEmergency) {
                entryQueue.offer(vehicleNumber);
                return "ERROR: No NORMAL slots available. Emergency/Reserved slots cannot be used.\n" +
                       "Vehicle " + vehicleNumber + " added to queue at position " + entryQueue.size();
            } else {
                return "ERROR: No slots available even for emergency vehicle!";
            }
        }

        ParkingSlot slot = allSlots.get(slotNumber - 1);
        slot.setOccupied(true);
        slot.setVehicleNumber(vehicleNumber);
        
        if (isEmergency) {
            slot.setHasEmergencyVehicle(true);
        }

        vehicleToSlot.put(vehicleNumber, slotNumber);
        entryTimes.put(vehicleNumber, LocalDateTime.now());

        int floor = slot.getFloor();
        int posOnFloor = (slotNumber - 1) % slotsPerFloor;
        floorLayout[floor][posOnFloor] = 1;

        if (!emergencySlots.contains(slotNumber)) {
            availableSlots.remove(slotNumber);
        }

        occupiedSlots++;
        if (!isEmergency && !emergencySlots.contains(slotNumber)) {
            freeSlots--;
        }

        operationHistory.recordPark(vehicleNumber, slotNumber);

        String typeLabel = isEmergency ? "EMERGENCY" : "NORMAL";
        return "SUCCESS: " + typeLabel + " vehicle " + vehicleNumber + 
               " parked at Slot " + slotNumber + " (Floor " + floor + ")";
    }

    // FIXED: Smart slot finder - Normal vehicles ONLY use slots 4-30
    private Integer findAvailableSlot(boolean isEmergency) {
        if (isEmergency) {
            // Emergency vehicles: First try slots 1-3
            for (int slotNum : emergencySlots) {
                ParkingSlot slot = allSlots.get(slotNum - 1);
                if (!slot.isOccupied()) {
                    return slotNum;
                }
            }
            
            // If all emergency slots full, emergency can use any available slot
            for (int slotNum = 4; slotNum <= allSlots.size(); slotNum++) {
                ParkingSlot slot = allSlots.get(slotNum - 1);
                if (!slot.isOccupied() && !reservedSlots.contains(slotNum)) {
                    return slotNum;
                }
            }
        } else {
            // Normal vehicles: ONLY slots 4-30 (NEVER 1-3)
            for (int slotNum = 4; slotNum <= allSlots.size(); slotNum++) {
                ParkingSlot slot = allSlots.get(slotNum - 1);
                
                if (!slot.isOccupied() && !reservedSlots.contains(slotNum)) {
                    return slotNum;
                }
            }
        }
        
        return null;
    }

    // ========== EXIT VEHICLE ==========
    
    public String exitVehicle(String vehicleNumber) {
        if (!vehicleToSlot.containsKey(vehicleNumber)) {
            return "ERROR: Vehicle " + vehicleNumber + " not found!";
        }

        int slotNumber = vehicleToSlot.get(vehicleNumber);
        ParkingSlot slot = allSlots.get(slotNumber - 1);
        boolean isEmergency = slot.hasEmergencyVehicle();

        LocalDateTime entryTime = entryTimes.get(vehicleNumber);
        LocalDateTime exitTime = LocalDateTime.now();
        Duration duration = Duration.between(entryTime, exitTime);
        long hours = duration.toHours() + (duration.toMinutes() % 60 > 0 ? 1 : 0);
        
        double charges = isEmergency ? 0.0 : hours * 20.0;

        ParkingLog log = new ParkingLog(vehicleNumber, slotNumber, entryTime, exitTime, charges);
        parkingLogs.addFirst(log);

        slot.setOccupied(false);
        slot.setVehicleNumber("");
        slot.setHasEmergencyVehicle(false);
        
        int floor = slot.getFloor();
        int posOnFloor = (slotNumber - 1) % slotsPerFloor;
        floorLayout[floor][posOnFloor] = 0;

        vehicleToSlot.remove(vehicleNumber);
        entryTimes.remove(vehicleNumber);

        occupiedSlots--;
        
        if (!emergencySlots.contains(slotNumber)) {
            availableSlots.offer(slotNumber);
            freeSlots++;
        }

        operationHistory.recordExit(vehicleNumber, slotNumber);

        if (!entryQueue.isEmpty() && freeSlots > 0) {
            String nextVehicle = entryQueue.poll();
            parkVehicle(nextVehicle, false);
        }

        String chargeInfo = isEmergency ? "Rs.0 (Emergency Service)" : "Rs." + charges;
        
        return "SUCCESS: Vehicle " + vehicleNumber + " exited. Charges: " + chargeInfo;
    }

    // ========== QUEUE PROCESSING ==========
    
    public String addToEntryQueue(String vehicleNumber) {
        entryQueue.offer(vehicleNumber);
        return "Vehicle " + vehicleNumber + " added to entry queue. Queue size: " + entryQueue.size();
    }

    public String processNextInQueue() {
        if (entryQueue.isEmpty()) {
            return "ERROR: Entry queue is empty!";
        }
        
        String vehicleNumber = entryQueue.poll();
        String result = parkVehicle(vehicleNumber, false);
        
        if (result.startsWith("ERROR")) {
            entryQueue.offer(vehicleNumber);
            return "Cannot park " + vehicleNumber + " - No normal slots available. Still in queue.";
        }
        
        return "Processed from queue: " + result;
    }

    public String viewEntryQueue() {
        if (entryQueue.isEmpty()) {
            return "Entry queue is empty.";
        }
        
        StringBuilder queueStatus = new StringBuilder("=== ENTRY QUEUE ===\n");
        queueStatus.append("Queue Size: ").append(entryQueue.size()).append("\n");
        
        int position = 1;
        for (String vehicle : entryQueue) {
            queueStatus.append(position++).append(". ").append(vehicle).append("\n");
        }
        
        return queueStatus.toString();
    }

    public int getQueueSize() {
        return entryQueue.size();
    }

    // ========== SEARCH ==========
    
    public String searchVehicle(String vehicleNumber) {
        if (vehicleToSlot.containsKey(vehicleNumber)) {
            int slotNumber = vehicleToSlot.get(vehicleNumber);
            ParkingSlot slot = allSlots.get(slotNumber - 1);
            return "Found: " + slot.toString();
        }
        return "Vehicle " + vehicleNumber + " not found!";
    }

    // ========== RESERVATION WITH SLOT MARKING ==========

    public String makeReservation(LocalDateTime time, String vehicleNumber, int slotNumber) {
        if (slotNumber < 1 || slotNumber > allSlots.size()) {
            return "ERROR: Invalid slot number!";
        }
        
        ParkingSlot slot = allSlots.get(slotNumber - 1);
        
        if (emergencySlots.contains(slotNumber)) {
            return "ERROR: Cannot reserve emergency slots (1-3)!";
        }
        
        if (slot.isOccupied()) {
            return "ERROR: Slot " + slotNumber + " is currently occupied!";
        }
        
        if (reservedSlots.contains(slotNumber)) {
            return "ERROR: Slot " + slotNumber + " is already reserved!";
        }
        
        String result = reservationTree.addReservation(time, vehicleNumber, slotNumber);
        
        if (result.startsWith("SUCCESS")) {
            reservedSlots.add(slotNumber);
            availableSlots.remove(slotNumber);
            freeSlots--;
        }
        
        return result;
    }

    public String cancelReservation(String vehicleNumber) {
        Reservation res = reservationTree.findByVehicle(vehicleNumber);
        
        if (res == null) {
            return "ERROR: No reservation found for vehicle " + vehicleNumber;
        }
        
        int slotNumber = res.getSlotNumber();
        String result = reservationTree.cancelReservation(vehicleNumber);
        
        if (result.startsWith("SUCCESS")) {
            reservedSlots.remove(slotNumber);
            availableSlots.offer(slotNumber);
            freeSlots++;
        }
        
        return result;
    }

    public List<String> processReadyReservations() {
        List<String> results = new ArrayList<>();
        List<Reservation> ready = reservationTree.getReadyToArriveReservations();
        
        for (Reservation res : ready) {
            int slotNumber = res.getSlotNumber();
            ParkingSlot slot = allSlots.get(slotNumber - 1);
            
            if (slot.isOccupied()) {
                results.add("CONFLICT: Reserved Slot " + slotNumber + " occupied!");
                
                Integer altSlot = findAvailableSlot(false);
                if (altSlot != null) {
                    ParkingSlot altParkingSlot = allSlots.get(altSlot - 1);
                    altParkingSlot.setOccupied(true);
                    altParkingSlot.setVehicleNumber(res.getVehicleNumber());
                    vehicleToSlot.put(res.getVehicleNumber(), altSlot);
                    entryTimes.put(res.getVehicleNumber(), LocalDateTime.now());
                    results.add("Vehicle " + res.getVehicleNumber() + 
                               " parked at alternative Slot " + altSlot);
                }
            } else {
                slot.setOccupied(true);
                slot.setVehicleNumber(res.getVehicleNumber());
                vehicleToSlot.put(res.getVehicleNumber(), slotNumber);
                entryTimes.put(res.getVehicleNumber(), LocalDateTime.now());
                results.add("Vehicle " + res.getVehicleNumber() + 
                           " parked at reserved Slot " + slotNumber);
            }
            
            reservedSlots.remove(slotNumber);
            reservationTree.completeReservation(res.getVehicleNumber());
        }
        
        return results;
    }

   public int cleanExpiredReservations() {
    int count = reservationTree.cleanExpiredReservations();
    
    // After cleaning expired reservations from tree, update our tracking
    List<Reservation> activeReservations = reservationTree.getAllReservations();
    
    // Find slots that are no longer reserved
    Set<Integer> currentlyReserved = new HashSet<>();
    for (Reservation res : activeReservations) {
        currentlyReserved.add(res.getSlotNumber());
    }
    
    // Remove slots from reservedSlots that are no longer in active reservations
    Iterator<Integer> iterator = reservedSlots.iterator();
    while (iterator.hasNext()) {
        int slotNum = iterator.next();
        if (!currentlyReserved.contains(slotNum)) {
            iterator.remove();
            if (!allSlots.get(slotNum - 1).isOccupied()) {
                availableSlots.offer(slotNum);
                freeSlots++;
            }
        }
    }
    
    return count;
}


    // ========== RESERVATION GETTERS ==========
    
    public List<Reservation> getAllReservations() {
        return reservationTree.getAllReservations();
    }

    public List<Reservation> getUpcomingReservations(int hours) {
        return reservationTree.getUpcomingReservations(hours);
    }

    public int getTotalReservations() {
        return reservationTree.getTotalReservations();
    }

    public Reservation findReservationByVehicle(String vehicleNumber) {
        return reservationTree.findByVehicle(vehicleNumber);
    }

    public boolean isSlotReserved(int slotNumber) {
        return reservedSlots.contains(slotNumber);
    }

    public String getSlotReservationInfo(int slotNumber) {
        List<Reservation> allReservations = reservationTree.getAllReservations();
        
        for (Reservation res : allReservations) {
            if (res.getSlotNumber() == slotNumber && res.isActive()) {
                return "Reserved: " + res.getVehicleNumber() + " at " + res.getFormattedTime();
            }
        }
        
        return null;
    }

    // ========== UNDO/REDO ==========

    public String undoLastOperation() {
        Operation op = operationHistory.undo();
        
        if (op == null) {
            return "ERROR: No operations to undo!";
        }
        
        if (op.type.equals("PARK")) {
            if (!vehicleToSlot.containsKey(op.vehicleNumber)) {
                return "ERROR: Vehicle not found!";
            }
            
            int slotNumber = vehicleToSlot.get(op.vehicleNumber);
            ParkingSlot slot = allSlots.get(slotNumber - 1);
            boolean isEmergency = slot.hasEmergencyVehicle();
            
            slot.setOccupied(false);
            slot.setVehicleNumber("");
            slot.setHasEmergencyVehicle(false);
            
            int floor = slot.getFloor();
            int posOnFloor = (slotNumber - 1) % slotsPerFloor;
            floorLayout[floor][posOnFloor] = 0;
            
            if (!emergencySlots.contains(slotNumber)) {
                availableSlots.offer(slotNumber);
                freeSlots++;
            }
            
            vehicleToSlot.remove(op.vehicleNumber);
            entryTimes.remove(op.vehicleNumber);
            occupiedSlots--;
            
            return "UNDO: Removed vehicle " + op.vehicleNumber + " from Slot " + slotNumber;
            
        } else {
            if (vehicleToSlot.containsKey(op.vehicleNumber)) {
                return "ERROR: Vehicle already parked!";
            }
            
            ParkingSlot slot = allSlots.get(op.slotNumber - 1);
            
            if (slot.isOccupied()) {
                return "ERROR: Slot now occupied!";
            }
            
            slot.setOccupied(true);
            slot.setVehicleNumber(op.vehicleNumber);
            vehicleToSlot.put(op.vehicleNumber, op.slotNumber);
            entryTimes.put(op.vehicleNumber, LocalDateTime.now());
            
            int floor = slot.getFloor();
            int posOnFloor = (op.slotNumber - 1) % slotsPerFloor;
            floorLayout[floor][posOnFloor] = 1;
            
            if (!emergencySlots.contains(op.slotNumber)) {
                availableSlots.remove(op.slotNumber);
                freeSlots--;
            }
            
            occupiedSlots++;
            
            return "UNDO: Re-parked vehicle " + op.vehicleNumber;
        }
    }

    public String redoLastOperation() {
        Operation op = operationHistory.redo();
        
        if (op == null) {
            return "ERROR: No operations to redo!";
        }
        
        if (op.type.equals("PARK")) {
            return "REDO: " + parkVehicle(op.vehicleNumber);
        } else {
            return "REDO: " + exitVehicle(op.vehicleNumber);
        }
    }

    public String viewOperationHistory() {
        return operationHistory.viewHistory();
    }

    public String viewRecentOperations(int count) {
        return operationHistory.getRecentOperations(count);
    }

    public boolean canUndo() {
        return operationHistory.canUndo();
    }

    public boolean canRedo() {
        return operationHistory.canRedo();
    }

    public int getHistorySize() {
        return operationHistory.getHistorySize();
    }

    public void clearHistory() {
        operationHistory.clear();
    }

    // ========== GRAPH METHODS ==========

    public String findShortestPathToExit(String vehicleNumber) {
        if (!vehicleToSlot.containsKey(vehicleNumber)) {
            return "ERROR: Vehicle not parked!";
        }
        
        int slotNumber = vehicleToSlot.get(vehicleNumber);
        int exitNode = parkingGraph.getExitNode();
        
        ParkingGraph.PathResult result = parkingGraph.findShortestPath(slotNumber, exitNode);
        
        if (result.path.isEmpty()) {
            return "ERROR: No path found!";
        }
        
        return parkingGraph.formatPath(result);
    }
    
    public ParkingGraph getParkingGraph() {
        return parkingGraph;
    }

    // ========== EMERGENCY VEHICLE METHODS ==========
    
    public String parkEmergencyVehicle(String vehicleNumber, String vehicleType) {
        if (vehicleToSlot.containsKey(vehicleNumber)) {
            return "ERROR: Vehicle already parked!";
        }
        
        String result = parkVehicle(vehicleNumber, true);
        
        if (result.startsWith("SUCCESS")) {
            emergencyVehicles.add(vehicleNumber);
            
            int slotNumber = vehicleToSlot.get(vehicleNumber);
            int floor = (slotNumber - 1) / slotsPerFloor;
            
            return "🚨 EMERGENCY: " + vehicleType + " (" + vehicleNumber + 
                   ") parked at PRIORITY Slot " + slotNumber + " (Floor " + floor + ")";
        }
        
        return result;
    }
    
    public String exitEmergencyVehicle(String vehicleNumber) {
        if (!emergencyVehicles.contains(vehicleNumber)) {
            return "ERROR: Not an emergency vehicle!";
        }
        
        emergencyVehicles.remove(vehicleNumber);
        return exitVehicle(vehicleNumber);
    }
    
    public String findEmergencyExitRoute(String vehicleNumber) {
        if (!vehicleToSlot.containsKey(vehicleNumber)) {
            return "ERROR: Vehicle not parked!";
        }
        
        if (!emergencyVehicles.contains(vehicleNumber)) {
            return "WARNING: " + vehicleNumber + " is not an emergency vehicle!";
        }
        
        int slotNumber = vehicleToSlot.get(vehicleNumber);
        int exitNode = parkingGraph.getExitNode();
        
        ParkingGraph.PathResult result = parkingGraph.findEmergencyExitPath(slotNumber, exitNode);
        
        return parkingGraph.formatEmergencyPath(result);
    }
    
    public String clearEmergencyRoute(String vehicleNumber) {
        return "ALERT: Emergency route cleared for " + vehicleNumber;
    }
    
    public boolean isEmergencySlot(int slotNumber) {
        return emergencySlots.contains(slotNumber);
    }
    
    public boolean isEmergencyVehicle(String vehicleNumber) {
        return emergencyVehicles.contains(vehicleNumber);
    }
    
    public int getEmergencyVehicleCount() {
        return emergencyVehicles.size();
    }

    // ========== GETTERS ==========

    public int getTotalSlots() { return totalSlots; }
    public int getFreeSlots() { return freeSlots; }
    public int getOccupiedSlots() { return occupiedSlots; }
    public int getTotalFloors() { return totalFloors; }
    public int getSlotsPerFloor() { return slotsPerFloor; }
    public ArrayList<ParkingSlot> getAllSlots() { return allSlots; }
    public LinkedList<ParkingLog> getParkingLogs() { return parkingLogs; }
    public int[][] getFloorLayout() { return floorLayout; }
    public Queue<String> getEntryQueue() { return entryQueue; }
}
