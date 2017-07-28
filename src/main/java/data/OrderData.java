package main.java.data;

import main.java.data.OrderedRoomData;

import java.util.ArrayList;
import java.util.List;

public class OrderData {
    public int id;
    public String name;
    public String phone;
    public int time_from;
    public int time_to;
    public List<OrderedRoomData> orderedRoomData = new ArrayList<OrderedRoomData>();
}
