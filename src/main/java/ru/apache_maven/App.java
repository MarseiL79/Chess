package ru.apache_maven;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class App extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        //Через FXML файл
        Parent panel = FXMLLoader.load(getClass().getResource("/maket.fxml"));

        Scene scene = new Scene(panel, 400, 400);

        primaryStage.setTitle("JavaFX TEST");
        System.out.println("HELLO");
        primaryStage.setScene(scene);
        primaryStage.show();

        //Без FXML файла, напрямую через Java-код
       /* Button button = new Button("Test");
        Text text = new Text(200, 200, "Test javafx!");
        text.setFont(new Font(60));
        BorderPane pane =  new BorderPane();
        pane.setCenter(button);
        pane.setTop(text);

        button.setOnAction(e -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "WOF WOF WOF!!!");
        alert.showAndWait();
    });

        InputStream iconStream = getClass().getResourceAsStream("/images/pion.png");
        Image image = new Image(iconStream);
        primaryStage.getIcons().add(image);

        Scene scene = new Scene(pane, 500, 500);

        primaryStage.setTitle("JavaFX title");
        primaryStage.setScene(scene);

        primaryStage.show();*/
    }

    public static void main(String[] args) {
        Application.launch();
    }


}
