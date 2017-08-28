package com.arny.flightlogbook.models;

import com.arny.arnylib.database.DBProvider;
import org.chalup.microorm.annotations.Column;
public class Flight {
    @Column("_id")
    private int id;
    @Column("datetime")
    private long datetime;
    @Column("log_time")
    private int logtime;
    @Column("reg_no")
    private String reg_no;
    private String airplanetypetitle;
    @Column("airplane_type")
    private int airplanetypeid;
    @Column("day_night")
    private int daynight;
    @Column("ifr_vfr")
    private int ifrvfr;
    @Column("flight_type")
    private int flighttype;
    @Column("description")
    private String description;

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

    @Override
    public String toString() {
        return DBProvider.getColumns(this);
    }
}
