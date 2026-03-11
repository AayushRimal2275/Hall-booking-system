package model;

/**
 * Booking.java - Represents a hall booking record.
 * Stores all details about a booking: who booked, which hall, when, and status.
 *
 * Booking statuses:
 *   "Pending"  - submitted by customer, awaiting scheduler approval
 *   "Approved" - approved by scheduler
 *   "Rejected" - rejected by scheduler
 *   "Cancelled" - cancelled by customer
 */
public class Booking {
    private String bookingId;
    private String customerUsername;
    private String hallName;
    private String date;        // Format: YYYY-MM-DD
    private String startTime;   // Format: HH:00  (e.g. "09:00")
    private String endTime;     // Format: HH:00  (e.g. "12:00")
    private double totalPrice;
    private String status;

    // Constructor to create a Booking with all required fields
    public Booking(String bookingId, String customerUsername, String hallName,
                   String date, String startTime, String endTime,
                   double totalPrice, String status) {
        this.bookingId = bookingId;
        this.customerUsername = customerUsername;
        this.hallName = hallName;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.totalPrice = totalPrice;
        this.status = status;
    }

    // --- Getters ---
    public String getBookingId()          { return bookingId; }
    public String getCustomerUsername()   { return customerUsername; }
    public String getHallName()           { return hallName; }
    public String getDate()               { return date; }
    public String getStartTime()          { return startTime; }
    public String getEndTime()            { return endTime; }
    public double getTotalPrice()         { return totalPrice; }
    public String getStatus()             { return status; }

    // --- Setters ---
    public void setBookingId(String bookingId)               { this.bookingId = bookingId; }
    public void setCustomerUsername(String customerUsername) { this.customerUsername = customerUsername; }
    public void setHallName(String hallName)                 { this.hallName = hallName; }
    public void setDate(String date)                         { this.date = date; }
    public void setStartTime(String startTime)               { this.startTime = startTime; }
    public void setEndTime(String endTime)                   { this.endTime = endTime; }
    public void setTotalPrice(double totalPrice)             { this.totalPrice = totalPrice; }
    public void setStatus(String status)                     { this.status = status; }

    /**
     * Converts this Booking to a pipe-delimited string for saving to bookings.txt.
     * Format: bookingId|customerUsername|hallName|date|startTime|endTime|totalPrice|status
     */
    public String toFileString() {
        return bookingId + "|" + customerUsername + "|" + hallName + "|" +
               date + "|" + startTime + "|" + endTime + "|" + totalPrice + "|" + status;
    }

    @Override
    public String toString() {
        return "Booking{id='" + bookingId + "', hall='" + hallName + "', date='" + date +
               "', " + startTime + "-" + endTime + ", status='" + status + "'}";
    }
}
