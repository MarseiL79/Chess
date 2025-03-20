package ru.apache_maven;

import javafx.scene.shape.Rectangle;

public class GameLogic {
    private ColorChess turnColor;
    // Добавляем поле для цвета игрока, если нужно
    private ColorChess playerColor;

    public GameLogic() {
        turnColor = ColorChess.WHITE;
    }

    public synchronized void setTurnColor(ColorChess color) {
        this.turnColor = color;
    }

    public synchronized ColorChess getTurnColor() {
        return turnColor;
    }

    // Для клиента: сохранить свой назначенный цвет
    public void setPlayerColor(ColorChess color) {
        this.playerColor = color;
    }

    public ColorChess getPlayerColor() {
        return playerColor;
    }

    public void changeTurnColor(Rectangle turnColorRectangle) {
        if (turnColor == ColorChess.WHITE) {
            turnColor = ColorChess.BLACK;
            turnColorRectangle.setFill(javafx.scene.paint.Color.BLACK);
        } else {
            turnColor = ColorChess.WHITE;
            turnColorRectangle.setFill(javafx.scene.paint.Color.WHITE);
        }
    }
}
