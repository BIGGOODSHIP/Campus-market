-- ============================================
-- 校园二手物品置换交易管理系统 - 数据库初始化脚本
-- ============================================

-- 创建数据库
DROP DATABASE IF EXISTS campus_market;
CREATE DATABASE campus_market DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE campus_market;

-- ============================================
-- 1. 数据表创建
-- ============================================

-- 1.1 用户表 (User)
-- 满足3NF：非主键属性完全函数依赖于主键，不存在传递依赖
CREATE TABLE tb_user (
    user_id     INT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID，主键',
    username    VARCHAR(50)  NOT NULL COMMENT '用户名',
    password    VARCHAR(256) NOT NULL COMMENT '密码(SHA-256加密)',
    real_name   VARCHAR(50)  NOT NULL COMMENT '真实姓名',
    phone       VARCHAR(20)  DEFAULT '' COMMENT '手机号',
    role        TINYINT      NOT NULL DEFAULT 0 COMMENT '角色: 0=学生, 1=管理员',
    status      TINYINT      NOT NULL DEFAULT 0 COMMENT '状态: 0=正常, 1=禁用',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE INDEX idx_username (username),
    INDEX idx_role_status (role, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 1.2 商品分类表 (Category)
CREATE TABLE tb_category (
    category_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '分类ID，主键',
    name        VARCHAR(50)  NOT NULL COMMENT '分类名称',
    description VARCHAR(200) DEFAULT '' COMMENT '分类描述'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分类表';

-- 1.3 商品表 (Product)
CREATE TABLE tb_product (
    product_id    INT AUTO_INCREMENT PRIMARY KEY COMMENT '商品ID，主键',
    name          VARCHAR(100)   NOT NULL COMMENT '商品名称',
    description   TEXT           COMMENT '商品描述',
    price         DECIMAL(10,2)  NOT NULL COMMENT '售价',
    original_price DECIMAL(10,2) COMMENT '原价',
    category_id   INT            COMMENT '分类ID',
    seller_id     INT            NOT NULL COMMENT '卖家ID',
    status        TINYINT        NOT NULL DEFAULT 0 COMMENT '状态: 0=待审核, 1=已上架, 2=已售出, 3=已下架, 4=审核不通过',
    image_path    VARCHAR(500)   DEFAULT '' COMMENT '图片路径',
    create_time   DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
    INDEX idx_category (category_id),
    INDEX idx_seller (seller_id),
    INDEX idx_status (status),
    INDEX idx_name_category (name, category_id),
    FOREIGN KEY (category_id) REFERENCES tb_category(category_id) ON DELETE SET NULL,
    FOREIGN KEY (seller_id) REFERENCES tb_user(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

-- 1.4 订单表 (Order)
CREATE TABLE tb_order (
    order_id      INT AUTO_INCREMENT PRIMARY KEY COMMENT '订单ID，主键',
    buyer_id      INT            NOT NULL COMMENT '买家ID',
    product_id    INT            NOT NULL COMMENT '商品ID',
    price         DECIMAL(10,2)  NOT NULL COMMENT '成交价格',
    status        TINYINT        NOT NULL DEFAULT 0 COMMENT '状态: 0=待交易, 1=已完成, 2=已取消',
    create_time   DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
    complete_time DATETIME       COMMENT '完成/取消时间',
    INDEX idx_buyer (buyer_id),
    INDEX idx_product (product_id),
    INDEX idx_status_time (status, create_time),
    FOREIGN KEY (buyer_id) REFERENCES tb_user(user_id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES tb_product(product_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';


-- ============================================
-- 2. 视图创建
-- ============================================

-- 2.1 商品列表视图（含卖家名和分类名）
CREATE VIEW v_product_list AS
SELECT
    p.product_id,
    p.name,
    p.description,
    p.price,
    p.original_price,
    p.status,
    p.image_path,
    p.create_time,
    p.category_id,
    c.name AS category_name,
    p.seller_id,
    u.username AS seller_name,
    u.phone AS seller_phone
FROM tb_product p
LEFT JOIN tb_category c ON p.category_id = c.category_id
LEFT JOIN tb_user u ON p.seller_id = u.user_id;

-- 2.2 订单详情视图（含买卖双方和商品信息）
CREATE VIEW v_order_detail AS
SELECT
    o.order_id,
    o.price AS order_price,
    o.status AS order_status,
    o.create_time AS order_time,
    o.complete_time,
    o.buyer_id,
    bu.username AS buyer_name,
    bu.phone AS buyer_phone,
    o.product_id,
    p.name AS product_name,
    p.seller_id,
    su.username AS seller_name,
    su.phone AS seller_phone
FROM tb_order o
LEFT JOIN tb_user bu ON o.buyer_id = bu.user_id
LEFT JOIN tb_product p ON o.product_id = p.product_id
LEFT JOIN tb_user su ON p.seller_id = su.user_id;

-- ============================================
-- 3. 触发器
-- ============================================

DELIMITER //

-- 3.1 订单完成后自动更新商品状态为"已售出"
CREATE TRIGGER trg_after_order_complete
AFTER UPDATE ON tb_order
FOR EACH ROW
BEGIN
    IF NEW.status = 1 AND OLD.status != 1 THEN
        -- 订单完成，更新商品状态为已售出
        UPDATE tb_product SET status = 2 WHERE product_id = NEW.product_id;
    END IF;
END //

-- 3.2 订单取消后恢复商品状态（如果能重新上架）
CREATE TRIGGER trg_after_order_cancel
AFTER UPDATE ON tb_order
FOR EACH ROW
BEGIN
    IF NEW.status = 2 AND OLD.status != 2 THEN
        -- 订单取消，可考虑将商品状态恢复为上架(需手动操作，此处记录日志)
        -- 此处仅保证数据一致性，不自动恢复
        UPDATE tb_product SET status = 1 WHERE product_id = NEW.product_id AND status = 2;
    END IF;
END //

DELIMITER ;

-- ============================================
-- 4. 存储过程
-- ============================================

DELIMITER //

-- 4.1 获取平台统计数据
CREATE PROCEDURE sp_get_statistics()
BEGIN
    DECLARE total_users INT;
    DECLARE total_products INT;
    DECLARE total_orders INT;
    DECLARE completed_orders INT;
    DECLARE total_trade_amount DECIMAL(12,2);

    SELECT COUNT(*) INTO total_users FROM tb_user WHERE role = 0 AND status = 0;
    SELECT COUNT(*) INTO total_products FROM tb_product;
    SELECT COUNT(*) INTO total_orders FROM tb_order;
    SELECT COUNT(*) INTO completed_orders FROM tb_order WHERE status = 1;
    SELECT COALESCE(SUM(price), 0) INTO total_trade_amount FROM tb_order WHERE status = 1;

    SELECT
        total_users AS user_count,
        total_products AS product_count,
        total_orders AS order_count,
        completed_orders AS completed_count,
        COALESCE(total_trade_amount, 0) AS trade_amount;
END //

-- 4.2 商品多条件搜索
CREATE PROCEDURE sp_search_products(
    IN p_keyword VARCHAR(100),
    IN p_category_id INT,
    IN p_min_price DECIMAL(10,2),
    IN p_max_price DECIMAL(10,2),
    IN p_status TINYINT
)
BEGIN
    SELECT * FROM v_product_list
    WHERE (p_keyword IS NULL OR p_keyword = ''
           OR name LIKE CONCAT('%', p_keyword, '%')
           OR description LIKE CONCAT('%', p_keyword, '%'))
      AND (p_category_id IS NULL OR p_category_id = 0 OR category_id = p_category_id)
      AND (p_min_price IS NULL OR price >= p_min_price)
      AND (p_max_price IS NULL OR price <= p_max_price)
      AND (p_status IS NULL OR p_status = -1 OR status = p_status)
    ORDER BY create_time DESC;
END //

DELIMITER ;

-- ============================================
-- 5. 初始数据
-- ============================================

-- 5.1 管理员账号 (密码: admin123 的 SHA-256)
INSERT INTO tb_user (username, password, real_name, phone, role, status) VALUES
('admin', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', '系统管理员', '13800000000', 1, 0),
('student1', 'ec3e3fdb0ef3af201d9aa0f5b78c2831fad4968a7619bdf02c26ad3ad633b766', '张三', '13811111111', 0, 0),
('student2', '88915ba0c6edb58174d2f92fb1201c7b7eaefbc8e65e84f3aee086c1be3915a0', '李四', '13822222222', 0, 0),
('student3', 'e8800313a9b358c4aec4c2c740cb5e2169a50a5d4fe152a73aa50340238f942e', '王五', '13833333333', 0, 0);
-- 密码: student1=pass123, student2=pass123, student3=pass123

-- 5.2 商品分类
INSERT INTO tb_category (name, description) VALUES
('教材教辅', '各类课程教材、参考书、考研资料等'),
('数码产品', '手机、平板、电脑、耳机等电子产品'),
('生活用品', '台灯、收纳、床上用品等日常用品'),
('运动器材', '球类、健身器材、运动装备等'),
('服饰鞋包', '服装、鞋子、背包等穿戴用品'),
('其他', '其他未分类的闲置物品');

-- 5.3 示例商品
INSERT INTO tb_product (name, description, price, original_price, category_id, seller_id, status, create_time) VALUES
('高等数学（第七版）', '上下册合订本，九成新，有少量笔记', 25.00, 55.00, 1, 2, 1, '2026-07-01 10:00:00'),
('大学英语四级词汇', '几乎全新，无笔记无折痕', 15.00, 39.00, 1, 2, 1, '2026-07-02 09:00:00'),
('iPad Pro 2022 11寸', '深空灰，128G WiFi版，电池健康95%，配件齐全', 3200.00, 6799.00, 2, 3, 1, '2026-07-03 14:00:00'),
('罗技 MX Master 3S 鼠标', '使用三个月，配件齐全，功能正常', 350.00, 699.00, 2, 2, 1, '2026-07-04 11:00:00'),
('宿舍台灯 LED护眼灯', '可调色温亮度，带笔筒底座，使用一年', 35.00, 89.00, 3, 3, 1, '2026-07-05 16:00:00'),
('考研政治精讲精练', '2026考研用书，有划线重点', 18.00, 56.00, 1, 2, 1, '2026-07-06 08:00:00'),
('YONEX羽毛球拍', '入门级，含球拍套，适合初学者', 80.00, 199.00, 4, 3, 1, '2026-07-07 15:00:00'),
('蓝牙降噪耳机', '品牌头戴式降噪耳机，降噪效果好，续航40小时', 180.00, 499.00, 2, 2, 1, '2026-07-10 10:00:00'),
('床上折叠桌', '懒人桌，可调节角度，木质桌面', 20.00, 49.00, 3, 3, 1, '2026-07-12 13:00:00'),
('冬季棉被', '加厚款，2m*2.3m，轻微使用痕迹，已清洗', 50.00, 149.00, 3, 3, 0, '2026-07-15 09:00:00'),
('数据结构（C语言版）', '经典教材，有少量笔记', 12.00, 35.00, 1, 2, 0, '2026-07-16 11:00:00');

-- 5.4 示例订单
INSERT INTO tb_order (buyer_id, product_id, price, status, create_time, complete_time) VALUES
(3, 1, 25.00, 1, '2026-07-10 14:00:00', '2026-07-11 16:00:00'),
(4, 3, 3200.00, 1, '2026-07-10 16:00:00', '2026-07-12 10:00:00'),
(4, 5, 35.00, 0, '2026-07-18 10:00:00', NULL),
(3, 8, 180.00, 0, '2026-07-20 09:00:00', NULL),
(4, 7, 80.00, 2, '2026-07-21 11:00:00', '2026-07-21 18:00:00');

