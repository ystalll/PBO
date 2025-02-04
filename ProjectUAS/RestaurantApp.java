package ProjectUAS;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import ProjectUAS.RestaurantApp.OrderFoodPage;
import ProjectUAS.RestaurantApp.ViewMenuPage;
import ProjectUAS.RestaurantApp.ViewReportsPage;
import ProjectUAS.RestaurantApp.ViewTotalPage;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.Vector;

public class RestaurantApp {

    private static Connection connect() {
        try {
            return DriverManager.getConnection("jdbc:mysql://localhost/db_restoran", "root", "");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Database connection failed: " + e.getMessage());
            return null;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginPage().setVisible(true));
    }

    static class LoginPage extends JFrame {
        JTextField usernameField;
        JPasswordField passwordField;

        public LoginPage() {
            setTitle("Login Page");
            setSize(300, 200);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setLocationRelativeTo(null);

            JPanel panel = new JPanel(new GridLayout(3, 2));

            JLabel usernameLabel = new JLabel("Username:");
            usernameField = new JTextField();
            JLabel passwordLabel = new JLabel("Password:");
            passwordField = new JPasswordField();

            JButton loginButton = new JButton("Login");
            loginButton.addActionListener(new LoginAction());

            panel.add(usernameLabel);
            panel.add(usernameField);
            panel.add(passwordLabel);
            panel.add(passwordField);
            panel.add(new JLabel());
            panel.add(loginButton);

            add(panel);
        }

        class LoginAction implements ActionListener {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());

