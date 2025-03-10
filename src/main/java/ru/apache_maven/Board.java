package ru.apache_maven;

import ru.apache_maven.pieces.*;

import java.util.HashMap;

public class Board {
    HashMap<Coordinates, Piece> pieces = new HashMap<>();

    public void setPiece(Coordinates coordinates, Piece piece) {
        piece.coordinates = coordinates;
        pieces.put(coordinates, piece);
    }

    public HashMap<Coordinates, Piece> getMatrix() {
        return pieces;
    }

    public void removePiece(Coordinates coordinates) {
        pieces.remove(coordinates);
    }

    public void movePiece(Coordinates from, Coordinates to) {
        Piece piece = getPiece(from);

        removePiece(from);
        setPiece(to, piece);
    }

    public void setupDefaultPiecePositions() {
        for (File file : File.values()) {
            //Setting Pawns
            setPiece(new Coordinates(file, 2), new Pawn(ColorChess.WHITE, new Coordinates(file, 2)));
            setPiece(new Coordinates(file, 7), new Pawn(ColorChess.BLACK, new Coordinates(file, 7)));
        }
        //Setting Rooks
        setPiece(new Coordinates(File.A, 1), new Rook(ColorChess.WHITE, new Coordinates(File.A, 1)));
        setPiece(new Coordinates(File.H, 1), new Rook(ColorChess.WHITE, new Coordinates(File.H, 1)));
        setPiece(new Coordinates(File.A, 8), new Rook(ColorChess.BLACK, new Coordinates(File.A, 8)));
        setPiece(new Coordinates(File.H, 8), new Rook(ColorChess.BLACK, new Coordinates(File.H, 8)));

        //Setting Bishops
        setPiece(new Coordinates(File.C, 1), new Bishop(ColorChess.WHITE, new Coordinates(File.C, 1)));
        setPiece(new Coordinates(File.F, 1), new Bishop(ColorChess.WHITE, new Coordinates(File.F, 1)));
        setPiece(new Coordinates(File.C, 8), new Bishop(ColorChess.BLACK, new Coordinates(File.C, 8)));
        setPiece(new Coordinates(File.F, 8), new Bishop(ColorChess.BLACK, new Coordinates(File.F, 8)));

        //Setting Knights
        setPiece(new Coordinates(File.B, 1), new Knight(ColorChess.WHITE, new Coordinates(File.B, 1)));
        setPiece(new Coordinates(File.G, 1), new Knight(ColorChess.WHITE, new Coordinates(File.G, 1)));
        setPiece(new Coordinates(File.B, 8), new Knight(ColorChess.BLACK, new Coordinates(File.B, 8)));
        setPiece(new Coordinates(File.G, 8), new Knight(ColorChess.BLACK, new Coordinates(File.G, 8)));

        //Setting Kings and Queens
        setPiece(new Coordinates(File.E, 1), new King(ColorChess.WHITE, new Coordinates(File.E, 1)));
        setPiece(new Coordinates(File.E, 8), new King(ColorChess.BLACK, new Coordinates(File.E, 8)));
        setPiece(new Coordinates(File.D, 1), new Queen(ColorChess.WHITE, new Coordinates(File.D, 1)));
        setPiece(new Coordinates(File.D, 8), new Queen(ColorChess.BLACK, new Coordinates(File.D, 8)));
    }

    public static boolean isCellDark(Coordinates coordinates) {
        return (((coordinates.file.ordinal() + 1) + coordinates.rank) % 2) == 0;
    }

    public boolean isSquareEmpty(Coordinates coordinates) {
        return !(pieces.containsKey(coordinates));
    }

    public Piece getPiece(Coordinates coordinates) {
        return pieces.get(coordinates);
    }
}
