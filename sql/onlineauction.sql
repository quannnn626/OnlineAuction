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

 Date: 12/03/2026 10:22:18
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
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '首页轮播图表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of auction_banner
-- ----------------------------
INSERT INTO `auction_banner` VALUES (1, '/upload/banner/202602/d3627db1824f407cbd03b8cb07c68253.png', NULL, 0, 0, '2026-02-26 15:52:56', '2026-02-26 15:52:56', 0);
INSERT INTO `auction_banner` VALUES (2, '/upload/banner/202602/4ee15e77bef74dfdaa3eae6b6e0be01d.jpg', NULL, 1, 1, '2026-02-26 15:53:03', '2026-02-26 15:53:03', 0);

-- ----------------------------
-- Table structure for auction_category
-- ----------------------------
DROP TABLE IF EXISTS `auction_category`;
CREATE TABLE `auction_category`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '类目ID，自增',
  `parent_id` bigint NOT NULL DEFAULT 0 COMMENT '父分类ID（0表示顶级分类/一级分类）',
  `level` tinyint NOT NULL DEFAULT 1 COMMENT '分类层级：1=一级分类（大类） 2=二级分类（小类） 3=三级分类（具体类型）',
  `category_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '类目名称（如：奢侈品、房产、艺术品）',
  `category_sort` int NOT NULL DEFAULT 0 COMMENT '排序值（数字越小越靠前）',
  `category_status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0=禁用 1=启用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=未删除 1=已删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_parent_id`(`parent_id` ASC) USING BTREE,
  INDEX `idx_level`(`level` ASC) USING BTREE,
  INDEX `idx_category_status`(`category_status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 101316 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '拍卖商品类目表（支持多级分类）' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of auction_category
