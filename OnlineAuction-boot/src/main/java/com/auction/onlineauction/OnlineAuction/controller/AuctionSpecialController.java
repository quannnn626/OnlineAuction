package com.auction.onlineauction.OnlineAuction.controller;

import com.auction.onlineauction.OnlineAuction.common.Result;
import com.auction.onlineauction.OnlineAuction.common.RoleCheckHelper;
import com.auction.onlineauction.OnlineAuction.entity.AuctionGoods;
import com.auction.onlineauction.OnlineAuction.entity.AuctionSpecial;
import com.auction.onlineauction.OnlineAuction.service.IAuctionGoodsService;
import com.auction.onlineauction.OnlineAuction.service.IAuctionSpecialService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * 专场管理：拍卖专场新增、编辑、删除，商品加入/移出专场
 * 权限：管理员、超级管理员、运营
 */
@RestController
@RequestMapping("/api/OnlineAuction/auctionSpecial")
public class AuctionSpecialController {

    @Autowired
    private IAuctionSpecialService specialService;
    @Autowired
    private IAuctionGoodsService goodsService;

    @GetMapping("/list")
    public Result<List<AuctionSpecial>> list(HttpServletRequest request) {
        try {
            if (!RoleCheckHelper.canViewSpecial(request.getSession(false))) {
                return Result.error("未登录");
            }
            QueryWrapper<AuctionSpecial> q = new QueryWrapper<>();
            q.eq("del_flag", 0);
            // 普通用户仅查看启用的专场；运营/管理员可查看全部（含禁用）
            if (!RoleCheckHelper.canManageSpecial(request.getSession(false))) {
                q.eq("status", 1);
            }
            q.orderByAsc("sort_order");
            return Result.success("查询成功", specialService.list(q));
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public Result<AuctionSpecial> getById(@PathVariable Long id, HttpServletRequest request) {
        try {
            if (!RoleCheckHelper.canViewSpecial(request.getSession(false))) {
                return Result.error("未登录");
            }
            AuctionSpecial s = specialService.getById(id);
            if (s == null || s.getDelFlag() == 1) {
                return Result.error("专场不存在");
            }
            // 普通用户仅可查看启用专场
            if (!RoleCheckHelper.canManageSpecial(request.getSession(false)) && (s.getStatus() == null || s.getStatus() != 1)) {
                return Result.error("专场不存在");
            }
            return Result.success("查询成功", s);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    @GetMapping("/{id}/goods")
    public Result<List<Map<String, Object>>> listGoods(@PathVariable Long id, HttpServletRequest request) {
        try {
            if (!RoleCheckHelper.canViewSpecial(request.getSession(false))) {
                return Result.error("未登录");
            }
            // 普通用户：专场需启用
            if (!RoleCheckHelper.canManageSpecial(request.getSession(false))) {
                AuctionSpecial s = specialService.getById(id);
                if (s == null || s.getDelFlag() == 1 || s.getStatus() == null || s.getStatus() != 1) {
                    return Result.error("专场不存在");
                }
            }
            return Result.success("查询成功", specialService.listGoodsBySpecialId(id));
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    @PostMapping
    public Result<AuctionSpecial> add(@RequestBody AuctionSpecial special, HttpServletRequest request) {
        try {
            if (!RoleCheckHelper.canManageSpecial(request.getSession(false))) {
                return Result.error("无权限新增专场");
            }
            if (special.getSpecialName() == null || special.getSpecialName().trim().isEmpty()) {
                return Result.error("专场名称不能为空");
            }
            if (special.getStatus() == null) special.setStatus(0);
            if (special.getSortOrder() == null) special.setSortOrder(0);
            special.setDelFlag(0);
            specialService.save(special);
            return Result.success("新增成功", special);
        } catch (Exception e) {
            return Result.error("新增失败：" + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public Result<AuctionSpecial> update(@PathVariable Long id, @RequestBody AuctionSpecial special, HttpServletRequest request) {
        try {
            if (!RoleCheckHelper.canManageSpecial(request.getSession(false))) {
                return Result.error("无权限编辑专场");
            }
            AuctionSpecial exist = specialService.getById(id);
            if (exist == null || exist.getDelFlag() == 1) {
                return Result.error("专场不存在");
            }
            if (special.getSpecialName() != null) exist.setSpecialName(special.getSpecialName().trim());
            if (special.getSpecialDesc() != null) exist.setSpecialDesc(special.getSpecialDesc());
            if (special.getCategoryId() != null) exist.setCategoryId(special.getCategoryId());
            if (special.getSortOrder() != null) exist.setSortOrder(special.getSortOrder());
            if (special.getStatus() != null) exist.setStatus(special.getStatus());
            specialService.updateById(exist);
            return Result.success("更新成功", exist);
        } catch (Exception e) {
            return Result.error("更新失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        try {
            if (!RoleCheckHelper.canManageSpecial(request.getSession(false))) {
                return Result.error("无权限删除专场");
            }
            AuctionSpecial exist = specialService.getById(id);
            if (exist == null || exist.getDelFlag() == 1) {
                return Result.error("专场不存在");
            }
            exist.setDelFlag(1);
            specialService.updateById(exist);
            return Result.success("删除成功", null);
        } catch (Exception e) {
            return Result.error("删除失败：" + e.getMessage());
        }
    }

    @PostMapping("/{id}/goods")
    public Result<Void> addGoods(@PathVariable Long id, @RequestBody Map<String, Object> body, HttpServletRequest request) {
        try {
            if (!RoleCheckHelper.canManageSpecial(request.getSession(false))) {
                return Result.error("无权限操作专场商品");
            }
            Long goodsId = body.get("goodsId") != null ? Long.valueOf(body.get("goodsId").toString()) : null;
            Integer sortOrder = body.get("sortOrder") != null ? Integer.valueOf(body.get("sortOrder").toString()) : 0;
            if (goodsId == null) return Result.error("商品ID不能为空");
            AuctionSpecial special = specialService.getById(id);
            if (special == null || special.getDelFlag() == 1) return Result.error("专场不存在");
            if (special.getCategoryId() != null) {
                AuctionGoods goods = goodsService.getById(goodsId);
                if (goods == null) return Result.error("商品不存在");
                String goodsCat = goods.getCategoryId();
                String specialCat = String.valueOf(special.getCategoryId());
                if (goodsCat == null || goodsCat.trim().isEmpty()) {
                    return Result.error("该商品未设置分类，仅能添加本专场所选分类下的商品");
                }
                boolean match = false;
                for (String part : goodsCat.split(",")) {
                    if (part.trim().equals(specialCat)) { match = true; break; }
                }
                if (!match) {
                    return Result.error("该商品不属于本专场所选分类，仅能添加该分类下的商品");
                }
            }
            specialService.addGoods(id, goodsId, sortOrder);
            return Result.success("添加成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}/goods/{goodsId}")
    public Result<Void> removeGoods(@PathVariable Long id, @PathVariable Long goodsId, HttpServletRequest request) {
        try {
            if (!RoleCheckHelper.canManageSpecial(request.getSession(false))) {
                return Result.error("无权限操作专场商品");
            }
            specialService.removeGoods(id, goodsId);
            return Result.success("移出成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
