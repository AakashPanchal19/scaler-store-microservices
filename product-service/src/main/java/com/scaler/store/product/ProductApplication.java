package com.scaler.store.product;

import com.scaler.store.product.entity.Category;
import com.scaler.store.product.entity.Product;
import com.scaler.store.product.repository.CategoryRepository;
import com.scaler.store.product.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;

import java.math.BigDecimal;

@SpringBootApplication
@EnableDiscoveryClient
@EnableCaching
public class ProductApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProductApplication.class, args);
    }

    @Bean
    public CommandLineRunner initData(CategoryRepository categoryRepo, ProductRepository productRepo) {
        return args -> {
            if (categoryRepo.count() == 0) {
                Category c1 = categoryRepo.save(new Category("Electronics", "Gadgets and gear", "electronics"));
                Category c2 = categoryRepo.save(new Category("Footwear & Apparel", "Shoes and apparel", "footwear-apparel"));
                Category c3 = categoryRepo.save(new Category("Home & Kitchen", "Appliances and decor", "home-kitchen"));
                Category c4 = categoryRepo.save(new Category("Books & Media", "Technical literature and books", "books-media"));

                Product p1 = new Product();
                p1.setSku("SKU-MACBOOK-M3");
                p1.setName("Apple MacBook Pro 16-inch M3 Max");
                p1.setDescription("36GB Unified Memory, 1TB SSD Storage, Liquid Retina XDR display, Space Black finish");
                p1.setPrice(new BigDecimal("2499.00"));
                p1.setStockQuantity(45);
                p1.setImageUrl("https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=500&auto=format&fit=crop&q=60");
                p1.setCategory(c1);
                productRepo.save(p1);

                Product p2 = new Product();
                p2.setSku("SKU-SONY-WH1000XM5");
                p2.setName("Sony WH-1000XM5 Wireless Headphones");
                p2.setDescription("Industry-leading noise cancellation, 30-hour battery life, Multipoint connection");
                p2.setPrice(new BigDecimal("399.99"));
                p2.setStockQuantity(120);
                p2.setImageUrl("https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=500&auto=format&fit=crop&q=60");
                p2.setCategory(c1);
                productRepo.save(p2);

                Product p3 = new Product();
                p3.setSku("SKU-LOGI-MXMASTER3S");
                p3.setName("Logitech MX Master 3S Wireless Mouse");
                p3.setDescription("Quiet clicks, 8K DPI any-surface tracking, ergonomic design for programmers");
                p3.setPrice(new BigDecimal("99.99"));
                p3.setStockQuantity(250);
                p3.setImageUrl("https://images.unsplash.com/photo-1527864550417-7fd91fc51a46?w=500&auto=format&fit=crop&q=60");
                p3.setCategory(c1);
                productRepo.save(p3);

                Product p4 = new Product();
                p4.setSku("SKU-NIKE-PEGASUS40");
                p4.setName("Nike Air Zoom Pegasus 40 Shoes");
                p4.setDescription("Responsive cushioning, engineered mesh upper, durable waffle outsole");
                p4.setPrice(new BigDecimal("130.00"));
                p4.setStockQuantity(85);
                p4.setImageUrl("https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=500&auto=format&fit=crop&q=60");
                p4.setCategory(c2);
                productRepo.save(p4);

                Product p5 = new Product();
                p5.setSku("SKU-DELL-U2723QE");
                p5.setName("Dell UltraSharp 27-inch 4K Monitor");
                p5.setDescription("IPS Black technology, 98% DCI-P3 color gamut, 90W power delivery");
                p5.setPrice(new BigDecimal("579.50"));
                p5.setStockQuantity(60);
                p5.setImageUrl("https://images.unsplash.com/photo-1527443224154-c4a3942d3acf?w=500&auto=format&fit=crop&q=60");
                p5.setCategory(c1);
                productRepo.save(p5);

                Product p6 = new Product();
                p6.setSku("SKU-BOOK-DDIA");
                p6.setName("Designing Data-Intensive Applications");
                p6.setDescription("The definitive guide to distributed systems, storage engines, and consensus by Martin Kleppmann");
                p6.setPrice(new BigDecimal("42.00"));
                p6.setStockQuantity(300);
                p6.setImageUrl("https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=500&auto=format&fit=crop&q=60");
                p6.setCategory(c4);
                productRepo.save(p6);
            }
        };
    }
}
