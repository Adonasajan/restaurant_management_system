
// Restaurant Management System - Complete Implementation
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.DriverManager;
import java.sql.Statement;

import java.util.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.math.BigDecimal;
import java.math.RoundingMode;

// Database Connection Ma
public class DatabaseManager {
    private static final String URL = "jdbc:mysql://127.0.0.1:3306/restaurant_db";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "moluz@213";

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }

    public static void initializeDatabase() {
        try (Connection conn = getConnection()) {
            Statement stmt = conn.createStatement();

            // Create users table
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS users (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    username VARCHAR(50) UNIQUE NOT NULL,
                    password VARCHAR(100) NOT NULL,
                    role ENUM('ADMIN', 'STAFF') NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """);

            // Create menu_items table
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS menu_items (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    name VARCHAR(100) NOT NULL,
                    category VARCHAR(50) NOT NULL,
                    price DECIMAL(10,2) NOT NULL,
                    available BOOLEAN DEFAULT TRUE,
                    description TEXT,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """);

            // Create tables table
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS tables (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    table_number INT UNIQUE NOT NULL,
                    capacity INT NOT NULL,
                    status ENUM('AVAILABLE', 'OCCUPIED', 'RESERVED') DEFAULT 'AVAILABLE'
                )
            """);

            // Create orders table
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS orders (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    table_id INT,
                    customer_name VARCHAR(100),
                    status ENUM('PENDING', 'PREPARING', 'COMPLETED', 'CANCELLED') DEFAULT 'PENDING',
                    total_amount DECIMAL(10,2) DEFAULT 0.00,
                    order_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (table_id) REFERENCES tables(id)
                )
            """);

            // Create order_items table
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS order_items (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    order_id INT,
                    menu_item_id INT,
                    quantity INT NOT NULL,
                    price DECIMAL(10,2) NOT NULL,
                    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
                    FOREIGN KEY (menu_item_id) REFERENCES menu_items(id)
                )
            """);

            // Create bills table
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS bills (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    order_id INT UNIQUE,
                    subtotal DECIMAL(10,2) NOT NULL,
                    tax DECIMAL(10,2) NOT NULL,
                    total DECIMAL(10,2) NOT NULL,
                    payment_status ENUM('PENDING', 'PAID') DEFAULT 'PENDING',
                    bill_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (order_id) REFERENCES orders(id)
                )
            """);

            System.out.println("Database initialized successfully!");

        } catch (SQLException e) {
            System.err.println("Error initializing database: " + e.getMessage());
        }
    }
}

// User Model
class User {
    private int id;
    private String username;
    private String password;
    private String role;

    public User(int id, String username, String password, String role) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
    }

    // Getters and setters
    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getRole() { return role; }
}

// MenuItem Model
class MenuItem {
    private int id;
    private String name;
    private String category;
    private BigDecimal price;
    private boolean available;
    private String description;

    public MenuItem(int id, String name, String category, BigDecimal price, boolean available, String description) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.price = price;
        this.available = available;
        this.description = description;
    }

    // Getters and setters
    public int getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @Override
    public String toString() {
        return String.format("ID: %d | %s | %s | $%.2f | %s",
                id, name, category, price, available ? "Available" : "Unavailable");
    }
}

// Table Model
class RestaurantTable {
    private int id;
    private int tableNumber;
    private int capacity;
    private String status;

    public RestaurantTable(int id, int tableNumber, int capacity, String status) {
        this.id = id;
        this.tableNumber = tableNumber;
        this.capacity = capacity;
        this.status = status;
    }

    // Getters and setters
    public int getId() { return id; }
    public int getTableNumber() { return tableNumber; }
    public int getCapacity() { return capacity; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return String.format("Table %d (Capacity: %d) - %s", tableNumber, capacity, status);
    }
}

// Order Model
class Order {
    private int id;
    private int tableId;
    private String customerName;
    private String status;
    private BigDecimal totalAmount;
    private LocalDateTime orderTime;
    private List<OrderItem> items;

    public Order(int id, int tableId, String customerName, String status, BigDecimal totalAmount, LocalDateTime orderTime) {
        this.id = id;
        this.tableId = tableId;
        this.customerName = customerName;
        this.status = status;
        this.totalAmount = totalAmount;
        this.orderTime = orderTime;
        this.items = new ArrayList<>();
    }

    // Getters and setters
    public int getId() { return id; }
    public int getTableId() { return tableId; }
    public String getCustomerName() { return customerName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public LocalDateTime getOrderTime() { return orderTime; }
    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }
}

// OrderItem Model
class OrderItem {
    private int id;
    private int orderId;
    private int menuItemId;
    private String itemName;
    private int quantity;
    private BigDecimal price;

    public OrderItem(int id, int orderId, int menuItemId, String itemName, int quantity, BigDecimal price) {
        this.id = id;
        this.orderId = orderId;
        this.menuItemId = menuItemId;
        this.itemName = itemName;
        this.quantity = quantity;
        this.price = price;
    }

