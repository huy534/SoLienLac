package com.example.school.model;

public class TKB {
    private int id;
    private int maLop;
    private int maMon;
    private int thu;
    private int tiet;

    public TKB(int id, int maLop, int maMon, int thu, int tiet) {
        this.id = id;
        this.maLop = maLop;
        this.maMon = maMon;
        this.thu = thu;
        this.tiet = tiet;
    }

    public int getId(){return id;}
    public int getMaLop(){return maLop;}
    public int getMaMon(){return maMon;}
    public int getThu(){return thu;}
    public int getTiet(){return tiet;}
}
