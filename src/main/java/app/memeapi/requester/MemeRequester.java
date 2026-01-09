package app.memeapi.requester;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;

public class MemeRequester {

    private static final String API_LINK = "https://meme-api.com/gimme";

    public static MemeResponse request() throws Exception {

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_LINK))
                .header("Accept", "application/json")
                .GET()
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new Exception("Error en la API: Código " + response.statusCode());
            }
            //Object mapper of the jackson library
            ObjectMapper mapper = new ObjectMapper();

            MemeResponse meme = mapper.readValue(response.body(), MemeResponse.class);

            return meme;

        } catch (IOException | InterruptedException e) {
            throw new Exception("Fallo de conexión al obtener el meme", e);
        }
    }
}