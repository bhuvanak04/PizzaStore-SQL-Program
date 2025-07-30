# PizzaStore-SQL-Program

Java‑based command‑line application backed by a PostgreSQL database, designed to simulate a full pizza‑ordering system with role‑based access control, robust auditing, and performance optimizations.

## Key Features

### User Management & Authentication
- **Registration**
  - Unique username
  - Secure password (minimum 6 characters)
  - 10‑digit phone number
- **Login**
  - Credential validation
  - Three‑attempt lockout
- **Profile**
  - View and update favorite items, phone number, password

### Role‑Based Access Control
- **Customers**
  - Browse menu items
  - Place orders
  - View own order history
- **Drivers**
  - View and update order statuses
- **Managers**
  - Add or modify menu items
  - Manage user roles
  - View all orders

### Menu Browsing & Ordering
- Filter and sort menu items by category, price range, or name
- Place orders across multiple stores, with itemized total and order‑history logging
- View detailed order information, recent orders, and full order history

### Database Design & Integrity
- Triggers and stored procedures automatically log every role change, order placement, and status update into dedicated audit tables (`RoleChangeLog`, `OrderLog`, `OrderStatusLog`)
- Indexes on frequently queried columns (e.g. `login`, `orderID`, `price`) to accelerate searches and range queries
- Auto‑incrementing `orderID` via PostgreSQL sequence to enforce uniqueness

### Error Handling & Validation
- Try‑catch blocks guard against database failures and invalid input
- Enforced constraints on phone numbers, passwords, and menu‑item uniqueness
- Friendly user feedback instead of application crashes