    // Getters and setters
    public int getId() { return id; }
    public int getOrderId() { return orderId; }
    public int getMenuItemId() { return menuItemId; }
    public String getItemName() { return itemName; }
    public int getQuantity() { return quantity; }
    public BigDecimal getPrice() { return price; }
    public BigDecimal getTotal() { return price.multiply(new BigDecimal(quantity)); }

    @Override
    public String toString() {
        return String.format("%s x%d @ $%.2f = $%.2f", itemName, quantity, price, getTotal());
    }
}

// Bill Model
class Bill {
    private int id;
    private int orderId;
    private BigDecimal subtotal;
    private BigDecimal tax;
    private BigDecimal total;
    private String paymentStatus;
    private LocalDateTime billTime;

    public Bill(int id, int orderId, BigDecimal subtotal, BigDecimal tax, BigDecimal total, String paymentStatus, LocalDateTime billTime) {
        this.id = id;
        this.orderId = orderId;
        this.subtotal = subtotal;
        this.tax = tax;
        this.total = total;
        this.paymentStatus = paymentStatus;
        this.billTime = billTime;
    }

    // Getters and setters
    public int getId() { return id; }
    public int getOrderId() { return orderId; }
    public BigDecimal getSubtotal() { return subtotal; }
    public BigDecimal getTax() { return tax; }
    public BigDecimal getTotal() { return total; }
    public String getPaymentStatus() { return paymentStatus; }
    public LocalDateTime getBillTime() { return billTime; }
}

// User Management Service
class UserManagementService {
    public boolean registerUser(String username, String password, String role) {
        try (Connection conn = DatabaseManager.getConnection()) {
            String sql = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            pstmt.setString(2, password); // In production, hash the password
            pstmt.setString(3, role);

            int affected = pstmt.executeUpdate();
            return affected > 0;

        } catch (SQLException e) {
            System.err.println("Error registering user: " + e.getMessage());
            return false;
        }
    }

    public users authenticateUser(String username, String password) {
        try (Connection conn = DatabaseManager.getConnection()) {
            String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            pstmt.setString(2, password);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new users(rs.getInt("id"), rs.getString("username"),
                        rs.getString("password"), rs.getString("role"));
            }

        } catch (SQLException e) {
            System.err.println("Error authenticating user: " + e.getMessage());
        }
        return null;
    }
}

// Menu Management Service
class MenuManagementService {
    public boolean addMenuItem(String name, String category, BigDecimal price, String description) {
        try (Connection conn = DatabaseManager.getConnection()) {
            String sql = "INSERT INTO menu_items (name, category, price, description) VALUES (?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, name);
            pstmt.setString(2, category);
            pstmt.setBigDecimal(3, price);
            pstmt.setString(4, description);

            int affected = pstmt.executeUpdate();
            return affected > 0;

        } catch (SQLException e) {
            System.err.println("Error adding menu item: " + e.getMessage());
            return false;
        }
    }

    public boolean updateMenuItem(int id, String name, String category, BigDecimal price, String description) {
        try (Connection conn = DatabaseManager.getConnection()) {
            String sql = "UPDATE menu_items SET name = ?, category = ?, price = ?, description = ? WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, name);
            pstmt.setString(2, category);
            pstmt.setBigDecimal(3, price);
            pstmt.setString(4, description);
            pstmt.setInt(5, id);

            int affected = pstmt.executeUpdate();
            return affected > 0;

        } catch (SQLException e) {
            System.err.println("Error updating menu item: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteMenuItem(int id) {
        try (Connection conn = DatabaseManager.getConnection()) {
            String sql = "DELETE FROM menu_items WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);

            int affected = pstmt.executeUpdate();
            return affected > 0;

        } catch (SQLException e) {
            System.err.println("Error deleting menu item: " + e.getMessage());
            return false;
        }
    }

    public List<MenuItem> getAllMenuItems() {
        List<MenuItem> items = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection()) {
            String sql = "SELECT * FROM menu_items ORDER BY category, name";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                MenuItem item = new MenuItem(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("category"),
                        rs.getBigDecimal("price"),
                        rs.getBoolean("available"),
                        rs.getString("description")
                );
                items.add(item);
            }

        } catch (SQLException e) {
            System.err.println("Error fetching menu items: " + e.getMessage());
        }
        return items;
    }

    public List<MenuItem> getMenuItemsByCategory(String category) {
        List<MenuItem> items = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection()) {
            String sql = "SELECT * FROM menu_items WHERE category = ? ORDER BY name";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, category);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                MenuItem item = new MenuItem(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("category"),
                        rs.getBigDecimal("price"),
                        rs.getBoolean("available"),
                        rs.getString("description")
                );
                items.add(item);
            }

        } catch (SQLException e) {
            System.err.println("Error fetching menu items by category: " + e.getMessage());
        }
        return items;
    }

    public boolean updateItemAvailability(int id, boolean available) {
        try (Connection conn = DatabaseManager.getConnection()) {
            String sql = "UPDATE menu_items SET available = ? WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setBoolean(1, available);
            pstmt.setInt(2, id);

            int affected = pstmt.executeUpdate();
            return affected > 0;

        } catch (SQLException e) {
            System.err.println("Error updating item availability: " + e.getMessage());
            return false;
        }
    }

    public MenuItem getMenuItemById(int id) {
        try (Connection conn = DatabaseManager.getConnection()) {
            String sql = "SELECT * FROM menu_items WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new MenuItem(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("category"),
                        rs.getBigDecimal("price"),
                        rs.getBoolean("available"),
                        rs.getString("description")
                );
            }

        } catch (SQLException e) {
            System.err.println("Error fetching menu item: " + e.getMessage());
        }
        return null;
    }
}

