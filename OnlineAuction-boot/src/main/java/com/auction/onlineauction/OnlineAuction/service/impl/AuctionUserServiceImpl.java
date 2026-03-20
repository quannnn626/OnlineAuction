package com.auction.onlineauction.OnlineAuction.service.impl;

import com.auction.onlineauction.OnlineAuction.dto.LoginDTO;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    public PageInfo<AuctionUser> getUserPage(Integer current, Integer size, String userName, Integer userRole, Integer userStatus, Long currentUserId) {
        PageHelper.startPage(current, size);

        QueryWrapper<AuctionUser> wrapper = new QueryWrapper<>();
        wrapper.eq("del_flag", 0);
        
        // 权限过滤：根据当前登录用户的角色过滤用户列表
        if (currentUserId != null) {
            AuctionUser currentUser = getById(currentUserId);
            if (currentUser != null && currentUser.getDelFlag() == 0) {
                String currentUserRole = currentUser.getUserRole();
                boolean isSuperAdmin = currentUserRole != null && currentUserRole.contains("4");
                boolean isAdmin = currentUserRole != null && currentUserRole.contains("3");
                
                boolean isCustomerService = currentUserRole != null && currentUserRole.contains("6");
                if (isSuperAdmin) {
                    // 超级管理员：可以查看所有用户（包括其他管理员/超级管理员）
                } else if (isAdmin || isCustomerService) {
                    // 管理员/客服：只能查看普通用户及运营岗位角色（1,2,5,6,7,8），不包括管理员和超级管理员
                    wrapper.and(w -> w
                        .notLike("user_role", "3")
                        .notLike("user_role", "4")
                        .and(w2 -> w2.like("user_role", "1").or().like("user_role", "2")
                            .or().like("user_role", "5").or().like("user_role", "6")
                            .or().like("user_role", "7").or().like("user_role", "8"))
                    );
                } else {
                    // 非管理员用户：不能查看用户列表（返回空列表）
                    wrapper.eq("id", -1L); // 设置一个不可能存在的ID，确保返回空列表
                }
            }
        }
        
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
    public List<Map<String, Object>> searchUsersForSelection(String keyword, int limit) {
        QueryWrapper<AuctionUser> wrapper = new QueryWrapper<>();
        wrapper.eq("del_flag", 0).select("id", "user_name", "nick_name");
        if (keyword != null && !keyword.trim().isEmpty()) {
            String kw = keyword.trim();
            wrapper.and(w -> w.like("user_name", kw).or().like("nick_name", kw));
        }
        wrapper.orderByDesc("id").last("LIMIT " + Math.min(Math.max(limit, 1), 50));
        List<AuctionUser> list = list(wrapper);
        return list.stream().map(u -> {
            Map<String, Object> m = new HashMap<>();
            m.put("userId", u.getId());
            m.put("userName", u.getUserName() != null ? u.getUserName() : "");
            m.put("nickName", u.getNickName() != null ? u.getNickName() : "");
            return m;
        }).collect(Collectors.toList());
    }

    @Override
    public PageInfo<AuctionUser> getSellerAuditPage(Integer current, Integer size, String userName, Integer sellerAuditStatus) {
        PageHelper.startPage(current, size);
        QueryWrapper<AuctionUser> wrapper = new QueryWrapper<>();
        wrapper.eq("del_flag", 0);
        // 仅查询有卖家资质记录的用户（已提交过申请或已审核）
        wrapper.ne("seller_audit_status", 0);
        if (sellerAuditStatus != null) {
            wrapper.eq("seller_audit_status", sellerAuditStatus);
        }
        if (userName != null && !userName.trim().isEmpty()) {
            wrapper.like("user_name", userName.trim());
        }
        wrapper.orderByDesc("seller_audit_apply_time");
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
    public AuctionUser createUser(AuctionUser user, Long currentUserId) {
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
        
        // 权限检查：只有管理员或超级管理员才能创建用户
        if (currentUserId == null) {
            throw new RuntimeException("未登录，无法创建用户");
        }
        
        AuctionUser currentUser = getById(currentUserId);
        if (currentUser == null || currentUser.getDelFlag() == 1) {
            throw new RuntimeException("当前用户不存在");
        }
        
        String currentUserRole = currentUser.getUserRole();
        boolean isSuperAdmin = currentUserRole != null && currentUserRole.contains("4");
        boolean isAdmin = currentUserRole != null && currentUserRole.contains("3");
        
        if (!isSuperAdmin && !isAdmin) {
            throw new RuntimeException("只有管理员或超级管理员才能创建用户");
        }
        
        // 验证角色值格式（支持单个或多个角色，用逗号分隔）
        String roleStr = user.getUserRole().trim();
        String[] roles = roleStr.split(",");
        for (String role : roles) {
            role = role.trim();
            if (!role.equals("1") && !role.equals("2") && !role.equals("3") && !role.equals("4")
                    && !role.equals("5") && !role.equals("6") && !role.equals("7") && !role.equals("8") && !role.equals("9")) {
                throw new RuntimeException("用户角色值无效（1=买方 2=卖方 3=管理员 4=超级管理员 5=拍卖师 6=客服 7=财务 8=运营 9=风控）");
            }
        }
        
        // 权限检查：管理员只能创建普通用户及运营岗位角色，超级管理员可以创建所有角色（含管理员）
        if (!isSuperAdmin) {
            for (String role : roles) {
                role = role.trim();
                if (role.equals("3") || role.equals("4")) {
                    throw new RuntimeException("管理员无法创建管理员或超级管理员账号");
                }
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
    public AuctionUser updateUser(Long id, AuctionUser user, Long currentUserId) {
        AuctionUser existingUser = getById(id);
        if (existingUser == null || existingUser.getDelFlag() == 1) {
            throw new RuntimeException("用户不存在");
        }
        
        // 设置ID
        user.setId(id);
        
        // 如果修改了角色，需要进行权限检查
        if (user.getUserRole() != null && !user.getUserRole().equals(existingUser.getUserRole())) {
            // 权限检查：只有管理员或超级管理员才能修改用户角色
            if (currentUserId == null) {
                throw new RuntimeException("未登录，无法修改用户角色");
            }
            
            AuctionUser currentUser = getById(currentUserId);
            if (currentUser == null || currentUser.getDelFlag() == 1) {
                throw new RuntimeException("当前用户不存在");
            }
            
            String currentUserRole = currentUser.getUserRole();
            boolean isSuperAdmin = currentUserRole != null && currentUserRole.contains("4");
            boolean isAdmin = currentUserRole != null && currentUserRole.contains("3");
            
            if (!isSuperAdmin && !isAdmin) {
                throw new RuntimeException("只有管理员或超级管理员才能修改用户角色");
            }
            
            // 验证角色值格式
            String roleStr = user.getUserRole().trim();
            String[] roles = roleStr.split(",");
            for (String role : roles) {
                role = role.trim();
                if (!role.equals("1") && !role.equals("2") && !role.equals("3") && !role.equals("4")
                        && !role.equals("5") && !role.equals("6") && !role.equals("7") && !role.equals("8") && !role.equals("9")) {
                    throw new RuntimeException("用户角色值无效（1=买方 2=卖方 3=管理员 4=超级管理员 5=拍卖师 6=客服 7=财务 8=运营 9=风控）");
                }
            }
            
            // 权限检查：管理员只能设置买方、卖方及运营岗位角色，超级管理员可以设置所有角色
            if (!isSuperAdmin) {
                for (String role : roles) {
                    role = role.trim();
                    if (role.equals("3") || role.equals("4")) {
                        throw new RuntimeException("管理员无法设置管理员或超级管理员角色");
                    }
                }
            }
            
            // 规范化角色字符串（去重、排序）
            user.setUserRole(String.join(",", roles));
        }
        
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
        
        // 检查是否为买方（1），只有买方才能申请成为卖方
        String userRole = user.getUserRole();
        if (userRole == null || !userRole.contains("1")) {
            throw new RuntimeException("只有买方用户才能申请成为卖方");
        }
        if (userRole.contains("2")) {
            throw new RuntimeException("您已是卖方用户，无需重复申请");
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
            // 审核通过：身份变为卖家（2），不再保留买方身份
            user.setSellerAuditRemark(null);
            user.setUserRole("2");
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

    @Override
    public LoginDTO login(String userName, String password, String loginIp) {
        // 验证参数
        if (userName == null || userName.trim().isEmpty()) {
            throw new RuntimeException("用户名不能为空");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new RuntimeException("密码不能为空");
        }
        
        // 查询用户
        QueryWrapper<AuctionUser> wrapper = new QueryWrapper<>();
        wrapper.eq("user_name", userName);
        wrapper.eq("del_flag", 0);
        AuctionUser user = getOne(wrapper);
        
        if (user == null) {
            throw new RuntimeException("用户名或密码错误");
        }
        
        // 验证密码（MD5加密后比较）
        String md5Password = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!md5Password.equals(user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }
        
        // 检查账号状态
        if (user.getUserStatus() != null && user.getUserStatus() == 1) {
            throw new RuntimeException("账号已被禁用，请联系管理员");
        }
        
        // 更新登录信息
        user.setLoginIp(loginIp);
        user.setLoginDate(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        updateById(user);
        
        // 构建登录响应DTO
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setId(user.getId());
        loginDTO.setUserName(user.getUserName());
        loginDTO.setNickName(user.getNickName());
        loginDTO.setAvatar(user.getAvatar());
        loginDTO.setUserRole(user.getUserRole());
        loginDTO.setUserStatus(user.getUserStatus());
        
        // 解析角色列表
        if (user.getUserRole() != null && !user.getUserRole().trim().isEmpty()) {
            List<String> roles = Arrays.stream(user.getUserRole().split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
            loginDTO.setRoles(roles);
            
            // 判断角色类型（3=管理员 4=超级管理员 5=拍卖师 6=客服 7=财务 8=运营 均可访问后台）
            boolean isSuperAdmin = roles.contains("4");
            boolean canAccessAdmin = roles.contains("3") || roles.contains("4")
                    || roles.contains("5") || roles.contains("6") || roles.contains("7") || roles.contains("8");
            boolean isNormalUser = roles.contains("1");

            loginDTO.setIsAdmin(canAccessAdmin);
            loginDTO.setIsSuperAdmin(isSuperAdmin);
            loginDTO.setIsBuyer(roles.contains("1"));
            loginDTO.setIsSeller(roles.contains("2"));
        } else {
            loginDTO.setRoles(Arrays.asList("1")); // 默认买方
            loginDTO.setIsAdmin(false);
            loginDTO.setIsSuperAdmin(false);
            loginDTO.setIsBuyer(true);
            loginDTO.setIsSeller(false);
        }
        
        return loginDTO;
    }

    @Override
    public AuctionUser updateProfile(Long userId, AuctionUser user) {
        AuctionUser existingUser = getById(userId);
        if (existingUser == null || existingUser.getDelFlag() == 1) {
            throw new RuntimeException("用户不存在");
        }

        // 只更新允许的字段
        if (user.getNickName() != null) {
            existingUser.setNickName(user.getNickName());
        }
        if (user.getRealName() != null) {
            existingUser.setRealName(user.getRealName());
        }
        if (user.getPhone() != null) {
            // 验证手机号是否已被其他用户使用
            QueryWrapper<AuctionUser> wrapper = new QueryWrapper<>();
            wrapper.eq("phone", user.getPhone());
            wrapper.ne("id", userId);
            wrapper.eq("del_flag", 0);
            long count = count(wrapper);
            if (count > 0) {
                throw new RuntimeException("该手机号已被其他用户使用");
            }
            existingUser.setPhone(user.getPhone());
        }
        if (user.getEmail() != null) {
            existingUser.setEmail(user.getEmail());
        }
        if (user.getSex() != null) {
            existingUser.setSex(user.getSex());
        }

        existingUser.setUpdateTime(LocalDateTime.now());
        boolean success = updateById(existingUser);
        if (!success) {
            throw new RuntimeException("更新失败");
        }

        return getUserByIdWithoutPassword(userId);
    }

    @Override
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        if (oldPassword == null || oldPassword.trim().isEmpty()) {
            throw new RuntimeException("旧密码不能为空");
        }
        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new RuntimeException("新密码不能为空");
        }
        if (newPassword.length() < 6) {
            throw new RuntimeException("新密码长度不能少于6位");
        }

        AuctionUser user = getById(userId);
        if (user == null || user.getDelFlag() == 1) {
            throw new RuntimeException("用户不存在");
        }

        // 验证旧密码
        String md5OldPassword = DigestUtils.md5DigestAsHex(oldPassword.getBytes());
        if (!md5OldPassword.equals(user.getPassword())) {
            throw new RuntimeException("旧密码错误");
        }

        // 更新密码
        String md5NewPassword = DigestUtils.md5DigestAsHex(newPassword.getBytes());
        user.setPassword(md5NewPassword);
        user.setUpdateTime(LocalDateTime.now());
        boolean success = updateById(user);
        if (!success) {
            throw new RuntimeException("修改密码失败");
        }
    }

    @Override
    public AuctionUser updateAvatar(Long userId, Long avatarFileId) {
        AuctionUser user = getById(userId);
        if (user == null || user.getDelFlag() == 1) {
            throw new RuntimeException("用户不存在");
        }

        // 验证文件是否存在
        if (avatarFileId != null) {
            // 这里可以添加文件存在性验证，如果需要的话
            // 暂时直接设置
            user.setAvatarFileId(avatarFileId);
            
            // 同时更新avatar字段（兼容旧数据）
            // 可以通过fileService获取文件路径，这里简化处理
            // user.setAvatar(filePath);
        } else {
            user.setAvatarFileId(null);
        }

        user.setUpdateTime(LocalDateTime.now());
        boolean success = updateById(user);
        if (!success) {
            throw new RuntimeException("更新头像失败");
        }

        return getUserByIdWithoutPassword(userId);
    }
}
