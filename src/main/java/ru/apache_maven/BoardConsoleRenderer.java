package ru.apache_maven;

import ru.apache_maven.pieces.Piece;

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
                Coordinates coordinates = new Coordinates(file, rank);
                if (board.isSquareEmpty(coordinates)) {
                    line += getSpriteForEmptyCell(new Coordinates(file, rank));
                } else {
                    line += getPieceSprite(board.getPiece(coordinates));
                }
            }
            line += ANSI_RESET;
            System.out.println(line);
        }

    }

    private String colorizeSprite(String sprite, ColorChess pieceColor, boolean isCellDark) {
        //format = background color, font color, text
        String result = sprite;

        if (pieceColor == ColorChess.WHITE) {
            result = ANSI_WHITE_PIECE_COLOR + result;
        } else {
            result = ANSI_BLACK_PIECE_COLOR + result;
        }

        if (isCellDark) {
            result = ANSI_BLACK_CELL_BACKGROUND_COLOR + result;
        } else {
            result = ANSI_WHITE_CELL_BACKGROUND_COLOR + result;
        }

        return result;
    }


    private String getSpriteForEmptyCell(Coordinates coordinates) {
        return colorizeSprite("    ", ColorChess.WHITE, Board.isCellDark(coordinates));
    }


    private String selectUnicodeSpriteForPiece(Piece piece) {
        switch (piece.getClass().getSimpleName()) {
            case "Pawn":
                return "♟"; // Пешка
            case "Knight":
                return "♘"; // Конь
            case "King":
                return "♔"; // Король
            case "Queen":
                return "♕"; // Ферзь
            case "Rook":
                return "♖"; // Ладья
            case "Bishop":
                return "♗"; // Слон
            default:
                return "?"; // На случай неизвестной фигуры
        }
    }

    private String getPieceSprite(Piece piece) {
        return colorizeSprite(" " + selectUnicodeSpriteForPiece(piece) + " ", piece.color, Board.isCellDark(piece.coordinates));
    }
}
