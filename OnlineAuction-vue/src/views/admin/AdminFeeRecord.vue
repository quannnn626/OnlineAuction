<template>
  <div class="admin-fee-record-page">
    <div class="page-header">
      <h2>佣金结算 / 平台扣费记录</h2>
    </div>
    <div class="filter-section">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="类型">
          <el-select v-model="searchForm.feeType" placeholder="全部" clearable style="width: 120px">
            <el-option label="平台扣费" :value="1"></el-option>
            <el-option label="佣金结算" :value="2"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 100px">
            <el-option label="待结算" :value="0"></el-option>
            <el-option label="已结算" :value="1"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="用户ID">
          <el-input v-model="searchForm.userId" placeholder="用户ID" clearable style="width: 100px"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" @click="loadData">搜索</el-button>
        </el-form-item>
      </el-form>
    </div>
    <el-table v-loading="loading" :data="tableData" stripe>
      <el-table-column prop="id" label="ID" width="70"></el-table-column>
      <el-table-column prop="orderId" label="订单ID" width="90">
        <template slot-scope="scope">{{ scope.row.orderId || '-' }}</template>
      </el-table-column>
      <el-table-column prop="userId" label="用户ID" width="90"></el-table-column>
      <el-table-column prop="amount" label="金额" width="100">
        <template slot-scope="scope">¥{{ scope.row.amount }}</template>
      </el-table-column>
      <el-table-column prop="feeType" label="类型" width="100">
        <template slot-scope="scope">{{ scope.row.feeType === 1 ? '平台扣费' : '佣金结算' }}</template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="90">
        <template slot-scope="scope">{{ scope.row.status === 1 ? '已结算' : '待结算' }}</template>
      </el-table-column>
      <el-table-column prop="remark" label="备注" min-width="120" show-overflow-tooltip></el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="160">
        <template slot-scope="scope">{{ formatDateTime(scope.row.createTime) }}</template>
      </el-table-column>
    </el-table>
    <div class="pagination-section">
      <el-pagination
        @size-change="(v) => { pagination.size = v; loadData(); }"
        @current-change="(v) => { pagination.current = v; loadData(); }"
        :current-page="pagination.current"
        :page-sizes="[10, 20, 50]"
        :page-size="pagination.size"
        layout="total, sizes, prev, pager, next"
        :total="pagination.total"
        background
      />
    </div>
  </div>
</template>

<script>
import { getFeeRecordPage } from "@/api/finance";

export default {
  name: "AdminFeeRecord",
  data() {
    return {
      loading: false,
      tableData: [],
      searchForm: { feeType: undefined, status: undefined, userId: "" },
      pagination: { current: 1, size: 10, total: 0 },
    };
  },
  mounted() {
    this.loadData();
  },
  methods: {
    async loadData() {
      this.loading = true;
      try {
        const params = {
          current: this.pagination.current,
          size: this.pagination.size,
          feeType: this.searchForm.feeType,
          status: this.searchForm.status,
          userId: this.searchForm.userId ? parseInt(this.searchForm.userId) : undefined,
        };
        const res = await getFeeRecordPage(params);
        this.tableData = (res && res.list) ? res.list : [];
        this.pagination.total = (res && res.total) ? res.total : 0;
      } catch (e) {
        this.tableData = [];
      } finally {
        this.loading = false;
      }
    },
    formatDateTime(v) {
      if (!v) return "-";
      const d = new Date(v);
      return isNaN(d.getTime()) ? v : d.toLocaleString("zh-CN", { hour12: false });
    },
  },
};
</script>

<style scoped>
.admin-fee-record-page { padding: 20px; }
.page-header { margin-bottom: 20px; }
.page-header h2 { margin: 0; }
.filter-section { margin-bottom: 16px; }
.pagination-section { margin-top: 20px; text-align: right; }
</style>
