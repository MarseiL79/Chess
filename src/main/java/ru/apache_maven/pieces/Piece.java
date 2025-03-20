package ru.apache_maven.pieces;

import javafx.scene.control.Label;
import ru.apache_maven.Board;
import ru.apache_maven.ColorChess;
import ru.apache_maven.Coordinates;
import ru.apache_maven.File;

import java.util.HashSet;
import java.util.Set;

public abstract class Piece {
    private final ColorChess color;
    public Coordinates coordinates;

    public Piece(ColorChess color, Coordinates coordinates) {
        this.color = color;
        this.coordinates = coordinates;
    }

    public Set<Coordinates> getLegalMoveSquares(Label label, Board board) {
        Set<Coordinates> available = getAvailableMoveSquares(board);
        Set<Coordinates> legal = new HashSet<>();
        for (Coordinates move : available) {
            // Создаем копию доски
            Board copy = new Board(board);
            // Симулируем ход: перемещаем фигуру с текущей позиции на move
            copy.movePiece(this.coordinates, move);
            // Если после хода король не под шахом, ход является законным
            if (!copy.isKingInCheck(label, this.getColor())) {
                legal.add(move);
            }
        }
        return legal;
    }

    public ColorChess getColor() {
        return color;
    }

    public Set<ru.apache_maven.Coordinates> getAvailableMoveSquares(Board board) {
        Set<Coordinates> result = new HashSet<>();

        for (CoordinatesShift shift : getPieceMoves()) {
            if(coordinates.canShift(shift)) {
                Coordinates newCoordinates = coordinates.shift(shift);

                if(isSquareAvailableForMove(newCoordinates, board)) {
                    result.add(newCoordinates);
                }
            }
        }
        return result;
    }

    public Coordinates getCoordinates() {
        return coordinates;
    }

    private boolean isSquareAvailableForMove(Coordinates coordinates, Board board) {
//        System.out.println("Пустой?: " + board.isSquareEmpty(coordinates) + coordinates.file + " " + coordinates.rank);
//        System.out.println("цвет this" +  this.getColor());
//        if(board.getPiece(coordinates) != null) {
//            System.out.println("цвет new: " + board.getPiece(coordinates).getColor());
//        }
//        System.out.println(" ");
        return board.isSquareEmpty(coordinates) || board.getPiece(coordinates).color != this.getColor();
    }

    public abstract String getPathToImage();

    protected abstract Set<CoordinatesShift> getPieceMoves();
    protected Set<CoordinatesShift> getPieceAttacks() {
        return getPieceMoves();
    }


}
