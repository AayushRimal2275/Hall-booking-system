# Hall Booking Management System

A Java Swing GUI application for managing hall bookings at a company that rents halls for events.

---

## Technology

- **Language:** Java (JDK 8 or higher)
- **GUI:** Java Swing (JFrame-based)
- **Data storage:** Text files (`.txt`) — no database required

---

## Project Structure

```
Hall-booking-system/
├── src/
│   ├── Main.java                    # Entry point
│   ├── model/
│   │   ├── User.java                # Base user class
│   │   ├── Customer.java            # extends User (email, phone)
│   │   ├── Scheduler.java           # extends User (employeeId)
│   │   ├── Hall.java                # Hall data model
│   │   └── Booking.java             # Booking data model
│   ├── gui/
│   │   ├── LoginFrame.java          # Login screen
│   │   ├── RegisterFrame.java       # Customer registration
│   │   ├── AdminDashboard.java      # Admin panel
│   │   ├── SchedulerDashboard.java  # Scheduler panel
│   │   ├── CustomerDashboard.java   # Customer panel
│   │   └── ManagerDashboard.java    # Manager panel
│   └── util/
│       ├── FileHandler.java         # Read/write .txt files
│       └── HallData.java            # Hall types & pricing
└── data/
    ├── users.txt                    # User accounts (pipe-delimited)
    └── bookings.txt                 # Booking records (pipe-delimited)
```

---

## How to Compile and Run

### Step 1 — Create the output directory
```bash
mkdir -p out
```

### Step 2 — Compile all Java source files
```bash
javac -d out src/model/*.java src/util/*.java src/gui/*.java src/Main.java
```

### Step 3 — Run the application
```bash
java -cp out Main
```

> **Important:** Run the program from the project root directory (where the `data/` folder is located) so that the application can find the `data/users.txt` and `data/bookings.txt` files.

---

## Default Login Accounts

| Username     | Password   | Role          |
|--------------|------------|---------------|
| `admin`      | `admin123` | Administrator |
| `scheduler1` | `sched123` | Scheduler     |
| `customer1`  | `cust123`  | Customer      |
| `manager1`   | `mgr123`   | Manager       |

---

## Hall Types

| Hall         | Capacity   | Price        |
|--------------|------------|--------------|
| Auditorium   | 1000 seats | RM300 / hour |
| Banquet Hall | 300 seats  | RM100 / hour |
| Meeting Room | 30 seats   | RM50 / hour  |

**Operating hours:** 8:00 AM – 6:00 PM

---

## Features by Role

### Administrator
- View all users in the system
- Add new users (any role)
- Delete existing users
- View all hall types
- View all bookings

### Scheduler
- View pending booking requests
- Approve or reject bookings (with time-conflict checking)
- View all bookings schedule

### Customer
- Register a new account
- Book a hall (select hall type, date, time slot)
- View own bookings
- Cancel pending bookings

### Manager
- View all bookings
- View revenue report (total and by hall type, from approved bookings)

---

## Data File Formats

### `data/users.txt`
```
username|password|role|fullName|email|phone|employeeId
```

### `data/bookings.txt`
```
bookingId|customerUsername|hallName|date|startTime|endTime|totalPrice|status
```

Booking statuses: `Pending`, `Approved`, `Rejected`, `Cancelled`
