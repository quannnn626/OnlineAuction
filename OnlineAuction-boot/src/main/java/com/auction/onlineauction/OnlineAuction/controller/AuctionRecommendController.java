package com.auction.onlineauction.OnlineAuction.controller;

import com.auction.onlineauction.OnlineAuction.common.Result;
import com.auction.onlineauction.OnlineAuction.common.RoleCheckHelper;
import com.auction.onlineauction.OnlineAuction.entity.AuctionRecommend;
import com.auction.onlineauction.OnlineAuction.service.IAuctionRecommendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * 推荐位管理：首页推荐、分类推荐商品添加、移除、排序
 * 权限：管理员、超级管理员、运营
 */
@RestController
@RequestMapping("/api/OnlineAuction/auctionRecommend")
public class AuctionRecommendController {

    @Autowired
    private IAuctionRecommendService recommendService;

    @GetMapping("/list")
    public Result<List<AuctionRecommend>> list(
            @RequestParam String recommendType,
            @RequestParam(required = false) Long targetId,
            HttpServletRequest request) {
        try {
            if (!AuctionRecommend.TYPE_HOME.equals(recommendType) && !AuctionRecommend.TYPE_CATEGORY.equals(recommendType)) {
                return Result.error("recommendType 需为 home 或 category");
            }
            return Result.success("查询成功", recommendService.listByTypeAndTarget(recommendType, targetId != null ? targetId : 0L));
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    @PostMapping
    public Result<AuctionRecommend> add(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        try {
            if (!RoleCheckHelper.canManageRecommend(request.getSession(false))) {
                return Result.error("无权限操作推荐位");
            }
            String type = body.get("recommendType") != null ? body.get("recommendType").toString().trim() : null;
            Object tid = body.get("targetId");
            Long targetId = tid != null && !tid.toString().trim().isEmpty() ? Long.valueOf(tid.toString()) : null;
            Long goodsId = body.get("goodsId") != null ? Long.valueOf(body.get("goodsId").toString()) : null;
            Integer sortOrder = body.get("sortOrder") != null ? Integer.valueOf(body.get("sortOrder").toString()) : 0;
            if (type == null || goodsId == null) {
                return Result.error("recommendType、goodsId 不能为空");
            }
            if (!AuctionRecommend.TYPE_HOME.equals(type) && !AuctionRecommend.TYPE_CATEGORY.equals(type)) {
                return Result.error("recommendType 需为 home 或 category");
            }
            if (AuctionRecommend.TYPE_HOME.equals(type)) {
                targetId = 0L;
            } else if (targetId == null) {
                return Result.error("分类推荐时 targetId（分类ID）不能为空");
            }
            AuctionRecommend r = new AuctionRecommend();
            r.setRecommendType(type);
            r.setTargetId(targetId);
            r.setGoodsId(goodsId);
            r.setSortOrder(sortOrder);
            r.setDelFlag(0);
            recommendService.save(r);
            return Result.success("添加成功", r);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/{id}/sort")
    public Result<Void> updateSort(@PathVariable Long id, @RequestBody Map<String, Object> body, HttpServletRequest request) {
        try {
            if (!RoleCheckHelper.canManageRecommend(request.getSession(false))) {
                return Result.error("无权限操作推荐位");
            }
            Integer sortOrder = body.get("sortOrder") != null ? Integer.valueOf(body.get("sortOrder").toString()) : null;
            if (sortOrder == null) return Result.error("sortOrder 不能为空");
            recommendService.updateSortOrder(id, sortOrder);
            return Result.success("排序更新成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id, HttpServletRequest request) {
        try {
            if (!RoleCheckHelper.canManageRecommend(request.getSession(false))) {
                return Result.error("无权限操作推荐位");
            }
            AuctionRecommend r = recommendService.getById(id);
            if (r != null) {
                r.setDelFlag(1);
                recommendService.updateById(r);
            }
            return Result.success("移除成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
