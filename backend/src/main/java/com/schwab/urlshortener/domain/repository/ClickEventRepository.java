package com.schwab.urlshortener.domain.repository;

import com.schwab.urlshortener.domain.model.ClickEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ClickEventRepository extends JpaRepository<ClickEvent, String> {

    long countByShortCode(String shortCode);

    List<ClickEvent> findByShortCodeOrderByClickedAtDesc(String shortCode);

    @Query("SELECT DATE(c.clickedAt), COUNT(c) FROM ClickEvent c WHERE c.shortCode = :code AND c.clickedAt >= :since GROUP BY DATE(c.clickedAt) ORDER BY DATE(c.clickedAt)")
    List<Object[]> countClicksByDay(@Param("code") String code, @Param("since") LocalDateTime since);

    @Query("SELECT COALESCE(c.referrer, 'Direct'), COUNT(c) FROM ClickEvent c WHERE c.shortCode = :code GROUP BY COALESCE(c.referrer, 'Direct') ORDER BY COUNT(c) DESC")
    List<Object[]> topReferrers(@Param("code") String code);
}
