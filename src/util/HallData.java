package util;

import model.Hall;
import java.util.ArrayList;
import java.util.List;

/**
 * HallData.java - Provides static information about available hall types.
 * The system has 3 fixed hall types with set capacities and hourly prices.
 * This data is hardcoded (not stored in a file) because hall types don't change.
 */
public class HallData {

    /**
     * Returns a list of the 3 available hall types.
     * Each Hall object contains: hallId, hallName, hallType, capacity, pricePerHour
     */
    public static List<Hall> getHalls() {
        List<Hall> halls = new ArrayList<>();

        // Hall 1: Auditorium - large venue for big events
        halls.add(new Hall("H001", "Auditorium", "Auditorium", 1000, 300.0));

        // Hall 2: Banquet Hall - medium venue for dinners and receptions
        halls.add(new Hall("H002", "Banquet Hall", "Banquet Hall", 300, 100.0));

        // Hall 3: Meeting Room - small venue for business meetings
        halls.add(new Hall("H003", "Meeting Room", "Meeting Room", 30, 50.0));

        return halls;
    }

    /**
     * Finds a Hall object by its name.
     * Returns null if no hall with that name is found.
     */
    public static Hall getHallByName(String hallName) {
        for (Hall hall : getHalls()) {
            if (hall.getHallName().equalsIgnoreCase(hallName)) {
                return hall;
            }
        }
        return null; // Hall not found
    }

    /**
     * Returns just the hall names as an array.
     * Useful for populating JComboBox dropdowns.
     */
    public static String[] getHallNames() {
        List<Hall> halls = getHalls();
        String[] names = new String[halls.size()];
        for (int i = 0; i < halls.size(); i++) {
            names[i] = halls.get(i).getHallName();
        }
        return names;
    }
}
