package gui;

import model.User;
import util.FileHandler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

/**
 * LoginFrame.java - The main login screen of the Hall Booking Management System.
 * Users enter their username and password to access their role-specific dashboard.
 * New customers can open the registration screen from here.
 */
public class LoginFrame extends JFrame {

    // Input fields
    private JTextField usernameField;
    private JPasswordField passwordField;

    // Buttons
    private JButton loginButton;
    private JButton registerButton;

    /**
     * Constructor - sets up the login window layout and components.
     */
    public LoginFrame() {
        // Set window title and basic properties
        setTitle("Hall Booking Management System - Login");
        setSize(420, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center on screen
        setResizable(false);

        // ---- Main panel with padding ----
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        // ---- Title label at the top ----
        JLabel titleLabel = new JLabel("Hall Booking Management System", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // ---- Form panel in the center ----
        JPanel formPanel = new JPanel(new GridLayout(2, 2, 10, 10));

        formPanel.add(new JLabel("Username:"));
        usernameField = new JTextField();
        formPanel.add(usernameField);

        formPanel.add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        formPanel.add(passwordField);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // ---- Button panel at the bottom ----
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));

        loginButton = new JButton("Login");
        loginButton.setPreferredSize(new Dimension(100, 30));
        loginButton.addActionListener(this::handleLogin);

        registerButton = new JButton("Register");
        registerButton.setPreferredSize(new Dimension(100, 30));
        registerButton.addActionListener(e -> openRegisterFrame());

        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Allow pressing Enter to trigger login
        getRootPane().setDefaultButton(loginButton);

        add(mainPanel);
        setVisible(true);
    }

    /**
     * Handles the Login button click.
     * Reads the username and password, validates against users.txt,
     * and opens the appropriate dashboard for the user's role.
     */
    private void handleLogin(ActionEvent e) {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        // Check that fields are not empty
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please enter both username and password.",
                "Missing Fields", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Load all users from file and search for matching credentials
        List<User> users = FileHandler.loadUsers();
        User loggedInUser = null;

        for (User user : users) {
            if (user.getUsername().equals(username) && user.getPassword().equals(password)) {
                loggedInUser = user;
                break;
            }
        }

        // If no matching user found, show an error
        if (loggedInUser == null) {
            JOptionPane.showMessageDialog(this,
                "Invalid username or password. Please try again.",
                "Login Failed", JOptionPane.ERROR_MESSAGE);
            passwordField.setText(""); // Clear password field for security
            return;
        }

        // Login successful — open the correct dashboard based on role
        JOptionPane.showMessageDialog(this,
            "Welcome, " + loggedInUser.getFullName() + "!\nRole: " + loggedInUser.getRole(),
            "Login Successful", JOptionPane.INFORMATION_MESSAGE);

        dispose(); // Close login window

        // Open the appropriate dashboard
        switch (loggedInUser.getRole()) {
            case "Admin":
                new AdminDashboard(loggedInUser);
                break;
            case "Scheduler":
                new SchedulerDashboard(loggedInUser);
                break;
            case "Customer":
                new CustomerDashboard(loggedInUser);
                break;
            case "Manager":
                new ManagerDashboard(loggedInUser);
                break;
            default:
                // Unknown role — show error and reopen login
                JOptionPane.showMessageDialog(null,
                    "Unknown role: " + loggedInUser.getRole(),
                    "Error", JOptionPane.ERROR_MESSAGE);
                new LoginFrame();
        }
    }

    /**
     * Opens the customer registration form.
     */
    private void openRegisterFrame() {
        new RegisterFrame(this);
        setVisible(false); // Hide login while registering
    }
}
