<template>
  <div class="service-orders-page">
    <div class="page-header">
      <el-button type="text" icon="el-icon-arrow-left" @click="backToMessage">返回消息中心</el-button>
      <h2>用户订单 — {{ displayUserName }}</h2>
    </div>
    <div class="filter-section">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="订单号">
          <el-input v-model="searchForm.orderNo" placeholder="订单编号" clearable style="width: 180px"></el-input>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.orderStatus" placeholder="全部" clearable style="width: 120px">
            <el-option label="待付款" :value="0"></el-option>
            <el-option label="待发货" :value="1"></el-option>
            <el-option label="待收货" :value="2"></el-option>
            <el-option label="已完成" :value="3"></el-option>
            <el-option label="已悔拍" :value="4"></el-option>
            <el-option label="已退款" :value="5"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" @click="handleSearch">搜索</el-button>
          <el-button icon="el-icon-refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>
    <div class="table-section">
      <el-table v-loading="loading" :data="tableData" stripe>
        <el-table-column prop="orderNo" label="订单号" width="160"></el-table-column>
        <el-table-column prop="goodsName" label="商品名称" min-width="120"></el-table-column>
        <el-table-column prop="buyerName" label="买方" width="100"></el-table-column>
        <el-table-column prop="sellerName" label="卖方" width="100"></el-table-column>
        <el-table-column prop="dealPrice" label="成交价" width="100"></el-table-column>
        <el-table-column prop="orderStatus" label="状态" width="90">
          <template slot-scope="scope">
            <el-tag :type="getStatusTagType(scope.row.orderStatus)">{{ getStatusText(scope.row.orderStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="payDeadline" label="尾款截止" width="160">
          <template slot-scope="scope">{{ formatDateTime(scope.row.payDeadline) }}</template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160">
          <template slot-scope="scope">{{ formatDateTime(scope.row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="物流" width="140" align="center">
          <template slot-scope="scope">
            <span v-if="scope.row.expressCompany">{{ scope.row.expressCompany }}<br>{{ scope.row.expressNo }}</span>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <div class="pagination-section">
      <el-pagination
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="pagination.current"
        :page-sizes="[10, 20, 50]"
        :page-size="pagination.size"
        layout="total, sizes, prev, pager, next"
        :total="pagination.total"
        background
      ></el-pagination>
    </div>
  </div>
</template>

<script>
import { getServiceUserOrderPage } from "@/api/order";

export default {
  name: "ServiceOrders",
  data() {
    return {
      userId: null,
      displayUserName: "用户",
      loading: false,
      tableData: [],
      searchForm: { orderNo: "", orderStatus: undefined },
      pagination: { current: 1, size: 10, total: 0 },
    };
  },
  mounted() {
    this.userId = this.$route.query.userId ? Number(this.$route.query.userId) : null;
    this.displayUserName = this.$route.query.userName ? decodeURIComponent(this.$route.query.userName) : (this.userId ? `用户 ${this.userId}` : "用户");
    if (!this.userId) {
      this.$router.replace("/message");
      return;
    }
    this.loadData();
  },
  methods: {
    backToMessage() {
      this.$router.push("/message");
    },
    async loadData() {
      if (!this.userId) return;
      this.loading = true;
      try {
        const params = {
          current: this.pagination.current,
          size: this.pagination.size,
          orderStatus: this.searchForm.orderStatus,
          orderNo: this.searchForm.orderNo || undefined,
        };
        const res = await getServiceUserOrderPage(this.userId, params);
        this.tableData = (res && res.list) ? res.list : [];
        this.pagination.total = (res && res.total) ? res.total : 0;
      } catch (e) {
        this.tableData = [];
        this.pagination.total = 0;
      } finally {
        this.loading = false;
      }
    },
    handleSearch() {
      this.pagination.current = 1;
      this.loadData();
    },
    handleReset() {
      this.searchForm = { orderNo: "", orderStatus: undefined };
      this.pagination.current = 1;
      this.loadData();
    },
    handleSizeChange(v) {
      this.pagination.size = v;
      this.pagination.current = 1;
      this.loadData();
    },
    handleCurrentChange(v) {
      this.pagination.current = v;
      this.loadData();
    },
    getStatusText(s) {
      const map = { 0: "待付款", 1: "待发货", 2: "待收货", 3: "已完成", 4: "已悔拍", 5: "已退款" };
      return map[s] || "-";
    },
    getStatusTagType(s) {
      const map = { 0: "warning", 1: "primary", 2: "primary", 3: "success", 4: "info", 5: "info" };
      return map[s] || "info";
    },
    formatDateTime(val) {
      if (!val) return "-";
      const d = new Date(val);
      return isNaN(d.getTime()) ? val : d.toLocaleString("zh-CN", { hour12: false });
    },
  },
};
</script>

<style scoped>
.service-orders-page { padding: 20px; }
.page-header { margin-bottom: 16px; }
.page-header h2 { margin: 8px 0 0 0; font-size: 18px; }
.filter-section { margin-bottom: 16px; }
.table-section { background: #fff; padding: 20px; border-radius: 4px; }
.pagination-section { margin-top: 20px; text-align: right; }
.text-muted { color: #909399; font-size: 12px; }
</style>
