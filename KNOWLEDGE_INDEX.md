# 在线拍卖系统 — 项目知识索引图谱

> 自动生成于 2026-05-23 | 每次提问前可快速查阅此文件，避免重复检索

---

## 一、目录结构总览

```
d:\WorkSpace\OnlineAuction\
├── OnlineAuction-boot/           # Spring Boot 后端 (Java 1.8, Boot 2.7.18)
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/auction/onlineauction/
│       │   ├── OnlineAuctionApplication.java    # 启动类 (@EnableScheduling)
│       │   └── OnlineAuction/
│       │       ├── common/       # Result.java, RoleCheckHelper.java
│       │       ├── config/       # WebMvcConfig, GoodsStatusScheduler, MpWechatConfig
│       │       ├── controller/   # 43个控制器 (含 mp/ 小程序控制器)
│       │       ├── dto/          # LoginDTO, MpWechatLoginRequest
│       │       ├── entity/       # 28个实体类 (对应28张表)
│       │       ├── mapper/       # Mapper接口 + xml/ (14个XML映射文件)
│       │       └── service/      # 25个接口 + impl/ (26个实现类)
│       └── resources/
│           └── application.yml   # 端口8080, MySQL 3307, session 30min
│
├── OnlineAuction-vue/            # Vue 2 前端
│   ├── package.json              # Vue 2.6.14, Element-UI 2.15.13, Axios 0.27.2
│   ├── vue.config.js             # 端口3000, 代理到 :8080
│   └── src/
│       ├── main.js               # Element-UI + Axios拦截器
│       ├── router/index.js       # 路由 + 角色路由守卫 (280+行)
│       ├── api/                  # 25个API模块
│       ├── utils/                # request.js (Axios实例), loginPath.js
│       └── views/                # 前台页面 + admin/ (18个) + seller/
│
├── OnlineAuction-miniprogram/    # 微信小程序 (TypeScript)
├── sql/
│   └── onlineauction.sql         # 完整数据库 (28张表, Navicat导出)
├── 项目分析/
│   ├── 论文拆分/                 # 论文章节 (00a ~ 08)
│   └── (30+ 分析文档)
├── upload/                       # 上传文件目录
├── KNOWLEDGE_INDEX.md            # ← 本文件
└── PPT大纲_*.md                  # PPT文案
```

---

## 二、技术栈

| 层次 | 技术 | 版本 |
|------|------|------|
| 后端框架 | Spring Boot | 2.7.18 |
| ORM | MyBatis-Plus | 3.3.1 |
| 分页 | PageHelper | 1.4.6 |
| 数据库 | MySQL | 8.0 |
| 连接池 | HikariCP | — |
| 前端框架 | Vue.js | 2.6.14 |
| UI组件库 | Element UI | 2.15.13 |
| 路由 | Vue Router | 3.5.4 |
| HTTP客户端 | Axios | 0.27.2 |
| 微信SDK | weixin-java-miniapp | 4.5.0 |
| 通信协议 | HTTP/REST (无WebSocket) | — |
| 认证方式 | HttpSession (非JWT) | 30min超时 |

---

## 三、十种角色体系

| 编码 | 角色 | 主要权限 |
|:--:|------|------|
| 1 | 买方 (Buyer) | 浏览商品、参与竞拍、管理订单、申请成为卖方 |
| 2 | 卖方 (Seller) | 发布商品、管理商品、订单发货 |
| 3 | 管理员 (Admin) | 用户管理、商品审核、公告、工单复核 |
| 4 | 超级管理员 (Super Admin) | 管理员权限 + 物理删除商品 + 创建高权限角色 |
| 5 | 拍卖师 (Auctioneer) | 商品审核、拍卖延时、流拍标记、成交确认 |
| 6 | 客服 (Customer Service) | 工单处理、消息回复 |
| 7 | 财务 (Finance) | 保证金管理、退款、佣金结算、发票 |
| 8 | 运营 (Operations) | 类目、轮播图、专场、推荐位、数据看板 |
| 9 | 风控 (Risk Control) | 异常出价监控、风险标记、冻结解封申请 |
| 10 | 审计 (Audit) | 操作日志、登录日志（只读） |

**实现**: 角色值以逗号分隔存储在 `auction_user.role`，`RoleCheckHelper` 逐接口校验。

---

## 四、后端控制器速查

### 认证 + 公开接口
| 控制器 | 路径 | 用途 |
|------|------|------|
| `AuthController` | `/api/auth` | 前台/后台登录、注册、登出 |
| `MpApiController` | `/api/mp` | 小程序微信登录 |
| `GoodsApiController` | `/api/goods` | 公开商品列表/详情/搜索/热门 |
| `OrderApiController` | `/api/order` | 用户订单/支付/收货/地址 |
| `DepositApiController` | `/api/deposit` | 用户保证金余额/流水 |
| `BannerApiController` | `/api/banner` | 首页轮播图 |
| `CategoryApiController` | `/api/category` | 商品类目 |
| `NoticeApiController` | `/api/notice` | 拍卖公告 |

