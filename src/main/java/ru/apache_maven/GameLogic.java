package ru.apache_maven;

import javafx.fxml.FXML;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class GameLogic {
    ColorChess turnColor;
    public GameLogic() {
        turnColor = ColorChess.WHITE;
    }

    public ColorChess getTurnColor() {
        return turnColor;
    }

    public void changeTurnColor(Rectangle turnColorRectangle) {
        if (turnColor == ColorChess.WHITE) {
            turnColor = ColorChess.BLACK; System.out.println("Ход черных");
            turnColorRectangle.setFill(Color.BLACK);
        }
        else {
            turnColor = ColorChess.WHITE; System.out.println("Ход белых");
            turnColorRectangle.setFill(Color.WHITE);
        }
    }
}