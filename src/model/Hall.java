package model;

/**
 * Hall.java - Represents a hall available for booking.
 * Stores the hall's ID, name, type, seating capacity, and hourly price.
 */
public class Hall {
    private String hallId;
    private String hallName;
    private String hallType;
    private int capacity;
    private double pricePerHour;

    // Constructor to create a Hall with all required information
    public Hall(String hallId, String hallName, String hallType, int capacity, double pricePerHour) {
        this.hallId = hallId;
        this.hallName = hallName;
        this.hallType = hallType;
        this.capacity = capacity;
        this.pricePerHour = pricePerHour;
    }

    // --- Getters ---
    public String getHallId()        { return hallId; }
    public String getHallName()      { return hallName; }
    public String getHallType()      { return hallType; }
    public int getCapacity()         { return capacity; }
    public double getPricePerHour()  { return pricePerHour; }

    // --- Setters ---
    public void setHallId(String hallId)               { this.hallId = hallId; }
    public void setHallName(String hallName)           { this.hallName = hallName; }
    public void setHallType(String hallType)           { this.hallType = hallType; }
    public void setCapacity(int capacity)              { this.capacity = capacity; }
    public void setPricePerHour(double pricePerHour)   { this.pricePerHour = pricePerHour; }

    @Override
    public String toString() {
        return hallName + " (" + hallType + ") - " + capacity + " seats - RM" + pricePerHour + "/hour";
    }
}
