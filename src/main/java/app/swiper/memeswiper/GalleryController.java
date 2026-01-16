package app.swiper.memeswiper;

import app.memeapi.requester.MemeResponse;
import com.fasterxml.jackson.databind.ObjectMapper; // Asumiendo Jackson
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.TilePane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.Set;

public class GalleryController {

    @FXML private TilePane galleryGrid;

    // 1.1 Referencia local a los memes favoritos
    private Set<MemeResponse> likedMemes;
    private final ObjectMapper mapper = new ObjectMapper();
    private final String JSON_PATH = "likedMemes.json";


    public void saveLikedMemes(Set<MemeResponse> memes) {
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(JSON_PATH), memes);
            System.out.println("Sincronización con JSON exitosa.");
        } catch (IOException e) {
            System.err.println("Error al guardar el JSON: " + e.getMessage());
        }
    }


    public void deleteMeme(MemeResponse meme) {
        if (likedMemes != null && likedMemes.remove(meme)) {
            saveLikedMemes(likedMemes);
            // Refrescamos visualmente la galería
            setImagesFromResponse(likedMemes);
        }
    }


    public void setImagesFromResponse(Set<MemeResponse> likedMemes) {
        this.likedMemes = likedMemes; // Guardamos la referencia

        galleryGrid.getChildren().clear();
        if (likedMemes == null || likedMemes.isEmpty()) return;

        new Thread(() -> {
            for (MemeResponse meme : likedMemes) {
                Image image = new Image(meme.getUrl(), 300, 0, true, true, true);
                ImageView iv = createThumbnail(meme, image);

                Platform.runLater(() -> {
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
            abrirVentanaDetalle(meme, image);
        });

        iv.setOnMouseEntered(e -> iv.setOpacity(0.7));
        iv.setOnMouseExited(e -> iv.setOpacity(1.0));

        return iv;
    }

    public void abrirVentanaDetalle(MemeResponse meme, Image img) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("MemeDetailView.fxml"));
            Parent root = loader.load();

            DetailController controller = loader.getController();
            controller.setMemeData(meme, img);
            controller.setGalleryController(this); // IMPORTANTE: Pasar el controlador para permitir el borrado

            Stage newStage = new Stage();
            newStage.setTitle(meme.getTitle());
            newStage.initModality(Modality.APPLICATION_MODAL);
            newStage.initOwner(galleryGrid.getScene().getWindow());
            newStage.setScene(new Scene(root));
            newStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}