// Table Management Service
class TableManagementService {
    public boolean addTable(int tableNumber, int capacity) {
        try (Connection conn = DatabaseManager.getConnection()) {
            String sql = "INSERT INTO tables (table_number, capacity) VALUES (?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, tableNumber);
            pstmt.setInt(2, capacity);

            int affected = pstmt.executeUpdate();
            return affected > 0;

        } catch (SQLException e) {
            System.err.println("Error adding table: " + e.getMessage());
            return false;
        }
    }

    public List<RestaurantTable> getAllTables() {
        List<RestaurantTable> tables = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection()) {
            String sql = "SELECT * FROM tables ORDER BY table_number";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                RestaurantTable table = new RestaurantTable(
                        rs.getInt("id"),
                        rs.getInt("table_number"),
                        rs.getInt("capacity"),
                        rs.getString("status")
                );
                tables.add(table);
            }

        } catch (SQLException e) {
            System.err.println("Error fetching tables: " + e.getMessage());
        }
        return tables;
    }

    public boolean updateTableStatus(int tableId, String status) {
        try (Connection conn = DatabaseManager.getConnection()) {
            String sql = "UPDATE tables SET status = ? WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, status);
            pstmt.setInt(2, tableId);

            int affected = pstmt.executeUpdate();
            return affected > 0;

        } catch (SQLException e) {
            System.err.println("Error updating table status: " + e.getMessage());
            return false;
        }
    }

    public List<RestaurantTable> getAvailableTables() {
        List<RestaurantTable> tables = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection()) {
            String sql = "SELECT * FROM tables WHERE status = 'AVAILABLE' ORDER BY table_number";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                RestaurantTable table = new RestaurantTable(
                        rs.getInt("id"),
                        rs.getInt("table_number"),
                        rs.getInt("capacity"),
                        rs.getString("status")
                );
                tables.add(table);
            }

        } catch (SQLException e) {
            System.err.println("Error fetching available tables: " + e.getMessage());
        }
        return tables;
    }
}

// Order Management Service
class OrderManagementService {
    public int createOrder(int tableId, String customerName) {
        try (Connection conn = DatabaseManager.getConnection()) {
            String sql = "INSERT INTO orders (table_id, customer_name) VALUES (?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setInt(1, tableId);
            pstmt.setString(2, customerName);

            int affected = pstmt.executeUpdate();
            if (affected > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error creating order: " + e.getMessage());
        }
        return -1;
    }

    public boolean addItemToOrder(int orderId, int menuItemId, int quantity) {
        try (Connection conn = DatabaseManager.getConnection()) {
            // Get menu item price
            String getPriceSql = "SELECT price FROM menu_items WHERE id = ?";
            PreparedStatement getPriceStmt = conn.prepareStatement(getPriceSql);
            getPriceStmt.setInt(1, menuItemId);
            ResultSet rs = getPriceStmt.executeQuery();

            if (rs.next()) {
                BigDecimal price = rs.getBigDecimal("price");

                // Add item to order
                String sql = "INSERT INTO order_items (order_id, menu_item_id, quantity, price) VALUES (?, ?, ?, ?)";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setInt(1, orderId);
                pstmt.setInt(2, menuItemId);
                pstmt.setInt(3, quantity);
                pstmt.setBigDecimal(4, price);

                int affected = pstmt.executeUpdate();
                if (affected > 0) {
                    updateOrderTotal(orderId);
                    return true;
                }
            }

        } catch (SQLException e) {
            System.err.println("Error adding item to order: " + e.getMessage());
        }
        return false;
    }

    private void updateOrderTotal(int orderId) {
        try (Connection conn = DatabaseManager.getConnection()) {
            String sql = "UPDATE orders SET total_amount = (SELECT SUM(price * quantity) FROM order_items WHERE order_id = ?) WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, orderId);
            pstmt.setInt(2, orderId);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error updating order total: " + e.getMessage());
        }
    }

    public List<Order> getAllOrders() {
        List<Order> orders = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection()) {
            String sql = "SELECT * FROM orders ORDER BY order_time DESC";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Order order = new Order(
                        rs.getInt("id"),
                        rs.getInt("table_id"),
                        rs.getString("customer_name"),
                        rs.getString("status"),
                        rs.getBigDecimal("total_amount"),
                        rs.getTimestamp("order_time").toLocalDateTime()
                );

                // Load order items
                order.setItems(getOrderItems(order.getId()));
                orders.add(order);
            }

        } catch (SQLException e) {
            System.err.println("Error fetching orders: " + e.getMessage());
        }
        return orders;
    }

    public List<OrderItem> getOrderItems(int orderId) {
        List<OrderItem> items = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection()) {
            String sql = """
                SELECT oi.*, mi.name 
                FROM order_items oi 
                JOIN menu_items mi ON oi.menu_item_id = mi.id 
                WHERE oi.order_id = ?
            """;
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, orderId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                OrderItem item = new OrderItem(
                        rs.getInt("id"),
                        rs.getInt("order_id"),
                        rs.getInt("menu_item_id"),
                        rs.getString("name"),
                        rs.getInt("quantity"),
                        rs.getBigDecimal("price")
                );
                items.add(item);
            }

        } catch (SQLException e) {
            System.err.println("Error fetching order items: " + e.getMessage());
        }
        return items;
    }

    public boolean updateOrderStatus(int orderId, String status) {
        try (Connection conn = DatabaseManager.getConnection()) {
            String sql = "UPDATE orders SET status = ? WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, status);
            pstmt.setInt(2, orderId);

            int affected = pstmt.executeUpdate();
            return affected > 0;

        } catch (SQLException e) {
            System.err.println("Error updating order status: " + e.getMessage());
            return false;
        }
    }

    public Order getOrderById(int orderId) {
        try (Connection conn = DatabaseManager.getConnection()) {
            String sql = "SELECT * FROM orders WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, orderId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Order order = new Order(
                        rs.getInt("id"),
                        rs.getInt("table_id"),
                        rs.getString("customer_name"),
                        rs.getString("status"),
                        rs.getBigDecimal("total_amount"),
                        rs.getTimestamp("order_time").toLocalDateTime()
                );
                order.setItems(getOrderItems(order.getId()));
                return order;
            }

        } catch (SQLException e) {
            System.err.println("Error fetching order: " + e.getMessage());
        }
        return null;
    }
}

