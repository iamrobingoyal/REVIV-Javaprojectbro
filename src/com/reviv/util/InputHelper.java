// src/com/reviv/util/InputHelper.java
package com.reviv.util;

import com.reviv.exception.InvalidInputException;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class InputHelper {
    // One Scanner for the whole program.
    private Scanner scanner;

    // Creates the scanner that reads from the keyboard.
    public InputHelper() {
        scanner = new Scanner(System.in);
    }

    // Reads one full line and trims it. Exits cleanly if the input is closed.
    public String readLine() {
        try {
            return scanner.nextLine().trim();
        } catch (NoSuchElementException e) {
            System.out.println("Input closed. Exiting REVIV.");
            System.exit(0);
            return "";
        }
    }

    // Shows the prompt and reads a whole number (used for menu choices).
    public int readInt(String prompt) throws InvalidInputException {
        System.out.print(prompt);
        String text = readLine();
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            throw new InvalidInputException("Please enter a valid whole number");
        }
    }

    // Shows the prompt and reads a number greater than zero.
    public int readPositiveInt(String prompt) throws InvalidInputException {
        int value = readInt(prompt);
        if (value <= 0) {
            throw new InvalidInputException("Number must be greater than zero");
        }
        return value;
    }

    // Shows the prompt and reads a decimal number greater than zero.
    public double readPositiveDouble(String prompt) throws InvalidInputException {
        System.out.print(prompt);
        String text = readLine();
        try {
            double value = Double.parseDouble(text);
            if (value <= 0) {
                throw new InvalidInputException("Amount must be greater than zero");
            }
            return value;
        } catch (NumberFormatException e) {
            throw new InvalidInputException("Please enter a valid amount");
        }
    }

    // Shows the prompt and reads text that must not be empty.
    public String readText(String prompt) throws InvalidInputException {
        System.out.print(prompt);
        String text = readLine();
        if (text.length() == 0) {
            throw new InvalidInputException("Input cannot be empty");
        }
        return text;
    }
}
