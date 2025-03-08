package ru.apache_maven.pieces;

import ru.apache_maven.Board;
import ru.apache_maven.Color;
import ru.apache_maven.Coordinates;

import java.util.HashSet;
import java.util.Set;

public abstract class Piece {
    public final Color color;
    public Coordinates coordinates;

    public Piece(Color color, Coordinates coordinates) {
        this.color = color;
        this.coordinates = coordinates;
    }

    public Color getColor() {
        return color;
    }
    public Set<Coordinates> getAvailableMoveSquares(Board board) {
        Set<Coordinates> result = new HashSet<>();

        for (CoordinatesShift shift : getPieceMoves()) {
            if(coordinates.canShift(shift)) {
                Coordinates newCoordinates = coordinates.shift(shift);

                if(isSquareAvailableForMove(newCoordinates, board)) {
                    result.add(newCoordinates);
                }
            }
        }
        //System.out.println(result);
        return result;
    }

    public Coordinates getCoordinates() {
        return coordinates;
    }

    private boolean isSquareAvailableForMove(Coordinates coordinates, Board board) {
        return board.isSquareEmpty(coordinates) || board.getPiece(coordinates).color != color;
    }

    protected abstract Set<CoordinatesShift> getPieceMoves();


}
