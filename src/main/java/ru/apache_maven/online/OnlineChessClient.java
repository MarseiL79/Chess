package ru.apache_maven.online;

import javafx.application.Platform;
import ru.apache_maven.ChessController;
import ru.apache_maven.ColorChess;

import java.io.*;
import java.net.Socket;

public class OnlineChessClient {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private String serverIP;
    private int serverPort;
    private ChessController chessController; // ссылка на UI-контроллер
    private ColorChess assignedColor;

    public OnlineChessClient(String serverIP, int serverPort, ChessController controller) {
        this.serverIP = serverIP;
        this.serverPort = serverPort;
        this.chessController = controller;
    }

    public void start() {
        try {
            socket = new Socket(serverIP, serverPort);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);

            // Поток для чтения сообщений от сервера
            new Thread(() -> {
                String message;
                try {
                    while ((message = in.readLine()) != null) {
                        System.out.println("Получено от сервера: " + message);
                        // Обработка сообщений
                        processMessage(message);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processMessage(String message) {
        // Сообщение вида "ASSIGN BLACK" или "ASSIGN WHITE"
        if (message.startsWith("ASSIGN")) {
            String[] parts = message.split(" ");
            if (parts.length == 2) {
                ColorChess assigned = ColorChess.valueOf(parts[1]);
                chessController.setPlayerColor(assigned);  // новый метод в ChessController
                System.out.println("Ваш назначенный цвет: " + assigned);
            }
        } else if (message.startsWith("START")) {
            // Можно обработать сообщение START, если нужно
            System.out.println("Игра начинается!");

            Platform.runLater(() -> chessController.updateBoard("update"));

        } else if (message.startsWith("MOVE")) {
            // Формат сообщения: "MOVE A2 A4 TURN BLACK"
            Platform.runLater(() -> {
                chessController.updateBoard(message);
            });
        } else if (message.startsWith("CASTLE")) {
            // Формат сообщения: "MOVE A2 A4 TURN BLACK"
            Platform.runLater(() -> {
                chessController.updateBoard(message);
            });
        }
    }

    public void sendMessage(String message) {
        out.println(message);
    }
}