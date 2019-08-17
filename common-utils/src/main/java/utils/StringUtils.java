package utils;

public class StringUtils {

    public static String trimEndSymbols(String text, String exclusion) {
        if (text == null || text.isEmpty() || exclusion == null || exclusion.isEmpty()) {
            return text;
        }

        while (text.endsWith(exclusion)) {
            text = text.substring(0, text.length() - exclusion.length());
        }

        return text;
    }
}