// Billing Service
class BillingService {
    private static final BigDecimal TAX_RATE = new BigDecimal("0.08"); // 8% tax

    public Bill generateBill(int orderId) {
        try (Connection conn = DatabaseManager.getConnection()) {
            // Get order total
            String getOrderSql = "SELECT total_amount FROM orders WHERE id = ?";
            PreparedStatement getOrderStmt = conn.prepareStatement(getOrderSql);
            getOrderStmt.setInt(1, orderId);
            ResultSet rs = getOrderStmt.executeQuery();

            if (rs.next()) {
                BigDecimal subtotal = rs.getBigDecimal("total_amount");
                BigDecimal tax = subtotal.multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP);
                BigDecimal total = subtotal.add(tax);

                // Insert bill
                String sql = "INSERT INTO bills (order_id, subtotal, tax, total) VALUES (?, ?, ?, ?)";
                PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                pstmt.setInt(1, orderId);
                pstmt.setBigDecimal(2, subtotal);
                pstmt.setBigDecimal(3, tax);
                pstmt.setBigDecimal(4, total);

                int affected = pstmt.executeUpdate();
                if (affected > 0) {
                    ResultSet generatedKeys = pstmt.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        int billId = generatedKeys.getInt(1);
                        return new Bill(billId, orderId, subtotal, tax, total, "PENDING", LocalDateTime.now());
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println("Error generating bill: " + e.getMessage());
        }
        return null;
    }

    public Bill getBillByOrderId(int orderId) {
        try (Connection conn = DatabaseManager.getConnection()) {
            String sql = "SELECT * FROM bills WHERE order_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, orderId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new Bill(
                        rs.getInt("id"),
                        rs.getInt("order_id"),
                        rs.getBigDecimal("subtotal"),
                        rs.getBigDecimal("tax"),
                        rs.getBigDecimal("total"),
                        rs.getString("payment_status"),
                        rs.getTimestamp("bill_time").toLocalDateTime()
                );
            }

        } catch (SQLException e) {
            System.err.println("Error fetching bill: " + e.getMessage());
        }
        return null;
    }

    public boolean markBillAsPaid(int billId) {
        try (Connection conn = DatabaseManager.getConnection()) {
            String sql = "UPDATE bills SET payment_status = 'PAID' WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, billId);

            int affected = pstmt.executeUpdate();
            return affected > 0;

        } catch (SQLException e) {
            System.err.println("Error updating bill payment status: " + e.getMessage());
            return false;
        }
    }

    public void printBill(Bill bill, Order order) {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("              RESTAURANT BILL");
        System.out.println("=".repeat(50));
        System.out.println("Bill ID: " + bill.getId());
        System.out.println("Order ID: " + order.getId());
        System.out.println("Table: " + order.getTableId());
        System.out.println("Customer: " + order.getCustomerName());
        System.out.println("Date: " + bill.getBillTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        System.out.println("-".repeat(50));

        for (OrderItem item : order.getItems()) {
            System.out.printf("%-25s %2dx$%6.2f = $%8.2f%n",
                    item.getItemName(), item.getQuantity(), item.getPrice(), item.getTotal());
        }

        System.out.println("-".repeat(50));
        System.out.printf("%-35s $%8.2f%n", "Subtotal:", bill.getSubtotal());
        System.out.printf("%-35s $%8.2f%n", "Tax (8%):", bill.getTax());
        System.out.println("=".repeat(50));
        System.out.printf("%-35s $%8.2f%n", "TOTAL:", bill.getTotal());
        System.out.println("=".repeat(50));
        System.out.println("Payment Status: " + bill.getPaymentStatus());
        System.out.println("\nThank you for dining with us!");
        System.out.println("=".repeat(50));
    }
}

