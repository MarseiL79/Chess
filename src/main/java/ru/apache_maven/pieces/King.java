package ru.apache_maven.pieces;

import ru.apache_maven.ColorChess;
import ru.apache_maven.Coordinates;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class King extends Piece {
    private boolean didMove = false;
    public King(ColorChess color, Coordinates coordinates) {
        super(color, coordinates);
    }

    @Override
    public String getPathToImage() {
        String res = "";
        if (this.getColor() == ColorChess.WHITE) { res = "/images/white_king.png"; }
        else { res = "/images/black_king.png"; }
        return res;
    }

    public boolean hasMoved() {
        return didMove;
    }

    public void setDidMove() {
        didMove = true;
    }

    @Override
    protected Set<CoordinatesShift> getPieceMoves() {
        // Все 8 направлений, по 1 клетке
        return new HashSet<>(Arrays.asList(
                new CoordinatesShift(0, 1),   // вверх
                new CoordinatesShift(1, 1),   // вверх вправо
                new CoordinatesShift(1, 0),   // вправо
                new CoordinatesShift(1, -1),  // вниз вправо
                new CoordinatesShift(0, -1),  // вниз
                new CoordinatesShift(-1, -1), // вниз влево
                new CoordinatesShift(-1, 0),  // влево
                new CoordinatesShift(-1, 1)   // вверх влево
        ));
    }
}
