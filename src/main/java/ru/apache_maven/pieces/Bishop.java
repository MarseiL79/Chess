package ru.apache_maven.pieces;

import ru.apache_maven.Board;
import ru.apache_maven.ColorChess;
import ru.apache_maven.Coordinates;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Bishop extends Piece {
    public Bishop(ColorChess color, Coordinates coordinates) {
        super(color, coordinates);
    }

    @Override
    public String getPathToImage() {
        String res = "";
        if (this.getColor() == ColorChess.WHITE) { res = "/images/white_bishop.png"; }
        else { res = "/images/black_bishop.png"; }
        return res;
    }

    @Override
    protected Set<CoordinatesShift> getPieceMoves() {
        return new HashSet<>(Arrays.asList(
                new CoordinatesShift(1, 1),
                new CoordinatesShift(1, -1),
                new CoordinatesShift(-1, 1),
                new CoordinatesShift(-1, -1)
        ));
    }

    // Переопределяем метод, чтобы двигаться по направлению до столкновения:
    @Override
    public Set<Coordinates> getAvailableMoveSquares(Board board) {
        Set<Coordinates> result = new HashSet<>();
        for (CoordinatesShift direction : getPieceMoves()) {
            Coordinates next = this.coordinates;
            while (next.canShift(direction)) {
                next = next.shift(direction);
                // Если клетка свободна, добавляем ход
                if (board.isSquareEmpty(next)) {
                    result.add(next);
                } else {
                    // Если на клетке находится фигура противника – добавляем как взятие и останавливаемся
                    if (board.getPiece(next).getColor() != this.getColor()) {
                        result.add(next);
                    }
                    break; // дальше по этому направлению двигаться нельзя
                }
            }
        }
        return result;
    }
}
