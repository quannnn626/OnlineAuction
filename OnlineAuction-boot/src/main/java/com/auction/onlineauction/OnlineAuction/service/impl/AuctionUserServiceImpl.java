package com.auction.onlineauction.OnlineAuction.service.impl;

import com.auction.onlineauction.OnlineAuction.entity.AuctionUser;
import com.auction.onlineauction.OnlineAuction.mapper.AuctionUserMapper;
import com.auction.onlineauction.OnlineAuction.service.IAuctionUserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 系统用户表 服务实现类
 * </p>
 *
 * @author MrYan
 * @since 2025-12-31
 */
@Service
public class AuctionUserServiceImpl extends ServiceImpl<AuctionUserMapper, AuctionUser> implements IAuctionUserService {

    @Override
    public PageInfo<AuctionUser> getUserPage(Integer current, Integer size, String userName, Integer userRole, Integer userStatus) {
        PageHelper.startPage(current, size);

        QueryWrapper<AuctionUser> wrapper = new QueryWrapper<>();
        wrapper.eq("del_flag", 0);
        
        if (userName != null && !userName.trim().isEmpty()) {
            wrapper.like("user_name", userName);
        }
        if (userRole != null) {
            // 支持多角色查询：如果 user_role 包含该角色值，则匹配
            wrapper.like("user_role", String.valueOf(userRole));
        }
        if (userStatus != null) {
            wrapper.eq("user_status", userStatus);
        }
        
        wrapper.orderByDesc("create_time");
        List<AuctionUser> list = list(wrapper);
        return new PageInfo<>(list);
    }

    @Override
    public AuctionUser getUserByIdWithoutPassword(Long id) {
        AuctionUser user = getById(id);
        if (user == null || user.getDelFlag() == 1) {
            throw new RuntimeException("用户不存在");
        }
        // 不返回密码
        user.setPassword(null);
        return user;
    }

    @Override
    public AuctionUser createUser(AuctionUser user) {
        // 验证必填字段
        if (user.getUserName() == null || user.getUserName().trim().isEmpty()) {
            throw new RuntimeException("用户名不能为空");
        }
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            throw new RuntimeException("密码不能为空");
        }
        if (user.getPhone() == null || user.getPhone().trim().isEmpty()) {
            throw new RuntimeException("手机号不能为空");
        }
        if (user.getUserRole() == null || user.getUserRole().trim().isEmpty()) {
            // 默认设置为买方用户
            user.setUserRole("1");
        }
        
        // 验证角色值格式（支持单个或多个角色，用逗号分隔）
        String roleStr = user.getUserRole().trim();
        String[] roles = roleStr.split(",");
        for (String role : roles) {
            role = role.trim();
            if (!role.equals("1") && !role.equals("2") && !role.equals("3")) {
                throw new RuntimeException("用户角色值无效（1=买方，2=卖方，3=管理员）");
            }
        }
        // 规范化角色字符串（去重、排序）
        user.setUserRole(String.join(",", roles));
        
        // 检查用户名是否已存在
        QueryWrapper<AuctionUser> userNameWrapper = new QueryWrapper<>();
        userNameWrapper.eq("user_name", user.getUserName());
        userNameWrapper.eq("del_flag", 0);
        if (count(userNameWrapper) > 0) {
            throw new RuntimeException("用户名已存在");
        }
        
        // 检查手机号是否已存在
        QueryWrapper<AuctionUser> phoneWrapper = new QueryWrapper<>();
        phoneWrapper.eq("phone", user.getPhone());
        phoneWrapper.eq("del_flag", 0);
        if (count(phoneWrapper) > 0) {
            throw new RuntimeException("手机号已存在");
        }
        
        // 密码MD5加密
        String md5Password = DigestUtils.md5DigestAsHex(user.getPassword().getBytes());
        user.setPassword(md5Password);
        
        // 设置默认值
        if (user.getUserStatus() == null) {
            user.setUserStatus(0); // 默认正常状态
        }
        if (user.getSellerAuditStatus() == null) {
            user.setSellerAuditStatus(0); // 默认未提交
        }
        if (user.getSex() == null) {
            user.setSex("2"); // 默认未知
        }
        
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        user.setDelFlag(0);
        
        boolean success = save(user);
        if (!success) {
            throw new RuntimeException("创建失败");
        }
        
