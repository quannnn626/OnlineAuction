<template>
  <div class="profile-deposit-page">
    <div class="balance-card">
      <div class="balance-label">当前保证金余额</div>
      <div class="balance-value">¥ {{ balance }}</div>
      <el-button type="primary" size="small" class="apply-btn" @click="applyRechargeVisible = true">申请充值</el-button>
    </div>
    <div class="record-section">
      <h4>我的充值申请</h4>
      <el-table v-loading="rechargeLoading" :data="rechargeList" stripe size="small">
        <el-table-column prop="amount" label="申请金额" width="100">
          <template slot-scope="scope">¥{{ scope.row.amount }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90">
          <template slot-scope="scope">{{ rechargeStatusText(scope.row.status) }}</template>
        </el-table-column>
        <el-table-column prop="applyRemark" label="备注" min-width="120" show-overflow-tooltip></el-table-column>
        <el-table-column prop="applyTime" label="申请时间" width="160">
          <template slot-scope="scope">{{ formatDateTime(scope.row.applyTime) }}</template>
        </el-table-column>
      </el-table>
      <div v-if="rechargeTotal > 0" class="pagination-wrap">
        <el-pagination small :current-page="Number(rechargePage)" :page-size="10" :total="rechargeTotal" layout="total, prev, pager, next" @current-change="(p) => { this.rechargePage = p; this.loadRechargeList(); }" />
      </div>
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

    <el-dialog title="申请充值" :visible.sync="applyRechargeVisible" width="420px" @close="rechargeForm = {}">
      <el-form :model="rechargeForm" :rules="rechargeRules" ref="rechargeFormRef" label-width="90px">
        <el-form-item label="充值金额" prop="amount">
          <el-input-number v-model="rechargeForm.amount" :min="1" :precision="2" style="width:100%"></el-input-number>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="rechargeForm.applyRemark" type="textarea" :rows="2" placeholder="选填"></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="applyRechargeVisible = false">取消</el-button>
        <el-button type="primary" @click="submitRechargeApply" :loading="rechargeSubmitLoading">提交</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { getMyBalance, getMyDepositPage } from "@/api/deposit";
import { getMyRechargeApplyPage, applyRecharge } from "@/api/rechargeApply";

export default {
  name: "ProfileDeposit",
  data() {
    return {
      loading: false,
      balance: "0.00",
      recordList: [],
      pagination: { current: 1, size: 10, total: 0 },
      rechargeLoading: false,
      rechargeList: [],
      rechargePage: 1,
      rechargeTotal: 0,
      applyRechargeVisible: false,
      rechargeSubmitLoading: false,
      rechargeForm: { amount: null, applyRemark: "" },
      rechargeRules: { amount: [{ required: true, message: "请输入充值金额", trigger: "blur" }] },
    };
  },
  mounted() {
    this.loadBalance();
    this.loadRecords();
    this.loadRechargeList();
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
    async loadRechargeList() {
      this.rechargeLoading = true;
      try {
        const res = await getMyRechargeApplyPage({ current: this.rechargePage, size: 10 });
        this.rechargeList = (res && res.list) ? res.list : [];
        this.rechargeTotal = (res && res.total) ? res.total : 0;
      } catch (e) {
        this.rechargeList = [];
      } finally {
        this.rechargeLoading = false;
      }
    },
    rechargeStatusText(s) {
      const m = { 0: "待审核", 1: "已通过", 2: "已驳回" };
      return m[s] || "-";
    },
    submitRechargeApply() {
      this.$refs.rechargeFormRef.validate(async (valid) => {
        if (!valid) return;
        this.rechargeSubmitLoading = true;
        try {
          await applyRecharge({ amount: this.rechargeForm.amount, applyRemark: this.rechargeForm.applyRemark });
          this.$message.success("申请已提交，请等待财务审核");
          this.applyRechargeVisible = false;
          this.loadRechargeList();
          this.loadBalance();
        } catch (e) {
          this.$message.error(e.message || "申请失败");
        } finally {
          this.rechargeSubmitLoading = false;
        }
      });
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
.apply-btn { margin-top: 12px; }
.record-section h4 { margin: 0 0 16px 0; font-size: 16px; }
.pagination-wrap { margin-top: 16px; }
.amount-plus { color: #67c23a; }
.amount-minus { color: #f56c6c; }
</style>

