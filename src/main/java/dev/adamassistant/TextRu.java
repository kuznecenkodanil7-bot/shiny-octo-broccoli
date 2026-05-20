package dev.adamassistant;

import java.text.Normalizer;
import java.util.Locale;

public final class TextRu {
    private TextRu() {
    }

    public static String normalize(String text) {
        if (text == null) {
            return "";
        }

        String normalized = Normalizer.normalize(text, Normalizer.Form.NFKC)
                .toLowerCase(Locale.ROOT)
                .replace('ё', 'е')
                .replace('-', ' ')
                .replace('_', ' ')
                .replaceAll("[\\p{Punct}&&[^:/]]", " ")
                .replaceAll("\\s+", " ")
                .trim();

        return normalized;
    }

    public static String normalizeKeepCommandChars(String text) {
        if (text == null) {
            return "";
        }

        return Normalizer.normalize(text, Normalizer.Form.NFKC)
                .toLowerCase(Locale.ROOT)
                .replace('ё', 'е')
                .replaceAll("\\s+", " ")
                .trim();
    }

    public static boolean containsAny(String text, String... needles) {
        for (String needle : needles) {
            if (text.contains(needle)) {
                return true;
            }
        }

        return false;
    }
}
