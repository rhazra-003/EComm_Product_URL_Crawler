package com.project.ecommerce_crawler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.function.client.WebClient;

import com.project.ecommerce_crawler.model.Domain;
import com.project.ecommerce_crawler.model.ProductUrl;
import com.project.ecommerce_crawler.repository.DomainRepository;
import com.project.ecommerce_crawler.repository.ProductUrlRepository;
import com.project.ecommerce_crawler.service.WebCrawlerService;

@SpringBootTest
@ActiveProfiles("test")
public class EcommerceCrawlerApplicationTests {
    
    @Autowired
    private WebCrawlerService crawlerService;
    
    @SuppressWarnings("removal")
	@MockBean
    private WebClient webClient;
    
    @SuppressWarnings("removal")
	@MockBean
    private DomainRepository domainRepository;
    
    @SuppressWarnings("removal")
	@MockBean
    private ProductUrlRepository productUrlRepository;
    
    @Test
    public void testExtractProductUrls() {
        String html = """
            <html>
                <body>
                    <a href="/product/123">Product 1</a>
                    <a href="/item/456">Product 2</a>
                    <a href="/about">About Us</a>
                    <a href="https://www.example.com/product/789">External Product</a>
                </body>
            </html>
            """;
        
        Domain domain = new Domain();
        domain.setUrl("https://www.example.com");
        
        List<ProductUrl> productUrls = crawlerService.extractProductUrls(domain, html);
        
        assertEquals(2, productUrls.size());
        assertTrue(productUrls.stream().anyMatch(u -> u.getUrl().equals("https://www.example.com/product/123")));
        assertTrue(productUrls.stream().anyMatch(u -> u.getUrl().equals("https://www.example.com/item/456")));
    }
    
    @Test
    public void testIsProductPage() {
        assertTrue(crawlerService.isProductPage("https://www.example.com/product/123"));
        assertTrue(crawlerService.isProductPage("https://www.example.com/item/456"));
        assertTrue(crawlerService.isProductPage("https://www.example.com/p/789"));
        assertFalse(crawlerService.isProductPage("https://www.example.com/about"));
    }
}
