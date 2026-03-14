package gui;

import managers.ParkingManager;
import models.ParkingSlot;
import models.Reservation;
import graphs.ParkingGraph;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

public class MainFrame extends JFrame {
    private ParkingManager parkingManager;
    
    private JLabel titleLabel;
    private JLabel totalLabel, freeLabel, occupiedLabel, queueLabel, reservationLabel, historyLabel;
    private JPanel dashboardPanel, buttonPanel, slotPanel;
    private JButton parkButton, exitButton, searchButton, viewLogsButton;
    private JTextArea outputArea;
    
    public MainFrame() {
        parkingManager = new ParkingManager(3, 10);
        
        setTitle("Smart Parking System");
        setSize(1200, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(8, 8));
        getContentPane().setBackground(new Color(240, 240, 240));
        
        createDashboard();
        createSlotDisplay();
        createButtons();
        createOutputArea();
        
        setVisible(true);
    }
    
    private void createDashboard() {
        dashboardPanel = new JPanel(new GridLayout(1, 7, 8, 8));
        dashboardPanel.setBackground(new Color(52, 73, 94));
        dashboardPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        titleLabel = new JLabel("SMART PARKING SYSTEM", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(Color.WHITE);
        
        totalLabel = createDashboardLabel("Total: " + parkingManager.getTotalSlots());
        freeLabel = createDashboardLabel("Free: " + parkingManager.getFreeSlots());
        occupiedLabel = createDashboardLabel("Occupied: " + parkingManager.getOccupiedSlots());
        queueLabel = createDashboardLabel("Queue: " + parkingManager.getQueueSize());
        reservationLabel = createDashboardLabel("Reserved: " + parkingManager.getTotalReservations());
        historyLabel = createDashboardLabel("History: " + parkingManager.getHistorySize());
        
        dashboardPanel.add(titleLabel);
        dashboardPanel.add(totalLabel);
        dashboardPanel.add(freeLabel);
        dashboardPanel.add(occupiedLabel);
        dashboardPanel.add(queueLabel);
        dashboardPanel.add(reservationLabel);
        dashboardPanel.add(historyLabel);
        
        add(dashboardPanel, BorderLayout.NORTH);
    }
    
    private JLabel createDashboardLabel(String text) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 12));
        label.setForeground(Color.WHITE);
        return label;
    }
    
    private void createSlotDisplay() {
        JPanel mainPanel = new JPanel(new BorderLayout(8, 8));
        mainPanel.setBackground(new Color(240, 240, 240));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 8));
        
        JPanel entrancePanel = createGatePanel("⬇ ENTRANCE GATE ⬇", new Color(46, 204, 113));
        
        JPanel floorsPanel = new JPanel();
        floorsPanel.setLayout(new BoxLayout(floorsPanel, BoxLayout.Y_AXIS));
        floorsPanel.setBackground(new Color(240, 240, 240));
        
        int totalFloors = parkingManager.getTotalFloors();
        
        for (int floor = 0; floor < totalFloors; floor++) {
            JPanel floorCard = createFloorCard(floor);
            floorsPanel.add(floorCard);
            
            if (floor < totalFloors - 1) {
                floorsPanel.add(Box.createVerticalStrut(8));
            }
        }
        
        slotPanel = floorsPanel;
        
        JPanel exitPanel = createGatePanel("⬆ MAIN EXIT GATE ⬆", new Color(231, 76, 60));
        
        mainPanel.add(entrancePanel, BorderLayout.NORTH);
        mainPanel.add(floorsPanel, BorderLayout.CENTER);
        mainPanel.add(exitPanel, BorderLayout.SOUTH);
        
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBackground(new Color(240, 240, 240));
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        add(scrollPane, BorderLayout.CENTER);
        
        updateSlotDisplay();
    }
    
    private JPanel createGatePanel(String text, Color color) {
        JPanel panel = new JPanel();
        panel.setBackground(color);
        panel.setBorder(BorderFactory.createLineBorder(color.darker(), 2, true));
        panel.setPreferredSize(new Dimension(0, 35));
        
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 16));
        label.setForeground(Color.WHITE);
        
        panel.add(label);
        
        return panel;
    }
    
    private JPanel createFloorCard(int floor) {
        JPanel card = new JPanel(new BorderLayout(8, 8));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(52, 73, 94), 2, true),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(52, 73, 94));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        
        JLabel floorLabel = new JLabel("FLOOR " + floor);
        floorLabel.setFont(new Font("Arial", Font.BOLD, 15));
        floorLabel.setForeground(Color.WHITE);
        
        int startSlot = floor * 10 + 1;
        int endSlot = startSlot + 9;
        
        String rangeText = "Slots " + startSlot + "-" + endSlot;
        if (floor == 0) {
            rangeText += " (Emergency: 1-3)";
        }
        
        JLabel rangeLabel = new JLabel(rangeText);
        rangeLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        rangeLabel.setForeground(new Color(220, 220, 220));
        
        headerPanel.add(floorLabel, BorderLayout.WEST);
        headerPanel.add(rangeLabel, BorderLayout.EAST);
        
        JPanel contentPanel = new JPanel(new BorderLayout(5, 0));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(6, 0, 0, 0));
        
        JPanel slotsPanel = new JPanel(new GridLayout(1, 10, 5, 5));
        slotsPanel.setBackground(Color.WHITE);
        
        for (int i = 0; i < 10; i++) {
            JButton slotBtn = createSlotButton(startSlot + i);
            slotsPanel.add(slotBtn);
        }
        
        JPanel exitIndicator = new JPanel(new BorderLayout());
        exitIndicator.setBackground(new Color(231, 76, 60));
        exitIndicator.setBorder(BorderFactory.createLineBorder(new Color(192, 57, 43), 2, true));
        exitIndicator.setPreferredSize(new Dimension(55, 50));
        
        JLabel exitLabel = new JLabel("<html><center>EXIT<br>→</center></html>");
        exitLabel.setFont(new Font("Arial", Font.BOLD, 10));
        exitLabel.setForeground(Color.WHITE);
        exitLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        exitIndicator.add(exitLabel, BorderLayout.CENTER);
        exitIndicator.setToolTipText("Floor Exit Gate - 15m from Slot " + endSlot);
        
        contentPanel.add(slotsPanel, BorderLayout.CENTER);
        contentPanel.add(exitIndicator, BorderLayout.EAST);
        
        card.add(headerPanel, BorderLayout.NORTH);
        card.add(contentPanel, BorderLayout.CENTER);
        
        return card;
    }
    
    private JButton createSlotButton(int slotNumber) {
        JButton button = new JButton(String.valueOf(slotNumber));
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setBorderPainted(true);
        button.setPreferredSize(new Dimension(50, 50));
        button.setMargin(new Insets(2, 2, 2, 2));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        return button;
    }
    
    private void updateSlotDisplay() {
        Component[] floors = slotPanel.getComponents();
        
        int slotIndex = 0;
        
        for (Component floorComp : floors) {
            if (floorComp instanceof JPanel) {
                JPanel floorCard = (JPanel) floorComp;
                Component[] cardComponents = floorCard.getComponents();
                
                for (Component comp : cardComponents) {
                    if (comp instanceof JPanel) {
                        JPanel panel = (JPanel) comp;
                        
                        Component[] innerComps = panel.getComponents();
                        for (Component innerComp : innerComps) {
                            if (innerComp instanceof JPanel) {
                                JPanel innerPanel = (JPanel) innerComp;
                                if (innerPanel.getLayout() instanceof GridLayout) {
                                    Component[] slotButtons = innerPanel.getComponents();
                                    
                                    for (Component slotComp : slotButtons) {
                                        if (slotComp instanceof JButton && slotIndex < parkingManager.getAllSlots().size()) {
                                            JButton button = (JButton) slotComp;
                                            ParkingSlot slot = parkingManager.getAllSlots().get(slotIndex);
                                            
                                            updateSlotButton(button, slot);
                                            slotIndex++;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        slotPanel.revalidate();
        slotPanel.repaint();
        updateDashboard();
    }
    
    private void updateSlotButton(JButton button, ParkingSlot slot) {
        String slotType = slot.getSlotType();
        
        if (slot.hasEmergencyVehicle()) {
            button.setBackground(new Color(255, 0, 0));
            button.setForeground(Color.WHITE);
            button.setText("E" + slot.getSlotNumber());
            button.setToolTipText("🚨 EMERGENCY: " + slot.getVehicleNumber());
            button.setBorder(BorderFactory.createLineBorder(new Color(139, 0, 0), 2, true));
        }
        else if (slotType.equals("EMERGENCY") && !slot.isOccupied()) {
            button.setBackground(new Color(255, 140, 0));
            button.setForeground(Color.WHITE);
            button.setText("E" + slot.getSlotNumber());
            button.setToolTipText("⚠️ EMERGENCY SLOT - Reserved for Emergency Vehicles");
            button.setBorder(BorderFactory.createLineBorder(new Color(204, 85, 0), 2, true));
        }
        else if (slot.isOccupied()) {
            button.setBackground(new Color(231, 76, 60));
            button.setForeground(Color.WHITE);
            button.setText(String.valueOf(slot.getSlotNumber()));
            button.setToolTipText("🚗 Occupied: " + slot.getVehicleNumber());
            button.setBorder(BorderFactory.createLineBorder(new Color(192, 57, 43), 2, true));
        }
        else if (parkingManager.isSlotReserved(slot.getSlotNumber())) {
            button.setBackground(new Color(241, 196, 15));
            button.setForeground(Color.BLACK);
            button.setText(String.valueOf(slot.getSlotNumber()));
            String info = parkingManager.getSlotReservationInfo(slot.getSlotNumber());
            button.setToolTipText("📅 RESERVED: " + (info != null ? info : "Unknown"));
            button.setBorder(BorderFactory.createLineBorder(new Color(183, 149, 11), 2, true));
        }
        else {
            button.setBackground(new Color(46, 204, 113));
            button.setForeground(Color.WHITE);
            button.setText(String.valueOf(slot.getSlotNumber()));
            button.setToolTipText("✅ Available for Normal Vehicles");
            button.setBorder(BorderFactory.createLineBorder(new Color(39, 174, 96), 2, true));
        }
    }
    
    private void createButtons() {
        buttonPanel = new JPanel(new GridLayout(5, 4, 6, 6));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 8));
        
        parkButton = new JButton("Park Vehicle");
        exitButton = new JButton("Exit Vehicle");
        searchButton = new JButton("Search Vehicle");
        viewLogsButton = new JButton("View Logs");
        
        JButton addToQueueButton = new JButton("Add to Queue");
        JButton processQueueButton = new JButton("Process Queue");
        JButton undoButton = new JButton("Undo");
        JButton redoButton = new JButton("Redo");
        
        JButton makeReservationButton = new JButton("Make Reservation");
        JButton viewReservationsButton = new JButton("View Reservations");
        JButton cancelReservationButton = new JButton("Cancel Reservation");
        JButton checkArrivalsButton = new JButton("Check Arrivals");
        
        JButton upcomingReservationsButton = new JButton("Upcoming");
        JButton cleanReservationsButton = new JButton("Clean Expired");
        JButton viewHistoryButton = new JButton("View History");
        JButton clearHistoryButton = new JButton("Clear History");
        
        JButton parkEmergencyButton = new JButton("Park Emergency");
        JButton emergencyExitButton = new JButton("Emergency Exit");
        JButton emergencyRouteButton = new JButton("Emergency Route");
        JButton showExitRouteButton = new JButton("Show Exit Route");
        
        parkButton.addActionListener(e -> parkVehicle());
        exitButton.addActionListener(e -> exitVehicle());
        searchButton.addActionListener(e -> searchVehicle());
        viewLogsButton.addActionListener(e -> viewLogs());
        addToQueueButton.addActionListener(e -> addToQueue());
        processQueueButton.addActionListener(e -> processQueue());
        undoButton.addActionListener(e -> undoOperation());
        redoButton.addActionListener(e -> redoOperation());
        makeReservationButton.addActionListener(e -> makeReservation());
        viewReservationsButton.addActionListener(e -> viewReservations());
        cancelReservationButton.addActionListener(e -> cancelReservation());
        checkArrivalsButton.addActionListener(e -> checkReadyReservations());
        upcomingReservationsButton.addActionListener(e -> viewUpcomingReservations());
        cleanReservationsButton.addActionListener(e -> cleanExpiredReservations());
        viewHistoryButton.addActionListener(e -> viewOperationHistory());
        clearHistoryButton.addActionListener(e -> clearOperationHistory());
        parkEmergencyButton.addActionListener(e -> parkEmergencyVehicle());
        emergencyExitButton.addActionListener(e -> exitEmergencyVehicle());
        emergencyRouteButton.addActionListener(e -> showEmergencyRoute());
        showExitRouteButton.addActionListener(e -> showExitRoute());
        
        buttonPanel.add(parkButton);
        buttonPanel.add(exitButton);
        buttonPanel.add(searchButton);
        buttonPanel.add(viewLogsButton);
        
        buttonPanel.add(addToQueueButton);
        buttonPanel.add(processQueueButton);
        buttonPanel.add(undoButton);
        buttonPanel.add(redoButton);
        
        buttonPanel.add(makeReservationButton);
        buttonPanel.add(viewReservationsButton);
        buttonPanel.add(cancelReservationButton);
        buttonPanel.add(checkArrivalsButton);
        
        buttonPanel.add(upcomingReservationsButton);
        buttonPanel.add(cleanReservationsButton);
        buttonPanel.add(viewHistoryButton);
        buttonPanel.add(clearHistoryButton);
        
        buttonPanel.add(parkEmergencyButton);
        buttonPanel.add(emergencyExitButton);
        buttonPanel.add(emergencyRouteButton);
        buttonPanel.add(showExitRouteButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void createOutputArea() {
        outputArea = new JTextArea(5, 33);
        outputArea.setEditable(false);
        outputArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        outputArea.setBackground(new Color(248, 249, 250));
        JScrollPane scrollPane = new JScrollPane(outputArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Activity Log"));
        
        JPanel container = new JPanel(new BorderLayout());
        container.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 15));
        container.add(scrollPane, BorderLayout.CENTER);
        
        add(container, BorderLayout.EAST);
    }
    
    private void parkVehicle() {
        String vehicleNumber = JOptionPane.showInputDialog(this, 
            "Enter Vehicle Number:", "Park Vehicle", JOptionPane.QUESTION_MESSAGE);
        
        if (vehicleNumber != null && !vehicleNumber.trim().isEmpty()) {
            String result = parkingManager.parkVehicle(vehicleNumber.trim().toUpperCase());
            outputArea.append(result + "\n");
            updateSlotDisplay();
        }
    }
    
    private void exitVehicle() {
        String vehicleNumber = JOptionPane.showInputDialog(this, 
            "Enter Vehicle Number:", "Exit Vehicle", JOptionPane.QUESTION_MESSAGE);
        
        if (vehicleNumber != null && !vehicleNumber.trim().isEmpty()) {
            String result = parkingManager.exitVehicle(vehicleNumber.trim().toUpperCase());
            outputArea.append(result + "\n");
            updateSlotDisplay();
        }
    }
    
    private void searchVehicle() {
        String vehicleNumber = JOptionPane.showInputDialog(this, 
            "Enter Vehicle Number:", "Search Vehicle", JOptionPane.QUESTION_MESSAGE);
        
        if (vehicleNumber != null && !vehicleNumber.trim().isEmpty()) {
            String result = parkingManager.searchVehicle(vehicleNumber.trim().toUpperCase());
            outputArea.append(result + "\n");
        }
    }
    
    private void viewLogs() {
        StringBuilder logs = new StringBuilder("=== PARKING LOGS ===\n");
        parkingManager.getParkingLogs().forEach(log -> logs.append(log.toString()).append("\n"));
        outputArea.setText(logs.toString());
    }
    
    private void updateDashboard() {
        freeLabel.setText("Free: " + parkingManager.getFreeSlots());
        occupiedLabel.setText("Occupied: " + parkingManager.getOccupiedSlots());
        queueLabel.setText("Queue: " + parkingManager.getQueueSize());
        reservationLabel.setText("Reserved: " + parkingManager.getTotalReservations());
        historyLabel.setText("History: " + parkingManager.getHistorySize());
    }
    
    private void addToQueue() {
        String vehicleNumber = JOptionPane.showInputDialog(this, 
            "Enter Vehicle Number:", "Add to Queue", JOptionPane.QUESTION_MESSAGE);
        
        if (vehicleNumber != null && !vehicleNumber.trim().isEmpty()) {
            String result = parkingManager.addToEntryQueue(vehicleNumber.trim().toUpperCase());
            outputArea.append(result + "\n");
            updateSlotDisplay();
        }
    }
    
    private void processQueue() {
        String result = parkingManager.processNextInQueue();
        outputArea.append(result + "\n");
        updateSlotDisplay();
    }
    
    private void makeReservation() {
        JTextField vehicleField = new JTextField();
        
        List<String> availableSlotsList = new ArrayList<>();
        Map<String, Integer> slotMap = new HashMap<>();
        
        for (ParkingSlot slot : parkingManager.getAllSlots()) {
            int slotNum = slot.getSlotNumber();
            
            if (slotNum > 3 && !slot.isOccupied() && !parkingManager.isSlotReserved(slotNum)) {
                int floor = (slotNum - 1) / parkingManager.getSlotsPerFloor();
                String displayText = "Slot " + slotNum + " (Floor " + floor + ")";
                availableSlotsList.add(displayText);
                slotMap.put(displayText, slotNum);
            }
        }
        
        if (availableSlotsList.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "No slots available for reservation!\nAll slots are either occupied or already reserved.",
                "No Available Slots", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        JComboBox<String> slotCombo = new JComboBox<>(availableSlotsList.toArray(new String[0]));
        
        LocalDateTime defaultTime = LocalDateTime.now().plusHours(1);
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        
        JTextField dateField = new JTextField(defaultTime.format(dateFormatter));
        JTextField timeField = new JTextField(defaultTime.format(timeFormatter));
        
        Object[] message = {
            "Vehicle Number:", vehicleField,
            "Select Available Slot:", slotCombo,
            "Date (yyyy-MM-dd):", dateField,
            "Time (HH:mm):", timeField
        };
        
        int option = JOptionPane.showConfirmDialog(this, message, 
            "Make Reservation", JOptionPane.OK_CANCEL_OPTION);
        
        if (option == JOptionPane.OK_OPTION) {
            try {
                String vehicle = vehicleField.getText().trim().toUpperCase();
                
                if (vehicle.isEmpty()) {
                    outputArea.append("ERROR: Vehicle number cannot be empty!\n");
                    return;
                }
                
                String selectedSlot = (String) slotCombo.getSelectedItem();
                int slot = slotMap.get(selectedSlot);
                String dateTime = dateField.getText() + " " + timeField.getText();
                
                LocalDateTime reservationTime = LocalDateTime.parse(dateTime, 
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                
                String result = parkingManager.makeReservation(reservationTime, vehicle, slot);
                outputArea.append(result + "\n");
                updateSlotDisplay();
                
            } catch (Exception ex) {
                outputArea.append("ERROR: Invalid input! " + ex.getMessage() + "\n");
            }
        }
    }
    
    private void viewReservations() {
        List<Reservation> reservations = parkingManager.getAllReservations();
        
        if (reservations.isEmpty()) {
            outputArea.append("No active reservations.\n");
        } else {
            outputArea.append("=== RESERVATIONS ===\n");
            for (Reservation res : reservations) {
                outputArea.append(res.toString() + "\n");
            }
        }
    }
    
    private void cancelReservation() {
        String vehicleNumber = JOptionPane.showInputDialog(this,
            "Enter Vehicle Number:", "Cancel Reservation", JOptionPane.QUESTION_MESSAGE);
        
        if (vehicleNumber != null && !vehicleNumber.trim().isEmpty()) {
            String result = parkingManager.cancelReservation(vehicleNumber.trim().toUpperCase());
            outputArea.append(result + "\n");
            updateSlotDisplay();
        }
    }
    
    private void checkReadyReservations() {
        List<String> results = parkingManager.processReadyReservations();
        
        if (results.isEmpty()) {
            outputArea.append("No reservations ready.\n");
        } else {
            outputArea.append("=== PROCESSING RESERVATIONS ===\n");
            for (String result : results) {
                outputArea.append(result + "\n");
            }
            updateSlotDisplay();
        }
    }
    
    private void viewUpcomingReservations() {
        String input = JOptionPane.showInputDialog(this, 
            "Enter hours ahead:", "Upcoming Reservations", JOptionPane.QUESTION_MESSAGE);
        
        if (input != null && !input.trim().isEmpty()) {
            try {
                int hours = Integer.parseInt(input.trim());
                List<Reservation> upcoming = parkingManager.getUpcomingReservations(hours);
                
                if (upcoming.isEmpty()) {
                    outputArea.append("No reservations in next " + hours + " hours.\n");
                } else {
                    outputArea.append("=== UPCOMING (Next " + hours + " hours) ===\n");
                    for (Reservation res : upcoming) {
                        outputArea.append(res.toString() + "\n");
                    }
                }
            } catch (NumberFormatException ex) {
                outputArea.append("ERROR: Invalid number!\n");
            }
        }
    }
    
    private void cleanExpiredReservations() {
        int count = parkingManager.cleanExpiredReservations();
        outputArea.append("Cleaned " + count + " expired reservation(s).\n");
        updateSlotDisplay();
    }
    
    private void undoOperation() {
        if (!parkingManager.canUndo()) {
            JOptionPane.showMessageDialog(this, "No operations to undo!", "Undo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        String result = parkingManager.undoLastOperation();
        outputArea.append(result + "\n");
        updateSlotDisplay();
    }
    
    private void redoOperation() {
        if (!parkingManager.canRedo()) {
            JOptionPane.showMessageDialog(this, "No operations to redo!", "Redo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        String result = parkingManager.redoLastOperation();
        outputArea.append(result + "\n");
        updateSlotDisplay();
    }
    
    private void viewOperationHistory() {
        String history = parkingManager.viewOperationHistory();
        outputArea.setText(history);
    }
    
    private void clearOperationHistory() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Clear history?", "Confirm", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            parkingManager.clearHistory();
            outputArea.append("History cleared.\n");
            updateSlotDisplay();
        }
    }
    
    // ========== ROUTE DIALOG METHODS (NEW) ==========
    
    private void showExitRoute() {
        String vehicleNumber = JOptionPane.showInputDialog(this,
            "Enter Vehicle Number:", "Show Exit Route", JOptionPane.QUESTION_MESSAGE);
        
        if (vehicleNumber != null && !vehicleNumber.trim().isEmpty()) {
            String result = parkingManager.findShortestPathToExit(vehicleNumber.trim().toUpperCase());
            outputArea.append(result + "\n");
            showRouteDialog(vehicleNumber.trim().toUpperCase(), result, false);
        }
    }
    
    private void showEmergencyRoute() {
        String vehicleNumber = JOptionPane.showInputDialog(this,
            "Enter Vehicle Number:", "Emergency Route", JOptionPane.QUESTION_MESSAGE);
        
        if (vehicleNumber != null && !vehicleNumber.trim().isEmpty()) {
            String result = parkingManager.findEmergencyExitRoute(vehicleNumber.trim().toUpperCase());
            outputArea.append(result + "\n");
            showRouteDialog(vehicleNumber.trim().toUpperCase(), result, true);
        }
    }
    
    private void showRouteDialog(String vehicleNumber, String routeInfo, boolean isEmergency) {
        JDialog dialog = new JDialog(this, "Exit Route - " + vehicleNumber, true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(550, 450);
        dialog.setLocationRelativeTo(this);
        
        // HEADER
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(isEmergency ? new Color(231, 76, 60) : new Color(52, 73, 94));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        String headerText = isEmergency ? "🚨 EMERGENCY EXIT ROUTE" : "🗺️ EXIT ROUTE";
        JLabel headerLabel = new JLabel(headerText);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 18));
        headerLabel.setForeground(Color.WHITE);
        headerLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        JLabel vehicleLabel = new JLabel("Vehicle: " + vehicleNumber);
        vehicleLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        vehicleLabel.setForeground(Color.WHITE);
        vehicleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        headerPanel.add(headerLabel, BorderLayout.NORTH);
        headerPanel.add(vehicleLabel, BorderLayout.CENTER);
        
        // ROUTE DISPLAY
        JTextArea routeArea = new JTextArea(routeInfo);
        routeArea.setEditable(false);
        routeArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        routeArea.setBackground(new Color(248, 249, 250));
        routeArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        routeArea.setLineWrap(true);
        routeArea.setWrapStyleWord(true);
        
        JScrollPane scrollPane = new JScrollPane(routeArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
        
        // INFO PANEL
        JPanel infoPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        infoPanel.setBackground(Color.WHITE);
        
        String distance = extractDistance(routeInfo);
        
        JLabel distanceLabel = new JLabel("📏 Total Distance: " + distance);
        distanceLabel.setFont(new Font("Arial", Font.BOLD, 14));
        
        JLabel timeLabel = new JLabel("⏱️ Estimated Time: " + estimateTime(distance));
        timeLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        
        JLabel instructionLabel = new JLabel("💡 Follow the route to reach EXIT GATE");
        instructionLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        instructionLabel.setForeground(new Color(100, 100, 100));
        
        infoPanel.add(distanceLabel);
        infoPanel.add(timeLabel);
        infoPanel.add(instructionLabel);
        
        // BUTTONS
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton okButton = new JButton("OK");
        okButton.setFont(new Font("Arial", Font.BOLD, 12));
        okButton.setPreferredSize(new Dimension(100, 35));
        okButton.setBackground(new Color(46, 204, 113));
        okButton.setForeground(Color.WHITE);
        okButton.setFocusPainted(false);
        okButton.addActionListener(e -> dialog.dispose());
        
        JButton copyButton = new JButton("Copy Route");
        copyButton.setFont(new Font("Arial", Font.PLAIN, 12));
        copyButton.setPreferredSize(new Dimension(120, 35));
        copyButton.setBackground(new Color(52, 152, 219));
        copyButton.setForeground(Color.WHITE);
        copyButton.setFocusPainted(false);
        copyButton.addActionListener(e -> {
            routeArea.selectAll();
            routeArea.copy();
            JOptionPane.showMessageDialog(dialog, "Route copied to clipboard!", 
                "Success", JOptionPane.INFORMATION_MESSAGE);
        });
        
        buttonPanel.add(okButton);
        buttonPanel.add(copyButton);
        
        // ASSEMBLE
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(infoPanel, BorderLayout.SOUTH);
        
        dialog.add(headerPanel, BorderLayout.NORTH);
        dialog.add(mainPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.setVisible(true);
    }
    
    private String extractDistance(String routeInfo) {
        try {
            String[] lines = routeInfo.split("\n");
            for (String line : lines) {
                if (line.contains("Total Distance:") || line.contains("Distance:")) {
                    int start = line.indexOf(":") + 1;
                    return line.substring(start).trim();
                }
            }
        } catch (Exception e) {
            // Ignore
        }
        return "Unknown";
    }
    
    private String estimateTime(String distanceStr) {
        try {
            String numStr = distanceStr.replaceAll("[^0-9]", "");
            if (!numStr.isEmpty()) {
                int meters = Integer.parseInt(numStr);
                int seconds = (int) (meters / 1.4);
                
                if (seconds < 60) {
                    return seconds + " seconds";
                } else {
                    int minutes = seconds / 60;
                    int remainingSeconds = seconds % 60;
                    return minutes + " min " + remainingSeconds + " sec";
                }
            }
        } catch (Exception e) {
            // Ignore
        }
        return "~1 minute";
    }
    
    // ========== EMERGENCY VEHICLE METHODS ==========
    
    private void parkEmergencyVehicle() {
        JTextField vehicleField = new JTextField();
        
        String[] types = {"Ambulance", "Fire Truck", "Police"};
        JComboBox<String> typeCombo = new JComboBox<>(types);
        
        Object[] message = {
            "Vehicle Number:", vehicleField,
            "Type:", typeCombo
        };
        
        int option = JOptionPane.showConfirmDialog(this, message,
            "Park Emergency Vehicle", JOptionPane.OK_CANCEL_OPTION);
        
        if (option == JOptionPane.OK_OPTION) {
            String vehicleNumber = vehicleField.getText().trim().toUpperCase();
            String type = (String) typeCombo.getSelectedItem();
            
            if (!vehicleNumber.isEmpty()) {
                String result = parkingManager.parkEmergencyVehicle(vehicleNumber, type);
                outputArea.append(result + "\n");
                updateSlotDisplay();
            }
        }
    }
    
    private void exitEmergencyVehicle() {
        String vehicleNumber = JOptionPane.showInputDialog(this,
            "Enter Vehicle Number:", "Emergency Exit", JOptionPane.QUESTION_MESSAGE);
        
        if (vehicleNumber != null && !vehicleNumber.trim().isEmpty()) {
            String result = parkingManager.exitEmergencyVehicle(vehicleNumber.trim().toUpperCase());
            outputArea.append(result + "\n");
            updateSlotDisplay();
        }
    }
}
