package assignment.newswebsocket.repository;

import assignment.newswebsocket.entity.TranslatedNews;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TranslatedNewsRepository extends JpaRepository<TranslatedNews, String> {
}
