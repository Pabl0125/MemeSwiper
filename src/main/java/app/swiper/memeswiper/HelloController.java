package app.swiper.memeswiper;

import app.memeapi.requester.MemeRequester;
import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.net.URL;

public class HelloController {

    // Contenedores
    @FXML private StackPane memeViewer;
    @FXML private VBox userCard1; // Tarjeta de fondo
    @FXML private VBox userCard2; // Tarjeta de frente (la que se mueve)
    @FXML private VBox sideBar;

    // Imágenes
    @FXML private ImageView card1;
    @FXML private ImageView card2;

    // Botones Globales
    @FXML private Button likeButton;
    @FXML private Button dislikeButton;

    private boolean isExpanded = true;
    private final double SIDEBAR_WIDTH = 250;
    private final String DEFAULT_IMAGE_PATH = "/app/swiper/memeswiper/defaultImage.jpg";

    @FXML
    public void initialize() {
        // 1. Configurar el clip del menú lateral
        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(sideBar.widthProperty());
        clip.heightProperty().bind(sideBar.heightProperty());
        sideBar.setClip(clip);

        // 2. Bindings para que las imágenes no se corten y respeten el contenedor
        card1.fitWidthProperty().bind(memeViewer.widthProperty());
        card1.fitHeightProperty().bind(memeViewer.heightProperty());
        card2.fitWidthProperty().bind(memeViewer.widthProperty());
        card2.fitHeightProperty().bind(memeViewer.heightProperty());

        // 3. Acciones de botones
        likeButton.setOnAction(event -> animateSwipe(true));
        dislikeButton.setOnAction(event -> animateSwipe(false));

        // 4. Carga inicial de memes
        loadMeme(card2); // Imagen visible al frente
        loadMeme(card1); // Imagen preparada detrás
    }

    private void loadMeme(ImageView iv) {
        URL defaultUrl = getClass().getResource(DEFAULT_IMAGE_PATH);
        String finalUrl = (defaultUrl != null) ? defaultUrl.toExternalForm() : "";

        try {
            String apiUrl = MemeRequester.request().getUrl();
            if (apiUrl != null && !apiUrl.isEmpty()) finalUrl = apiUrl;
        } catch (Exception e) {
            System.err.println("Error API: " + e.getMessage());
        }

        if (!finalUrl.isEmpty()) {
            iv.setImage(new Image(finalUrl, true)); // Carga asíncrona
        }
    }

    private void animateSwipe(boolean isLike) {
        Duration duration = Duration.millis(450);
        double deltaX = isLike ? 750 : -750;
        double rotateAngle = isLike ? 35 : -35;

        // Animación de la tarjeta superior (userCard2)
        TranslateTransition translate = new TranslateTransition(duration, userCard2);
        translate.setToX(deltaX);

        RotateTransition rotate = new RotateTransition(duration, userCard2);
        rotate.setToAngle(rotateAngle);

        FadeTransition fade = new FadeTransition(duration, userCard2);
        fade.setToValue(0);

        ParallelTransition parallel = new ParallelTransition(translate, rotate, fade);

        parallel.setOnFinished(event -> {
            // INTERCAMBIO: La imagen de atrás pasa al frente
            card2.setImage(card1.getImage());

            // RESET: Devolver la tarjeta superior al centro de forma invisible
            userCard2.setTranslateX(0);
            userCard2.setRotate(0);
            userCard2.setOpacity(1.0);

            // PRECARGA: Traer nuevo meme para la tarjeta de atrás
            loadMeme(card1);
        });

        parallel.play();
    }

    @FXML
    private void handleToggleSidebar() {
        double targetWidth = isExpanded ? 0 : SIDEBAR_WIDTH;
        Timeline timeline = new Timeline();
        KeyValue kvPref = new KeyValue(sideBar.prefWidthProperty(), targetWidth, Interpolator.EASE_BOTH);
        KeyFrame kf = new KeyFrame(Duration.millis(300), kvPref);
        timeline.getKeyFrames().add(kf);

        timeline.setOnFinished(e -> { if (!isExpanded) sideBar.setVisible(false); });
        if (!isExpanded) sideBar.setVisible(true);

        timeline.play();
        isExpanded = !isExpanded;
    }
}