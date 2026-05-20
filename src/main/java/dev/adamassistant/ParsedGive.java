package dev.adamassistant;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public record ParsedGive(int count, String itemPhrase) {
    private static final Map<String, Integer> NUMBER_WORDS = new LinkedHashMap<>();

    static {
        NUMBER_WORDS.put("один", 1);
        NUMBER_WORDS.put("одна", 1);
        NUMBER_WORDS.put("одно", 1);
        NUMBER_WORDS.put("два", 2);
        NUMBER_WORDS.put("две", 2);
        NUMBER_WORDS.put("три", 3);
        NUMBER_WORDS.put("четыре", 4);
        NUMBER_WORDS.put("пять", 5);
        NUMBER_WORDS.put("шесть", 6);
        NUMBER_WORDS.put("семь", 7);
        NUMBER_WORDS.put("восемь", 8);
        NUMBER_WORDS.put("девять", 9);
        NUMBER_WORDS.put("десять", 10);
        NUMBER_WORDS.put("полстака", 32);
        NUMBER_WORDS.put("стак", 64);
        NUMBER_WORDS.put("стакан", 64);
        NUMBER_WORDS.put("стек", 64);
    }

    public static ParsedGive parse(String rawItemRequest) {
        String normalized = TextRu.normalize(rawItemRequest);
        int count = 1;

        String[] words = normalized.split(" ");
        if (words.length == 0) {
            return new ParsedGive(1, "");
        }

        ParseResult first = parseCountWord(words[0]);
        if (first.matched()) {
            count = first.count();
            normalized = removeFirstWord(normalized);
        } else {
            ParseResult last = parseCountWord(words[words.length - 1]);
            if (last.matched()) {
                count = last.count();
                normalized = removeLastWord(normalized);
            }
        }

        // Обработка формата "2 стака алмазов".
        words = normalized.split(" ");
        if (words.length >= 2 && isStackWord(words[0])) {
            count = Math.max(1, count) * 64;
            normalized = removeFirstWord(normalized);
        } else if (words.length >= 3 && isStackWord(words[1])) {
            ParseResult amountBeforeStack = parseCountWord(words[0]);
            if (amountBeforeStack.matched()) {
                count = amountBeforeStack.count() * 64;
                normalized = normalized.substring((words[0] + " " + words[1]).length()).trim();
            }
        }

        count = Math.max(1, Math.min(count, 2304));
        return new ParsedGive(count, normalized.trim());
    }

    private static ParseResult parseCountWord(String word) {
        String clean = word.toLowerCase(Locale.ROOT).replaceAll("[^0-9а-яa-z]", "");

        if (clean.matches("\\d+")) {
            return new ParseResult(true, Integer.parseInt(clean));
        }

        Integer known = NUMBER_WORDS.get(clean);
        return known == null ? new ParseResult(false, 1) : new ParseResult(true, known);
    }

    private static boolean isStackWord(String word) {
        String clean = word.toLowerCase(Locale.ROOT);
        return clean.equals("стак") || clean.equals("стака") || clean.equals("стаков") || clean.equals("стек") || clean.equals("стека");
    }

    private static String removeFirstWord(String text) {
        int firstSpace = text.indexOf(' ');
        return firstSpace < 0 ? "" : text.substring(firstSpace + 1).trim();
    }

    private static String removeLastWord(String text) {
        int lastSpace = text.lastIndexOf(' ');
        return lastSpace < 0 ? "" : text.substring(0, lastSpace).trim();
    }

    private record ParseResult(boolean matched, int count) {
    }
}
