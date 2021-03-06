package com.example.invoicetextdetector;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class DataClass {

    public DataClass() {
    }

    private List<Pradrclass> pradr;
    private String tradeNam;
    private String sts;
    private String lstupdt;
    private String rgdt;
    private String dty;


    public DataClass(List<Pradrclass> pradr, String tradeNam, String sts, String lstupdt,String rgdt, String dty) {
        this.pradr = pradr;
        this.tradeNam = tradeNam;
        this.sts = sts;
        this.lstupdt = lstupdt;
        this.rgdt=rgdt;
        this.dty=dty;
    }

    public List<Pradrclass> getPradr() {
        return pradr;
    }

    public void setPradr(List<Pradrclass> pradr) {
        this.pradr = pradr;
    }

    public String getTradeNam() {
        return tradeNam;
    }

    public void setTradeNam(String tradeNam) {
        this.tradeNam = tradeNam;
    }

    public String getSts() {
        return sts;
    }

    public void setSts(String sts) {
        this.sts = sts;
    }

    public String getLstupdt() {
        return lstupdt;
    }

    public void setLstupdt(String lstupdt) {
        this.lstupdt = lstupdt;
    }

    public String getRgdt() {
        return rgdt;
    }

    public void setRgdt(String rgdt) {
        this.rgdt = rgdt;
    }

    public String getDty() {
        return dty;
    }

    public void setDty(String dty) {
        this.dty = dty;
    }
}
