<template>
  <div class="admin-dashboard-page">
    <div class="page-header">
      <h2>数据看板</h2>
      <el-button icon="el-icon-refresh" size="small" @click="loadData">刷新</el-button>
    </div>
    <div v-loading="loading" class="stats-grid">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-value">{{ stats.goodsTotal || 0 }}</div>
            <div class="stat-label">商品总数</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-value">{{ stats.userTotal || 0 }}</div>
            <div class="stat-label">用户总数</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-value">{{ stats.bidRecordTotal || 0 }}</div>
            <div class="stat-label">参拍次数</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-value">{{ stats.orderTotal || 0 }}</div>
            <div class="stat-label">订单总数</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-value">{{ stats.orderDealCount || 0 }}</div>
            <div class="stat-label">成交订单数</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-value">{{ stats.dealRate != null ? stats.dealRate + '%' : '-' }}</div>
            <div class="stat-label">成交率</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-value">¥{{ formatMoney(stats.depositTotal) }}</div>
            <div class="stat-label">保证金总额</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-value">¥{{ formatMoney(stats.depositAvailable) }}</div>
            <div class="stat-label">保证金可用</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-value">¥{{ formatMoney(stats.depositFrozen) }}</div>
            <div class="stat-label">保证金冻结</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-value">{{ stats.depositUserCount != null ? stats.depositUserCount : 0 }}</div>
            <div class="stat-label">保证金用户数</div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script>
import { getDashboardStats } from "@/api/dashboard";

export default {
  name: "AdminDashboard",
  data() {
    return {
      loading: false,
      stats: {},
    };
  },
  mounted() {
    this.loadData();
  },
  methods: {
    async loadData() {
      this.loading = true;
      try {
        this.stats = await getDashboardStats();
      } catch (e) {
        this.$message.error("加载数据失败");
      } finally {
        this.loading = false;
      }
    },
    formatMoney(v) {
      if (v == null) return "0.00";
      return Number(v).toLocaleString("zh-CN", { minimumFractionDigits: 2, maximumFractionDigits: 2 });
    },
  },
};
</script>

<style scoped>
.admin-dashboard-page { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; }
.page-header h2 { margin: 0; }
.stats-grid { margin-top: 16px; }
.stat-card { text-align: center; }
.stat-value { font-size: 28px; font-weight: 600; color: #303133; }
.stat-label { font-size: 14px; color: #909399; margin-top: 8px; }
</style>
