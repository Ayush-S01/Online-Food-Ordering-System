-- ============================================================
-- Online Food Ordering System - Database Schema
-- Database: food_ordering_system
-- ============================================================

CREATE DATABASE IF NOT EXISTS food_ordering_system
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE food_ordering_system;

-- ============================================================
-- 1. customers
-- ============================================================
CREATE TABLE IF NOT EXISTS customers (
    customer_id   INT          NOT NULL AUTO_INCREMENT,
    name          VARCHAR(100) NOT NULL,
    email         VARCHAR(150) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,  -- SHA-256 hash
    role          ENUM('CUSTOMER','ADMIN') NOT NULL DEFAULT 'CUSTOMER',
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (customer_id),
    UNIQUE KEY uk_customers_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 2. food_items
-- ============================================================
CREATE TABLE IF NOT EXISTS food_items (
    food_id     INT            NOT NULL AUTO_INCREMENT,
    name        VARCHAR(150)   NOT NULL,
    category    VARCHAR(80)    NOT NULL,
    cuisine     VARCHAR(80)    NOT NULL DEFAULT 'North Indian',
    description TEXT,
    price       DECIMAL(10, 2) NOT NULL,
    image_path  VARCHAR(300),
    is_veg      TINYINT(1)     NOT NULL DEFAULT 1,  -- 1=Veg, 0=Non-Veg
    rating      DECIMAL(2, 1)  NOT NULL DEFAULT 4.5,
    prep_time   VARCHAR(30)    NOT NULL DEFAULT '20-25 mins',
    available   TINYINT(1)     NOT NULL DEFAULT 1,  -- 1=available, 0=unavailable

    PRIMARY KEY (food_id),
    INDEX idx_food_category (category),
    INDEX idx_food_cuisine (cuisine),
    INDEX idx_food_available (available)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 3. discounts
-- ============================================================
CREATE TABLE IF NOT EXISTS discounts (
    discount_id          INT            NOT NULL AUTO_INCREMENT,
    coupon_code          VARCHAR(50)    NOT NULL,
    discount_type        ENUM('PERCENTAGE','FIXED') NOT NULL,
    discount_value       DECIMAL(10, 2) NOT NULL,
    minimum_order_amount DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    expiry_date          DATE           NOT NULL,
    enabled              TINYINT(1)     NOT NULL DEFAULT 1,

    PRIMARY KEY (discount_id),
    UNIQUE KEY uk_discounts_coupon_code (coupon_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 4. orders
-- ============================================================
CREATE TABLE IF NOT EXISTS orders (
    order_id         INT            NOT NULL AUTO_INCREMENT,
    customer_id      INT            NOT NULL,
    order_date       DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    subtotal         DECIMAL(10, 2) NOT NULL,
    discount_amount  DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    final_amount     DECIMAL(10, 2) NOT NULL,
    order_status     ENUM('PLACED','CONFIRMED','PREPARING','OUT_FOR_DELIVERY','DELIVERED','CANCELLED')
                                    NOT NULL DEFAULT 'PLACED',
    delivery_address TEXT,
    contact_phone    VARCHAR(20),

    PRIMARY KEY (order_id),
    CONSTRAINT fk_orders_customer FOREIGN KEY (customer_id)
        REFERENCES customers (customer_id) ON DELETE RESTRICT ON UPDATE CASCADE,
    INDEX idx_orders_customer (customer_id),
    INDEX idx_orders_status  (order_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 5. order_items
-- ============================================================
CREATE TABLE IF NOT EXISTS order_items (
    order_item_id      INT            NOT NULL AUTO_INCREMENT,
    order_id           INT            NOT NULL,
    food_id            INT            NOT NULL,
    food_name_snapshot VARCHAR(150)   NOT NULL,
    unit_price         DECIMAL(10, 2) NOT NULL,
    quantity           INT            NOT NULL,
    line_total         DECIMAL(10, 2) NOT NULL,

    PRIMARY KEY (order_item_id),
    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id)
        REFERENCES orders (order_id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_order_items_food  FOREIGN KEY (food_id)
        REFERENCES food_items (food_id) ON DELETE RESTRICT ON UPDATE CASCADE,
    INDEX idx_order_items_order (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 6. payments
-- ============================================================
CREATE TABLE IF NOT EXISTS payments (
    payment_id      INT            NOT NULL AUTO_INCREMENT,
    order_id        INT            NOT NULL,
    payment_method  VARCHAR(50)    NOT NULL,
    amount          DECIMAL(10, 2) NOT NULL,
    payment_status  ENUM('PENDING','COMPLETED','FAILED') NOT NULL DEFAULT 'PENDING',
    payment_date    DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    transaction_ref VARCHAR(100),

    PRIMARY KEY (payment_id),
    CONSTRAINT fk_payments_order FOREIGN KEY (order_id)
        REFERENCES orders (order_id) ON DELETE CASCADE ON UPDATE CASCADE,
    UNIQUE KEY uk_payments_order (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
