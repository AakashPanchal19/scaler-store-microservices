-- Initialize Databases for Scaler Store Microservices
CREATE DATABASE IF NOT EXISTS user_db;
CREATE DATABASE IF NOT EXISTS product_db;
CREATE DATABASE IF NOT EXISTS order_db;
CREATE DATABASE IF NOT EXISTS payment_db;

-- Grant privileges
GRANT ALL PRIVILEGES ON user_db.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON product_db.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON order_db.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON payment_db.* TO 'root'@'%';
FLUSH PRIVILEGES;

-- Seed Data for Product Catalog Service
USE product_db;

CREATE TABLE IF NOT EXISTS categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255),
    slug VARCHAR(100) UNIQUE
);

CREATE TABLE IF NOT EXISTS products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sku VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL,
    stock_quantity INT NOT NULL DEFAULT 0,
    image_url VARCHAR(500),
    category_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES categories(id)
);

-- Seed Categories
INSERT IGNORE INTO categories (id, name, description, slug) VALUES
(1, 'Electronics', 'Smartphones, Laptops, Audio Gear, and Accessories', 'electronics'),
(2, 'Footwear & Apparel', 'Running shoes, casual sneakers, and sportswear', 'footwear-apparel'),
(3, 'Home & Kitchen', 'Smart appliances, kitchenware, and decor', 'home-kitchen'),
(4, 'Books & Media', 'Technical literature, programming books, and novels', 'books-media');

-- Seed Products
INSERT IGNORE INTO products (id, sku, name, description, price, stock_quantity, image_url, category_id) VALUES
(1, 'SKU-MACBOOK-M3', 'Apple MacBook Pro 16-inch M3 Max', '36GB Unified Memory, 1TB SSD Storage, Liquid Retina XDR display, Space Black finish', 2499.00, 45, 'https://images.unsplash.com/photo-1517336714731-489689fd1ca8', 1),
(2, 'SKU-SONY-WH1000XM5', 'Sony WH-1000XM5 Wireless Noise-Canceling Headphones', 'Industry-leading noise cancellation, 30-hour battery life, Multipoint connection', 399.99, 120, 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e', 1),
(3, 'SKU-LOGI-MXMASTER3S', 'Logitech MX Master 3S Performance Wireless Mouse', 'Quiet clicks, 8K DPI any-surface tracking, ergonomic design for programmers', 99.99, 250, 'https://images.unsplash.com/photo-1527864550417-7fd91fc51a46', 1),
(4, 'SKU-NIKE-PEGASUS40', 'Nike Air Zoom Pegasus 40 Running Shoes', 'Responsive cushioning, engineered mesh upper, durable waffle outsole', 130.00, 85, 'https://images.unsplash.com/photo-1542291026-7eec264c27ff', 2),
(5, 'SKU-DELL-U2723QE', 'Dell UltraSharp 27-inch 4K USB-C Hub Monitor', 'IPS Black technology, 98% DCI-P3 color gamut, 90W power delivery', 579.50, 60, 'https://images.unsplash.com/photo-1527443224154-c4a3942d3acf', 1),
(6, 'SKU-BOOK-DDIA', 'Designing Data-Intensive Applications by Martin Kleppmann', 'The definitive guide to distributed systems, storage engines, and consensus', 42.00, 300, 'https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c', 4);
