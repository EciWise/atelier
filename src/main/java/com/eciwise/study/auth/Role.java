package com.eciwise.study.auth;

public enum Role {
    ESTUDIANTE("estudiante"),
    TUTOR("tutor"),
    ADMIN("admin");

    private final String value;

    Role(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
