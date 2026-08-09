package com.fakemoney.scoreboard.client;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses plain numbers and common magnitude suffixes without using long,
 * allowing values far larger than Java primitive integer limits.
 */
public final class MoneyParser {
    private static final Pattern NUMBER =
            Pattern.compile("^\\$?([+-]?[0-9][0-9,]*(?:\\.[0-9]+)?)\\s*([A-Za-z]*)$");

    private MoneyParser() {}

    public static final class ParseException extends RuntimeException {
        public ParseException(String message) {
            super(message);
        }
    }

    public static BigDecimal toNumber(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new ParseException("Enter a number.");
        }

        Matcher matcher = NUMBER.matcher(raw.trim());
        if (!matcher.matches()) {
            throw new ParseException("Invalid number. Examples: 50, 1,000, 2.5M, 50B, 1.25T.");
        }

        String digits = matcher.group(1).replace(",", "");
        String suffix = matcher.group(2).toUpperCase(Locale.ROOT);

        BigDecimal value;
        try {
            value = new BigDecimal(digits);
        } catch (NumberFormatException e) {
            throw new ParseException("Invalid number.");
        }

        int power = suffixPower(suffix);
        if (power < 0) {
            throw new ParseException("Unknown suffix: " + suffix);
        }

        return value.scaleByPowerOfTen(power);
    }

    public static String formatNumber(BigDecimal value) {
        if (value == null) return "";
        if (value.signum() == 0) return "0";

        BigDecimal normalized = value.stripTrailingZeros();

        // Use suffixes for readable large values while retaining arbitrary precision.
        String[] suffixes = {"", "K", "M", "B", "T", "Qa", "Qi", "Sx", "Sp", "Oc", "No", "Dc"};
        int group = 0;

        BigDecimal abs = normalized.abs();
        while (abs.compareTo(BigDecimal.valueOf(1000)) >= 0 && group < suffixes.length - 1) {
            abs = abs.movePointLeft(3);
            group++;
        }

        BigDecimal display = normalized.movePointLeft(group * 3).stripTrailingZeros();
        if (group == 0) {
            return display.setScale(Math.max(0, display.scale()), RoundingMode.DOWN).toPlainString();
        }

        return display.toPlainString() + suffixes[group];
    }

    private static int suffixPower(String suffix) {
        return switch (suffix) {
            case "" -> 0;
            case "K" -> 3;
            case "M" -> 6;
            case "B" -> 9;
            case "T" -> 12;
            case "QA" -> 15;
            case "QI" -> 18;
            case "SX" -> 21;
            case "SP" -> 24;
            case "OC" -> 27;
            case "NO" -> 30;
            case "DC" -> 33;
            default -> -1;
        };
    }
}
