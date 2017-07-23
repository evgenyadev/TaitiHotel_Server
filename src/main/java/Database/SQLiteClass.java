package main.java.Database;

import main.java.data.OrderedRoomData;

import java.sql.*;
import java.util.*;

import static org.sqlite.core.Codes.SQLITE_CONSTRAINT;

public class SQLiteClass {

    public static final int REQUEST_FAILED = -1;
    public static final int REQUEST_SUCCESS = 1;

    private static Connection getConnection() throws SQLException, ClassNotFoundException {
        Class.forName("org.sqlite.JDBC");
        return DriverManager.getConnection("jdbc:sqlite:TaitiDB.db3");
    }

    private static void closeConnection(Connection conn) throws SQLException {
        conn.close();
    }

    public static int deviceAdd(String pseudoId, String phoneNum, String name) throws SQLException, ClassNotFoundException {

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
            System.err.println(e.getMessage());
            if (e.getErrorCode() == SQLITE_CONSTRAINT) {
                System.out.println("That pseudo_id is already exists.");
                res = 2;
            } else throw e;
        } finally {
            if (!pStatement.isClosed()) pStatement.close();
            if (!conn.isClosed()) closeConnection(conn);
        }
        return res;
    }

    @Deprecated
    public static int userUpdateInfo(String pseudoId, String phoneNum, String name) throws SQLException, ClassNotFoundException {

        Connection conn = getConnection();
        PreparedStatement pStatement;

        pStatement = conn.prepareStatement("UPDATE devices SET phone_num = (?), name = (?) WHERE pseudo_id = (?)");
        pStatement.setString(1, phoneNum);
        pStatement.setString(2, name);
        pStatement.setString(3, pseudoId);
        int res;
        try {
            res = pStatement.executeUpdate();
        } finally {
            if (!pStatement.isClosed()) pStatement.close();
            if (!conn.isClosed()) closeConnection(conn);
        }

        return res;
    }

    @Deprecated
    public static Map<String, Object> userGetInfo(String pseudoId) throws SQLException, ClassNotFoundException {

        Connection conn = getConnection();
        PreparedStatement pStatement;
        ResultSet rSet = null;

        pStatement = conn.prepareStatement("SELECT name,phone_num FROM devices WHERE pseudo_id = (?)");
        pStatement.setString(1, pseudoId);
        Map<String, Object> response = new HashMap<String, Object>();
        try {
            rSet = pStatement.executeQuery();
            if (rSet.next()) {
                response.put("name", rSet.getString("name"));
                response.put("phone_num", rSet.getString("phone_num"));
            }
        } finally {
            if (rSet != null && !rSet.isClosed()) rSet.close();
            if (!pStatement.isClosed()) pStatement.close();
            if (!conn.isClosed()) closeConnection(conn);
        }

        return response;
    }

    public static List<Map<String, Object>> userGetAll() throws SQLException, ClassNotFoundException {
        List<Map<String, Object>> users = new ArrayList<new HashMap<String, Object>>();

        Connection conn = getConnection();
        Statement statement = conn.createStatement();
        ResultSet rSet = null;

        try {
            rSet = statement.executeQuery("SELECT pseudo_id,name,phone_num FROM devices");
            while (rSet.next()) {
                Map<String, Object> cUser = new HashMap<>();
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

    @Deprecated
    private static void putRoomData(Map<String, Object> room, ResultSet rSet) throws SQLException {
        room.put("id", rSet.getInt("id"));
        room.put("capacity", rSet.getInt("capacity"));
        room.put("floor", rSet.getInt("floor"));
        room.put("room_type", rSet.getString("room_type"));
        room.put("description", rSet.getString("description"));
    }

    @Deprecated
    public static Map<String, Object> roomGetRoom(int id) throws SQLException, ClassNotFoundException {

        Connection conn = getConnection();
        PreparedStatement pStatement;
        ResultSet rSet = null;

        Map<String, Object> room = new HashMap<>();
        pStatement = conn.prepareStatement("SELECT rooms.id, room_types.capacity, rooms.floor, rooms.room_type, room_types.description\n" +
                "FROM rooms INNER JOIN room_types ON rooms.room_type = room_types.type WHERE rooms.id = (?)");
        pStatement.setInt(1, id);
        try {
            rSet = pStatement.executeQuery();
            if (rSet.next()) {
                putRoomData(room, rSet);
            }
        } finally {
            if (rSet != null && !rSet.isClosed()) rSet.close();
            if (!pStatement.isClosed()) pStatement.close();
            if (!conn.isClosed()) closeConnection(conn);
        }
        return room;
    }

    @Deprecated
    public static List<Map<String, Object>> roomGetAll() throws SQLException, ClassNotFoundException {

        Connection conn = getConnection();
        PreparedStatement pStatement;
        ResultSet rSet = null;

        pStatement = conn.prepareStatement("SELECT rooms.id, room_types.capacity, rooms.floor, rooms.room_type, room_types.description " +
                "FROM rooms INNER JOIN room_types ON rooms.room_type = room_types.type");
        List<Map<String, Object>> rooms = new ArrayList<>();
        try {
            rSet = pStatement.executeQuery();
            while (rSet.next()) {
                Map<String, Object> cRoom = new HashMap<>();
                putRoomData(cRoom, rSet);
                rooms.add(cRoom);
            }
        } finally {
            if (rSet != null && !rSet.isClosed()) rSet.close();
            if (pStatement != null && !pStatement.isClosed()) pStatement.close();
            if (!conn.isClosed()) closeConnection(conn);
        }
        return rooms;
    }

    @Deprecated
    public static int roomUpdateParameters(int id, Map<String, Boolean> params) throws SQLException, ClassNotFoundException {

        Connection conn = getConnection();
        PreparedStatement pStatement;

        StringBuilder sql = new StringBuilder("UPDATE rooms SET ");

        // TODO: переделать без итератора
        Iterator mapIterator = params.entrySet().iterator();
        while (mapIterator.hasNext()) {
            Map.Entry pair = (Map.Entry) mapIterator.next();
            sql.append(pair.getKey()).append(" = ");
            sql.append(pair.getValue().equals(true) ? "1" : "0");
            if (mapIterator.hasNext()) sql.append(",");
        }

        sql.append(" WHERE id = (?)");

        pStatement = conn.prepareStatement(sql.toString());
        pStatement.setInt(1, id);

        int res;
        try {
            res = pStatement.executeUpdate();
        } finally {
            if (!pStatement.isClosed()) pStatement.close();
            if (!conn.isClosed()) closeConnection(conn);
        }
        return res;
    }

    public static List<Map<String, Object>> roomSearchByRange(String strCheckIn, String strCheckOut) throws SQLException, ClassNotFoundException {
        List<Map<String, Object>> rooms = new ArrayList<>();

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
                        "GROUP BY rooms.room_type " +
                        "ORDER BY room_types.id");

        pStatement.setString(1, strCheckOut);
        pStatement.setString(2, strCheckIn);

        try {
            rSet = pStatement.executeQuery();
            while (rSet.next()) {
                Map<String, Object> cRoom = new HashMap<>();
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

    public static List<Map<String, Object>> priceGetAll() throws SQLException, ClassNotFoundException {
        List<Map<String, Object>> prices = new ArrayList<>();

        Connection conn = getConnection();
        Statement statement;
        ResultSet rSet = null;

        statement = conn.createStatement();

        try {
            rSet = statement.executeQuery("SELECT * FROM prices");

            while (rSet.next()) {
                Map<String, Object> row = new HashMap<>();
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

    public static int orderAdd(String name, String phone, int time_from, int time_to, List<OrderedRoomData> orderedRoomsData) throws SQLException, ClassNotFoundException {

        Connection conn = getConnection();
        PreparedStatement pStatement1 = null,pStatement2 = null;
        ResultSet rSet1 = null, rSet2 = null;

        int res;

        try {
            conn.setAutoCommit(false);

            pStatement1 = conn.prepareStatement("INSERT INTO users_pending (name, phone, time_from, time_to) VALUES (?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
            pStatement1.setString(1, name);
            pStatement1.setString(2, phone);
            pStatement1.setInt(3, time_from);
            pStatement1.setInt(4, time_to);

            pStatement1.executeUpdate();

            rSet1 = pStatement1.getGeneratedKeys();
            long userId = rSet1.next() ? rSet1.getInt(1) : REQUEST_FAILED;

            if (userId == REQUEST_FAILED)
                throw new SQLException("request failed");

            for (int i = 0; i < orderedRoomsData.size(); i++) {
                pStatement2 = conn.prepareStatement("INSERT INTO rooms_pending (user_id, room_type, rooms_count, adult_count, child_3_10_count, child_3_count) VALUES (?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
                pStatement2.setLong(1, userId);
                pStatement2.setString(2, orderedRoomsData.get(i).roomType);
                pStatement2.setInt(3, orderedRoomsData.get(i).roomsCount);
                pStatement2.setInt(4, orderedRoomsData.get(i).adultsCount);
                pStatement2.setInt(5, orderedRoomsData.get(i).child_3_10);
                pStatement2.setInt(6, orderedRoomsData.get(i).child_3);

                pStatement2.executeUpdate();

                rSet2 = pStatement2.getGeneratedKeys();
                int r = rSet1.next() ? rSet1.getInt(1) : REQUEST_FAILED;
                if (r == REQUEST_FAILED)
                    throw new SQLException("request failed");
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
            if (!conn.isClosed()) conn.close();
        }

        return res;
    }
}