// Main Restaurant Management System
class RestaurantManagementSystem {
    private static Scanner scanner = new Scanner(System.in);
    private static users currentUser = null;
    private static UserManagementService userService = new UserManagementService();
    private static MenuManagementService menuService = new MenuManagementService();
    private static TableManagementService tableService = new TableManagementService();
    private static OrderManagementService orderService = new OrderManagementService();
    private static BillingService billingService = new BillingService();

    public static void main(String[] args) {
        System.out.println("Welcome to Restaurant Management System!");

        // Initialize database
        DatabaseManager.initializeDatabase();

        // Create default admin user if not exists
        createDefaultAdmin();

        // Main application loop
        while (true) {
            if (currentUser == null) {
                showLoginMenu();
            } else {
                showMainMenu();
            }
        }
    }

    private static void createDefaultAdmin() {
        userService.registerUser("admin", "admin123", "ADMIN");
    }

    private static void showLoginMenu() {
        System.out.println("\n=== LOGIN ===");
        System.out.println("1. Login");
        System.out.println("2. Register User");
        System.out.println("3. Exit");
        System.out.print("Choose an option: ");

        int choice = scanner.nextInt();
        scanner.nextLine(); // consume newline

        switch (choice) {
            case 1:
                login();
                break;
            case 2:
                registerUser();
                break;
            case 3:
                System.out.println("Goodbye!");
                System.exit(0);
                break;
            default:
                System.out.println("Invalid option!");
        }
    }

    private static void login() {
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();

        currentUser = userService.authenticateUser(username, password);
        if (currentUser != null) {
            System.out.println("Login successful! Welcome, " + currentUser.getUsername());
        } else {
            System.out.println("Invalid credentials!");
        }
    }

    private static void registerUser() {
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();
        System.out.print("Role (ADMIN/STAFF): ");
        String role = scanner.nextLine().toUpperCase();

        if (!role.equals("ADMIN") && !role.equals("STAFF")) {
            System.out.println("Invalid role! Must be ADMIN or STAFF");
            return;
        }

        if (userService.registerUser(username, password, role)) {
            System.out.println("User registered successfully!");
        } else {
            System.out.println("Registration failed! Username might already exist.");
        }
    }

    private static void showMainMenu() {
        System.out.println("\n=== RESTAURANT MANAGEMENT SYSTEM ===");
        System.out.println("User: " + currentUser.getUsername() + " (" + currentUser.getRole() + ")");
        System.out.println();
        System.out.println("1. Menu Management");
        System.out.println("2. Order Management");
        System.out.println("3. Table Management");
        System.out.println("4. Billing System");
        System.out.println("5. User Management");
        System.out.println("6. Logout");
        System.out.print("Choose an option: ");

        int choice = scanner.nextInt();
        scanner.nextLine(); // consume newline

        switch (choice) {
            case 1:
                showMenuManagement();
                break;
            case 2:
                showOrderManagement();
                break;
            case 3:
                showTableManagement();
                break;
            case 4:
                showBillingSystem();
                break;
            case 5:
                if (currentUser.getRole().equals("ADMIN")) {
                    showUserManagement();
                } else {
                    System.out.println("Access denied! Admin privileges required.");
                }
                break;
            case 6:
                currentUser = null;
                System.out.println("Logged out successfully!");
                break;
            default:
                System.out.println("Invalid option!");
        }
    }

    private static void showMenuManagement() {
        System.out.println("\n=== MENU MANAGEMENT ===");
        System.out.println("1. Add Menu Item");
        System.out.println("2. View All Menu Items");
        System.out.println("3. View by Category");
        System.out.println("4. Update Menu Item");
        System.out.println("5. Delete Menu Item");
        System.out.println("6. Update Item Availability");
        System.out.println("7. Back to Main Menu");
        System.out.print("Choose an option: ");

        int choice = scanner.nextInt();
        scanner.nextLine(); // consume newline

        switch (choice) {
            case 1:
                addMenuItem();
                break;
            case 2:
                viewAllMenuItems();
                break;
            case 3:
                viewMenuByCategory();
                break;
            case 4:
                updateMenuItem();
                break;
            case 5:
                deleteMenuItem();
                break;
            case 6:
                updateItemAvailability();
                break;
            case 7:
                return;
            default:
                System.out.println("Invalid option!");
        }
    }

    private static void addMenuItem() {
        System.out.print("Item name: ");
        String name = scanner.nextLine();
        System.out.print("Category: ");
        String category = scanner.nextLine();
        System.out.print("Price: $");
        BigDecimal price = scanner.nextBigDecimal();
        scanner.nextLine(); // consume newline
        System.out.print("Description: ");
        String description = scanner.nextLine();

        if (menuService.addMenuItem(name, category, price, description)) {
            System.out.println("Menu item added successfully!");
        } else {
            System.out.println("Failed to add menu item!");
        }
    }

