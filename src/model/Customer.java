package model;

/**
 * Customer.java - Extends User with email and phone fields.
 * Customers can book halls, view their bookings, and cancel pending bookings.
 */
public class Customer extends User {
    private String email;
    private String phone;

    // Constructor for creating a Customer with all fields
    public Customer(String username, String password, String fullName, String email, String phone) {
        // Call the parent User constructor with role set to "Customer"
        super(username, password, "Customer", fullName);
        this.email = email;
        this.phone = phone;
    }

    // --- Getters ---
    public String getEmail() { return email; }
    public String getPhone() { return phone; }

    // --- Setters ---
    public void setEmail(String email) { this.email = email; }
    public void setPhone(String phone) { this.phone = phone; }

    /**
     * Converts this Customer to a pipe-delimited string for saving to users.txt.
     * Format: username|password|role|fullName|email|phone|employeeId
     */
    @Override
    public String toFileString() {
        return username + "|" + password + "|" + role + "|" + fullName + "|" + email + "|" + phone + "|";
    }

    @Override
    public String toString() {
        return "Customer{username='" + username + "', email='" + email + "', phone='" + phone + "'}";
    }
}
