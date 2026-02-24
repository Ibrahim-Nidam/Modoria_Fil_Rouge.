package com.modoria.infrastructure.utils;

import java.text.Normalizer;
import java.util.Locale;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * Utility class for generating URL-friendly slugs.
 */
public final class SlugUtil {

    private static final Pattern NONLATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");
    private static final Pattern MULTIPLE_DASHES = Pattern.compile("-+");

    private SlugUtil() {
        // Utility class
    }

    /**
     * Generate a slug from a string.
     */
    public static String toSlug(String input) {
        if (input == null || input.isBlank()) {
            return "";
        }

        String noWhitespace = WHITESPACE.matcher(input).replaceAll("-");
        String normalized = Normalizer.normalize(noWhitespace, Normalizer.Form.NFD);
        String slug = NONLATIN.matcher(normalized).replaceAll("");
        slug = MULTIPLE_DASHES.matcher(slug).replaceAll("-");
        slug = slug.toLowerCase(Locale.ENGLISH);

        // Remove leading/trailing dashes
        slug = slug.replaceAll("^-+", "").replaceAll("-+$", "");

        return slug;
    }

    /**
     * Generate a unique slug by appending a number if necessary.
     */
    public static String generateUniqueSlug(String input, Predicate<String> existsChecker) {
        String baseSlug = toSlug(input);
        String slug = baseSlug;
        int counter = 1;

        while (existsChecker.test(slug)) {
            slug = baseSlug + "-" + counter;
            counter++;
        }

        return slug;
    }
}

