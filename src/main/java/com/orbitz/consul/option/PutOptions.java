package com.orbitz.consul.option;

public class PutOptions {

    private Integer cas;
    private String acquire;
    private String release;
    private String dc;

    public static PutOptions BLANK = new PutOptions(null, null, null, null);

    PutOptions(Integer cas, String acquire, String release, String dc) {
        this.cas = cas;
        this.acquire = acquire;
        this.release = release;
        this.dc = dc;
    }

    public Integer getCas() {
        return cas;
    }

    public void setCas(Integer cas) {
        this.cas = cas;
    }

    public String getAcquire() {
        return acquire;
    }

    public void setAcquire(String acquire) {
        this.acquire = acquire;
    }

    public String getRelease() {
        return release;
    }

    public void setRelease(String release) {
        this.release = release;
    }

    public String getDc() {
        return dc;
    }

    public void setDc(String dc) {
        this.dc = dc;
    }

}
