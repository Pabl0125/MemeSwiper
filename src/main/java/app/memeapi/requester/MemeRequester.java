package app.memeapi.requester;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;

public class MemeRequester {
    private String subreddit = "memes";
    private MemeResponse lastResponse;
    public MemeResponse request() throws Exception {
        String API_LINK = "https://meme-api.com/gimme/" + this.subreddit;
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
            this.lastResponse = meme;
            return meme;

        } catch (IOException | InterruptedException e) {
            throw new Exception("Fallo de conexión al obtener el meme", e);
        }
    }

    public void setSubreddit(String subreddit) {
        this.subreddit = subreddit;

    }
    public String getSubreddit(){
        return this.subreddit;
    }
    public MemeResponse getLastResponse() {
        return this.lastResponse;
    }
}