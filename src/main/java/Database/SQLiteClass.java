package main.java.Database;

import main.java.data.OrderedRoomData;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SQLiteClass {

    public static final int REQUEST_FAILED = -1;
    public static final int REQUEST_SUCCESS = 1;

    private static Connection getConnection() throws SQLException {
        String dbUrl = System.getenv("JDBC_DATABASE_URL");
        return DriverManager.getConnection(dbUrl);
    }

    private static void closeConnection(Connection conn) throws SQLException {
        conn.close();
    }

    public static int deviceAdd(String pseudoId, String phoneNum, String name) throws SQLException {

        Connection conn = getConnection();
        PreparedStatement pStatement;

        if (phoneNum.equals("null")) phoneNum = null;
        if (name.equals("null")) name = null;

        pStatement = conn.prepareStatement("INSERT INTO devices (pseudo_id,phone_num,name) VALUES (?,?,?)");
        pStatement.setString(1, pseudoId);
        pStatement.setString(2, phoneNum);
        pStatement.setString(3, name);
        int res;
        try {
            res = pStatement.executeUpdate();
        } catch (SQLException e) {
            if (e.getMessage().contains("unique constraint")) {
                System.out.println("That pseudo_id is already exists.");
                res = 2;
            } else throw e;
        } finally {
            if (!pStatement.isClosed()) pStatement.close();
            if (!conn.isClosed()) closeConnection(conn);
        }
        return res;
    }

    public static String version() throws SQLException {
        Connection conn = getConnection();
        Statement statement = conn.createStatement();
        ResultSet rSet = null;

        String version;
        try {
            rSet = statement.executeQuery("SELECT version() AS version");
            if (rSet.next()) {
                version = rSet.getString("version");
            } else
                version = "null";
        } finally {
            if (rSet != null && !rSet.isClosed()) rSet.close();
            if (statement != null && !statement.isClosed()) statement.close();
            if (!conn.isClosed()) closeConnection(conn);
        }

        return version;
    }

    public static List<Map<String, Object>> deviceGetAll() throws SQLException {
        List<Map<String, Object>> users = new ArrayList<Map<String, Object>>();

        Connection conn = getConnection();
        Statement statement = conn.createStatement();
        ResultSet rSet = null;

        try {
            rSet = statement.executeQuery("SELECT pseudo_id,name,phone_num FROM devices");
            while (rSet.next()) {
                Map<String, Object> cUser = new HashMap<String, Object>();
                cUser.put("pseudo_id", rSet.getString("pseudo_id"));
                cUser.put("name", rSet.getString("name"));
                cUser.put("phone_num", rSet.getString("phone_num"));
                users.add(cUser);
            }
        } finally {
            if (rSet != null && !rSet.isClosed()) rSet.close();
            if (statement != null && !statement.isClosed()) statement.close();
            if (!conn.isClosed()) closeConnection(conn);
        }

        return users;
    }
	
	public static List<Map<String, Object>> orderGetAll() throws SQLException {
		List<Map<String, Object>> orderDataList = new ArrayList<Map<String, Object>>();

		Connection conn = getConnection();
		Statement statement = null;
		ResultSet rSet = null;

		try {
		    statement = conn.createStatement();
            rSet = statement.executeQuery("SELECT * FROM rooms_ordered ORDER BY room_id, date_begin, date_end");

            while (rSet.next()) {
                Map<String, Object> roomRow = new HashMap<String, Object>();
                roomRow.put("id", rSet.getInt("id"));
                roomRow.put("room_id", rSet.getInt("room_id"));
                roomRow.put("date_begin", rSet.getString("date_begin"));
                roomRow.put("date_end", rSet.getString("date_end"));

                orderDataList.add(roomRow);
            }
        } finally {
            if (rSet != null && !rSet.isClosed()) rSet.close();
            if (statement != null && !statement.isClosed()) statement.close();
            if (!conn.isClosed()) conn.close();
        }

        return orderDataList;
    }

    public static List<Map<String, Object>> requestGetAll() throws SQLException {
        List<Map<String, Object>> requestDataList = new ArrayList<Map<String, Object>>();

        Connection conn = getConnection();
        Statement statement1 = null;
        PreparedStatement statement2 = null;
        ResultSet rSet1 = null, rSet2 = null;

        try {
            statement1 = conn.createStatement();
            rSet1 = statement1.executeQuery("SELECT * FROM users_pending ORDER BY rowid");
            while (rSet1.next()) {
                Map<String, Object> userRow = new HashMap<String, Object>();
                int userId = rSet1.getInt("rowid");
                userRow.put("id", userId);
                userRow.put("name", rSet1.getString("name"));
                userRow.put("phone", rSet1.getString("phone"));
                userRow.put("time_from", rSet1.getInt("time_from"));
                userRow.put("time_to", rSet1.getInt("time_to"));
                userRow.put("date_check_in", rSet1.getString("date_check_in"));
                userRow.put("date_check_out", rSet1.getString("date_check_out"));

                statement2 = conn.prepareStatement("SELECT * FROM rooms_pending WHERE user_id = (?)");
                statement2.setInt(1, userId);
                rSet2 = statement2.executeQuery();

                List<Map<String, Object>> orderedRoomData = new ArrayList<Map<String, Object>>();
                while (rSet2.next()) {
                    Map<String, Object> roomRow = new HashMap<String, Object>();
                    roomRow.put("roomType", rSet2.getString("room_type"));
                    roomRow.put("roomsCount", rSet2.getInt("rooms_count"));
                    roomRow.put("adultsCount", rSet2.getInt("adult_count"));
                    roomRow.put("child_3_10_count", rSet2.getInt("child_3_10_count"));
                    roomRow.put("child_3_count", rSet2.getInt("child_3_count"));

                    orderedRoomData.add(roomRow);
                }
                userRow.put("orderedRoomData", orderedRoomData);

                requestDataList.add(userRow);
            }
        } finally {
            if (rSet1 != null && !rSet1.isClosed()) rSet1.close();
            if (rSet2 != null && !rSet2.isClosed()) rSet2.close();
            if (statement1 != null && !statement1.isClosed()) statement1.close();
            if (statement2 != null && !statement2.isClosed()) statement2.close();
            if (!conn.isClosed()) conn.close();
        }

        return requestDataList;
    }

    private static void putRoomData(Map<String, Object> room, ResultSet rSet) throws SQLException {
        room.put("id", rSet.getInt("id"));
        room.put("capacity", rSet.getInt("capacity"));
        room.put("floor", rSet.getInt("floor"));
        room.put("room_type", rSet.getString("room_type"));
        room.put("description", rSet.getString("description"));
    }

    public static List<Map<String, Object>> roomSearchByRange(String strCheckIn, String strCheckOut) throws SQLException {
        List<Map<String, Object>> rooms = new ArrayList<Map<String, Object>>();

        Connection conn = getConnection();
        PreparedStatement pStatement;
        ResultSet rSet = null;

        // найти все пересечения дат в заказанных номерах по заданному диапазону
        /**
         * SELECT sum(room_types.capacity) AS capacity,
         *        count(*) AS COUNT,
         *        rooms.room_type,
         *        room_types.description
         * FROM rooms
         * INNER JOIN room_types ON rooms.room_type = room_types.type
         * WHERE NOT EXISTS
         *      (SELECT *
         *       FROM rooms_ordered
         *       WHERE (date_begin < ? // ? - конец диапазона поиска
         *              AND date_end > ?) // ? - начало диапазона поиска
         *          AND rooms_ordered.room_id = rooms.id)
         * GROUP BY rooms.room_type
         * ORDER BY room_types.id
         */
        pStatement = conn.prepareStatement(
                "SELECT sum(room_types.capacity) AS capacity, count(*) AS count, rooms.room_type, room_types.description " +
                        "FROM rooms INNER JOIN room_types ON rooms.room_type = room_types.type " +
                        "WHERE NOT EXISTS " +
                        "(SELECT * " +
                        "FROM rooms_ordered " +
                        "WHERE (date_begin < ? AND date_end > ?) " +
                        "AND rooms_ordered.room_id = rooms.id) " +
                        "GROUP BY rooms.room_type, room_types.description, room_types.id " +
                        "ORDER BY room_types.id");

        pStatement.setDate(1, Date.valueOf(strCheckOut));
        pStatement.setDate(2, Date.valueOf(strCheckIn));

        try {
            rSet = pStatement.executeQuery();
            while (rSet.next()) {
                Map<String, Object> cRoom = new HashMap<String, Object>();
                cRoom.put("capacity", rSet.getInt("capacity"));
                cRoom.put("count", rSet.getInt("count"));
                cRoom.put("room_type", rSet.getString("room_type"));
                cRoom.put("description", rSet.getString("description"));
                rooms.add(cRoom);
            }
        } finally {
            if (rSet != null && !rSet.isClosed()) rSet.close();
            if (!pStatement.isClosed()) pStatement.close();
            if (!conn.isClosed()) closeConnection(conn);
        }
        return rooms;
    }

    public static List<Map<String, Object>> priceGetAll() throws SQLException {
        List<Map<String, Object>> prices = new ArrayList<Map<String, Object>>();

        Connection conn = getConnection();
        Statement statement;
        ResultSet rSet = null;

        statement = conn.createStatement();

        try {
            rSet = statement.executeQuery("SELECT * FROM prices");

            while (rSet.next()) {
                Map<String, Object> row = new HashMap<String, Object>();
                row.put("room_type", rSet.getString("room_type"));
                row.put("may", rSet.getInt("may"));
                row.put("june", rSet.getInt("june"));
                row.put("july", rSet.getInt("july"));
                row.put("august", rSet.getInt("august"));
                row.put("september", rSet.getInt("september"));
                row.put("child_3_price", rSet.getInt("child_3_price"));
                row.put("child_3_10_discount", rSet.getInt("child_3_10_discount"));
                prices.add(row);
            }
        } finally {
            if (rSet != null && !rSet.isClosed()) rSet.close();
            if (statement != null && !statement.isClosed()) statement.close();
            if (!conn.isClosed()) closeConnection(conn);

        }
        return prices;
    }

    public static int orderAdd(int roomId, String dateBegin, String dateEnd) throws SQLException {

        Connection conn = getConnection();
        PreparedStatement pStatement;

        pStatement = conn.prepareStatement("INSERT INTO rooms_ordered (room_id, date_begin, date_end) SELECT ?,?,? FROM rooms WHERE id = ?");
        pStatement.setInt(1, roomId);
        pStatement.setDate(2, Date.valueOf(dateBegin));
        pStatement.setDate(3, Date.valueOf(dateEnd));
		pStatement.setInt(4, roomId);

        int res;
        try {
            res = pStatement.executeUpdate();
            if (res > 0) res = REQUEST_SUCCESS;
        } finally {
            if (!pStatement.isClosed()) pStatement.close();
            if (!conn.isClosed()) closeConnection(conn);
        }

        return res;
    }

    public static int requestAdd(String name, String phone, int timeFrom, int timeTo, String dateCheckIn, String dateCheckOut, List<OrderedRoomData> orderedRoomsData) throws SQLException {

        Connection conn = getConnection();
        PreparedStatement pStatement1 = null, pStatement2 = null;
        ResultSet rSet1 = null, rSet2 = null;

        int res;

        try {
            conn.setAutoCommit(false);

            pStatement1 = conn.prepareStatement("INSERT INTO users_pending (name, phone, time_from, time_to, date_check_in, date_check_out) VALUES (?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
            pStatement1.setString(1, name);
            pStatement1.setString(2, phone);
            pStatement1.setInt(3, timeFrom);
            pStatement1.setInt(4, timeTo);
            pStatement1.setDate(5, Date.valueOf(dateCheckIn));
            pStatement1.setDate(6, Date.valueOf(dateCheckOut));

            pStatement1.executeUpdate();

            rSet1 = pStatement1.getGeneratedKeys();
            long userId = rSet1.next() ? rSet1.getInt(1) : REQUEST_FAILED;

            if (userId == REQUEST_FAILED)
                throw new SQLException("Request failed. User not added.");

            for (int i = 0; i < orderedRoomsData.size(); i++) {
                pStatement2 = conn.prepareStatement("INSERT INTO rooms_pending (user_id, room_type, rooms_count, adult_count, child_3_10_count, child_3_count) VALUES (?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
                pStatement2.setLong(1, userId);
                pStatement2.setString(2, orderedRoomsData.get(i).roomType);
                pStatement2.setInt(3, orderedRoomsData.get(i).roomsCount);
                pStatement2.setInt(4, orderedRoomsData.get(i).adultsCount);
                pStatement2.setInt(5, orderedRoomsData.get(i).child_3_10_count);
                pStatement2.setInt(6, orderedRoomsData.get(i).child_3_count);

                pStatement2.executeUpdate();

                rSet2 = pStatement2.getGeneratedKeys();
                int r = rSet2.next() ? rSet2.getInt(1) : REQUEST_FAILED;
                if (r == REQUEST_FAILED)
                    throw new SQLException("Request failed. Order not added.");
            }

            conn.commit();
            res = REQUEST_SUCCESS;

        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            if (rSet1 != null && !rSet1.isClosed()) rSet1.close();
            if (rSet2 != null && !rSet2.isClosed()) rSet2.close();
            if (pStatement1 != null && !pStatement1.isClosed()) pStatement1.close();
            if (pStatement2 != null && !pStatement2.isClosed()) pStatement2.close();
            if (!conn.isClosed()) closeConnection(conn);
        }

        return res;
    }

    public static int requestDelete(int userId) throws SQLException {

        Connection conn = getConnection();
        PreparedStatement pStatement = null;

        int res;
        try {
            conn.setAutoCommit(false);

            pStatement = conn.prepareStatement("DELETE FROM users_pending WHERE rowid = (?)");
            pStatement.setInt(1, userId);
            res = pStatement.executeUpdate();

            pStatement = conn.prepareStatement("DELETE FROM rooms_pending WHERE user_id = (?)");
            pStatement.setInt(1, userId);
            res += pStatement.executeUpdate();

            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            if (pStatement != null && !pStatement.isClosed()) pStatement.close();
            if (conn != null && !conn.isClosed()) closeConnection(conn);
        }

        return res;
    }
}
