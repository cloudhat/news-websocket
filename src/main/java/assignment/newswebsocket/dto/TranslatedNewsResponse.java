package assignment.newswebsocket.dto;

import assignment.newswebsocket.entity.TranslatedNews;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class TranslatedNewsResponse {

    private String id;
    private String title;
    private String body;

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime publishedAt;

    public TranslatedNewsResponse(TranslatedNews translatedNews) {
        this.id = translatedNews.getId();
        this.title = translatedNews.getTitle();
        this.body = translatedNews.getContent();
        this.publishedAt = translatedNews.getPublishedAt();
    }
}