-- ----------------------------
INSERT INTO `auction_category` VALUES (1, 0, 1, '艺术品', 1, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (2, 0, 1, '珠宝首饰', 2, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (3, 0, 1, '古玩杂项', 3, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (4, 0, 1, '名表名包', 4, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (5, 0, 1, '房产汽车', 5, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (6, 0, 1, '酒类收藏', 6, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (7, 0, 1, '邮品钱币', 7, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (8, 0, 1, '古籍善本', 8, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (9, 0, 1, '现当代艺术', 9, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (10, 0, 1, '奢侈品', 10, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (11, 1, 2, '书画', 1, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (12, 1, 2, '陶瓷', 2, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (13, 1, 2, '玉器', 3, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (14, 1, 2, '雕塑', 4, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (15, 1, 2, '当代艺术', 5, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (21, 2, 2, '翡翠玉石', 1, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (22, 2, 2, '钻石', 2, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (23, 2, 2, '黄金', 3, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (24, 2, 2, '彩色宝石', 4, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (25, 2, 2, '珍珠', 5, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (31, 3, 2, '青铜器', 1, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (32, 3, 2, '钱币', 2, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (33, 3, 2, '文房四宝', 3, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (34, 3, 2, '紫砂壶', 4, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (35, 3, 2, '竹木牙角', 5, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (41, 4, 2, '瑞士名表', 1, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (42, 4, 2, '古董表', 2, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (43, 4, 2, '奢侈包袋', 3, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (44, 4, 2, '配饰', 4, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (51, 5, 2, '房产', 1, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (52, 5, 2, '汽车', 2, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (53, 5, 2, '摩托车', 3, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (61, 6, 2, '白酒', 1, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (62, 6, 2, '红酒', 2, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (63, 6, 2, '威士忌', 3, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (71, 7, 2, '邮票', 1, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (72, 7, 2, '邮封', 2, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (73, 7, 2, '邮戳', 3, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (74, 7, 2, '钱币', 4, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (75, 7, 2, '纸币', 5, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (81, 8, 2, '古籍', 1, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (82, 8, 2, '善本', 2, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (83, 8, 2, '碑帖', 3, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (84, 8, 2, '信札', 4, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (85, 8, 2, '手稿', 5, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (91, 9, 2, '当代绘画', 1, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (92, 9, 2, '当代雕塑', 2, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (93, 9, 2, '装置艺术', 3, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (94, 9, 2, '影像艺术', 4, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (95, 9, 2, '行为艺术', 5, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (101, 10, 2, '高级定制', 1, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (102, 10, 2, '珠宝', 2, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (103, 10, 2, '腕表', 3, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (104, 10, 2, '皮具', 4, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (105, 10, 2, '配饰', 5, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (111, 11, 3, '国画', 1, 1, '2026-01-20 16:08:33', '2026-01-21 14:50:33', 0);
INSERT INTO `auction_category` VALUES (112, 11, 3, '油画', 2, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (113, 11, 3, '书法', 3, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (114, 11, 3, '版画', 4, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (115, 11, 3, '水彩画', 5, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (121, 12, 3, '明清瓷器', 1, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (122, 12, 3, '现代陶瓷', 2, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (123, 12, 3, '官窑瓷器', 3, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (124, 12, 3, '民窑瓷器', 4, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (131, 13, 3, '和田玉', 1, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (132, 13, 3, '翡翠', 2, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (133, 13, 3, '岫玉', 3, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (134, 13, 3, '古玉', 4, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (141, 14, 3, '石雕', 1, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (142, 14, 3, '木雕', 2, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (143, 14, 3, '铜雕', 3, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (144, 14, 3, '玉雕', 4, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (145, 14, 3, '现代雕塑', 5, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (151, 15, 3, '抽象艺术', 1, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (152, 15, 3, '观念艺术', 2, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (153, 15, 3, '新媒体艺术', 3, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (200, 0, 1, '钟表收藏', 11, 1, '2026-01-20 16:08:34', '2026-01-20 16:08:34', 0);
INSERT INTO `auction_category` VALUES (201, 0, 1, '茶具收藏', 12, 1, '2026-01-20 16:08:34', '2026-01-20 16:08:34', 0);
INSERT INTO `auction_category` VALUES (202, 0, 1, '文玩收藏', 13, 1, '2026-01-20 16:08:34', '2026-01-20 16:08:34', 0);
INSERT INTO `auction_category` VALUES (203, 0, 1, '红色收藏', 14, 1, '2026-01-20 16:08:34', '2026-01-20 16:08:34', 0);
INSERT INTO `auction_category` VALUES (204, 0, 1, '当代工艺品', 15, 1, '2026-01-20 16:08:34', '2026-01-20 16:08:34', 0);
INSERT INTO `auction_category` VALUES (211, 21, 3, '翡翠手镯', 1, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (212, 21, 3, '挂件吊坠', 2, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (213, 21, 3, '翡翠戒指', 3, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (214, 21, 3, '翡翠项链', 4, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (215, 21, 3, '翡翠耳环', 5, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (221, 22, 3, '钻戒', 1, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (222, 22, 3, '钻石项链', 2, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (223, 22, 3, '钻石耳环', 3, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (224, 22, 3, '钻石手链', 4, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (231, 23, 3, '黄金饰品', 1, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (232, 23, 3, '金条', 2, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (233, 23, 3, '金币', 3, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (241, 24, 3, '红宝石', 1, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (242, 24, 3, '蓝宝石', 2, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (243, 24, 3, '祖母绿', 3, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (244, 24, 3, '碧玺', 4, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (245, 24, 3, '坦桑石', 5, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (246, 24, 3, '其他彩色宝石', 6, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (251, 25, 3, '海水珍珠', 1, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (252, 25, 3, '淡水珍珠', 2, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (253, 25, 3, '南洋珍珠', 3, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (254, 25, 3, '大溪地黑珍珠', 4, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (255, 25, 3, '古董珍珠', 5, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (311, 31, 3, '青铜礼器', 1, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (312, 31, 3, '青铜兵器', 2, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (313, 31, 3, '青铜杂器', 3, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (321, 32, 3, '古钱币', 1, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (322, 32, 3, '机制币', 2, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (323, 32, 3, '纸币', 3, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (324, 32, 3, '纪念币', 4, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (331, 33, 3, '毛笔', 1, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (332, 33, 3, '墨', 2, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (333, 33, 3, '纸', 3, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (334, 33, 3, '砚台', 4, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (341, 34, 3, '明清紫砂', 1, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (342, 34, 3, '现代紫砂', 2, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (343, 34, 3, '名家紫砂', 3, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (344, 34, 3, '紫砂摆件', 4, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (351, 35, 3, '竹雕', 1, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (352, 35, 3, '木雕', 2, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (353, 35, 3, '象牙', 3, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (354, 35, 3, '角雕', 4, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (355, 35, 3, '核雕', 5, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (411, 41, 3, '百达翡丽', 1, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (412, 41, 3, '劳力士', 2, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (413, 41, 3, '江诗丹顿', 3, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (414, 41, 3, '欧米茄', 4, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (421, 42, 3, '怀表', 1, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (422, 42, 3, '座钟', 2, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (423, 42, 3, '古董腕表', 3, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (424, 42, 3, '限量版', 4, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (431, 43, 3, '爱马仕', 1, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (432, 43, 3, '香奈儿', 2, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (433, 43, 3, '路易威登', 3, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (434, 43, 3, '古驰', 4, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (441, 44, 3, '太阳镜', 1, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (442, 44, 3, '丝巾', 2, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (443, 44, 3, '腰带', 3, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (444, 44, 3, '钱包', 4, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (445, 44, 3, '卡包', 5, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (511, 51, 3, '住宅', 1, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (512, 51, 3, '商业地产', 2, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (513, 51, 3, '土地', 3, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (514, 51, 3, '别墅', 4, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (515, 51, 3, '商铺', 5, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (516, 51, 3, '写字楼', 6, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (521, 52, 3, '轿车', 1, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (522, 52, 3, 'SUV', 2, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (523, 52, 3, '跑车', 3, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (524, 52, 3, '古董车', 4, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (525, 52, 3, '豪华车', 5, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (526, 52, 3, '新能源车', 6, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (531, 53, 3, '哈雷', 1, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (532, 53, 3, '杜卡迪', 2, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (533, 53, 3, '宝马摩托', 3, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (534, 53, 3, '其他品牌', 4, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (611, 61, 3, '茅台', 1, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (612, 61, 3, '五粮液', 2, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (613, 61, 3, '剑南春', 3, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (614, 61, 3, '泸州老窖', 4, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (615, 61, 3, '洋河', 5, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (616, 61, 3, '郎酒', 6, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (617, 61, 3, '汾酒', 7, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (618, 61, 3, '古井贡酒', 8, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (619, 61, 3, '其他名酒', 9, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (621, 62, 3, '拉菲', 1, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (622, 62, 3, '拉图', 2, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (623, 62, 3, '玛歌', 3, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (624, 62, 3, '木桐', 4, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (625, 62, 3, '奥比昂', 5, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (626, 62, 3, '勃艮第', 6, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (627, 62, 3, '其他红酒', 7, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (631, 63, 3, '苏格兰威士忌', 1, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (632, 63, 3, '日本威士忌', 2, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (633, 63, 3, '美国威士忌', 3, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (634, 63, 3, '其他威士忌', 4, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (711, 71, 3, '清代邮票', 1, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (712, 71, 3, '民国邮票', 2, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (713, 71, 3, '新中国邮票', 3, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (714, 71, 3, '外国邮票', 4, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (715, 71, 3, '特种邮票', 5, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (716, 71, 3, '纪念邮票', 6, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (721, 72, 3, '实寄封', 1, 1, '2026-01-20 16:08:34', '2026-01-20 16:08:34', 0);
INSERT INTO `auction_category` VALUES (722, 72, 3, '首日封', 2, 1, '2026-01-20 16:08:34', '2026-01-20 16:08:34', 0);
INSERT INTO `auction_category` VALUES (723, 72, 3, '纪念封', 3, 1, '2026-01-20 16:08:34', '2026-01-20 16:08:34', 0);
INSERT INTO `auction_category` VALUES (724, 72, 3, '其他邮封', 4, 1, '2026-01-20 16:08:34', '2026-01-20 16:08:34', 0);
INSERT INTO `auction_category` VALUES (731, 73, 3, '日戳', 1, 1, '2026-01-20 16:08:34', '2026-01-20 16:08:34', 0);
INSERT INTO `auction_category` VALUES (732, 73, 3, '纪念戳', 2, 1, '2026-01-20 16:08:34', '2026-01-20 16:08:34', 0);
INSERT INTO `auction_category` VALUES (733, 73, 3, '风景戳', 3, 1, '2026-01-20 16:08:34', '2026-01-20 16:08:34', 0);
INSERT INTO `auction_category` VALUES (734, 73, 3, '其他邮戳', 4, 1, '2026-01-20 16:08:34', '2026-01-20 16:08:34', 0);
INSERT INTO `auction_category` VALUES (741, 74, 3, '古钱币', 1, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (742, 74, 3, '机制币', 2, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (743, 74, 3, '银元', 3, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (744, 74, 3, '铜币', 4, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (745, 74, 3, '纪念币', 5, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (751, 75, 3, '第一套人民币', 1, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (752, 75, 3, '第二套人民币', 2, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (753, 75, 3, '第三套人民币', 3, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (754, 75, 3, '第四套人民币', 4, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (755, 75, 3, '第五套人民币', 5, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (756, 75, 3, '外汇券', 6, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (757, 75, 3, '外国纸币', 7, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (811, 81, 3, '宋刻本', 1, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (812, 81, 3, '元刻本', 2, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (813, 81, 3, '明刻本', 3, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (814, 81, 3, '清刻本', 4, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (815, 81, 3, '民国刻本', 5, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (816, 81, 3, '抄本', 6, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (821, 82, 3, '经部', 1, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (822, 82, 3, '史部', 2, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (823, 82, 3, '子部', 3, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (824, 82, 3, '集部', 4, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (831, 83, 3, '碑拓', 1, 1, '2026-01-20 16:08:34', '2026-01-20 16:08:34', 0);
INSERT INTO `auction_category` VALUES (832, 83, 3, '帖拓', 2, 1, '2026-01-20 16:08:34', '2026-01-20 16:08:34', 0);
INSERT INTO `auction_category` VALUES (833, 83, 3, '其他碑帖', 3, 1, '2026-01-20 16:08:34', '2026-01-20 16:08:34', 0);
INSERT INTO `auction_category` VALUES (841, 84, 3, '名人信札', 1, 1, '2026-01-20 16:08:34', '2026-01-20 16:08:34', 0);
INSERT INTO `auction_category` VALUES (842, 84, 3, '历史信札', 2, 1, '2026-01-20 16:08:34', '2026-01-20 16:08:34', 0);
INSERT INTO `auction_category` VALUES (843, 84, 3, '其他信札', 3, 1, '2026-01-20 16:08:34', '2026-01-20 16:08:34', 0);
INSERT INTO `auction_category` VALUES (851, 85, 3, '名人手稿', 1, 1, '2026-01-20 16:08:34', '2026-01-20 16:08:34', 0);
INSERT INTO `auction_category` VALUES (852, 85, 3, '历史手稿', 2, 1, '2026-01-20 16:08:34', '2026-01-20 16:08:34', 0);
INSERT INTO `auction_category` VALUES (853, 85, 3, '其他手稿', 3, 1, '2026-01-20 16:08:34', '2026-01-20 16:08:34', 0);
INSERT INTO `auction_category` VALUES (911, 91, 3, '抽象绘画', 1, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (912, 91, 3, '写实绘画', 2, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (913, 91, 3, '表现主义', 3, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (914, 91, 3, '超现实主义', 4, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (915, 91, 3, '波普艺术', 5, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (921, 92, 3, '金属雕塑', 1, 1, '2026-01-20 16:08:34', '2026-01-20 16:08:34', 0);
INSERT INTO `auction_category` VALUES (922, 92, 3, '石雕', 2, 1, '2026-01-20 16:08:34', '2026-01-20 16:08:34', 0);
INSERT INTO `auction_category` VALUES (923, 92, 3, '木雕', 3, 1, '2026-01-20 16:08:34', '2026-01-20 16:08:34', 0);
INSERT INTO `auction_category` VALUES (924, 92, 3, '其他雕塑', 4, 1, '2026-01-20 16:08:34', '2026-01-20 16:08:34', 0);
INSERT INTO `auction_category` VALUES (931, 93, 3, '装置艺术', 1, 1, '2026-01-20 16:08:34', '2026-01-20 16:08:34', 0);
INSERT INTO `auction_category` VALUES (932, 93, 3, '其他装置', 2, 1, '2026-01-20 16:08:34', '2026-01-20 16:08:34', 0);
INSERT INTO `auction_category` VALUES (941, 94, 3, '摄影作品', 1, 1, '2026-01-20 16:08:34', '2026-01-20 16:08:34', 0);
INSERT INTO `auction_category` VALUES (942, 94, 3, '视频艺术', 2, 1, '2026-01-20 16:08:34', '2026-01-20 16:08:34', 0);
INSERT INTO `auction_category` VALUES (943, 94, 3, '其他影像', 3, 1, '2026-01-20 16:08:34', '2026-01-20 16:08:34', 0);
INSERT INTO `auction_category` VALUES (951, 95, 3, '行为艺术', 1, 1, '2026-01-20 16:08:34', '2026-01-20 16:08:34', 0);
INSERT INTO `auction_category` VALUES (952, 95, 3, '其他行为', 2, 1, '2026-01-20 16:08:34', '2026-01-20 16:08:34', 0);
INSERT INTO `auction_category` VALUES (1011, 101, 3, '高级时装', 1, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (1012, 101, 3, '定制礼服', 2, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (1013, 101, 3, '定制西装', 3, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (1021, 102, 3, '高级珠宝', 1, 1, '2026-01-20 16:08:34', '2026-01-20 16:08:34', 0);
INSERT INTO `auction_category` VALUES (1022, 102, 3, '其他珠宝', 2, 1, '2026-01-20 16:08:34', '2026-01-20 16:08:34', 0);
INSERT INTO `auction_category` VALUES (1031, 103, 3, '高级腕表', 1, 1, '2026-01-20 16:08:34', '2026-01-20 16:08:34', 0);
INSERT INTO `auction_category` VALUES (1032, 103, 3, '其他腕表', 2, 1, '2026-01-20 16:08:34', '2026-01-20 16:08:34', 0);
INSERT INTO `auction_category` VALUES (1041, 104, 3, '高级皮具', 1, 1, '2026-01-20 16:08:34', '2026-01-20 16:08:34', 0);
INSERT INTO `auction_category` VALUES (1042, 104, 3, '其他皮具', 2, 1, '2026-01-20 16:08:34', '2026-01-20 16:08:34', 0);
INSERT INTO `auction_category` VALUES (1051, 105, 3, '高级配饰', 1, 1, '2026-01-20 16:08:34', '2026-01-20 16:08:34', 0);
INSERT INTO `auction_category` VALUES (1052, 105, 3, '其他配饰', 2, 1, '2026-01-20 16:08:34', '2026-01-20 16:08:34', 0);
INSERT INTO `auction_category` VALUES (2001, 200, 2, '怀表', 1, 1, '2026-01-20 16:08:34', '2026-01-20 16:08:34', 0);
INSERT INTO `auction_category` VALUES (2002, 200, 2, '座钟', 2, 1, '2026-01-20 16:08:34', '2026-01-20 16:08:34', 0);
INSERT INTO `auction_category` VALUES (2003, 200, 2, '挂钟', 3, 1, '2026-01-20 16:08:34', '2026-01-20 16:08:34', 0);
INSERT INTO `auction_category` VALUES (2011, 201, 2, '紫砂茶具', 1, 1, '2026-01-20 16:08:34', '2026-01-20 16:08:34', 0);
INSERT INTO `auction_category` VALUES (2012, 201, 2, '瓷器茶具', 2, 1, '2026-01-20 16:08:34', '2026-01-20 16:08:34', 0);
INSERT INTO `auction_category` VALUES (2013, 201, 2, '银器茶具', 3, 1, '2026-01-20 16:08:34', '2026-01-20 16:08:34', 0);
INSERT INTO `auction_category` VALUES (2014, 201, 2, '其他茶具', 4, 1, '2026-01-20 16:08:34', '2026-01-20 16:08:34', 0);
INSERT INTO `auction_category` VALUES (2021, 202, 2, '手串', 1, 1, '2026-01-20 16:08:34', '2026-01-20 16:08:34', 0);
INSERT INTO `auction_category` VALUES (2022, 202, 2, '把件', 2, 1, '2026-01-20 16:08:34', '2026-01-20 16:08:34', 0);
INSERT INTO `auction_category` VALUES (2023, 202, 2, '摆件', 3, 1, '2026-01-20 16:08:34', '2026-01-20 16:08:34', 0);
INSERT INTO `auction_category` VALUES (2024, 202, 2, '其他文玩', 4, 1, '2026-01-20 16:08:34', '2026-01-20 16:08:34', 0);
INSERT INTO `auction_category` VALUES (2031, 203, 2, '红色文献', 1, 1, '2026-01-20 16:08:34', '2026-01-20 16:08:34', 0);
INSERT INTO `auction_category` VALUES (2032, 203, 2, '红色徽章', 2, 1, '2026-01-20 16:08:34', '2026-01-20 16:08:34', 0);
INSERT INTO `auction_category` VALUES (2033, 203, 2, '红色纪念品', 3, 1, '2026-01-20 16:08:34', '2026-01-20 16:08:34', 0);
INSERT INTO `auction_category` VALUES (2041, 204, 2, '当代陶瓷', 1, 1, '2026-01-20 16:08:34', '2026-01-20 16:08:34', 0);
INSERT INTO `auction_category` VALUES (2042, 204, 2, '当代漆器', 2, 1, '2026-01-20 16:08:34', '2026-01-20 16:08:34', 0);
INSERT INTO `auction_category` VALUES (2043, 204, 2, '当代金属工艺', 3, 1, '2026-01-20 16:08:34', '2026-01-20 16:08:34', 0);
INSERT INTO `auction_category` VALUES (2044, 204, 2, '其他工艺品', 4, 1, '2026-01-20 16:08:34', '2026-01-20 16:08:34', 0);
INSERT INTO `auction_category` VALUES (4124, 41, 3, '真力时', 13, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (4125, 41, 3, '百年灵', 14, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (4126, 41, 3, '宇舶', 15, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (4127, 41, 3, '萧邦', 16, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (4128, 41, 3, '伯爵', 17, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (4129, 41, 3, '其他品牌', 18, 1, '2026-01-20 16:08:33', '2026-01-20 16:08:33', 0);
INSERT INTO `auction_category` VALUES (20111, 2011, 3, '紫砂壶', 1, 1, '2026-01-20 16:08:34', '2026-01-20 16:08:34', 0);
INSERT INTO `auction_category` VALUES (20112, 2011, 3, '紫砂杯', 2, 1, '2026-01-20 16:08:34', '2026-01-20 16:08:34', 0);
INSERT INTO `auction_category` VALUES (20113, 2011, 3, '紫砂茶盘', 3, 1, '2026-01-20 16:08:34', '2026-01-20 16:08:34', 0);
INSERT INTO `auction_category` VALUES (20114, 2011, 3, '其他紫砂', 4, 1, '2026-01-20 16:08:34', '2026-01-20 16:08:34', 0);
INSERT INTO `auction_category` VALUES (20121, 2012, 3, '青花茶具', 1, 1, '2026-01-20 16:08:34', '2026-01-20 16:08:34', 0);
INSERT INTO `auction_category` VALUES (20122, 2012, 3, '粉彩茶具', 2, 1, '2026-01-20 16:08:34', '2026-01-20 16:08:34', 0);
INSERT INTO `auction_category` VALUES (20123, 2012, 3, '单色釉茶具', 3, 1, '2026-01-20 16:08:34', '2026-01-20 16:08:34', 0);
INSERT INTO `auction_category` VALUES (20124, 2012, 3, '其他瓷器茶具', 4, 1, '2026-01-20 16:08:34', '2026-01-20 16:08:34', 0);
INSERT INTO `auction_category` VALUES (20211, 2021, 3, '小叶紫檀', 1, 1, '2026-01-20 16:08:34', '2026-01-20 16:08:34', 0);
INSERT INTO `auction_category` VALUES (20212, 2021, 3, '海南黄花梨', 2, 1, '2026-01-20 16:08:34', '2026-01-20 16:08:34', 0);
INSERT INTO `auction_category` VALUES (20213, 2021, 3, '金丝楠', 3, 1, '2026-01-20 16:08:34', '2026-01-20 16:08:34', 0);
INSERT INTO `auction_category` VALUES (20214, 2021, 3, '沉香', 4, 1, '2026-01-20 16:08:34', '2026-01-20 16:08:34', 0);
INSERT INTO `auction_category` VALUES (20215, 2021, 3, '崖柏', 5, 1, '2026-01-20 16:08:34', '2026-01-20 16:08:34', 0);
INSERT INTO `auction_category` VALUES (20216, 2021, 3, '菩提', 6, 1, '2026-01-20 16:08:34', '2026-01-20 16:08:34', 0);
INSERT INTO `auction_category` VALUES (20217, 2021, 3, '其他手串', 7, 1, '2026-01-20 16:08:34', '2026-01-20 16:08:34', 0);
INSERT INTO `auction_category` VALUES (20221, 2022, 3, '和田玉把件', 1, 1, '2026-01-20 16:08:34', '2026-01-20 16:08:34', 0);
INSERT INTO `auction_category` VALUES (20222, 2022, 3, '翡翠把件', 2, 1, '2026-01-20 16:08:34', '2026-01-20 16:08:34', 0);
INSERT INTO `auction_category` VALUES (20223, 2022, 3, '其他把件', 3, 1, '2026-01-20 16:08:34', '2026-01-20 16:08:34', 0);
INSERT INTO `auction_category` VALUES (41111, 4111, 3, '百达翡丽', 1, 1, '2026-01-20 16:08:34', '2026-01-20 16:08:34', 0);
INSERT INTO `auction_category` VALUES (41112, 4111, 3, '其他百达翡丽', 2, 1, '2026-01-20 16:08:34', '2026-01-20 16:08:34', 0);
INSERT INTO `auction_category` VALUES (41121, 4112, 3, '劳力士', 1, 1, '2026-01-20 16:08:34', '2026-01-20 16:08:34', 0);
INSERT INTO `auction_category` VALUES (41122, 4112, 3, '其他劳力士', 2, 1, '2026-01-20 16:08:34', '2026-01-20 16:08:34', 0);
INSERT INTO `auction_category` VALUES (101313, 2041, 3, '义乌出品', 1, 1, '2026-01-20 16:35:41', '2026-01-20 16:35:41', 0);
INSERT INTO `auction_category` VALUES (101314, 0, 1, '现代艺术', 0, 1, '2026-01-21 14:51:05', '2026-01-21 14:52:29', 1);
INSERT INTO `auction_category` VALUES (101315, 101314, 2, '雕塑', 1, 1, '2026-01-21 14:51:20', '2026-01-21 14:52:26', 1);
INSERT INTO `auction_category` VALUES (101316, 101315, 3, '克劳狄乌斯半身像', 0, 1, '2026-01-21 14:52:13', '2026-01-21 14:52:19', 1);

-- ----------------------------
-- Table structure for auction_deposit
-- ----------------------------
DROP TABLE IF EXISTS `auction_deposit`;
CREATE TABLE `auction_deposit`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '保证金记录ID，自增',
  `user_id` bigint NOT NULL COMMENT '用户ID（关联auction_user.id）',
  `order_id` bigint NULL DEFAULT NULL COMMENT '关联订单ID（auction_order.id，无订单时为NULL）',
  `amount` decimal(20, 2) NOT NULL COMMENT '保证金金额',
  `deposit_type` tinyint NOT NULL COMMENT '变动类型：0=充值 1=冻结 2=解冻 3=抵扣尾款 4=扣除（悔拍）',
  `balance` decimal(20, 2) NOT NULL COMMENT '操作后保证金余额',
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
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=未删除 1=已删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_file_type`(`file_type` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 43 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '通用文件表（可存储商品图片、用户头像等各类文件）' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of auction_file
-- ----------------------------
INSERT INTO `auction_file` VALUES (1, 'QQ图片20230426170644.jpg', 'D:/auction/upload/goods/20261/35e88165a70643a899b720b354ed33ea.jpg', 'image', '2026-01-07 14:57:05', '2026-01-07 14:57:05', 0);
INSERT INTO `auction_file` VALUES (2, '微信图片_20250329220729.png', 'D:/auction/upload/goods/20261/fbad0c49f41d4b0198032fc30dd3148a.png', 'image', '2026-01-07 14:57:19', '2026-01-07 14:57:19', 0);
INSERT INTO `auction_file` VALUES (3, 'QQ图片20230426170644.jpg', 'D:/auction/upload/goods/202601/a7f1ea4e75764fac9867108c1a363fc0.jpg', 'image', '2026-01-07 17:14:46', '2026-01-07 17:14:45', 0);
INSERT INTO `auction_file` VALUES (4, 'QQ图片20230426170644.jpg', '/upload/goods/202601/a1c24be60eb5471b80a446dba4ee374e.jpg', 'image', '2026-01-07 17:38:15', '2026-01-07 17:38:15', 0);
INSERT INTO `auction_file` VALUES (5, '微信图片_20250329220729.png', '/upload/goods/202601/aa592a1385ec47288f092916a4b365d3.png', 'image', '2026-01-07 17:38:25', '2026-01-07 17:38:25', 0);
INSERT INTO `auction_file` VALUES (6, '46颜炳权《鸡汤来喽》.MP4', '/upload/goods/202601/09b8f2f8766e43ebb86cc4c76e5f3103.MP4', 'video', '2026-01-07 17:43:10', '2026-01-07 17:43:09', 0);
INSERT INTO `auction_file` VALUES (7, '[tools.miku.ac][图片转像素画]-1764923622832.png', '/upload/goods/202601/4c6b461f93a54812b4428ca6e1615dd4.png', 'image', '2026-01-07 17:47:53', '2026-01-07 17:47:52', 0);
INSERT INTO `auction_file` VALUES (8, '3149315cea3d09be2a697f074dcbf9fd.jpeg', '/upload/goods/202601/fc5cbf27abb242ecaa6567869a2178cb.jpeg', 'image', '2026-01-07 17:50:18', '2026-01-07 17:50:18', 0);
INSERT INTO `auction_file` VALUES (9, 'f684d9eb209cd13e3aa85be069f4eaa6.jpeg', '/upload/goods/202601/3a04cc14df3f47f2ace620689e3b7fbd.jpeg', 'image', '2026-01-21 09:49:57', '2026-01-21 09:50:31', 0);
INSERT INTO `auction_file` VALUES (10, 'f684d9eb209cd13e3aa85be069f4eaa6.jpeg', '/upload/goods/202601/de952b23e1ea433faeebb27434160ffd.jpeg', 'image', '2026-01-28 14:46:54', '2026-01-28 14:46:53', 0);
INSERT INTO `auction_file` VALUES (11, 'f684d9eb209cd13e3aa85be069f4eaa6.jpeg', '/upload/goods/202601/1b4e797c4ce0466aa7d507675fe3cb8f.jpeg', 'image', '2026-01-28 14:47:02', '2026-01-28 14:47:01', 0);
INSERT INTO `auction_file` VALUES (12, 'gyCD5xD7z_PK5g9M.mp4', '/upload/goods/202601/7e47dbacf66f4c658d38f8815d5640e1.mp4', 'video', '2026-01-28 14:52:02', '2026-01-28 14:52:01', 0);
INSERT INTO `auction_file` VALUES (13, 'photo_2025-05-08_15-45-24.jpg', '/upload/goods/202601/7a972597cb014fb0958887dd3e1efb55.jpg', 'image', '2026-01-28 14:52:35', '2026-01-28 14:52:35', 0);
INSERT INTO `auction_file` VALUES (14, 'photo_2025-07-10_22-12-38.jpg', '/upload/goods/202601/cec5e469a0e64e80b415caa5d2780a7f.jpg', 'image', '2026-01-28 14:52:47', '2026-01-28 14:52:47', 0);
INSERT INTO `auction_file` VALUES (15, 'QQ图片20230426170644.jpg', '/upload/profile/202601/ebb015e215514cd182d4593b49b39b7b.jpg', 'image', '2026-01-28 15:14:47', '2026-01-28 15:14:47', 0);
INSERT INTO `auction_file` VALUES (16, 'QQ图片20230426170644.jpg', '/upload/profile/202601/4e8c7dbaea7f4501bba3ce12ef8d175b.jpg', 'image', '2026-01-28 15:23:07', '2026-01-28 15:23:07', 0);
INSERT INTO `auction_file` VALUES (17, 'QQ图片20230426170644.jpg', '/upload/profile/202601/b0e4aaa51fbd4171a2906512d142a99b.jpg', 'image', '2026-01-28 15:23:32', '2026-01-28 15:23:31', 0);
INSERT INTO `auction_file` VALUES (18, 'QQ图片20230426170644.jpg', '/upload/profile/202601/5a5144261ee0490796d6d1f3c657d81c.jpg', 'image', '2026-01-28 15:24:57', '2026-01-28 15:24:56', 0);
INSERT INTO `auction_file` VALUES (19, 'avatar.png', '/upload/profile/202601/b86cbe81ff4e41c082f81e353b9e2c10.png', 'image', '2026-01-28 15:30:46', '2026-01-28 15:30:46', 0);
INSERT INTO `auction_file` VALUES (20, 'avatar.png', '/upload/profile/202601/a94bf287f4874381b304de80eef3d031.png', 'image', '2026-01-28 15:31:06', '2026-01-28 15:31:05', 0);
INSERT INTO `auction_file` VALUES (21, 'avatar.png', '/upload/profile/202601/279c9d0ea041478a808acd75114811c6.png', 'image', '2026-01-28 15:33:45', '2026-01-28 15:33:45', 0);
INSERT INTO `auction_file` VALUES (22, 'avatar.png', '/upload/profile/202601/e7312c80f5f84c919a7b2465034a8087.png', 'image', '2026-01-28 15:34:29', '2026-01-28 15:34:28', 0);
INSERT INTO `auction_file` VALUES (23, 'avatar.png', '/upload/profile/202601/7b9110809a3649eb926e0abfafb3da06.png', 'image', '2026-01-28 15:41:39', '2026-01-28 15:41:38', 0);
INSERT INTO `auction_file` VALUES (24, 'avatar.png', '/upload/profile/202601/dd39cd3c01d94c4da9ad207336288da9.png', 'image', '2026-01-28 15:49:17', '2026-01-28 15:49:16', 0);
INSERT INTO `auction_file` VALUES (25, 'avatar.png', '/upload/profile/202601/62bc724702d04a429fb063803a0a07da.png', 'image', '2026-01-28 15:52:07', '2026-01-28 15:52:07', 0);
INSERT INTO `auction_file` VALUES (26, 'avatar.png', '/upload/profile/202601/5eec80614ad846d38ae6e9ac0db2fe25.png', 'image', '2026-01-28 15:52:28', '2026-01-28 15:52:28', 0);
INSERT INTO `auction_file` VALUES (27, 'avatar.png', '/upload/profile/202601/cb9c560c229a4e6698348d669549b071.png', 'image', '2026-01-28 15:55:22', '2026-01-28 15:55:22', 0);
INSERT INTO `auction_file` VALUES (28, 'avatar.png', '/upload/profile/202601/e6faa7c436164d3ab0701c4f79e385fc.png', 'image', '2026-01-28 15:57:08', '2026-01-28 15:57:07', 0);
INSERT INTO `auction_file` VALUES (29, 'avatar.png', '/upload/profile/202602/cbb7bda35553405db352d52322f92be4.png', 'image', '2026-02-08 15:24:45', '2026-02-08 15:24:45', 0);
INSERT INTO `auction_file` VALUES (30, 'avatar.png', '/upload/profile/202602/c70fc319f64544419c52c340e35dce74.png', 'image', '2026-02-24 10:19:30', '2026-02-24 10:19:29', 0);
INSERT INTO `auction_file` VALUES (31, '微信图片_20250329220729.png', '/upload/avatar/202602/1a582b8261c841739f977f329eb75ab0.png', 'image', '2026-02-25 14:06:46', '2026-02-25 14:06:46', 0);
INSERT INTO `auction_file` VALUES (32, 'QQ图片20230426170644.jpg', '/upload/avatar/202602/23cf3cf96a924d3e80db8b38fe24c047.jpg', 'image', '2026-02-25 14:22:58', '2026-02-25 14:22:58', 0);
INSERT INTO `auction_file` VALUES (33, '微信图片_20250329220729.png', '/upload/banner/202602/d3627db1824f407cbd03b8cb07c68253.png', 'image', '2026-02-26 15:52:50', '2026-02-26 15:52:50', 0);
INSERT INTO `auction_file` VALUES (34, 'QQ图片20230426170644.jpg', '/upload/banner/202602/4ee15e77bef74dfdaa3eae6b6e0be01d.jpg', 'image', '2026-02-26 15:53:02', '2026-02-26 15:53:02', 0);
INSERT INTO `auction_file` VALUES (35, 'QQ图片20230426170644.jpg', '/upload/goods/202603/ab9b76c4bfaf4f8e94b6666eae40517c.jpg', 'image', '2026-03-02 07:14:31', '2026-03-02 07:14:30', 0);
INSERT INTO `auction_file` VALUES (36, 'QQ图片20230426170644.jpg', '/upload/goods/202603/a86dcd5648254f489f6ca188dd070282.jpg', 'image', '2026-03-02 07:14:46', '2026-03-02 07:14:46', 0);
INSERT INTO `auction_file` VALUES (37, '微信图片_20250329220729.png', '/upload/goods/202603/97a82698bf3848ec9139f2d6a86edce4.png', 'image', '2026-03-02 07:15:38', '2026-03-02 07:15:38', 0);
INSERT INTO `auction_file` VALUES (38, '10月31日 (1).mp4', '/upload/goods/202603/8e1f50c824c54c34b125bc9be8fd5f45.mp4', 'video', '2026-03-02 07:20:02', '2026-03-02 07:20:02', 0);
INSERT INTO `auction_file` VALUES (39, 'QQ图片20230426170644.jpg', '/upload/goods/202603/11089026f078448485a66419ff0a33f4.jpg', 'image', '2026-03-02 07:20:07', '2026-03-02 07:20:07', 0);
INSERT INTO `auction_file` VALUES (40, 'Zephyrus Duo 15 x ZЯØFØRM_3840x2160.jpg', '/upload/avatar/202603/6ba154027f8e46db9381ff388de7dd6d.jpg', 'image', '2026-03-03 09:43:58', '2026-03-03 09:43:58', 0);
INSERT INTO `auction_file` VALUES (41, 'QQ图片20230426170644.jpg', '/upload/avatar/202603/f3a2bfab4dbc466c8fb505a86ea9dfc8.jpg', 'image', '2026-03-04 14:35:52', '2026-03-04 14:35:52', 0);
INSERT INTO `auction_file` VALUES (42, '46颜炳权《鸡汤来喽》.MP4', '/upload/goods/202603/c67ad0959ca84400a50aa783e7553425.MP4', 'video', '2026-03-10 16:26:32', '2026-03-10 16:26:31', 0);
INSERT INTO `auction_file` VALUES (43, '微信图片_20250329220729.png', '/upload/goods/202603/2f20362123464af69a96f832d45698e0.png', 'image', '2026-03-10 16:31:47', '2026-03-10 16:31:46', 0);

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
  `file_ids` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '关联文件ID列表（多个文件ID用逗号分隔，如：1,2,3）',
  `base_price` decimal(20, 2) NOT NULL COMMENT '起拍价',
  `current_highest_price` decimal(20, 2) NULL DEFAULT NULL COMMENT '当前最高出价（实时更新，用于快速查询）',
  `add_price` decimal(20, 2) NOT NULL COMMENT '最小加价幅度',
  `reserve_price` decimal(20, 2) NULL DEFAULT NULL COMMENT '保留价（仅卖方可见）',
  `start_time` datetime NOT NULL COMMENT '拍卖开始时间',
  `end_time` datetime NOT NULL COMMENT '拍卖结束时间',
  `audit_status` tinyint NOT NULL DEFAULT 0 COMMENT '审核状态：0=待审核 1=审核通过 2=审核驳回 3=已下架',
  `audit_remark` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '审核驳回原因',
  `goods_status` tinyint NOT NULL DEFAULT 0 COMMENT '商品状态：0=未开始 1=竞拍中 2=已成交 3=已流拍',
  `shelf_status` tinyint(1) NOT NULL DEFAULT 0 COMMENT '上架状态：0=下架 1=上架',
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
) ENGINE = InnoDB AUTO_INCREMENT = 22 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '拍卖商品表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of auction_goods
-- ----------------------------
INSERT INTO `auction_goods` VALUES (1, '青眼白龙', '1,2', 1, '青眼白龙', NULL, 200.00, NULL, 20.00, 1000.00, '2025-12-31 16:00:00', '2026-01-30 16:00:00', 3, '', 3, 0, '2026-01-07 16:42:23', '2026-02-08 23:49:52', 0, 0, 0, 0);
INSERT INTO `auction_goods` VALUES (2, '夜明珠', '1,2', 1, '古玩类', NULL, 200.00, NULL, 20.00, 600.00, '2025-12-31 16:00:00', '2026-01-30 16:00:00', 3, '', 3, 0, '2026-01-07 17:03:21', '2026-02-08 23:49:52', 0, 0, 0, 0);
INSERT INTO `auction_goods` VALUES (3, '测试图片', '1,2', 1, '111', NULL, 100.00, NULL, 20.00, 500.00, '2025-12-31 16:00:00', '2026-01-30 16:00:00', 3, '', 3, 0, '2026-01-07 17:07:06', '2026-02-08 23:49:52', 0, 0, 0, 0);
INSERT INTO `auction_goods` VALUES (4, '测试视频', '2', 1, '视频', NULL, 200.00, NULL, 30.00, 500.00, '2025-12-31 16:00:00', '2026-01-30 16:00:00', 3, '', 3, 0, '2026-01-07 17:08:26', '2026-02-08 23:49:52', 0, 0, 0, 0);
INSERT INTO `auction_goods` VALUES (5, '测试文件', '1,2', 1, '文件', NULL, 200.00, NULL, 20.00, 1000.00, '2025-12-31 16:00:00', '2026-01-30 16:00:00', 3, '', 3, 0, '2026-01-07 17:15:11', '2026-02-08 23:49:52', 0, 0, 0, 0);
INSERT INTO `auction_goods` VALUES (6, '测试添加多个图片', '1,2', 1, '多个图片', NULL, 200.00, NULL, 50.00, 2000.00, '2025-12-31 16:00:00', '2026-01-30 16:00:00', 3, '', 3, 0, '2026-01-07 17:18:05', '2026-02-08 23:49:52', 0, 0, 0, 0);
INSERT INTO `auction_goods` VALUES (7, '测试图片', '1,2', 1, 'dcz', NULL, 5411.00, NULL, 85.00, 8745.00, '2025-12-31 16:00:00', '2026-01-30 16:00:00', 3, '', 3, 0, '2026-01-07 17:23:19', '2026-02-08 23:49:52', 0, 0, 0, 0);
INSERT INTO `auction_goods` VALUES (8, '测试文件上传', '1,2', 1, '图片', NULL, 0.00, NULL, 0.00, 0.00, '2025-12-31 16:00:00', '2026-01-30 16:00:00', 3, '', 3, 0, '2026-01-07 17:38:43', '2026-02-08 23:49:52', 0, 0, 0, 0);
INSERT INTO `auction_goods` VALUES (9, '测试视频', '2,1', 1, '123', NULL, 0.00, NULL, 0.00, 0.00, '2025-12-31 16:00:00', '2026-01-30 16:00:00', 3, '', 3, 0, '2026-01-07 17:43:24', '2026-02-08 23:49:52', 0, 0, 0, 0);
INSERT INTO `auction_goods` VALUES (10, '测试添加视频', '2,1', 1, '视频', NULL, 0.00, NULL, 0.00, 0.00, '2025-12-31 16:00:00', '2026-01-30 16:00:00', 3, '', 3, 0, '2026-01-07 17:48:00', '2026-02-08 23:49:52', 0, 0, 0, 0);
INSERT INTO `auction_goods` VALUES (11, '祝友志的苦茶', '1', 1, '商周的新鲜货', NULL, 10000.00, NULL, 500.00, 9000.00, '2026-01-05 16:00:00', '2026-01-27 16:00:00', 3, '', 3, 0, '2026-01-07 17:51:22', '2026-02-08 23:49:52', 0, 0, 0, 0);
INSERT INTO `auction_goods` VALUES (12, '测试添加多个分类', '11,111,112,113,114,115', 1, '分类', '9', 100000000.00, 100200000.00, 200000.00, 104000000.00, '2025-12-31 16:00:00', '2026-03-02 23:02:19', 3, '', 2, 1, '2026-01-21 09:50:31', '2026-03-03 09:01:27', 0, 0, 0, 2);
INSERT INTO `auction_goods` VALUES (13, '测试更新逻辑后的商品添加', '9,91,911,912,913,914,915,92,921,922,923,924,93,931,932,94,941,942,943,95,951,952', 1, '测试更新逻辑后的商品添加', '11', 2000.00, NULL, 100.00, 3000.00, '2025-12-31 16:00:00', '2026-01-30 16:00:00', 1, '', 3, 0, '2026-01-28 14:47:20', '2026-03-02 06:44:10', 0, 0, 0, 0);
INSERT INTO `auction_goods` VALUES (14, '第二次测试更新商品添加逻辑', '9,91,911,912,913,914,915,92,921,922,923,924,93,931,932,94,941,942,943,95,951,952', 1, '第二次测试更新商品添加逻辑', '12', 2000.00, 2000.00, 100.00, 3000.00, '2025-12-31 16:00:00', '2026-02-28 08:00:00', 3, '', 2, 0, '2026-01-28 14:52:14', '2026-03-02 06:39:04', 0, 0, 0, 1);
INSERT INTO `auction_goods` VALUES (15, '测试竞拍订单', '1,11,111,112,113,114,115,12,121,122,123,124,13,131,132,133,134,14,141,142,143,144,145,15,151,152,153', 1, '测试', '[]', 7891.00, NULL, 9178.00, 0.00, '2026-03-01 22:43:46', '2026-03-01 22:45:47', 1, '', 3, 0, '2026-03-02 06:42:41', '2026-03-02 06:45:22', 1, 0, 0, 0);
INSERT INTO `auction_goods` VALUES (16, '第二次测试竞拍订单', '1,11,111,112,113,114,115,12,121,122,123,124,13,131,132,133,134,14,141,142,143,144,145,15,151,152,153', 1, '测试', '[]', 7891.00, NULL, 9178.00, 0.00, '2026-03-01 22:44:40', '2026-03-01 22:46:42', 1, '', 3, 0, '2026-03-02 06:44:50', '2026-03-02 06:45:15', 1, 0, 0, 0);
INSERT INTO `auction_goods` VALUES (17, '测试竞拍订单', '1,11,111,112,113,114,115,12,121,122,123,124,13,131,132,133,134,14,141,142,143,144,145,15,151,152,153', 1, '测试', '35', 7891.00, NULL, 9178.00, 0.00, '2026-03-01 22:45:42', '2026-03-02 16:00:00', 3, '', 3, 1, '2026-03-02 06:45:50', '2026-03-02 17:18:58', 0, 0, 0, 0);
INSERT INTO `auction_goods` VALUES (18, '测试添加视频以及图片', '1,11,111,112,113,114,115,12,121,122,123,124,13,131,132,133,134,14,141,142,143,144,145,15,151,152,153', 1, 'd\'s', '36', 1.00, 1.00, 2.00, 0.00, '2026-03-01 23:15:09', '2026-03-06 16:00:00', 3, '', 2, 1, '2026-03-02 07:15:13', '2026-03-06 16:53:30', 0, 0, 0, 1);
INSERT INTO `auction_goods` VALUES (19, '测试添加视频图片', '21,211,212,213,214,215', 1, 'd\'c\'x\'s', '38,39', 1.00, NULL, 3.00, 0.00, '2026-02-28 16:00:00', '2026-03-31 08:00:00', 3, '', 1, 0, '2026-03-02 07:20:31', '2026-03-05 16:39:49', 0, 0, 0, 0);
INSERT INTO `auction_goods` VALUES (20, '测试首页展示热门商品', '1,11,111,112,113,114,115,12,121,122,123,124,13,131,132,133,134,14,141,142,143,144,145,15,151,152,153', 6, '测试首页展示热门商品', '43,42', 200.00, NULL, 20.00, 500.00, '2026-02-28 16:00:00', '2026-11-30 16:00:00', 1, '', 1, 1, '2026-03-10 16:32:06', '2026-03-10 17:47:22', 0, 6, 0, 0);
INSERT INTO `auction_goods` VALUES (21, '测试无文件商品', '1,11,111,112,113,114,115,12,121,122,123,124,13,131,132,133,134,14,141,142,143,144,145,15,151,152,153', 6, '测试无文件商品', '[]', 1000.00, NULL, 50.00, 1500.00, '2026-02-28 16:00:00', '2026-12-30 16:00:00', 1, '', 1, 1, '2026-03-10 17:45:50', '2026-03-10 17:45:53', 0, 1, 0, 0);

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
) ENGINE = InnoDB AUTO_INCREMENT = 21 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统菜单表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of auction_menu
-- ----------------------------
INSERT INTO `auction_menu` VALUES (1, '首页', 0, '/home', 'el-icon-s-home', 1, 'home:view', 1, 1, '用户端首页', '2025-12-31 14:40:18', '2025-12-31 14:40:18', 0);
INSERT INTO `auction_menu` VALUES (2, '拍卖商品', 0, '/goods', 'el-icon-goods', 1, 'goods:view', 2, 1, '拍卖商品列表', '2025-12-31 14:40:18', '2025-12-31 14:40:18', 0);
INSERT INTO `auction_menu` VALUES (3, '竞拍公告', 0, '/notice', 'el-icon-bell', 1, 'notice:view', 3, 1, '竞拍公告列表', '2025-12-31 14:40:18', '2025-12-31 14:40:18', 0);
INSERT INTO `auction_menu` VALUES (4, '个人中心', 0, '/profile', 'el-icon-user', 1, 'profile:view', 4, 1, '个人中心', '2025-12-31 14:40:18', '2025-12-31 14:40:18', 0);
INSERT INTO `auction_menu` VALUES (5, '订单管理', 4, '/profile/order', NULL, 1, 'order:view', 1, 1, '订单管理', '2025-12-31 14:40:18', '2025-12-31 14:40:18', 0);
INSERT INTO `auction_menu` VALUES (6, '保证金管理', 4, '/profile/deposit', NULL, 1, 'deposit:view', 2, 1, '保证金管理', '2025-12-31 14:40:18', '2025-12-31 14:40:18', 0);
INSERT INTO `auction_menu` VALUES (7, '消息中心', 4, '/profile/message', NULL, 1, 'message:view', 3, 1, '消息中心', '2025-12-31 14:40:18', '2026-03-03 09:40:03', 1);
INSERT INTO `auction_menu` VALUES (8, '留言板', 0, '/message-board', 'el-icon-chat-line-square', 1, 'message:board:view', 5, 1, '留言板', '2025-12-31 14:40:18', '2025-12-31 14:40:18', 0);
INSERT INTO `auction_menu` VALUES (10, '个人管理中心', 0, '/admin/profile', 'el-icon-user', 1, 'admin:profile:view', 1, 1, '管理员个人中心', '2025-12-31 14:40:18', '2025-12-31 14:40:18', 0);
INSERT INTO `auction_menu` VALUES (11, '用户账号管理', 0, '/admin/user', 'el-icon-user-solid', 1, 'admin:user:view', 2, 1, '用户账号管理', '2025-12-31 14:40:18', '2025-12-31 14:40:18', 0);
INSERT INTO `auction_menu` VALUES (12, '拍卖商品类目管理', 0, '/admin/category', 'el-icon-folder', 1, 'admin:category:view', 3, 1, '商品类目管理', '2025-12-31 14:40:18', '2025-12-31 14:40:18', 0);
INSERT INTO `auction_menu` VALUES (13, '拍卖商品管理', 0, '/admin/goods', 'el-icon-goods', 1, 'admin:goods:view', 4, 1, '拍卖商品管理', '2025-12-31 14:40:18', '2025-12-31 14:40:18', 0);
INSERT INTO `auction_menu` VALUES (14, '历史竞拍管理', 0, '/admin/history', 'el-icon-time', 1, 'admin:history:view', 5, 1, '历史竞拍管理', '2025-12-31 14:40:18', '2025-12-31 14:40:18', 0);
INSERT INTO `auction_menu` VALUES (15, '竞拍订单', 0, '/admin/order', 'el-icon-s-order', 1, 'admin:order:view', 6, 1, '竞拍订单管理', '2025-12-31 14:40:18', '2026-03-03 10:28:00', 0);
INSERT INTO `auction_menu` VALUES (16, '留言板管理', 0, '/admin/message', 'el-icon-chat-line-square', 1, 'admin:message:view', 7, 1, '留言板管理', '2025-12-31 14:40:18', '2025-12-31 14:40:18', 0);
INSERT INTO `auction_menu` VALUES (17, '系统设置', 0, '/admin/settings', 'el-icon-setting', 1, 'admin:settings:view', 8, 1, '系统设置', '2025-12-31 14:40:18', '2025-12-31 14:40:18', 0);
INSERT INTO `auction_menu` VALUES (18, '轮播图管理', 17, '/admin/settings/banner', NULL, 1, 'admin:banner:view', 1, 1, '首页轮播图管理', '2025-12-31 14:40:18', '2025-12-31 14:40:18', 0);
INSERT INTO `auction_menu` VALUES (19, '竞拍公告管理', 17, '/admin/settings/notice', NULL, 1, 'admin:notice:view', 2, 1, '竞拍公告管理', '2025-12-31 14:40:18', '2025-12-31 14:40:18', 0);
INSERT INTO `auction_menu` VALUES (20, '保证金管理', 0, '/admin/deposit', 'el-icon-money', 1, 'admin:deposit:view', 6, 1, '财务-保证金管理', '2026-02-26 16:53:11', '2026-02-26 16:53:11', 0);
INSERT INTO `auction_menu` VALUES (21, '消息中心', 0, '/message', 'el-icon-chat-dot-round', 1, 'message:center:view', 6, 1, '商品咨询客服（买方、卖方、客服）', '2026-03-03 09:40:03', '2026-03-03 09:40:03', 0);

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
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '留言板表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of auction_message
-- ----------------------------
INSERT INTO `auction_message` VALUES (1, 3, NULL, '买方留言', NULL, NULL, 0, '2026-03-02 17:38:29', 0);
INSERT INTO `auction_message` VALUES (2, 4, NULL, '卖方留言', NULL, NULL, 0, '2026-03-02 17:38:50', 0);

-- ----------------------------
-- Table structure for auction_message_center
-- ----------------------------
DROP TABLE IF EXISTS `auction_message_center`;
CREATE TABLE `auction_message_center`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `session_id` bigint NOT NULL COMMENT '会话ID（auction_message_session.id）',
  `sender_id` bigint NOT NULL COMMENT '发送者ID（auction_user.id）',
  `receiver_id` bigint NOT NULL COMMENT '接收者ID（auction_user.id）',
  `content_type` tinyint NOT NULL DEFAULT 1 COMMENT '内容类型：1=文本 2=订单信息 3=附件（图片/视频等）',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '文本内容或订单ID（content_type=2时存order_id）',
  `file_id` bigint NULL DEFAULT NULL COMMENT '附件文件ID（content_type=3时，关联auction_file.id）',
  `is_read` tinyint NOT NULL DEFAULT 0 COMMENT '是否已读：0=未读 1=已读',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=未删除 1=已删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_session_id`(`session_id` ASC) USING BTREE,
  INDEX `idx_sender_id`(`sender_id` ASC) USING BTREE,
  INDEX `idx_receiver_id`(`receiver_id` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '消息中心消息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of auction_message_center
-- ----------------------------

-- ----------------------------
-- Table structure for auction_message_session
-- ----------------------------
DROP TABLE IF EXISTS `auction_message_session`;
CREATE TABLE `auction_message_session`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '会话ID',
  `session_type` tinyint NOT NULL DEFAULT 1 COMMENT '1=客服咨询 2=管理沟通',
  `goods_id` bigint NULL DEFAULT NULL COMMENT '商品ID(type=1必填,type=2为NULL)',
  `user_id` bigint NOT NULL COMMENT '发起咨询的用户ID（买方/卖方，auction_user.id）',
  `service_id` bigint NULL DEFAULT NULL COMMENT '分配的客服ID（auction_user.id，首次咨询时随机分配在线客服）',
  `session_status` tinyint NOT NULL DEFAULT 0 COMMENT '状态：0=进行中 1=已关闭（商品成交/流拍/下架后关闭）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=未删除 1=已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_goods_user`(`goods_id` ASC, `user_id` ASC) USING BTREE COMMENT '同一用户对同一商品唯一会话',
  INDEX `idx_goods_id`(`goods_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_service_id`(`service_id` ASC) USING BTREE,
  INDEX `idx_session_status`(`session_status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '消息中心会话表（以商品为核心，商品-用户唯一）' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of auction_message_session
-- ----------------------------
INSERT INTO `auction_message_session` VALUES (1, 1, 18, 3, NULL, 0, '2026-03-03 09:45:07', '2026-03-03 09:45:07', 0);

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
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '竞拍公告表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of auction_notice
-- ----------------------------
INSERT INTO `auction_notice` VALUES (1, '测试发布公告1', '测试发布公告1', 0, 1, '2026-03-04 14:36:54', '2026-02-26 15:29:45', '2026-02-26 15:29:45', 0);

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
  `deal_price` decimal(20, 2) NOT NULL COMMENT '成交金额',
  `deposit_amount` decimal(20, 2) NOT NULL COMMENT '冻结保证金金额',
  `remain_amount` decimal(20, 2) NOT NULL COMMENT '尾款金额（deal_price - deposit_amount）',
  `pay_deadline` datetime NOT NULL COMMENT '尾款付款截止时间（成交后24小时）',
  `order_status` tinyint NOT NULL DEFAULT 0 COMMENT '订单状态：0=待付款 1=待发货 2=待收货 3=已完成 4=已悔拍 5=已退款',
  `confirm_deal_at` datetime NULL DEFAULT NULL COMMENT '落槌确认时间（拍卖师/管理员确认成交）',
  `confirm_user_id` bigint NULL DEFAULT NULL COMMENT '落槌确认人ID（关联auction_user.id）',
  `confirmation_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '成交确认书编号',
  `express_company` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '快递公司',
  `express_no` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '快递单号',
  `ship_time` datetime NULL DEFAULT NULL COMMENT '发货时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间（竞拍成交后系统自动生成）',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=未删除 1=已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `idx_order_no`(`order_no` ASC) USING BTREE,
  INDEX `idx_buyer_id`(`buyer_id` ASC) USING BTREE,
  INDEX `idx_seller_id`(`seller_id` ASC) USING BTREE,
  INDEX `idx_order_status`(`order_status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '竞拍订单表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of auction_order
-- ----------------------------
INSERT INTO `auction_order` VALUES (1, '2026030250659300', 14, 3, 1, 1, 2000.00, 0.00, 2000.00, '2026-03-03 06:39:04', 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-03-02 06:39:04', '2026-03-02 06:39:04', 0);
INSERT INTO `auction_order` VALUES (2, '2026030327970801', 12, 8, 1, 3, 100200000.00, 0.00, 100200000.00, '2026-03-04 09:01:27', 3, '2026-03-05 17:05:21', 5, 'CJQS2026030327970801', '哪都通', '114514', '2026-03-05 17:07:54', '2026-03-03 09:01:27', '2026-03-05 17:13:20', 0);
INSERT INTO `auction_order` VALUES (3, '2026030670724553', 18, 3, 1, 4, 1.00, 0.00, 1.00, '2026-03-07 16:53:30', 0, '2026-03-06 16:55:56', 2, 'CJQS2026030670724553', NULL, NULL, NULL, '2026-03-06 16:53:30', '2026-03-06 16:55:56', 0);

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
) ENGINE = InnoDB AUTO_INCREMENT = 101 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统权限表' ROW_FORMAT = DYNAMIC;

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
INSERT INTO `auction_permission` VALUES (17, '个人中心查看', 'profile:view', 1, '查看个人中心', 1, '2025-12-31 14:40:32', '2025-12-31 14:40:32', 0);
INSERT INTO `auction_permission` VALUES (18, '留言板发布', 'message:board:add', 2, '发布留言板留言', 1, '2025-12-31 14:40:32', '2025-12-31 14:40:32', 0);
INSERT INTO `auction_permission` VALUES (19, '留言板查看', 'message:board:view', 1, '查看留言板所有留言', 1, '2025-12-31 14:40:32', '2025-12-31 14:40:32', 0);
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
INSERT INTO `auction_permission` VALUES (94, '管理员个人中心查看', 'admin:profile:view', 1, '查看管理员个人中心', 1, '2025-12-31 14:40:32', '2025-12-31 14:40:32', 0);
INSERT INTO `auction_permission` VALUES (95, '系统设置查看', 'admin:settings:view', 1, '查看系统设置', 1, '2025-12-31 14:40:32', '2025-12-31 14:40:32', 0);
INSERT INTO `auction_permission` VALUES (96, '留言板编辑', 'message:board:edit', 2, '编辑自己的留言', 1, '2025-12-31 14:40:32', '2025-12-31 14:40:32', 0);
INSERT INTO `auction_permission` VALUES (97, '后台保证金管理', 'admin:deposit:view', 1, '查看和管理用户保证金', 1, '2026-02-26 16:53:11', '2026-02-26 16:53:11', 0);
INSERT INTO `auction_permission` VALUES (98, '历史竞拍查看', 'history:view', 1, '普通用户查看自己的历史竞拍', 1, '2026-03-02 07:47:38', '2026-03-02 07:47:38', 0);
INSERT INTO `auction_permission` VALUES (99, '消息中心查看', 'message:center:view', 1, '查看消息中心（买方、卖方、客服）', 1, '2026-03-03 09:40:03', '2026-03-03 09:40:03', 0);
INSERT INTO `auction_permission` VALUES (100, '消息中心发送', 'message:center:send', 2, '发送消息', 1, '2026-03-03 09:40:03', '2026-03-03 09:40:03', 0);
INSERT INTO `auction_permission` VALUES (101, '消息中心监管', 'message:center:admin', 1, '超级管理员查看所有消息', 1, '2026-03-03 09:40:03', '2026-03-03 09:40:03', 0);

-- ----------------------------
-- Table structure for auction_record
-- ----------------------------
DROP TABLE IF EXISTS `auction_record`;
CREATE TABLE `auction_record`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '竞拍记录ID，自增',
  `goods_id` bigint NOT NULL COMMENT '商品ID（关联auction_goods.id）',
  `buyer_id` bigint NOT NULL COMMENT '买方用户ID（关联auction_user.id）',
  `bid_price` decimal(20, 2) NOT NULL COMMENT '出价金额',
  `is_agent` tinyint NOT NULL DEFAULT 0 COMMENT '是否代理出价：0=手动出价 1=代理出价',
  `agent_max_price` decimal(20, 2) NULL DEFAULT NULL COMMENT '代理出价最高心理价位（is_agent=1时必填）',
  `bid_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '出价时间',
  `is_highest` tinyint NOT NULL DEFAULT 0 COMMENT '是否当前最高价：0=否 1=是（实时更新）',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=未删除 1=已删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_goods_id`(`goods_id` ASC) USING BTREE,
  INDEX `idx_buyer_id`(`buyer_id` ASC) USING BTREE,
  INDEX `idx_is_highest`(`is_highest` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '竞拍记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of auction_record
-- ----------------------------
INSERT INTO `auction_record` VALUES (1, 14, 3, 2000.00, 0, NULL, '2026-02-09 00:09:24', 1, 0);
INSERT INTO `auction_record` VALUES (2, 12, 3, 100000000.00, 0, NULL, '2026-03-02 07:06:36', 0, 0);
INSERT INTO `auction_record` VALUES (3, 12, 8, 100200000.00, 0, NULL, '2026-03-02 07:48:12', 1, 0);
INSERT INTO `auction_record` VALUES (4, 18, 3, 1.00, 0, NULL, '2026-03-03 09:43:20', 1, 0);

-- ----------------------------
-- Table structure for auction_role_permission
-- ----------------------------
DROP TABLE IF EXISTS `auction_role_permission`;
CREATE TABLE `auction_role_permission`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID，自增',
  `role_type` tinyint NOT NULL COMMENT '角色类型：1=普通用户 2=（历史卖方，已合并至1） 3=管理员 4=超级管理员 5=拍卖师 6=客服 7=财务 8=运营',
  `permission_id` bigint NOT NULL COMMENT '权限ID（关联auction_permission.id）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `idx_role_permission`(`role_type` ASC, `permission_id` ASC) USING BTREE,
  INDEX `idx_role_type`(`role_type` ASC) USING BTREE,
  INDEX `idx_permission_id`(`permission_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 227 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '角色权限关联表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of auction_role_permission
-- ----------------------------
INSERT INTO `auction_role_permission` VALUES (1, 1, 1, '2026-01-21 16:14:50');
INSERT INTO `auction_role_permission` VALUES (2, 1, 2, '2026-01-21 16:14:50');
INSERT INTO `auction_role_permission` VALUES (3, 1, 3, '2026-01-21 16:14:50');
INSERT INTO `auction_role_permission` VALUES (4, 1, 4, '2026-01-21 16:14:50');
INSERT INTO `auction_role_permission` VALUES (5, 1, 5, '2026-01-21 16:14:50');
INSERT INTO `auction_role_permission` VALUES (6, 1, 9, '2026-01-21 16:14:50');
INSERT INTO `auction_role_permission` VALUES (7, 1, 10, '2026-01-21 16:14:50');
INSERT INTO `auction_role_permission` VALUES (8, 1, 11, '2026-01-21 16:14:50');
INSERT INTO `auction_role_permission` VALUES (9, 1, 12, '2026-01-21 16:14:50');
INSERT INTO `auction_role_permission` VALUES (10, 1, 13, '2026-01-21 16:14:50');
INSERT INTO `auction_role_permission` VALUES (11, 1, 14, '2026-01-21 16:14:50');
INSERT INTO `auction_role_permission` VALUES (12, 1, 15, '2026-01-21 16:14:50');
INSERT INTO `auction_role_permission` VALUES (13, 1, 16, '2026-01-21 16:14:50');
INSERT INTO `auction_role_permission` VALUES (14, 2, 1, '2026-01-21 16:14:50');
INSERT INTO `auction_role_permission` VALUES (15, 2, 2, '2026-01-21 16:14:50');
INSERT INTO `auction_role_permission` VALUES (16, 2, 3, '2026-01-21 16:14:50');
INSERT INTO `auction_role_permission` VALUES (17, 2, 4, '2026-01-21 16:14:50');
INSERT INTO `auction_role_permission` VALUES (18, 2, 5, '2026-01-21 16:14:50');
INSERT INTO `auction_role_permission` VALUES (19, 2, 6, '2026-01-21 16:14:50');
INSERT INTO `auction_role_permission` VALUES (20, 2, 7, '2026-01-21 16:14:50');
INSERT INTO `auction_role_permission` VALUES (21, 2, 8, '2026-01-21 16:14:50');
INSERT INTO `auction_role_permission` VALUES (22, 2, 9, '2026-01-21 16:14:50');
INSERT INTO `auction_role_permission` VALUES (23, 2, 10, '2026-01-21 16:14:50');
INSERT INTO `auction_role_permission` VALUES (24, 2, 11, '2026-01-21 16:14:50');
INSERT INTO `auction_role_permission` VALUES (25, 2, 12, '2026-01-21 16:14:50');
INSERT INTO `auction_role_permission` VALUES (26, 2, 13, '2026-01-21 16:14:50');
INSERT INTO `auction_role_permission` VALUES (27, 2, 14, '2026-01-21 16:14:50');
INSERT INTO `auction_role_permission` VALUES (28, 2, 15, '2026-01-21 16:14:50');
INSERT INTO `auction_role_permission` VALUES (29, 2, 16, '2026-01-21 16:14:50');
INSERT INTO `auction_role_permission` VALUES (30, 3, 20, '2026-01-21 16:14:50');
INSERT INTO `auction_role_permission` VALUES (31, 3, 21, '2026-01-21 16:14:50');
INSERT INTO `auction_role_permission` VALUES (32, 3, 22, '2026-01-21 16:14:50');
INSERT INTO `auction_role_permission` VALUES (33, 3, 23, '2026-01-21 16:14:50');
INSERT INTO `auction_role_permission` VALUES (34, 3, 24, '2026-01-21 16:14:50');
INSERT INTO `auction_role_permission` VALUES (35, 3, 25, '2026-01-21 16:14:50');
INSERT INTO `auction_role_permission` VALUES (44, 3, 50, '2026-01-21 16:14:50');
INSERT INTO `auction_role_permission` VALUES (45, 3, 51, '2026-01-21 16:14:50');
INSERT INTO `auction_role_permission` VALUES (46, 3, 52, '2026-01-21 16:14:50');
INSERT INTO `auction_role_permission` VALUES (47, 3, 60, '2026-01-21 16:14:50');
INSERT INTO `auction_role_permission` VALUES (48, 3, 61, '2026-01-21 16:14:50');
INSERT INTO `auction_role_permission` VALUES (49, 3, 62, '2026-01-21 16:14:50');
INSERT INTO `auction_role_permission` VALUES (50, 3, 70, '2026-01-21 16:14:50');
INSERT INTO `auction_role_permission` VALUES (51, 3, 71, '2026-01-21 16:14:50');
INSERT INTO `auction_role_permission` VALUES (52, 3, 72, '2026-01-21 16:14:50');
INSERT INTO `auction_role_permission` VALUES (57, 3, 90, '2026-01-21 16:14:50');
INSERT INTO `auction_role_permission` VALUES (58, 3, 91, '2026-01-21 16:14:50');
INSERT INTO `auction_role_permission` VALUES (59, 3, 92, '2026-01-21 16:14:50');
INSERT INTO `auction_role_permission` VALUES (60, 3, 93, '2026-01-21 16:14:50');
INSERT INTO `auction_role_permission` VALUES (61, 4, 1, '2026-01-21 16:14:50');
INSERT INTO `auction_role_permission` VALUES (62, 4, 2, '2026-01-21 16:14:50');
INSERT INTO `auction_role_permission` VALUES (63, 4, 3, '2026-01-21 16:14:50');
INSERT INTO `auction_role_permission` VALUES (64, 4, 4, '2026-01-21 16:14:50');
INSERT INTO `auction_role_permission` VALUES (65, 4, 5, '2026-01-21 16:14:50');
INSERT INTO `auction_role_permission` VALUES (66, 4, 6, '2026-01-21 16:14:50');
INSERT INTO `auction_role_permission` VALUES (67, 4, 7, '2026-01-21 16:14:50');
INSERT INTO `auction_role_permission` VALUES (68, 4, 8, '2026-01-21 16:14:50');
INSERT INTO `auction_role_permission` VALUES (69, 4, 9, '2026-01-21 16:14:50');
INSERT INTO `auction_role_permission` VALUES (70, 4, 10, '2026-01-21 16:14:50');
INSERT INTO `auction_role_permission` VALUES (71, 4, 11, '2026-01-21 16:14:50');
INSERT INTO `auction_role_permission` VALUES (72, 4, 12, '2026-01-21 16:14:50');
INSERT INTO `auction_role_permission` VALUES (73, 4, 13, '2026-01-21 16:14:50');
INSERT INTO `auction_role_permission` VALUES (74, 4, 14, '2026-01-21 16:14:50');
INSERT INTO `auction_role_permission` VALUES (75, 4, 15, '2026-01-21 16:14:50');
INSERT INTO `auction_role_permission` VALUES (76, 4, 16, '2026-01-21 16:14:50');
INSERT INTO `auction_role_permission` VALUES (77, 4, 20, '2026-01-21 16:14:50');
INSERT INTO `auction_role_permission` VALUES (78, 4, 21, '2026-01-21 16:14:50');
INSERT INTO `auction_role_permission` VALUES (79, 4, 22, '2026-01-21 16:14:50');
INSERT INTO `auction_role_permission` VALUES (80, 4, 23, '2026-01-21 16:14:50');
INSERT INTO `auction_role_permission` VALUES (81, 4, 24, '2026-01-21 16:14:50');
INSERT INTO `auction_role_permission` VALUES (82, 4, 25, '2026-01-21 16:14:50');
INSERT INTO `auction_role_permission` VALUES (83, 4, 30, '2026-01-21 16:14:50');
INSERT INTO `auction_role_permission` VALUES (84, 4, 31, '2026-01-21 16:14:50');
INSERT INTO `auction_role_permission` VALUES (85, 4, 32, '2026-01-21 16:14:50');
INSERT INTO `auction_role_permission` VALUES (86, 4, 33, '2026-01-21 16:14:50');
INSERT INTO `auction_role_permission` VALUES (87, 4, 40, '2026-01-21 16:14:50');
INSERT INTO `auction_role_permission` VALUES (88, 4, 41, '2026-01-21 16:14:50');
INSERT INTO `auction_role_permission` VALUES (89, 4, 42, '2026-01-21 16:14:50');
INSERT INTO `auction_role_permission` VALUES (90, 4, 43, '2026-01-21 16:14:50');
INSERT INTO `auction_role_permission` VALUES (91, 4, 50, '2026-01-21 16:14:50');
INSERT INTO `auction_role_permission` VALUES (92, 4, 51, '2026-01-21 16:14:50');
INSERT INTO `auction_role_permission` VALUES (93, 4, 52, '2026-01-21 16:14:50');
INSERT INTO `auction_role_permission` VALUES (94, 4, 60, '2026-01-21 16:14:50');
INSERT INTO `auction_role_permission` VALUES (95, 4, 61, '2026-01-21 16:14:50');
INSERT INTO `auction_role_permission` VALUES (96, 4, 62, '2026-01-21 16:14:50');
INSERT INTO `auction_role_permission` VALUES (97, 4, 70, '2026-01-21 16:14:50');
INSERT INTO `auction_role_permission` VALUES (98, 4, 71, '2026-01-21 16:14:50');
INSERT INTO `auction_role_permission` VALUES (99, 4, 72, '2026-01-21 16:14:50');
INSERT INTO `auction_role_permission` VALUES (100, 4, 80, '2026-01-21 16:14:50');
INSERT INTO `auction_role_permission` VALUES (101, 4, 81, '2026-01-21 16:14:50');
INSERT INTO `auction_role_permission` VALUES (102, 4, 82, '2026-01-21 16:14:50');
INSERT INTO `auction_role_permission` VALUES (103, 4, 83, '2026-01-21 16:14:50');
INSERT INTO `auction_role_permission` VALUES (104, 4, 90, '2026-01-21 16:14:50');
INSERT INTO `auction_role_permission` VALUES (105, 4, 91, '2026-01-21 16:14:50');
INSERT INTO `auction_role_permission` VALUES (106, 4, 92, '2026-01-21 16:14:50');
INSERT INTO `auction_role_permission` VALUES (107, 4, 93, '2026-01-21 16:14:50');
INSERT INTO `auction_role_permission` VALUES (108, 1, 17, '2026-01-21 16:14:50');
INSERT INTO `auction_role_permission` VALUES (109, 1, 18, '2026-01-21 16:14:50');
INSERT INTO `auction_role_permission` VALUES (110, 2, 17, '2026-01-21 16:14:50');
INSERT INTO `auction_role_permission` VALUES (112, 3, 94, '2026-01-21 16:14:50');
INSERT INTO `auction_role_permission` VALUES (113, 3, 95, '2026-01-21 16:14:50');
INSERT INTO `auction_role_permission` VALUES (114, 4, 17, '2026-01-21 16:14:50');
INSERT INTO `auction_role_permission` VALUES (116, 4, 94, '2026-01-21 16:14:50');
INSERT INTO `auction_role_permission` VALUES (117, 4, 95, '2026-01-21 16:14:50');
INSERT INTO `auction_role_permission` VALUES (118, 1, 19, '2026-01-21 16:14:50');
INSERT INTO `auction_role_permission` VALUES (119, 1, 96, '2026-01-21 16:14:50');
INSERT INTO `auction_role_permission` VALUES (124, 1, 6, '2026-02-25 11:37:45');
INSERT INTO `auction_role_permission` VALUES (125, 1, 8, '2026-02-25 11:37:45');
INSERT INTO `auction_role_permission` VALUES (126, 1, 7, '2026-02-25 11:37:45');
INSERT INTO `auction_role_permission` VALUES (127, 5, 41, '2026-02-25 11:37:45');
INSERT INTO `auction_role_permission` VALUES (128, 5, 40, '2026-02-25 11:37:45');
INSERT INTO `auction_role_permission` VALUES (129, 5, 52, '2026-02-25 11:37:45');
INSERT INTO `auction_role_permission` VALUES (130, 5, 50, '2026-02-25 11:37:45');
INSERT INTO `auction_role_permission` VALUES (131, 5, 60, '2026-02-25 11:37:45');
INSERT INTO `auction_role_permission` VALUES (132, 5, 5, '2026-02-25 11:37:45');
INSERT INTO `auction_role_permission` VALUES (133, 5, 4, '2026-02-25 11:37:45');
INSERT INTO `auction_role_permission` VALUES (134, 5, 3, '2026-02-25 11:37:45');
INSERT INTO `auction_role_permission` VALUES (135, 5, 2, '2026-02-25 11:37:45');
INSERT INTO `auction_role_permission` VALUES (142, 6, 71, '2026-02-25 11:37:45');
INSERT INTO `auction_role_permission` VALUES (143, 6, 70, '2026-02-25 11:37:45');
INSERT INTO `auction_role_permission` VALUES (144, 6, 60, '2026-02-25 11:37:45');
INSERT INTO `auction_role_permission` VALUES (145, 6, 16, '2026-02-25 11:37:45');
INSERT INTO `auction_role_permission` VALUES (149, 6, 15, '2026-02-25 11:37:45');
INSERT INTO `auction_role_permission` VALUES (157, 7, 52, '2026-02-25 11:37:45');
INSERT INTO `auction_role_permission` VALUES (158, 7, 50, '2026-02-25 11:37:45');
INSERT INTO `auction_role_permission` VALUES (159, 7, 61, '2026-02-25 11:37:45');
INSERT INTO `auction_role_permission` VALUES (160, 7, 62, '2026-02-25 11:37:45');
INSERT INTO `auction_role_permission` VALUES (161, 7, 60, '2026-02-25 11:37:45');
INSERT INTO `auction_role_permission` VALUES (162, 7, 13, '2026-02-25 11:37:45');
INSERT INTO `auction_role_permission` VALUES (163, 7, 14, '2026-02-25 11:37:45');
INSERT INTO `auction_role_permission` VALUES (164, 7, 12, '2026-02-25 11:37:45');
INSERT INTO `auction_role_permission` VALUES (172, 8, 31, '2026-02-25 11:37:45');
INSERT INTO `auction_role_permission` VALUES (173, 8, 32, '2026-02-25 11:37:45');
INSERT INTO `auction_role_permission` VALUES (174, 8, 30, '2026-02-25 11:37:45');
INSERT INTO `auction_role_permission` VALUES (175, 8, 41, '2026-02-25 11:37:45');
INSERT INTO `auction_role_permission` VALUES (176, 8, 42, '2026-02-25 11:37:45');
INSERT INTO `auction_role_permission` VALUES (177, 8, 40, '2026-02-25 11:37:45');
INSERT INTO `auction_role_permission` VALUES (178, 8, 81, '2026-02-25 11:37:45');
INSERT INTO `auction_role_permission` VALUES (179, 8, 82, '2026-02-25 11:37:45');
INSERT INTO `auction_role_permission` VALUES (180, 8, 80, '2026-02-25 11:37:45');
INSERT INTO `auction_role_permission` VALUES (183, 6, 20, '2026-02-25 11:52:45');
INSERT INTO `auction_role_permission` VALUES (197, 8, 90, '2026-02-26 14:46:15');
INSERT INTO `auction_role_permission` VALUES (198, 8, 91, '2026-02-26 14:46:15');
INSERT INTO `auction_role_permission` VALUES (199, 8, 92, '2026-02-26 14:46:15');
INSERT INTO `auction_role_permission` VALUES (200, 8, 1, '2026-02-26 14:54:21');
INSERT INTO `auction_role_permission` VALUES (201, 8, 95, '2026-02-26 14:58:05');
INSERT INTO `auction_role_permission` VALUES (202, 3, 80, '2026-02-26 15:43:37');
INSERT INTO `auction_role_permission` VALUES (203, 3, 81, '2026-02-26 15:43:37');
INSERT INTO `auction_role_permission` VALUES (204, 3, 82, '2026-02-26 15:43:37');
INSERT INTO `auction_role_permission` VALUES (205, 5, 95, '2026-02-26 15:43:37');
INSERT INTO `auction_role_permission` VALUES (206, 5, 80, '2026-02-26 15:43:37');
INSERT INTO `auction_role_permission` VALUES (207, 6, 95, '2026-02-26 15:43:37');
INSERT INTO `auction_role_permission` VALUES (208, 6, 80, '2026-02-26 15:43:37');
INSERT INTO `auction_role_permission` VALUES (209, 7, 95, '2026-02-26 15:43:37');
INSERT INTO `auction_role_permission` VALUES (210, 7, 80, '2026-02-26 15:43:37');
INSERT INTO `auction_role_permission` VALUES (212, 4, 97, '2026-02-26 16:53:11');
INSERT INTO `auction_role_permission` VALUES (213, 7, 97, '2026-02-26 16:53:11');
INSERT INTO `auction_role_permission` VALUES (214, 1, 99, '2026-03-03 09:40:03');
INSERT INTO `auction_role_permission` VALUES (215, 1, 100, '2026-03-03 09:40:03');
INSERT INTO `auction_role_permission` VALUES (216, 2, 99, '2026-03-03 09:40:03');
INSERT INTO `auction_role_permission` VALUES (217, 2, 100, '2026-03-03 09:40:03');
INSERT INTO `auction_role_permission` VALUES (218, 6, 99, '2026-03-03 09:40:03');
INSERT INTO `auction_role_permission` VALUES (219, 6, 100, '2026-03-03 09:40:03');
INSERT INTO `auction_role_permission` VALUES (220, 4, 101, '2026-03-03 09:40:03');
INSERT INTO `auction_role_permission` VALUES (221, 4, 99, '2026-03-03 09:40:03');
INSERT INTO `auction_role_permission` VALUES (222, 4, 100, '2026-03-03 09:40:03');
INSERT INTO `auction_role_permission` VALUES (223, 3, 99, '2026-03-04 14:55:49');
INSERT INTO `auction_role_permission` VALUES (224, 3, 100, '2026-03-04 14:55:49');
INSERT INTO `auction_role_permission` VALUES (225, 5, 99, '2026-03-05 16:52:15');
INSERT INTO `auction_role_permission` VALUES (226, 5, 100, '2026-03-05 16:52:15');
INSERT INTO `auction_role_permission` VALUES (227, 8, 60, '2026-03-05 17:12:38');

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
  `avatar_file_id` bigint NULL DEFAULT NULL COMMENT '头像文件ID（关联auction_file.id）',
  `sex` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '2' COMMENT '用户性别（0男 1女 2未知）',
  `user_role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '1' COMMENT '用户角色（多个角色用逗号分隔）：1=普通用户 3=管理员 4=超级管理员 5=拍卖师 6=客服 7=财务 8=运营，默认1（普通用户）',
  `seller_audit_status` tinyint NULL DEFAULT 0 COMMENT '卖方资质审核状态：0=未提交 1=待审核 2=审核通过 3=审核驳回（买方默认为0）',
  `seller_audit_remark` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '卖方资质审核驳回原因',
  `seller_certificate_files` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '卖方资质证明材料（文件路径，多个文件用逗号分隔）',
  `seller_audit_apply_time` datetime NULL DEFAULT NULL COMMENT '卖方资质申请时间',
  `seller_audit_time` datetime NULL DEFAULT NULL COMMENT '卖方资质审核时间',
  `seller_audit_user_id` bigint NULL DEFAULT NULL COMMENT '卖方资质审核人ID（管理员ID）',
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
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统用户表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of auction_user
-- ----------------------------
INSERT INTO `auction_user` VALUES (1, 'superadmin', 'e10adc3949ba59abbe56e057f20f883e', 'superadmin', '超级管理员', '17713356500', '', '', 31, '2', '4', 0, NULL, NULL, NULL, NULL, NULL, 0, '127.0.0.1', '2026-03-10 15:12:44', '2026-01-21 14:54:06', '2026-03-10 15:12:44', 0);
INSERT INTO `auction_user` VALUES (2, 'admin', 'e10adc3949ba59abbe56e057f20f883e', 'admin', '管理员', '17034570777', '1709348112@qq.com', '', NULL, '0', '3', 0, NULL, NULL, NULL, NULL, NULL, 0, '127.0.0.1', '2026-03-10 15:26:14', '2026-01-21 15:50:21', '2026-03-10 15:26:14', 0);
INSERT INTO `auction_user` VALUES (3, 'buyuser', 'e10adc3949ba59abbe56e057f20f883e', '买方用户', '买方用户', '19234532287', '1709348112@qq.com', '', 40, '0', '1', 0, NULL, NULL, NULL, NULL, NULL, 0, '127.0.0.1', '2026-03-10 17:36:21', '2026-01-21 16:17:43', '2026-03-10 17:36:21', 0);
INSERT INTO `auction_user` VALUES (4, 'selluser', 'e10adc3949ba59abbe56e057f20f883e', '卖方用户', '卖方用户', '18766753345', '1709348112@qq.com', '', 32, '0', '1', 0, NULL, NULL, NULL, NULL, NULL, 0, '127.0.0.1', '2026-03-02 17:38:40', '2026-01-21 16:18:22', '2026-03-03 09:47:42', 0);
INSERT INTO `auction_user` VALUES (5, 'paimaishi', 'e10adc3949ba59abbe56e057f20f883e', '拍卖师', '拍卖师', '13848553521', '', '', NULL, '2', '5', 0, NULL, NULL, NULL, NULL, NULL, 0, '127.0.0.1', '2026-03-05 16:52:41', '2026-02-25 12:26:19', '2026-03-05 16:52:41', 0);
INSERT INTO `auction_user` VALUES (6, 'yunying', 'e10adc3949ba59abbe56e057f20f883e', '运营', '运营', '13348553521', '', '', NULL, '2', '8', 0, NULL, NULL, NULL, NULL, NULL, 0, '127.0.0.1', '2026-03-10 17:44:49', '2026-02-26 14:47:27', '2026-03-10 17:44:49', 0);
INSERT INTO `auction_user` VALUES (7, 'caiwu', 'e10adc3949ba59abbe56e057f20f883e', '财务', '财务', '18948553521', '', '', NULL, '2', '7', 0, NULL, NULL, NULL, NULL, NULL, 0, '127.0.0.1', '2026-03-05 17:06:24', '2026-02-26 16:28:29', '2026-03-05 17:06:24', 0);
INSERT INTO `auction_user` VALUES (8, 'buyuser2', 'e10adc3949ba59abbe56e057f20f883e', '买方用户2', '买方用户2', '13345647785', '', '', NULL, '2', '1', 0, NULL, NULL, NULL, NULL, NULL, 0, '127.0.0.1', '2026-03-05 17:08:44', '2026-03-02 07:36:52', '2026-03-05 17:08:44', 0);
INSERT INTO `auction_user` VALUES (9, 'kefu', 'e10adc3949ba59abbe56e057f20f883e', '客服', '客服', '13136792405', '', '', 41, '2', '6', 0, NULL, NULL, NULL, NULL, NULL, 0, '127.0.0.1', '2026-03-04 15:05:33', '2026-03-03 09:47:26', '2026-03-04 15:05:33', 0);

SET FOREIGN_KEY_CHECKS = 1;
