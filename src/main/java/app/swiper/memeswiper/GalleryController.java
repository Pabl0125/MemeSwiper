package app.swiper.memeswiper;

import app.memeapi.requester.MemeResponse;
import javafx.fxml.FXML;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView; // IMPORTACIÓN CORRECTA
import javafx.scene.layout.TilePane;
import java.util.Set;

public class GalleryController {

    @FXML
    private TilePane galleryGrid;

    public void setImages(Set<String> imageUrls) {
        galleryGrid.getChildren().clear();

        new Thread(() -> {
            for (String url : imageUrls) {
                Image img = new Image(url, 200, 200, true, true);
                ImageView iv = new ImageView(img);

                // Volvemos al hilo de UI para añadir el nodo
                javafx.application.Platform.runLater(() -> {
                    galleryGrid.getChildren().add(iv);
                });
            }
        }).start();
    }

    private ImageView createThumbnail(String url) {
        // 2.1.1 Carga asíncrona y redimensionamiento (200px de ancho)
        Image image = new Image(url, 200, 0, true, true);
        ImageView iv = new ImageView(image);

        iv.setFitWidth(200);
        iv.setPreserveRatio(true);
        iv.getStyleClass().add("gallery-image");

        // 2.1.2 Interactividad básica
        iv.setOnMouseEntered(e -> iv.setOpacity(0.7));
        iv.setOnMouseExited(e -> iv.setOpacity(1.0));

        return iv;
    }
    public void setImagesFromResponse(Set<MemeResponse> likedMemes) {
        // 1.1.1 Limpiar la cuadrícula antes de cargar
        galleryGrid.getChildren().clear();

        if (likedMemes == null) return;

        for (MemeResponse meme : likedMemes) {
            // 1.1.2 Creamos el ImageView usando la URL del objeto
            ImageView imageView = createThumbnail(meme.getUrl());

            // Opcional: Podría añadir un Tooltip con el título del meme
            Tooltip.install(imageView, new Tooltip(meme.getTitle()));

            galleryGrid.getChildren().add(imageView);
        }
    }

}