### 后台管理接口 (`/api/OnlineAuction/`)
| 控制器 | 路径 | 用途 |
|------|------|------|
| `AuctionUserController` | `/auctionUser` | 用户CRUD、卖方审核、密码/头像 |
| `AuctionGoodsController` | `/auctionGoods` | 商品CRUD、审核、上下架、延时、流拍 |
| `AuctionRecordController` | `/auctionRecord` | ★竞拍出价、代理出价、历史竞拍 |
| `AuctionOrderController` | `/auctionOrder` | 订单管理、成交确认、违约、退款、发货 |
| `AuctionDepositController` | `/auctionDeposit` | 保证金汇总、冻结/解冻、手动充值 |
| `AuctionDepositRechargeApplyController` | `/auctionDepositRechargeApply` | 充值申请审核 |
| `AuctionFeeRecordController` | `/auctionFeeRecord` | 佣金比例、结算、费用导出 |
| `AuctionInvoiceController` | `/auctionInvoice` | 发票申请/开票 |
| `AuctionCategoryController` | `/auctionCategory` | 类目CRUD |
| `AuctionBannerController` | `/auctionBanner` | 轮播图CRUD |
| `AuctionSpecialController` | `/auctionSpecial` | 专场管理 |
| `AuctionRecommendController` | `/auctionRecommend` | 推荐位管理 |
| `AuctionNoticeController` | `/auctionNotice` | 公告管理 |
| `AuctionFileController` | `/auctionFile` | 文件上传 |
| `AuctionNotificationController` | `/notifications` | 管理通知发送/收件箱 |
| `AuctionAddressController` | `/auctionAddress` | 用户地址管理 |
| `AuctionMenuController` | `/auctionMenu` | 菜单管理 |
| `AuctionPermissionController` | `/auctionPermission` | 权限管理 |
| `AuctionRolePermissionController` | `/auctionRolePermission` | 角色权限分配 |
| `AuctionOperLogController` | `/auctionOperLog` | 操作日志查询 |
| `DashboardController` | `/dashboard` | 数据看板 |

### 专项控制器
| 控制器 | 路径 | 用途 |
|------|------|------|
| `WorkOrderApiController` | `/api/work-order` | 工单提交/客服处理/管理员复核 |
| `RiskApiController` | `/api/risk` | 风控：风险等级/异常出价/冻结申请 |
| `AuditApiController` | `/api/audit` | 审计：操作日志/登录日志（只读） |
| `AuctionComplaintController` | `/api/complaint` | 投诉提交 |
| `AuctionMessageController` | `/api/message` | 留言板 |
| `MessageCenterController` | `/api/message-center` | 消息中心会话/消息 |

---

## 五、前端文件速查

### API模块 (`src/api/` — 25个文件)
`auth.js` `user.js` `goods.js` `record.js` `order.js` `deposit.js` `rechargeApply.js` `finance.js` `invoice.js` `category.js` `banner.js` `special.js` `recommend.js` `notice.js` `menu.js` `message.js` `messageCenter.js` `workOrder.js` `complaint.js` `notification.js` `risk.js` `audit.js` `dashboard.js` `address.js` `file.js`

### 前台页面 (`src/views/`)
`Login.vue` `Register.vue` `Home.vue` `Goods.vue` `GoodsDetail.vue` `Special.vue` `SearchResults.vue` `MyGoods.vue` `BidHistory.vue` `ApplySeller.vue` `Notice.vue` `MessageBoard.vue` `MessageCenter.vue` `WorkOrder.vue` `RiskDashboard.vue` `AuditDashboard.vue` `Profile.vue` + `ProfileAddress/Deposit/Invoice/Order/Message.vue`

### 后台页面 (`src/views/admin/` — 18个)
`AdminLogin.vue` `AdminProfile.vue` `AdminDashboard.vue` `AdminUser.vue` `AdminCategory.vue` `AdminGoods.vue` `AdminGoodsTime.vue` `AdminHistory.vue` `AdminSellerAudit.vue` `AdminOrder.vue` `AdminDeposit.vue` `AdminFeeRecord.vue` `AdminInvoice.vue` `AdminRechargeApply.vue` `AdminMessage.vue` `AdminSettings.vue` `AdminSpecial.vue` `AdminRecommend.vue` `AdminNotificationsSend.vue` `AdminNotificationsInbox.vue`

---

## 六、数据库 28 张表

