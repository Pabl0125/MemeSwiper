package app.swiper.memeswiper;

import app.memeapi.requester.MemeResponse;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

public class DetailController {

    @FXML private BorderPane detailContainer;
    @FXML private ImageView fullImageView;
    @FXML private VBox infoPanel;
    @FXML private ToggleButton btnInfoToggle;
    @FXML private Button btnDownload;
    @FXML private Label lblTitle, lblAuthor, lblSubreddit, lblUps, lblUrl;
    @FXML private Button btnRemoveFromGallery;
    @FXML private StackPane imageContainer;
    private MemeResponse currentMeme;
    private GalleryController galleryController; // Referencia al "padre"

    public void setGalleryController(GalleryController controller) {
        this.galleryController = controller;
    }

    @FXML
    public void initialize() {
        if (fullImageView != null && detailContainer != null) {
            // Forzamos a que la imagen mantenga proporciones
            fullImageView.setPreserveRatio(true);
            fullImageView.setSmooth(true);

            // Bindeamos el tamaño de la imagen al contenedor menos el espacio de botones
            fullImageView.fitWidthProperty().bind(imageContainer.widthProperty());
            fullImageView.fitHeightProperty().bind(imageContainer.heightProperty());
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
        if (currentMeme == null || currentMeme.getUrl() == null) return;

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Download Meme");

        // Extraer la extensión original (jpg, png, gif)
        String url = currentMeme.getUrl();
        String extension = url.substring(url.lastIndexOf("."));
        if (extension.length() > 4) extension = ".jpg"; // Fallback por si la URL tiene parámetros

        fileChooser.setInitialFileName(currentMeme.getTitle().replaceAll("[^a-zA-Z0-9]", "_") + extension);
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image", "*" + extension));

        // 2. Mostrar la ventana de guardado
        File file = fileChooser.showSaveDialog(fullImageView.getScene().getWindow());

        if (file != null) {
            // 3. Ejecutar la descarga en un hilo aparte para no congelar la UI
            new Thread(() -> {
                try {
                    URL urlObj = java.net.URI.create(url).toURL();
                    try (BufferedInputStream in = new BufferedInputStream(urlObj.openStream());
                         FileOutputStream fileOutputStream = new FileOutputStream(file)) {

                        byte[] dataBuffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                            fileOutputStream.write(dataBuffer, 0, bytesRead);
                        }

                        javafx.application.Platform.runLater(() -> {
                            btnDownload.setText("¡Guardado!");
                            btnDownload.setStyle("-fx-background-color: #2ecc71;"); // Cambia a verde

                            new Thread(() -> {
                                try {
                                    Thread.sleep(2000);
                                } catch (InterruptedException ex) {
                                }
                                javafx.application.Platform.runLater(() -> {
                                    btnDownload.setText("⬇ Descargar");
                                    btnDownload.getStyleClass().add("download-button");
                                });
                            }).start();
                        });

                        // Opcional: Mostrar una alerta de éxito al usuario
                    } catch (IOException e) {}
                }catch (Exception E){}
            }).start();
        }
    }
    public void handleRemoveFromGallery(){
        if (currentMeme == null) return;

        if (galleryController != null) {
            galleryController.deleteMeme(currentMeme);

            handleClose();
        }
    }
    @FXML
    private void handleClose() {
        Stage stage = (Stage) fullImageView.getScene().getWindow();
        stage.close();
    }
}