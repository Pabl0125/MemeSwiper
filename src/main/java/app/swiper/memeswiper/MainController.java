package app.swiper.memeswiper;

import app.memeapi.requester.MemeRequester;
import app.memeapi.requester.MemeResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import javafx.animation.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.util.Duration;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;

public class MainController {

    // Contenedores
    @FXML private StackPane memeViewer;
    @FXML private VBox userCard1; // Tarjeta de fondo
    @FXML private VBox userCard2; // Tarjeta de frente (la que se mueve)
    @FXML private VBox sideBar;
    @FXML private StackPane contentArea;
    // Imágenes
    @FXML private ImageView card1;
    @FXML private ImageView card2;
    // Botones Globales
    @FXML private Button likeButton;
    @FXML private Button dislikeButton;
    @FXML private Button starButton;
    // Sidebar buttons (y btnToggle)
    @FXML private Button btnToggle;
    @FXML private TextField txtSubreddit;

    // Persistencia y Datos
    private HashSet<String> showedMemes = new HashSet<>();
    private MemeRequester memeRequester = new MemeRequester();
    private HashSet<MemeResponse> likedMemes = new HashSet<>();
    private int apiCallsCounter = 0;
    private boolean isExpanded = true;
    private final double SIDEBAR_WIDTH = 250;
    private final String DEFAULT_IMAGE_PATH = "/app/swiper/memeswiper/defaultImage.jpg";
    private Image LOADING_IMAGE = new Image(getClass().getResource("/app/swiper/memeswiper/loadingImage.gif").toExternalForm());
    private Image PAGE_NOT_FOUND_IMAGE = new Image(getClass().getResource("/app/swiper/memeswiper/pageNotFound.png").toExternalForm());
    private Image NO_MORE_MEMES  = new Image(getClass().getResource("/app/swiper/memeswiper/noMoreMemes.jpg").toExternalForm());
    @FXML
    public void initialize() {
        // 1. Clip del sidebar
        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(sideBar.widthProperty());
        clip.heightProperty().bind(sideBar.heightProperty());
        sideBar.setClip(clip);
        starButton.setOnAction(e -> handleDownload());
        // 2. Cargar historial
        likedMemes = loadLikedMemes();

        // 3. Iniciar lógica si estamos en la vista principal
        if (memeViewer != null) {
            rebindMemeLogic();
        }
    }
    public MemeResponse getActiveMeme() {
        // 1. Obtenemos lo que haya en el userData de la carta frontal
        Object data = card2.getUserData();

        // 2. Verificamos si es una instancia válida de MemeResponse
        if (data instanceof MemeResponse) {
            return (MemeResponse) data;
        }

        // Si está cargando o no hay nada, devolvemos null
        return null;
    }
    private void handleDownload(){
        MemeResponse currentMeme = getActiveMeme();
        if(starButton.isDisable() || (getActiveMeme() == null)) return;
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Download Meme");

        String url = currentMeme.getUrl();
        String extension = url.substring(url.lastIndexOf("."));
        if (extension.length() > 4) extension = ".jpg"; // Fallback por si la URL tiene parámetros

        fileChooser.setInitialFileName(currentMeme.getTitle().replaceAll("[^a-zA-Z0-9]", "_") + extension);
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image", "*" + extension));

        // 2. Mostrar la ventana de guardado
        File file = fileChooser.showSaveDialog(card2.getScene().getWindow());
        if (file != null) {
            // 3. Ejecutar la descarga en un hilo aparte para no congelar la UI
            new Thread(() -> {
                try (BufferedInputStream in = new BufferedInputStream(new URL(url).openStream());
                     FileOutputStream fileOutputStream = new FileOutputStream(file)) {

                    byte[] dataBuffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                        fileOutputStream.write(dataBuffer, 0, bytesRead);
                    }
                } catch (IOException e) {}
            }).start();
        }
    }
    private void loadMeme(ImageView iv) {
        iv.setImage(LOADING_IMAGE);
        iv.setUserData(null);

        Thread downloadThread = new Thread(() -> {
            MemeResponse response = null;
            int attempts = 0;
            boolean foundNew = false;

            try {
                while (attempts < 5 && !foundNew) {
                    response = memeRequester.request();
                    attempts++;

                    // Si el meme es válido y NO ha sido mostrado antes
                    if (response != null && !showedMemes.contains(response.getUrl())) {
                        foundNew = true;
                    }
                }
            } catch (Exception e) {
                System.err.println("Error en petición API: " + e.getMessage());
            }

            final MemeResponse finalResponse = response;
            final boolean isMemeValid = foundNew;

            javafx.application.Platform.runLater(() -> {
                if (isMemeValid) {
                    // CASO ÉXITO: Meme nuevo encontrado
                    String finalUrl = finalResponse.getUrl();
                    showedMemes.add(finalUrl);
                    iv.setUserData(finalResponse);
                    iv.setImage(new Image(finalUrl, true));
                    System.out.println("Meme cargado con éxito.");
                }
                else {
                    // CASO FALLO: O se agotaron los 5 intentos sin éxito, o hubo error de red
                    // Aquí forzamos el cambio de imagen para que el GIF desaparezca
                    if (showedMemes.size() > 0) {
                        iv.setImage(NO_MORE_MEMES);
                        System.out.println("Se alcanzó el límite de 5 intentos.");
                    } else {
                        // Si ni siquiera hay memes previos, probablemente sea falta de internet
                        iv.setImage(PAGE_NOT_FOUND_IMAGE);
                        System.out.println("Error de conexión inicial.");
                    }
                    iv.setUserData(null);
                }
            });
        });

        downloadThread.setDaemon(true);
        downloadThread.start();
    }

    private void animateSwipe(boolean isLike) {
        Duration duration = Duration.millis(450);
        double deltaX = isLike ? 750 : -750;
        double rotateAngle = isLike ? 35 : -35;
        if(card2.getUserData() == null) return; //todavia no tenemos la imagen en pantalla, esta cargando
        // 1. LOGICA DE GUARDADO (Antes de animar)
        if (isLike) {
            // Recuperamos el objeto "pegado" a la imagen que estamos viendo
            Object data = card2.getUserData();
            // Verificamos que sea un MemeResponse válido
            if (data instanceof MemeResponse meme) {
                if (likedMemes.add(meme)) {
                    saveLikedMemes(likedMemes); // Guardado en tiempo real
                    System.out.println("Like guardado: " + meme.getTitle());
                }
            }
        }

        // 2. CONFIGURACIÓN DE ANIMACIONES
        TranslateTransition translate = new TranslateTransition(duration, userCard2);
        translate.setToX(deltaX);
        RotateTransition rotate = new RotateTransition(duration, userCard2);
        rotate.setToAngle(rotateAngle);
        FadeTransition fadeOut = new FadeTransition(duration, userCard2);
        fadeOut.setToValue(0);

        FadeTransition fadeIn = new FadeTransition(duration, userCard1);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);

        ParallelTransition parallel = new ParallelTransition(translate, rotate, fadeOut, fadeIn);

        parallel.setOnFinished(event -> {
            // 3. SINCRONIZACIÓN VISUAL Y DE DATOS (Intercambio de antorcha)

            // Imagen: Fondo -> Frente
            card2.setImage(card1.getImage());
            // Datos: Fondo -> Frente (IMPORTANTE)
            card2.setUserData(card1.getUserData());

            // Reset visual
            userCard2.setTranslateX(0);
            userCard2.setRotate(0);
            userCard2.setOpacity(1.0);
            userCard1.setOpacity(0.0);

            // Cargar nuevo meme en el fondo
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

    private void rebindMemeLogic() {
        if (memeViewer == null || likeButton == null) return;

        card1.fitWidthProperty().bind(memeViewer.widthProperty());
        card1.fitHeightProperty().bind(memeViewer.heightProperty());
        card2.fitWidthProperty().bind(memeViewer.widthProperty());
        card2.fitHeightProperty().bind(memeViewer.heightProperty());

        likeButton.setOnAction(event -> animateSwipe(true));
        dislikeButton.setOnAction(event -> animateSwipe(false));

        userCard1.setOpacity(0.0);
        userCard1.setMouseTransparent(true);

        // Carga inicial si están vacías
        if (card2.getImage() == null) loadMeme(card2);
        if (card1.getImage() == null) loadMeme(card1);
    }

    private void setView(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            if (fxmlFile.contains("memeViewerContent")) {
                loader.setController(this);
            }

            Node node = loader.load();

            // Inyectar datos a la galería si corresponde
            if (fxmlFile.contains("GalleryView")) {
                GalleryController galleryCtrl = loader.getController();
                galleryCtrl.setImagesFromResponse(likedMemes);
            }

            contentArea.getChildren().remove(btnToggle);
            contentArea.getChildren().setAll(node);
            contentArea.getChildren().add(btnToggle);

            if (fxmlFile.contains("memeViewerContent")) {
                rebindMemeLogic();
            }
        } catch (IOException e) {
            System.err.println("Error setView: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML void showGallery(ActionEvent event) { setView("GalleryView.fxml"); }
    @FXML void showMainViewer(ActionEvent event) { setView("memeViewerContent.fxml"); }

    public void saveLikedMemes(HashSet<MemeResponse> favouriteMemes) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            mapper.writeValue(new File("likedMemes.json"), favouriteMemes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public HashSet<MemeResponse> loadLikedMemes() {
        File archivo = new File("likedMemes.json");
        if (!archivo.exists() || archivo.length() == 0) return new HashSet<>();

        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(archivo, new TypeReference<HashSet<MemeResponse>>(){});
        } catch (IOException e) {
            System.err.println("Error carga JSON: " + e.getMessage());
            return new HashSet<>();
        }
    }
    private void setControlsLocked(boolean locked) {
        likeButton.setDisable(locked);
        dislikeButton.setDisable(locked);
        starButton.setDisable(locked);

        // Opcional: Cambiar el cursor para dar feedback visual
        if (locked) {
            memeViewer.setCursor(javafx.scene.Cursor.WAIT);
        } else {
            memeViewer.setCursor(javafx.scene.Cursor.DEFAULT);
        }
    }
    @FXML
    private void handleChangeSubreddit(ActionEvent event) {
        // Obtenemos el texto y limpiamos espacios
        String subInput = txtSubreddit.getText().trim();

        // Feedback visual en consola
        System.out.println("Cambiando fuente a: " + (subInput.isEmpty() ? "Global" : subInput));

        // 1. Configurar el Requester
        memeRequester.setSubreddit(subInput);

        // 2. Limpieza de estado para permitir ver memes repetidos si cambiamos de tema
        showedMemes.clear();

        // 3. Forzar recarga visual
        forceReloadCards();

        // 4. (Opcional) Quitar el foco del TextField para que el usuario pueda usar las flechas o teclas
        memeViewer.requestFocus();
    }
    private void forceReloadCards() {
        // 1. Limpiamos datos lógicos de ambas tarjetas
        card1.setUserData(null);
        card2.setUserData(null);

        // 2. Intentamos cargar la imagen de feedback visual
        try {
            var resource = getClass().getResource("/app/swiper/memeswiper/loadingImage.gif");

            if (resource != null) {
                // Solo creamos el objeto Image una vez para ahorrar memoria
                Image imgLoad = new Image(resource.toExternalForm());
                card1.setImage(imgLoad);
                card2.setImage(imgLoad);
            }
        } catch (Exception e) {
            System.err.println("No se pudo cargar la imagen de carga: " + e.getMessage());
        }

        // 3. Disparamos la carga asíncrona de los nuevos memes
        // Es mejor cargar primero el de card2 (el que está al frente)
        loadMeme(card2);
        loadMeme(card1);
    }
    public HashSet<MemeResponse> getLikedMemes() { return likedMemes; }
}
