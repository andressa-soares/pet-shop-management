package com.br.pet_shop_management.util;

public final class PhoneUtils {
    private PhoneUtils() {}

    public static String normalize(String phone) {
        if (phone == null) return null;
        return phone.replaceAll("\\D", "");
    }

    public static boolean hasValidLength(String phoneDigits) {
        return phoneDigits != null && (phoneDigits.length() == 10 || phoneDigits.length() == 11);
    }

    public static String format(String phoneDigits) {
        if (phoneDigits == null) return null;

        if (phoneDigits.length() == 10) {
            return "(" + phoneDigits.substring(0, 2) + ") " +
                    phoneDigits.substring(2, 6) + "-" +
                    phoneDigits.substring(6, 10);
        }

        if (phoneDigits.length() == 11) {
            return "(" + phoneDigits.substring(0, 2) + ") " +
                    phoneDigits.substring(2, 7) + "-" +
                    phoneDigits.substring(7, 11);
        }

        return phoneDigits;
    }
}