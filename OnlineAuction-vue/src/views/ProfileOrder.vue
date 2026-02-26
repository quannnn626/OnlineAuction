<template>
  <div class="profile-order-page">
    <div class="filter-bar">
      <el-radio-group v-model="roleType" @change="handleRoleChange">
        <el-radio-button label="buyer">我的竞拍订单</el-radio-button>
        <el-radio-button label="seller">我的销售订单</el-radio-button>
      </el-radio-group>
      <el-select v-model="searchStatus" placeholder="全部状态" clearable @change="loadData" style="width: 120px; margin-left: 12px">
        <el-option label="待付款" :value="0"></el-option>
        <el-option label="待发货" :value="1"></el-option>
        <el-option label="待收货" :value="2"></el-option>
        <el-option label="已完成" :value="3"></el-option>
        <el-option label="已悔拍" :value="4"></el-option>
        <el-option label="已退款" :value="5"></el-option>
      </el-select>
    </div>
    <el-table v-loading="loading" :data="tableData" stripe>
      <el-table-column prop="orderNo" label="订单号" width="160"></el-table-column>
      <el-table-column prop="goodsId" label="商品ID" width="80"></el-table-column>
      <el-table-column prop="dealPrice" label="成交价" width="100"></el-table-column>
      <el-table-column prop="depositAmount" label="保证金" width="90"></el-table-column>
      <el-table-column prop="remainAmount" label="尾款" width="90"></el-table-column>
      <el-table-column prop="orderStatus" label="状态" width="90">
        <template slot-scope="scope">
          <el-tag :type="getStatusTagType(scope.row.orderStatus)" size="small">
            {{ getStatusText(scope.row.orderStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="payDeadline" label="尾款截止" width="160">
        <template slot-scope="scope">{{ formatDateTime(scope.row.payDeadline) }}</template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="160">
        <template slot-scope="scope">{{ formatDateTime(scope.row.createTime) }}</template>
      </el-table-column>
    </el-table>
    <el-empty v-if="!loading && tableData.length === 0" description="暂无订单"></el-empty>
    <div class="pagination-wrap" v-if="pagination.total > 0">
      <el-pagination
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="pagination.current"
        :page-sizes="[10, 20]"
        :page-size="pagination.size"
        layout="total, prev, pager, next"
        :total="pagination.total"
      ></el-pagination>
    </div>
  </div>
</template>

<script>
import { getMyOrderPage } from "@/api/order";

export default {
  name: "ProfileOrder",
  data() {
    return {
      loading: false,
      roleType: "buyer",
      searchStatus: undefined,
      tableData: [],
      pagination: { current: 1, size: 10, total: 0 },
    };
  },
  mounted() {
    this.loadData();
  },
  methods: {
    handleRoleChange() {
      this.pagination.current = 1;
      this.loadData();
    },
    async loadData() {
      this.loading = true;
      try {
        const res = await getMyOrderPage({
          current: this.pagination.current,
          size: this.pagination.size,
          orderStatus: this.searchStatus,
          role: this.roleType,
        });
        this.tableData = (res && res.list) ? res.list : [];
        this.pagination.total = (res && res.total) ? res.total : 0;
      } catch (e) {
        this.tableData = [];
      } finally {
        this.loading = false;
      }
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
.profile-order-page { padding: 20px; }
.filter-bar { margin-bottom: 20px; display: flex; align-items: center; }
.pagination-wrap { margin-top: 20px; }
</style>
