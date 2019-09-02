package app;

import java.util.Random;

public class PasswordGenerator {
    private static final String[] SYMBOLS = new String[] {
            "2346789", "ABCDEFGHJKLM", "NPQRSTUVWXYZ", "abcdefkmn", "pstuvwxyz", "#$%&,./-+=" };

    private Random random;

    public PasswordGenerator() {
        random = new Random();
    }

    public String generatePassword(int length) {
        StringBuilder password = new StringBuilder();

        while (password.length() < length) {
            int categoryIndex = random.nextInt(SYMBOLS.length);
            String category = SYMBOLS[categoryIndex];

            int symbolIndex = random.nextInt(category.length());
            String symbol = category.substring(symbolIndex, symbolIndex + 1);
            password.append(symbol);
        }

        return password.toString();
    }

    public void randomize() {
        random.nextInt();
    }
}
