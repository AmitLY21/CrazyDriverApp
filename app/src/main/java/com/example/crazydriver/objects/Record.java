package com.example.crazydriver.objects;

public class Record {
    private String time;
    private int score;
    private double lat;
    private double lon;

    public Record() {
    }

    public String getTime() {
        return time;
    }

    public Record setTime(String time) {
        this.time = time;
        return this;
    }

    @Override
    public String toString() {
        return "Time: " + time +
                "\nScore:" + score +
                "           [ " + lat +
                ", " + lon + " ]";
    }

    public int getScore() {
        return score;
    }

    public Record setScore(int score) {
        this.score = score;
        return this;
    }

    public double getLat() {
        return lat;
    }

    public Record setLat(double lat) {
        this.lat = lat;
        return this;
    }

    public double getLon() {
        return lon;
    }

    public Record setLon(double lon) {
        this.lon = lon;
        return this;
    }

}

