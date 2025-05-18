package com.project.ecommerce_crawler.model;

import lombok.Data;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Data
@Entity
public class Domain {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String url;
    
    @Enumerated(EnumType.STRING)
    private CrawlStatus status = CrawlStatus.PENDING;
    
    private LocalDateTime lastCrawledAt;
    
}
