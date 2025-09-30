import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class RestaurantGUI extends JFrame {
    // Services
    private UserManagementService userService = new UserManagementService();
    private MenuManagementService menuService = new MenuManagementService();
    private TableManagementService tableService = new TableManagementService();
    private OrderManagementService orderService = new OrderManagementService();
    private BillingService billingService = new BillingService();

    // Current user
    private User currentUser = null;

    // UI Components
    private JTabbedPane tabbedPane;
    private JPanel loginPanel, mainPanel;
    private JTextField usernameField, passwordField;
    private JButton loginButton, registerButton;

    // Menu Management Components
    private JTable menuTable;
    private DefaultTableModel menuTableModel;

    // Order Management Components
    private JTable ordersTable;
    private DefaultTableModel ordersTableModel;

    // Table Management Components
    private JTable tablesTable;
    private DefaultTableModel tablesTableModel;
    // Add this inside RestaurantGUI class, not inside main
    private static void setUIFont(Font f) {
        java.util.Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof javax.swing.plaf.FontUIResource) {
                UIManager.put(key, new javax.swing.plaf.FontUIResource(f));
            }
        }
    }

    // Constructor
    public RestaurantGUI() {
        initializeDatabase();
        initializeUI();
    }

    private void initializeDatabase() {
        DatabaseManager.initializeDatabase();
        // Create default admin if not exists
        userService.registerUser("admin", "admin123", "ADMIN");
    }

    private void initializeUI() {
        setTitle("Restaurant Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);

        // Create login panel
        createLoginPanel();
        getContentPane().add(loginPanel);

        setVisible(true);
    }

    private void createLoginPanel() {
        loginPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLabel = new JLabel("Restaurant Management System", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        loginPanel.add(titleLabel, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 1;
        loginPanel.add(new JLabel("Username:"), gbc);

        gbc.gridx = 1;
        usernameField = new JTextField(15);
        loginPanel.add(usernameField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        loginPanel.add(new JLabel("Password:"), gbc);

        gbc.gridx = 1;
        passwordField = new JPasswordField(15);
        loginPanel.add(passwordField, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        JPanel buttonPanel = new JPanel(new FlowLayout());
        loginButton = new JButton("Login");
        registerButton = new JButton("Register");

        loginButton.addActionListener(e -> login());
        registerButton.addActionListener(e -> register());

        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);
        loginPanel.add(buttonPanel, gbc);
    }

    private void login() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        currentUser = userService.authenticateUser(username, password);
        if (currentUser != null) {
            JOptionPane.showMessageDialog(this, "Login successful! Welcome, " + currentUser.getUsername());
            createMainPanel();
            getContentPane().removeAll();
            getContentPane().add(mainPanel);
            revalidate();
            repaint();
        } else {
            JOptionPane.showMessageDialog(this, "Invalid credentials!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void register() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username and password cannot be empty!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String role = (String) JOptionPane.showInputDialog(this, "Select Role:", "User Registration",
                JOptionPane.QUESTION_MESSAGE, null, new String[]{"STAFF", "ADMIN"}, "STAFF");

        if (role != null) {
            if (userService.registerUser(username, password, role)) {
                JOptionPane.showMessageDialog(this, "User registered successfully!");
            } else {
                JOptionPane.showMessageDialog(this, "Registration failed! Username might already exist.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void createMainPanel() {
        mainPanel = new JPanel(new BorderLayout());

        // Create menu bar
        JMenuBar menuBar = new JMenuBar();
        JMenu userMenu = new JMenu("User");
        JMenuItem logoutItem = new JMenuItem("Logout");
        logoutItem.addActionListener(e -> logout());
        userMenu.add(logoutItem);
        menuBar.add(userMenu);

        JLabel userInfo = new JLabel("Logged in as: " + currentUser.getUsername() + " (" + currentUser.getRole() + ")");
        menuBar.add(Box.createHorizontalGlue());
        menuBar.add(userInfo);

        mainPanel.add(menuBar, BorderLayout.NORTH);

        // Create tabbed pane
        tabbedPane = new JTabbedPane();

        // Add tabs based on user role
        if (currentUser.getRole().equals("ADMIN") || currentUser.getRole().equals("STAFF")) {
            tabbedPane.addTab("Menu Management", createMenuManagementPanel());
            tabbedPane.addTab("Order Management", createOrderManagementPanel());
            tabbedPane.addTab("Table Management", createTableManagementPanel());
            tabbedPane.addTab("Billing System", createBillingPanel());

            if (currentUser.getRole().equals("ADMIN")) {
                tabbedPane.addTab("User Management", createUserManagementPanel());
            }
        }

        mainPanel.add(tabbedPane, BorderLayout.CENTER);
    }

    private void logout() {
        currentUser = null;
        getContentPane().removeAll();
        getContentPane().add(loginPanel);
        revalidate();
        repaint();
    }

    private JPanel createMenuManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Table for menu items
        String[] columns = {"ID", "Name", "Category", "Price", "Available", "Description"};
        menuTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        menuTable = new JTable(menuTableModel);
        JScrollPane scrollPane = new JScrollPane(menuTable);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton refreshButton = new JButton("Refresh");
        JButton addButton = new JButton("Add Item");
        JButton editButton = new JButton("Edit Item");
        JButton deleteButton = new JButton("Delete Item");
        JButton toggleButton = new JButton("Toggle Availability");

        refreshButton.addActionListener(e -> refreshMenuTable());
        addButton.addActionListener(e -> addMenuItem());
        editButton.addActionListener(e -> editMenuItem());
        deleteButton.addActionListener(e -> deleteMenuItem());
        toggleButton.addActionListener(e -> toggleMenuItemAvailability());

        buttonPanel.add(refreshButton);
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(toggleButton);

        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        refreshMenuTable();
        return panel;
    }

    private void refreshMenuTable() {
        menuTableModel.setRowCount(0);
        List<MenuItem> items = menuService.getAllMenuItems();
        for (MenuItem item : items) {
            menuTableModel.addRow(new Object[]{
                    item.getId(),
                    item.getName(),
                    item.getCategory(),
                    item.getPrice(),
                    item.isAvailable() ? "Yes" : "No",
                    item.getDescription()
            });
        }
    }

    private void addMenuItem() {
        JTextField nameField = new JTextField();
        JTextField categoryField = new JTextField();
        JTextField priceField = new JTextField();
        JTextArea descriptionArea = new JTextArea(3, 20);

        // ✅ ADD: Availability ComboBox
        String[] availabilityOptions = {"Yes", "No"};
        JComboBox<String> availabilityCombo = new JComboBox<>(availabilityOptions);
        availabilityCombo.setSelectedIndex(0); // Default to "Yes"

        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Category:"));
        panel.add(categoryField);
        panel.add(new JLabel("Price:"));
        panel.add(priceField);
        panel.add(new JLabel("Available:")); // ✅ NEW
        panel.add(availabilityCombo); // ✅ NEW
        panel.add(new JLabel("Description:"));
        panel.add(new JScrollPane(descriptionArea));

        int result = JOptionPane.showConfirmDialog(this, panel, "Add Menu Item",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                BigDecimal price = new BigDecimal(priceField.getText());
                if (menuService.addMenuItem(nameField.getText(), categoryField.getText(),
                        price, descriptionArea.getText())) {

                    // ✅ NEW: Get the menu item ID and set availability
                    List<MenuItem> items = menuService.getAllMenuItems();
                    if (!items.isEmpty()) {
                        MenuItem lastItem = items.get(items.size() - 1);
                        boolean available = availabilityCombo.getSelectedItem().equals("Yes");
                        menuService.updateItemAvailability(lastItem.getId(), available);
                    }

                    JOptionPane.showMessageDialog(this, "Menu item added successfully!");
                    refreshMenuTable();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to add menu item!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid price format!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void editMenuItem() {
        int selectedRow = menuTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a menu item to edit!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int id = (int) menuTableModel.getValueAt(selectedRow, 0);
        MenuItem item = menuService.getMenuItemById(id);

        if (item != null) {
            JTextField nameField = new JTextField(item.getName());
            JTextField categoryField = new JTextField(item.getCategory());
            JTextField priceField = new JTextField(item.getPrice().toString());
            JTextArea descriptionArea = new JTextArea(item.getDescription(), 3, 20);

            // ✅ ADD: Availability ComboBox
            String[] availabilityOptions = {"Yes", "No"};
            JComboBox<String> availabilityCombo = new JComboBox<>(availabilityOptions);
            availabilityCombo.setSelectedItem(item.isAvailable() ? "Yes" : "No");

            JPanel panel = new JPanel(new GridLayout(0, 1));
            panel.add(new JLabel("Name:"));
            panel.add(nameField);
            panel.add(new JLabel("Category:"));
            panel.add(categoryField);
            panel.add(new JLabel("Price:"));
            panel.add(priceField);
            panel.add(new JLabel("Available:")); // ✅ NEW
            panel.add(availabilityCombo); // ✅ NEW
            panel.add(new JLabel("Description:"));
            panel.add(new JScrollPane(descriptionArea));

            int result = JOptionPane.showConfirmDialog(this, panel, "Edit Menu Item",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (result == JOptionPane.OK_OPTION) {
                try {
                    BigDecimal price = new BigDecimal(priceField.getText());
                    if (menuService.updateMenuItem(id, nameField.getText(), categoryField.getText(),
                            price, descriptionArea.getText())) {

                        // ✅ NEW: Update availability based on combo box selection
                        boolean available = availabilityCombo.getSelectedItem().equals("Yes");
                        menuService.updateItemAvailability(id, available);

                        JOptionPane.showMessageDialog(this, "Menu item updated successfully!");
                        refreshMenuTable();
                    } else {
                        JOptionPane.showMessageDialog(this, "Failed to update menu item!", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Invalid price format!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
    private void deleteMenuItem() {
        int selectedRow = menuTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a menu item to delete!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int id = (int) menuTableModel.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this menu item?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            if (menuService.deleteMenuItem(id)) {
                JOptionPane.showMessageDialog(this, "Menu item deleted successfully!");
                refreshMenuTable();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete menu item!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void toggleMenuItemAvailability() {
        int selectedRow = menuTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a menu item!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int id = (int) menuTableModel.getValueAt(selectedRow, 0);
        boolean currentAvailability = menuTableModel.getValueAt(selectedRow, 4).equals("Yes");

        if (menuService.updateItemAvailability(id, !currentAvailability)) {
            JOptionPane.showMessageDialog(this, "Menu item availability updated!");
            refreshMenuTable();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to update availability!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel createOrderManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Table for orders
        String[] columns = {"ID", "Table ID", "Customer", "Status", "Total", "Order Time"};
        ordersTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        ordersTable = new JTable(ordersTableModel);
        JScrollPane scrollPane = new JScrollPane(ordersTable);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton refreshButton = new JButton("Refresh");
        JButton createButton = new JButton("Create Order");
        JButton addItemsButton = new JButton("Add Items");
        JButton viewDetailsButton = new JButton("View Details");
        JButton updateStatusButton = new JButton("Update Status");

        refreshButton.addActionListener(e -> refreshOrdersTable());
        createButton.addActionListener(e -> createOrder());
        addItemsButton.addActionListener(e -> addItemsToOrder());
        viewDetailsButton.addActionListener(e -> viewOrderDetails());
        updateStatusButton.addActionListener(e -> updateOrderStatus());

        buttonPanel.add(refreshButton);
        buttonPanel.add(createButton);
        buttonPanel.add(addItemsButton);
        buttonPanel.add(viewDetailsButton);
        buttonPanel.add(updateStatusButton);

        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        refreshOrdersTable();
        return panel;
    }

    private void refreshOrdersTable() {
        ordersTableModel.setRowCount(0);
        List<Order> orders = orderService.getAllOrders();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        for (Order order : orders) {
            ordersTableModel.addRow(new Object[]{
                    order.getId(),
                    order.getTableId(),
                    order.getCustomerName(),
                    order.getStatus(),
                    order.getTotalAmount(),
                    order.getOrderTime().format(formatter)
            });
        }
    }

    private void createOrder() {
        List<RestaurantTable> availableTables = tableService.getAvailableTables();
        if (availableTables.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No available tables!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String[] tableOptions = availableTables.stream()
                .map(table -> "Table " + table.getTableNumber() + " (Capacity: " + table.getCapacity() + ")")
                .toArray(String[]::new);

        JComboBox<String> tableCombo = new JComboBox<>(tableOptions);
        JTextField customerField = new JTextField();

        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Select Table:"));
        panel.add(tableCombo);
        panel.add(new JLabel("Customer Name:"));
        panel.add(customerField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Create New Order",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            int selectedTableIndex = tableCombo.getSelectedIndex();
            int tableId = availableTables.get(selectedTableIndex).getId();
            String customerName = customerField.getText();

            int orderId = orderService.createOrder(tableId, customerName);
            if (orderId > 0) {
                // Update table status to occupied
                tableService.updateTableStatus(tableId, "OCCUPIED");
                JOptionPane.showMessageDialog(this, "Order created successfully! Order ID: " + orderId);
                refreshOrdersTable();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to create order!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void addItemsToOrder() {
        int selectedRow = ordersTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an order!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int orderId = (int) ordersTableModel.getValueAt(selectedRow, 0);
        Order order = orderService.getOrderById(orderId);

        if (order == null) {
            JOptionPane.showMessageDialog(this, "Order not found!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Get available menu items
        List<MenuItem> availableItems = menuService.getAllMenuItems().stream()
                .filter(MenuItem::isAvailable)
                .toList();

        if (availableItems.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No available menu items!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String[] itemOptions = availableItems.stream()
                .map(item -> item.getName() + " - $" + item.getPrice())
                .toArray(String[]::new);

        JComboBox<String> itemCombo = new JComboBox<>(itemOptions);
        JSpinner quantitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));

        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Select Menu Item:"));
        panel.add(itemCombo);
        panel.add(new JLabel("Quantity:"));
        panel.add(quantitySpinner);

        int result = JOptionPane.showConfirmDialog(this, panel, "Add Items to Order",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            int selectedItemIndex = itemCombo.getSelectedIndex();
            int menuItemId = availableItems.get(selectedItemIndex).getId();
            int quantity = (int) quantitySpinner.getValue();

            if (orderService.addItemToOrder(orderId, menuItemId, quantity)) {
                JOptionPane.showMessageDialog(this, "Item added to order successfully!");
                refreshOrdersTable();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add item to order!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void viewOrderDetails() {
        int selectedRow = ordersTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an order!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int orderId = (int) ordersTableModel.getValueAt(selectedRow, 0);
        Order order = orderService.getOrderById(orderId);

        if (order == null) {
            JOptionPane.showMessageDialog(this, "Order not found!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        StringBuilder details = new StringBuilder();
        details.append("Order ID: ").append(order.getId()).append("\n");
        details.append("Table: ").append(order.getTableId()).append("\n");
        details.append("Customer: ").append(order.getCustomerName()).append("\n");
        details.append("Status: ").append(order.getStatus()).append("\n");
        details.append("Order Time: ").append(order.getOrderTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n\n");
        details.append("Items:\n");

        if (order.getItems().isEmpty()) {
            details.append("No items in this order.\n");
        } else {
            for (OrderItem item : order.getItems()) {
                details.append("  ").append(item).append("\n");
            }
            details.append("\nTotal Amount: $").append(order.getTotalAmount()).append("\n");
        }

        JTextArea textArea = new JTextArea(details.toString());
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(500, 300));

        JOptionPane.showMessageDialog(this, scrollPane, "Order Details", JOptionPane.PLAIN_MESSAGE);
    }

    private void updateOrderStatus() {
        int selectedRow = ordersTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an order!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int orderId = (int) ordersTableModel.getValueAt(selectedRow, 0);
        String currentStatus = (String) ordersTableModel.getValueAt(selectedRow, 3);

        String[] statusOptions = {"PENDING", "PREPARING", "COMPLETED", "CANCELLED"};
        JComboBox<String> statusCombo = new JComboBox<>(statusOptions);
        statusCombo.setSelectedItem(currentStatus);

        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Select New Status:"));
        panel.add(statusCombo);

        int result = JOptionPane.showConfirmDialog(this, panel, "Update Order Status",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String newStatus = (String) statusCombo.getSelectedItem();
            if (orderService.updateOrderStatus(orderId, newStatus)) {
                // If order is completed or cancelled, free up the table
                if (newStatus.equals("COMPLETED") || newStatus.equals("CANCELLED")) {
                    Order order = orderService.getOrderById(orderId);
                    if (order != null) {
                        tableService.updateTableStatus(order.getTableId(), "AVAILABLE");
                    }
                }

                JOptionPane.showMessageDialog(this, "Order status updated successfully!");
                refreshOrdersTable();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update order status!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private JPanel createTableManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Table for tables
        String[] columns = {"ID", "Table Number", "Capacity", "Status"};
        tablesTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablesTable = new JTable(tablesTableModel);
        JScrollPane scrollPane = new JScrollPane(tablesTable);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton refreshButton = new JButton("Refresh");
        JButton addButton = new JButton("Add Table");
        JButton updateStatusButton = new JButton("Update Status");

        refreshButton.addActionListener(e -> refreshTablesTable());
        addButton.addActionListener(e -> addTable());
        updateStatusButton.addActionListener(e -> updateTableStatus());

        buttonPanel.add(refreshButton);
        buttonPanel.add(addButton);
        buttonPanel.add(updateStatusButton);

        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        refreshTablesTable();
        return panel;
    }

    private void refreshTablesTable() {
        tablesTableModel.setRowCount(0);
        List<RestaurantTable> tables = tableService.getAllTables();
        for (RestaurantTable table : tables) {
            tablesTableModel.addRow(new Object[]{
                    table.getId(),
                    table.getTableNumber(),
                    table.getCapacity(),
                    table.getStatus()
            });
        }
    }

    private void addTable() {
        JTextField tableNumberField = new JTextField();
        JTextField capacityField = new JTextField();

        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Table Number:"));
        panel.add(tableNumberField);
        panel.add(new JLabel("Capacity:"));
        panel.add(capacityField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Add Table",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                int tableNumber = Integer.parseInt(tableNumberField.getText());
                int capacity = Integer.parseInt(capacityField.getText());

                if (tableService.addTable(tableNumber, capacity)) {
                    JOptionPane.showMessageDialog(this, "Table added successfully!");
                    refreshTablesTable();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to add table! Table number might already exist.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid number format!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void updateTableStatus() {
        int selectedRow = tablesTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a table!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int tableId = (int) tablesTableModel.getValueAt(selectedRow, 0);
        String currentStatus = (String) tablesTableModel.getValueAt(selectedRow, 3);

        String[] statusOptions = {"AVAILABLE", "OCCUPIED", "RESERVED"};
        JComboBox<String> statusCombo = new JComboBox<>(statusOptions);
        statusCombo.setSelectedItem(currentStatus);

        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Select New Status:"));
        panel.add(statusCombo);

        int result = JOptionPane.showConfirmDialog(this, panel, "Update Table Status",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String newStatus = (String) statusCombo.getSelectedItem();
            if (tableService.updateTableStatus(tableId, newStatus)) {
                JOptionPane.showMessageDialog(this, "Table status updated successfully!");
                refreshTablesTable();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update table status!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private JPanel createBillingPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Create form for billing
        JPanel formPanel = new JPanel(new GridLayout(0, 2, 5, 5));

        JLabel orderIdLabel = new JLabel("Order ID:");
        JTextField orderIdField = new JTextField();
        JButton generateBillButton = new JButton("Generate Bill");
        JButton viewBillButton = new JButton("View Bill");
        JButton markPaidButton = new JButton("Mark as Paid");
        JButton printBillButton = new JButton("Print Bill");

        formPanel.add(orderIdLabel);
        formPanel.add(orderIdField);
        formPanel.add(generateBillButton);
        formPanel.add(viewBillButton);
        formPanel.add(markPaidButton);
        formPanel.add(printBillButton);

        // Text area for bill display
        JTextArea billArea = new JTextArea();
        billArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(billArea);

        generateBillButton.addActionListener(e -> {
            try {
                int orderId = Integer.parseInt(orderIdField.getText());
                generateBill(orderId, billArea);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter a valid order ID!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        viewBillButton.addActionListener(e -> {
            try {
                int orderId = Integer.parseInt(orderIdField.getText());
                viewBill(orderId, billArea);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter a valid order ID!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        markPaidButton.addActionListener(e -> {
            try {
                int orderId = Integer.parseInt(orderIdField.getText());
                markBillAsPaid(orderId);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter a valid order ID!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        printBillButton.addActionListener(e -> {
            try {
                int orderId = Integer.parseInt(orderIdField.getText());
                printBill(orderId, billArea);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter a valid order ID!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        panel.add(formPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void generateBill(int orderId, JTextArea billArea) {
        Order order = orderService.getOrderById(orderId);
        if (order == null) {
            JOptionPane.showMessageDialog(this, "Order not found!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!order.getStatus().equals("COMPLETED")) {
            JOptionPane.showMessageDialog(this, "Cannot generate bill for incomplete order!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Check if bill already exists
        Bill existingBill = billingService.getBillByOrderId(orderId);
        if (existingBill != null) {
            JOptionPane.showMessageDialog(this, "Bill already exists for this order!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Bill bill = billingService.generateBill(orderId);
        if (bill != null) {
            JOptionPane.showMessageDialog(this, "Bill generated successfully! Bill ID: " + bill.getId());
            viewBill(orderId, billArea);
        } else {
            JOptionPane.showMessageDialog(this, "Failed to generate bill!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void viewBill(int orderId, JTextArea billArea) {
        Bill bill = billingService.getBillByOrderId(orderId);
        Order order = orderService.getOrderById(orderId);

        if (bill == null || order == null) {
            JOptionPane.showMessageDialog(this, "Bill or order not found!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        StringBuilder billText = new StringBuilder();
        billText.append("Bill ID: ").append(bill.getId()).append("\n");
        billText.append("Order ID: ").append(order.getId()).append("\n");
        billText.append("Table: ").append(order.getTableId()).append("\n");
        billText.append("Customer: ").append(order.getCustomerName()).append("\n");
        billText.append("Date: ").append(bill.getBillTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n");
        billText.append("----------------------------------------\n");

        for (OrderItem item : order.getItems()) {
            billText.append(String.format("%-25s %2dx$%6.2f = $%8.2f%n",
                    item.getItemName(), item.getQuantity(), item.getPrice(), item.getTotal()));
        }

        billText.append("----------------------------------------\n");
        billText.append(String.format("%-35s $%8.2f%n", "Subtotal:", bill.getSubtotal()));
        billText.append(String.format("%-35s $%8.2f%n", "Tax (8%):", bill.getTax()));
        billText.append("========================================\n");
        billText.append(String.format("%-35s $%8.2f%n", "TOTAL:", bill.getTotal()));
        billText.append("========================================\n");
        billText.append("Payment Status: ").append(bill.getPaymentStatus()).append("\n");

        billArea.setText(billText.toString());
    }

    private void markBillAsPaid(int orderId) {
        Bill bill = billingService.getBillByOrderId(orderId);
        if (bill == null) {
            JOptionPane.showMessageDialog(this, "Bill not found for this order!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (billingService.markBillAsPaid(bill.getId())) {
            JOptionPane.showMessageDialog(this, "Bill marked as paid successfully!");
        } else {
            JOptionPane.showMessageDialog(this, "Failed to update bill payment status!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void printBill(int orderId, JTextArea billArea) {
        Bill bill = billingService.getBillByOrderId(orderId);
        Order order = orderService.getOrderById(orderId);

        if (bill == null || order == null) {
            JOptionPane.showMessageDialog(this, "Bill or order not found!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        viewBill(orderId, billArea);
        JOptionPane.showMessageDialog(this, "Bill printed to console and displayed above.");

        // Also print to console
        billingService.printBill(bill, order);
    }

    private JPanel createUserManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel formPanel = new JPanel(new GridLayout(0, 2, 5, 5));

        JLabel usernameLabel = new JLabel("Username:");
        JTextField usernameField = new JTextField();
        JLabel passwordLabel = new JLabel("Password:");
        JTextField passwordField = new JTextField();
        JLabel roleLabel = new JLabel("Role:");
        JComboBox<String> roleCombo = new JComboBox<>(new String[]{"STAFF", "ADMIN"});
        JButton registerButton = new JButton("Register User");

        formPanel.add(usernameLabel);
        formPanel.add(usernameField);
        formPanel.add(passwordLabel);
        formPanel.add(passwordField);
        formPanel.add(roleLabel);
        formPanel.add(roleCombo);
        formPanel.add(new JLabel());
        formPanel.add(registerButton);

        registerButton.addActionListener(e -> {
            String username = usernameField.getText();
            String password = passwordField.getText();
            String role = (String) roleCombo.getSelectedItem();

            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Username and password cannot be empty!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (userService.registerUser(username, password, role)) {
                JOptionPane.showMessageDialog(this, "User registered successfully!");
                usernameField.setText("");
                passwordField.setText("");
            } else {
                JOptionPane.showMessageDialog(this, "Registration failed! Username might already exist.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        panel.add(formPanel, BorderLayout.NORTH);

        // Table for users
        String[] columns = {"ID", "Username", "Role", "Created At"};
        DefaultTableModel userTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable userTable = new JTable(userTableModel);
        JScrollPane scrollPane = new JScrollPane(userTable);

        // Refresh button
        JButton refreshButton = new JButton("Refresh Users");
        refreshButton.addActionListener(e -> {
            userTableModel.setRowCount(0);
            List<User> users = UserManagementService.getAllUsers();
            for (User user : users) {
                userTableModel.addRow(new Object[]{
                        user.getId(),
                        user.getUsername(),
                        user.getRole(),
                        user.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                });
            }
        });

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(refreshButton);

        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        // Initial refresh
        refreshButton.doClick();

        return panel;
    }

    public static void main(String[] args) {
        setUIFont(new Font("Seagoe UI",Font.PLAIN,16));
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new RestaurantGUI();
        });
    }
}
