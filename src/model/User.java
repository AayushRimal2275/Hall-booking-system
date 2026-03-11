package model;

/**
 * User.java - Base class for all users in the Hall Booking System.
 * All user types (Admin, Scheduler, Customer, Manager) extend this class.
 */
public class User {
    // Basic fields shared by all user types
    protected String username;
    protected String password;
    protected String role;
    protected String fullName;

    // Constructor to create a new User with all required fields
    public User(String username, String password, String role, String fullName) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.fullName = fullName;
    }

    // --- Getters ---
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getRole()     { return role; }
    public String getFullName() { return fullName; }

    // --- Setters ---
    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
    public void setRole(String role)         { this.role = role; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    /**
     * Converts this User to a pipe-delimited string for saving to users.txt.
     * Format: username|password|role|fullName|email|phone|employeeId
     * Admin and Manager leave email, phone, employeeId blank.
     */
    public String toFileString() {
        return username + "|" + password + "|" + role + "|" + fullName + "|||";
    }

    // Returns a readable string representation of this user
    @Override
    public String toString() {
        return "User{username='" + username + "', role='" + role + "', fullName='" + fullName + "'}";
    }
}
