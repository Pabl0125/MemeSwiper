package app.swiper.memeswiper;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;

public class HelloApplication extends Application {

    private static final double WIDTH = 900;
    private static final double HEIGHT = 600;

    @Override
    public void start(Stage stage) throws IOException {
        URL fxmlLocation = getClass().getResource("/app/swiper/memeswiper/MainWindow.fxml");
        if (fxmlLocation == null) {
            throw new RuntimeException("ERROR: No se encontró MainWindow.fxml en la ruta especificada.");
        }
        FXMLLoader fxmlLoader = new FXMLLoader(fxmlLocation);
        Scene scene = new Scene(fxmlLoader.load(), WIDTH, HEIGHT);

        URL cssLocation = getClass().getResource("/app/swiper/memeswiper/styleMainWindow.css");
        if (cssLocation != null) {
            scene.getStylesheets().add(cssLocation.toExternalForm());
        } else {
            System.err.println("ADVERTENCIA: No se encontró styleMainWindow.css. Se cargará la app sin estilos.");
        }

        stage.setTitle("MemeSwiper");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}