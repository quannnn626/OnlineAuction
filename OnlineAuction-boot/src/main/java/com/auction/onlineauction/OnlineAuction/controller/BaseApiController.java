package com.auction.onlineauction.OnlineAuction.controller;

import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 基础API控制器
 * 所有API Controller都应该继承此类，统一使用 /api 前缀
 *
 * @author MrYan
 * @since 2025-12-31
 */
@RequestMapping("/api")
public abstract class BaseApiController {
    // 这个类只用于统一路径前缀，不需要实现任何方法
}

