package com.github.a5809909.mygpsapplication.model;

public class PhoneState {

    private int _id;
    private String time;

    private String mcc;
    private String mnc;

    private String radioType;

    private int numberOfCells;
    private int numberOfWifi;

    private int cellId;
    private int lac;
    private int signalStrength_0;

    private String cellInfo;
    private String wifiInfo;

    public String getWifiInfo() {
        return wifiInfo;
    }

    public void setWifiInfo(String pWifiInfo) {
        wifiInfo = pWifiInfo;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int id) {
        this._id = id;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getMcc() {
        return mcc;
    }

    public void setMcc(String mcc) {
        this.mcc = mcc;
    }

    public String getMnc() {
        return mnc;
    }

    public void setMnc(String mnc) {
        this.mnc = mnc;
    }

    public String getRadioType() {
        return radioType;
    }

    public void setRadioType(String pRadioType) {
        radioType = pRadioType;
    }

    public int getNumberOfCells() {
        return numberOfCells;
    }

    public void setNumberOfCells(int numberOfCells) {
        this.numberOfCells = numberOfCells;
    }

    public int getCellId() {
        return cellId;
    }

    public void setCellId(int cellId_0) {
        this.cellId = cellId_0;
    }

    public int getLac() {
        return lac;
    }

    public void setLac(int lac_0) {
        this.lac = lac_0;
    }

    public String getCellInfo() {
        return cellInfo;
    }

    public void setCellInfo(String pCellInfo) {
        cellInfo = pCellInfo;
    }

    public int getSignalStrength_0() {
        return signalStrength_0;
    }

    public void setSignalStrength_0(int signalStrength_0) {
        this.signalStrength_0 = signalStrength_0;
    }

    public int getNumberOfWifi() {
        return numberOfWifi;
    }

    public void setNumberOfWifi(int numberOfWifi) {
        this.numberOfWifi = numberOfWifi;
    }

}
