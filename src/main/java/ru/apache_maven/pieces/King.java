package ru.apache_maven.pieces;

import ru.apache_maven.ColorChess;
import ru.apache_maven.Coordinates;

import java.util.Set;

public class King extends Piece {
    public King(ColorChess color, Coordinates coordinates) {
        super(color, coordinates);
    }

    @Override
    protected Set<CoordinatesShift> getPieceMoves() {
        return null;
    }
}
