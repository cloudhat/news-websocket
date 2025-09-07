package assignment.newswebsocket.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "TRANSLATED_NEWS")
public class TranslatedNews {

    @Id
    private String id;

    @Column
    private String title;

    @Column
    private String content;

    @Column
    private LocalDateTime publishedAt;
}
