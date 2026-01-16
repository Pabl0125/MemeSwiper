package app.memeapi.requester;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MemeResponse {

    private String postLink;
    private String subreddit;
    private String title;
    private String url; // Main image (high quality one)
    private boolean nsfw;
    private boolean spoiler;
    private String author;
    private int ups;

    private List<String> preview;

    public MemeResponse() {}

    // Getters y Setters
    public String getSubreddit() { return subreddit; }

    public String getTitle() { return title; }

    public String getUrl() { return url; }

    public String getAuthor() { return author; }

    public int getUps() { return ups; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MemeResponse that)) return false;
        return Objects.equals(url, that.url);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(url);
    }
}