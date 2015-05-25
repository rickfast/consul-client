package com.orbitz.consul.option;

public class CatalogOptions {

    private String datacenter;
    private String tag;

    public static CatalogOptions BLANK = new CatalogOptions();

    private CatalogOptions() {

    }

    CatalogOptions(String datacenter, String tag) {
        this.datacenter = datacenter;
        this.tag = tag;
    }

    public String getDatacenter() {
        return datacenter;
    }

    public String getTag() {
        return tag;
    }
}
