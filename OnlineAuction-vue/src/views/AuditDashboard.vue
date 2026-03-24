<template>
  <div class="audit-dashboard-page">
    <div class="page-header">
      <h2>审计中心</h2>
      <el-button icon="el-icon-refresh" size="small" @click="refreshCurrent">刷新</el-button>
    </div>

    <el-tabs v-model="activeTab">
      <el-tab-pane label="操作日志" name="oper">
        <el-form :inline="true" class="toolbar">
          <el-form-item label="操作人ID">
            <el-input v-model="operQuery.operUserId" placeholder="可选" style="width:140px" />
          </el-form-item>
          <el-form-item label="模块">
            <el-input v-model="operQuery.operModule" placeholder="如 risk/auth" style="width:160px" />
          </el-form-item>
          <el-form-item label="类型">
            <el-input v-model="operQuery.operType" placeholder="如 login" style="width:140px" />
          </el-form-item>
          <el-form-item label="关键词">
            <el-input v-model="operQuery.keyword" placeholder="内容关键字" style="width:180px" />
          </el-form-item>
          <el-button type="primary" size="small" @click="loadOperLogs">查询</el-button>
        </el-form>

        <el-table v-loading="operLoading" :data="operList" stripe style="margin-top:10px">
          <el-table-column prop="id" label="ID" width="90"></el-table-column>
          <el-table-column prop="operUserId" label="操作人ID" width="110"></el-table-column>
          <el-table-column prop="operModule" label="模块" width="120"></el-table-column>
          <el-table-column prop="operType" label="类型" width="140"></el-table-column>
          <el-table-column prop="operIp" label="IP" width="130"></el-table-column>
          <el-table-column prop="operContent" label="内容" min-width="280"></el-table-column>
          <el-table-column prop="createTime" label="时间" min-width="170">
            <template slot-scope="scope">{{ formatTime(scope.row.createTime) }}</template>
          </el-table-column>
        </el-table>
        <div class="pagination-wrap" v-if="operPage.total > 0">
          <el-pagination
            :current-page="operPage.current"
            :page-size="operPage.size"
            :total="operPage.total"
            layout="total, prev, pager, next"
            @current-change="handleOperPageChange"
          />
        </div>
      </el-tab-pane>

      <el-tab-pane label="登录日志" name="login">
        <el-form :inline="true" class="toolbar">
          <el-form-item label="操作人ID">
            <el-input v-model="loginQuery.operUserId" placeholder="可选" style="width:140px" />
          </el-form-item>
          <el-form-item label="关键词">
            <el-input v-model="loginQuery.keyword" placeholder="账号/失败原因" style="width:220px" />
          </el-form-item>
          <el-button type="primary" size="small" @click="loadLoginLogs">查询</el-button>
        </el-form>

        <el-table v-loading="loginLoading" :data="loginList" stripe style="margin-top:10px">
          <el-table-column prop="id" label="ID" width="90"></el-table-column>
          <el-table-column prop="operUserId" label="用户ID" width="100"></el-table-column>
          <el-table-column prop="operType" label="结果" width="120">
            <template slot-scope="scope">
              <el-tag :type="scope.row.operType === 'login_fail' ? 'danger' : 'success'" size="small">
                {{ scope.row.operType === "login_fail" ? "登录失败" : "登录成功" }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="operIp" label="IP" width="140"></el-table-column>
          <el-table-column prop="operContent" label="内容" min-width="320"></el-table-column>
          <el-table-column prop="createTime" label="时间" min-width="170">
            <template slot-scope="scope">{{ formatTime(scope.row.createTime) }}</template>
          </el-table-column>
        </el-table>
        <div class="pagination-wrap" v-if="loginPage.total > 0">
          <el-pagination
            :current-page="loginPage.current"
            :page-size="loginPage.size"
            :total="loginPage.total"
            layout="total, prev, pager, next"
            @current-change="handleLoginPageChange"
          />
        </div>
      </el-tab-pane>

      <el-tab-pane label="敏感操作审计" name="sensitive">
        <el-form :inline="true" class="toolbar">
          <el-form-item label="操作人ID">
            <el-input v-model="sensitiveQuery.operUserId" placeholder="可选" style="width:140px" />
          </el-form-item>
          <el-form-item label="关键词">
            <el-input v-model="sensitiveQuery.keyword" placeholder="冻结/退款/删除等" style="width:220px" />
          </el-form-item>
          <el-button type="primary" size="small" @click="loadSensitiveLogs">查询</el-button>
        </el-form>

        <el-table v-loading="sensitiveLoading" :data="sensitiveList" stripe style="margin-top:10px">
          <el-table-column prop="id" label="ID" width="90"></el-table-column>
          <el-table-column prop="operUserId" label="操作人ID" width="110"></el-table-column>
          <el-table-column prop="operModule" label="模块" width="130"></el-table-column>
          <el-table-column prop="operType" label="类型" width="140"></el-table-column>
          <el-table-column prop="operContent" label="内容" min-width="320"></el-table-column>
          <el-table-column prop="createTime" label="时间" min-width="170">
            <template slot-scope="scope">{{ formatTime(scope.row.createTime) }}</template>
          </el-table-column>
        </el-table>
        <div class="pagination-wrap" v-if="sensitivePage.total > 0">
          <el-pagination
            :current-page="sensitivePage.current"
            :page-size="sensitivePage.size"
            :total="sensitivePage.total"
            layout="total, prev, pager, next"
            @current-change="handleSensitivePageChange"
          />
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script>
import { getAuditOperLogsPage, getAuditLoginLogsPage, getAuditSensitiveLogsPage } from "@/api/audit";

export default {
  name: "AuditDashboard",
  data() {
    return {
      activeTab: "oper",
      operLoading: false,
      operList: [],
      operQuery: { operUserId: "", operModule: "", operType: "", keyword: "" },
      operPage: { current: 1, size: 10, total: 0 },

      loginLoading: false,
      loginList: [],
      loginQuery: { operUserId: "", keyword: "" },
      loginPage: { current: 1, size: 10, total: 0 },

      sensitiveLoading: false,
      sensitiveList: [],
      sensitiveQuery: { operUserId: "", keyword: "" },
      sensitivePage: { current: 1, size: 10, total: 0 },
    };
  },
  mounted() {
    this.loadOperLogs();
  },
  methods: {
    refreshCurrent() {
      if (this.activeTab === "oper") this.loadOperLogs();
      else if (this.activeTab === "login") this.loadLoginLogs();
      else this.loadSensitiveLogs();
    },
    handleOperPageChange(p) {
      this.operPage.current = p;
      this.loadOperLogs();
    },
    handleLoginPageChange(p) {
      this.loginPage.current = p;
      this.loadLoginLogs();
    },
    handleSensitivePageChange(p) {
      this.sensitivePage.current = p;
      this.loadSensitiveLogs();
    },
    async loadOperLogs() {
      this.operLoading = true;
      try {
        const params = { current: this.operPage.current, size: this.operPage.size };
        if (this.operQuery.operUserId !== "") params.operUserId = Number(this.operQuery.operUserId);
        if (this.operQuery.operModule.trim()) params.operModule = this.operQuery.operModule.trim();
        if (this.operQuery.operType.trim()) params.operType = this.operQuery.operType.trim();
        if (this.operQuery.keyword.trim()) params.keyword = this.operQuery.keyword.trim();
        const res = await getAuditOperLogsPage(params);
        this.operList = (res && res.list) || [];
        this.operPage.total = (res && res.total) || 0;
      } catch (e) {
        this.$message.error(e.message || "加载操作日志失败");
      } finally {
        this.operLoading = false;
      }
    },
    async loadLoginLogs() {
      this.loginLoading = true;
      try {
        const params = { current: this.loginPage.current, size: this.loginPage.size };
        if (this.loginQuery.operUserId !== "") params.operUserId = Number(this.loginQuery.operUserId);
        if (this.loginQuery.keyword.trim()) params.keyword = this.loginQuery.keyword.trim();
        const res = await getAuditLoginLogsPage(params);
        this.loginList = (res && res.list) || [];
        this.loginPage.total = (res && res.total) || 0;
      } catch (e) {
        this.$message.error(e.message || "加载登录日志失败");
      } finally {
        this.loginLoading = false;
      }
    },
    async loadSensitiveLogs() {
      this.sensitiveLoading = true;
      try {
        const params = { current: this.sensitivePage.current, size: this.sensitivePage.size };
        if (this.sensitiveQuery.operUserId !== "") params.operUserId = Number(this.sensitiveQuery.operUserId);
        if (this.sensitiveQuery.keyword.trim()) params.keyword = this.sensitiveQuery.keyword.trim();
        const res = await getAuditSensitiveLogsPage(params);
        this.sensitiveList = (res && res.list) || [];
        this.sensitivePage.total = (res && res.total) || 0;
      } catch (e) {
        this.$message.error(e.message || "加载敏感审计日志失败");
      } finally {
        this.sensitiveLoading = false;
      }
    },
    formatTime(v) {
      if (!v) return "-";
      const d = new Date(v);
      return isNaN(d.getTime()) ? v : d.toLocaleString("zh-CN", { hour12: false });
    },
  },
};
</script>

<style scoped>
.audit-dashboard-page {
  padding: 20px;
}
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.toolbar {
  margin-top: 8px;
}
.pagination-wrap {
  margin-top: 14px;
  text-align: right;
}
</style>
