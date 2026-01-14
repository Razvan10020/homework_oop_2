package main.Commands;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

@Data
@JsonPropertyOrder({
        "author", "content", "createdAt"
})
public class Comment {
    private String author;
    private String content;
    private String createdAt;

    public Comment(final String author, final String content, final String createdAt) {
        this.author = author;
        this.content = content;
        this.createdAt = createdAt;
    }
}
