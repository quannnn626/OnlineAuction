<template>
  <div class="profile-deposit-page">
    <div class="balance-card">
      <div class="balance-label">当前保证金余额</div>
      <div class="balance-value">¥ {{ balance }}</div>
    </div>
    <div class="record-section">
      <h4>变动记录</h4>
      <el-table v-loading="loading" :data="recordList" stripe>
        <el-table-column prop="amount" label="金额" width="120">
          <template slot-scope="scope">
            <span :class="isPlus(scope.row.depositType) ? 'amount-plus' : 'amount-minus'">
              {{ isPlus(scope.row.depositType) ? '+' : '-' }}{{ scope.row.amount }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="depositType" label="类型" width="100">
          <template slot-scope="scope">{{ getTypeText(scope.row.depositType) }}</template>
        </el-table-column>
        <el-table-column prop="balance" label="余额" width="120"></el-table-column>
        <el-table-column prop="remark" label="备注" min-width="150" show-overflow-tooltip></el-table-column>
        <el-table-column prop="operateTime" label="时间" width="170">
          <template slot-scope="scope">{{ formatDateTime(scope.row.operateTime) }}</template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrap">
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
  </div>
</template>

<script>
import { getMyBalance, getMyDepositPage } from "@/api/deposit";

export default {
  name: "ProfileDeposit",
  data() {
    return {
      loading: false,
      balance: "0.00",
      recordList: [],
      pagination: { current: 1, size: 10, total: 0 },
    };
  },
  mounted() {
    this.loadBalance();
    this.loadRecords();
  },
  methods: {
    isPlus(t) {
      return t === 0 || t === 2;
    },
    getTypeText(t) {
      const map = { 0: "充值", 1: "冻结", 2: "解冻", 3: "抵扣尾款", 4: "扣除(悔拍)" };
      return map[t] || "-";
    },
    async loadBalance() {
      try {
        const res = await getMyBalance();
        this.balance = (res && res.balance != null) ? Number(res.balance).toFixed(2) : "0.00";
      } catch (e) {
        this.balance = "0.00";
      }
    },
    async loadRecords() {
      this.loading = true;
      try {
        const res = await getMyDepositPage({
          current: this.pagination.current,
          size: this.pagination.size,
        });
        this.recordList = (res && res.list) ? res.list : [];
        this.pagination.total = (res && res.total) ? res.total : 0;
      } catch (e) {
        this.recordList = [];
      } finally {
        this.loading = false;
      }
    },
    handleSizeChange(v) {
      this.pagination.size = v;
      this.pagination.current = 1;
      this.loadRecords();
    },
    handleCurrentChange(v) {
      this.pagination.current = v;
      this.loadRecords();
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
.profile-deposit-page { padding: 20px; }
.balance-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  padding: 24px;
  border-radius: 8px;
  margin-bottom: 24px;
}
.balance-label { font-size: 14px; opacity: 0.9; }
.balance-value { font-size: 28px; font-weight: 600; margin-top: 8px; }
.record-section h4 { margin: 0 0 16px 0; font-size: 16px; }
.pagination-wrap { margin-top: 16px; }
.amount-plus { color: #67c23a; }
.amount-minus { color: #f56c6c; }
</style>

