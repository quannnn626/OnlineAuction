<template>
  <div class="admin-fee-record-page">
    <div class="page-header">
      <h2>佣金结算 / 平台扣费记录</h2>
    </div>

    <el-tabs v-model="activeTab" @tab-click="onTabClick">
      <!-- 佣金结算 -->
      <el-tab-pane label="佣金结算" name="commission">
        <div class="commission-config">
          <span>当前佣金比例：</span>
          <el-input-number
            v-model="commissionRatePercent"
            :min="0"
            :max="100"
            :precision="2"
            size="small"
            style="width: 120px; margin: 0 8px"
          ></el-input-number>
          <span>%</span>
          <el-button type="primary" size="small" @click="saveCommissionRate" :loading="rateSaving"
            >保存</el-button
          >
        </div>
        <p class="commission-desc">对已完成的订单做结算确认后，佣金 = 成交价 × 佣金比例，计入平台收入。</p>
        <div class="table-section">
          <el-table v-loading="pendingLoading" :data="pendingOrders" stripe>
            <el-table-column prop="orderNo" label="订单号" width="160"></el-table-column>
            <el-table-column prop="goodsName" label="商品" min-width="120" show-overflow-tooltip></el-table-column>
            <el-table-column prop="sellerName" label="卖方" width="100"></el-table-column>
            <el-table-column prop="dealPrice" label="成交价" width="100">
              <template slot-scope="scope">¥{{ scope.row.dealPrice }}</template>
            </el-table-column>
            <el-table-column label="预计佣金" width="110">
              <template slot-scope="scope">¥{{ calcCommission(scope.row.dealPrice) }}</template>
            </el-table-column>
            <el-table-column label="操作" width="200" fixed="right">
              <template slot-scope="scope">
                <el-button type="text" size="small" @click="openSettleDialog(scope.row)">结算确认</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
        <div class="pagination-section">
          <el-pagination
            @size-change="(v) => { pendingSize = v; pendingPage = 1; loadPending(); }"
            @current-change="(v) => { pendingPage = v; loadPending(); }"
            :current-page="pendingPage"
            :page-sizes="[10, 20, 50]"
            :page-size="pendingSize"
            layout="total, sizes, prev, pager, next"
            :total="pendingTotal"
            background
          />
        </div>

        <el-dialog title="佣金结算确认" :visible.sync="settleDialogVisible" width="420px">
          <div v-if="settleOrder">
            <p>订单号：{{ settleOrder.orderNo }}</p>
            <p>成交价：¥{{ settleOrder.dealPrice }}</p>
            <p>按当前比例预计佣金：¥{{ calcCommission(settleOrder.dealPrice) }}</p>
            <el-form-item label="手动输入佣金（可选）">
              <el-input-number
                v-model="settleOverrideAmount"
                :min="0"
                :precision="2"
                placeholder="留空则按比例计算"
                style="width: 100%"
              ></el-input-number>
            </el-form-item>
          </div>
          <span slot="footer">
            <el-button @click="settleDialogVisible = false">取消</el-button>
            <el-button type="primary" @click="submitSettle" :loading="settleLoading">确认结算</el-button>
          </span>
        </el-dialog>
      </el-tab-pane>

      <!-- 平台扣费记录 -->
      <el-tab-pane label="平台扣费记录" name="record">
        <div class="filter-section">
          <el-form :inline="true" :model="searchForm">
            <el-form-item label="类型">
              <el-select v-model="searchForm.feeType" placeholder="全部" clearable style="width: 140px">
                <el-option label="佣金" :value="1"></el-option>
                <el-option label="违约金" :value="2"></el-option>
                <el-option label="违规扣保证金" :value="3"></el-option>
                <el-option label="其他手续费" :value="4"></el-option>
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
              <el-button type="success" icon="el-icon-download" @click="exportRecords">导出对账</el-button>
            </el-form-item>
          </el-form>
        </div>
        <el-table v-loading="loading" :data="tableData" stripe>
          <el-table-column prop="id" label="ID" width="70"></el-table-column>
          <el-table-column prop="orderId" label="订单ID" width="90">
            <template slot-scope="scope">{{ scope.row.orderId || "-" }}</template>
          </el-table-column>
          <el-table-column prop="userId" label="扣谁(用户ID)" width="110"></el-table-column>
          <el-table-column prop="amount" label="扣费金额" width="100">
            <template slot-scope="scope">¥{{ scope.row.amount }}</template>
          </el-table-column>
          <el-table-column prop="feeType" label="类型" width="120">
            <template slot-scope="scope">{{ getFeeTypeText(scope.row.feeType) }}</template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="90">
            <template slot-scope="scope">{{ scope.row.status === 1 ? "已结算" : "待结算" }}</template>
          </el-table-column>
          <el-table-column prop="remark" label="扣费原因" min-width="180" show-overflow-tooltip></el-table-column>
          <el-table-column prop="createTime" label="扣费时间" width="160">
            <template slot-scope="scope">{{ formatDateTime(scope.row.createTime) }}</template>
          </el-table-column>
        </el-table>
        <div class="pagination-section">
          <el-pagination
            @size-change="(v) => { pagination.size = v; pagination.current = 1; loadData(); }"
            @current-change="(v) => { pagination.current = v; loadData(); }"
            :current-page="pagination.current"
            :page-sizes="[10, 20, 50]"
            :page-size="pagination.size"
            layout="total, sizes, prev, pager, next"
            :total="pagination.total"
            background
          />
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script>
import {
  getFeeRecordPage,
  getCommissionRate,
  setCommissionRate,
  getOrdersPendingCommission,
  confirmCommissionSettle,
  exportFeeRecords,
} from "@/api/finance";

