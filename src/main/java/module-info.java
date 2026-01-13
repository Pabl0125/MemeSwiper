module app.swiper.memeswiper {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;
    requires com.fasterxml.jackson.databind;
    requires java.desktop;

    opens app.swiper.memeswiper to javafx.fxml;

    opens app.memeapi.requester to com.fasterxml.jackson.databind;

    exports app.swiper.memeswiper;
}