package ru.apache_maven.online;

import ru.apache_maven.*;
import ru.apache_maven.File;
import ru.apache_maven.pieces.*;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class OnlineChessServer {
    private ServerSocket serverSocket;
    private List<ClientHandler> clients = new CopyOnWriteArrayList<>();
    // Список клиентов, ожидающих начала игры (2 штуки)
    private List<ClientHandler> waitingClients = new ArrayList<>();
    private final List<PrintWriter> clientOutputs = new ArrayList<>();

    private Board board;
    // Текущий ход (будет переключаться сервером)
    private ColorChess turnColor = ColorChess.WHITE;

    public OnlineChessServer(int port) throws IOException {
        // Прослушиваем все сетевые интерфейсы
        serverSocket = new ServerSocket(port, 50, InetAddress.getByName("0.0.0.0"));
        board = new Board();
        board.setupDefaultPiecePositions();
        System.out.println("Сервер запущен на порту " + port);
    }

    public void start() {
        while (true) {
            try {
                Socket socket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(socket);
                waitingClients.add(handler);
                new Thread(handler).start();
                System.out.println("Новый клиент подключён. Всего ожидающих: " + waitingClients.size());

                if (waitingClients.size() == 2) {
                    // Случайное распределение цветов между двумя клиентами
                    if (new Random().nextBoolean()) {
                        waitingClients.get(0).setAssignedColor(ColorChess.WHITE);
                        waitingClients.get(1).setAssignedColor(ColorChess.BLACK);
                    } else {
                        waitingClients.get(0).setAssignedColor(ColorChess.BLACK);
                        waitingClients.get(1).setAssignedColor(ColorChess.WHITE);
                    }
                    // Отправляем клиентам их назначенный цвет
                    for (ClientHandler client : waitingClients) {
                        client.sendMessage("ASSIGN " + client.getAssignedColor());
                        // Добавляем в список активных клиентов
                        clients.add(client);
                    }
                    // Сообщаем, что игра начинается, и сообщаем начальный ход
                    broadcast("START TURN " + turnColor, null);
                    waitingClients.clear();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Метод для переключения хода
    private void toggleTurn() {
        turnColor = (turnColor == ColorChess.WHITE ? ColorChess.BLACK : ColorChess.WHITE);
    }

    // Обработка сообщения от клиента
    private void processMessage(String message, ClientHandler sender) {
        if (message.startsWith("MOVE")) {
            String[] parts = message.split(" ");
            if (parts.length == 3) {
                Coordinates from = parseCoordinates(parts[1]);
                Coordinates to = parseCoordinates(parts[2]);

                // Определяем, есть ли фигура в клетке to (то есть ход является захватом)
                boolean capture = board.getPiece(to) != null;

                board.movePiece(from, to);
                toggleTurn(); // переключаем ход

                boolean isCheck = board.isKingInCheck(null, turnColor);

                // Формируем сообщение с флагом захвата
                String moveMessage = "MOVE " + parts[1] + " " + parts[2] + " CAPTURE " + capture + " CHECK " + isCheck + " TURN " + turnColor;
                broadcast(moveMessage, null);
            }
        }
        if (message.startsWith("CASTLE")) {
            String[] parts = message.split(" ");
            if(parts.length == 3) {
                Coordinates kingFrom = parseCoordinates(parts[1]);
                Coordinates rookFrom = parseCoordinates(parts[2]);

                toggleTurn();

                message = board.doCastling(kingFrom, rookFrom) + turnColor;
                broadcast(message, null);
            }
        }
        if (message.startsWith("PROMOTION")) {
            String[] parts = message.split(" ");
            // parts: [ "PROMOTION", "E7", "E8", "QUEEN" ]
            if (parts.length == 4) {
                Coordinates from = parseCoordinates(parts[1]);
                Coordinates to = parseCoordinates(parts[2]);
                String newPieceType = parts[3];

                // Удаляем пешку
                ColorChess color = board.getPiece(from).getColor(); // Определите цвет, например, по пешке, которую убрали
                board.removePiece(from);
                // Ставим новую фигуру
                Piece newPiece = null;

                if (newPieceType.equals("QUEEN")) {
                    newPiece = new Queen(color, to);
                } else if (newPieceType.equals("ROOK")) {
                    newPiece = new Rook(color, to);
                } else if (newPieceType.equals("BISHOP")) {
                    newPiece = new Bishop(color, to);
                } else if (newPieceType.equals("KNIGHT")) {
                    newPiece = new Knight(color, to);
                }
                if (newPiece != null) {
                    board.setPiece(to, newPiece);
                }

                // Меняем ход
                toggleTurn();

                // Рассылаем сообщение всем клиентам
                String promoMsg = "PROMOTION " + from + " " + to + " " + newPieceType + " TURN " + turnColor;
                broadcast(promoMsg, null);
            }
        }

    }


    private Coordinates parseCoordinates(String coordStr) {
        // Ожидается формат, например, "A2". Первая буква – File, число – rank.
        char fileChar = Character.toUpperCase(coordStr.charAt(0));
        int rank = Integer.parseInt(coordStr.substring(1));
        ru.apache_maven.File file = ru.apache_maven.File.valueOf(String.valueOf(fileChar));
        return new Coordinates(file, rank);
    }

    // Рассылаем сообщение всем клиентам
    public void broadcast(String message, ClientHandler sender) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }

    private class ClientHandler implements Runnable {
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;
        private ColorChess assignedColor;

        public ClientHandler(Socket socket) throws IOException {
            this.socket = socket;
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);

            clientOutputs.add(out);
        }

        @Override
        public void run() {
            try {
                String line;
                while ((line = in.readLine()) != null) {
                    System.out.println("Получено от клиента: " + line);
                    processMessage(line, this);
                }
                System.out.println("Client disconnected (line == null)");
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try { socket.close(); } catch (IOException e) { }
                clients.remove(this);
                System.out.println("Client disconnected");
            }
        }



        public void sendMessage(String message) {
            out.println(message);
        }

        public void setAssignedColor(ColorChess color) {
            this.assignedColor = color;
        }

        public ColorChess getAssignedColor() {
            return assignedColor;
        }
    }

    public static void main(String[] args) {
        try {
            OnlineChessServer server = new OnlineChessServer(12345);
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
