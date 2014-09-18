package com.orbitz.consul.option;

public class CatalogOptionsBuilder {

    private String datacenter;
    private String tag;

    private CatalogOptionsBuilder() {

    }

    public static CatalogOptionsBuilder builder() {
        return new CatalogOptionsBuilder();
    }

    public CatalogOptionsBuilder datacenter(String datacenter) {
        this.datacenter = datacenter;

        return this;
    }

    public CatalogOptionsBuilder tag(String tag) {
        this.tag = tag;

        return this;
    }

    public CatalogOptions build() {
        return new CatalogOptions(this.datacenter, this.tag);
    }
}
