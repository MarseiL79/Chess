package ru.apache_maven.pieces;

import ru.apache_maven.ColorChess;
import ru.apache_maven.Coordinates;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Knight extends Piece {
    public Knight(ColorChess color, Coordinates coordinates) {
        super(color, coordinates);
    }

    @Override
    public String getPathToImage() {
        String res = "";
        if (this.getColor() == ColorChess.WHITE) { res = "/images/white_knight.png"; }
        else { res = "/images/black_knight.png"; }
        return res;
    }

    @Override
    protected Set<CoordinatesShift> getPieceMoves() {
        return new HashSet<>(Arrays.asList(
            new CoordinatesShift(-1, -2),
            new CoordinatesShift(-2, -1),

            new CoordinatesShift(-2, 1),
            new CoordinatesShift(-1, 2),

            new CoordinatesShift(2, 1),
            new CoordinatesShift(1, 2),

            new CoordinatesShift(1, -2),
            new CoordinatesShift(2, -1)
        )
        );
    }
}
