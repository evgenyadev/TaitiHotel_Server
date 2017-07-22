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

//    @PUT
//    @Consumes(MediaType.APPLICATION_JSON)
//    @Produces(MediaType.APPLICATION_JSON)
//    @Path("/user.updateInfo")
//    public Response userUpdateInfo(final String strRequest) throws SQLException, ClassNotFoundException {
//        String pseudoId;
//        String phoneNum;
//        String name;
//        try {
//            JSONObject request = new JSONObject(strRequest);
//            phoneNum = request.getString("phone_num");
//            pseudoId = request.getString("pseudo_id");
//            name = request.getString("name");
//        } catch (JSONException e) {
//            return Response.status(Response.Status.OK).entity("{\"error\":\"Invalid JSON data.\"}").build();
//        }
//
//        JSONObject jsonEntity = new JSONObject();
//        if (SQLiteClass.userUpdateInfo(pseudoId, phoneNum, name) == 1) {
//            jsonEntity.put("pseudo_id", pseudoId);
//            jsonEntity.put("phone_num", phoneNum);
//            jsonEntity.put("name", name);
//        } else
//            jsonEntity.put("error", "Data not updated.");
//
//        return Response.status(Response.Status.OK).entity(jsonEntity.toString()).build();
//    }

//    @GET
//    @Produces(MediaType.APPLICATION_JSON)
//    @Path("/user.getInfo")
//    public Response userGetInfo(@QueryParam("pseudo_id") final String pseudoId) throws SQLException, ClassNotFoundException {
//        Map<String, Object> user = SQLiteClass.userGetInfo(pseudoId);
//        JSONObject jsonObject = new JSONObject(user);
//
//        if (user.isEmpty())
//            jsonObject.put("error", "No such user or data is empty.");
//
//        return Response.status(Response.Status.OK).entity(jsonObject.toString()).build();
//    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/user.getAll")
    public Response userGetAll() {
        JSONObject response = new JSONObject();
        List<Map<String, Object>> users;
        try {
            users = SQLiteClass.userGetAll();
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            return Response.serverError().entity("{\"error\":\"" + e.getMessage() + "\"}").build();
        }
        for (Map<String, Object> user : users) {
            response.append("users", user);
        }
        return Response.ok(response.toString()).build();
    }

//    @GET
//    @Produces(MediaType.APPLICATION_JSON)
//    @Path("/room.getRoom")
//    public Response roomGetRoom(@QueryParam("id") final int id) throws SQLException, ClassNotFoundException {
//        Map<String, Object> room = SQLiteClass.roomGetRoom(id);
//        JSONObject response = new JSONObject(room);
//
//        if (room.isEmpty()) response.put("error", "No such room.");
//
//        return Response.status(Response.Status.OK).entity(response.toString()).build();
//    }

//    @GET
//    @Produces(MediaType.APPLICATION_JSON)
//    @Path("/room.getAll")
//    public Response roomGetAll() throws SQLException, ClassNotFoundException {
//        JSONObject response = new JSONObject();
//        List<Map<String, Object>> rooms = SQLiteClass.roomGetAll();
//
//        for (Map<String, Object> room : rooms) {
//            response.append("rooms", room);
//        }
//
//        return Response.status(Response.Status.OK).entity(response.toString()).build();
//    }

//    @Deprecated
//    @PUT
//    @Produces(MediaType.APPLICATION_JSON)
//    @Consumes(MediaType.APPLICATION_JSON)
//    @Path("/room.updateParameters")
//    public Response roomUpdateParameters(final String strRequest) throws SQLException, ClassNotFoundException {
//        //region проверка входных данных
//        List<String> allowedParams = Arrays.asList("has_air_condition", "has_fridge", "has_tv");
//        HashMap<String, Boolean> mapParams = new HashMap<>();
//        int id;
//        try {
//            JSONObject request = new JSONObject(strRequest);
//
//            id = request.getInt("id");
//            JSONObject reqParams = request.getJSONObject("params");
//            // все ли принятые параметры есть в списке разрешенных
//            for (Object parameter : reqParams.keySet()) {
//                if (allowedParams.contains((String) parameter))
//                    mapParams.put((String) parameter, reqParams.getBoolean((String) parameter));
//                else
//                    return Response.status(Response.Status.OK).entity("{\"error\":\"Invalid JSON data.\"}").build();
//            }
//
//        } catch (JSONException e) {
//            return Response.status(Response.Status.OK).entity("{\"error\":\"Invalid JSON data.\"}").build();
//        }
//        //endregion
//        //region запрос и обработка ответа
//        JSONObject jsonEntity = new JSONObject();
//        if (SQLiteClass.roomUpdateParameters(id, mapParams) == 1) {
//            jsonEntity.put("id", id);
//            jsonEntity.put("params", mapParams);
//        } else
//            jsonEntity.put("error", "Data not updated.");
//        //endregion
//        return Response.status(Response.Status.OK).entity(jsonEntity.toString()).build();
//    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/room.searchByRange")
    public Response roomSearchByRange(@QueryParam("check_in") final String checkIn, @QueryParam("check_out") final String checkOut) {
        List<Map<String, Object>> rooms;
        try {
            rooms = SQLiteClass.roomSearchByRange(checkIn, checkOut);
        } catch (SQLException | ClassNotFoundException e) {
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
        } catch (SQLException | ClassNotFoundException e) {
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
    @Path("/order.add")
    public Response orderAdd(final String strRequest) {
        String name;
        String phone;
        int time_from;
        int time_to;
        List<OrderedRoomData> orderedRoomsData = new ArrayList<>();

        // разобрать данные по переменным
        try {
            JSONObject order = new JSONObject(strRequest);
            name = order.getString("name");
            phone = order.getString("phone");
            time_from = order.getInt("time_from");
            time_to = order.getInt("time_to");
            JSONArray roomsData = order.getJSONArray("orderedRoomsData");
            for (int i = 0; i < roomsData.length(); i++) {
                JSONObject row = roomsData.getJSONObject(i);
                OrderedRoomData roomData = new OrderedRoomData();
                roomData.roomType = row.getString("roomType");
                roomData.roomsCount = row.getInt("roomsCount");
                roomData.adultsCount = row.getInt("adultsCount");
                roomData.child_3 = row.getInt("child_3");
                roomData.child_3_10 = row.getInt("child_3_10");
                orderedRoomsData.add(roomData);
            }
        } catch (JSONException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("{\"error\":\"Invalid JSON data.\"}").build();
        }

        // добавить заказ в базу
        try {
            SQLiteClass.orderAdd(name, phone, time_from, time_to, orderedRoomsData);
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            return Response.serverError().entity("{\"error\":\"" + e.getMessage() + "\"}").build();
        }

        return Response.ok("{\"info\":\"Order added.\"}").build();
    }
}


