package com.auction.onlineauction.OnlineAuction.controller;

import com.auction.onlineauction.OnlineAuction.common.Result;
import com.auction.onlineauction.OnlineAuction.common.RoleCheckHelper;
import com.auction.onlineauction.OnlineAuction.entity.AuctionGoods;
import com.auction.onlineauction.OnlineAuction.entity.AuctionOrder;
import com.auction.onlineauction.OnlineAuction.entity.AuctionRecord;
import com.auction.onlineauction.OnlineAuction.entity.AuctionUser;
import com.auction.onlineauction.OnlineAuction.mapper.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

/**
 * 数据看板：商品、用户、参拍、订单、成交率、保证金等统计数据
 * 权限：管理员、超级管理员、运营
 */
@RestController
@RequestMapping("/api/OnlineAuction/dashboard")
public class DashboardController {

    @Autowired
    private AuctionGoodsMapper goodsMapper;
    @Autowired
    private AuctionUserMapper userMapper;
    @Autowired
    private AuctionRecordMapper recordMapper;
    @Autowired
    private AuctionOrderMapper orderMapper;
    @Autowired
    private AuctionDepositMapper depositMapper;

    @GetMapping("/stats")
    public Result<Map<String, Object>> stats(HttpServletRequest request) {
        try {
            if (!RoleCheckHelper.canViewDashboard(request.getSession(false))) {
                return Result.error("无权限查看数据看板");
            }
            Map<String, Object> data = new HashMap<>();

            long goodsTotal = goodsMapper.selectCount(new QueryWrapper<AuctionGoods>().eq("del_flag", 0));
            data.put("goodsTotal", goodsTotal);

            long userTotal = userMapper.selectCount(new QueryWrapper<AuctionUser>().eq("del_flag", 0));
            data.put("userTotal", userTotal);

            long bidRecordTotal = recordMapper.selectCount(new QueryWrapper<AuctionRecord>());
            data.put("bidRecordTotal", bidRecordTotal);

            long orderTotal = orderMapper.selectCount(new QueryWrapper<AuctionOrder>().eq("del_flag", 0));
            data.put("orderTotal", orderTotal);

            long orderDealCount = orderMapper.selectCount(new QueryWrapper<AuctionOrder>().eq("del_flag", 0).eq("order_status", 3));
            data.put("orderDealCount", orderDealCount);

            BigDecimal dealRate = orderTotal > 0
                    ? BigDecimal.valueOf(orderDealCount).multiply(BigDecimal.valueOf(100)).divide(BigDecimal.valueOf(orderTotal), 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
            data.put("dealRate", dealRate);

            Map<String, Object> depositSummary = depositMapper.selectPlatformSummary();
            if (depositSummary != null) {
                Object av = depositSummary.get("totalAvailable");
                Object fr = depositSummary.get("totalFrozen");
                BigDecimal available = av != null ? new BigDecimal(av.toString()) : BigDecimal.ZERO;
                BigDecimal frozen = fr != null ? new BigDecimal(fr.toString()) : BigDecimal.ZERO;
                data.put("depositTotal", available.add(frozen));
                data.put("depositAvailable", available);
                data.put("depositFrozen", frozen);
                data.put("depositUserCount", depositSummary.get("userCount"));
            } else {
                data.put("depositTotal", BigDecimal.ZERO);
                data.put("depositAvailable", BigDecimal.ZERO);
                data.put("depositFrozen", BigDecimal.ZERO);
                data.put("depositUserCount", 0);
            }

            return Result.success("查询成功", data);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }
}
