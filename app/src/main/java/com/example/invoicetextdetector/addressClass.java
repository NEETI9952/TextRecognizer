package com.example.invoicetextdetector;

public class addressClass {


    private String bno;
    private String flno;
    private String st;
    private String loc;
    private String city;
    private String dst;
    private String stcd;
    private String pncd;

    public addressClass() {
    }

    public addressClass(String bno, String flno, String st, String loc, String city, String dst, String stcd, String pncd) {
        this.bno = bno;
        this.flno = flno;
        this.st = st;
        this.loc = loc;
        this.city = city;
        this.dst = dst;
        this.stcd = stcd;
        this.pncd = pncd;
    }


    public String getBno() {
        return bno;
    }

    public void setBno(String bno) {
        this.bno = bno;
    }

    public String getFlno() {
        return flno;
    }

    public void setFlno(String flno) {
        this.flno = flno;
    }

    public String getSt() {
        return this.st;
    }

    public void setSt(String st) {
        this.st = st;
    }

    public String getLoc() {
        return loc;
    }

    public void setLoc(String loc) {
        this.loc = loc;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getDst() {
        return dst;
    }

    public void setDst(String dst) {
        this.dst = dst;
    }

    public String getStcd() {
        return stcd;
    }

    public void setStcd(String stcd) {
        this.stcd = stcd;
    }

    public String getPncd() {
        return pncd;
    }

    public void setPncd(String pncd) {
        this.pncd = pncd;
    }


}
