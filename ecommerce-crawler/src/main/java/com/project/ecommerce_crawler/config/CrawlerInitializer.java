package com.project.ecommerce_crawler.config;


import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import com.project.ecommerce_crawler.model.Domain;
import com.project.ecommerce_crawler.repository.DomainRepository;

import java.util.List;

@Component
public class CrawlerInitializer implements CommandLineRunner {

    private final DomainRepository domainRepository;

    public CrawlerInitializer(DomainRepository domainRepository) {
        this.domainRepository = domainRepository;
    }

    @Override
    public void run(String... args) {
        List<String> domains = List.of(
                "https://www.virgio.com/",
                "https://www.tatacliq.com/",
                "https://nykaafashion.com/",
                "https://www.westside.com/"
        );

        domains.forEach(url -> {
            if (!domainRepository.existsByUrl(url)) {
                Domain domain = new Domain();
                domain.setUrl(url);
                domainRepository.save(domain);
            }
        });
    }
}
