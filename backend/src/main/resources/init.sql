-- 创建数据库
CREATE DATABASE IF NOT EXISTS library DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE library;

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `username` VARCHAR(50) NOT NULL UNIQUE,
    `password` VARCHAR(255) NOT NULL COMMENT 'BCrypt加密存储',
    `role` VARCHAR(20) NOT NULL DEFAULT 'user' COMMENT 'admin或user',
    `avatar_url` TEXT COMMENT '头像URL或Base64',
    `bio` VARCHAR(200) COMMENT '个人简介',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 如果user表已存在但缺少字段，手动执行：
-- ALTER TABLE `user` ADD COLUMN `avatar_url` TEXT COMMENT '头像URL或Base64' AFTER `role`;
-- ALTER TABLE `user` ADD COLUMN `bio` VARCHAR(200) COMMENT '个人简介' AFTER `avatar_url`;

-- 图书表
CREATE TABLE IF NOT EXISTS `book` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `title` VARCHAR(100) NOT NULL,
    `author` VARCHAR(50) NOT NULL,
    `isbn` VARCHAR(20),
    `publisher` VARCHAR(100),
    `publication_date` DATE,
    `description` TEXT,
    `cover_url` VARCHAR(255),
    `category` VARCHAR(50) COMMENT '图书分类',
    `total` INT NOT NULL DEFAULT 1 COMMENT '总库存',
    `available` INT NOT NULL DEFAULT 1 COMMENT '当前可借数量',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 如果book表已存在但缺少category列，手动执行：
-- ALTER TABLE `book` ADD COLUMN `category` VARCHAR(50) COMMENT '图书分类' AFTER `cover_url`;
-- UPDATE `book` SET `category`='编程语言' WHERE `title` IN ('Java核心技术','Spring Boot实战','Effective Java','Python编程从入门到实践','Go语言程序设计');
-- UPDATE `book` SET `category`='计算机基础' WHERE `title` IN ('深入理解计算机系统','算法导论','数据结构与算法分析');
-- UPDATE `book` SET `category`='软件工程' WHERE `title` IN ('设计模式','代码整洁之道');
-- UPDATE `book` SET `category`='计算机网络' WHERE `title`='计算机网络：自顶向下方法';
-- UPDATE `book` SET `category`='操作系统' WHERE `title`='Linux命令行与Shell脚本编程';
-- UPDATE `book` SET `category`='人工智能' WHERE `title`='人工智能：现代方法';
-- UPDATE `book` SET `category`='数据库' WHERE `title` IN ('Redis设计与实现','MySQL技术内幕');

-- 借阅记录表
CREATE TABLE IF NOT EXISTS `borrow_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `book_id` BIGINT NOT NULL,
    `borrow_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `due_time` DATETIME NOT NULL COMMENT '应还日期，借期30天',
    `return_time` DATETIME,
    `status` VARCHAR(20) NOT NULL DEFAULT 'BORROWED' COMMENT 'BORROWED或RETURNED',
    PRIMARY KEY (`id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_book_id` (`book_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 插入内置管理员账号（密码: admin123/super123，BCrypt加密由DataInitRunner保障）
INSERT IGNORE INTO `user` (`username`, `password`, `role`) VALUES
('admin', '$2a$10$EqKcp1WFKVQISheBxmVMOun6PsEG8zZ0z4g0WQHlK5eK0YzGqS.KS', 'admin'),
('superadmin', '$2a$10$EqKcp1WFKVQISheBxmVMOun6PsEG8zZ0z4g0WQHlK5eK0YzGqS.KS', 'super_admin');

-- 插入演示图书（15本）
INSERT INTO `book` (`title`, `author`, `isbn`, `publisher`, `publication_date`, `description`, `category`, `total`, `available`) VALUES
('Java核心技术', 'Cay S. Horstmann', '9787111547426', '机械工业出版社', '2020-06-01', 'Java经典教程，涵盖Java SE核心技术，适合中高级开发者深入学习。', '编程语言', 5, 5),
('Spring Boot实战', '丁雪丰', '9787115487610', '人民邮电出版社', '2021-03-15', '全面讲解Spring Boot框架的使用，从入门到实战项目开发。', '编程语言', 3, 3),
('深入理解计算机系统', 'Randal E. Bryant', '9787111544937', '机械工业出版社', '2019-10-01', '计算机科学经典教材，从程序员视角理解计算机系统底层原理。', '计算机基础', 4, 4),
('算法导论', 'Thomas H. Cormen', '9787111407010', '机械工业出版社', '2018-01-01', '算法领域权威教材，系统讲解算法设计与分析的方法论。', '计算机基础', 2, 2),
('设计模式', 'Erich Gamma', '9787111075752', '机械工业出版社', '2019-07-01', 'GoF设计模式经典著作，23种设计模式的详细阐述与应用场景。', '软件工程', 3, 3),
('Effective Java', 'Joshua Bloch', '9787111258074', '机械工业出版社', '2018-12-01', 'Java编程最佳实践，78条高效编程规则，提升代码质量必备。', '编程语言', 4, 4),
('Python编程从入门到实践', 'Eric Matthes', '9787115428028', '人民邮电出版社', '2020-10-01', 'Python零基础入门经典，项目驱动教学，适合初学者快速上手。', '编程语言', 6, 6),
('数据结构与算法分析', 'Mark Allen Weiss', '9787111528395', '机械工业出版社', '2019-05-01', '系统讲解常用数据结构和算法，C语言描述，兼顾理论与实践。', '计算机基础', 3, 3),
('计算机网络：自顶向下方法', 'James Kurose', '9787111654158', '机械工业出版社', '2021-08-01', '计算机网络经典教材，自顶向下方法论，深入理解网络协议栈。', '计算机网络', 3, 3),
('Linux命令行与Shell脚本编程', 'Richard Blum', '9787115389903', '人民邮电出版社', '2020-04-01', 'Linux系统管理必备指南，命令行操作与Shell脚本编程大全。', '操作系统', 5, 5),
('人工智能：现代方法', 'Stuart Russell', '9787302534094', '清华大学出版社', '2021-06-01', 'AI领域权威教材，全面覆盖搜索、推理、学习、规划等核心主题。', '人工智能', 2, 2),
('Go语言程序设计', 'Alan Donovan', '9787115428783', '人民邮电出版社', '2020-07-01', 'Go语言编程入门与进阶，并发编程、网络编程实战指南。', '编程语言', 3, 3),
('代码整洁之道', 'Robert C. Martin', '9787115388746', '人民邮电出版社', '2019-01-01', '软件工艺经典著作，教你编写优雅、可读、可维护的高质量代码。', '软件工程', 4, 4),
('Redis设计与实现', '黄健宏', '9787111480138', '机械工业出版社', '2021-02-01', '深入Redis内核设计，数据结构、持久化、复制、集群全面解析。', '数据库', 2, 2),
('MySQL技术内幕', '姜承尧', '9787111446296', '机械工业出版社', '2020-11-01', 'InnoDB存储引擎深度剖析，SQL优化与数据库架构设计参考。', '数据库', 3, 3);
