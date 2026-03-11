package gui;

import model.Booking;
import model.Hall;
import model.User;
import util.FileHandler;
import util.HallData;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * CustomerDashboard.java - Dashboard for Customer users.
 *
 * Features:
 *   Tab 1 - Book a Hall:   select hall type, date, time slot; submit booking (status: Pending)
 *   Tab 2 - My Bookings:   view own bookings, cancel a pending booking
 */
public class CustomerDashboard extends JFrame {

    // Date format used throughout this class for booking dates
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // The logged-in customer
    private User currentUser;

    // Booking form components
    private JComboBox<String> hallCombo;
    private JTextField dateField;
    private JComboBox<String> startTimeCombo;
    private JComboBox<String> endTimeCombo;
    private JLabel priceLabel;

    // My Bookings table
    private DefaultTableModel myBookingsTableModel;

    /**
     * Constructor — builds the Customer Dashboard window.
     *
     * @param user  the logged-in Customer User object
     */
    public CustomerDashboard(User user) {
        this.currentUser = user;

        setTitle("Customer Dashboard - Welcome, " + user.getFullName());
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Top bar: welcome message + logout
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        JLabel welcomeLabel = new JLabel("Customer Dashboard  |  Logged in as: " + user.getFullName());
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 13));
        topPanel.add(welcomeLabel, BorderLayout.WEST);

        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> handleLogout());
        topPanel.add(logoutButton, BorderLayout.EAST);

        // Tabbed pane
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Book a Hall",   buildBookingPanel());
        tabbedPane.addTab("My Bookings",   buildMyBookingsPanel());

        setLayout(new BorderLayout());
        add(topPanel, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);

        setVisible(true);
    }

    // ====================================================================
    //  TAB 1: Book a Hall
    // ====================================================================

    /**
     * Builds the "Book a Hall" tab with a form for making a new booking.
     */
    private JPanel buildBookingPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        // Title
        JLabel titleLabel = new JLabel("Make a New Hall Booking", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 15));
        panel.add(titleLabel, BorderLayout.NORTH);

        // Form grid
        JPanel formPanel = new JPanel(new GridLayout(6, 2, 10, 10));

        // Hall type dropdown
        formPanel.add(new JLabel("Select Hall:"));
        hallCombo = new JComboBox<>(HallData.getHallNames());
        hallCombo.addActionListener(e -> updatePriceDisplay());
        formPanel.add(hallCombo);

        // Date field
        formPanel.add(new JLabel("Date (YYYY-MM-DD):"));
        dateField = new JTextField(LocalDate.now().format(DATE_FORMATTER));
        formPanel.add(dateField);

        // Start time dropdown (8:00 to 17:00 — last possible start for 1-hour minimum)
        formPanel.add(new JLabel("Start Time:"));
        startTimeCombo = new JComboBox<>(buildTimeOptions(8, 17));
        startTimeCombo.addActionListener(e -> updatePriceDisplay());
        formPanel.add(startTimeCombo);

        // End time dropdown (9:00 to 18:00)
        formPanel.add(new JLabel("End Time:"));
        endTimeCombo = new JComboBox<>(buildTimeOptions(9, 18));
        // Set default end time to one hour after start
        endTimeCombo.setSelectedIndex(1);
        endTimeCombo.addActionListener(e -> updatePriceDisplay());
        formPanel.add(endTimeCombo);

        // Calculated price display
        formPanel.add(new JLabel("Estimated Price:"));
        priceLabel = new JLabel("RM 0.00");
        priceLabel.setFont(new Font("Arial", Font.BOLD, 13));
        formPanel.add(priceLabel);

        // Empty row for spacing
        formPanel.add(new JLabel(""));

        // Book Now button
        JButton bookButton = new JButton("Book Now");
        bookButton.addActionListener(e -> handleBookNow());
        formPanel.add(bookButton);

        panel.add(formPanel, BorderLayout.CENTER);

        // Initialize price label
        updatePriceDisplay();

        // Hall information panel
        JPanel infoPanel = buildHallInfoPanel();
        panel.add(infoPanel, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Creates an array of time strings from startHour to endHour (inclusive), e.g.:
     * buildTimeOptions(8, 10) → ["08:00", "09:00", "10:00"]
     */
    private String[] buildTimeOptions(int startHour, int endHour) {
        int count = endHour - startHour + 1;
        String[] times = new String[count];
        for (int i = 0; i < count; i++) {
            times[i] = String.format("%02d:00", startHour + i);
        }
        return times;
    }

    /**
     * Updates the price label based on selected hall, start time, and end time.
     */
    private void updatePriceDisplay() {
        try {
            String hallName  = (String) hallCombo.getSelectedItem();
            String startStr  = (String) startTimeCombo.getSelectedItem();
            String endStr    = (String) endTimeCombo.getSelectedItem();

            if (hallName == null || startStr == null || endStr == null) return;

            int startHour = Integer.parseInt(startStr.split(":")[0]);
            int endHour   = Integer.parseInt(endStr.split(":")[0]);
            int hours     = endHour - startHour;

            Hall hall = HallData.getHallByName(hallName);
            if (hall == null || hours <= 0) {
                priceLabel.setText("RM 0.00 (invalid time range)");
                return;
            }

            double totalPrice = hours * hall.getPricePerHour();
            priceLabel.setText(String.format("RM %.2f  (%d hour(s) × RM%.0f/hr)",
                                             totalPrice, hours, hall.getPricePerHour()));
        } catch (Exception ex) {
            priceLabel.setText("RM 0.00");
        }
    }

    /**
     * Builds a small info panel showing hall capacities and prices.
     */
    private JPanel buildHallInfoPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Hall Information"));

        for (Hall hall : HallData.getHalls()) {
            JLabel info = new JLabel(hall.getHallName() + ": " +
                hall.getCapacity() + " seats | RM" + (int)hall.getPricePerHour() + "/hr");
            info.setFont(new Font("Arial", Font.PLAIN, 11));
            panel.add(info);
        }
        return panel;
    }

    /**
     * Handles the "Book Now" button click.
     * Validates input, calculates price, generates a booking ID, and saves to file.
     */
    private void handleBookNow() {
        String hallName  = (String) hallCombo.getSelectedItem();
        String dateStr   = dateField.getText().trim();
        String startStr  = (String) startTimeCombo.getSelectedItem();
        String endStr    = (String) endTimeCombo.getSelectedItem();

        // ---- Validate date format ----
        if (dateStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a date.", "Missing Date", JOptionPane.WARNING_MESSAGE);
            return;
        }

        LocalDate bookingDate;
        try {
            bookingDate = LocalDate.parse(dateStr, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this,
                "Invalid date format. Please use YYYY-MM-DD (e.g. 2026-03-15).",
                "Invalid Date", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Booking must not be in the past
        if (bookingDate.isBefore(LocalDate.now())) {
            JOptionPane.showMessageDialog(this,
                "Booking date cannot be in the past.",
                "Invalid Date", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // ---- Validate time range ----
        int startHour = Integer.parseInt(startStr.split(":")[0]);
        int endHour   = Integer.parseInt(endStr.split(":")[0]);

        if (endHour <= startHour) {
            JOptionPane.showMessageDialog(this,
                "End time must be after start time.",
                "Invalid Time", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // ---- Calculate total price ----
        Hall hall = HallData.getHallByName(hallName);
        if (hall == null) {
            JOptionPane.showMessageDialog(this, "Hall not found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int hours = endHour - startHour;
        double totalPrice = hours * hall.getPricePerHour();

        // ---- Generate a unique booking ID ----
        String bookingId = "B" + System.currentTimeMillis();

        // ---- Create and save the booking ----
        Booking newBooking = new Booking(
            bookingId,
            currentUser.getUsername(),
            hallName,
            dateStr,
            startStr,
            endStr,
            totalPrice,
            "Pending"   // All new bookings start as "Pending"
        );

        FileHandler.saveBooking(newBooking);

        // Refresh "My Bookings" table
        loadMyBookings();

        JOptionPane.showMessageDialog(this,
            "Booking submitted successfully!\n\n" +
            "Hall: " + hallName + "\n" +
            "Date: " + dateStr + "\n" +
            "Time: " + startStr + " – " + endStr + "\n" +
            "Total: RM " + String.format("%.2f", totalPrice) + "\n" +
            "Status: Pending (awaiting scheduler approval)",
            "Booking Confirmed", JOptionPane.INFORMATION_MESSAGE);
    }

    // ====================================================================
    //  TAB 2: My Bookings
    // ====================================================================

    /**
     * Builds the "My Bookings" tab showing the customer's own bookings.
     */
    private JPanel buildMyBookingsPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] columns = {"Booking ID", "Hall", "Date", "Start", "End", "Total (RM)", "Status"};
        myBookingsTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        JTable myTable = new JTable(myBookingsTableModel);
        loadMyBookings();

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));

        JButton cancelButton = new JButton("Cancel Selected Booking");
        cancelButton.addActionListener(e -> handleCancelBooking(myTable));

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadMyBookings());

        buttonPanel.add(cancelButton);
        buttonPanel.add(refreshButton);

        // Note about cancellation
        JLabel noteLabel = new JLabel("Note: Only 'Pending' bookings can be cancelled.");
        noteLabel.setFont(new Font("Arial", Font.ITALIC, 11));
        noteLabel.setForeground(Color.GRAY);
        buttonPanel.add(noteLabel);

        panel.add(new JScrollPane(myTable), BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Loads only this customer's bookings into the "My Bookings" table.
     */
    private void loadMyBookings() {
        myBookingsTableModel.setRowCount(0);
        List<Booking> allBookings = FileHandler.loadBookings();
        for (Booking b : allBookings) {
            if (b.getCustomerUsername().equals(currentUser.getUsername())) {
                myBookingsTableModel.addRow(new Object[]{
                    b.getBookingId(),
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
     * Handles the "Cancel Selected Booking" button click.
     * Only allows cancelling bookings with "Pending" status.
     */
    private void handleCancelBooking(JTable table) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a booking to cancel.",
                "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String bookingId = (String) myBookingsTableModel.getValueAt(selectedRow, 0);
        String status    = (String) myBookingsTableModel.getValueAt(selectedRow, 6);

        // Only pending bookings can be cancelled
        if (!"Pending".equals(status)) {
            JOptionPane.showMessageDialog(this,
                "Only 'Pending' bookings can be cancelled.\n" +
                "This booking has status: " + status,
                "Cannot Cancel", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to cancel booking " + bookingId + "?",
            "Confirm Cancellation", JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) return;

        // Find the booking and mark it as Cancelled
        List<Booking> allBookings = FileHandler.loadBookings();
        for (Booking b : allBookings) {
            if (b.getBookingId().equals(bookingId) &&
                b.getCustomerUsername().equals(currentUser.getUsername())) {
                b.setStatus("Cancelled");
                break;
            }
        }

        FileHandler.saveAllBookings(allBookings);
        loadMyBookings(); // Refresh table

        JOptionPane.showMessageDialog(this, "Booking " + bookingId + " has been cancelled.",
            "Cancelled", JOptionPane.INFORMATION_MESSAGE);
    }

    // ====================================================================
    //  LOGOUT
    // ====================================================================

    private void handleLogout() {
        dispose();
        new LoginFrame();
    }
}
