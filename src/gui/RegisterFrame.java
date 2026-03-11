package gui;

import model.Customer;
import model.User;
import util.FileHandler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

/**
 * RegisterFrame.java - Registration form for new customers.
 * New users fill in their details and are registered with the "Customer" role.
 * After registration they are redirected back to the login screen.
 */
public class RegisterFrame extends JFrame {

    // Reference to the login frame so we can show it again after registration
    private LoginFrame loginFrame;

    // Input fields
    private JTextField fullNameField;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JTextField emailField;
    private JTextField phoneField;

    // Buttons
    private JButton registerButton;
    private JButton cancelButton;

    /**
     * Constructor - sets up the registration form.
     *
     * @param loginFrame  the LoginFrame to show again after closing this form
     */
    public RegisterFrame(LoginFrame loginFrame) {
        this.loginFrame = loginFrame;

        setTitle("Register - New Customer");
        setSize(450, 380);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null); // Center on screen
        setResizable(false);

        // Main panel with padding
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        // Title label
        JLabel titleLabel = new JLabel("Customer Registration", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // Form panel with labels and fields
        JPanel formPanel = new JPanel(new GridLayout(6, 2, 10, 8));

        formPanel.add(new JLabel("Full Name:"));
        fullNameField = new JTextField();
        formPanel.add(fullNameField);

        formPanel.add(new JLabel("Username:"));
        usernameField = new JTextField();
        formPanel.add(usernameField);

        formPanel.add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        formPanel.add(passwordField);

        formPanel.add(new JLabel("Confirm Password:"));
        confirmPasswordField = new JPasswordField();
        formPanel.add(confirmPasswordField);

        formPanel.add(new JLabel("Email:"));
        emailField = new JTextField();
        formPanel.add(emailField);

        formPanel.add(new JLabel("Phone:"));
        phoneField = new JTextField();
        formPanel.add(phoneField);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));

        registerButton = new JButton("Register");
        registerButton.setPreferredSize(new Dimension(100, 30));
        registerButton.addActionListener(this::handleRegister);

        cancelButton = new JButton("Cancel");
        cancelButton.setPreferredSize(new Dimension(100, 30));
        cancelButton.addActionListener(e -> handleCancel());

        buttonPanel.add(registerButton);
        buttonPanel.add(cancelButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);
        setVisible(true);
    }

    /**
     * Handles the Register button click.
     * Validates all input fields and saves the new customer to users.txt.
     */
    private void handleRegister(ActionEvent e) {
        // Read all field values
        String fullName        = fullNameField.getText().trim();
        String username        = usernameField.getText().trim();
        String password        = new String(passwordField.getPassword()).trim();
        String confirmPassword = new String(confirmPasswordField.getPassword()).trim();
        String email           = emailField.getText().trim();
        String phone           = phoneField.getText().trim();

        // Validate that required fields are filled in
        if (fullName.isEmpty() || username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Full Name, Username, and Password are required.",
                "Missing Fields", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Validate that passwords match
        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this,
                "Passwords do not match. Please try again.",
                "Password Mismatch", JOptionPane.WARNING_MESSAGE);
            confirmPasswordField.setText("");
            return;
        }

        // Check that the username is not already taken
        List<User> existingUsers = FileHandler.loadUsers();
        for (User user : existingUsers) {
            if (user.getUsername().equalsIgnoreCase(username)) {
                JOptionPane.showMessageDialog(this,
                    "Username '" + username + "' is already taken. Please choose another.",
                    "Username Taken", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        // All validations passed — create and save the new customer
        Customer newCustomer = new Customer(username, password, fullName, email, phone);
        FileHandler.saveUser(newCustomer);

        JOptionPane.showMessageDialog(this,
            "Registration successful! You can now log in with your credentials.",
            "Registration Successful", JOptionPane.INFORMATION_MESSAGE);

        // Close registration form and show login screen again
        dispose();
        loginFrame.setVisible(true);
    }

    /**
     * Handles the Cancel button — closes this form and shows the login screen again.
     */
    private void handleCancel() {
        dispose();
        loginFrame.setVisible(true);
    }
}