                try (Connection conn = connect();
                     PreparedStatement stmt = conn.prepareStatement("SELECT role FROM users WHERE username = ? AND password = ?")) {

                    stmt.setString(1, username);
                    stmt.setString(2, password);
                    ResultSet rs = stmt.executeQuery();

                    if (rs.next()) {
                        String role = rs.getString("role");
                        dispose();

                        switch (role) {
                            case "admin":
                                new AdminPage().setVisible(true);
                                break;
                            case "kasir":
                                new KasirPage().setVisible(true);
                                break;
                            case "pelanggan":
                                new PelangganPage(username).setVisible(true);
                                break;
                            default:
                                JOptionPane.showMessageDialog(null, "Invalid role");
                        }
                    } else {
                        JOptionPane.showMessageDialog(null, "Invalid username or password");
                    }
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(null, "Database error: " + ex.getMessage());
                }
            }
        }
    }

    static class AdminPage extends JFrame {
        public AdminPage() {
            setTitle("Admin Page");
            setSize(400, 300);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setLocationRelativeTo(null);

            JButton manageUsersButton = new JButton("Manage Users");
            manageUsersButton.addActionListener(e -> new ManageUsersPage().setVisible(true));

            JButton manageMenuButton = new JButton("Manage Menu");
            manageMenuButton.addActionListener(e -> new ManageMenuPage().setVisible(true));

            JButton viewReportsButton = new JButton("View Reports");
            viewReportsButton.addActionListener(e -> new ViewReportsPage().setVisible(true));

            JPanel panel = new JPanel(new GridLayout(3, 1));
            panel.add(manageUsersButton);
            panel.add(manageMenuButton);
            panel.add(viewReportsButton);

            add(panel);
        }
    }

    static class KasirPage extends JFrame {
        public KasirPage() {
            setTitle("Kasir Page");
            setSize(600, 400);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setLocationRelativeTo(null);

            JTable transactionsTable = new JTable();

            JPanel panel = new JPanel(new BorderLayout());
            panel.add(new JScrollPane(transactionsTable), BorderLayout.CENTER);

            add(panel);

            try (Connection conn = connect();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT o.id, u.username, m.name AS menu, o.quantity, o.total_price, o.status " +
                         "FROM orders o JOIN users u ON o.user_id = u.id JOIN menu m ON o.menu_id = m.id")) {

                transactionsTable.setModel(buildTableModel(rs));

            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, "Error loading transactions: " + e.getMessage());
            }
        }
    }

    static class PelangganPage extends JFrame {
        public PelangganPage(String username) {
            setTitle("Pelanggan Page");
            setSize(400, 300);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setLocationRelativeTo(null);

            JButton viewMenuButton = new JButton("View Menu");
            viewMenuButton.addActionListener(e -> new ViewMenuPage(username).setVisible(true));

            JButton orderFoodButton = new JButton("Order Food");
            orderFoodButton.addActionListener(e -> new OrderFoodPage(username).setVisible(true));

            JPanel panel = new JPanel(new GridLayout(2, 1));
            panel.add(viewMenuButton);
            panel.add(orderFoodButton);

            add(panel);
        }
    }

    static class ManageUsersPage extends JFrame {
        JTable usersTable;
        DefaultTableModel model;

        public ManageUsersPage() {
            setTitle("Manage Users");
            setSize(600, 400);
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            setLocationRelativeTo(null);

            model = new DefaultTableModel();
            usersTable = new JTable(model);
            model.addColumn("ID");
            model.addColumn("Username");
            model.addColumn("Role");

            JPanel panel = new JPanel(new BorderLayout());
            panel.add(new JScrollPane(usersTable), BorderLayout.CENTER);

            JButton addButton = new JButton("Add User");
            JButton editButton = new JButton("Edit User");
            JButton deleteButton = new JButton("Delete User");

            JPanel buttonPanel = new JPanel(new FlowLayout());
            buttonPanel.add(addButton);
            buttonPanel.add(editButton);
            buttonPanel.add(deleteButton);
            panel.add(buttonPanel, BorderLayout.SOUTH);

            add(panel);

            loadUsers();

            addButton.addActionListener(e -> addUser());
            editButton.addActionListener(e -> editUser());
            deleteButton.addActionListener(e -> deleteUser());
        }

        private void loadUsers() {
            try (Connection conn = connect();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT * FROM users")) {
                model.setRowCount(0);
                while (rs.next()) {
                    model.addRow(new Object[]{rs.getInt("id"), rs.getString("username"), rs.getString("role")});
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, "Error loading users: " + e.getMessage());
            }
        }

        private void addUser() {
            String username = JOptionPane.showInputDialog("Enter username:");
            String role = JOptionPane.showInputDialog("Enter role (admin/kasir/pelanggan):");

            if (username != null && role != null) {
                try (Connection conn = connect();
                     PreparedStatement stmt = conn.prepareStatement("INSERT INTO users (username, role) VALUES (?, ?)");) {
                    stmt.setString(1, username);
                    stmt.setString(2, role);
                    stmt.executeUpdate();
                    loadUsers();
                } catch (SQLException e) {
                    JOptionPane.showMessageDialog(null, "Error adding user: " + e.getMessage());
                }
            }
        }

        private void editUser() {
            int selectedRow = usersTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(null, "Select a user to edit");
                return;
            }

            int id = (int) model.getValueAt(selectedRow, 0);
            String newUsername = JOptionPane.showInputDialog("Enter new username:");
            String newRole = JOptionPane.showInputDialog("Enter new role (admin/kasir/pelanggan):");

            if (newUsername != null && newRole != null) {
                try (Connection conn = connect();
                     PreparedStatement stmt = conn.prepareStatement("UPDATE users SET username = ?, role = ? WHERE id = ?")) {
                    stmt.setString(1, newUsername);
                    stmt.setString(2, newRole);
                    stmt.setInt(3, id);
                    stmt.executeUpdate();
                    loadUsers();
                } catch (SQLException e) {
                    JOptionPane.showMessageDialog(null, "Error editing user: " + e.getMessage());
                }
            }
        }

        private void deleteUser() {
            int selectedRow = usersTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(null, "Select a user to delete");
                return;
            }

            int id = (int) model.getValueAt(selectedRow, 0);
            try (Connection conn = connect();
                 PreparedStatement stmt = conn.prepareStatement("DELETE FROM users WHERE id = ?")) {
                stmt.setInt(1, id);
                stmt.executeUpdate();
                loadUsers();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, "Error deleting user: " + e.getMessage());
            }
        }
    }

   static class ManageMenuPage extends JFrame {
        public ManageMenuPage() {
            setTitle("Manage Menu");
            setSize(500, 400);
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            setLocationRelativeTo(null);

            JTable menuTable = new JTable();
            JButton addButton = new JButton("Add Menu");
            JButton deleteButton = new JButton("Delete Menu");

            JPanel panel = new JPanel(new BorderLayout());
            panel.add(new JScrollPane(menuTable), BorderLayout.CENTER);

            JPanel buttonPanel = new JPanel();
            buttonPanel.add(addButton);
            buttonPanel.add(deleteButton);
            panel.add(buttonPanel, BorderLayout.SOUTH);

            add(panel);

            // Load menu data
            try (Connection conn = connect();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT * FROM menu")) {

                menuTable.setModel(buildTableModel(rs));

            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, "Error loading menu: " + e.getMessage());
            }

            addButton.addActionListener(e -> {
                String name = JOptionPane.showInputDialog("Enter menu name:");
                String category = JOptionPane.showInputDialog("Enter category:");
                String price = JOptionPane.showInputDialog("Enter price:");

                try (Connection conn = connect();
                     PreparedStatement stmt = conn.prepareStatement("INSERT INTO menu (name, category, price) VALUES (?, ?, ?)")) {

                    stmt.setString(1, name);
                    stmt.setString(2, category);
                    stmt.setDouble(3, Double.parseDouble(price));
                    stmt.executeUpdate();

                    JOptionPane.showMessageDialog(null, "Menu added successfully!");
                    dispose();
                    new ManageMenuPage().setVisible(true);

                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(null, "Error adding menu: " + ex.getMessage());
                }
            });

            deleteButton.addActionListener(e -> {
                String id = JOptionPane.showInputDialog("Enter menu ID to delete:");

                try (Connection conn = connect();
                     PreparedStatement stmt = conn.prepareStatement("DELETE FROM menu WHERE id = ?")) {

                    stmt.setInt(1, Integer.parseInt(id));
                    stmt.executeUpdate();

                    JOptionPane.showMessageDialog(null, "Menu deleted successfully!");
                    dispose();
                    new ManageMenuPage().setVisible(true);

                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(null, "Error deleting menu: " + ex.getMessage());
                }
            });
        }
   }

    static class ViewReportsPage extends JFrame {
        public ViewReportsPage() {
            setTitle("View Reports");
            setSize(500, 400);
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            setLocationRelativeTo(null);

            JTable reportsTable = new JTable();

            JPanel panel = new JPanel(new BorderLayout());
            panel.add(new JScrollPane(reportsTable), BorderLayout.CENTER);
            add(panel);

            try (Connection conn = connect();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT id, user_id, menu_id, quantity, total_price, status FROM orders")) {

                reportsTable.setModel(buildTableModel(rs));

            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, "Error loading reports: " + e.getMessage());
            }
        }
    }

    static class ViewTotalPage extends JFrame {
        public ViewTotalPage() {
            setTitle("View Total Transactions");
            setSize(400, 300);
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            setLocationRelativeTo(null);

            JLabel totalLabel = new JLabel("Total Transactions: ");
            totalLabel.setFont(new Font("Arial", Font.BOLD, 16));

            JPanel panel = new JPanel(new BorderLayout());
            panel.add(totalLabel, BorderLayout.CENTER);
            add(panel);

            try (Connection conn = connect();
     Statement stmt = conn.createStatement();
     ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS total_price FROM orders")) {


                if (rs.next()) {
                    int total = rs.getInt("total_price");
                    totalLabel.setText("Total Transactions: " + total);
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, "Error loading total: " + e.getMessage());
            }
        }
    }

    static class ViewMenuPage extends JFrame {
        public ViewMenuPage(String username) {
            setTitle("View Menu");
            setSize(500, 400);
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            setLocationRelativeTo(null);

            JTable menuTable = new JTable();

            JPanel panel = new JPanel(new BorderLayout());
            panel.add(new JScrollPane(menuTable), BorderLayout.CENTER);
            add(panel);

            try (Connection conn = connect();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT * FROM menu")) {

                menuTable.setModel(buildTableModel(rs));

            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, "Error loading menu: " + e.getMessage());
            }
        }
    }

    static class OrderFoodPage extends JFrame {
        public OrderFoodPage(String username) {
            setTitle("Order Food");
            setSize(500, 400);
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            setLocationRelativeTo(null);

            JTable menuTable = new JTable();
            JButton orderButton = new JButton("Order");

            JPanel panel = new JPanel(new BorderLayout());
            panel.add(new JScrollPane(menuTable), BorderLayout.CENTER);
            panel.add(orderButton, BorderLayout.SOUTH);
            add(panel);

            try (Connection conn = connect();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT * FROM menu")) {

                menuTable.setModel(buildTableModel(rs));

            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, "Error loading menu: " + e.getMessage());
            }

            orderButton.addActionListener(e -> {
                int selectedRow = menuTable.getSelectedRow();
                if (selectedRow == -1) {
                    JOptionPane.showMessageDialog(null, "Please select a menu item to order.");
                    return;
                }
            
                int menuId = (int) menuTable.getValueAt(selectedRow, 0);
                String quantityStr = JOptionPane.showInputDialog("Enter quantity:");
                
                if (quantityStr == null || quantityStr.trim().isEmpty()) return; // Cancel jika input kosong
                int quantity = Integer.parseInt(quantityStr);
            
                try (Connection conn = connect()) {
                    if (conn == null) return;
            
                    // Cari user_id berdasarkan username
                    int userId = -1;
                    try (PreparedStatement stmt = conn.prepareStatement("SELECT id FROM users WHERE username = ?")) {
                        stmt.setString(1, username);
                        ResultSet rs = stmt.executeQuery();
                        if (rs.next()) {
                            userId = rs.getInt("id");
                        }
                    }
            
                    if (userId == -1) {
                        JOptionPane.showMessageDialog(null, "User not found.");
                        return;
                    }
            
                    // Ambil harga menu
                    double price = 0;
                    try (PreparedStatement stmt = conn.prepareStatement("SELECT price FROM menu WHERE id = ?")) {
                        stmt.setInt(1, menuId);
                        ResultSet rs = stmt.executeQuery();
                        if (rs.next()) {
                            price = rs.getDouble("price");
                        }
                    }
            
                    double totalPrice = price * quantity;
            
                    // Simpan order ke database
                    try (PreparedStatement stmt = conn.prepareStatement(
                            "INSERT INTO orders (user_id, menu_id, quantity, total_price, status) VALUES (?, ?, ?, ?, 'pending')")) {
                        stmt.setInt(1, userId);
                        stmt.setInt(2, menuId);
                        stmt.setInt(3, quantity);
                        stmt.setDouble(4, totalPrice);
                        stmt.executeUpdate();
                    }
            
                    JOptionPane.showMessageDialog(null, "Order placed successfully!");
            
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(null, "Error placing order: " + ex.getMessage());
                }
            });            
        }
    }

    private static DefaultTableModel buildTableModel(ResultSet rs) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        Vector<String> columnNames = new Vector<>();
        for (int column = 1; column <= columnCount; column++) {
            columnNames.add(metaData.getColumnName(column));
        }

        Vector<Vector<Object>> data = new Vector<>();
        while (rs.next()) {
            Vector<Object> vector = new Vector<>();
            for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
                vector.add(rs.getObject(columnIndex));
            }
            data.add(vector);
        }

        return new DefaultTableModel(data, columnNames);
    }
}