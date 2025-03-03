package ru.apache_maven;

import javafx.scene.shape.Rectangle;

import javax.swing.*;

public class BoardConsoleRenderer {

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_WHITE_PIECE_COLOR = "\u001B[97m";
    public static final String ANSI_BLACK_PIECE_COLOR = "\u001B[30m";
    public static final String ANSI_WHITE_CELL_BACKGROUND_COLOR = "\u001B[47m";
    public static final String ANSI_BLACK_CELL_BACKGROUND_COLOR = "\u001B[0;100m";

    public void render(Board board) {
        for (int rank = 8; rank >= 1; rank--) {
            String line = "";
            for (File file : File.values()) {
                line += getSpriteForEmptyCell(new Coordinates(file, rank));
            }
            line += ANSI_RESET;
            System.out.println(line);
        }

    }

    private String colorizeSprite(String sprite, Color pieceColor, boolean isCellDark) {
        //format = background color, font color, text
        String result = sprite;

        if(pieceColor == Color.WHITE) {
            result = ANSI_WHITE_PIECE_COLOR + result;
        } else {
            result = ANSI_BLACK_PIECE_COLOR + result;
        }

        if(isCellDark) {
            result = ANSI_BLACK_CELL_BACKGROUND_COLOR + result;
        } else {
            result = ANSI_WHITE_CELL_BACKGROUND_COLOR + result;
        }

        return result;
    }


    private String getSpriteForEmptyCell(Coordinates coordinates) {
        return colorizeSprite("   ", Color.WHITE, Board.isCellDark(coordinates));
    }
}
