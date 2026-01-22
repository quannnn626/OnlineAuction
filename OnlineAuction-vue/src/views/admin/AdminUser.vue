<template>
  <div class="admin-user-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>用户账号管理</h2>
      <div class="header-actions">
        <el-button type="primary" icon="el-icon-plus" @click="handleAdd">
          创建用户
        </el-button>
      </div>
    </div>

    <!-- 搜索和筛选区域 -->
    <div class="filter-section">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-input
            v-model="searchForm.userName"
            placeholder="搜索用户名"
            clearable
            @keyup.enter.native="handleSearch"
          >
            <el-button
              slot="append"
              icon="el-icon-search"
              @click="handleSearch"
            ></el-button>
          </el-input>
        </el-col>
        <el-col :span="4">
          <el-select
            v-model="searchForm.userRole"
            placeholder="用户角色"
            clearable
            @change="handleSearch"
          >
            <el-option label="全部" value=""></el-option>
            <el-option label="买方用户" :value="1"></el-option>
            <el-option label="卖方用户" :value="2"></el-option>
            <el-option label="管理员" :value="3"></el-option>
            <el-option label="超级管理员" :value="4"></el-option>
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-select
            v-model="searchForm.userStatus"
            placeholder="账号状态"
            clearable
            @change="handleSearch"
          >
            <el-option label="全部" value=""></el-option>
            <el-option label="正常" :value="0"></el-option>
            <el-option label="禁用" :value="1"></el-option>
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-button type="info" icon="el-icon-refresh" @click="handleReset"
            >重置</el-button
          >
        </el-col>
      </el-row>
    </div>

    <!-- 用户表格 -->
    <div class="table-section">
      <el-table
        v-loading="loading"
        :data="tableData"
        stripe
        style="width: 100%"
      >
        <el-table-column type="selection" width="55"></el-table-column>
        <el-table-column prop="id" label="ID" width="80"></el-table-column>
        <el-table-column prop="userName" label="用户名" width="150"></el-table-column>
        <el-table-column prop="realName" label="真实姓名" width="120"></el-table-column>
        <el-table-column prop="nickName" label="昵称" width="120"></el-table-column>
        <el-table-column prop="phone" label="手机号" width="130"></el-table-column>
        <el-table-column prop="email" label="邮箱" width="180"></el-table-column>
        <el-table-column prop="userRole" label="角色" width="120">
          <template slot-scope="scope">
            <el-tag :type="getRoleTagType(scope.row.userRole)">
              {{ getRoleText(scope.row.userRole) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="userStatus" label="账号状态" width="100">
          <template slot-scope="scope">
            <el-tag :type="scope.row.userStatus === 0 ? 'success' : 'danger'">
              {{ scope.row.userStatus === 0 ? "正常" : "禁用" }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          v-if="showSellerAuditColumn"
          prop="sellerAuditStatus"
          label="卖方资质"
          width="120"
        >
          <template slot-scope="scope">
            <el-tag
              v-if="scope.row.userRole === 2"
              :type="getSellerAuditTagType(scope.row.sellerAuditStatus)"
            >
              {{ getSellerAuditText(scope.row.sellerAuditStatus) }}
            </el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160">
          <template slot-scope="scope">
            {{ formatDateTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="300" fixed="right">
          <template slot-scope="scope">
            <el-button
              size="mini"
              type="primary"
              icon="el-icon-edit"
              @click="handleEdit(scope.row)"
            >
              编辑
            </el-button>
            <el-button
              size="mini"
              :type="scope.row.userStatus === 0 ? 'warning' : 'success'"
              :icon="scope.row.userStatus === 0 ? 'el-icon-lock' : 'el-icon-unlock'"
              @click="handleToggleStatus(scope.row)"
            >
              {{ scope.row.userStatus === 0 ? "禁用" : "恢复" }}
            </el-button>
            <el-button
              v-if="scope.row.userRole === 2 && scope.row.sellerAuditStatus === 1"
              size="mini"
              type="success"
              icon="el-icon-check"
              @click="handleAuditSeller(scope.row)"
            >
              审核
            </el-button>
            <el-button
              size="mini"
              type="danger"
              icon="el-icon-delete"
              @click="handleDelete(scope.row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 分页 -->
    <div class="pagination-section">
      <el-pagination
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="pagination.current"
        :page-sizes="[10, 20, 50, 100]"
        :page-size="pagination.size"
        layout="total, sizes, prev, pager, next, jumper"
        :total="pagination.total"
      >
      </el-pagination>
    </div>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      :title="dialogTitle"
      :visible.sync="dialogVisible"
      width="600px"
      @close="handleDialogClose"
    >
      <el-form
        :model="formData"
        :rules="formRules"
        ref="userForm"
        label-width="100px"
      >
        <el-form-item label="用户名" prop="userName">
          <el-input
            v-model="formData.userName"
            placeholder="请输入用户名"
            :disabled="isEdit"
          ></el-input>
        </el-form-item>
        <el-form-item label="密码" prop="password" :required="!isEdit">
          <el-input
            v-model="formData.password"
            type="password"
            :placeholder="isEdit ? '不修改请留空' : '请输入密码'"
            show-password
          ></el-input>
        </el-form-item>
        <el-form-item label="用户角色" prop="userRole">
          <el-radio-group v-model="formData.userRole">
            <el-radio :label="1">买方用户</el-radio>
            <el-radio :label="2">卖方用户</el-radio>
            <el-radio v-if="isSuperAdmin" :label="3">管理员</el-radio>
            <el-radio v-if="isSuperAdmin" :label="4">超级管理员</el-radio>
          </el-radio-group>
          <div v-if="!isSuperAdmin" style="color: #909399; font-size: 12px; margin-top: 5px;">
            提示：管理员只能{{ isEdit ? '修改为' : '创建' }}买方和卖方账号
          </div>
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="formData.realName" placeholder="请输入真实姓名"></el-input>
        </el-form-item>
        <el-form-item label="昵称" prop="nickName">
          <el-input v-model="formData.nickName" placeholder="请输入昵称"></el-input>
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="formData.phone" placeholder="请输入手机号"></el-input>
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="formData.email" placeholder="请输入邮箱"></el-input>
        </el-form-item>
        <el-form-item label="性别" prop="sex">
          <el-radio-group v-model="formData.sex">
            <el-radio label="0">男</el-radio>
            <el-radio label="1">女</el-radio>
            <el-radio label="2">未知</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="账号状态" prop="userStatus">
          <el-radio-group v-model="formData.userStatus">
            <el-radio :label="0">正常</el-radio>
            <el-radio :label="1">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading"
          >确定</el-button
        >
      </div>
    </el-dialog>

    <!-- 卖方资质审核对话框 -->
    <el-dialog
      title="卖方用户资质审核"
      :visible.sync="auditDialogVisible"
      width="500px"
    >
      <el-form ref="auditForm" :model="auditData" label-width="120px">
        <el-form-item label="用户信息">
          <div>
            <p><strong>用户名：</strong>{{ auditData.userName }}</p>
            <p><strong>真实姓名：</strong>{{ auditData.realName || "未填写" }}</p>
            <p><strong>手机号：</strong>{{ auditData.phone }}</p>
          </div>
        </el-form-item>
        <el-form-item label="审核结果" prop="auditStatus">
          <el-radio-group v-model="auditData.auditStatus">
            <el-radio :label="2">审核通过</el-radio>
            <el-radio :label="3">审核驳回</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item
          v-if="auditData.auditStatus === 3"
          label="驳回原因"
          prop="auditRemark"
        >
          <el-input
            type="textarea"
            v-model="auditData.auditRemark"
            placeholder="请输入驳回原因"
            :rows="4"
            maxlength="200"
            show-word-limit
          ></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="auditDialogVisible = false">取消</el-button>
        <el-button
          type="primary"
          @click="handleAuditSubmit"
          :loading="auditLoading"
          >确定</el-button
        >
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  getUserPage,
  getUserById,
  createUser,
  updateUser,
  updateUserStatus,
  auditSeller,
  deleteUser,
} from "@/api/user";

export default {
  name: "AdminUser",
  data() {
    return {
      loading: false,
      submitLoading: false,
      auditLoading: false,
      tableData: [],
      searchForm: {
        userName: "",
        userRole: "",
        userStatus: "",
      },
      pagination: {
        current: 1,
        size: 10,
        total: 0,
      },
      dialogVisible: false,
      dialogTitle: "创建用户",
      isEdit: false,
      formData: {
        id: null,
        userName: "",
        password: "",
        userRole: 1,
        realName: "",
        nickName: "",
        phone: "",
        email: "",
        sex: "2",
        userStatus: 0,
      },
      formRules: {
        userName: [
          { required: true, message: "请输入用户名", trigger: "blur" },
          {
            min: 3,
            max: 50,
            message: "用户名长度在 3 到 50 个字符",
            trigger: "blur",
          },
        ],
        password: [
          {
            required: true,
            message: "请输入密码",
            trigger: "blur",
            validator: (rule, value, callback) => {
              if (!this.isEdit && (!value || value.trim() === "")) {
                callback(new Error("请输入密码"));
              } else {
                callback();
              }
            },
          },
          {
            min: 6,
            max: 20,
            message: "密码长度在 6 到 20 个字符",
            trigger: "blur",
          },
        ],
        userRole: [
          { required: true, message: "请选择用户角色", trigger: "change" },
        ],
        phone: [
          { required: true, message: "请输入手机号", trigger: "blur" },
          {
            pattern: /^1[3-9]\d{9}$/,
            message: "请输入正确的手机号",
            trigger: "blur",
          },
        ],
        email: [
          {
            type: "email",
            message: "请输入正确的邮箱地址",
            trigger: "blur",
          },
        ],
      },
      auditDialogVisible: false,
      auditData: {
        userId: null,
        userName: "",
        realName: "",
        phone: "",
        auditStatus: 2,
        auditRemark: "",
      },
    };
  },
  computed: {
    showSellerAuditColumn() {
      // 如果表格中有卖方用户，显示资质审核列
      return this.tableData.some((user) => {
        const roles = this.parseUserRole(user.userRole);
        return roles.includes("2");
      });
    },
    isSuperAdmin() {
      // 检查当前登录用户是否是超级管理员
      const userInfo = localStorage.getItem("userInfo");
      if (userInfo) {
        try {
          const user = JSON.parse(userInfo);
          return user.isSuperAdmin === true;
        } catch (e) {
          return false;
        }
      }
      return false;
    },
  },
  mounted() {
    this.loadData();
  },
  methods: {
    // 加载数据
    async loadData() {
      this.loading = true;
      try {
        const params = {
          current: this.pagination.current,
          size: this.pagination.size,
          userName: this.searchForm.userName || undefined,
          userRole: this.searchForm.userRole !== "" ? this.searchForm.userRole : undefined,
          userStatus: this.searchForm.userStatus !== "" ? this.searchForm.userStatus : undefined,
        };
        const result = await getUserPage(params);
        if (result && result.list) {
          this.tableData = result.list;
          this.pagination.total = result.total || 0;
        } else {
          this.tableData = [];
          this.pagination.total = 0;
        }
      } catch (error) {
        console.error("加载用户列表失败:", error);
        this.$message.error("加载失败");
      } finally {
        this.loading = false;
      }
    },
    // 搜索
    handleSearch() {
      this.pagination.current = 1;
      this.loadData();
    },
    // 重置
    handleReset() {
      this.searchForm.userName = "";
      this.searchForm.userRole = "";
      this.searchForm.userStatus = "";
      this.pagination.current = 1;
      this.loadData();
    },
    // 添加
    handleAdd() {
      this.dialogTitle = "创建用户";
      this.isEdit = false;
      this.formData = {
        id: null,
        userName: "",
        password: "",
        userRole: 1,
        realName: "",
        nickName: "",
        phone: "",
        email: "",
        sex: "2",
        userStatus: 0,
      };
      this.dialogVisible = true;
    },
    // 编辑
    async handleEdit(user) {
      this.dialogTitle = "编辑用户";
      this.isEdit = true;
      try {
        const result = await getUserById(user.id);
        if (result) {
          // 处理角色字段：如果是字符串，优先显示最高权限角色（因为radio只能选一个）
          let userRole = result.userRole;
          if (typeof userRole === "string" && userRole.includes(",")) {
            // 多角色时，优先显示最高权限角色
            const roles = userRole.split(",").map(r => r.trim());
            if (roles.includes("4")) {
              userRole = 4; // 超级管理员
            } else if (roles.includes("3")) {
              userRole = 3; // 管理员
            } else if (roles.includes("2")) {
              userRole = 2; // 卖方
            } else {
              userRole = parseInt(roles[0]); // 取第一个角色
            }
          } else if (typeof userRole === "string") {
            userRole = parseInt(userRole);
          }
          
          this.formData = {
            ...result,
            userRole: userRole || 1, // 默认买方
            password: "", // 编辑时不显示密码
          };
          this.dialogVisible = true;
        }
      } catch (error) {
        console.error("获取用户详情失败:", error);
        this.$message.error("获取用户详情失败");
      }
    },
    // 切换账号状态
    async handleToggleStatus(user) {
      const newStatus = user.userStatus === 0 ? 1 : 0;
      const action = newStatus === 0 ? "恢复" : "禁用";
      try {
        await this.$confirm(
          `确定${action}用户 "${user.userName}" 吗？`,
          "提示",
          {
            confirmButtonText: "确定",
            cancelButtonText: "取消",
            type: "warning",
          }
        );
        await updateUserStatus(user.id, newStatus);
        this.$message.success(`${action}成功`);
        this.loadData();
      } catch (error) {
        if (error !== "cancel") {
          console.error(`${action}用户失败:`, error);
          this.$message.error(`${action}失败`);
        }
      }
    },
    // 审核卖方用户
    handleAuditSeller(user) {
      this.auditData = {
        userId: user.id,
        userName: user.userName,
        realName: user.realName || "",
        phone: user.phone,
        auditStatus: 2,
        auditRemark: user.sellerAuditRemark || "",
      };
      this.auditDialogVisible = true;
    },
    // 审核提交
    async handleAuditSubmit() {
      if (this.auditData.auditStatus === 3 && !this.auditData.auditRemark) {
        this.$message.warning("审核驳回时必须填写驳回原因");
        return;
      }
      this.auditLoading = true;
      try {
        await auditSeller(
          this.auditData.userId,
          this.auditData.auditStatus,
          this.auditData.auditRemark
        );
        this.$message.success("审核成功");
        this.auditDialogVisible = false;
        this.loadData();
      } catch (error) {
        console.error("审核失败:", error);
        this.$message.error("审核失败");
      } finally {
        this.auditLoading = false;
      }
    },
    // 删除
    async handleDelete(user) {
      try {
        await this.$confirm(`确定删除用户 "${user.userName}" 吗？`, "提示", {
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          type: "warning",
        });
        await deleteUser(user.id);
        this.$message.success("删除成功");
        this.loadData();
      } catch (error) {
        if (error !== "cancel") {
          console.error("删除用户失败:", error);
          this.$message.error("删除失败");
        }
      }
    },
    // 分页大小变化
    handleSizeChange(size) {
      this.pagination.size = size;
      this.pagination.current = 1;
      this.loadData();
    },
    // 当前页变化
    handleCurrentChange(current) {
      this.pagination.current = current;
      this.loadData();
    },
    // 提交表单
    async handleSubmit() {
      this.$refs.userForm.validate(async (valid) => {
        if (!valid) return;
        this.submitLoading = true;
        try {
          const data = { ...this.formData };
          // 编辑时如果密码为空，则不传密码字段
          if (this.isEdit && !data.password) {
            delete data.password;
          }
          // 将角色转换为字符串格式（后端期望字符串）
          if (data.userRole !== null && data.userRole !== undefined) {
            data.userRole = String(data.userRole);
          }
          if (this.isEdit) {
            await updateUser(data.id, data);
            this.$message.success("修改成功");
          } else {
            await createUser(data);
            this.$message.success("创建成功");
          }
          this.dialogVisible = false;
          this.loadData();
        } catch (error) {
          console.error("提交失败:", error);
          const errorMessage = error.response?.data?.message || error.message || "提交失败";
          this.$message.error(errorMessage);
        } finally {
          this.submitLoading = false;
        }
      });
    },
    // 对话框关闭
    handleDialogClose() {
      this.$refs.userForm.resetFields();
    },
    // 解析用户角色字符串（可能是"1"、"1,2"、"3"等格式）
    parseUserRole(roleStr) {
      if (!roleStr) return [];
      if (typeof roleStr === "number") {
        return [String(roleStr)];
      }
      return roleStr.toString().split(",").map((r) => r.trim());
    },
    // 获取角色文本（支持多角色）
    getRoleText(role) {
      const roles = this.parseUserRole(role);
      const roleMap = {
        "1": "买方",
        "2": "卖方",
        "3": "管理员",
        "4": "超级管理员",
      };
      if (roles.length === 0) return "未知";
      if (roles.length === 1) {
        return roleMap[roles[0]] || "未知";
      }
      // 多角色显示
      return roles.map((r) => roleMap[r] || r).join("、");
    },
    // 获取角色标签类型（多角色时显示最高权限）
    getRoleTagType(role) {
      const roles = this.parseUserRole(role);
      if (roles.includes("4")) return "danger"; // 超级管理员
      if (roles.includes("3")) return "warning"; // 管理员
      if (roles.includes("2")) return "success"; // 卖方
      if (roles.includes("1")) return "primary"; // 买方
      return "info";
    },
    // 获取卖方审核状态文本
    getSellerAuditText(status) {
      const statusMap = {
        0: "未提交",
        1: "待审核",
        2: "审核通过",
        3: "审核驳回",
      };
      return statusMap[status] || "未知";
    },
    // 获取卖方审核状态标签类型
    getSellerAuditTagType(status) {
      const typeMap = {
        0: "info",
        1: "warning",
        2: "success",
        3: "danger",
      };
      return typeMap[status] || "info";
    },
    // 格式化日期时间
    formatDateTime(timeStr) {
      if (!timeStr) return "";
      const date = new Date(timeStr);
      const year = date.getFullYear();
      const month = (date.getMonth() + 1).toString().padStart(2, "0");
      const day = date.getDate().toString().padStart(2, "0");
      const hours = date.getHours().toString().padStart(2, "0");
      const minutes = date.getMinutes().toString().padStart(2, "0");
      return `${year}-${month}-${day} ${hours}:${minutes}`;
    },
  },
};
</script>

<style scoped>
.admin-user-page {
  padding: 20px;
  background: #f5f5f5;
  min-height: 100vh;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.page-header h2 {
  margin: 0;
  color: #333;
}

.filter-section {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.table-section {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.pagination-section {
  display: flex;
  justify-content: center;
  margin-top: 20px;
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}
</style>