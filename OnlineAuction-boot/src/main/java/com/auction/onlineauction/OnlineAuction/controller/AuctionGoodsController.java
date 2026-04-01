package com.auction.onlineauction.OnlineAuction.controller;

import com.auction.onlineauction.OnlineAuction.common.Result;
import com.auction.onlineauction.OnlineAuction.common.RoleCheckHelper;
import com.auction.onlineauction.OnlineAuction.entity.AuctionGoods;
import com.auction.onlineauction.OnlineAuction.service.IAuctionGoodsService;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 拍卖商品表 前端控制器
 * </p>
 *
 * @author MrYan
 * @since 2025-12-31
 */
@RestController
@RequestMapping("/api/OnlineAuction/auctionGoods")
public class AuctionGoodsController {

    @Autowired
    private IAuctionGoodsService goodsService;

    /**
     * 分页查询商品列表（后台，需管理员/运营/拍卖师/客服/财务角色）
     */
    @GetMapping("/page")
    public Result<PageInfo<AuctionGoods>> getGoodsPage(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String goodsName,
            @RequestParam(required = false) String categoryId,
            @RequestParam(required = false) Integer auditStatus,
            @RequestParam(required = false) Integer goodsStatus,
            HttpServletRequest request) {
        try {
            if (!RoleCheckHelper.canAccessAdmin(request.getSession(false))) {
                return Result.error("无权限访问");
            }
            PageInfo<AuctionGoods> pageInfo = goodsService.getGoodsPage(current, size, goodsName, categoryId, auditStatus, goodsStatus);
            return Result.success("查询成功", pageInfo);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 获取所有商品列表（不分页，后台使用，需管理员/运营/拍卖师/客服/财务角色）
     */
    @GetMapping("/list")
    public Result<List<AuctionGoods>> getGoodsList(HttpServletRequest request) {
        try {
            if (!RoleCheckHelper.canAccessAdmin(request.getSession(false))) {
                return Result.error("无权限访问");
            }
            List<AuctionGoods> list = goodsService.getGoodsList();
            return Result.success("查询成功", list);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 获取我的商品列表（仅卖方可访问，买方无此功能）
     */
    @GetMapping("/my")
    public Result<PageInfo<AuctionGoods>> getMyGoodsList(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            HttpServletRequest request) {
        try {
            HttpSession session = request.getSession(false);
            if (session == null) {
                return Result.error("未登录");
            }
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                return Result.error("未登录");
            }
            if (!RoleCheckHelper.hasAnyRole(session, 2, 3, 4, 8)) {
                return Result.error("无权限，仅卖方可查看自己的商品");
            }
            PageInfo<AuctionGoods> pageInfo = goodsService.getMyGoodsList(current, size, userId);
            return Result.success("查询成功", pageInfo);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 根据ID获取商品详情（后台）
     */
    @GetMapping("/{id}")
    public Result<AuctionGoods> getGoodsById(@PathVariable Long id, HttpServletRequest request) {
        try {
            if (!RoleCheckHelper.canAccessAdmin(request.getSession(false))) {
                return Result.error("无权限访问");
            }
            AuctionGoods goods = goodsService.getGoodsById(id);
            return Result.success("查询成功", goods);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 新增商品
     */
    @PostMapping("/addGoods")
    public Result<AuctionGoods> addGoods(@RequestBody Map<String, Object> requestData, HttpServletRequest request) {
        try {
            HttpSession session = request.getSession(false);
            if (session == null) {
                return Result.error("未登录");
            }
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                return Result.error("未登录");
            }
            // 仅卖方(2)、运营(8)、管理员(3)、超管(4)可发布；买方(1)不可上架商品
            if (!RoleCheckHelper.hasAnyRole(session, 2, 3, 4, 8)) {
                return Result.error("无权限新增商品，仅卖方角色可上架商品");
            }
            AuctionGoods goods = goodsService.addGoods(requestData, userId);
            return Result.success("新增成功", goods);
        } catch (Exception e) {
            return Result.error("新增失败：" + e.getMessage());
        }
    }

    /**
     * 更新商品（管理员/运营/拍卖师可编辑；卖方仅可编辑自己的商品；买方不可编辑）
     */
    @PutMapping("/{id}")
    public Result<AuctionGoods> updateGoods(@PathVariable Long id, @RequestBody Map<String, Object> requestData,
                                           HttpServletRequest request) {
        try {
            HttpSession session = request.getSession(false);
            if (session == null) {
                return Result.error("未登录");
            }
            Long userId = (Long) session.getAttribute("userId");
            AuctionGoods existing = goodsService.getById(id);
            if (existing == null || existing.getDelFlag() == 1) {
                return Result.error("商品不存在");
            }
            boolean canEdit = RoleCheckHelper.hasAnyRole(session, 3, 4, 5, 8)
                    || (RoleCheckHelper.hasAnyRole(session, 2) && userId != null && userId.equals(existing.getSellerId()));
            if (!canEdit) {
                return Result.error("无权限编辑商品，仅卖方可编辑自己的商品");
            }
            AuctionGoods goods = goodsService.updateGoods(id, requestData);
            return Result.success("更新成功", goods);
        } catch (Exception e) {
            return Result.error("更新失败：" + e.getMessage());
        }
    }

    /**
     * 上架/下架商品（仅运营、管理员、超级管理员可操作）
     */
    @PutMapping("/shelf/{id}")
    public Result<Void> updateShelfStatus(@PathVariable Long id, @RequestParam Integer shelfStatus,
                                         HttpServletRequest request) {
        try {
            if (!RoleCheckHelper.canUpdateShelf(request.getSession(false))) {
                return Result.error("无权限修改商品上架状态");
            }
            goodsService.updateShelfStatus(id, shelfStatus);
            return Result.success(shelfStatus == 1 ? "已上架" : "已下架", null);
        } catch (Exception e) {
            return Result.error("操作失败：" + e.getMessage());
        }
    }

    /**
     * 限时拍管理：设置商品竞拍起止时间（管理员、超级管理员、运营）
     */
    @PutMapping("/admin/{id}/time")
    public Result<Void> setGoodsTime(@PathVariable Long id, @RequestBody Map<String, Object> body, HttpServletRequest request) {
        try {
            if (!RoleCheckHelper.canManageGoodsTime(request.getSession(false))) {
                return Result.error("无权限设置竞拍时间");
            }
            goodsService.setGoodsStartEndTime(id, body);
            return Result.success("设置成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 拍卖延时：拍卖师/管理员将竞拍中商品结束时间延长（分钟）
     */
    @PutMapping("/{id}/extend-time")
    public Result<Void> extendAuctionTime(@PathVariable Long id, @RequestParam(defaultValue = "5") Integer minutes,
                                          HttpServletRequest request) {
        try {
            if (!RoleCheckHelper.canAuctioneerManage(request.getSession(false))) {
                return Result.error("无权限，仅拍卖师/管理员可操作");
            }
            goodsService.extendAuctionTime(id, minutes != null ? minutes : 5);
            return Result.success("已延长 " + (minutes != null ? minutes : 5) + " 分钟", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 流拍：拍卖师/管理员将竞拍中商品标记为流拍
     */
    @PutMapping("/{id}/mark-no-sale")
    public Result<Void> markNoSale(@PathVariable Long id, HttpServletRequest request) {
        try {
            if (!RoleCheckHelper.canAuctioneerManage(request.getSession(false))) {
                return Result.error("无权限，仅拍卖师/管理员可操作");
            }
            goodsService.markNoSale(id);
            return Result.success("已标记流拍", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 卖家对自己商品的上下架（仅本人可操作自己的商品）
     */
    @PutMapping("/my/shelf/{id}")
    public Result<Void> updateMyGoodsShelf(@PathVariable Long id, @RequestParam Integer shelfStatus,
                                          HttpServletRequest request) {
        try {
            HttpSession session = request.getSession(false);
            if (session == null) return Result.error("未登录");
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) return Result.error("未登录");
            if (!RoleCheckHelper.hasAnyRole(session, 2, 3, 4, 8)) {
                return Result.error("无权限，仅卖方可操作自己的商品上下架");
            }
            goodsService.updateShelfStatusByOwner(id, userId, shelfStatus);
            return Result.success(shelfStatus == 1 ? "已上架" : "已下架", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除商品：超级管理员真删除，其余有权限角色逻辑删除
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteGoods(@PathVariable Long id, HttpServletRequest request) {
        try {
            HttpSession session = request.getSession(false);
            if (!RoleCheckHelper.canDeleteGoods(session)) {
                return Result.error("无权限删除商品");
            }
            if (RoleCheckHelper.hasAnyRole(session, 4)) {
                goodsService.hardDeleteGoods(id);
                return Result.success("删除成功", null);
            } else {
                goodsService.deleteGoods(id);
                return Result.success("删除成功", null);
            }
        } catch (Exception e) {
            return Result.error("删除失败：" + e.getMessage());
        }
    }

    /**
     * 批量删除商品：超级管理员真删除，其余有权限角色逻辑删除
     */
    @DeleteMapping("/batch")
    public Result<Void> batchDeleteGoods(@RequestBody List<Long> ids, HttpServletRequest request) {
        try {
            HttpSession session = request.getSession(false);
            if (!RoleCheckHelper.canDeleteGoods(session)) {
                return Result.error("无权限批量删除商品");
            }
            if (RoleCheckHelper.hasAnyRole(session, 4)) {
                goodsService.batchHardDeleteGoods(ids);
                return Result.success("批量删除成功", null);
            } else {
                goodsService.batchDeleteGoods(ids);
                return Result.success("批量删除成功", null);
            }
        } catch (Exception e) {
            return Result.error("批量删除失败：" + e.getMessage());
        }
    }

    /**
     * 审核商品（管理员、超级管理员、拍卖师、运营可操作）
     */
    @PutMapping("/audit/{id}")
    public Result<Void> auditGoods(@PathVariable Long id, @RequestParam Integer auditStatus,
                                  @RequestParam(required = false) String auditRemark,
                                  HttpServletRequest request) {
        try {
            if (!RoleCheckHelper.canAuditGoods(request.getSession(false))) {
                return Result.error("无权限审核商品");
            }
            goodsService.auditGoods(id, auditStatus, auditRemark);
            return Result.success("审核成功", null);
        } catch (Exception e) {
            return Result.error("审核失败：" + e.getMessage());
        }
    }

    /**
     * 重新申请上架（卖家操作）
     */
    @PutMapping("/reapply/{id}")
    public Result<Void> reapplyGoods(@PathVariable Long id, HttpServletRequest request) {
        try {
            HttpSession session = request.getSession(false);
            if (session == null) {
                return Result.error("未登录");
            }
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                return Result.error("未登录");
            }

            // 管理员/超级管理员/运营可在商品管理中代商品所有者发起重新申请；卖方仅可对自己的商品重新申请；买方不可操作
            if (RoleCheckHelper.hasAnyRole(session, 3, 4, 8)) {
                AuctionGoods goods = goodsService.getById(id);
                if (goods == null || goods.getDelFlag() == 1) {
                    return Result.error("商品不存在");
                }
                goodsService.reapplyGoods(id, goods.getSellerId());
            } else if (RoleCheckHelper.hasAnyRole(session, 2)) {
                goodsService.reapplyGoods(id, userId);
            } else {
                return Result.error("无权限操作，仅卖方可重新申请上架自己的商品");
            }
            return Result.success("重新申请上架成功，等待管理员审核", null);
        } catch (Exception e) {
            return Result.error("重新申请上架失败：" + e.getMessage());
        }
    }
}
