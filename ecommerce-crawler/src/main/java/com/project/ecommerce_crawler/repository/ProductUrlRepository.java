package com.project.ecommerce_crawler.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.ecommerce_crawler.model.Domain;
import com.project.ecommerce_crawler.model.ProductUrl;

public interface ProductUrlRepository extends JpaRepository<ProductUrl, Long> {
    boolean existsByUrl(String url);
    List<ProductUrl> findByDomain(Domain domain);
}