package com.project.ecommerce_crawler.service;

import com.google.common.hash.BloomFilter;
import com.project.ecommerce_crawler.model.CrawlStatus;
import com.project.ecommerce_crawler.model.Domain;
import com.project.ecommerce_crawler.model.ProductUrl;
import com.project.ecommerce_crawler.repository.DomainRepository;
import com.project.ecommerce_crawler.repository.ProductUrlRepository;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import lombok.RequiredArgsConstructor;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WebCrawlerService {
    private static final List<String> PRODUCT_PATH_KEYWORDS = 
            List.of("/product/", "/item/", "/p/", "/pr/", "-p-", "/prod/");
    
    private final DomainRepository domainRepository;
    private final ProductUrlRepository productUrlRepository;
    private final WebClient webClient;
    private final BloomFilter<String> urlBloomFilter;
    
    @Cacheable("robotsTxt")
    public Mono<String> fetchRobotsTxt(String baseUrl) {
        return webClient.get()
                .uri(baseUrl + "/robots.txt")
                .retrieve()
                .bodyToMono(String.class)
                .onErrorResume(e -> Mono.just(""));
    }
    
    public Flux<ProductUrl> crawlDomain(Domain domain) {
        domain.setStatus(CrawlStatus.IN_PROGRESS);
        domainRepository.save(domain);
        
        return fetchRobotsTxt(domain.getUrl())
                .flatMapMany(robotsTxt -> {
                    Set<String> disallowedPaths = parseRobotsTxt(robotsTxt);
                    return crawlPage(domain, domain.getUrl(), disallowedPaths);
                })
                .onErrorResume(e -> {
                    domain.setStatus(CrawlStatus.FAILED);
                    domainRepository.save(domain);
                    return Flux.empty();
                })
                .doOnComplete(() -> {
                    domain.setStatus(CrawlStatus.COMPLETED);
                    domain.setLastCrawledAt(LocalDateTime.now());
                    domainRepository.save(domain);
                });
    }
    
    private Flux<ProductUrl> crawlPage(Domain domain, String url, Set<String> disallowedPaths) {
        if (isUrlDisallowed(url, disallowedPaths) || urlBloomFilter.mightContain(url)) {
            return Flux.empty();
        }
        
        urlBloomFilter.put(url);
        
        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .flatMapMany(html -> {
                    List<ProductUrl> productUrls = extractProductUrls(domain, html);
                    saveProductUrls(productUrls);
                    
                    if (isProductPage(url)) {
                        return Flux.fromIterable(productUrls);
                    } else {
                        List<String> links = extractLinks(html, domain.getUrl());
                        return Flux.fromIterable(links)
                                .parallel()
                                .runOn(Schedulers.boundedElastic())
                                .flatMap(link -> crawlPage(domain, link, disallowedPaths))
                                .sequential();
                    }
                })
                .onErrorResume(e -> Flux.empty());
    }
    
    public boolean isProductPage(String url) {
        return PRODUCT_PATH_KEYWORDS.stream().anyMatch(url::contains);
    }
    
    public List<ProductUrl> extractProductUrls(Domain domain, String html) {
        Document doc = Jsoup.parse(html);
        Elements links = doc.select("a[href]");
        
        return links.stream()
                .map(link -> link.attr("href"))
                .filter(this::isProductPage)
                .map(href -> normalizeUrl(domain.getUrl(), href))
                .filter(href -> href.startsWith(domain.getUrl()))
                .distinct()
                .map(href -> {
                    ProductUrl productUrl = new ProductUrl();
                    productUrl.setUrl(href);
                    productUrl.setDomain(domain);
                    productUrl.setDiscoveredAt(LocalDateTime.now());
                    return productUrl;
                })
                .collect(Collectors.toList());
    }
    
    private List<String> extractLinks(String html, String baseUrl) {
        Document doc = Jsoup.parse(html);
        Elements links = doc.select("a[href]");
        
        return links.stream()
                .map(link -> link.attr("href"))
                .map(href -> normalizeUrl(baseUrl, href))
                .filter(href -> href.startsWith(baseUrl))
                .distinct()
                .collect(Collectors.toList());
    }
    
    private String normalizeUrl(String baseUrl, String href) {
        try {
            if (href.startsWith("http")) {
                return href;
            } else if (href.startsWith("/")) {
                URI baseUri = new URI(baseUrl);
                return baseUri.getScheme() + "://" + baseUri.getHost() + href;
            } else {
                return baseUrl + "/" + href;
            }
        } catch (URISyntaxException e) {
            return href;
        }
    }
    
    private Set<String> parseRobotsTxt(String robotsTxt) {
        return Arrays.stream(robotsTxt.split("\n"))
                .filter(line -> line.startsWith("Disallow:"))
                .map(line -> line.substring("Disallow:".length()).trim())
                .collect(Collectors.toSet());
    }
    
    private boolean isUrlDisallowed(String url, Set<String> disallowedPaths) {
        URI uri = URI.create(url);
        String path = uri.getPath();
        return disallowedPaths.stream().anyMatch(path::startsWith);
    }
    
    @Transactional
    private void saveProductUrls(List<ProductUrl> productUrls) {
        List<ProductUrl> newUrls = productUrls.stream()
                .filter(url -> !productUrlRepository.existsByUrl(url.getUrl()))
                .collect(Collectors.toList());
        
        if (!newUrls.isEmpty()) {
            productUrlRepository.saveAll(newUrls);
        }
    }
}