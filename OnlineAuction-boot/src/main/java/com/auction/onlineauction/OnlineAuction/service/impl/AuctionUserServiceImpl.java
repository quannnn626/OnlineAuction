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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

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

    /** 后台岗位角色：与前台路由、权限判断保持一致 */
    private static final Set<String> STAFF_ROLE_IDS;
    static {
        Set<String> s = new HashSet<>();
        Collections.addAll(s, "3", "4", "5", "6", "7", "8", "9", "10");
        STAFF_ROLE_IDS = Collections.unmodifiableSet(s);
    }

    private static boolean hasStaffRole(List<String> roles) {
        if (roles == null) {
            return false;
        }
        for (String r : roles) {
            if (r != null && STAFF_ROLE_IDS.contains(r.trim())) {
                return true;
            }
        }
        return false;
    }

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
                    // 管理员/客服：只能查看普通用户及运营岗位角色，不包括管理员和超级管理员
                    wrapper.and(w -> w
                        .notLike("user_role", "3")
                        .notLike("user_role", "4")
                        .and(w2 -> w2.like("user_role", "1").or().like("user_role", "2")
                            .or().like("user_role", "5").or().like("user_role", "6")
                            .or().like("user_role", "7").or().like("user_role", "8")
                            .or().like("user_role", "9").or().like("user_role", "10"))
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
                    && !role.equals("5") && !role.equals("6") && !role.equals("7") && !role.equals("8")
                    && !role.equals("9") && !role.equals("10")) {
                throw new RuntimeException("用户角色值无效（1=买方 2=卖方 3=管理员 4=超级管理员 5=拍卖师 6=客服 7=财务 8=运营 9=风控 10=审计）");
            }
        }
        
        // 权限检查：
        // - 管理员：不能创建管理员/超级管理员
        // - 超级管理员：仅额外允许创建管理员，不允许创建超级管理员
        if (!isSuperAdmin) {
            for (String role : roles) {
                role = role.trim();
                if (role.equals("3") || role.equals("4")) {
                    throw new RuntimeException("管理员无法创建管理员或超级管理员账号");
                }
            }
        } else {
            for (String role : roles) {
                role = role.trim();
                if (role.equals("4")) {
                    throw new RuntimeException("超级管理员不能创建超级管理员账号");
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
                        && !role.equals("5") && !role.equals("6") && !role.equals("7") && !role.equals("8")
                        && !role.equals("9") && !role.equals("10")) {
                    throw new RuntimeException("用户角色值无效（1=买方 2=卖方 3=管理员 4=超级管理员 5=拍卖师 6=客服 7=财务 8=运营 9=风控 10=审计）");
                }
            }
            
            // 权限检查：
            // - 管理员：不能设置管理员/超级管理员
            // - 超级管理员：仅额外允许设置管理员，不允许设置超级管理员
            if (!isSuperAdmin) {
                for (String role : roles) {
                    role = role.trim();
                    if (role.equals("3") || role.equals("4")) {
                        throw new RuntimeException("管理员无法设置管理员或超级管理员角色");
                    }
                }
            } else {
                for (String role : roles) {
                    role = role.trim();
                    if (role.equals("4")) {
                        throw new RuntimeException("超级管理员不能设置超级管理员角色");
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
            
            // 判断角色类型（3～10 为后台岗位，可访问管理端）
            boolean isSuperAdmin = roles.contains("4");
            boolean canAccessAdmin = hasStaffRole(roles);

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
    public LoginDTO loginForPublicPortal(String userName, String password, String loginIp) {
        LoginDTO dto = login(userName, password, loginIp);
        if (hasStaffRole(dto.getRoles())) {
            throw new RuntimeException("该账号为后台管理账号，请使用后台登录入口（/admin/login）");
        }
        return dto;
    }

    @Override
    public LoginDTO loginForAdminPortal(String userName, String password, String loginIp) {
        LoginDTO dto = login(userName, password, loginIp);
        if (!hasStaffRole(dto.getRoles())) {
            throw new RuntimeException("请使用前台用户登录入口（/login）");
        }
        return dto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void registerPublicUser(String userName, String password, String phone, String nickName) {
        if (userName == null || userName.trim().isEmpty()) {
            throw new RuntimeException("用户名不能为空");
        }
        userName = userName.trim();
        if (userName.length() < 3 || userName.length() > 32) {
            throw new RuntimeException("用户名长度应为 3～32 个字符");
        }
        if (!userName.matches("^[a-zA-Z0-9_]+$")) {
            throw new RuntimeException("用户名仅允许字母、数字、下划线");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new RuntimeException("密码不能为空");
        }
        if (password.length() < 6) {
            throw new RuntimeException("密码长度不能少于6位");
        }
        if (phone == null || phone.trim().isEmpty()) {
            throw new RuntimeException("手机号不能为空");
        }
        phone = phone.trim();
        if (!phone.matches("^1\\d{10}$")) {
            throw new RuntimeException("请输入正确的11位手机号");
        }

        QueryWrapper<AuctionUser> userNameWrapper = new QueryWrapper<>();
        userNameWrapper.eq("user_name", userName);
        userNameWrapper.eq("del_flag", 0);
        if (count(userNameWrapper) > 0) {
            throw new RuntimeException("用户名已存在");
        }

        QueryWrapper<AuctionUser> phoneWrapper = new QueryWrapper<>();
        phoneWrapper.eq("phone", phone);
        phoneWrapper.eq("del_flag", 0);
        if (count(phoneWrapper) > 0) {
            throw new RuntimeException("手机号已被注册");
        }

        AuctionUser u = new AuctionUser();
        u.setUserName(userName);
        u.setPassword(DigestUtils.md5DigestAsHex(password.getBytes()));
        u.setPhone(phone);
        String nn = (nickName != null && !nickName.trim().isEmpty()) ? nickName.trim() : userName;
        if (nn.length() > 50) {
            throw new RuntimeException("昵称过长");
        }
        u.setNickName(nn);
        u.setUserRole("1");
        u.setUserStatus(0);
        u.setSellerAuditStatus(0);
        u.setSex("2");
        u.setDelFlag(0);
        u.setRiskLevel(0);
        u.setCreateTime(LocalDateTime.now());
        u.setUpdateTime(LocalDateTime.now());
        if (!save(u)) {
            throw new RuntimeException("注册失败，请稍后重试");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginDTO loginOrRegisterByWechat(String wxOpenid, String phone, String nickName, String avatar, String loginIp) {
        if (wxOpenid == null || wxOpenid.trim().isEmpty()) {
            throw new RuntimeException("微信标识不能为空");
        }
        if (phone == null || phone.trim().isEmpty()) {
            throw new RuntimeException("手机号不能为空");
        }
        wxOpenid = wxOpenid.trim();
        phone = phone.trim();
        if (!phone.matches("^1\\d{10}$")) {
            throw new RuntimeException("请输入正确的11位手机号");
        }

        QueryWrapper<AuctionUser> wxWrapper = new QueryWrapper<>();
        wxWrapper.eq("wx_openid", wxOpenid);
        wxWrapper.eq("del_flag", 0);
        AuctionUser user = getOne(wxWrapper);

        // username 规则：wx_openid + 手机号（用于区分不同微信账号）
        String baseUserName = "wx_" + wxOpenid.replaceAll("[^a-zA-Z0-9_]", "") + "_" + phone;
        if (baseUserName.length() > 50) {
            baseUserName = baseUserName.substring(0, 50);
        }

        if (user == null) {
            QueryWrapper<AuctionUser> phoneWrapper = new QueryWrapper<>();
            phoneWrapper.eq("phone", phone);
            phoneWrapper.eq("del_flag", 0);
            AuctionUser phoneUser = getOne(phoneWrapper);

            if (phoneUser != null) {
                if (phoneUser.getWxOpenid() != null && !phoneUser.getWxOpenid().trim().isEmpty()
                        && !wxOpenid.equals(phoneUser.getWxOpenid())) {
                    throw new RuntimeException("手机号已绑定其他微信账号");
                }
                phoneUser.setWxOpenid(wxOpenid);
                if (nickName != null && !nickName.trim().isEmpty()) {
                    phoneUser.setNickName(nickName.trim());
                }
                if (avatar != null && !avatar.trim().isEmpty()) {
                    phoneUser.setAvatar(avatar.trim());
                }
                phoneUser.setUserRole("1");

                // 绑定成功后，更新 username 规则（wx_openid + phone）
                String finalUserName = baseUserName;
                int suffix = 1;
                while (true) {
                    QueryWrapper<AuctionUser> userNameWrapper = new QueryWrapper<>();
                    userNameWrapper.eq("user_name", finalUserName);
                    userNameWrapper.eq("del_flag", 0);
                    AuctionUser existed = getOne(userNameWrapper);
                    if (existed == null || (existed.getId() != null && existed.getId().equals(phoneUser.getId()))) {
                        break;
                    }
                    String suffixStr = "_" + suffix++;
                    int keepLen = Math.max(1, 50 - suffixStr.length());
                    finalUserName = baseUserName.substring(0, Math.min(baseUserName.length(), keepLen)) + suffixStr;
                }
                phoneUser.setUserName(finalUserName);

                phoneUser.setUpdateTime(LocalDateTime.now());
                updateById(phoneUser);
                user = phoneUser;
            } else {
                AuctionUser u = new AuctionUser();
                String finalUserName = baseUserName;
                int suffix = 1;
                while (true) {
                    QueryWrapper<AuctionUser> userNameWrapper = new QueryWrapper<>();
                    userNameWrapper.eq("user_name", finalUserName);
                    userNameWrapper.eq("del_flag", 0);
                    if (count(userNameWrapper) == 0) {
                        break;
                    }
                    String suffixStr = "_" + suffix++;
                    int keepLen = Math.max(1, 50 - suffixStr.length());
                    finalUserName = baseUserName.substring(0, Math.min(baseUserName.length(), keepLen)) + suffixStr;
                }

                u.setUserName(finalUserName);
                // 小程序首次自动注册：先不设置密码（password 为空；前端引导设置）
                u.setPassword("");
                u.setPhone(phone);
                u.setWxOpenid(wxOpenid);
                String nn = (nickName != null && !nickName.trim().isEmpty()) ? nickName.trim() : "微信用户";
                if (nn.length() > 50) {
                    nn = nn.substring(0, 50);
                }
                u.setNickName(nn);
                if (avatar != null && !avatar.trim().isEmpty()) {
                    u.setAvatar(avatar.trim());
                }
                u.setUserRole("1");
                u.setUserStatus(0);
                u.setSellerAuditStatus(0);
                u.setSex("2");
                u.setDelFlag(0);
                u.setRiskLevel(0);
                u.setCreateTime(LocalDateTime.now());
                u.setUpdateTime(LocalDateTime.now());
                if (!save(u)) {
                    throw new RuntimeException("微信登录注册失败，请稍后重试");
                }
                user = u;
            }
        } else {
            // wx_openid 存在：确保手机号一致（否则拒绝，避免一个手机号被绑定多个微信）
            if (phone != null && !phone.trim().isEmpty()) {
                String existedPhone = user.getPhone();
                if (existedPhone != null && !existedPhone.trim().isEmpty() && !phone.equals(existedPhone.trim())) {
                    throw new RuntimeException("该手机号已绑定其他微信账号");
                }
                if (existedPhone == null || existedPhone.trim().isEmpty()) {
                    user.setPhone(phone);
                }
            }

            // 更新昵称/头像（可选）
            if (nickName != null && !nickName.trim().isEmpty()) {
                user.setNickName(nickName.trim());
            }
            if (avatar != null && !avatar.trim().isEmpty()) {
                user.setAvatar(avatar.trim());
            }

            // 更新 username 规则：wx_openid + phone
            if (user.getPhone() != null && !user.getPhone().trim().isEmpty()) {
                String desired = "wx_" + wxOpenid.replaceAll("[^a-zA-Z0-9_]", "") + "_" + user.getPhone().trim();
                if (desired.length() > 50) desired = desired.substring(0, 50);
                if (!desired.equals(user.getUserName())) {
                    user.setUserName(desired);
                }
            }

            user.setUpdateTime(LocalDateTime.now());
            updateById(user);
        }

        if (user.getUserStatus() != null && user.getUserStatus() == 1) {
            throw new RuntimeException("账号已被禁用，请联系管理员");
        }
        user.setLoginIp(loginIp);
        user.setLoginDate(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        updateById(user);

        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setId(user.getId());
        loginDTO.setUserName(user.getUserName());
        loginDTO.setNickName(user.getNickName());
        loginDTO.setAvatar(user.getAvatar());
        loginDTO.setUserRole("1");
        loginDTO.setUserStatus(user.getUserStatus());
        loginDTO.setRoles(Arrays.asList("1"));
        loginDTO.setIsAdmin(false);
        loginDTO.setIsSuperAdmin(false);
        loginDTO.setIsBuyer(true);
        loginDTO.setIsSeller(false);

        boolean needSetPassword = user.getPassword() == null || user.getPassword().trim().isEmpty();
        loginDTO.setNeedSetPassword(needSetPassword);
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
    @Transactional(rollbackFor = Exception.class)
    public void setPassword(Long userId, String newPassword) {
        if (userId == null) {
            throw new RuntimeException("用户不存在");
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

        String md5NewPassword = DigestUtils.md5DigestAsHex(newPassword.getBytes());
        user.setPassword(md5NewPassword);
        user.setUpdateTime(LocalDateTime.now());
        boolean success = updateById(user);
        if (!success) {
            throw new RuntimeException("设置密码失败");
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchUpdateRiskLevelForRisk(List<Long> userIds, Integer riskLevel) {
        if (userIds == null || userIds.isEmpty()) {
            throw new RuntimeException("请选择用户");
        }
        if (riskLevel == null) {
            throw new RuntimeException("风险等级不能为空");
        }
        if (riskLevel < 0) riskLevel = 0;
        if (riskLevel > 3) riskLevel = 3;
        LinkedHashSet<Long> unique = new LinkedHashSet<>(userIds);
        List<AuctionUser> users = listByIds(new ArrayList<>(unique));
        List<AuctionUser> toUpdate = new ArrayList<>();
        for (AuctionUser u : users) {
            if (u.getDelFlag() != null && u.getDelFlag() == 1) {
                continue;
            }
            u.setRiskLevel(riskLevel);
            toUpdate.add(u);
        }
        if (toUpdate.isEmpty()) {
            throw new RuntimeException("未找到可更新的用户");
        }
        updateBatchById(toUpdate);
    }

    @Override
    public List<Map<String, Object>> listUsersWithRiskActivityForRisk() {
        return baseMapper.selectUsersWithRiskActivityForRisk();
    }
}
