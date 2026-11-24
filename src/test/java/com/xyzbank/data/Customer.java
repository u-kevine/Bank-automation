package com.xyzbank.data;

public record Customer(String firstName, String lastName, String postCode) {
    public String fullName() {
        return firstName + " " + lastName;
    }
}
