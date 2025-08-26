import commands.BoardParser;
import model.Board;

import java.io.IOException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        BoardParser parser = new BoardParser();
        Board board = null;

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();
            if (line.equals("quit")) {
                break;
            }
            try {
                String[] parts = line.split("\\s+", 3);
                if (parts.length >= 2 && parts[0].equals("load") && parts[1].equals("board")) {
                    if (parts.length != 3) {
                        throw new IllegalArgumentException("Error, invalid load board command");
                    }
                    board = parser.parse(parts[2]);
                    board.print();
                    // Optional: Print ladybugs for testing
                    System.out.println("Ladybugs: " + board.getLadybugList());
                } else {
                    System.out.println("Error, invalid command");
                }
            } catch (IOException e) {
                System.out.println("Error, cannot read file: " + e.getMessage());
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            }
        }
        scanner.close();
    }
}