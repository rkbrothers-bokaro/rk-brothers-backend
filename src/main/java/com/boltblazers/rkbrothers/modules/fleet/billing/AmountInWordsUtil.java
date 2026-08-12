package com.boltblazers.rkbrothers.modules.fleet.billing;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Converts a rupee amount to Indian English words (lakh/crore grouping),
 * e.g. 48750.00 -> "Rupees Forty Eight Thousand Seven Hundred Fifty Only".
 */
public final class AmountInWordsUtil {

    private static final String[] ONES = {
            "Zero", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten",
            "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen", "Seventeen", "Eighteen", "Nineteen"
    };

    private static final String[] TENS = {
            "", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety"
    };

    private AmountInWordsUtil() {
    }

    public static String convert(BigDecimal amount) {
        BigDecimal normalized = (amount != null ? amount : BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);

        long rupees = normalized.longValue();
        int paise = normalized.subtract(BigDecimal.valueOf(rupees))
                .multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP)
                .intValue();

        StringBuilder result = new StringBuilder("Rupees ");
        result.append(rupees == 0 ? "Zero" : rupeesToWords(rupees));

        if (paise > 0) {
            result.append(" and ").append(belowHundred(paise)).append(" Paise");
        }

        result.append(" Only");
        return result.toString();
    }

    private static String rupeesToWords(long rupees) {
        long crore = rupees / 10000000;
        rupees %= 10000000;
        long lakh = rupees / 100000;
        rupees %= 100000;
        long thousand = rupees / 1000;
        rupees %= 1000;
        long hundred = rupees;

        StringBuilder parts = new StringBuilder();
        if (crore > 0) {
            appendPart(parts, belowThousand((int) crore) + " Crore");
        }
        if (lakh > 0) {
            appendPart(parts, belowHundred((int) lakh) + " Lakh");
        }
        if (thousand > 0) {
            appendPart(parts, belowHundred((int) thousand) + " Thousand");
        }
        if (hundred > 0) {
            appendPart(parts, belowThousand((int) hundred));
        }
        return parts.toString();
    }

    private static void appendPart(StringBuilder builder, String part) {
        if (builder.length() > 0) {
            builder.append(' ');
        }
        builder.append(part);
    }

    private static String belowThousand(int n) {
        if (n < 100) {
            return belowHundred(n);
        }
        String remainder = belowHundred(n % 100);
        return ONES[n / 100] + " Hundred" + (n % 100 != 0 ? " " + remainder : "");
    }

    private static String belowHundred(int n) {
        if (n < 20) {
            return ONES[n];
        }
        String remainder = n % 10 != 0 ? " " + ONES[n % 10] : "";
        return TENS[n / 10] + remainder;
    }
}
