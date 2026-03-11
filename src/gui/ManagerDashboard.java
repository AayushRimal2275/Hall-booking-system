package gui;

import model.Booking;
import model.User;
import util.FileHandler;
import util.HallData;
import model.Hall;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ManagerDashboard.java - Dashboard for Manager users.
 *
 * Features:
 *   Tab 1 - All Bookings:    view every booking in the system
 *   Tab 2 - Revenue Report:  total revenue from approved bookings, broken down by hall type
 */
public class ManagerDashboard extends JFrame {

    // The logged-in manager
    private User currentUser;

    // Table models
    private DefaultTableModel allBookingsTableModel;
    private DefaultTableModel revenueTableModel;

    // Summary labels
    private JLabel totalRevenueLabel;

    /**
     * Constructor — builds the Manager Dashboard window.
     *
     * @param user  the logged-in Manager User object
     */
    public ManagerDashboard(User user) {
        this.currentUser = user;

        setTitle("Manager Dashboard - Welcome, " + user.getFullName());
        setSize(850, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Top bar: welcome + logout
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        JLabel welcomeLabel = new JLabel("Manager Dashboard  |  Logged in as: " + user.getFullName());
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 13));
        topPanel.add(welcomeLabel, BorderLayout.WEST);

        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> handleLogout());
        topPanel.add(logoutButton, BorderLayout.EAST);

        // Tabbed pane
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("All Bookings",    buildAllBookingsPanel());
        tabbedPane.addTab("Revenue Report",  buildRevenuePanel());

        setLayout(new BorderLayout());
        add(topPanel, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);

        setVisible(true);
    }

    // ====================================================================
    //  TAB 1: All Bookings
    // ====================================================================

    /**
     * Builds the "All Bookings" tab showing every booking in the system.
     */
    private JPanel buildAllBookingsPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] columns = {"Booking ID", "Customer", "Hall", "Date", "Start", "End", "Total (RM)", "Status"};
        allBookingsTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        JTable allTable = new JTable(allBookingsTableModel);
        loadAllBookings();

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> {
            loadAllBookings();
            loadRevenueData(); // Also refresh revenue when all bookings are refreshed
        });

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottomPanel.add(refreshButton);

        panel.add(new JScrollPane(allTable), BorderLayout.CENTER);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Loads all bookings into the All Bookings table.
     */
    private void loadAllBookings() {
        allBookingsTableModel.setRowCount(0);
        List<Booking> bookings = FileHandler.loadBookings();
        for (Booking b : bookings) {
            allBookingsTableModel.addRow(new Object[]{
                b.getBookingId(),
                b.getCustomerUsername(),
                b.getHallName(),
                b.getDate(),
                b.getStartTime(),
                b.getEndTime(),
                String.format("%.2f", b.getTotalPrice()),
                b.getStatus()
            });
        }
    }

    // ====================================================================
    //  TAB 2: Revenue Report
    // ====================================================================

    /**
     * Builds the "Revenue Report" tab.
     * Shows total revenue and a breakdown by hall type (from Approved bookings only).
     */
    private JPanel buildRevenuePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // ---- Summary area at the top ----
        JPanel summaryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        summaryPanel.setBorder(BorderFactory.createTitledBorder("Revenue Summary (Approved Bookings Only)"));

        totalRevenueLabel = new JLabel("Total Revenue: RM 0.00");
        totalRevenueLabel.setFont(new Font("Arial", Font.BOLD, 15));
        summaryPanel.add(totalRevenueLabel);

        panel.add(summaryPanel, BorderLayout.NORTH);

        // ---- Revenue breakdown table ----
        String[] columns = {"Hall Type", "Number of Bookings", "Revenue (RM)"};
        revenueTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        JTable revenueTable = new JTable(revenueTableModel);
        revenueTable.setRowHeight(25);

        loadRevenueData();

        panel.add(new JScrollPane(revenueTable), BorderLayout.CENTER);

        // Refresh button
        JButton refreshButton = new JButton("Refresh Report");
        refreshButton.addActionListener(e -> {
            loadRevenueData();
            loadAllBookings();
        });

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottomPanel.add(refreshButton);

        // Note about revenue calculation
        JLabel noteLabel = new JLabel("* Revenue is calculated from 'Approved' bookings only.");
        noteLabel.setFont(new Font("Arial", Font.ITALIC, 11));
        noteLabel.setForeground(Color.GRAY);
        bottomPanel.add(noteLabel);

        panel.add(bottomPanel, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Calculates and displays the revenue data broken down by hall type.
     * Only counts bookings with status "Approved".
     */
    private void loadRevenueData() {
        revenueTableModel.setRowCount(0);

        // Map to store: hallName -> {count, totalRevenue}
        Map<String, double[]> revenueMap = new HashMap<>();

        // Initialize map with all hall names (so halls with 0 revenue still appear)
        for (Hall hall : HallData.getHalls()) {
            revenueMap.put(hall.getHallName(), new double[]{0, 0.0}); // [count, revenue]
        }

        // Sum up revenue from approved bookings
        List<Booking> bookings = FileHandler.loadBookings();
        double grandTotal = 0.0;

        for (Booking b : bookings) {
            if ("Approved".equals(b.getStatus())) {
                String hallName = b.getHallName();
                if (revenueMap.containsKey(hallName)) {
                    revenueMap.get(hallName)[0]++;              // Increment count
                    revenueMap.get(hallName)[1] += b.getTotalPrice(); // Add price
                }
                grandTotal += b.getTotalPrice();
            }
        }

        // Populate the revenue table (in the same order as HallData.getHalls())
        for (Hall hall : HallData.getHalls()) {
            double[] data = revenueMap.get(hall.getHallName());
            revenueTableModel.addRow(new Object[]{
                hall.getHallName(),
                (int) data[0],
                String.format("RM %.2f", data[1])
            });
        }

        // Update the total revenue label
        totalRevenueLabel.setText(String.format("Total Revenue: RM %.2f", grandTotal));
    }

    // ====================================================================
    //  LOGOUT
    // ====================================================================

    private void handleLogout() {
        dispose();
        new LoginFrame();
    }
}
