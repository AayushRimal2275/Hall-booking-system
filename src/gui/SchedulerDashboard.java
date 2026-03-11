package gui;

import model.Booking;
import model.User;
import util.FileHandler;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * SchedulerDashboard.java - Dashboard for Scheduler users.
 *
 * Features:
 *   Tab 1 - Pending Bookings: view bookings with status "Pending", approve or reject them
 *   Tab 2 - All Bookings:     view all bookings in the system
 *
 * When approving a booking, the scheduler checks:
 *   1. The booking time is within operating hours (8AM – 6PM)
 *   2. There is no time conflict with another approved booking for the same hall on the same date
 */
public class SchedulerDashboard extends JFrame {

    // The logged-in scheduler
    private User currentUser;

    // Table models
    private DefaultTableModel pendingTableModel;
    private DefaultTableModel allBookingsTableModel;

    /**
     * Constructor — builds the Scheduler Dashboard window.
     *
     * @param user  the logged-in Scheduler User object
     */
    public SchedulerDashboard(User user) {
        this.currentUser = user;

        setTitle("Scheduler Dashboard - Welcome, " + user.getFullName());
        setSize(850, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Top panel: welcome + logout
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        JLabel welcomeLabel = new JLabel("Scheduler Dashboard  |  Logged in as: " + user.getFullName());
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 13));
        topPanel.add(welcomeLabel, BorderLayout.WEST);

        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> handleLogout());
        topPanel.add(logoutButton, BorderLayout.EAST);

        // Tabbed pane
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Pending Bookings", buildPendingPanel());
        tabbedPane.addTab("All Bookings",     buildAllBookingsPanel());

        setLayout(new BorderLayout());
        add(topPanel, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);

        setVisible(true);
    }

    // ====================================================================
    //  TAB 1: Pending Bookings
    // ====================================================================

    /**
     * Builds the "Pending Bookings" tab with approve/reject buttons.
     */
    private JPanel buildPendingPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] columns = {"Booking ID", "Customer", "Hall", "Date", "Start", "End", "Total (RM)", "Status"};
        pendingTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        JTable pendingTable = new JTable(pendingTableModel);

        loadPendingBookings();

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));

        JButton approveButton = new JButton("Approve Selected");
        approveButton.addActionListener(e -> handleApproveOrReject(pendingTable, "Approved"));

        JButton rejectButton = new JButton("Reject Selected");
        rejectButton.addActionListener(e -> handleApproveOrReject(pendingTable, "Rejected"));

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadPendingBookings());

        buttonPanel.add(approveButton);
        buttonPanel.add(rejectButton);
        buttonPanel.add(refreshButton);

        panel.add(new JScrollPane(pendingTable), BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Loads only "Pending" bookings into the pending table.
     */
    private void loadPendingBookings() {
        pendingTableModel.setRowCount(0);
        List<Booking> bookings = FileHandler.loadBookings();
        for (Booking b : bookings) {
            if ("Pending".equals(b.getStatus())) {
                pendingTableModel.addRow(new Object[]{
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
    }

    /**
     * Handles Approve or Reject action for the selected pending booking.
     * For approval, performs time-conflict and operating-hours checks.
     *
     * @param table      the pending bookings table
     * @param newStatus  either "Approved" or "Rejected"
     */
    private void handleApproveOrReject(JTable table, String newStatus) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a booking first.",
                "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String bookingId = (String) pendingTableModel.getValueAt(selectedRow, 0);

        // Load all bookings and find the one we're updating
        List<Booking> allBookings = FileHandler.loadBookings();
        Booking targetBooking = null;
        for (Booking b : allBookings) {
            if (b.getBookingId().equals(bookingId)) {
                targetBooking = b;
                break;
            }
        }

        if (targetBooking == null) {
            JOptionPane.showMessageDialog(this, "Booking not found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // ---- Extra validation only when APPROVING ----
        if ("Approved".equals(newStatus)) {
            // 1. Check operating hours (8:00 – 18:00)
            int startHour = parseHour(targetBooking.getStartTime());
            int endHour   = parseHour(targetBooking.getEndTime());

            if (startHour < 8 || endHour > 18 || startHour >= endHour) {
                JOptionPane.showMessageDialog(this,
                    "Cannot approve: booking must be within operating hours (8:00 AM – 6:00 PM).",
                    "Time Restriction", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // 2. Check for time conflicts with already-approved bookings for the same hall on the same date
            if (hasTimeConflict(targetBooking, allBookings)) {
                JOptionPane.showMessageDialog(this,
                    "Cannot approve: there is a time conflict with another approved booking\n" +
                    "for the same hall on the same date.",
                    "Time Conflict", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        // Confirm the action with the scheduler
        String message = "Are you sure you want to " + newStatus.toLowerCase() +
                         " booking " + bookingId + "?";
        int confirm = JOptionPane.showConfirmDialog(this, message,
            "Confirm " + newStatus, JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) return;

        // Update the booking status and save back to file
        targetBooking.setStatus(newStatus);
        FileHandler.saveAllBookings(allBookings);

        // Refresh both tabs
        loadPendingBookings();
        loadAllBookings();

        JOptionPane.showMessageDialog(this,
            "Booking " + bookingId + " has been " + newStatus.toLowerCase() + ".",
            "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Parses an hour from a time string like "09:00" → 9.
     * Returns -1 if parsing fails.
     */
    private int parseHour(String time) {
        try {
            return Integer.parseInt(time.split(":")[0]);
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * Checks if the target booking would conflict with any already-approved booking
     * for the same hall on the same date.
     *
     * Two bookings conflict if they overlap in time:
     *   bookingA.start < bookingB.end  AND  bookingA.end > bookingB.start
     *
     * @return true if there is a conflict, false if safe to approve
     */
    private boolean hasTimeConflict(Booking target, List<Booking> allBookings) {
        int targetStart = parseHour(target.getStartTime());
        int targetEnd   = parseHour(target.getEndTime());

        for (Booking b : allBookings) {
            // Only check approved bookings for the same hall on the same date
            if (!"Approved".equals(b.getStatus())) continue;
            if (!b.getHallName().equals(target.getHallName())) continue;
            if (!b.getDate().equals(target.getDate())) continue;
            if (b.getBookingId().equals(target.getBookingId())) continue; // Skip itself

            int existingStart = parseHour(b.getStartTime());
            int existingEnd   = parseHour(b.getEndTime());

            // Check if the time ranges overlap using the standard interval overlap formula:
            // Two intervals [A_start, A_end) and [B_start, B_end) overlap when:
            //   A_start < B_end  AND  A_end > B_start
            if (targetStart < existingEnd && targetEnd > existingStart) {
                return true; // Conflict found
            }
        }
        return false; // No conflict
    }

    // ====================================================================
    //  TAB 2: All Bookings
    // ====================================================================

    /**
     * Builds the "All Bookings" tab showing every booking.
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
        refreshButton.addActionListener(e -> loadAllBookings());

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottomPanel.add(refreshButton);

        panel.add(new JScrollPane(allTable), BorderLayout.CENTER);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Loads all bookings into the "All Bookings" table.
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
    //  LOGOUT
    // ====================================================================

    private void handleLogout() {
        dispose();
        new LoginFrame();
    }
}
