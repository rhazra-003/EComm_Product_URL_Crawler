package com.project.ecommerce_crawler;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.project.ecommerce_crawler.controller.CrawlerController;
import com.project.ecommerce_crawler.repository.DomainRepository;
import com.project.ecommerce_crawler.repository.ProductUrlRepository;
import com.project.ecommerce_crawler.service.CrawlerScheduler;

@WebFluxTest(controllers = CrawlerController.class)
public class CrawlerControllerTest {
    
    @Autowired
    private WebTestClient webTestClient;
    
    @SuppressWarnings("removal")
	@MockBean
    private CrawlerScheduler crawlerScheduler;
    
    @SuppressWarnings("removal")
	@MockBean
    private DomainRepository domainRepository;
    
    @SuppressWarnings("removal")
	@MockBean
    private ProductUrlRepository productUrlRepository;
    
    @Test
    public void testInitializeCrawler() {
        webTestClient.post()
                .uri("/api/crawler/init")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(List.of("https://www.example.com"))
                .exchange()
                .expectStatus().isOk();
    }
}
