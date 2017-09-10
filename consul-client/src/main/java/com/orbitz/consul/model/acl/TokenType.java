package com.orbitz.consul.model.acl;

public enum TokenType {

    CLIENT("client"), MANAGEMENT("management");

    private String display;

    TokenType(String display) {
        this.display = display;
    }

    public String toDisplay() {
        return this.display;
    }
}
