package com.project.ecommerce_crawler.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.ecommerce_crawler.model.CrawlStatus;
import com.project.ecommerce_crawler.model.Domain;
import com.project.ecommerce_crawler.model.ProductUrl;
import com.project.ecommerce_crawler.repository.DomainRepository;
import com.project.ecommerce_crawler.repository.ProductUrlRepository;
import com.project.ecommerce_crawler.service.CrawlerScheduler;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/crawler")
@RequiredArgsConstructor
public class CrawlerController {
    private final CrawlerScheduler crawlerScheduler;
    private final DomainRepository domainRepository;
    private final ProductUrlRepository productUrlRepository;
    
    @PostMapping("/init")
    public ResponseEntity<String> initializeCrawler(@RequestBody List<String> domains) {
        crawlerScheduler.initializeDomains(domains);
        return ResponseEntity.ok("Domains initialized for crawling");
    }
    
    @PostMapping("/start")
    public ResponseEntity<String> startCrawling() {
        crawlerScheduler.scheduleCrawling();
        return ResponseEntity.ok("Crawling process started");
    }
    
    @GetMapping("/domains")
    public ResponseEntity<List<Domain>> getAllDomains() {
        return ResponseEntity.ok(domainRepository.findAll());
    }
    
    @GetMapping("/products/{domainId}")
    public ResponseEntity<List<ProductUrl>> getProductUrls(@PathVariable Long domainId) {
        return domainRepository.findById(domainId)
                .map(domain -> ResponseEntity.ok(productUrlRepository.findByDomain(domain)))
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getCrawlerStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("totalDomains", domainRepository.count());
        status.put("completedDomains", domainRepository.countByStatus(CrawlStatus.COMPLETED));
        status.put("pendingDomains", domainRepository.countByStatus(CrawlStatus.PENDING));
        status.put("failedDomains", domainRepository.countByStatus(CrawlStatus.FAILED));
        status.put("inProgressDomains", domainRepository.countByStatus(CrawlStatus.IN_PROGRESS));
        status.put("totalProductUrls", productUrlRepository.count());

        return ResponseEntity.ok(status);
    }
}
