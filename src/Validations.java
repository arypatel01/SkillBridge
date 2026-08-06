import Util.InputUtil;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

class Validations{
    boolean isValidEmail(String email) {

        if (email == null || email.isEmpty()) {
            return false;
        }

        int atIndex = email.indexOf('@');
        int lastAtIndex = email.lastIndexOf('@');

        // There should be exactly one '@'
        if (atIndex == -1 || atIndex != lastAtIndex) {
            return false;
        }

        // '@' should not be the first or last character
        if (atIndex == 0 || atIndex == email.length() - 1) {
            return false;
        }

        // Find '.' after '@'
        int dotIndex = email.indexOf('.', atIndex);

        // '.' must exist after '@'
        if (dotIndex == -1) {
            return false;
        }

        // '.' should not be immediately after '@'
        if (dotIndex == atIndex + 1) {
            return false;
        }

        // At least 2 characters after '.'
        if (dotIndex >= email.length() - 2) {
            return false;
        }

        return true;
    }
    boolean isValidName(String name) {

        if (name == null) {
            return false;
        }

        name = name.trim();

        if (name.length() < 2) {
            return false;
        }

        for (int i = 0; i < name.length(); i++) {

            char ch = name.charAt(i);

            if (!Character.isLetter(ch) && ch != ' ') {
                return false;
            }
        }

        return true;
    }
    String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");

            byte[] hashBytes = md.digest(password.getBytes());

            StringBuilder hash = new StringBuilder();

            for (byte b : hashBytes) {
                hash.append(String.format("%02x", b));
            }

            return hash.toString();

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
    public boolean isValidPhoneNumber(String phoneNumber) {

        // Check null
        if (phoneNumber == null) {
            return false;
        }

        // Remove leading and trailing spaces
        phoneNumber = phoneNumber.trim();

        // Check length
        if (phoneNumber.length() != 10) {
            return false;
        }

        // Check first digit (must be 6,7,8,9)
        char firstDigit = phoneNumber.charAt(0);

        if (firstDigit != '6' &&
                firstDigit != '7' &&
                firstDigit != '8' &&
                firstDigit != '9') {

            return false;
        }

        // Check every character is a digit
        for (int i = 0; i < phoneNumber.length(); i++) {

            char ch = phoneNumber.charAt(i);

            if (ch < '0' || ch > '9') {
                return false;
            }
        }

        return true;
    }
}