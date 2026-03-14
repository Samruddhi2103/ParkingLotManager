package managers;

import java.util.Stack;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

class Operation {
    String type;              // "PARK" or "EXIT"
    String vehicleNumber;
    int slotNumber;
    LocalDateTime timestamp;
    
    public Operation(String type, String vehicle, int slot) {
        this.type = type;
        this.vehicleNumber = vehicle;
        this.slotNumber = slot;
        this.timestamp = LocalDateTime.now();
    }
    
    public String getFormattedTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        return timestamp.format(formatter);
    }
    
    @Override
    public String toString() {
        return String.format("[%s] %s: %s at Slot %d", 
                           getFormattedTime(), type, vehicleNumber, slotNumber);
    }
}

public class OperationHistory {
    private Stack<Operation> undoStack;  // Stack for undo operations (LIFO)
    private Stack<Operation> redoStack;  // Stack for redo operations (LIFO)
    private int maxHistorySize;
    
    public OperationHistory(int maxSize) {
        undoStack = new Stack<>();
        redoStack = new Stack<>();
        this.maxHistorySize = maxSize;
    }
    
    // Record a parking operation (push to stack)
    public void recordPark(String vehicleNumber, int slotNumber) {
        Operation op = new Operation("PARK", vehicleNumber, slotNumber);
        undoStack.push(op);  // Push to top of stack
        redoStack.clear();    // Clear redo stack when new action performed
        
        // Limit stack size (remove oldest if exceeded)
        if (undoStack.size() > maxHistorySize) {
            undoStack.remove(0);
        }
    }
    
    // Record an exit operation (push to stack)
    public void recordExit(String vehicleNumber, int slotNumber) {
        Operation op = new Operation("EXIT", vehicleNumber, slotNumber);
        undoStack.push(op);  // Push to top of stack
        redoStack.clear();
        
        if (undoStack.size() > maxHistorySize) {
            undoStack.remove(0);
        }
    }
    
    // Undo last operation (pop from undo stack)
    public Operation undo() {
        if (undoStack.isEmpty()) {
            return null;
        }
        
        Operation op = undoStack.pop();  // Pop from top (LIFO)
        redoStack.push(op);              // Push to redo stack
        return op;
    }
    
    // Redo last undone operation (pop from redo stack)
    public Operation redo() {
        if (redoStack.isEmpty()) {
            return null;
        }
        
        Operation op = redoStack.pop();  // Pop from redo stack
        undoStack.push(op);              // Push back to undo stack
        return op;
    }
    
    // Peek at last operation without removing
    public Operation peekLast() {
        if (undoStack.isEmpty()) {
            return null;
        }
        return undoStack.peek();
    }
    
    // Check if undo is available
    public boolean canUndo() {
        return !undoStack.isEmpty();
    }
    
    // Check if redo is available
    public boolean canRedo() {
        return !redoStack.isEmpty();
    }
    
    // View complete operation history (most recent first)
    public String viewHistory() {
        if (undoStack.isEmpty()) {
            return "No operation history available.";
        }
        
        StringBuilder history = new StringBuilder("=== OPERATION HISTORY (Stack View) ===\n");
        history.append("Recent operations (Top → Bottom):\n\n");
        
        // Create temporary stack to preserve original
        Stack<Operation> temp = new Stack<>();
        int position = undoStack.size();
        
        // Pop all elements (displays in reverse order - most recent first)
        while (!undoStack.isEmpty()) {
            Operation op = undoStack.pop();
            history.append(position--).append(". ").append(op.toString()).append("\n");
            temp.push(op);
        }
        
        // Restore original stack
        while (!temp.isEmpty()) {
            undoStack.push(temp.pop());
        }
        
        history.append("\nTotal operations in stack: ").append(undoStack.size());
        return history.toString();
    }
    
    // Get stack size
    public int getHistorySize() {
        return undoStack.size();
    }
    
    // Get redo stack size
    public int getRedoSize() {
        return redoStack.size();
    }
    
    // Clear all history
    public void clear() {
        undoStack.clear();
        redoStack.clear();
    }
    
    // Get last N operations
    public String getRecentOperations(int count) {
        if (undoStack.isEmpty()) {
            return "No recent operations.";
        }
        
        StringBuilder recent = new StringBuilder("=== LAST " + count + " OPERATIONS ===\n");
        Stack<Operation> temp = new Stack<>();
        int displayed = 0;
        
        while (!undoStack.isEmpty() && displayed < count) {
            Operation op = undoStack.pop();
            recent.append((displayed + 1)).append(". ").append(op.toString()).append("\n");
            temp.push(op);
            displayed++;
        }
        
        // Restore
        while (!temp.isEmpty()) {
            undoStack.push(temp.pop());
        }
        
        return recent.toString();
    }
}
