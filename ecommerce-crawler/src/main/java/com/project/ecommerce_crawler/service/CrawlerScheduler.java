package com.project.ecommerce_crawler.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.project.ecommerce_crawler.model.CrawlStatus;
import com.project.ecommerce_crawler.model.Domain;
import com.project.ecommerce_crawler.repository.DomainRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CrawlerScheduler {
    private final WebCrawlerService crawlerService;
    private final DomainRepository domainRepository;
    
    @Scheduled(fixedDelay = 24 * 60 * 60 * 1000) // Run daily
    public void scheduleCrawling() {
        List<Domain> domainsToCrawl = domainRepository.findByStatusIn(
                List.of(CrawlStatus.PENDING, CrawlStatus.FAILED));
        
        Flux.fromIterable(domainsToCrawl)
                .parallel()
                .runOn(Schedulers.boundedElastic())
                .flatMap(crawlerService::crawlDomain)
                .sequential()
                .subscribe();
    }
    
    public void initializeDomains(List<String> domainUrls) {
        domainUrls.forEach(url -> {
            if (!domainRepository.existsByUrl(url)) { 
                Domain domain = new Domain();
                domain.setUrl(url);
                domain.setStatus(CrawlStatus.PENDING);
                domainRepository.save(domain);
            }
        });
    }
}
