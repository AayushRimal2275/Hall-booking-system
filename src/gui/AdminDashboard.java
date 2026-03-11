package gui;

import model.*;
import util.FileHandler;
import util.HallData;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * AdminDashboard.java - The dashboard for Administrator users.
 *
 * Features:
 *   Tab 1 - Manage Users: view all users, add a new user, delete a user
 *   Tab 2 - View Halls:   view the 3 available hall types
 *   Tab 3 - All Bookings: view every booking in the system
 */
public class AdminDashboard extends JFrame {

    // The currently logged-in admin user
    private User currentUser;

    // Table models — these hold the data displayed in each JTable
    private DefaultTableModel userTableModel;
    private DefaultTableModel hallTableModel;
    private DefaultTableModel bookingTableModel;

    /**
     * Constructor — builds the Admin Dashboard window.
     *
     * @param user  the logged-in admin User object
     */
    public AdminDashboard(User user) {
        this.currentUser = user;

        setTitle("Admin Dashboard - Welcome, " + user.getFullName());
        setSize(850, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // ---- Top panel: welcome message and logout button ----
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        JLabel welcomeLabel = new JLabel("Administrator Dashboard  |  Logged in as: " + user.getFullName());
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 13));
        topPanel.add(welcomeLabel, BorderLayout.WEST);

        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> handleLogout());
        topPanel.add(logoutButton, BorderLayout.EAST);

        // ---- Tabbed pane with 3 tabs ----
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Manage Users",   buildManageUsersPanel());
        tabbedPane.addTab("View Halls",     buildViewHallsPanel());
        tabbedPane.addTab("All Bookings",   buildAllBookingsPanel());

        // ---- Assemble main frame ----
        setLayout(new BorderLayout());
        add(topPanel, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);

        setVisible(true);
    }

    // ====================================================================
    //  TAB 1: Manage Users
    // ====================================================================

    /**
     * Builds the "Manage Users" tab panel.
     * Shows a table of all users with Add and Delete buttons.
     */
    private JPanel buildManageUsersPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Table columns
        String[] columns = {"Username", "Role", "Full Name", "Email", "Phone", "Employee ID"};
        userTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; } // Read-only
        };

        JTable userTable = new JTable(userTableModel);
        JScrollPane scrollPane = new JScrollPane(userTable);

        // Load user data into the table
        loadUsersIntoTable();

        // Button panel below the table
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));

        JButton addUserButton = new JButton("Add User");
        addUserButton.addActionListener(e -> showAddUserDialog());

        JButton deleteUserButton = new JButton("Delete Selected User");
        deleteUserButton.addActionListener(e -> deleteSelectedUser(userTable));

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadUsersIntoTable());

        buttonPanel.add(addUserButton);
        buttonPanel.add(deleteUserButton);
        buttonPanel.add(refreshButton);

        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Loads all users from users.txt and populates the users table.
     */
    private void loadUsersIntoTable() {
        userTableModel.setRowCount(0); // Clear existing rows

        List<User> users = FileHandler.loadUsers();
        for (User user : users) {
            String email      = "";
            String phone      = "";
            String employeeId = "";

            // Extract extra fields from subclasses
            if (user instanceof Customer) {
                email = ((Customer) user).getEmail();
                phone = ((Customer) user).getPhone();
            } else if (user instanceof Scheduler) {
                employeeId = ((Scheduler) user).getEmployeeId();
            }

            userTableModel.addRow(new Object[]{
                user.getUsername(),
                user.getRole(),
                user.getFullName(),
                email,
                phone,
                employeeId
            });
        }
    }

    /**
     * Shows a dialog to add a new user.
     * Admin can set username, password, full name, and role.
     */
    private void showAddUserDialog() {
        JPanel formPanel = new JPanel(new GridLayout(5, 2, 8, 8));

        JTextField usernameField  = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JTextField fullNameField  = new JTextField();
        JTextField extraField     = new JTextField(); // email/phone/employeeId depending on role
        String[] roles = {"Admin", "Scheduler", "Customer", "Manager"};
        JComboBox<String> roleCombo = new JComboBox<>(roles);

        formPanel.add(new JLabel("Username:"));
        formPanel.add(usernameField);
        formPanel.add(new JLabel("Password:"));
        formPanel.add(passwordField);
        formPanel.add(new JLabel("Full Name:"));
        formPanel.add(fullNameField);
        formPanel.add(new JLabel("Role:"));
        formPanel.add(roleCombo);
        formPanel.add(new JLabel("Employee ID (Scheduler only):"));
        formPanel.add(extraField);

        int result = JOptionPane.showConfirmDialog(this, formPanel,
            "Add New User", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String username  = usernameField.getText().trim();
            String password  = new String(passwordField.getPassword()).trim();
            String fullName  = fullNameField.getText().trim();
            String role      = (String) roleCombo.getSelectedItem();
            String extra     = extraField.getText().trim();

            if (username.isEmpty() || password.isEmpty() || fullName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Username, password, and full name are required.",
                    "Missing Fields", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Check for duplicate username
            List<User> users = FileHandler.loadUsers();
            for (User u : users) {
                if (u.getUsername().equalsIgnoreCase(username)) {
                    JOptionPane.showMessageDialog(this, "Username already exists.",
                        "Duplicate Username", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }

            // Create the correct type of user
            User newUser;
            if ("Scheduler".equals(role)) {
                newUser = new Scheduler(username, password, fullName, extra);
            } else if ("Customer".equals(role)) {
                newUser = new Customer(username, password, fullName, "", "");
            } else {
                newUser = new User(username, password, role, fullName);
            }

            FileHandler.saveUser(newUser);
            loadUsersIntoTable(); // Refresh table
            JOptionPane.showMessageDialog(this, "User '" + username + "' added successfully.",
                "Success", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Deletes the selected user from the users table and from users.txt.
     * Cannot delete the currently logged-in admin.
     */
    private void deleteSelectedUser(JTable table) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a user to delete.",
                "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String usernameToDelete = (String) userTableModel.getValueAt(selectedRow, 0);

        // Prevent deleting yourself
        if (usernameToDelete.equals(currentUser.getUsername())) {
            JOptionPane.showMessageDialog(this, "You cannot delete your own account.",
                "Action Denied", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete user '" + usernameToDelete + "'?",
            "Confirm Delete", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            // Remove user from list and save back to file
            List<User> users = FileHandler.loadUsers();
            users.removeIf(u -> u.getUsername().equals(usernameToDelete));
            FileHandler.saveAllUsers(users);

            loadUsersIntoTable(); // Refresh table
            JOptionPane.showMessageDialog(this, "User deleted successfully.",
                "Deleted", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // ====================================================================
    //  TAB 2: View Halls
    // ====================================================================

    /**
     * Builds the "View Halls" tab panel.
     * Shows the 3 available hall types with their details.
     */
    private JPanel buildViewHallsPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] columns = {"Hall ID", "Hall Name", "Type", "Capacity (seats)", "Price Per Hour (RM)"};
        hallTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        // Add hall data to the table
        for (Hall hall : HallData.getHalls()) {
            hallTableModel.addRow(new Object[]{
                hall.getHallId(),
                hall.getHallName(),
                hall.getHallType(),
                hall.getCapacity(),
                "RM " + hall.getPricePerHour()
            });
        }

        JTable hallTable = new JTable(hallTableModel);
        panel.add(new JScrollPane(hallTable), BorderLayout.CENTER);

        return panel;
    }

    // ====================================================================
    //  TAB 3: All Bookings
    // ====================================================================

    /**
     * Builds the "All Bookings" tab panel.
     * Shows every booking in the system.
     */
    private JPanel buildAllBookingsPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] columns = {"Booking ID", "Customer", "Hall", "Date", "Start", "End", "Total (RM)", "Status"};
        bookingTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        JTable bookingTable = new JTable(bookingTableModel);

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadBookingsIntoTable());

        loadBookingsIntoTable();

        panel.add(new JScrollPane(bookingTable), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottomPanel.add(refreshButton);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Loads all bookings from bookings.txt and populates the bookings table.
     */
    private void loadBookingsIntoTable() {
        bookingTableModel.setRowCount(0);

        List<Booking> bookings = FileHandler.loadBookings();
        for (Booking b : bookings) {
            bookingTableModel.addRow(new Object[]{
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

    /**
     * Logs out the admin and returns to the login screen.
     */
    private void handleLogout() {
        dispose();
        new LoginFrame();
    }
}
