package main.java;

import main.java.Database.SQLiteClass;
import main.java.data.OrderedRoomData;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Path("/v1")
public class ApiServiceV1 {

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/device.add")
    public Response deviceAdd(final String strRequest) throws SQLException, ClassNotFoundException {
        String pseudoId;
        String phoneNum;
        String name;
        try {
            JSONObject request = new JSONObject(strRequest);
            pseudoId = request.getString("pseudo_id");
            phoneNum = request.getString("phone_num");
            name = request.getString("name");
        } catch (JSONException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("{\"error\":\"Invalid JSON data.\"}").build();
        }

        JSONObject jsonEntity = new JSONObject();
        Response.ResponseBuilder response;
        switch (SQLiteClass.deviceAdd(pseudoId, phoneNum, name)) {
            case 1:
                jsonEntity.put("info", "Device registered.");
                response = Response.ok();
                break;
            case 2:
                response = Response.notModified();
                jsonEntity.put("error", "Device already exists.");
                break;
            default:
                response = Response.serverError();
                jsonEntity.put("error", "Device not created.");
        }
        return response.entity(jsonEntity.toString()).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/info")
    public Response info() {
        JSONObject jsonObject = new JSONObject();

		String version = null;
		try {
			version = SQLiteClass.version();
		} catch (SQLException e) {
            e.printStackTrace();
            return Response.serverError().entity("{\"error\":\"" + e.getMessage() + "\"}").build();
        }
		
		jsonObject.put("v", version);

        return Response.status(Response.Status.OK).entity(jsonObject.toString()).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/device.getAll")
    public Response deviceGetAll() {
        JSONObject response = new JSONObject();
        List<Map<String, Object>> users;
        try {
            users = SQLiteClass.deviceGetAll();
        } catch (SQLException e) {
            e.printStackTrace();
            return Response.serverError().entity("{\"error\":\"" + e.getMessage() + "\"}").build();
        }

		// todo make json array without "users"
        for (Map<String, Object> user : users) {
            response.append("users", user);
        }
        return Response.ok(response.toString()).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/request.getAll")
    public Response orderGetAll() {
        List<Map<String, Object>> orderDataList;

        try {
            orderDataList = SQLiteClass.orderGetAll();
        } catch (SQLException e) {
            e.printStackTrace();
            return Response.serverError().entity("{\"error\":\"" + e.getMessage() + "\"}").build();
        }

        JSONArray response = new JSONArray(orderDataList);

        if (orderDataList.isEmpty()) {
            return Response.noContent().build();
        } else
            return Response.ok(response.toString()).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/room.searchByRange")
    public Response roomSearchByRange(@QueryParam("check_in") final String checkIn, @QueryParam("check_out") final String checkOut) {
        List<Map<String, Object>> rooms;
        try {
            rooms = SQLiteClass.roomSearchByRange(checkIn, checkOut);
        } catch (SQLException e) {
            e.printStackTrace();
            return Response.serverError().entity("{\"error\":\"" + e.getMessage() + "\"}").build();
        }

        JSONArray response = new JSONArray(rooms);

        if (rooms.isEmpty())
            return Response.noContent().build();
        else
            return Response.ok(response.toString()).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/price.getAll")
    public Response priceGetAll()  {
        List<Map<String, Object>> prices;
        try {
            prices = SQLiteClass.priceGetAll();
        } catch (SQLException e) {
            e.printStackTrace();
            return Response.serverError().entity("{\"error\":\"" + e.getMessage() + "\"}").build();
        }

        JSONArray response = new JSONArray(prices);

        if (prices.isEmpty())
            return Response.noContent().build();
        else
            return Response.ok(response.toString()).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/request.add")
    public Response requestAdd(final String strRequest) {
        String name;
        String phone;
        int timeFrom;
        int timeTo;
        String dateCheckIn;
        String dateCheckOut;
        List<OrderedRoomData> orderedRoomsData = new ArrayList<OrderedRoomData>();

        // разобрать данные по переменным
        try {
            JSONObject order = new JSONObject(strRequest);
            name = order.getString("name");
            phone = order.getString("phone");
            timeFrom = order.getInt("time_from");
            timeTo = order.getInt("time_to");
            dateCheckIn = order.getString("date_check_in");
            dateCheckOut = order.getString("date_check_out");
            JSONArray roomsData = order.getJSONArray("orderedRoomsData");
            for (int i = 0; i < roomsData.length(); i++) {
                JSONObject row = roomsData.getJSONObject(i);
                OrderedRoomData roomData = new OrderedRoomData();
                roomData.roomType = row.getString("roomType");
                roomData.roomsCount = row.getInt("roomsCount");
                roomData.adultsCount = row.getInt("adultsCount");
                roomData.child_3_count = row.getInt("child_3");
                roomData.child_3_10_count = row.getInt("child_3_10");
                orderedRoomsData.add(roomData);
            }
        } catch (JSONException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("{\"error\":\"Invalid JSON data.\"}").build();
        }

        // добавить заказ в базу
        try {
            SQLiteClass.requestAdd(name, phone, timeFrom, timeTo, dateCheckIn, dateCheckOut, orderedRoomsData);
        } catch (SQLException e) {
            e.printStackTrace();
            return Response.serverError().entity("{\"error\":\"" + e.getMessage() + "\"}").build();
        }

        return Response.ok("{\"info\":\"Order added.\"}").build();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/order.add")
    public Response orderAdd(final String strRequest) {
        String userName;
        int roomId;
        String dateBegin;
        String dateEnd;

        try {
            JSONObject request = new JSONObject(strRequest);
            userName = request.getString("user_name");
            roomId = request.getInt("room_id");
            dateBegin = request.getString("date_begin");
            dateEnd = request.getString("date_end");
        } catch (JSONException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("{\"error\":\"Invalid JSON data.\"}").build();
        }

        try {
            if(SQLiteClass.orderAdd(userName, roomId, dateBegin, dateEnd) > 0)
                return Response.ok("{\"info\":\"Order added.\"}").build();
            else
                return Response.ok("{\"info\":\"Order not added.\"}").build();
        } catch (SQLException e) {
            return Response.serverError().entity("{\"error\":\"" + e.getMessage() + "\"}").build();
        }
    }

    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/request.delete")
    public Response requestDelete(final String strRequest) {
        int userId;

        try {
            JSONObject request = new JSONObject(strRequest);
            userId = request.getInt("user_id");
        } catch (JSONException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("{\"error\":\"Invalid JSON data.\"}").build();
        }

        int res;
        try {
            res = SQLiteClass.requestDelete(userId);
        } catch (SQLException e) {
            e.printStackTrace();
            return Response.serverError().entity("{\"error\":\"" + e.getMessage() + "\"}").build();
        }

        if (res > 0)
            return Response.ok("{\"info\":\"Request deleted.\"}").build();
        else
            return Response.ok("{\"info\":\"Nothing to delete.\"}").build();
    }
}
