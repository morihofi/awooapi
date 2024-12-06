package net.fuxle.awooapi.component.cryptography.utils;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PasswordGenerator {


    // Inner Builder class
    public static class Builder {
        private int length = 12; // Default length
        private boolean includeUppercase = true;
        private boolean includeLowercase = true;
        private boolean includeDigits = true;
        private boolean includeSpecialChars = true;

        public Builder setLength(int length) {
            if (length < 6) {
                throw new IllegalArgumentException("Password length must be at least 6 characters.");
            }
            this.length = length;
            return this;
        }

        public Builder includeUppercase(boolean includeUppercase) {
            this.includeUppercase = includeUppercase;
            return this;
        }

        public Builder includeLowercase(boolean includeLowercase) {
            this.includeLowercase = includeLowercase;
            return this;
        }

        public Builder includeDigits(boolean includeDigits) {
            this.includeDigits = includeDigits;
            return this;
        }

        public Builder includeSpecialChars(boolean includeSpecialChars) {
            this.includeSpecialChars = includeSpecialChars;
            return this;
        }

        public PasswordGenerator build() {
            return new PasswordGenerator(this);
        }
    }

    private final int length;
    private final boolean includeUppercase;
    private final boolean includeLowercase;
    private final boolean includeDigits;
    private final boolean includeSpecialChars;

    private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS = "0123456789";
    private static final String SPECIAL_CHARS = "!@#$%^&*()-_=+[]{}|;:,.<>?/";

    private PasswordGenerator(Builder builder) {
        this.length = builder.length;
        this.includeUppercase = builder.includeUppercase;
        this.includeLowercase = builder.includeLowercase;
        this.includeDigits = builder.includeDigits;
        this.includeSpecialChars = builder.includeSpecialChars;
    }

    public String generate() {
        if (!includeUppercase && !includeLowercase && !includeDigits && !includeSpecialChars) {
            throw new IllegalStateException("At least one character type must be included.");
        }

        StringBuilder charPool = new StringBuilder();
        if (includeUppercase) charPool.append(UPPERCASE);
        if (includeLowercase) charPool.append(LOWERCASE);
        if (includeDigits) charPool.append(DIGITS);
        if (includeSpecialChars) charPool.append(SPECIAL_CHARS);

        SecureRandom random = new SecureRandom();
        List<Character> password = new ArrayList<>();

        // Ensure at least one character of each selected type
        if (includeUppercase) password.add(UPPERCASE.charAt(random.nextInt(UPPERCASE.length())));
        if (includeLowercase) password.add(LOWERCASE.charAt(random.nextInt(LOWERCASE.length())));
        if (includeDigits) password.add(DIGITS.charAt(random.nextInt(DIGITS.length())));
        if (includeSpecialChars) password.add(SPECIAL_CHARS.charAt(random.nextInt(SPECIAL_CHARS.length())));

        // Fill the remaining length with random characters
        while (password.size() < length) {
            password.add(charPool.charAt(random.nextInt(charPool.length())));
        }

        // Shuffle the password to ensure randomness
        Collections.shuffle(password, random);

        // Convert list to string
        StringBuilder result = new StringBuilder();
        for (char c : password) {
            result.append(c);
        }
        return result.toString();
    }

}