    private static void viewAllMenuItems() {
        List<MenuItem> items = menuService.getAllMenuItems();
        if (items.isEmpty()) {
            System.out.println("No menu items found.");
            return;
        }

        System.out.println("\n=== ALL MENU ITEMS ===");
        for (MenuItem item : items) {
            System.out.println(item);
            if (item.getDescription() != null && !item.getDescription().isEmpty()) {
                System.out.println("   Description: " + item.getDescription());
            }
            System.out.println();
        }
    }

    private static void viewMenuByCategory() {
        System.out.print("Enter category: ");
        String category = scanner.nextLine();

        List<MenuItem> items = menuService.getMenuItemsByCategory(category);
        if (items.isEmpty()) {
            System.out.println("No items found in category: " + category);
            return;
        }

        System.out.println("\n=== MENU ITEMS - " + category.toUpperCase() + " ===");
        for (MenuItem item : items) {
            System.out.println(item);
            if (item.getDescription() != null && !item.getDescription().isEmpty()) {
                System.out.println("   Description: " + item.getDescription());
            }
            System.out.println();
        }
    }

    private static void updateMenuItem() {
        System.out.print("Enter item ID to update: ");
        int id = scanner.nextInt();
        scanner.nextLine(); // consume newline

        MenuItem item = menuService.getMenuItemById(id);
        if (item == null) {
            System.out.println("Menu item not found!");
            return;
        }

        System.out.println("Current item: " + item);
        System.out.print("New name (press Enter to keep current): ");
        String name = scanner.nextLine();
        if (name.isEmpty()) name = item.getName();

        System.out.print("New category (press Enter to keep current): ");
        String category = scanner.nextLine();
        if (category.isEmpty()) category = item.getCategory();

        System.out.print("New price (press Enter to keep current): $");
        String priceStr = scanner.nextLine();
        BigDecimal price = priceStr.isEmpty() ? item.getPrice() : new BigDecimal(priceStr);

        System.out.print("New description (press Enter to keep current): ");
        String description = scanner.nextLine();
        if (description.isEmpty()) description = item.getDescription();

        if (menuService.updateMenuItem(id, name, category, price, description)) {
            System.out.println("Menu item updated successfully!");
        } else {
            System.out.println("Failed to update menu item!");
        }
    }

    private static void deleteMenuItem() {
        System.out.print("Enter item ID to delete: ");
        int id = scanner.nextInt();
        scanner.nextLine(); // consume newline

        MenuItem item = menuService.getMenuItemById(id);
        if (item == null) {
            System.out.println("Menu item not found!");
            return;
        }

        System.out.println("Item to delete: " + item);
        System.out.print("Are you sure? (y/N): ");
        String confirm = scanner.nextLine();

        if (confirm.toLowerCase().startsWith("y")) {
            if (menuService.deleteMenuItem(id)) {
                System.out.println("Menu item deleted successfully!");
            } else {
                System.out.println("Failed to delete menu item!");
            }
        }
    }

    private static void updateItemAvailability() {
        System.out.print("Enter item ID: ");
        int id = scanner.nextInt();
        scanner.nextLine(); // consume newline

        MenuItem item = menuService.getMenuItemById(id);
        if (item == null) {
            System.out.println("Menu item not found!");
            return;
        }

        System.out.println("Current item: " + item);
        System.out.print("Set as available? (y/n): ");
        String available = scanner.nextLine();

        boolean isAvailable = available.toLowerCase().startsWith("y");

        if (menuService.updateItemAvailability(id, isAvailable)) {
            System.out.println("Item availability updated successfully!");
        } else {
            System.out.println("Failed to update item availability!");
        }
    }

    private static void showTableManagement() {
        System.out.println("\n=== TABLE MANAGEMENT ===");
        System.out.println("1. Add Table");
        System.out.println("2. View All Tables");
        System.out.println("3. View Available Tables");
        System.out.println("4. Update Table Status");
        System.out.println("5. Back to Main Menu");
        System.out.print("Choose an option: ");

        int choice = scanner.nextInt();
        scanner.nextLine(); // consume newline

        switch (choice) {
            case 1:
                addTable();
                break;
            case 2:
                viewAllTables();
                break;
            case 3:
                viewAvailableTables();
                break;
            case 4:
                updateTableStatus();
                break;
            case 5:
                return;
            default:
                System.out.println("Invalid option!");
        }
    }

    private static void addTable() {
        System.out.print("Table number: ");
        int tableNumber = scanner.nextInt();
        System.out.print("Capacity: ");
        int capacity = scanner.nextInt();
        scanner.nextLine(); // consume newline

        if (tableService.addTable(tableNumber, capacity)) {
            System.out.println("Table added successfully!");
        } else {
            System.out.println("Failed to add table! Table number might already exist.");
        }
    }

    private static void viewAllTables() {
        List<RestaurantTable> tables = tableService.getAllTables();
        if (tables.isEmpty()) {
            System.out.println("No tables found.");
            return;
        }

        System.out.println("\n=== ALL TABLES ===");
        for (RestaurantTable table : tables) {
            System.out.println("ID: " + table.getId() + " | " + table);
        }
    }

