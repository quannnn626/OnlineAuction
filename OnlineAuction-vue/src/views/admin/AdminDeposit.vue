<template>
  <div class="admin-deposit-page">
    <div class="page-header">
      <h2>保证金管理</h2>
      <div>
        <el-button type="primary" icon="el-icon-plus" @click="handleTopUp">手动充值</el-button>
        <el-button type="warning" icon="el-icon-lock" @click="openFreezeDialog">冻结</el-button>
        <el-button type="success" icon="el-icon-unlock" @click="openUnfreezeDialog">解冻</el-button>
      </div>
    </div>
    <div class="filter-section">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="用户ID">
          <el-input v-model="searchForm.userId" placeholder="用户ID" clearable style="width: 120px"></el-input>
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="searchForm.depositType" placeholder="全部" clearable style="width: 120px">
            <el-option label="充值" :value="0"></el-option>
            <el-option label="冻结" :value="1"></el-option>
            <el-option label="解冻" :value="2"></el-option>
            <el-option label="抵扣尾款" :value="3"></el-option>
            <el-option label="扣除(悔拍)" :value="4"></el-option>
          <el-option label="财务冻结" :value="5"></el-option>
          <el-option label="财务解冻" :value="6"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" @click="handleSearch">搜索</el-button>
          <el-button icon="el-icon-refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>
    <el-dialog title="冻结保证金" :visible.sync="freezeVisible" width="420px" @close="freezeForm = {}">
      <el-form :model="freezeForm" :rules="freezeRules" ref="freezeFormRef" label-width="90px">
        <el-form-item label="用户ID" prop="userId">
          <el-input-number v-model="freezeForm.userId" :min="1" placeholder="用户ID" style="width:100%"></el-input-number>
        </el-form-item>
        <el-form-item label="冻结金额" prop="amount">
          <el-input-number v-model="freezeForm.amount" :min="0.01" :precision="2" style="width:100%"></el-input-number>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="freezeForm.remark" type="textarea" :rows="2" placeholder="可选"></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="freezeVisible = false">取消</el-button>
        <el-button type="primary" @click="submitFreeze" :loading="freezeLoading">确定</el-button>
      </span>
    </el-dialog>
    <el-dialog title="解冻保证金" :visible.sync="unfreezeVisible" width="420px" @close="unfreezeForm = {}">
      <el-form :model="unfreezeForm" :rules="unfreezeRules" ref="unfreezeFormRef" label-width="90px">
        <el-form-item label="用户ID" prop="userId">
          <el-input-number v-model="unfreezeForm.userId" :min="1" placeholder="用户ID" style="width:100%"></el-input-number>
        </el-form-item>
        <el-form-item label="解冻金额" prop="amount">
          <el-input-number v-model="unfreezeForm.amount" :min="0.01" :precision="2" style="width:100%"></el-input-number>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="unfreezeForm.remark" type="textarea" :rows="2" placeholder="可选"></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="unfreezeVisible = false">取消</el-button>
        <el-button type="primary" @click="submitUnfreeze" :loading="unfreezeLoading">确定</el-button>
      </span>
    </el-dialog>
    <div class="table-section">
      <el-table v-loading="loading" :data="tableData" stripe>
        <el-table-column prop="id" label="ID" width="70"></el-table-column>
        <el-table-column prop="userId" label="用户ID" width="90"></el-table-column>
        <el-table-column prop="amount" label="金额" width="100">
          <template slot-scope="scope">
            <span :class="(scope.row.depositType === 0 || scope.row.depositType === 2 || scope.row.depositType === 6) ? 'amount-plus' : 'amount-minus'">
              {{ (scope.row.depositType === 0 || scope.row.depositType === 2 || scope.row.depositType === 6) ? '+' : '-' }}{{ scope.row.amount }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="depositType" label="类型" width="100">
          <template slot-scope="scope">
            {{ getTypeText(scope.row.depositType) }}
          </template>
        </el-table-column>
        <el-table-column prop="balance" label="操作后余额" width="110"></el-table-column>
        <el-table-column prop="orderId" label="订单ID" width="90">
          <template slot-scope="scope">{{ scope.row.orderId || '-' }}</template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="150" show-overflow-tooltip></el-table-column>
        <el-table-column prop="operateTime" label="操作时间" width="160">
          <template slot-scope="scope">{{ formatDateTime(scope.row.operateTime) }}</template>
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

    <el-dialog title="手动充值" :visible.sync="topUpVisible" width="450px" @close="topUpForm = {}">
      <el-form :model="topUpForm" :rules="topUpRules" ref="topUpFormRef" label-width="90px">
        <el-form-item label="用户ID" prop="userId">
          <el-input-number v-model="topUpForm.userId" :min="1" placeholder="请输入用户ID" style="width:100%"></el-input-number>
        </el-form-item>
        <el-form-item label="充值金额" prop="amount">
          <el-input-number v-model="topUpForm.amount" :min="0.01" :precision="2" placeholder="请输入金额" style="width:100%"></el-input-number>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="topUpForm.remark" type="textarea" :rows="2" placeholder="可选"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="topUpVisible = false">取消</el-button>
        <el-button type="primary" @click="submitTopUp" :loading="topUpLoading">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getAdminDepositPage, manualTopUp, freezeDeposit, unfreezeDeposit } from "@/api/deposit";

