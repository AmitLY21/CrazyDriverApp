package com.example.crazydriver.callbacks;


import com.example.crazydriver.objects.Record;

import java.util.ArrayList;

public interface CallBack_List {
    ArrayList<Record> getRecords();
    void ZoomOnMap(double lat, double lon);
}

