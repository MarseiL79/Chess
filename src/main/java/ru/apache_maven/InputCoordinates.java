package ru.apache_maven;

import java.util.Scanner;

public class InputCoordinates {
    Scanner scanner = new Scanner(System.in);

    public Coordinates input() {
        while(true) {
            System.out.println("Input coordinates, (example: a1)");

            String line = scanner.nextLine();

            if(line.length() != 2) {
                System.out.println("invalid format");
            }
        }
    }
}