export default {
  name: "AdminDeposit",
  data() {
    return {
      loading: false,
      tableData: [],
      searchForm: { userId: "", depositType: undefined },
      pagination: { current: 1, size: 10, total: 0 },
      topUpVisible: false,
      topUpLoading: false,
      topUpForm: { userId: null, amount: null, remark: "" },
      topUpRules: {
        userId: [{ required: true, message: "请输入用户ID", trigger: "blur" }],
        amount: [{ required: true, message: "请输入充值金额", trigger: "blur" }],
      },
      freezeVisible: false,
      freezeLoading: false,
      freezeForm: { userId: null, amount: null, remark: "" },
      freezeRules: {
        userId: [{ required: true, message: "请输入用户ID", trigger: "blur" }],
        amount: [{ required: true, message: "请输入冻结金额", trigger: "blur" }],
      },
      unfreezeVisible: false,
      unfreezeLoading: false,
      unfreezeForm: { userId: null, amount: null, remark: "" },
      unfreezeRules: {
        userId: [{ required: true, message: "请输入用户ID", trigger: "blur" }],
        amount: [{ required: true, message: "请输入解冻金额", trigger: "blur" }],
      },
    };
  },
  mounted() {
    this.loadData();
  },
  methods: {
    async loadData() {
      this.loading = true;
      try {
        const res = await getAdminDepositPage({
          current: this.pagination.current,
          size: this.pagination.size,
          userId: this.searchForm.userId ? parseInt(this.searchForm.userId) : undefined,
          depositType: this.searchForm.depositType,
        });
        this.tableData = (res && res.list) ? res.list : [];
        this.pagination.total = (res && res.total) ? res.total : 0;
      } catch (e) {
        this.tableData = [];
      } finally {
        this.loading = false;
      }
    },
    handleSearch() {
      this.pagination.current = 1;
      this.loadData();
    },
    handleReset() {
      this.searchForm = { userId: "", depositType: undefined };
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
    handleTopUp() {
      this.topUpForm = { userId: null, amount: null, remark: "" };
      this.topUpVisible = true;
    },
    submitTopUp() {
      this.$refs.topUpFormRef.validate(async (valid) => {
        if (!valid) return;
        this.topUpLoading = true;
        try {
          await manualTopUp(this.topUpForm);
          this.$message.success("充值成功");
          this.topUpVisible = false;
          this.loadData();
        } catch (e) {
          this.$message.error(e.message || "充值失败");
        } finally {
          this.topUpLoading = false;
        }
      });
    },
    openFreezeDialog() {
      this.freezeForm = { userId: null, amount: null, remark: "" };
      this.freezeVisible = true;
    },
    submitFreeze() {
      this.$refs.freezeFormRef.validate(async (valid) => {
        if (!valid) return;
        this.freezeLoading = true;
        try {
          await freezeDeposit(this.freezeForm);
          this.$message.success("冻结成功");
          this.freezeVisible = false;
          this.loadData();
        } catch (e) {
          this.$message.error(e.message || "冻结失败");
        } finally {
          this.freezeLoading = false;
        }
      });
    },
    openUnfreezeDialog() {
      this.unfreezeForm = { userId: null, amount: null, remark: "" };
      this.unfreezeVisible = true;
    },
    submitUnfreeze() {
      this.$refs.unfreezeFormRef.validate(async (valid) => {
        if (!valid) return;
        this.unfreezeLoading = true;
        try {
          await unfreezeDeposit(this.unfreezeForm);
          this.$message.success("解冻成功");
          this.unfreezeVisible = false;
          this.loadData();
        } catch (e) {
          this.$message.error(e.message || "解冻失败");
        } finally {
          this.unfreezeLoading = false;
        }
      });
    },
    getTypeText(t) {
      const map = { 0: "充值", 1: "冻结", 2: "解冻", 3: "抵扣尾款", 4: "扣除(悔拍)", 5: "财务冻结", 6: "财务解冻" };
      return map[t] || "-";
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
.admin-deposit-page { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-header h2 { margin: 0; font-size: 20px; }
.filter-section { margin-bottom: 20px; }
.table-section { background: #fff; padding: 20px; border-radius: 4px; }
.pagination-section { margin-top: 20px; text-align: right; }
.amount-plus { color: #67c23a; }
.amount-minus { color: #f56c6c; }
</style>
