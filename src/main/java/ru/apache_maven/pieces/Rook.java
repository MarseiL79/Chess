package ru.apache_maven.pieces;

import ru.apache_maven.Board;
import ru.apache_maven.ColorChess;
import ru.apache_maven.Coordinates;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Rook extends Piece {
    public boolean didMove = false;
    public Rook(ColorChess color, Coordinates coordinates) {
        super(color, coordinates);
    }

    @Override
    public String getPathToImage() {
        String res = "";
        if (this.getColor() == ColorChess.WHITE) { res = "/images/white_rook.png"; }
        else { res = "/images/black_rook.png"; }
        return res;
    }

    @Override
    protected Set<CoordinatesShift> getPieceMoves() {
        return new HashSet<>(Arrays.asList(
                new CoordinatesShift(0, 1),   // вверх
                new CoordinatesShift(1, 0),   // вправо
                new CoordinatesShift(0, -1),  // вниз
                new CoordinatesShift(-1, 0)   // влево
        ));
    }

    public boolean hasMoved() {
        return didMove;
    }

    public void setDidMove() {
        didMove = true;
    }

    @Override
    public Set<Coordinates> getAvailableMoveSquares(Board board) {
        Set<Coordinates> result = new HashSet<>();
        for (CoordinatesShift direction : getPieceMoves()) {
            Coordinates next = this.coordinates;
            while (next.canShift(direction)) {
                next = next.shift(direction);
                if (board.isSquareEmpty(next)) {
                    result.add(next);
                } else {
                    if (board.getPiece(next).getColor() != this.getColor()) {
                        result.add(next);
                    }
                    break;
                }
            }
        }
        return result;
    }
}
