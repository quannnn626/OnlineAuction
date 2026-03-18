package com.auction.onlineauction.OnlineAuction.service;

import java.math.BigDecimal;

/**
 * 系统配置（佣金比例等）
 */
public interface IAuctionSystemConfigService {

    /** 获取佣金比例，如 0.05 表示 5%，不存在则返回默认 0.05 */
    BigDecimal getCommissionRate();

    /** 设置佣金比例 */
    void setCommissionRate(BigDecimal rate);
}
