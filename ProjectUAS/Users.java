package ProjectUAS;

import java.sql.*;

public class Users {
    public static void insertUser(String username, String password, String role) {
        String query = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";
        try (Connection connection = Koneksi.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setString(3, role);
            stmt.executeUpdate();
            System.out.println("User berhasil ditambahkan!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void getAllUsers() {
        String query = "SELECT * FROM users";
        try (Connection conn = Koneksi.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                System.out.println("ID: " + rs.getInt("id") +
                                   ", Username: " + rs.getString("username") +
                                   ", Role: " + rs.getString("role"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void updateUser(int userId, String newUsername, String newPassword, String newRole) {
        String query = "UPDATE users SET username = ?, password = ?, role = ? WHERE id = ?";
        try (Connection conn = Koneksi.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, newUsername);
            stmt.setString(2, newPassword);
            stmt.setString(3, newRole);
            stmt.setInt(4, userId);
            stmt.executeUpdate();
            System.out.println("User berhasil diperbarui!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void deleteUser(int userId) {
        String query = "DELETE FROM users WHERE id = ?";
        try (Connection conn = Koneksi.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
            System.out.println("User berhasil dihapus!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}