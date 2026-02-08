package com.auction.onlineauction.OnlineAuction.controller;

import com.auction.onlineauction.OnlineAuction.common.Result;
import com.auction.onlineauction.OnlineAuction.entity.AuctionRecord;
import com.auction.onlineauction.OnlineAuction.service.IAuctionRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 竞拍记录表 前端控制器
 * </p>
 *
 * @author MrYan
 * @since 2025-12-31
 */
@RestController
@RequestMapping("/api/OnlineAuction/auctionRecord")
public class AuctionRecordController {

    @Autowired
    private IAuctionRecordService recordService;

    /**
     * 提交竞拍出价
     */
    @PostMapping("/bid")
    public Result<AuctionRecord> submitBid(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        try {
            HttpSession session = request.getSession(false);
            if (session == null) {
                return Result.error("未登录");
            }

            Long buyerId = (Long) session.getAttribute("userId");
            if (buyerId == null) {
                return Result.error("未登录");
            }

            Long goodsId = Long.valueOf(params.get("goodsId").toString());
            BigDecimal bidPrice = new BigDecimal(params.get("bidPrice").toString());

            AuctionRecord record = recordService.submitBid(goodsId, buyerId, bidPrice);
            return Result.success("出价成功", record);
        } catch (Exception e) {
            return Result.error("出价失败：" + e.getMessage());
        }
    }

    /**
     * 查询商品的竞拍记录列表
     */
    @GetMapping("/list")
    public Result<List<AuctionRecord>> getRecordsByGoodsId(
            @RequestParam Long goodsId,
            @RequestParam(defaultValue = "10") Integer limit) {
        try {
            List<AuctionRecord> records = recordService.getRecordsByGoodsId(goodsId, limit);
            return Result.success("查询成功", records);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }
}
