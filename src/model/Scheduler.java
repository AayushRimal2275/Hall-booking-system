package model;

/**
 * Scheduler.java - Extends User with an employee ID field.
 * Schedulers can approve or reject hall booking requests.
 */
public class Scheduler extends User {
    private String employeeId;

    // Constructor for creating a Scheduler with all fields
    public Scheduler(String username, String password, String fullName, String employeeId) {
        // Call the parent User constructor with role set to "Scheduler"
        super(username, password, "Scheduler", fullName);
        this.employeeId = employeeId;
    }

    // --- Getter ---
    public String getEmployeeId() { return employeeId; }

    // --- Setter ---
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

    /**
     * Converts this Scheduler to a pipe-delimited string for saving to users.txt.
     * Format: username|password|role|fullName|email|phone|employeeId
     */
    @Override
    public String toFileString() {
        return username + "|" + password + "|" + role + "|" + fullName + "|||" + employeeId;
    }

    @Override
    public String toString() {
        return "Scheduler{username='" + username + "', employeeId='" + employeeId + "'}";
    }
}
