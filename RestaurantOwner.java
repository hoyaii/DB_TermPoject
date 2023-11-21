import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class RestaurantOwner {
    private Database db;
    private Scanner scanner;

    public RestaurantOwner(Database db) {
        this.db = db;
        this.scanner = new Scanner(System.in);
    }
    public void registerRestaurant(String name, String address, String cuisineType) { // 음식점 등록
        String sql = "INSERT INTO Restaurant (name, address, cuisine_type) VALUES (?, ?, ?)";
        try {
            PreparedStatement preparedStatement = this.db.connection.prepareStatement(sql);
            preparedStatement.setString(1, name);
            preparedStatement.setString(2, address);
            preparedStatement.setString(3, cuisineType);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error executing SQL query.");
            e.printStackTrace();
        }
    }

    public void registerMenu(int restaurantId, String name, double price) { // 메뉴 및 가격 등록
        String sql = "INSERT INTO Menu (name, price, restaurant_id) VALUES (?, ?, ?)";
        try {
            PreparedStatement preparedStatement = this.db.connection.prepareStatement(sql);
            preparedStatement.setString(1, name);
            preparedStatement.setDouble(2, price);
            preparedStatement.setInt(3, restaurantId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error executing SQL query.");
            e.printStackTrace();
        }
    }

    public void updateMenu(int menuId, String name, double price) { // 메뉴 및 가격 업데이트
        String sql = "UPDATE Menu SET name = ?, price = ? WHERE menu_id = ?";
        try {
            PreparedStatement preparedStatement = this.db.connection.prepareStatement(sql);
            preparedStatement.setString(1, name);
            preparedStatement.setDouble(2, price);
            preparedStatement.setInt(3, menuId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error executing SQL query.");
            e.printStackTrace();
        }
    }

    public void processOrder(int orderId, String status) {
        String sql = "UPDATE `Order` SET order_status = ? WHERE order_id = ?";
        try {
            PreparedStatement preparedStatement = this.db.connection.prepareStatement(sql);
            preparedStatement.setString(1, status);
            preparedStatement.setInt(2, orderId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error executing SQL query.");
            e.printStackTrace();
        }
    }

    public ResultSet getOrderHistory(int restaurantId) {
        String sql = "SELECT * FROM `Order` WHERE menu_id IN (SELECT menu_id FROM Menu WHERE restaurant_id = ?)";
        try {
            PreparedStatement preparedStatement = this.db.connection.prepareStatement(sql);
            preparedStatement.setInt(1, restaurantId);
            return preparedStatement.executeQuery();
        } catch (SQLException e) {
            System.out.println("Error executing SQL query.");
            e.printStackTrace();
            return null;
        }
    }
}