/*
 Navicat Premium Data Transfer

 Source Server         : MySQL8.0
 Source Server Type    : MySQL
 Source Server Version : 80019
 Source Host           : localhost:3307
 Source Schema         : onlineauction

 Target Server Type    : MySQL
 Target Server Version : 80019
 File Encoding         : 65001

 Date: 19/01/2026 10:53:25
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for auction_banner
-- ----------------------------
DROP TABLE IF EXISTS `auction_banner`;
CREATE TABLE `auction_banner`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '轮播图ID，自增',
  `banner_img` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '轮播图本地路径（如D:/auction/upload/banner/202512/xxx.jpg）',
  `goods_id` bigint NULL DEFAULT NULL COMMENT '关联商品ID（点击跳转，无则为NULL）',
  `banner_sort` int NOT NULL DEFAULT 0 COMMENT '排序值（数字越小越靠前）',
  `banner_status` tinyint NOT NULL DEFAULT 0 COMMENT '状态：0=未启用 1=已启用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=未删除 1=已删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_banner_status`(`banner_status` ASC) USING BTREE,
  INDEX `idx_banner_sort`(`banner_sort` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '首页轮播图表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of auction_banner
-- ----------------------------

-- ----------------------------
-- Table structure for auction_category
-- ----------------------------
DROP TABLE IF EXISTS `auction_category`;
CREATE TABLE `auction_category`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '类目ID，自增',
  `category_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '类目名称（如：奢侈品、房产、艺术品）',
  `category_sort` int NOT NULL DEFAULT 0 COMMENT '排序值（数字越小越靠前）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=未删除 1=已删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '拍卖商品类目表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of auction_category
-- ----------------------------
INSERT INTO `auction_category` VALUES (1, '古玩', 1, '2026-01-04 17:01:42', '2026-01-04 17:01:42', 0);
INSERT INTO `auction_category` VALUES (2, '青眼白龙', 2, '2026-01-04 17:02:08', '2026-01-04 17:02:08', 0);

-- ----------------------------
-- Table structure for auction_deposit
-- ----------------------------
DROP TABLE IF EXISTS `auction_deposit`;
CREATE TABLE `auction_deposit`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '保证金记录ID，自增',
  `user_id` bigint NOT NULL COMMENT '用户ID（关联auction_user.id）',
  `order_id` bigint NULL DEFAULT NULL COMMENT '关联订单ID（auction_order.id，无订单时为NULL）',
  `amount` decimal(10, 2) NOT NULL COMMENT '保证金金额',
  `deposit_type` tinyint NOT NULL COMMENT '变动类型：0=充值 1=冻结 2=解冻 3=抵扣尾款 4=扣除（悔拍）',
  `balance` decimal(10, 2) NOT NULL COMMENT '操作后保证金余额',
  `operate_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  `remark` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注（如：冻结订单ID=123保证金）',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=未删除 1=已删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_order_id`(`order_id` ASC) USING BTREE,
  INDEX `idx_deposit_type`(`deposit_type` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '保证金记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of auction_deposit
-- ----------------------------

-- ----------------------------
-- Table structure for auction_file
-- ----------------------------
DROP TABLE IF EXISTS `auction_file`;
CREATE TABLE `auction_file`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '文件ID，自增',
  `file_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '原始文件名',
  `file_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '文件存储路径',
  `file_type` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '文件类型：image=图片 video=视频',
  `goods_id` bigint NULL DEFAULT NULL COMMENT '关联商品ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=未删除 1=已删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_goods_id`(`goods_id` ASC) USING BTREE,
  INDEX `idx_file_type`(`file_type` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '商品文件表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of auction_file
-- ----------------------------
INSERT INTO `auction_file` VALUES (1, 'QQ图片20230426170644.jpg', 'D:/auction/upload/goods/20261/35e88165a70643a899b720b354ed33ea.jpg', 'image', NULL, '2026-01-07 14:57:05', '2026-01-07 14:57:05', 0);
INSERT INTO `auction_file` VALUES (2, '微信图片_20250329220729.png', 'D:/auction/upload/goods/20261/fbad0c49f41d4b0198032fc30dd3148a.png', 'image', NULL, '2026-01-07 14:57:19', '2026-01-07 14:57:19', 0);
INSERT INTO `auction_file` VALUES (3, 'QQ图片20230426170644.jpg', 'D:/auction/upload/goods/202601/a7f1ea4e75764fac9867108c1a363fc0.jpg', 'image', NULL, '2026-01-07 17:14:46', '2026-01-07 17:14:45', 0);
INSERT INTO `auction_file` VALUES (4, 'QQ图片20230426170644.jpg', '/upload/goods/202601/a1c24be60eb5471b80a446dba4ee374e.jpg', 'image', NULL, '2026-01-07 17:38:15', '2026-01-07 17:38:15', 0);
INSERT INTO `auction_file` VALUES (5, '微信图片_20250329220729.png', '/upload/goods/202601/aa592a1385ec47288f092916a4b365d3.png', 'image', NULL, '2026-01-07 17:38:25', '2026-01-07 17:38:25', 0);
INSERT INTO `auction_file` VALUES (6, '46颜炳权《鸡汤来喽》.MP4', '/upload/goods/202601/09b8f2f8766e43ebb86cc4c76e5f3103.MP4', 'video', NULL, '2026-01-07 17:43:10', '2026-01-07 17:43:09', 0);
INSERT INTO `auction_file` VALUES (7, '[tools.miku.ac][图片转像素画]-1764923622832.png', '/upload/goods/202601/4c6b461f93a54812b4428ca6e1615dd4.png', 'image', NULL, '2026-01-07 17:47:53', '2026-01-07 17:47:52', 0);
INSERT INTO `auction_file` VALUES (8, '3149315cea3d09be2a697f074dcbf9fd.jpeg', '/upload/goods/202601/fc5cbf27abb242ecaa6567869a2178cb.jpeg', 'image', NULL, '2026-01-07 17:50:18', '2026-01-07 17:50:18', 0);

-- ----------------------------
-- Table structure for auction_goods
-- ----------------------------
DROP TABLE IF EXISTS `auction_goods`;
CREATE TABLE `auction_goods`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '商品ID，自增',
  `goods_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '商品名称',
  `category_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '商品类目ID（关联auction_category.id，多个用逗号分隔）',
  `seller_id` bigint NOT NULL COMMENT '卖方用户ID（关联auction_user.id）',
  `goods_desc` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '商品详情（含瑕疵说明）',
  `base_price` decimal(10, 2) NOT NULL COMMENT '起拍价',
  `add_price` decimal(10, 2) NOT NULL COMMENT '最小加价幅度',
  `reserve_price` decimal(10, 2) NULL DEFAULT NULL COMMENT '保留价（仅卖方可见）',
  `start_time` datetime NOT NULL COMMENT '拍卖开始时间',
  `end_time` datetime NOT NULL COMMENT '拍卖结束时间',
  `audit_status` tinyint NOT NULL DEFAULT 0 COMMENT '审核状态：0=待审核 1=审核通过 2=审核驳回 3=已下架',
  `audit_remark` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '审核驳回原因',
  `goods_status` tinyint NOT NULL DEFAULT 0 COMMENT '商品状态：0=未开始 1=竞拍中 2=已成交 3=已流拍',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=未删除 1=已删除',
  `view_count` int NOT NULL DEFAULT 0 COMMENT '商品点击量',
  `collect_count` int NOT NULL DEFAULT 0 COMMENT '商品收藏量',
  `bid_count` int NOT NULL DEFAULT 0 COMMENT '商品竞价次数',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_category_id`(`category_id` ASC) USING BTREE,
  INDEX `idx_seller_id`(`seller_id` ASC) USING BTREE,
  INDEX `idx_audit_status`(`audit_status` ASC) USING BTREE,
  INDEX `idx_goods_status`(`goods_status` ASC) USING BTREE,
  INDEX `idx_view_count`(`view_count` DESC) USING BTREE,
  INDEX `idx_collect_count`(`collect_count` DESC) USING BTREE,
  INDEX `idx_bid_count`(`bid_count` DESC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '拍卖商品表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of auction_goods
-- ----------------------------
INSERT INTO `auction_goods` VALUES (1, '青眼白龙', '1,2', 1, '青眼白龙', 200.00, 20.00, 1000.00, '2025-12-31 16:00:00', '2026-01-30 16:00:00', 1, '', 0, '2026-01-07 16:42:23', '2026-01-07 17:07:35', 0, 0, 0, 0);
INSERT INTO `auction_goods` VALUES (2, '夜明珠', '1,2', 1, '古玩类', 200.00, 20.00, 600.00, '2025-12-31 16:00:00', '2026-01-30 16:00:00', 1, '', 0, '2026-01-07 17:03:21', '2026-01-07 17:07:34', 0, 0, 0, 0);
INSERT INTO `auction_goods` VALUES (3, '测试图片', '1,2', 1, '111', 100.00, 20.00, 500.00, '2025-12-31 16:00:00', '2026-01-30 16:00:00', 1, '', 0, '2026-01-07 17:07:06', '2026-01-07 17:07:33', 0, 0, 0, 0);
INSERT INTO `auction_goods` VALUES (4, '测试视频', '2', 1, '视频', 200.00, 30.00, 500.00, '2025-12-31 16:00:00', '2026-01-30 16:00:00', 1, '', 0, '2026-01-07 17:08:26', '2026-01-07 17:17:05', 0, 0, 0, 0);
INSERT INTO `auction_goods` VALUES (5, '测试文件', '1,2', 1, '文件', 200.00, 20.00, 1000.00, '2025-12-31 16:00:00', '2026-01-30 16:00:00', 1, '', 0, '2026-01-07 17:15:11', '2026-01-07 17:17:04', 0, 0, 0, 0);
INSERT INTO `auction_goods` VALUES (6, '测试添加多个图片', '1,2', 1, '多个图片', 200.00, 50.00, 2000.00, '2025-12-31 16:00:00', '2026-01-30 16:00:00', 1, '', 0, '2026-01-07 17:18:05', '2026-01-07 17:37:54', 0, 0, 0, 0);
INSERT INTO `auction_goods` VALUES (7, '测试图片', '1,2', 1, 'dcz', 5411.00, 85.00, 8745.00, '2025-12-31 16:00:00', '2026-01-30 16:00:00', 1, '', 0, '2026-01-07 17:23:19', '2026-01-07 17:37:53', 0, 0, 0, 0);
INSERT INTO `auction_goods` VALUES (8, '测试文件上传', '1,2', 1, '图片', 0.00, 0.00, 0.00, '2025-12-31 16:00:00', '2026-01-30 16:00:00', 1, '', 0, '2026-01-07 17:38:43', '2026-01-08 09:13:14', 0, 0, 0, 0);
INSERT INTO `auction_goods` VALUES (9, '测试视频', '2,1', 1, '123', 0.00, 0.00, 0.00, '2025-12-31 16:00:00', '2026-01-30 16:00:00', 1, '', 0, '2026-01-07 17:43:24', '2026-01-08 09:13:13', 0, 0, 0, 0);
INSERT INTO `auction_goods` VALUES (10, '测试添加视频', '2,1', 1, '视频', 0.00, 0.00, 0.00, '2025-12-31 16:00:00', '2026-01-30 16:00:00', 1, '', 0, '2026-01-07 17:48:00', '2026-01-08 09:13:12', 0, 0, 0, 0);
INSERT INTO `auction_goods` VALUES (11, '祝友志的苦茶', '1', 1, '商周的新鲜货', 10000.00, 500.00, 9000.00, '2026-01-05 16:00:00', '2026-01-27 16:00:00', 1, '', 0, '2026-01-07 17:51:22', '2026-01-08 09:13:10', 0, 0, 0, 0);

-- ----------------------------
-- Table structure for auction_menu
-- ----------------------------
DROP TABLE IF EXISTS `auction_menu`;
CREATE TABLE `auction_menu`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '菜单ID，自增',
  `menu_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '菜单名称',
  `parent_id` bigint NOT NULL DEFAULT 0 COMMENT '父菜单ID（0表示顶级菜单）',
  `menu_path` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '菜单路径/路由（如：/goods/list）',
  `menu_icon` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '菜单图标（Element UI图标类名，如：el-icon-goods）',
  `menu_type` tinyint NOT NULL DEFAULT 1 COMMENT '菜单类型：1=菜单 2=按钮',
  `permission_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '权限标识（如：goods:list，用于权限控制）',
  `menu_sort` int NOT NULL DEFAULT 0 COMMENT '排序值（数字越小越靠前）',
  `menu_status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0=禁用 1=启用',
  `remark` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注说明',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=未删除 1=已删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_parent_id`(`parent_id` ASC) USING BTREE,
  INDEX `idx_menu_status`(`menu_status` ASC) USING BTREE,
  INDEX `idx_menu_sort`(`menu_sort` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 20 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统菜单表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of auction_menu
-- ----------------------------
INSERT INTO `auction_menu` VALUES (1, '首页', 0, '/home', 'el-icon-s-home', 1, 'home:view', 1, 1, '用户端首页', '2025-12-31 14:40:18', '2025-12-31 14:40:18', 0);
INSERT INTO `auction_menu` VALUES (2, '拍卖商品', 0, '/goods', 'el-icon-goods', 1, 'goods:view', 2, 1, '拍卖商品列表', '2025-12-31 14:40:18', '2025-12-31 14:40:18', 0);
INSERT INTO `auction_menu` VALUES (3, '竞拍公告', 0, '/notice', 'el-icon-bell', 1, 'notice:view', 3, 1, '竞拍公告列表', '2025-12-31 14:40:18', '2025-12-31 14:40:18', 0);
INSERT INTO `auction_menu` VALUES (4, '个人中心', 0, '/profile', 'el-icon-user', 1, 'profile:view', 4, 1, '个人中心', '2025-12-31 14:40:18', '2025-12-31 14:40:18', 0);
INSERT INTO `auction_menu` VALUES (5, '订单管理', 4, '/profile/order', NULL, 1, 'order:view', 1, 1, '订单管理', '2025-12-31 14:40:18', '2025-12-31 14:40:18', 0);
INSERT INTO `auction_menu` VALUES (6, '保证金管理', 4, '/profile/deposit', NULL, 1, 'deposit:view', 2, 1, '保证金管理', '2025-12-31 14:40:18', '2025-12-31 14:40:18', 0);
INSERT INTO `auction_menu` VALUES (7, '消息中心', 4, '/profile/message', NULL, 1, 'message:view', 3, 1, '消息中心', '2025-12-31 14:40:18', '2025-12-31 14:40:18', 0);
INSERT INTO `auction_menu` VALUES (10, '个人管理中心', 0, '/admin/profile', 'el-icon-user', 1, 'admin:profile:view', 1, 1, '管理员个人中心', '2025-12-31 14:40:18', '2025-12-31 14:40:18', 0);
INSERT INTO `auction_menu` VALUES (11, '用户账号管理', 0, '/admin/user', 'el-icon-user-solid', 1, 'admin:user:view', 2, 1, '用户账号管理', '2025-12-31 14:40:18', '2025-12-31 14:40:18', 0);
INSERT INTO `auction_menu` VALUES (12, '拍卖商品类目管理', 0, '/admin/category', 'el-icon-folder', 1, 'admin:category:view', 3, 1, '商品类目管理', '2025-12-31 14:40:18', '2025-12-31 14:40:18', 0);
INSERT INTO `auction_menu` VALUES (13, '拍卖商品管理', 0, '/admin/goods', 'el-icon-goods', 1, 'admin:goods:view', 4, 1, '拍卖商品管理', '2025-12-31 14:40:18', '2025-12-31 14:40:18', 0);
INSERT INTO `auction_menu` VALUES (14, '历史竞拍管理', 0, '/admin/history', 'el-icon-time', 1, 'admin:history:view', 5, 1, '历史竞拍管理', '2025-12-31 14:40:18', '2025-12-31 14:40:18', 0);
INSERT INTO `auction_menu` VALUES (15, '竞拍订单管理', 0, '/admin/order', 'el-icon-s-order', 1, 'admin:order:view', 6, 1, '竞拍订单管理', '2025-12-31 14:40:18', '2025-12-31 14:40:18', 0);
INSERT INTO `auction_menu` VALUES (16, '留言板管理', 0, '/admin/message', 'el-icon-chat-line-square', 1, 'admin:message:view', 7, 1, '留言板管理', '2025-12-31 14:40:18', '2025-12-31 14:40:18', 0);
INSERT INTO `auction_menu` VALUES (17, '系统设置', 0, '/admin/settings', 'el-icon-setting', 1, 'admin:settings:view', 8, 1, '系统设置', '2025-12-31 14:40:18', '2025-12-31 14:40:18', 0);
INSERT INTO `auction_menu` VALUES (18, '轮播图管理', 17, '/admin/settings/banner', NULL, 1, 'admin:banner:view', 1, 1, '首页轮播图管理', '2025-12-31 14:40:18', '2025-12-31 14:40:18', 0);
INSERT INTO `auction_menu` VALUES (19, '竞拍公告管理', 17, '/admin/settings/notice', NULL, 1, 'admin:notice:view', 2, 1, '竞拍公告管理', '2025-12-31 14:40:18', '2025-12-31 14:40:18', 0);

-- ----------------------------
-- Table structure for auction_message
-- ----------------------------
DROP TABLE IF EXISTS `auction_message`;
CREATE TABLE `auction_message`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '留言ID，自增',
  `user_id` bigint NOT NULL COMMENT '留言用户ID（关联auction_user.id）',
  `goods_id` bigint NULL DEFAULT NULL COMMENT '关联商品ID（无则为NULL）',
  `message_content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '留言内容',
  `reply_content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '管理员回复内容',
  `reply_time` datetime NULL DEFAULT NULL COMMENT '回复时间',
  `message_status` tinyint NOT NULL DEFAULT 0 COMMENT '状态：0=未回复 1=已回复',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '留言时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=未删除 1=已删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_goods_id`(`goods_id` ASC) USING BTREE,
  INDEX `idx_message_status`(`message_status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '留言板表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of auction_message
-- ----------------------------

-- ----------------------------
-- Table structure for auction_notice
-- ----------------------------
DROP TABLE IF EXISTS `auction_notice`;
CREATE TABLE `auction_notice`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '公告ID，自增',
  `notice_title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '公告标题',
  `notice_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '公告内容',
  `is_top` tinyint NOT NULL DEFAULT 0 COMMENT '是否置顶：0=否 1=是',
  `notice_status` tinyint NOT NULL DEFAULT 0 COMMENT '状态：0=未发布 1=已发布 2=已下架',
  `publish_time` datetime NULL DEFAULT NULL COMMENT '发布时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=未删除 1=已删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_is_top`(`is_top` ASC) USING BTREE,
  INDEX `idx_notice_status`(`notice_status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '竞拍公告表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of auction_notice
-- ----------------------------

-- ----------------------------
-- Table structure for auction_oper_log
-- ----------------------------
DROP TABLE IF EXISTS `auction_oper_log`;
CREATE TABLE `auction_oper_log`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志ID，自增',
  `oper_user_id` bigint NOT NULL COMMENT '操作人ID（关联auction_user.id）',
  `oper_module` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '操作模块（如：商品审核、用户管理）',
  `oper_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '操作类型：新增/修改/删除/审核/禁用',
  `oper_content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '操作内容（如：审核商品ID=123，状态改为通过）',
  `oper_ip` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '操作IP地址',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_oper_user_id`(`oper_user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统操作日志表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of auction_oper_log
-- ----------------------------

-- ----------------------------
-- Table structure for auction_order
-- ----------------------------
DROP TABLE IF EXISTS `auction_order`;
CREATE TABLE `auction_order`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '订单ID，自增',
  `order_no` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '订单编号（唯一索引，规则：YYYYMMDD+随机8位）',
  `goods_id` bigint NOT NULL COMMENT '商品ID（关联auction_goods.id）',
  `buyer_id` bigint NOT NULL COMMENT '买方用户ID（关联auction_user.id）',
  `seller_id` bigint NOT NULL COMMENT '卖方用户ID（关联auction_user.id）',
  `record_id` bigint NOT NULL COMMENT '成交竞拍记录ID（关联auction_record.id）',
  `deal_price` decimal(10, 2) NOT NULL COMMENT '成交金额',
  `deposit_amount` decimal(10, 2) NOT NULL COMMENT '冻结保证金金额',
  `remain_amount` decimal(10, 2) NOT NULL COMMENT '尾款金额（deal_price - deposit_amount）',
  `pay_deadline` datetime NOT NULL COMMENT '尾款付款截止时间（成交后24小时）',
  `order_status` tinyint NOT NULL DEFAULT 0 COMMENT '订单状态：0=待付款 1=待发货 2=待收货 3=已完成 4=已悔拍 5=已退款',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间（竞拍成交后系统自动生成）',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=未删除 1=已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `idx_order_no`(`order_no` ASC) USING BTREE,
  INDEX `idx_buyer_id`(`buyer_id` ASC) USING BTREE,
  INDEX `idx_seller_id`(`seller_id` ASC) USING BTREE,
  INDEX `idx_order_status`(`order_status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '竞拍订单表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of auction_order
-- ----------------------------

-- ----------------------------
-- Table structure for auction_permission
-- ----------------------------
DROP TABLE IF EXISTS `auction_permission`;
CREATE TABLE `auction_permission`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '权限ID，自增',
  `permission_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '权限名称',
  `permission_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '权限标识（唯一，如：goods:list, goods:add）',
  `permission_type` tinyint NOT NULL DEFAULT 1 COMMENT '权限类型：1=菜单权限 2=按钮权限 3=数据权限',
  `permission_desc` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '权限描述',
  `permission_status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0=禁用 1=启用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=未删除 1=已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `idx_permission_code`(`permission_code` ASC) USING BTREE,
  INDEX `idx_permission_status`(`permission_status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 93 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统权限表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of auction_permission
-- ----------------------------
INSERT INTO `auction_permission` VALUES (1, '首页查看', 'home:view', 1, '查看首页', 1, '2025-12-31 14:40:32', '2025-12-31 14:40:32', 0);
INSERT INTO `auction_permission` VALUES (2, '商品查看', 'goods:view', 1, '查看商品列表', 1, '2025-12-31 14:40:32', '2025-12-31 14:40:32', 0);
INSERT INTO `auction_permission` VALUES (3, '商品详情', 'goods:detail', 1, '查看商品详情', 1, '2025-12-31 14:40:32', '2025-12-31 14:40:32', 0);
INSERT INTO `auction_permission` VALUES (4, '参与竞拍', 'goods:bid', 2, '参与商品竞拍', 1, '2025-12-31 14:40:32', '2025-12-31 14:40:32', 0);
INSERT INTO `auction_permission` VALUES (5, '代理出价', 'goods:agent', 2, '设置代理出价', 1, '2025-12-31 14:40:32', '2025-12-31 14:40:32', 0);
INSERT INTO `auction_permission` VALUES (6, '商品上架', 'goods:add', 2, '上架商品', 1, '2025-12-31 14:40:32', '2025-12-31 14:40:32', 0);
INSERT INTO `auction_permission` VALUES (7, '商品编辑', 'goods:edit', 2, '编辑自己的商品', 1, '2025-12-31 14:40:32', '2025-12-31 14:40:32', 0);
INSERT INTO `auction_permission` VALUES (8, '商品下架', 'goods:delete', 2, '下架自己的商品', 1, '2025-12-31 14:40:32', '2025-12-31 14:40:32', 0);
INSERT INTO `auction_permission` VALUES (9, '公告查看', 'notice:view', 1, '查看竞拍公告', 1, '2025-12-31 14:40:32', '2025-12-31 14:40:32', 0);
INSERT INTO `auction_permission` VALUES (10, '订单查看', 'order:view', 1, '查看订单列表', 1, '2025-12-31 14:40:32', '2025-12-31 14:40:32', 0);
INSERT INTO `auction_permission` VALUES (11, '订单支付', 'order:pay', 2, '支付订单尾款', 1, '2025-12-31 14:40:32', '2025-12-31 14:40:32', 0);
INSERT INTO `auction_permission` VALUES (12, '保证金查看', 'deposit:view', 1, '查看保证金', 1, '2025-12-31 14:40:32', '2025-12-31 14:40:32', 0);
INSERT INTO `auction_permission` VALUES (13, '保证金充值', 'deposit:add', 2, '充值保证金', 1, '2025-12-31 14:40:32', '2025-12-31 14:40:32', 0);
INSERT INTO `auction_permission` VALUES (14, '保证金解冻', 'deposit:edit', 2, '申请保证金解冻', 1, '2025-12-31 14:40:32', '2025-12-31 14:40:32', 0);
INSERT INTO `auction_permission` VALUES (15, '消息查看', 'message:view', 1, '查看消息', 1, '2025-12-31 14:40:32', '2025-12-31 14:40:32', 0);
INSERT INTO `auction_permission` VALUES (16, '消息发送', 'message:add', 2, '发送消息', 1, '2025-12-31 14:40:32', '2025-12-31 14:40:32', 0);
INSERT INTO `auction_permission` VALUES (20, '用户查看', 'admin:user:view', 1, '查看用户列表', 1, '2025-12-31 14:40:32', '2025-12-31 14:40:32', 0);
INSERT INTO `auction_permission` VALUES (21, '用户新增', 'admin:user:add', 2, '新增用户', 1, '2025-12-31 14:40:32', '2025-12-31 14:40:32', 0);
INSERT INTO `auction_permission` VALUES (22, '用户编辑', 'admin:user:edit', 2, '编辑用户信息', 1, '2025-12-31 14:40:32', '2025-12-31 14:40:32', 0);
INSERT INTO `auction_permission` VALUES (23, '用户删除', 'admin:user:delete', 2, '删除用户（禁用）', 1, '2025-12-31 14:40:32', '2025-12-31 14:40:32', 0);
INSERT INTO `auction_permission` VALUES (24, '用户状态管理', 'admin:user:status', 2, '启用/禁用用户', 1, '2025-12-31 14:40:32', '2025-12-31 14:40:32', 0);
INSERT INTO `auction_permission` VALUES (25, '卖方资质审核', 'admin:user:audit', 2, '审核卖方用户资质', 1, '2025-12-31 14:40:32', '2025-12-31 14:40:32', 0);
INSERT INTO `auction_permission` VALUES (30, '类目查看', 'admin:category:view', 1, '查看类目列表', 1, '2025-12-31 14:40:32', '2025-12-31 14:40:32', 0);
INSERT INTO `auction_permission` VALUES (31, '类目新增', 'admin:category:add', 2, '新增商品类目', 1, '2025-12-31 14:40:32', '2025-12-31 14:40:32', 0);
INSERT INTO `auction_permission` VALUES (32, '类目编辑', 'admin:category:edit', 2, '编辑商品类目', 1, '2025-12-31 14:40:32', '2025-12-31 14:40:32', 0);
INSERT INTO `auction_permission` VALUES (33, '类目删除', 'admin:category:delete', 2, '删除商品类目', 1, '2025-12-31 14:40:32', '2025-12-31 14:40:32', 0);
INSERT INTO `auction_permission` VALUES (40, '商品查看', 'admin:goods:view', 1, '查看商品列表', 1, '2025-12-31 14:40:32', '2025-12-31 14:40:32', 0);
INSERT INTO `auction_permission` VALUES (41, '商品审核', 'admin:goods:audit', 2, '审核商品', 1, '2025-12-31 14:40:32', '2025-12-31 14:40:32', 0);
INSERT INTO `auction_permission` VALUES (42, '商品编辑', 'admin:goods:edit', 2, '编辑商品信息', 1, '2025-12-31 14:40:32', '2025-12-31 14:40:32', 0);
INSERT INTO `auction_permission` VALUES (43, '商品删除', 'admin:goods:delete', 2, '删除/下架商品', 1, '2025-12-31 14:40:32', '2025-12-31 14:40:32', 0);
INSERT INTO `auction_permission` VALUES (50, '历史竞拍查看', 'admin:history:view', 1, '查看历史竞拍', 1, '2025-12-31 14:40:32', '2025-12-31 14:40:32', 0);
INSERT INTO `auction_permission` VALUES (51, '历史订单审核', 'admin:history:audit', 2, '审核历史订单', 1, '2025-12-31 14:40:32', '2025-12-31 14:40:32', 0);
INSERT INTO `auction_permission` VALUES (52, '历史订单导出', 'admin:history:export', 2, '导出历史订单报表', 1, '2025-12-31 14:40:32', '2025-12-31 14:40:32', 0);
INSERT INTO `auction_permission` VALUES (60, '订单查看', 'admin:order:view', 1, '查看订单列表', 1, '2025-12-31 14:40:32', '2025-12-31 14:40:32', 0);
INSERT INTO `auction_permission` VALUES (61, '订单编辑', 'admin:order:edit', 2, '编辑订单信息', 1, '2025-12-31 14:40:32', '2025-12-31 14:40:32', 0);
INSERT INTO `auction_permission` VALUES (62, '订单状态管理', 'admin:order:status', 2, '管理订单状态', 1, '2025-12-31 14:40:32', '2025-12-31 14:40:32', 0);
INSERT INTO `auction_permission` VALUES (70, '留言查看', 'admin:message:view', 1, '查看留言列表', 1, '2025-12-31 14:40:32', '2025-12-31 14:40:32', 0);
INSERT INTO `auction_permission` VALUES (71, '留言回复', 'admin:message:edit', 2, '回复留言', 1, '2025-12-31 14:40:32', '2025-12-31 14:40:32', 0);
INSERT INTO `auction_permission` VALUES (72, '留言删除', 'admin:message:delete', 2, '删除留言', 1, '2025-12-31 14:40:32', '2025-12-31 14:40:32', 0);
INSERT INTO `auction_permission` VALUES (80, '轮播图查看', 'admin:banner:view', 1, '查看轮播图列表', 1, '2025-12-31 14:40:32', '2025-12-31 14:40:32', 0);
INSERT INTO `auction_permission` VALUES (81, '轮播图新增', 'admin:banner:add', 2, '新增轮播图', 1, '2025-12-31 14:40:32', '2025-12-31 14:40:32', 0);
INSERT INTO `auction_permission` VALUES (82, '轮播图编辑', 'admin:banner:edit', 2, '编辑轮播图', 1, '2025-12-31 14:40:32', '2025-12-31 14:40:32', 0);
INSERT INTO `auction_permission` VALUES (83, '轮播图删除', 'admin:banner:delete', 2, '删除轮播图', 1, '2025-12-31 14:40:32', '2025-12-31 14:40:32', 0);
INSERT INTO `auction_permission` VALUES (90, '公告查看', 'admin:notice:view', 1, '查看公告列表', 1, '2025-12-31 14:40:32', '2025-12-31 14:40:32', 0);
INSERT INTO `auction_permission` VALUES (91, '公告新增', 'admin:notice:add', 2, '发布公告', 1, '2025-12-31 14:40:32', '2025-12-31 14:40:32', 0);
INSERT INTO `auction_permission` VALUES (92, '公告编辑', 'admin:notice:edit', 2, '编辑公告', 1, '2025-12-31 14:40:32', '2025-12-31 14:40:32', 0);
INSERT INTO `auction_permission` VALUES (93, '公告删除', 'admin:notice:delete', 2, '删除/下架公告', 1, '2025-12-31 14:40:32', '2025-12-31 14:40:32', 0);

-- ----------------------------
-- Table structure for auction_record
-- ----------------------------
DROP TABLE IF EXISTS `auction_record`;
CREATE TABLE `auction_record`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '竞拍记录ID，自增',
  `goods_id` bigint NOT NULL COMMENT '商品ID（关联auction_goods.id）',
  `buyer_id` bigint NOT NULL COMMENT '买方用户ID（关联auction_user.id）',
  `bid_price` decimal(10, 2) NOT NULL COMMENT '出价金额',
  `is_agent` tinyint NOT NULL DEFAULT 0 COMMENT '是否代理出价：0=手动出价 1=代理出价',
  `agent_max_price` decimal(10, 2) NULL DEFAULT NULL COMMENT '代理出价最高心理价位（is_agent=1时必填）',
  `bid_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '出价时间',
  `is_highest` tinyint NOT NULL DEFAULT 0 COMMENT '是否当前最高价：0=否 1=是（实时更新）',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=未删除 1=已删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_goods_id`(`goods_id` ASC) USING BTREE,
  INDEX `idx_buyer_id`(`buyer_id` ASC) USING BTREE,
  INDEX `idx_is_highest`(`is_highest` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '竞拍记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of auction_record
-- ----------------------------

-- ----------------------------
-- Table structure for auction_role_permission
-- ----------------------------
DROP TABLE IF EXISTS `auction_role_permission`;
CREATE TABLE `auction_role_permission`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID，自增',
  `role_type` tinyint NOT NULL COMMENT '角色类型：1=买方用户 2=卖方用户 3=后台管理员',
  `permission_id` bigint NOT NULL COMMENT '权限ID（关联auction_permission.id）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `idx_role_permission`(`role_type` ASC, `permission_id` ASC) USING BTREE,
  INDEX `idx_role_type`(`role_type` ASC) USING BTREE,
  INDEX `idx_permission_id`(`permission_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 128 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '角色权限关联表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of auction_role_permission
-- ----------------------------
INSERT INTO `auction_role_permission` VALUES (71, 1, 1, '2025-12-31 14:40:32');
INSERT INTO `auction_role_permission` VALUES (72, 1, 2, '2025-12-31 14:40:32');
INSERT INTO `auction_role_permission` VALUES (73, 1, 3, '2025-12-31 14:40:32');
INSERT INTO `auction_role_permission` VALUES (74, 1, 4, '2025-12-31 14:40:32');
INSERT INTO `auction_role_permission` VALUES (75, 1, 5, '2025-12-31 14:40:32');
INSERT INTO `auction_role_permission` VALUES (76, 1, 9, '2025-12-31 14:40:32');
INSERT INTO `auction_role_permission` VALUES (77, 1, 10, '2025-12-31 14:40:32');
INSERT INTO `auction_role_permission` VALUES (78, 1, 11, '2025-12-31 14:40:32');
INSERT INTO `auction_role_permission` VALUES (79, 1, 12, '2025-12-31 14:40:32');
INSERT INTO `auction_role_permission` VALUES (80, 1, 13, '2025-12-31 14:40:32');
INSERT INTO `auction_role_permission` VALUES (81, 1, 14, '2025-12-31 14:40:32');
INSERT INTO `auction_role_permission` VALUES (82, 1, 15, '2025-12-31 14:40:32');
INSERT INTO `auction_role_permission` VALUES (83, 1, 16, '2025-12-31 14:40:32');
INSERT INTO `auction_role_permission` VALUES (84, 2, 1, '2025-12-31 14:40:32');
INSERT INTO `auction_role_permission` VALUES (85, 2, 2, '2025-12-31 14:40:32');
INSERT INTO `auction_role_permission` VALUES (86, 2, 3, '2025-12-31 14:40:32');
INSERT INTO `auction_role_permission` VALUES (87, 2, 9, '2025-12-31 14:40:32');
INSERT INTO `auction_role_permission` VALUES (88, 2, 10, '2025-12-31 14:40:32');
INSERT INTO `auction_role_permission` VALUES (89, 2, 11, '2025-12-31 14:40:32');
INSERT INTO `auction_role_permission` VALUES (90, 2, 12, '2025-12-31 14:40:32');
INSERT INTO `auction_role_permission` VALUES (91, 2, 13, '2025-12-31 14:40:32');
INSERT INTO `auction_role_permission` VALUES (92, 2, 14, '2025-12-31 14:40:32');
INSERT INTO `auction_role_permission` VALUES (93, 2, 15, '2025-12-31 14:40:32');
INSERT INTO `auction_role_permission` VALUES (94, 2, 16, '2025-12-31 14:40:32');
INSERT INTO `auction_role_permission` VALUES (95, 2, 6, '2025-12-31 14:40:32');
INSERT INTO `auction_role_permission` VALUES (96, 2, 7, '2025-12-31 14:40:32');
INSERT INTO `auction_role_permission` VALUES (97, 2, 8, '2025-12-31 14:40:32');
INSERT INTO `auction_role_permission` VALUES (98, 3, 20, '2025-12-31 14:40:32');
INSERT INTO `auction_role_permission` VALUES (99, 3, 21, '2025-12-31 14:40:32');
INSERT INTO `auction_role_permission` VALUES (100, 3, 22, '2025-12-31 14:40:32');
INSERT INTO `auction_role_permission` VALUES (101, 3, 23, '2025-12-31 14:40:32');
INSERT INTO `auction_role_permission` VALUES (102, 3, 24, '2025-12-31 14:40:32');
INSERT INTO `auction_role_permission` VALUES (103, 3, 25, '2025-12-31 14:40:32');
INSERT INTO `auction_role_permission` VALUES (104, 3, 30, '2025-12-31 14:40:32');
INSERT INTO `auction_role_permission` VALUES (105, 3, 31, '2025-12-31 14:40:32');
INSERT INTO `auction_role_permission` VALUES (106, 3, 32, '2025-12-31 14:40:32');
INSERT INTO `auction_role_permission` VALUES (107, 3, 33, '2025-12-31 14:40:32');
INSERT INTO `auction_role_permission` VALUES (108, 3, 40, '2025-12-31 14:40:32');
INSERT INTO `auction_role_permission` VALUES (109, 3, 41, '2025-12-31 14:40:32');
INSERT INTO `auction_role_permission` VALUES (110, 3, 42, '2025-12-31 14:40:32');
INSERT INTO `auction_role_permission` VALUES (111, 3, 43, '2025-12-31 14:40:32');
INSERT INTO `auction_role_permission` VALUES (112, 3, 50, '2025-12-31 14:40:32');
INSERT INTO `auction_role_permission` VALUES (113, 3, 51, '2025-12-31 14:40:32');
INSERT INTO `auction_role_permission` VALUES (114, 3, 52, '2025-12-31 14:40:32');
INSERT INTO `auction_role_permission` VALUES (115, 3, 60, '2025-12-31 14:40:32');
INSERT INTO `auction_role_permission` VALUES (116, 3, 61, '2025-12-31 14:40:32');
INSERT INTO `auction_role_permission` VALUES (117, 3, 62, '2025-12-31 14:40:32');
INSERT INTO `auction_role_permission` VALUES (118, 3, 70, '2025-12-31 14:40:32');
INSERT INTO `auction_role_permission` VALUES (119, 3, 71, '2025-12-31 14:40:32');
INSERT INTO `auction_role_permission` VALUES (120, 3, 72, '2025-12-31 14:40:32');
INSERT INTO `auction_role_permission` VALUES (121, 3, 80, '2025-12-31 14:40:32');
INSERT INTO `auction_role_permission` VALUES (122, 3, 81, '2025-12-31 14:40:32');
INSERT INTO `auction_role_permission` VALUES (123, 3, 82, '2025-12-31 14:40:32');
INSERT INTO `auction_role_permission` VALUES (124, 3, 83, '2025-12-31 14:40:32');
INSERT INTO `auction_role_permission` VALUES (125, 3, 90, '2025-12-31 14:40:32');
INSERT INTO `auction_role_permission` VALUES (126, 3, 91, '2025-12-31 14:40:32');
INSERT INTO `auction_role_permission` VALUES (127, 3, 92, '2025-12-31 14:40:32');
INSERT INTO `auction_role_permission` VALUES (128, 3, 93, '2025-12-31 14:40:32');

-- ----------------------------
-- Table structure for auction_user
-- ----------------------------
DROP TABLE IF EXISTS `auction_user`;
CREATE TABLE `auction_user`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID，自增',
  `user_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名（登录账号）',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码（MD5加密存储）',
  `real_name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '真实姓名（实名认证用）',
  `nick_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户昵称',
  `phone` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '手机号（唯一索引）',
  `email` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '用户邮箱',
  `avatar` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '用户头像地址',
  `sex` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '2' COMMENT '用户性别（0男 1女 2未知）',
  `user_role` tinyint NOT NULL COMMENT '用户角色：1=买方用户 2=卖方用户 3=后台管理员',
  `seller_audit_status` tinyint NULL DEFAULT 0 COMMENT '卖方资质审核状态：0=未提交 1=待审核 2=审核通过 3=审核驳回（买方默认为0）',
  `seller_audit_remark` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '卖方资质审核驳回原因',
  `user_status` tinyint NOT NULL DEFAULT 0 COMMENT '账号状态：0=正常 1=禁用（替代物理删除）',
  `login_ip` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '最后登录IP',
  `login_date` datetime NULL DEFAULT NULL COMMENT '最后登录时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间（自动填充）',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间（自动填充）',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=未删除 1=已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `idx_phone`(`phone` ASC) USING BTREE,
  UNIQUE INDEX `idx_user_name`(`user_name` ASC) USING BTREE,
  INDEX `idx_user_role`(`user_role` ASC) USING BTREE,
  INDEX `idx_user_status`(`user_status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统用户表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of auction_user
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;
