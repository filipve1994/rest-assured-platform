package com.framework.utils;

import net.datafaker.Faker;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Central place for generating realistic-looking random test data, so tests
 * don't collide on unique fields (emails, usernames, ids) across parallel
 * runs and don't hardcode "John Doe" everywhere.
 */
public final class RandomDataUtils {

    private static final ThreadLocal<Faker> FAKER = ThreadLocal.withInitial(Faker::new);

    private RandomDataUtils() {
    }

    public static Faker faker() {
        return FAKER.get();
    }

    public static String uniqueEmail() {
        return "test.%s@example.com".formatted(UUID.randomUUID().toString().substring(0, 8));
    }

    public static String firstName() {
        return faker().name().firstName();
    }

    public static String lastName() {
        return faker().name().lastName();
    }

    public static String fullName() {
        return faker().name().fullName();
    }

    public static String phoneNumber() {
        return faker().phoneNumber().cellPhone();
    }

    public static String streetAddress() {
        return faker().address().fullAddress();
    }

    public static String companyName() {
        return faker().company().name();
    }

    public static String uuid() {
        return UUID.randomUUID().toString();
    }

    public static int intBetween(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    public static String alphanumeric(int length) {
        return faker().regexify("[A-Za-z0-9]{" + length + "}");
    }

    public static String iban(String countryCode) {
        return faker().finance().iban(countryCode);
    }
}
