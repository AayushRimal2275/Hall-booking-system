package util;

import model.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * FileHandler.java - Utility class for reading and writing text files.
 * All methods are static so they can be called without creating an instance.
 *
 * File locations:
 *   data/users.txt    - stores user accounts
 *   data/bookings.txt - stores booking records
 *
 * File format uses pipe "|" as delimiter.
 */
public class FileHandler {

    // File paths for data storage
    private static final String USERS_FILE    = "data/users.txt";
    private static final String BOOKINGS_FILE = "data/bookings.txt";

    /**
     * Ensures the data/ directory exists. Creates it if it doesn't.
     */
    private static void ensureDataDirectoryExists() {
        File dataDir = new File("data");
        if (!dataDir.exists()) {
            dataDir.mkdirs(); // Create the directory (and any parent directories)
        }
    }

    // ======================== USER METHODS ========================

    /**
     * Reads all users from data/users.txt and returns them as a list.
     * Returns an empty list if the file does not exist yet.
     *
     * File format per line:
     *   username|password|role|fullName|email|phone|employeeId
     */
    public static List<User> loadUsers() {
        List<User> users = new ArrayList<>();
        ensureDataDirectoryExists();

        File file = new File(USERS_FILE);
        if (!file.exists()) {
            // File doesn't exist yet — return empty list (no error)
            return users;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                // Skip empty lines and comment lines starting with #
                if (line.isEmpty() || line.startsWith("#")) continue;

                String[] parts = line.split("\\|", -1); // -1 keeps trailing empty strings
                if (parts.length < 4) continue; // Skip malformed lines

                String username   = parts[0];
                String password   = parts[1];
                String role       = parts[2];
                String fullName   = parts[3];
                String email      = parts.length > 4 ? parts[4] : "";
                String phone      = parts.length > 5 ? parts[5] : "";
                String employeeId = parts.length > 6 ? parts[6] : "";

                // Create the correct subclass based on role
                switch (role) {
                    case "Customer":
                        users.add(new Customer(username, password, fullName, email, phone));
                        break;
                    case "Scheduler":
                        users.add(new Scheduler(username, password, fullName, employeeId));
                        break;
                    default:
                        // Admin, Manager — use base User class
                        users.add(new User(username, password, role, fullName));
                        break;
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading users file: " + e.getMessage());
        }

        return users;
    }

    /**
     * Appends a single user to data/users.txt.
     * Used when registering a new customer.
     */
    public static void saveUser(User user) {
        ensureDataDirectoryExists();
        try (PrintWriter writer = new PrintWriter(new FileWriter(USERS_FILE, true))) {
            // append=true so we add to the end of the file
            writer.println(user.toFileString());
        } catch (IOException e) {
            System.err.println("Error saving user: " + e.getMessage());
        }
    }

    /**
     * Overwrites data/users.txt with the provided list of users.
     * Used when deleting a user (admin function).
     */
    public static void saveAllUsers(List<User> users) {
        ensureDataDirectoryExists();
        try (PrintWriter writer = new PrintWriter(new FileWriter(USERS_FILE, false))) {
            // append=false so we overwrite the file
            for (User user : users) {
                writer.println(user.toFileString());
            }
        } catch (IOException e) {
            System.err.println("Error saving all users: " + e.getMessage());
        }
    }

    // ======================== BOOKING METHODS ========================

    /**
     * Reads all bookings from data/bookings.txt and returns them as a list.
     * Returns an empty list if the file does not exist yet.
     *
     * File format per line:
     *   bookingId|customerUsername|hallName|date|startTime|endTime|totalPrice|status
     */
    public static List<Booking> loadBookings() {
        List<Booking> bookings = new ArrayList<>();
        ensureDataDirectoryExists();

        File file = new File(BOOKINGS_FILE);
        if (!file.exists()) {
            return bookings; // No bookings yet
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                // Skip empty lines and comment lines
                if (line.isEmpty() || line.startsWith("#")) continue;

                String[] parts = line.split("\\|", -1);
                if (parts.length < 8) continue; // Skip malformed lines

                String bookingId        = parts[0];
                String customerUsername = parts[1];
                String hallName         = parts[2];
                String date             = parts[3];
                String startTime        = parts[4];
                String endTime          = parts[5];
                double totalPrice       = 0.0;
                try {
                    totalPrice = Double.parseDouble(parts[6]);
                } catch (NumberFormatException e) {
                    // Keep 0.0 if parsing fails
                }
                String status = parts[7];

                bookings.add(new Booking(bookingId, customerUsername, hallName,
                                         date, startTime, endTime, totalPrice, status));
            }
        } catch (IOException e) {
            System.err.println("Error reading bookings file: " + e.getMessage());
        }

        return bookings;
    }

    /**
     * Appends a single booking to data/bookings.txt.
     * Used when a customer creates a new booking.
     */
    public static void saveBooking(Booking booking) {
        ensureDataDirectoryExists();
        try (PrintWriter writer = new PrintWriter(new FileWriter(BOOKINGS_FILE, true))) {
            writer.println(booking.toFileString());
        } catch (IOException e) {
            System.err.println("Error saving booking: " + e.getMessage());
        }
    }

    /**
     * Overwrites data/bookings.txt with the provided list of bookings.
     * Used when approving, rejecting, or cancelling a booking.
     */
    public static void saveAllBookings(List<Booking> bookings) {
        ensureDataDirectoryExists();
        try (PrintWriter writer = new PrintWriter(new FileWriter(BOOKINGS_FILE, false))) {
            for (Booking booking : bookings) {
                writer.println(booking.toFileString());
            }
        } catch (IOException e) {
            System.err.println("Error saving all bookings: " + e.getMessage());
        }
    }
}
