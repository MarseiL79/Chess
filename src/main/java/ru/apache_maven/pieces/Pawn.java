package ru.apache_maven.pieces;

import ru.apache_maven.Board;
import ru.apache_maven.ColorChess;
import ru.apache_maven.Coordinates;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Pawn extends Piece {
    public Pawn(ColorChess color, Coordinates coordinates) {
        super(color, coordinates);
    }

    @Override
    public String getPathToImage() {
        String res = "";
        if (this.getColor() == ColorChess.WHITE) { res = "/images/white_pawn.png"; }
        else { res = "/images/black_pawn.png"; }
        return res;
    }

    @Override
    public Set<Coordinates> getAvailableMoveSquares(Board board) {
        Set<Coordinates> result = new HashSet<>();

        for (CoordinatesShift shift : getPieceMoves()) {
            if (coordinates.canShift(shift)) {
                Coordinates newCoordinates = coordinates.shift(shift);

                // Проверка двойного хода
                if (Math.abs(shift.rankShift) == 2) {
                    // Определяем промежуточную клетку между текущей позицией и новой
                    Coordinates intermediate = coordinates.shift(new CoordinatesShift(0, shift.rankShift / 2));

                    // Если промежуточная клетка занята, пешка не может прыгнуть через неё
                    if (!board.isSquareEmpty(intermediate)) {
                        continue; // Пропускаем этот ход
                    }
                }

                // Добавляем ход только если конечная клетка пуста
                if (board.isSquareEmpty(newCoordinates)) {
                    result.add(newCoordinates);
                }
            }
        }

        // Добавляем возможные атаки (если есть фигура противника по диагонали)
        for (CoordinatesShift shift : getPieceAttacks()) {
            if (coordinates.canShift(shift)) {
                Coordinates attackCoordinates = coordinates.shift(shift);

                if (!board.isSquareEmpty(attackCoordinates) &&
                        board.getPiece(attackCoordinates).getColor() != this.getColor() && board.getPiece(attackCoordinates) != null) {
                    result.add(attackCoordinates);
                }
            }
        }

        return result;
    }

    @Override
    protected Set<CoordinatesShift> getPieceAttacks() {
        Set<CoordinatesShift> result = new HashSet<>();

        if (this.getColor() == ColorChess.WHITE) {
            result.add(new CoordinatesShift(-1, 1));
            result.add(new CoordinatesShift(1, 1));
        } else {
            result.add(new CoordinatesShift(-1, -1));
            result.add(new CoordinatesShift(1, -1));
        }

        return result;
    }

    @Override
    protected Set<CoordinatesShift> getPieceMoves() {
        Set<CoordinatesShift> result = new HashSet<>();
        if (this.getColor() == ColorChess.WHITE) {
            // Белая пешка двигается вперед, увеличивая rank на 1
            result.add(new CoordinatesShift(0, 1));
            // Если пешка на начальной позиции (rank 2), добавляем возможность двойного хода
            if (this.coordinates.rank == 2) {
                result.add(new CoordinatesShift(0, 2));
            }
        } else {
            // Чёрная пешка двигается вперед (в данном случае – вниз, уменьшая rank)
            result.add(new CoordinatesShift(0, -1));
            // Если чёрная пешка на начальной позиции (rank 7), добавляем двойной ход
            if (this.coordinates.rank == 7) {
                result.add(new CoordinatesShift(0, -2));
            }
        }
        return result;
    }
}

