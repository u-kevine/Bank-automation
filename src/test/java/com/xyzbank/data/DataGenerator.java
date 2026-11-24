package com.xyzbank.data;

import java.util.concurrent.ThreadLocalRandom;

public final class DataGenerator {
    private DataGenerator() {
    }

    private static String letters(int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append((char) ('a' + ThreadLocalRandom.current().nextInt(26)));
        }
        return sb.toString();
    }

    public static Customer uniqueCustomer(String firstName, String lastName, String postCode) {
        return new Customer(firstName + letters(6), lastName + letters(4), postCode);
    }

    public static Customer uniqueCustomer(String postCode) {
        return uniqueCustomer("Test", "Customer", postCode);
    }

    public static Customer uniqueCustomer() {
        return uniqueCustomer("10001");
    }
}
