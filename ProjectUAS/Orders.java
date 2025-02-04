package ProjectUAS;

import java.sql.*;

public class Orders {
    public static void insertOrder(int userId, int menuId, int quantity, double totalPrice, String status) {
        String query = "INSERT INTO orders (user_id, menu_id, quantity, total_price, status) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = Koneksi.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, menuId);
            stmt.setInt(3, quantity);
            stmt.setDouble(4, totalPrice);
            stmt.setString(5, status);
            stmt.executeUpdate();
            System.out.println("Pesanan berhasil ditambahkan!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void getAllOrders() {
        String query = "SELECT o.id, u.username, m.name, o.quantity, o.total_price, o.status " +
                       "FROM orders o " +
                       "JOIN users u ON o.user_id = u.id " +
                       "JOIN menu m ON o.menu_id = m.id";
        try (Connection conn = Koneksi.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                System.out.println("Order ID: " + rs.getInt("id") +
                                   ", User: " + rs.getString("username") +
                                   ", Menu: " + rs.getString("name") +
                                   ", Quantity: " + rs.getInt("quantity") +
                                   ", Total Price: " + rs.getDouble("total_price") +
                                   ", Status: " + rs.getString("status"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void updateOrderStatus(int orderId, String newStatus) {
        String query = "UPDATE orders SET status = ? WHERE id = ?";
        try (Connection conn = Koneksi.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, newStatus);
            stmt.setInt(2, orderId);
            stmt.executeUpdate();
            System.out.println("Status pesanan berhasil diperbarui!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void deleteOrder(int orderId) {
        String query = "DELETE FROM orders WHERE id = ?";
        try (Connection conn = Koneksi.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, orderId);
            stmt.executeUpdate();
            System.out.println("Pesanan berhasil dihapus!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}