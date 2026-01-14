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

    public Comment (String author, String content, String createdAt) {
        this.author = author;
        this.content = content;
        this.createdAt = createdAt;
    }
}
