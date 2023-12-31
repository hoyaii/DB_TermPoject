import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class DeliveryPerson {
    private Database db;
    private Scanner scanner;
    private int userId;

    public DeliveryPerson(Database db, int userId) {
        this.db = db;
        this.scanner = new Scanner(System.in);
        this.userId = userId;
    }

    public void manageDeliveryRequestService(){ //  배달원이 요청 리스트를 보고 승낙하여 매칭
        ResultSet resultSet = getDeliveryList("notAccepted");
        List<Integer> deliveryIdList = printDeliveryList(resultSet);

        if(deliveryIdList.isEmpty()){
            System.out.println("배달 요청이 존재하지 않습니다.");
            return;
        }

        Integer deliveryId;
        do {
            System.out.println("수락하고 싶은 요청의 id를 입력하세요:");
            deliveryId = scanner.nextInt();
            scanner.nextLine();

            if(!deliveryIdList.contains(deliveryId)){
                System.out.println("선택하신 배달 ID는 유효하지 않습니다.");
            }
        } while(!deliveryIdList.contains(deliveryId));

        Integer orderId = getOrderIdByDeliveryId(deliveryId, "notMatched");

        updateDeliveryStatus("accepted", deliveryId);
        updateOrderStatus("deliveryMatched", orderId);
        updateUserStatus("notFree");
        System.out.println("요청이 수락되었습니다.");
    }

    public void finishDeliveryService(){
        ResultSet resultSet = getDeliveryList("accepted");
        List<Integer> deliveryIdList = printDeliveryList(resultSet);

        if(deliveryIdList.isEmpty()){
            System.out.println("배달 내역이 존재하지 않습니다.");
            return;
        }

        Integer deliveryId;
        do {
            System.out.println("완료한 배달의 ID를 입력해 주세요:");
            deliveryId = scanner.nextInt();
            scanner.nextLine();

            if(!deliveryIdList.contains(deliveryId)){
                System.out.println("선택하신 배달 ID는 유효하지 않습니다.");
            }
        } while(!deliveryIdList.contains(deliveryId));

        Integer orderId = getOrderIdByDeliveryId(deliveryId, "cooked"); // deliveryId를 가지고 orderId를 구한다

        updateDeliveryStatus("finished", deliveryId);
        updateOrderStatus("finished", orderId);
        updateUserStatus("free");

        updateSales(orderId); // 모든 과정이 완료되었으니, 정산이 진행되서 식당의 매출에 반영된다.

        System.out.println("배달 완료 처리가 되었습니다.");
    }

    public void printDeliveryHistoryService() {
        try {
            List<Integer> deliveryIdList = getDeliveryIdList("finished");

            System.out.println("지금까지 " + deliveryIdList.size() + " 번 배달을 완료하셨습니다!");

            if(deliveryIdList.isEmpty()){
                System.out.println("배달 내역이 존재하지 않습니다.");
            }

            for (int deliveryId : deliveryIdList) {
                printSingleDeliveryHistory(deliveryId);
            }

        } catch (SQLException e) {
            handleSQLException(e);
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    public void updateSales(int orderId){
        int price = getPriceByOrderId(orderId);
        updateSalesPrice(orderId, price);
        updateSalesNum(price);
    }

    public void updateSalesPrice(int orderId, int price) {
        String sql = "UPDATE Restaurant SET sales_price = sales_price + ? WHERE restaurant_id = (SELECT restaurant_id FROM Orders WHERE order_id = ?)";
        try {
            PreparedStatement preparedStatement = this.db.connection.prepareStatement(sql);
            preparedStatement.setInt(1, price);
            preparedStatement.setInt(2, orderId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            handleSQLException(e);
        }
    }

    public void updateSalesNum(int orderId) {
        String sql = "UPDATE Restaurant SET sales_num = sales_num + 1 WHERE restaurant_id = (SELECT restaurant_id FROM Orders WHERE order_id = ?)";
        try {
            PreparedStatement preparedStatement = this.db.connection.prepareStatement(sql);
            preparedStatement.setInt(1, orderId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            handleSQLException(e);
        }
    }

    public Integer getPriceByOrderId(int orderId) {
        String sql = "SELECT price FROM Menu WHERE menu_id = (SELECT menu_id FROM Orders WHERE order_id = ?)";
        try {
            PreparedStatement preparedStatement = this.db.connection.prepareStatement(sql);
            preparedStatement.setInt(1, orderId);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("price");
            } else {
                return null;
            }
        } catch (SQLException e) {
            handleSQLException(e);
            return null;
        }
    }

    public void printSingleDeliveryHistory(int deliveryId) throws SQLException {
        ResultSet resultSet = getDeliveryHistory(deliveryId);

        if(resultSet == null || !resultSet.next()){
            return;
        }

        int orderId = resultSet.getInt("order_id");
        int menuId = resultSet.getInt("menu_id");
        int restaurantId = resultSet.getInt("restaurant_id");
        Timestamp orderTime = resultSet.getTimestamp("order_time");
        LocalDateTime orderDateTime = orderTime.toLocalDateTime();
        String formattedOrderTime = orderDateTime.toString();

        String menuName = getMenuName(menuId);
        String restaurantName = getRestaurantName(restaurantId);
        String deliveryAddress = getDeliveryAddress(orderId);
        System.out.println("주문 ID: " + orderId + "| 가게 이름: " + restaurantName + "| 메뉴 이름: " + menuName + "| 주문 시간: " + formattedOrderTime + "| 배달 주소: " + deliveryAddress);
    }

    public ResultSet getDeliveryList(String status){
        String sql = "SELECT * FROM Delivery WHERE delivery_person_id = ? AND status = ?";
        try{
            PreparedStatement preparedStatement = this.db.connection.prepareStatement(sql);
            preparedStatement.setInt(1, userId);
            preparedStatement.setString(2, status);
            return preparedStatement.executeQuery();
        } catch (SQLException e) {
            handleSQLException(e);
            return null;
        }
    }

    public List<Integer> getDeliveryIdList(String status) throws SQLException {
        String sql = "SELECT delivery_id FROM Delivery WHERE delivery_person_id = ? AND status = ?";
        List<Integer> deliveryIdList = new ArrayList<>();

        PreparedStatement preparedStatement = this.db.connection.prepareStatement(sql);
        preparedStatement.setInt(1, userId);
        preparedStatement.setString(2, status);
        ResultSet resultSet = preparedStatement.executeQuery();

        while(resultSet.next()){
            deliveryIdList.add(resultSet.getInt("delivery_id"));
        }

        return deliveryIdList;
    }

    public List<Integer> printDeliveryList(ResultSet resultSet){
        List<Integer> deliveryIdList = new ArrayList<>();
        try {
            while (resultSet.next()) {
                int deliveryId = resultSet.getInt("delivery_id");
                int restaurantId = resultSet.getInt("restaurant_id");
                String deliveryAddress = resultSet.getString("delivery_address");
                String restaurantAddress = getRestaurantAddress(restaurantId);
                System.out.println("id: " + deliveryId + "| 가게 주소: " + restaurantAddress + "| 배달할 주소: " + deliveryAddress);

                deliveryIdList.add(deliveryId);
            }
        } catch (SQLException e) {
            System.out.println("Error executing SQL query.");
            e.printStackTrace();
        }

        return deliveryIdList;
    }

    public void updateDeliveryStatus(String status, int deliveryId) {
        String sql = "UPDATE Delivery SET status = ? WHERE delivery_id = ?";
        try {
            PreparedStatement preparedStatement = this.db.connection.prepareStatement(sql);
            preparedStatement.setString(1, status);
            preparedStatement.setInt(2, deliveryId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            handleSQLException(e);
        }
    }

    public void updateUserStatus(String status) {
        String sql = "UPDATE User SET status = ? WHERE user_id = ?";
        try {
            PreparedStatement preparedStatement = this.db.connection.prepareStatement(sql);
            preparedStatement.setString(1, status);
            preparedStatement.setInt(2, userId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            handleSQLException(e);
        }
    }

    public void updateOrderStatus(String status, int orderId) {
        String sql = "UPDATE Orders SET status = ? WHERE order_id = ?";
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

    public ResultSet getDeliveryHistory(int deliveryId) {
        String sql = "SELECT * FROM Orders WHERE delivery_id = ? AND status = 'finished'";
        try {
            PreparedStatement preparedStatement = this.db.connection.prepareStatement(sql);
            preparedStatement.setInt(1, deliveryId);
            return preparedStatement.executeQuery();
        } catch (SQLException e) {
            handleSQLException(e);
            return null;
        }
    }

    public String getRestaurantAddress(int restaurantId) {
        String sql = "SELECT address FROM Restaurant  WHERE restaurant_id = ?";
        try {
            PreparedStatement preparedStatement = this.db.connection.prepareStatement(sql);
            preparedStatement.setInt(1, restaurantId);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString("address");
            } else {
                return null;
            }
        } catch (SQLException e) {
            handleSQLException(e);
            return null;
        }
    }

    public String getRestaurantName(int restaurantId) {
        String sql = "SELECT name FROM Restaurant  WHERE restaurant_id = ?";
        try {
            PreparedStatement preparedStatement = this.db.connection.prepareStatement(sql);
            preparedStatement.setInt(1, restaurantId);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString("name");
            } else {
                return null;
            }
        } catch (SQLException e) {
            handleSQLException(e);
            return null;
        }
    }

    public Integer getOrderIdByDeliveryId(int deliveryId, String status) {
        String sql = "SELECT order_id FROM Orders WHERE delivery_id = ? AND status = ?";
        try {
            PreparedStatement preparedStatement = this.db.connection.prepareStatement(sql);
            preparedStatement.setInt(1, deliveryId);
            preparedStatement.setString(2, status);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("order_id");
            }
            else{
                return null;
            }
        } catch (SQLException e) {
            handleSQLException(e);
            return null;
        }
    }

    public String getMenuName(int menuId) {
        String sql = "SELECT name  FROM Menu WHERE menu_id = ?";
        try {
            PreparedStatement preparedStatement = this.db.connection.prepareStatement(sql);
            preparedStatement.setInt(1, menuId);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getString("name");
            } else {
                return null;
            }
        } catch (SQLException e) {
            handleSQLException(e);
            return null;
        }
    }

    public String getDeliveryAddress(int orderId) {
        String sql = "SELECT delivery_address FROM Delivery WHERE delivery_id = (SELECT delivery_id FROM Orders WHERE order_id = ?)";
        try {
            PreparedStatement preparedStatement = this.db.connection.prepareStatement(sql);
            preparedStatement.setInt(1, orderId);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getString("delivery_address");
            } else {
                return null;
            }
        } catch (SQLException e) {
            handleSQLException(e);
            return null;
        }
    }

    private void handleSQLException(SQLException e) {
        System.out.println("SQL 쿼리 실행에서 에러가 발생하였습니다.");
        e.printStackTrace();
    }
}