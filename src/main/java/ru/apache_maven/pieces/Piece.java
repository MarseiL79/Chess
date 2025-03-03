package ru.apache_maven.pieces;

import ru.apache_maven.Color;
import ru.apache_maven.Coordinates;

public class Piece {
    public final Color color;
    public Coordinates coordinates;

    public Piece(Color color, Coordinates coordinates) {
        this.color = color;
    }
}
