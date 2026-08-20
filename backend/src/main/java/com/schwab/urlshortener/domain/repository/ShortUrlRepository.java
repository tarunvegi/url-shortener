package com.schwab.urlshortener.domain.repository;

import com.schwab.urlshortener.domain.model.ShortUrl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ShortUrlRepository extends JpaRepository<ShortUrl, String> {

    Optional<ShortUrl> findByCode(String code);

    Optional<ShortUrl> findByCustomAlias(String customAlias);

    boolean existsByCode(String code);

    boolean existsByCustomAlias(String customAlias);

    List<ShortUrl> findAllByOrderByCreatedAtDesc();

    List<ShortUrl> findByExpiresAtBeforeAndExpiresAtIsNotNull(LocalDateTime dateTime);
}
