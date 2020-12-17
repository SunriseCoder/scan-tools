package utils;

import java.text.DecimalFormat;

public class NumberUtils {

    public static String generateNumberByMaxNumber(int number, int maxNumber) {
        int length = MathUtils.ceilToInt(Math.log10(maxNumber));
        String result = generateNumberByLength(number, length);
        return result;
    }

    public static String generateNumberByLength(int number, int length) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.valueOf(number));

        while (sb.length() < length) {
            sb.insert(0, "0");
        }

        return sb.toString();
    }

    public static String humanReadableSize(long value) {
        double result = value;
        String arr[] = {"", "k", "M", "G", "T", "P", "E"};
        int index = 0;
        while ((result / 1024) >= 1) {
            result = result / 1024;
            index++;
        }
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        return String.format("%s %s", decimalFormat.format(result), arr[index]);
    }
}
