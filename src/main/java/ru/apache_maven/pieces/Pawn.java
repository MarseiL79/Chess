package ru.apache_maven.pieces;

import ru.apache_maven.Board;
import ru.apache_maven.ColorChess;
import ru.apache_maven.Coordinates;
import ru.apache_maven.File;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Pawn extends Piece {
    private boolean justDoubleStepped = false;

    public void setJustDoubleStepped(boolean value) {
        this.justDoubleStepped = value;
    }

    public boolean isJustDoubleStepped() {
        return justDoubleStepped;
    }

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

        // 1) Обычные прямые ходы (включая проверку на двойной шаг)
        for (CoordinatesShift shift : getPieceMoves()) {
            if (!coordinates.canShift(shift)) continue;
            Coordinates newCoordinates = coordinates.shift(shift);

            // Проверка двойного хода
            if (Math.abs(shift.rankShift) == 2) {
                Coordinates intermediate = coordinates.shift(new CoordinatesShift(0, shift.rankShift / 2));
                if (!board.isSquareEmpty(intermediate)) {
                    continue;
                }
            }
            if (board.isSquareEmpty(newCoordinates)) {
                result.add(newCoordinates);
            }
        }

        // 2) Обычные атаки по диагонали (если в целевой клетке вражеская фигура)
        for (CoordinatesShift shift : getPieceAttacks()) {
            if (!coordinates.canShift(shift)) continue;
            Coordinates attackCoordinates = coordinates.shift(shift);
            Piece target = board.getPiece(attackCoordinates);
            if (target != null && target.getColor() != this.getColor()) {
                result.add(attackCoordinates);
            }
        }

        // 3) Специальное взятие «на проходе»
        // Для белых: пешка находится на rank 5, и справа/слева от неё есть чёрная пешка,
        // которая только что сделала двойной шаг (isJustDoubleStepped == true).
        // Целевая клетка — пустая клетка на одну диагональ вперёд (rank+1).
        if (this.getColor() == ColorChess.WHITE && this.coordinates.rank == 5) {
            // Проверяем файлы слева и справа
            for (int fileShift : new int[]{-1, 1}) {
                int adjacentFileIndex = this.coordinates.file.ordinal() + fileShift;
                if (adjacentFileIndex < 0 || adjacentFileIndex > 7) continue;
                File adjacentFile = File.values()[adjacentFileIndex];
                Coordinates adjacentPawnCoord = new Coordinates(adjacentFile, 5);
                Piece adjacentPiece = board.getPiece(adjacentPawnCoord);
                if (adjacentPiece instanceof Pawn
                        && adjacentPiece.getColor() == ColorChess.BLACK
                        && ((Pawn) adjacentPiece).isJustDoubleStepped()) {
                    // Целевая клетка для en passant: файл adjacentFile, rank = 6
                    Coordinates captureCoord = new Coordinates(adjacentFile, 6);
                    if (board.isSquareEmpty(captureCoord)) {
                        result.add(captureCoord);
                    }
                }
            }
        }
        // Для чёрных: аналогично, но пешка должна быть на rank 4, вражеская пешка на rank 4,
        // и целевая клетка — rank 3.
        if (this.getColor() == ColorChess.BLACK && this.coordinates.rank == 4) {
            for (int fileShift : new int[]{-1, 1}) {
                int adjacentFileIndex = this.coordinates.file.ordinal() + fileShift;
                if (adjacentFileIndex < 0 || adjacentFileIndex > 7) continue;
                File adjacentFile = File.values()[adjacentFileIndex];
                Coordinates adjacentPawnCoord = new Coordinates(adjacentFile, 4);
                Piece adjacentPiece = board.getPiece(adjacentPawnCoord);
                if (adjacentPiece instanceof Pawn
                        && adjacentPiece.getColor() == ColorChess.WHITE
                        && ((Pawn) adjacentPiece).isJustDoubleStepped()) {
                    Coordinates captureCoord = new Coordinates(adjacentFile, 3);
                    if (board.isSquareEmpty(captureCoord)) {
                        result.add(captureCoord);
                    }
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

