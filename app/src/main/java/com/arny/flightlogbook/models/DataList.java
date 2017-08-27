package com.arny.flightlogbook.models;

public class DataList {
    private int id;
    private long datetime;
    private int logtime;
    private String reg_no;
    private int airplanetype;
    private String airplanetypetitle;
    private int airplanetypeid;
    private int daynight;
    private int ifrvfr;
    private int flighttype;
    private String description;

    // Empty constructor
    public DataList(){

    }

    public int getId(){
        return this.id;
    }

    public void setId(int id){
        this.id = id;
    }

    public long getDatetime(){
        return this.datetime;
    }

    public void setDatetime(long datetime){
        this.datetime = datetime;
    }

    public int getLogtime(){
        return this.logtime;
    }

    public void setLogtime(int logtime){
        this.logtime = logtime;
    }

    public String getReg_no(){
        return this.reg_no;
    }

    public void setReg_no(String reg_no){
        this.reg_no = reg_no;
    }

    public int getAirplanetype(){
        return this.airplanetype;
    }

    public void setAirplanetype(int airplanetype){
        this.airplanetype = airplanetype;
    }

    public String getAirplanetypetitle(){
        return this.airplanetypetitle;
    }

    public void setAirplanetypetitle(String airplanetypetitle){
        this.airplanetypetitle = airplanetypetitle;
    }

    public int getDaynight(){
        return this.daynight;
    }

    public void setDaynight(int daynight){
        this.daynight = daynight;
    }

    public int getIfrvfr(){
        return this.ifrvfr;
    }

    public void setIfrvfr(int ifrvfr){
        this.ifrvfr = ifrvfr;
    }

    public int getFlighttype(){
        return this.flighttype;
    }

    public void setFlighttype(int flighttype){
        this.flighttype = flighttype;
    }

    public String getDescription(){
        return this.description;
    }

    public void setDescription(String description){
        this.description = description;
    }

    public void setAirplanetypeid(int airplanetypeid){
        this.airplanetypeid = airplanetypeid;
    }

    public int getAirplanetypeid(){
        return this.airplanetypeid;
    }
}
