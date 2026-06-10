-- ========================================
-- 今天吃什么留言板 - 数据库初始化脚本
-- ========================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS food_board
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE food_board;

-- 创建用户表
CREATE TABLE IF NOT EXISTS user (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    username    VARCHAR(30)  NOT NULL UNIQUE COMMENT '用户名（登录用）',
    password    VARCHAR(255) NOT NULL COMMENT '密码（BCrypt加密）',
    nickname    VARCHAR(30)  NOT NULL COMMENT '昵称',
    avatar      TEXT         COMMENT '自定义头像（emoji或base64图片）',
    is_admin    TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否管理员：0否 1是',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 创建留言表
CREATE TABLE IF NOT EXISTS message (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    author      VARCHAR(50)  NOT NULL DEFAULT '匿名吃货' COMMENT '作者昵称',
    content     TEXT         NOT NULL COMMENT '留言内容',
    likes       INT          NOT NULL DEFAULT 0 COMMENT '点赞数',
    view_count  INT          NOT NULL DEFAULT 0 COMMENT '浏览量',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='留言表';

-- 创建评论表
CREATE TABLE IF NOT EXISTS comment (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    message_id  BIGINT       NOT NULL COMMENT '关联留言ID',
    author      VARCHAR(50)  NOT NULL DEFAULT '匿名吃货' COMMENT '评论者',
    content     TEXT         NOT NULL COMMENT '评论内容',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评论时间',
    INDEX idx_message_id (message_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论表';

-- 插入管理员账号（密码: admin123，BCrypt加密）
INSERT INTO user (username, password, nickname, is_admin) VALUES
('admin', '$2a$10$dSQQ.GbzzIvtSKeuHk2GWeLrAZpIXP6HI2RYy5nmpoNAYjpAZJ.Be', '管理员', 1);

-- 插入一些示例数据
INSERT INTO message (author, content, likes, view_count) VALUES
('吃货小王', '今天中午吃黄焖鸡米饭，香得不行！🍗', 12, 25),
('美食家阿明', '推荐学校西门那家螺蛳粉，真的绝了！🍜', 8, 18),
('干饭人', '食堂三楼的麻辣烫 yyds！🍲', 5, 12),
('小红', '今天减肥，吃沙拉…算了还是来碗牛肉面吧 🍜', 3, 9),
('大胃王', '一个人干掉了一整个披萨，罪恶感满满 🍕', 15, 32),
('学生党', '有谁知道附近哪里有好吃又便宜的炸鸡？🍗', 2, 7);
