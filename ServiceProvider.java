import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class ServiceProvider {
    private Database db;
    private Scanner scanner;

    public ServiceProvider(Database db) {
        this.db = db;
        this.scanner = new Scanner(System.in);
    }

    public String login(String email, String password) {
        String sql = "SELECT role FROM User WHERE email = ? AND password = ?";
        try {
            PreparedStatement preparedStatement = this.db.connection.prepareStatement(sql);
            preparedStatement.setString(1, email);
            preparedStatement.setString(2, password);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString("role");
            } else {
                return null;
            }
        } catch (SQLException e) {
            System.out.println("Error executing SQL query.");
            e.printStackTrace();
            return null;
        }
    }

    public Integer getUserIdByEmail(String email) { // userId 구하기
        String sql = "SELECT user_id FROM User WHERE email = ?";
        try {
            PreparedStatement preparedStatement = this.db.connection.prepareStatement(sql);
            preparedStatement.setString(1, email);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("user_id");
            } else {
                return null;
            }
        } catch (SQLException e) {
            System.out.println("Error executing SQL query.");
            e.printStackTrace();
            return null;
        }
    }

    public void registerUser(String email, String username, String password, String role) {
        String sql = "INSERT INTO User (email, username, password, role) VALUES (?, ?, ?, ?)";
        try {
            PreparedStatement preparedStatement = this.db.connection.prepareStatement(sql);
            preparedStatement.setString(1, email);
            preparedStatement.setString(2, username);
            preparedStatement.setString(3, password);
            preparedStatement.setString(4, role);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error executing SQL query.");
            e.printStackTrace();
        }
    }

    public void registerUserService(){
        System.out.println("등록할 유저의 이메일을 입력해 주세요:");
        String email = scanner.nextLine();
        System.out.println("등록할 유저의 이름을 입력해 주세요:");
        String username = scanner.nextLine();
        System.out.println("등록할 유저의 비밀번호를 입력해 주세요:");
        String password = scanner.nextLine();
        System.out.println("등록할 유저의 역할을 입력해 주세요:");
        String role = scanner.nextLine();

        registerUser(email, username, password, role);
    }

    public void updateUser(int userId, String username, String password, String role) { // email은 unique해야 해서 수정이 불가능하다
        String sql = "UPDATE User SET username = ?, password = ?, role = ? WHERE user_id = ?";
        try {
            PreparedStatement preparedStatement = this.db.connection.prepareStatement(sql);
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);
            preparedStatement.setString(3, role);
            preparedStatement.setInt(4, userId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error executing SQL query.");
            e.printStackTrace();
        }
    }

    public void updateUserService(){
        System.out.println("수정할 유저의 ID를 입력해 주세요:");
        int userId = scanner.nextInt();
        scanner.nextLine();  // nextInt 후에 남은 개행문자 처리

        System.out.println("새로운 이름을 입력해 주세요:");
        String newName = scanner.nextLine();
        System.out.println("새로운 역할을 입력해 주세요:");
        String newRole = scanner.nextLine();
        System.out.println("새로운 비밀번호를 입력해 주세요:");
        String newPassword = scanner.nextLine();

        updateUser(userId, newName, newPassword, newRole);
    }

    public void deleteUser(int userId) {
        String sql = "DELETE FROM User WHERE user_id = ?";
        try {
            PreparedStatement preparedStatement = this.db.connection.prepareStatement(sql);
            preparedStatement.setInt(1, userId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error executing SQL query.");
            e.printStackTrace();
        }
    }

    public void deleteUserService(){
        System.out.println("삭제할 유저의 ID를 입력해 주세요:");
        int userId = scanner.nextInt();
        scanner.nextLine();

        deleteUser(userId);
    }
}
