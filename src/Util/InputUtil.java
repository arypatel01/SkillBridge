package Util;

import java.util.InputMismatchException;
import java.util.Scanner;

public class InputUtil {

    private static final Scanner scanner = new Scanner(System.in);

    private InputUtil() {}

    public static int readInt(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                int value = Integer.parseInt(scanner.nextLine().trim());
                return value;
            } catch (Exception e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
    }

    public static String readString(String prompt) {
        String input = "";
        while (input.isEmpty()) {
            System.out.print(prompt);
            input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                System.out.println("Input cannot be empty. Please try again.");
            }
        }
        return input;
    }

    public static Scanner getScanner() {

        return scanner;
    }
}