        // 不返回密码
        user.setPassword(null);
        return user;
    }

    @Override
    public AuctionUser updateUser(Long id, AuctionUser user) {
        AuctionUser existingUser = getById(id);
        if (existingUser == null || existingUser.getDelFlag() == 1) {
            throw new RuntimeException("用户不存在");
        }
        
        // 设置ID
        user.setId(id);
        
        // 如果密码不为空，则加密更新
        if (user.getPassword() != null && !user.getPassword().trim().isEmpty()) {
            String md5Password = DigestUtils.md5DigestAsHex(user.getPassword().getBytes());
            user.setPassword(md5Password);
        } else {
            // 密码为空时不更新密码
            user.setPassword(null);
        }
        
        // 检查用户名是否与其他用户重复
        if (user.getUserName() != null && !user.getUserName().equals(existingUser.getUserName())) {
            QueryWrapper<AuctionUser> userNameWrapper = new QueryWrapper<>();
            userNameWrapper.eq("user_name", user.getUserName());
            userNameWrapper.eq("del_flag", 0);
            userNameWrapper.ne("id", id);
            if (count(userNameWrapper) > 0) {
                throw new RuntimeException("用户名已存在");
            }
        }
        
        // 检查手机号是否与其他用户重复
        if (user.getPhone() != null && !user.getPhone().equals(existingUser.getPhone())) {
            QueryWrapper<AuctionUser> phoneWrapper = new QueryWrapper<>();
            phoneWrapper.eq("phone", user.getPhone());
            phoneWrapper.eq("del_flag", 0);
            phoneWrapper.ne("id", id);
            if (count(phoneWrapper) > 0) {
                throw new RuntimeException("手机号已存在");
            }
        }
        
        // 保留创建时间，更新修改时间
        user.setCreateTime(existingUser.getCreateTime());
        user.setUpdateTime(LocalDateTime.now());
        // 不更新删除标志
        user.setDelFlag(null);
        
        boolean success = updateById(user);
        if (!success) {
            throw new RuntimeException("更新失败");
        }
        
        AuctionUser updatedUser = getById(id);
        updatedUser.setPassword(null);
        return updatedUser;
    }

    @Override
    public void updateUserStatus(Long id, Integer userStatus) {
        AuctionUser user = getById(id);
        if (user == null || user.getDelFlag() == 1) {
            throw new RuntimeException("用户不存在");
        }
        
        if (userStatus != 0 && userStatus != 1) {
            throw new RuntimeException("账号状态值无效（0=正常，1=禁用）");
        }
        
        user.setUserStatus(userStatus);
        user.setUpdateTime(LocalDateTime.now());
        
        boolean success = updateById(user);
        if (!success) {
            throw new RuntimeException("操作失败");
        }
    }

    @Override
    public void applySeller(Long id, String certificateFiles) {
        AuctionUser user = getById(id);
        if (user == null || user.getDelFlag() == 1) {
            throw new RuntimeException("用户不存在");
        }
        
        // 检查用户角色是否包含买方（1）
        String userRole = user.getUserRole();
        if (userRole == null || !userRole.contains("1")) {
            throw new RuntimeException("只有买方用户可以申请成为卖方用户");
        }
        
        // 检查是否已经是卖方用户
        if (userRole.contains("2")) {
            throw new RuntimeException("您已经是卖方用户，无需重复申请");
        }
        
        // 检查是否已有待审核的申请
        if (user.getSellerAuditStatus() != null && user.getSellerAuditStatus() == 1) {
            throw new RuntimeException("您已有待审核的申请，请等待审核结果");
        }
        
        // 设置申请信息
        user.setSellerAuditStatus(1); // 待审核
        user.setSellerCertificateFiles(certificateFiles);
        user.setSellerAuditApplyTime(LocalDateTime.now());
        user.setSellerAuditRemark(null); // 清空之前的驳回原因
        user.setUpdateTime(LocalDateTime.now());
        
        boolean success = updateById(user);
        if (!success) {
            throw new RuntimeException("申请失败");
        }
    }

    @Override
    public void auditSeller(Long id, Integer auditStatus, String auditRemark, Long auditUserId) {
        AuctionUser user = getById(id);
        if (user == null || user.getDelFlag() == 1) {
            throw new RuntimeException("用户不存在");
        }
        
        // 检查是否有待审核的申请
        if (user.getSellerAuditStatus() == null || user.getSellerAuditStatus() != 1) {
            throw new RuntimeException("该用户没有待审核的申请");
        }
        
        // 审核状态：2=审核通过 3=审核驳回
        if (auditStatus != 2 && auditStatus != 3) {
            throw new RuntimeException("审核状态值无效（2=审核通过，3=审核驳回）");
        }
        
        user.setSellerAuditStatus(auditStatus);
        user.setSellerAuditTime(LocalDateTime.now());
        if (auditUserId != null) {
            user.setSellerAuditUserId(auditUserId);
        }
        
        if (auditStatus == 3) {
            // 审核驳回
            if (auditRemark == null || auditRemark.trim().isEmpty()) {
                throw new RuntimeException("审核驳回时必须填写驳回原因");
            }
            user.setSellerAuditRemark(auditRemark);
        } else if (auditStatus == 2) {
            // 审核通过：更新用户角色，添加卖方身份（2）
            String currentRole = user.getUserRole();
            if (currentRole == null || currentRole.trim().isEmpty()) {
                currentRole = "1"; // 默认买方
            }
            
            // 如果角色中不包含2（卖方），则添加
            if (!currentRole.contains("2")) {
                if (currentRole.equals("1")) {
                    user.setUserRole("1,2"); // 买方+卖方
                } else {
                    user.setUserRole(currentRole + ",2");
                }
            }
            
            // 清空驳回原因
            user.setSellerAuditRemark(null);
        }
        
        user.setUpdateTime(LocalDateTime.now());
        
        boolean success = updateById(user);
        if (!success) {
            throw new RuntimeException("操作失败");
        }
    }

    @Override
    public void deleteUser(Long id) {
        AuctionUser user = getById(id);
        if (user == null || user.getDelFlag() == 1) {
            throw new RuntimeException("用户不存在");
        }
        
        // 逻辑删除
        user.setDelFlag(1);
        user.setUpdateTime(LocalDateTime.now());
        
        boolean success = updateById(user);
        if (!success) {
            throw new RuntimeException("删除失败");
        }
    }
}