| # | 表名 | 用途 | 类别 |
|---|------|------|------|
| 1 | `auction_user` | 用户 (角色/状态/卖方资质/风险等级) | 用户 |
| 2 | `auction_user_address` | 收货地址 | 用户 |
| 3 | `auction_goods` | 拍卖商品 (价格/时间/审核/状态/统计) | 商品 |
| 4 | `auction_record` | 竞拍记录 (出价/代理/最高价标记/风控类型/IP) | 竞拍 |
| 5 | `auction_order` | 订单 (成交价/保证金/尾款/物流/成交确认号) | 交易 |
| 6 | `auction_deposit` | 保证金流水 (充值/冻结/解冻/抵扣/扣除) | 资金 |
| 7 | `auction_deposit_recharge_apply` | 保证金充值申请 | 资金 |
| 8 | `auction_fee_record` | 平台费用/佣金记录 | 资金 |
| 9 | `auction_invoice` | 发票申请 (抬头/税号/文件/状态) | 资金 |
| 10 | `auction_category` | 商品类目 (三级树形结构) | 内容 |
| 11 | `auction_banner` | 首页轮播图 | 内容 |
| 12 | `auction_special` | 拍卖专场 | 内容 |
| 13 | `auction_special_goods` | 专场-商品关联 (多对多) | 内容 |
| 14 | `auction_recommend` | 推荐位 (首页/分类) | 内容 |
| 15 | `auction_notice` | 竞拍公告 | 内容 |
| 16 | `auction_message` | 留言板 | 消息 |
| 17 | `auction_message_center` | 消息中心消息 | 消息 |
| 18 | `auction_message_session` | 消息中心会话 | 消息 |
| 19 | `auction_work_order` | 客服工单 | 工单 |
| 20 | `auction_complaint` | 投诉记录 | 工单 |
| 21 | `auction_notification` | 管理通知 | 通知 |
| 22 | `auction_notification_target` | 通知接收记录 | 通知 |
| 23 | `auction_oper_log` | 操作审计日志 | 审计 |
| 24 | `auction_file` | 上传文件元数据 | 基础 |
| 25 | `auction_system_config` | 系统配置 (键值对) | 配置 |
| 26 | `auction_menu` | 系统菜单 (RBAC) | 权限 |
| 27 | `auction_permission` | 权限定义 (RBAC) | 权限 |
| 28 | `auction_role_permission` | 角色-权限关联 (RBAC) | 权限 |

---

## 七、核心业务流程速查

### 竞拍出价 (最核心)
```
submitBid() → SELECT FOR UPDATE 行锁商品
  → 校验: 商品存在/未删除/竞拍中/时间窗口内/非卖方自拍/金额满足加价幅度
  → 五条风控规则检测 (3拦截+2预警)
  → 首次出价冻结保证金
  → 清除旧最高价 → 写入新出价记录(isHighest=1) → 更新商品最高价+竞价次数 → 提交事务
```
- **入口**: `AuctionRecordController.submitBid()` → `AuctionRecordServiceImpl.submitBid()`

### 自动结算
- `GoodsStatusScheduler` 每5分钟执行 → `AuctionGoodsServiceImpl` 自动更新到期商品状态 → 有最高出价生成订单 / 无出价标记流拍

### 工单流转
```
用户提交 → 自动分配客服(负载均衡) → 客服处理 → 直接关闭 / 提交复核 → 管理员复核(通过+处罚 / 驳回)
```

### 订单状态
```
待付款 → 支付 → 待发货 → 发货 → 待收货 → 确认收货 → 已完成
```

---

## 八、论文文档位置

| 文件 | 路径 |
|------|------|
| 中文摘要 | `项目分析/论文拆分/00b_摘要.md` |
| 英文摘要 | `项目分析/论文拆分/00c_Abstract.md` |
| 第1章 绪论 | `项目分析/论文拆分/01_第1章_绪论.md` |
| 第2章 需求分析 | `项目分析/论文拆分/02_第2章_需求分析.md` |
| 第3章 总体设计 | `项目分析/论文拆分/03_第3章_系统总体设计.md` |
| 第4章 详细设计 | `项目分析/论文拆分/04_第4章_系统详细设计.md` |
| 第5章 测试 | `项目分析/论文拆分/05_第5章_系统运行与测试.md` |
| 结论 | `项目分析/论文拆分/06_结论.md` |
| 参考文献 | `项目分析/论文拆分/07_参考文献与致谢.md` |
| 表格汇总 | `项目分析/论文拆分/08_表格汇总.md` |
| 第一章重写版 | `项目分析/第一章_重写.md` |
| PPT大纲(9页) | `PPT大纲_六大功能模块.md` |
| PPT大纲(15页) | `PPT大纲_六大功能模块_15页版.md` |
| PPT大纲(27页) | `PPT大纲_六大功能模块_详细版.md` |
| 数据库SQL | `sql/onlineauction.sql` |
| 业务流程分析 | `项目分析/业务流程分析.md` |
| 数据库设计 | `项目分析/数据库表结构文档.md` |
