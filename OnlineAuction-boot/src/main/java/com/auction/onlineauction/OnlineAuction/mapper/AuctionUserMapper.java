package com.auction.onlineauction.OnlineAuction.mapper;

import com.auction.onlineauction.OnlineAuction.entity.AuctionUser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 系统用户表 Mapper 接口
 * </p>
 *
 * @author MrYan
 * @since 2025-12-31
 */
public interface AuctionUserMapper extends BaseMapper<AuctionUser> {

    /**
     * 风控：存在异常出价标记（risk_rule_type 大于 0）或当前风险等级大于 0 的用户
     */
    List<Map<String, Object>> selectUsersWithRiskActivityForRisk();
}