    private static void viewAvailableTables() {
        List<RestaurantTable> tables = tableService.getAvailableTables();
        if (tables.isEmpty()) {
            System.out.println("No available tables.");
            return;
        }

        System.out.println("\n=== AVAILABLE TABLES ===");
        for (RestaurantTable table : tables) {
            System.out.println("ID: " + table.getId() + " | " + table);
        }
    }

    private static void updateTableStatus() {
        System.out.print("Enter table ID: ");
        int tableId = scanner.nextInt();
        scanner.nextLine(); // consume newline

        System.out.println("Status options: AVAILABLE, OCCUPIED, RESERVED");
        System.out.print("Enter new status: ");
        String status = scanner.nextLine().toUpperCase();

        if (!status.equals("AVAILABLE") && !status.equals("OCCUPIED") && !status.equals("RESERVED")) {
            System.out.println("Invalid status!");
            return;
        }

        if (tableService.updateTableStatus(tableId, status)) {
            System.out.println("Table status updated successfully!");
        } else {
            System.out.println("Failed to update table status!");
        }
    }

    private static void showOrderManagement() {
        System.out.println("\n=== ORDER MANAGEMENT ===");
        System.out.println("1. Create New Order");
        System.out.println("2. Add Items to Order");
        System.out.println("3. View All Orders");
        System.out.println("4. Update Order Status");
        System.out.println("5. View Order Details");
        System.out.println("6. Back to Main Menu");
        System.out.print("Choose an option: ");

        int choice = scanner.nextInt();
        scanner.nextLine(); // consume newline

        switch (choice) {
            case 1:
                createNewOrder();
                break;
            case 2:
                addItemsToOrder();
                break;
            case 3:
                viewAllOrders();
                break;
            case 4:
                updateOrderStatus();
                break;
            case 5:
                viewOrderDetails();
                break;
            case 6:
                return;
            default:
                System.out.println("Invalid option!");
        }
    }

    private static void createNewOrder() {
        // Show available tables
        viewAvailableTables();

        System.out.print("Enter table ID: ");
        int tableId = scanner.nextInt();
        scanner.nextLine(); // consume newline

        System.out.print("Customer name: ");
        String customerName = scanner.nextLine();

        int orderId = orderService.createOrder(tableId, customerName);
        if (orderId > 0) {
            System.out.println("Order created successfully! Order ID: " + orderId);
            // Update table status to occupied
            tableService.updateTableStatus(tableId, "OCCUPIED");
        } else {
            System.out.println("Failed to create order!");
        }
    }

    private static void addItemsToOrder() {
        System.out.print("Enter order ID: ");
        int orderId = scanner.nextInt();
        scanner.nextLine(); // consume newline

        Order order = orderService.getOrderById(orderId);
        if (order == null) {
            System.out.println("Order not found!");
            return;
        }

        System.out.println("Order details: Customer: " + order.getCustomerName() +
                ", Table: " + order.getTableId() + ", Status: " + order.getStatus());

        while (true) {
            // Show available menu items
            List<MenuItem> availableItems = menuService.getAllMenuItems().stream()
                    .filter(MenuItem::isAvailable)
                    .toList();

            if (availableItems.isEmpty()) {
                System.out.println("No available menu items.");
                break;
            }

            System.out.println("\n=== AVAILABLE MENU ITEMS ===");
            for (MenuItem item : availableItems) {
                System.out.println(item);
            }

            System.out.print("\nEnter menu item ID (0 to finish): ");
            int itemId = scanner.nextInt();

            if (itemId == 0) break;

            MenuItem item = menuService.getMenuItemById(itemId);
            if (item == null || !item.isAvailable()) {
                System.out.println("Invalid item or item not available!");
                continue;
            }

            System.out.print("Quantity: ");
            int quantity = scanner.nextInt();
            scanner.nextLine(); // consume newline

            if (orderService.addItemToOrder(orderId, itemId, quantity)) {
                System.out.println("Item added to order successfully!");
            } else {
                System.out.println("Failed to add item to order!");
            }
        }
    }

