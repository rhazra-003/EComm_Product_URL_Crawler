package com.project.ecommerce_crawler.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.ecommerce_crawler.model.CrawlStatus;
import com.project.ecommerce_crawler.model.Domain;

import java.util.List;

public interface DomainRepository extends JpaRepository<Domain, Long> {
    List<Domain> findByStatus(CrawlStatus status);
    
    long countByStatus(CrawlStatus status);
    
    List<Domain> findByStatusIn(List<CrawlStatus> statuses);
    
    boolean existsByUrl(String url);
    
    List<Domain> findAllByOrderByLastCrawledAtDesc();
}