export default {
  name: "AdminFeeRecord",
  data() {
    return {
      activeTab: "commission",
      commissionRatePercent: 5,
      rateSaving: false,
      pendingLoading: false,
      pendingOrders: [],
      pendingPage: 1,
      pendingSize: 10,
      pendingTotal: 0,
      settleDialogVisible: false,
      settleOrder: null,
      settleOverrideAmount: null,
      settleLoading: false,
      loading: false,
      tableData: [],
      searchForm: { feeType: undefined, status: undefined, userId: "" },
      pagination: { current: 1, size: 10, total: 0 },
    };
  },
  mounted() {
    this.loadCommissionRate();
    this.loadPending();
    this.loadData();
  },
  methods: {
    onTabClick() {
      if (this.activeTab === "commission") this.loadPending();
      else this.loadData();
    },
    async loadCommissionRate() {
      try {
        const res = await getCommissionRate();
        if (res && res.commissionRatePercent != null)
          this.commissionRatePercent = Number(res.commissionRatePercent) || 5;
        else if (res && res.commissionRate != null)
          this.commissionRatePercent = Number(res.commissionRate) * 100 || 5;
      } catch (e) {}
    },
    saveCommissionRate() {
      const rate = this.commissionRatePercent / 100;
      if (rate < 0 || rate > 1) {
        this.$message.warning("比例需在 0～100% 之间");
        return;
      }
      this.rateSaving = true;
      setCommissionRate(rate)
        .then(() => {
          this.$message.success("已保存");
        })
        .catch((e) => this.$message.error(e.message || "保存失败"))
        .finally(() => { this.rateSaving = false; });
    },
    calcCommission(dealPrice) {
      if (dealPrice == null) return "0.00";
      const r = (this.commissionRatePercent || 0) / 100;
      return (Number(dealPrice) * r).toFixed(2);
    },
    async loadPending() {
      this.pendingLoading = true;
      try {
        const res = await getOrdersPendingCommission({
          current: this.pendingPage,
          size: this.pendingSize,
        });
        this.pendingOrders = (res && res.list) ? res.list : [];
        this.pendingTotal = (res && res.total) ? res.total : 0;
      } catch (e) {
        this.pendingOrders = [];
      } finally {
        this.pendingLoading = false;
      }
    },
    openSettleDialog(row) {
      this.settleOrder = row;
      this.settleOverrideAmount = null;
      this.settleDialogVisible = true;
    },
    submitSettle() {
      if (!this.settleOrder) return;
      this.settleLoading = true;
      const override = this.settleOverrideAmount != null && this.settleOverrideAmount > 0
        ? this.settleOverrideAmount
        : undefined;
      confirmCommissionSettle(this.settleOrder.id, override)
        .then(() => {
          this.$message.success("结算成功");
          this.settleDialogVisible = false;
          this.loadPending();
          this.loadData();
        })
        .catch((e) => this.$message.error(e.message || e.msg || "结算失败"))
        .finally(() => { this.settleLoading = false; });
    },
    getFeeTypeText(t) {
      const m = { 1: "佣金", 2: "违约金", 3: "违规扣保证金", 4: "其他手续费" };
      return m[t] || "-";
    },
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
    exportRecords() {
      const params = {
        feeType: this.searchForm.feeType,
        status: this.searchForm.status,
        userId: this.searchForm.userId ? parseInt(this.searchForm.userId) : undefined,
      };
      exportFeeRecords(params)
        .then((res) => {
          if (!res || !Array.isArray(res)) return;
          const headers = ["ID", "订单ID", "用户ID", "金额", "类型", "状态", "扣费原因", "创建时间"];
          const rows = res.map((r) => [
            r.id,
            r.orderId || "",
            r.userId,
            r.amount,
            this.getFeeTypeText(r.feeType),
            r.status === 1 ? "已结算" : "待结算",
            r.remark || "",
            r.createTime || "",
          ]);
          const csv = [headers.join(","), ...rows.map((row) => row.map((c) => `"${String(c).replace(/"/g, '""')}"`).join(","))].join("\n");
          const blob = new Blob(["\ufeff" + csv], { type: "text/csv;charset=utf-8" });
          const a = document.createElement("a");
          a.href = URL.createObjectURL(blob);
          a.download = "扣费记录_" + new Date().toISOString().slice(0, 10) + ".csv";
          a.click();
          URL.revokeObjectURL(a.href);
          this.$message.success("已导出");
        })
        .catch((e) => this.$message.error(e.message || "导出失败"));
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
.admin-fee-record-page {
  padding: 20px;
}
.page-header {
  margin-bottom: 20px;
}
.page-header h2 {
  margin: 0;
}
.commission-config {
  margin-bottom: 12px;
}
.commission-desc {
  color: #606266;
  font-size: 13px;
  margin-bottom: 16px;
}
.filter-section {
  margin-bottom: 16px;
}
.table-section {
  margin-bottom: 16px;
}
.pagination-section {
  margin-top: 16px;
  text-align: right;
}
</style>
