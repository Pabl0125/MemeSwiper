package app.swiper.memeswiper;

import app.memeapi.requester.MemeResponse;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class DetailController {

    @FXML private BorderPane detailContainer;
    @FXML private ImageView fullImageView;
    @FXML private VBox infoPanel;
    @FXML private ToggleButton btnInfoToggle;
    @FXML private Label lblTitle, lblAuthor, lblSubreddit, lblUps, lblUrl;

    private MemeResponse currentMeme;

    @FXML
    public void initialize() {
        if (fullImageView != null && detailContainer != null) {
            // Forzamos a que la imagen mantenga proporciones
            fullImageView.setPreserveRatio(true);
            fullImageView.setSmooth(true);

            // Bindeamos el tamaño de la imagen al contenedor menos el espacio de botones
            fullImageView.fitWidthProperty().bind(detailContainer.widthProperty().subtract(40));
            fullImageView.fitHeightProperty().bind(detailContainer.heightProperty().subtract(160));
        }
    }

    public void setMemeData(MemeResponse meme, Image image) {
        if (meme == null) return;
        this.currentMeme = meme;
        // 1.1 Actualizar textos (Hilo de UI garantizado)
        lblTitle.setText(meme.getTitle());
        lblAuthor.setText("Autor: " + meme.getAuthor());
        lblSubreddit.setText("r/" + meme.getSubreddit());
        lblUps.setText("👍 " + meme.getUps());
        lblUrl.setText(meme.getUrl());
        //Primero cargamos la imagen a baja resolucion
        if (image != null) {
            fullImageView.setImage(image);
        } else {
            fullImageView.setImage(new Image(meme.getUrl(), true));
        }
        //Luego cargamos la imagen completa
        Image fullResImage = new Image(meme.getUrl(), true);
        fullResImage.progressProperty().addListener((obs, oldProgress, newProgress) -> {
            if (newProgress.doubleValue() == 1.0) {
                Platform.runLater(() -> fullImageView.setImage(fullResImage));
            }
        });
    }

    @FXML
    private void handleToggleInfo() {
        boolean show = btnInfoToggle.isSelected();

        // Hacemos que el panel aparezca/desaparezca lógicamente
        infoPanel.setVisible(show);
        infoPanel.setManaged(show);

        // Forzamos a la ventana a ajustarse al nuevo contenido
        if (infoPanel.getScene() != null) {
            Stage stage = (Stage) infoPanel.getScene().getWindow();
            stage.sizeToScene();
        }
    }

    @FXML
    private void handleDownload() {
        if (currentMeme != null) {
            System.out.println("Iniciando descarga de: " + currentMeme.getUrl());
            // TODO: Implementar lógica de guardado en disco
        }
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) fullImageView.getScene().getWindow();
        stage.close();
    }
}