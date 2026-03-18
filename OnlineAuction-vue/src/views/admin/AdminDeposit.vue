<template>
  <div class="admin-deposit-page">
    <div class="page-header">
      <h2>保证金管理</h2>
      <div>
        <el-button type="primary" icon="el-icon-plus" @click="handleTopUp"
          >手动充值</el-button
        >
        <el-button type="warning" icon="el-icon-lock" @click="openFreezeDialog"
          >冻结</el-button
        >
        <el-button
          type="success"
          icon="el-icon-unlock"
          @click="openUnfreezeDialog"
          >解冻</el-button
        >
      </div>
    </div>

    <!-- 平台汇总卡片 -->
    <div class="summary-cards">
      <el-row :gutter="16">
        <el-col :span="6">
          <el-card shadow="hover" class="summary-card">
            <div class="card-title">保证金总额</div>
            <div class="card-value primary">{{ formatMoney(totalAmount) }}</div>
            <div class="card-desc">可用 + 冻结</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="summary-card">
            <div class="card-title">可用总额</div>
            <div class="card-value success">{{ formatMoney(summary.totalAvailable) }}</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="summary-card">
            <div class="card-title">冻结总额</div>
            <div class="card-value warning">{{ formatMoney(summary.totalFrozen) }}</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="summary-card">
            <div class="card-title">用户数</div>
            <div class="card-value info">{{ summary.userCount || 0 }}</div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <el-tabs v-model="activeTab" @tab-click="onTabClick">
      <!-- 用户汇总 -->
      <el-tab-pane label="用户汇总" name="summary">
        <div class="filter-section">
          <el-form :inline="true" :model="summarySearchForm" class="search-form">
            <el-form-item label="用户名">
              <el-input
                v-model="summarySearchForm.userName"
                placeholder="用户名/昵称"
                clearable
                style="width: 180px"
              ></el-input>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" icon="el-icon-search" @click="loadSummaryData"
                >搜索</el-button
              >
              <el-button icon="el-icon-refresh" @click="resetSummarySearch"
                >重置</el-button
              >
            </el-form-item>
          </el-form>
        </div>
        <div class="table-section">
          <el-table v-loading="summaryLoading" :data="summaryTableData" stripe>
            <el-table-column label="" width="50" align="center">
              <template slot-scope="scope">
                <el-radio v-model="selectedUserId" :label="scope.row.userId">&nbsp;</el-radio>
              </template>
            </el-table-column>
            <el-table-column prop="userId" label="用户ID" width="90"></el-table-column>
            <el-table-column prop="userName" label="用户名" width="120"></el-table-column>
            <el-table-column prop="nickName" label="昵称" width="120">
              <template slot-scope="scope">{{ scope.row.nickName || "-" }}</template>
            </el-table-column>
            <el-table-column prop="availableBalance" label="可用金额" width="110">
              <template slot-scope="scope">{{ formatMoney(scope.row.availableBalance) }}</template>
            </el-table-column>
            <el-table-column prop="frozenAmount" label="冻结金额" width="110">
              <template slot-scope="scope">{{ formatMoney(scope.row.frozenAmount) }}</template>
            </el-table-column>
            <el-table-column prop="totalRecharge" label="总充入" width="110">
              <template slot-scope="scope">{{ formatMoney(scope.row.totalRecharge) }}</template>
            </el-table-column>
            <el-table-column label="操作" width="120" fixed="right">
              <template slot-scope="scope">
                <el-button type="text" size="small" @click="openDetailDrawer(scope.row)"
                  >查看明细</el-button
                >
              </template>
            </el-table-column>
          </el-table>
        </div>
        <div class="pagination-section">
          <el-pagination
            @size-change="handleSummarySizeChange"
            @current-change="handleSummaryCurrentChange"
            :current-page="summaryPagination.current"
            :page-sizes="[10, 20, 50]"
            :page-size="summaryPagination.size"
            layout="total, sizes, prev, pager, next"
            :total="summaryPagination.total"
            background
          ></el-pagination>
        </div>
      </el-tab-pane>

      <!-- 明细记录 -->
      <el-tab-pane label="明细记录" name="detail">
        <div class="filter-section">
          <el-form :inline="true" :model="searchForm" class="search-form">
            <el-form-item label="用户ID">
              <el-input
                v-model="searchForm.userId"
                placeholder="用户ID"
                clearable
                style="width: 120px"
              ></el-input>
            </el-form-item>
            <el-form-item label="类型">
              <el-select
                v-model="searchForm.depositType"
                placeholder="全部"
                clearable
                style="width: 120px"
              >
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
              <el-button type="primary" icon="el-icon-search" @click="handleSearch"
                >搜索</el-button
              >
              <el-button icon="el-icon-refresh" @click="handleReset"
                >重置</el-button
              >
            </el-form-item>
          </el-form>
        </div>
        <div class="table-section">
          <el-table v-loading="loading" :data="tableData" stripe>
            <el-table-column prop="id" label="ID" width="70"></el-table-column>
            <el-table-column prop="userId" label="用户ID" width="90"></el-table-column>
            <el-table-column prop="amount" label="金额" width="100">
              <template slot-scope="scope">
                <span
                  :class="
                    scope.row.depositType === 0 ||
                    scope.row.depositType === 2 ||
                    scope.row.depositType === 6
                      ? 'amount-plus'
                      : 'amount-minus'
                  "
                >
                  {{
                    scope.row.depositType === 0 ||
                    scope.row.depositType === 2 ||
                    scope.row.depositType === 6
                      ? "+"
                      : "-"
                  }}{{ scope.row.amount }}
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
              <template slot-scope="scope">{{ scope.row.orderId || "-" }}</template>
            </el-table-column>
            <el-table-column
              prop="remark"
              label="变动原因"
              min-width="180"
              show-overflow-tooltip
            >
              <template slot-scope="scope">
                <span :title="scope.row.remark">{{ scope.row.remark || "-" }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="operateTime" label="操作时间" width="160">
              <template slot-scope="scope">{{
                formatDateTime(scope.row.operateTime)
              }}</template>
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
      </el-tab-pane>
    </el-tabs>

    <!-- 用户详情抽屉 -->
    <el-drawer
      :title="detailDrawerTitle"
      :visible.sync="detailDrawerVisible"
      size="70%"
      direction="rtl"
      @close="closeDetailDrawer"
    >
      <div v-if="detailUser" class="detail-drawer">
        <div class="detail-summary">
          <p><strong>用户：</strong>{{ detailUser.userName }}（{{ detailUser.nickName || "-" }}）</p>
          <p><strong>可用：</strong>{{ formatMoney(detailUser.availableBalance) }} &nbsp; <strong>冻结：</strong>{{ formatMoney(detailUser.frozenAmount) }} &nbsp; <strong>总充入：</strong>{{ formatMoney(detailUser.totalRecharge) }}</p>
        </div>
        <el-tabs v-model="detailTab">
          <el-tab-pane label="保证金明细" name="deposit">
            <el-table v-loading="detailDepositLoading" :data="detailDepositList" size="small" max-height="400">
              <el-table-column prop="amount" label="金额" width="90">
                <template slot-scope="scope">
                  <span :class="[0,2,6].includes(scope.row.depositType)?'amount-plus':'amount-minus'">
                    {{ [0,2,6].includes(scope.row.depositType)?"+":"-" }}{{ scope.row.amount }}
                  </span>
                </template>
              </el-table-column>
              <el-table-column prop="depositType" label="类型" width="90">
                <template slot-scope="scope">{{ getTypeText(scope.row.depositType) }}</template>
              </el-table-column>
              <el-table-column prop="balance" label="余额" width="90"></el-table-column>
              <el-table-column prop="remark" label="变动原因" min-width="150" show-overflow-tooltip></el-table-column>
              <el-table-column prop="operateTime" label="时间" width="140">
                <template slot-scope="scope">{{ formatDateTime(scope.row.operateTime) }}</template>
              </el-table-column>
            </el-table>
          </el-tab-pane>
          <el-tab-pane label="关联订单" name="order">
            <el-table v-loading="detailOrderLoading" :data="detailOrderList" size="small" max-height="400">
              <el-table-column prop="orderNo" label="订单号" width="150"></el-table-column>
              <el-table-column prop="goodsName" label="商品" min-width="120" show-overflow-tooltip></el-table-column>
              <el-table-column prop="depositAmount" label="保证金" width="90"></el-table-column>
              <el-table-column prop="orderStatus" label="状态" width="90">
                <template slot-scope="scope">{{ getOrderStatusText(scope.row.orderStatus) }}</template>
              </el-table-column>
              <el-table-column prop="createTime" label="创建时间" width="140">
                <template slot-scope="scope">{{ formatDateTime(scope.row.createTime) }}</template>
              </el-table-column>
            </el-table>
          </el-tab-pane>
          <el-tab-pane label="竞拍记录" name="record">
            <el-table v-loading="detailRecordLoading" :data="detailRecordList" size="small" max-height="400">
              <el-table-column prop="goodsName" label="商品" min-width="120" show-overflow-tooltip></el-table-column>
              <el-table-column prop="bidPrice" label="出价" width="90"></el-table-column>
              <el-table-column prop="bidTime" label="出价时间" width="140">
                <template slot-scope="scope">{{ formatDateTime(scope.row.bidTime) }}</template>
              </el-table-column>
            </el-table>
          </el-tab-pane>
        </el-tabs>
      </div>
    </el-drawer>

    <el-dialog title="冻结保证金" :visible.sync="freezeVisible" width="460px" @close="onDialogClose('freeze')">
      <el-form :model="freezeForm" :rules="freezeRules" ref="freezeFormRef" label-width="90px">
        <el-form-item label="选择用户" prop="userId">
          <el-select
            v-model="freezeForm.userId"
            filterable
            remote
            placeholder="输入用户名或昵称搜索"
            :remote-method="searchUsersRemote"
            :loading="userSearchLoading"
            style="width: 100%"
            clearable
          >
            <el-option
              v-for="u in userOptions"
              :key="u.userId"
              :label="userOptionLabel(u)"
              :value="u.userId"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="冻结金额" prop="amount">
          <el-input-number v-model="freezeForm.amount" :min="0.01" :precision="2" style="width: 100%"></el-input-number>
        </el-form-item>
        <el-form-item label="冻结原因">
          <el-input v-model="freezeForm.remark" type="textarea" :rows="2" placeholder="必填，系统将记录"></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="freezeVisible = false">取消</el-button>
        <el-button type="primary" @click="submitFreeze" :loading="freezeLoading">确定</el-button>
      </span>
    </el-dialog>
    <el-dialog title="解冻保证金" :visible.sync="unfreezeVisible" width="460px" @close="onDialogClose('unfreeze')">
      <el-form :model="unfreezeForm" :rules="unfreezeRules" ref="unfreezeFormRef" label-width="90px">
        <el-form-item label="选择用户" prop="userId">
          <el-select
            v-model="unfreezeForm.userId"
            filterable
            remote
            placeholder="输入用户名或昵称搜索"
            :remote-method="searchUsersRemote"
            :loading="userSearchLoading"
            style="width: 100%"
            clearable
          >
            <el-option
              v-for="u in userOptions"
              :key="u.userId"
              :label="userOptionLabel(u)"
              :value="u.userId"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="解冻金额" prop="amount">
          <el-input-number v-model="unfreezeForm.amount" :min="0.01" :precision="2" style="width: 100%"></el-input-number>
        </el-form-item>
        <el-form-item label="解冻原因">
          <el-input v-model="unfreezeForm.remark" type="textarea" :rows="2" placeholder="必填，系统将记录"></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="unfreezeVisible = false">取消</el-button>
        <el-button type="primary" @click="submitUnfreeze" :loading="unfreezeLoading">确定</el-button>
      </span>
    </el-dialog>
    <el-dialog title="手动充值" :visible.sync="topUpVisible" width="460px" @close="onDialogClose('topUp')">
      <el-form :model="topUpForm" :rules="topUpRules" ref="topUpFormRef" label-width="90px">
        <el-form-item label="选择用户" prop="userId">
          <el-select
            v-model="topUpForm.userId"
            filterable
            remote
            placeholder="输入用户名或昵称搜索"
            :remote-method="searchUsersRemote"
            :loading="userSearchLoading"
            style="width: 100%"
            clearable
          >
            <el-option
              v-for="u in userOptions"
              :key="u.userId"
              :label="userOptionLabel(u)"
              :value="u.userId"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="充值金额" prop="amount">
          <el-input-number v-model="topUpForm.amount" :min="0.01" :precision="2" placeholder="请输入金额" style="width: 100%"></el-input-number>
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
import {
  getPlatformSummary,
  getDepositSummaryPage,
  getAdminDepositPage,
  searchUsersForDeposit,
  manualTopUp,
  freezeDeposit,
  unfreezeDeposit,
} from "@/api/deposit";
import { getAdminOrderPage } from "@/api/order";
import { getAdminRecordsByBuyerIdPage } from "@/api/record";

export default {
  name: "AdminDeposit",
  data() {
    return {
      activeTab: "summary",
      summary: { totalAvailable: 0, totalFrozen: 0, userCount: 0 },
      summaryLoading: false,
      summaryTableData: [],
      summarySearchForm: { userName: "" },
      summaryPagination: { current: 1, size: 10, total: 0 },
      loading: false,
      tableData: [],
      searchForm: { userId: "", depositType: undefined },
      pagination: { current: 1, size: 10, total: 0 },
      topUpVisible: false,
      topUpLoading: false,
      topUpForm: { userId: null, amount: null, remark: "" },
      topUpRules: {
        userId: [{ required: true, message: "请选择用户", trigger: "change" }],
        amount: [{ required: true, message: "请输入充值金额", trigger: "blur" }],
      },
      freezeVisible: false,
      freezeLoading: false,
      freezeForm: { userId: null, amount: null, remark: "" },
      freezeRules: {
        userId: [{ required: true, message: "请选择用户", trigger: "change" }],
        amount: [{ required: true, message: "请输入冻结金额", trigger: "blur" }],
      },
      unfreezeVisible: false,
      unfreezeLoading: false,
      unfreezeForm: { userId: null, amount: null, remark: "" },
      unfreezeRules: {
        userId: [{ required: true, message: "请选择用户", trigger: "change" }],
        amount: [{ required: true, message: "请输入解冻金额", trigger: "blur" }],
      },
      detailDrawerVisible: false,
      detailUser: null,
      detailTab: "deposit",
      detailDepositLoading: false,
      detailDepositList: [],
      detailOrderLoading: false,
      detailOrderList: [],
      detailRecordLoading: false,
      detailRecordList: [],
      userOptions: [],
      userSearchLoading: false,
      selectedUserId: null,
    };
  },
  computed: {
    totalAmount() {
      const a = Number(this.summary.totalAvailable) || 0;
      const f = Number(this.summary.totalFrozen) || 0;
      return a + f;
    },
    detailDrawerTitle() {
      return this.detailUser
        ? `保证金详情 - ${this.detailUser.userName}（${this.detailUser.nickName || "-"}）`
        : "保证金详情";
    },
  },
  mounted() {
    this.loadSummary();
    this.loadSummaryData();
    this.loadData();
  },
  methods: {
    searchUsersRemote(query) {
      const kw = query ? String(query).trim() : "";
      this.userSearchLoading = true;
      searchUsersForDeposit({ keyword: kw || undefined, limit: 20 })
        .then((res) => {
          this.userOptions = Array.isArray(res) ? res : [];
        })
        .catch(() => { this.userOptions = []; })
        .finally(() => { this.userSearchLoading = false; });
    },
    userOptionLabel(u) {
      if (!u) return "";
      const name = u.userName || "";
      const nick = u.nickName ? `（${u.nickName}）` : "";
      return name ? `${name}${nick}` : (nick || "-");
    },
    onDialogClose(type) {
      this.userOptions = [];
      if (type === "freeze") this.freezeForm = { userId: null, amount: null, remark: "" };
      else if (type === "unfreeze") this.unfreezeForm = { userId: null, amount: null, remark: "" };
      else if (type === "topUp") this.topUpForm = { userId: null, amount: null, remark: "" };
    },
    loadSummary() {
      getPlatformSummary()
        .then((res) => {
          if (res) this.summary = res;
        })
        .catch(() => {});
    },
    onTabClick() {
      if (this.activeTab === "summary") this.loadSummaryData();
      else this.loadData();
    },
    async loadSummaryData() {
      this.summaryLoading = true;
      try {
        const res = await getDepositSummaryPage({
          current: this.summaryPagination.current,
          size: this.summaryPagination.size,
          userName: this.summarySearchForm.userName || undefined,
        });
        this.summaryTableData = res && res.list ? res.list : [];
        this.summaryPagination.total = res && res.total ? res.total : 0;
        this.loadSummary();
      } catch (e) {
        this.summaryTableData = [];
      } finally {
        this.summaryLoading = false;
      }
    },
    resetSummarySearch() {
      this.summarySearchForm.userName = "";
      this.summaryPagination.current = 1;
      this.loadSummaryData();
    },
    handleSummarySizeChange(v) {
      this.summaryPagination.size = v;
      this.summaryPagination.current = 1;
      this.loadSummaryData();
    },
    handleSummaryCurrentChange(v) {
      this.summaryPagination.current = v;
      this.loadSummaryData();
    },
    openDetailDrawer(row) {
      this.detailUser = row;
      this.detailDrawerVisible = true;
      this.detailTab = "deposit";
      this.loadDetailDeposit();
      this.loadDetailOrder();
      this.loadDetailRecord();
    },
    closeDetailDrawer() {
      this.detailUser = null;
      this.detailDepositList = [];
      this.detailOrderList = [];
      this.detailRecordList = [];
    },
    async loadDetailDeposit() {
      if (!this.detailUser || !this.detailUser.userId) return;
      this.detailDepositLoading = true;
      try {
        const res = await getAdminDepositPage({
          current: 1,
          size: 100,
          userId: this.detailUser.userId,
        });
        this.detailDepositList = res && res.list ? res.list : [];
      } catch (e) {
        this.detailDepositList = [];
      } finally {
        this.detailDepositLoading = false;
      }
    },
    async loadDetailOrder() {
      if (!this.detailUser || !this.detailUser.userId) return;
      this.detailOrderLoading = true;
      try {
        const res = await getAdminOrderPage({
          current: 1,
          size: 100,
          buyerId: this.detailUser.userId,
        });
        this.detailOrderList = res && res.list ? res.list : [];
      } catch (e) {
        this.detailOrderList = [];
      } finally {
        this.detailOrderLoading = false;
      }
    },
    async loadDetailRecord() {
      if (!this.detailUser || !this.detailUser.userId) return;
      this.detailRecordLoading = true;
      try {
        const res = await getAdminRecordsByBuyerIdPage(this.detailUser.userId, {
          current: 1,
          size: 100,
        });
        this.detailRecordList = res && res.list ? res.list : [];
      } catch (e) {
        this.detailRecordList = [];
      } finally {
        this.detailRecordLoading = false;
      }
    },
    getOrderStatusText(s) {
      const m = { 0: "待付款", 1: "待发货", 2: "待收货", 3: "已完成", 4: "已悔拍", 5: "已退款" };
      return m[s] || "-";
    },
    formatMoney(v) {
      if (v == null || v === "" || isNaN(Number(v))) return "0.00";
      return Number(v).toFixed(2);
    },
    async loadData() {
      this.loading = true;
      try {
        const res = await getAdminDepositPage({
          current: this.pagination.current,
          size: this.pagination.size,
          userId: this.searchForm.userId ? parseInt(this.searchForm.userId) : undefined,
          depositType: this.searchForm.depositType,
        });
        this.tableData = res && res.list ? res.list : [];
        this.pagination.total = res && res.total ? res.total : 0;
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
      if (!this.selectedUserId) {
        this.$message.warning("请先在用户汇总中选择用户");
        this.activeTab = "summary";
        return;
      }
      const row = this.summaryTableData.find((r) => r.userId == this.selectedUserId);
      this.topUpForm = { userId: this.selectedUserId, amount: null, remark: "" };
      this.userOptions = row ? [row] : [{ userId: this.selectedUserId, userName: "已选用户", nickName: "" }];
      this.topUpVisible = true;
      this.$nextTick(() => {
        if (this.userOptions.length === 0) this.searchUsersRemote("");
      });
    },
    submitTopUp() {
      this.$refs.topUpFormRef.validate(async (valid) => {
        if (!valid) return;
        this.topUpLoading = true;
        try {
          await manualTopUp(this.topUpForm);
          this.$message.success("充值成功");
          this.topUpVisible = false;
          this.loadSummary();
          this.loadSummaryData();
          this.loadData();
        } catch (e) {
          this.$message.error(e.message || e.msg || "充值失败");
        } finally {
          this.topUpLoading = false;
        }
      });
    },
    openFreezeDialog() {
      if (!this.selectedUserId) {
        this.$message.warning("请先在用户汇总中选择用户");
        this.activeTab = "summary";
        return;
      }
      const row = this.summaryTableData.find((r) => r.userId == this.selectedUserId);
      this.freezeForm = { userId: this.selectedUserId, amount: null, remark: "" };
      this.userOptions = row ? [row] : [{ userId: this.selectedUserId, userName: "已选用户", nickName: "" }];
      this.freezeVisible = true;
      this.$nextTick(() => {
        if (this.userOptions.length === 0) this.searchUsersRemote("");
      });
    },
    submitFreeze() {
      this.$refs.freezeFormRef.validate(async (valid) => {
        if (!valid) return;
        this.freezeLoading = true;
        try {
          await freezeDeposit(this.freezeForm);
          this.$message.success("冻结成功");
          this.freezeVisible = false;
          this.loadSummary();
          this.loadSummaryData();
          this.loadData();
        } catch (e) {
          this.$message.error(e.message || e.msg || "冻结失败");
        } finally {
          this.freezeLoading = false;
        }
      });
    },
    openUnfreezeDialog() {
      if (!this.selectedUserId) {
        this.$message.warning("请先在用户汇总中选择用户");
        this.activeTab = "summary";
        return;
      }
      const row = this.summaryTableData.find((r) => r.userId == this.selectedUserId);
      this.unfreezeForm = { userId: this.selectedUserId, amount: null, remark: "" };
      this.userOptions = row ? [row] : [{ userId: this.selectedUserId, userName: "已选用户", nickName: "" }];
      this.unfreezeVisible = true;
      this.$nextTick(() => {
        if (this.userOptions.length === 0) this.searchUsersRemote("");
      });
    },
    submitUnfreeze() {
      this.$refs.unfreezeFormRef.validate(async (valid) => {
        if (!valid) return;
        this.unfreezeLoading = true;
        try {
          await unfreezeDeposit(this.unfreezeForm);
          this.$message.success("解冻成功");
          this.unfreezeVisible = false;
          this.loadSummary();
          this.loadSummaryData();
          this.loadData();
        } catch (e) {
          this.$message.error(e.message || e.msg || "解冻失败");
        } finally {
          this.unfreezeLoading = false;
        }
      });
    },
    getTypeText(t) {
      const map = {
        0: "充值",
        1: "冻结",
        2: "解冻",
        3: "抵扣尾款",
        4: "扣除(悔拍)",
        5: "财务冻结",
        6: "财务解冻",
      };
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
.admin-deposit-page {
  padding: 20px;
}
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.page-header h2 {
  margin: 0;
  font-size: 20px;
}
.summary-cards {
  margin-bottom: 20px;
}
.summary-card {
  text-align: center;
}
.card-title {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}
.card-value {
  font-size: 22px;
  font-weight: 600;
}
.card-value.primary {
  color: #409eff;
}
.card-value.success {
  color: #67c23a;
}
.card-value.warning {
  color: #e6a23c;
}
.card-value.info {
  color: #909399;
}
.card-desc {
  font-size: 12px;
  color: #c0c4cc;
  margin-top: 4px;
}
.filter-section {
  margin-bottom: 16px;
}
.table-section {
  background: #fff;
  padding: 20px;
  border-radius: 4px;
}
.pagination-section {
  margin-top: 16px;
  text-align: right;
}
.amount-plus {
  color: #67c23a;
}
.amount-minus {
  color: #f56c6c;
}
.detail-drawer .detail-summary {
  padding: 0 0 16px 0;
  border-bottom: 1px solid #ebeef5;
  margin-bottom: 16px;
}
.detail-drawer .detail-summary p {
  margin: 8px 0;
}
</style>
