package com.auction.onlineauction.OnlineAuction.controller;

import com.auction.onlineauction.OnlineAuction.common.Result;
import com.auction.onlineauction.OnlineAuction.entity.AuctionAddress;
import com.auction.onlineauction.OnlineAuction.service.IAuctionAddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;

@RestController
@RequestMapping("/api/OnlineAuction/auctionAddress")
public class AuctionAddressController {

    @Autowired
    private IAuctionAddressService addressService;

    private Long getUserId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        return (Long) session.getAttribute("userId");
    }

    @GetMapping("/list")
    public Result<List<AuctionAddress>> getAddressList(HttpServletRequest request) {
        Long userId = getUserId(request);
        if (userId == null) {
            return Result.error("未登录");
        }
        List<AuctionAddress> list = addressService.getAddressList(userId);
        return Result.success("查询成功", list);
    }

    @PostMapping
    public Result<AuctionAddress> saveAddress(@RequestBody AuctionAddress address, HttpServletRequest request) {
        try {
            Long userId = getUserId(request);
            if (userId == null) {
                return Result.error("未登录");
            }
            AuctionAddress saved = addressService.saveAddress(address, userId);
            return Result.success("保存成功", saved);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteAddress(@PathVariable Long id, HttpServletRequest request) {
        try {
            Long userId = getUserId(request);
            if (userId == null) {
                return Result.error("未登录");
            }
            addressService.deleteAddress(id, userId);
            return Result.success("删除成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/{id}/default")
    public Result<Void> setDefault(@PathVariable Long id, HttpServletRequest request) {
        try {
            Long userId = getUserId(request);
            if (userId == null) {
                return Result.error("未登录");
            }
            addressService.setDefault(id, userId);
            return Result.success("设置默认成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
