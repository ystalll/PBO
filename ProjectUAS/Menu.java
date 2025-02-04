package ProjectUAS;

import java.sql.*;

public class Menu {
    public static void insertMenu(String name, String category, double price) {
        String query = "INSERT INTO menu (name, category, price) VALUES (?, ?, ?)";
        try (Connection conn = Koneksi.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, name);
            stmt.setString(2, category);
            stmt.setDouble(3, price);
            stmt.executeUpdate();
            System.out.println("Menu berhasil ditambahkan!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void getAllMenu() {
        String query = "SELECT * FROM menu";
        try (Connection conn = Koneksi.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                System.out.println("ID: " + rs.getInt("id") +
                                   ", Name: " + rs.getString("name") +
                                   ", Category: " + rs.getString("category") +
                                   ", Price: " + rs.getDouble("price"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void updateMenu(int menuId, String newName, String newCategory, double newPrice) {
        String query = "UPDATE menu SET name = ?, category = ?, price = ? WHERE id = ?";
        try (Connection conn = Koneksi.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, newName);
            stmt.setString(2, newCategory);
            stmt.setDouble(3, newPrice);
            stmt.setInt(4, menuId);
            stmt.executeUpdate();
            System.out.println("Menu berhasil diperbarui!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void deleteMenu(int menuId) {
        String query = "DELETE FROM menu WHERE id = ?";
        try (Connection conn = Koneksi.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, menuId);
            stmt.executeUpdate();
            System.out.println("Menu berhasil dihapus!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}