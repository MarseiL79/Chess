package ru.apache_maven;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import ru.apache_maven.online.OnlineChessClient;

import java.io.InputStream;
import java.util.Optional;

public class App extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Показываем диалог для ввода IP-адреса сервера
        TextInputDialog ipDialog = new TextInputDialog("your-external-ip-or-ddns");
        ipDialog.setTitle("Подключение к серверу");
        ipDialog.setHeaderText("Введите внешний IP адрес или доменное имя сервера:");
        ipDialog.setContentText("IP/DNS:");

        Optional<String> result = ipDialog.showAndWait();
        if (!result.isPresent() || result.get().trim().isEmpty()) {
            // Если пользователь не ввёл IP или отменил, завершаем приложение
            Platform.exit();
            return;
        }
        String serverIP = result.get().trim();

        // Загрузка FXML разметки основного окна
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/maket.fxml"));
        Parent panel = loader.load();

        // Инициализация логики игры и доски
        Board board = new Board();
        GameLogic gameLogic = new GameLogic();
        board.setupDefaultPiecePositions();

        // Получение контроллера и установка зависимостей
        ChessController controller = loader.getController();
        controller.setBoard(board);
        controller.setGameLogic(gameLogic);

        // Создаём объект клиента, используя введённый IP-адрес и порт 12345
        OnlineChessClient onlineClient = new OnlineChessClient(serverIP, 12345, controller);
        onlineClient.start();
        controller.setOnlineClient(onlineClient);

        // Создание и настройка сцены
        Scene scene = new Scene(panel, 1000, 800);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        primaryStage.setTitle("Chess Game");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Установка иконки приложения
        InputStream iconStream = getClass().getResourceAsStream("/images/pion.png");
        Image image = new Image(iconStream);
        primaryStage.getIcons().add(image);
        primaryStage.setResizable(false);
    }

    public static void main(String[] args) {
        Application.launch();
    }
}
