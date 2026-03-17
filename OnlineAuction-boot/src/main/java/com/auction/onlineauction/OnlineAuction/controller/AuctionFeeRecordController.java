package com.auction.onlineauction.OnlineAuction.controller;

import com.auction.onlineauction.OnlineAuction.common.Result;
import com.auction.onlineauction.OnlineAuction.common.RoleCheckHelper;
import com.auction.onlineauction.OnlineAuction.entity.AuctionFeeRecord;
import com.auction.onlineauction.OnlineAuction.service.IAuctionFeeRecordService;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * 佣金结算、平台扣费记录（财务查看）
 */
@RestController
@RequestMapping("/api/OnlineAuction/auctionFeeRecord")
public class AuctionFeeRecordController {

    @Autowired
    private IAuctionFeeRecordService feeRecordService;

    @GetMapping("/page")
    public Result<PageInfo<AuctionFeeRecord>> getPage(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer feeType,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long userId,
            HttpServletRequest request) {
        try {
            if (!RoleCheckHelper.canManageDepositAdmin(request.getSession(false))) {
                return Result.error("无权限查看");
            }
            PageInfo<AuctionFeeRecord> page = feeRecordService.getPage(current, size, feeType, status, userId);
            return Result.success("查询成功", page);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }
}
