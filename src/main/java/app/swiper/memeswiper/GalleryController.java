package app.swiper.memeswiper;

import app.memeapi.requester.MemeResponse;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView; // IMPORTACIÓN CORRECTA
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.TilePane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Set;

public class GalleryController {
    @FXML private TilePane galleryGrid;

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

    private ImageView createThumbnail(MemeResponse meme, Image image) {

        ImageView iv = new ImageView(image);
        iv.setFitWidth(300);
        iv.setPreserveRatio(true);
        iv.getStyleClass().add("gallery-image");

        iv.setOnMouseClicked(event -> {
            // Aquí defines qué pasa al hacer clic
            abrirVentanaDetalle(meme, image);
        });

        iv.setOnMouseEntered(e -> iv.setOpacity(0.7));
        iv.setOnMouseExited(e -> iv.setOpacity(1.0));

        return iv;
    }
    public void setImagesFromResponse(Set<MemeResponse> likedMemes) {
        galleryGrid.getChildren().clear();
        if (likedMemes == null || likedMemes.isEmpty()) return;

        // 1. Usar un hilo para que la galería no congele la app al abrirse
        new Thread(() -> {
            for (MemeResponse meme : likedMemes) {
                // 2. Carga asíncrona real: añadimos el parámetro 'true' al final
                // Bajamos la resolución a 300 para ahorrar RAM
                Image image = new Image(meme.getUrl(), 300, 0, true, true, true);

                // 3. Creamos el ImageView (esto sigue siendo ligero)
                ImageView iv = createThumbnail(meme, image);

                // 4. Solo volvemos al hilo de UI para meter la imagen en la rejilla
                javafx.application.Platform.runLater(() -> {
                    galleryGrid.getChildren().add(iv);
                });
            }
        }).start();
    }
    public void abrirVentanaDetalle(MemeResponse meme, Image img) {
        try {
            // 1. Cargar el FXML de la nueva ventana
            FXMLLoader loader = new FXMLLoader(getClass().getResource("MemeDetailView.fxml"));
            Parent root = loader.load();

            DetailController controller = loader.getController();
            controller.setMemeData(meme, img); //Paso la informacion con el meme a cargar
            // 2. Crear el nuevo escenario (Stage)
            Stage newStage = new Stage();
            newStage.setTitle(meme.getTitle());

            // 3. CONFIGURAR EL BLOQUEO (La clave)
            newStage.initModality(Modality.APPLICATION_MODAL);

            // 4. (Opcional) Vincularla a la ventana principal
            // Esto hace que si minimizas la principal, esta también se gestione
            newStage.initOwner(galleryGrid.getScene().getWindow());
            // 5. Mostrar y esperar
            newStage.setScene(new Scene(root));
            newStage.initModality(Modality.APPLICATION_MODAL);
            newStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}