package com.orbitz.consul.option;

public class PutOptions {

    private Integer cas;
    private String acquire;
    private String release;

    public static PutOptions BLANK = new PutOptions(null, null, null);

    PutOptions(Integer cas, String acquire, String release) {
        this.cas = cas;
        this.acquire = acquire;
        this.release = release;
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
}
