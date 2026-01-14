package com.br.pet_shop_management.util;

public final class CpfUtils {
    private CpfUtils() {}

    public static String normalize(String cpf) {
        if (cpf == null) return null;
        return cpf.replaceAll("\\D", "");
    }

    public static boolean hasValidLength(String cpfDigits) {
        return cpfDigits != null && cpfDigits.length() == 11;
    }

    public static String format(String cpfDigits) {
        if (cpfDigits == null || cpfDigits.length() != 11) {
            return cpfDigits;
        }
        return cpfDigits.substring(0, 3) + "." +
                cpfDigits.substring(3, 6) + "." +
                cpfDigits.substring(6, 9) + "-" +
                cpfDigits.substring(9, 11);
    }
}
