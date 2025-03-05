package ru.apache_maven.pieces;

import ru.apache_maven.Color;
import ru.apache_maven.Coordinates;

import java.util.Set;

public class Pawn extends Piece {
    public Pawn(Color color, Coordinates coordinates) {
        super(color, coordinates);
    }

    @Override
    protected Set<CoordinatesShift> getPieceMoves() {
        return null;
    }
}
