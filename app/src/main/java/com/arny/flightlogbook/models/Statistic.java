package com.arny.flightlogbook.models;

public class Statistic {
    private int totalByMonth, cnt, daysTime, nightsTime, ifrTime, vfrTime, circleTime, zoneTime, marshTime;
    private long DT;
    private String strMoths, strTotalByMonths, dnTime, ivTime, czmTime;

    public int getTotalByMonth() {
        return totalByMonth;
    }

    public void setTotalByMonth(int totalByMonth) {
        this.totalByMonth = totalByMonth;
    }

    public int getCnt() {
        return cnt;
    }

    public void setCnt(int cnt) {
        this.cnt = cnt;
    }

    public long getDT() {
        return DT;
    }

    public void setDT(long DT) {
        this.DT = DT;
    }

    public String getStrMoths() {
        return strMoths;
    }

    public void setStrMoths(String strMoths) {
        this.strMoths = strMoths;
    }

    public String getStrTotalByMonths() {
        return strTotalByMonths;
    }

    public void setStrTotalByMonths(String strTotalByMonths) {
        this.strTotalByMonths = strTotalByMonths;
    }

    public int getDaysTime() {
        return daysTime;
    }

    public void setDaysTime(int daysTime) {
        this.daysTime = daysTime;
    }

    public int getNightsTime() {
        return nightsTime;
    }

    public void setNightsTime(int nightsTime) {
        this.nightsTime = nightsTime;
    }

    public int getIfrTime() {
        return ifrTime;
    }

    public void setIfrTime(int ifrTime) {
        this.ifrTime = ifrTime;
    }

    public int getVfrTime() {
        return vfrTime;
    }

    public void setVfrTime(int vfrTime) {
        this.vfrTime = vfrTime;
    }

    public int getCircleTime() {
        return circleTime;
    }

    public void setCircleTime(int circleTime) {
        this.circleTime = circleTime;
    }

    public int getZoneTime() {
        return zoneTime;
    }

    public void setZoneTime(int zoneTime) {
        this.zoneTime = zoneTime;
    }

    public int getMarshTime() {
        return marshTime;
    }

    public void setMarshTime(int marshTime) {
        this.marshTime = marshTime;
    }

    public String getDnTime() {
        return dnTime;
    }

    public void setDnTime(String dnTime) {
        this.dnTime = dnTime;
    }

    public String getIVTime() {
        return ivTime;
    }

    public void setIVTime(String ivTime) {
        this.ivTime = ivTime;
    }

    public String getCzmTime() {
        return czmTime;
    }

    public void setCzmTime(String czmTime) {
        this.czmTime = czmTime;
    }
}
