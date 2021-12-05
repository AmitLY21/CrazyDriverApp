package com.example.crazydriver.objects;

import java.util.Comparator;

public class RecordComparator implements Comparator<Record> {

    // override the compare() method
    public int compare(Record r1, Record r2) {
        if (r1.getScore() == r2.getScore())
            return 0;
        else if (r1.getScore() > r2.getScore())
            return -1;
        else
            return 1;
    }
}