    private static void viewAllOrders() {
        List<Order> orders = orderService.getAllOrders();
        if (orders.isEmpty()) {
            System.out.println("No orders found.");
            return;
        }

        System.out.println("\n=== ALL ORDERS ===");
        for (Order order : orders) {
            System.out.printf("Order ID: %d | Table: %d | Customer: %s | Status: %s | Total: $%.2f | Time: %s%n",
                    order.getId(), order.getTableId(), order.getCustomerName(),
                    order.getStatus(), order.getTotalAmount(),
                    order.getOrderTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        }
    }

    private static void updateOrderStatus() {
        System.out.print("Enter order ID: ");
        int orderId = scanner.nextInt();
        scanner.nextLine(); // consume newline

        System.out.println("Status options: PENDING, PREPARING, COMPLETED, CANCELLED");
        System.out.print("Enter new status: ");
        String status = scanner.nextLine().toUpperCase();

        if (!Arrays.asList("PENDING", "PREPARING", "COMPLETED", "CANCELLED").contains(status)) {
            System.out.println("Invalid status!");
            return;
        }

        if (orderService.updateOrderStatus(orderId, status)) {
            System.out.println("Order status updated successfully!");

            // If order is completed or cancelled, free up the table
            if (status.equals("COMPLETED") || status.equals("CANCELLED")) {
                Order order = orderService.getOrderById(orderId);
                if (order != null) {
                    tableService.updateTableStatus(order.getTableId(), "AVAILABLE");
                }
            }
        } else {
            System.out.println("Failed to update order status!");
        }
    }

    private static void viewOrderDetails() {
        System.out.print("Enter order ID: ");
        int orderId = scanner.nextInt();
        scanner.nextLine(); // consume newline

        Order order = orderService.getOrderById(orderId);
        if (order == null) {
            System.out.println("Order not found!");
            return;
        }

        System.out.println("\n=== ORDER DETAILS ===");
        System.out.println("Order ID: " + order.getId());
        System.out.println("Table: " + order.getTableId());
        System.out.println("Customer: " + order.getCustomerName());
        System.out.println("Status: " + order.getStatus());
        System.out.println("Order Time: " + order.getOrderTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        System.out.println("\nItems:");

        if (order.getItems().isEmpty()) {
            System.out.println("No items in this order.");
        } else {
            for (OrderItem item : order.getItems()) {
                System.out.println("  " + item);
            }
            System.out.printf("\nTotal Amount: $%.2f%n", order.getTotalAmount());
        }
    }

    private static void showBillingSystem() {
        System.out.println("\n=== BILLING SYSTEM ===");
        System.out.println("1. Generate Bill");
        System.out.println("2. View Bill");
        System.out.println("3. Mark Bill as Paid");
        System.out.println("4. Print Bill");
        System.out.println("5. Back to Main Menu");
        System.out.print("Choose an option: ");

        int choice = scanner.nextInt();
        scanner.nextLine(); // consume newline

        switch (choice) {
            case 1:
                generateBill();
                break;
            case 2:
                viewBill();
                break;
            case 3:
                markBillAsPaid();
                break;
            case 4:
                printBill();
                break;
            case 5:
                return;
            default:
                System.out.println("Invalid option!");
        }
    }

    private static void generateBill() {
        System.out.print("Enter order ID: ");
        int orderId = scanner.nextInt();
        scanner.nextLine(); // consume newline

        Order order = orderService.getOrderById(orderId);
        if (order == null) {
            System.out.println("Order not found!");
            return;
        }

        if (!order.getStatus().equals("COMPLETED")) {
            System.out.println("Cannot generate bill for incomplete order!");
            return;
        }

        // Check if bill already exists
        Bill existingBill = billingService.getBillByOrderId(orderId);
        if (existingBill != null) {
            System.out.println("Bill already exists for this order!");
            return;
        }

        Bill bill = billingService.generateBill(orderId);
        if (bill != null) {
            System.out.println("Bill generated successfully!");
            System.out.printf("Bill ID: %d, Total: $%.2f%n", bill.getId(), bill.getTotal());
        } else {
            System.out.println("Failed to generate bill!");
        }
    }

    private static void viewBill() {
        System.out.print("Enter order ID: ");
        int orderId = scanner.nextInt();
        scanner.nextLine(); // consume newline

        Bill bill = billingService.getBillByOrderId(orderId);
        if (bill == null) {
            System.out.println("Bill not found for this order!");
            return;
        }

        System.out.println("\n=== BILL DETAILS ===");
        System.out.println("Bill ID: " + bill.getId());
        System.out.println("Order ID: " + bill.getOrderId());
        System.out.printf("Subtotal: $%.2f%n", bill.getSubtotal());
        System.out.printf("Tax: $%.2f%n", bill.getTax());
        System.out.printf("Total: $%.2f%n", bill.getTotal());
        System.out.println("Payment Status: " + bill.getPaymentStatus());
        System.out.println("Bill Time: " + bill.getBillTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }

    private static void markBillAsPaid() {
        System.out.print("Enter bill ID: ");
        int billId = scanner.nextInt();
        scanner.nextLine(); // consume newline

        if (billingService.markBillAsPaid(billId)) {
            System.out.println("Bill marked as paid successfully!");
        } else {
            System.out.println("Failed to update bill payment status!");
        }
    }

    private static void printBill() {
        System.out.print("Enter order ID: ");
        int orderId = scanner.nextInt();
        scanner.nextLine(); // consume newline

        Bill bill = billingService.getBillByOrderId(orderId);
        Order order = orderService.getOrderById(orderId);

        if (bill == null || order == null) {
            System.out.println("Bill or order not found!");
            return;
        }

        billingService.printBill(bill, order);
    }

    private static void showUserManagement() {
        System.out.println("\n=== USER MANAGEMENT ===");
        System.out.println("1. Register New User");
        System.out.println("2. Back to Main Menu");
        System.out.print("Choose an option: ");

        int choice = scanner.nextInt();
        scanner.nextLine(); // consume newline

        switch (choice) {
            case 1:
                registerUser();
                break;
            case 2:
                return;
            default:
                System.out.println("Invalid option!");
        }
    }
}