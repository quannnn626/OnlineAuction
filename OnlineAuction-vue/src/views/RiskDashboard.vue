<template>
  <div class="risk-dashboard-page">
    <div class="page-header">
      <h2>风控中心</h2>
      <el-button icon="el-icon-refresh" size="small" @click="refreshAll">刷新</el-button>
    </div>

    <el-tabs v-model="activeTab">
      <el-tab-pane label="异常出价监控" name="abnormal">
        <el-form :inline="true" class="toolbar">
          <el-form-item label="商品ID">
            <el-input v-model="abnormalQuery.goodsId" placeholder="可选" style="width:140px" />
          </el-form-item>
          <el-form-item label="异常类型">
            <el-select v-model="abnormalQuery.abnormalType" placeholder="选择" clearable style="width:200px">
              <el-option label="全部类型" value=""></el-option>
              <el-option label="秒速出价" :value="1"></el-option>
              <el-option label="超大额跳价" :value="2"></el-option>
              <el-option label="倒计时疯狂加价" :value="3"></el-option>
              <el-option label="频繁顶价" :value="4"></el-option>
              <el-option label="围标嫌疑" :value="5"></el-option>
            </el-select>
          </el-form-item>
          <el-button type="primary" size="small" @click="loadAbnormal">查询</el-button>
        </el-form>

        <el-table v-loading="abnormalLoading" :data="abnormalList" stripe style="margin-top:10px">
          <el-table-column prop="id" label="记录ID" width="90"></el-table-column>
          <el-table-column prop="goodsName" label="商品" min-width="160"></el-table-column>
          <el-table-column prop="buyerName" label="买家" min-width="120"></el-table-column>
          <el-table-column prop="bidPrice" label="出价" min-width="110"></el-table-column>
          <el-table-column prop="riskRuleType" label="异常类型" min-width="130">
            <template slot-scope="scope">{{ formatRiskRuleType(scope.row.riskRuleType) }}</template>
          </el-table-column>
          <el-table-column prop="isAgent" label="代理出价" width="100"></el-table-column>
          <el-table-column prop="bidTime" label="出价时间" min-width="170">
            <template slot-scope="scope">{{ formatTime(scope.row.bidTime) }}</template>
          </el-table-column>
        </el-table>

        <div class="pagination-wrap" v-if="abnormalPage.total > 0">
          <el-pagination
            :current-page="abnormalPage.current"
            :page-size="abnormalPage.size"
            :total="abnormalPage.total"
            layout="total, prev, pager, next"
            @current-change="handleAbnormalPageChange"
          />
        </div>
      </el-tab-pane>

      <el-tab-pane label="恶意/围标" name="patterns">
        <el-row :gutter="20" class="section-row">
          <el-col :span="12">
            <h3 class="section-title">恶意参拍（汇总）</h3>
            <el-form :inline="true" class="toolbar">
              <el-form-item label="窗口(分钟)">
                <el-input-number v-model="maliciousQuery.windowMinutes" :min="1" :max="1440" />
              </el-form-item>
              <el-button size="small" type="primary" @click="loadMalicious">查询</el-button>
            </el-form>
            <el-table v-loading="maliciousLoading" :data="maliciousList" stripe style="margin-top:10px">
              <el-table-column prop="buyerId" label="买家ID" width="90"></el-table-column>
              <el-table-column prop="buyerName" label="买家" min-width="140"></el-table-column>
              <el-table-column prop="bidCount" label="异常出价次数" width="160"></el-table-column>
            </el-table>
          </el-col>

          <el-col :span="12">
            <h3 class="section-title">疑似围标/刷价（汇总）</h3>
            <el-form :inline="true" class="toolbar">
              <el-form-item label="窗口(分钟)">
                <el-input-number v-model="ringQuery.windowMinutes" :min="1" :max="1440" />
              </el-form-item>
              <el-form-item label="阈值(人)">
                <el-input-number v-model="ringQuery.minBidCount" :min="1" :max="1000" />
              </el-form-item>
              <el-button size="small" type="primary" @click="loadRing">查询</el-button>
            </el-form>
            <el-table v-loading="ringLoading" :data="ringList" stripe style="margin-top:10px">
              <el-table-column prop="buyerId" label="买家ID" width="90"></el-table-column>
              <el-table-column prop="buyerName" label="买家" min-width="140"></el-table-column>
              <el-table-column prop="bidCount" label="高频出价次数" width="160"></el-table-column>
            </el-table>
          </el-col>
        </el-row>
      </el-tab-pane>

      <el-tab-pane label="风险等级&冻结解封" name="actions">
        <p class="hint-muted">
          下列用户至少满足其一：存在带风控标记的出价记录，或当前风险等级已标记为非正常。请勾选后批量操作。
        </p>
        <el-table
          ref="riskUserTable"
          v-loading="riskUsersLoading"
          :data="riskUsersList"
          stripe
          row-key="userId"
          style="margin-top:8px"
          @selection-change="onRiskUserSelectionChange"
        >
          <el-table-column type="selection" width="48" :reserve-selection="true"></el-table-column>
          <el-table-column prop="userId" label="用户ID" width="90"></el-table-column>
          <el-table-column prop="nickName" label="昵称" min-width="120"></el-table-column>
          <el-table-column prop="userName" label="用户名" min-width="120"></el-table-column>
          <el-table-column prop="riskLevel" label="当前风险等级" width="120">
            <template slot-scope="scope">{{ formatUserRiskLevel(scope.row.riskLevel) }}</template>
          </el-table-column>
          <el-table-column prop="riskBidCount" label="风控标记出价次数" width="150"></el-table-column>
        </el-table>
        <div class="pagination-wrap" v-if="riskUsersPage.total > 0">
          <el-pagination
            :current-page="riskUsersPage.current"
            :page-size="riskUsersPage.size"
            :total="riskUsersPage.total"
            layout="total, prev, pager, next"
            @current-change="handleRiskUsersPageChange"
          />
        </div>

        <div class="batch-risk-actions">
          <h3 class="section-title">批量更新风险等级</h3>
          <el-form :inline="true" class="toolbar">
            <el-form-item label="风险等级">
              <el-select v-model="riskLevelBatch" style="width:200px">
                <el-option label="正常" :value="0"></el-option>
                <el-option label="低风险" :value="1"></el-option>
                <el-option label="中风险" :value="2"></el-option>
                <el-option label="高风险" :value="3"></el-option>
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="submitRiskLevelBatch">应用到已选用户</el-button>
            </el-form-item>
          </el-form>

          <h3 class="section-title">账户冻结 / 解封</h3>
          <el-alert
            type="info"
            :closable="false"
            show-icon
            title="提交后为每条选中用户各生成一条工单，需管理员复核通过后才会生效。"
            style="margin-bottom:12px"
          />
          <el-form :inline="false" label-width="100px">
            <el-form-item label="操作类型">
              <el-select v-model="freezeForm.actionType" style="width:220px">
                <el-option label="冻结账号" value="freeze"></el-option>
                <el-option label="解封账号" value="unfreeze"></el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="申请说明">
              <el-input
                v-model="freezeForm.remark"
                type="textarea"
                :rows="3"
                style="max-width:480px"
                maxlength="1000"
                show-word-limit
                placeholder="请填写申请原因，管理员将据此复核"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="submitFreezeBatch">提交申请</el-button>
            </el-form-item>
          </el-form>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script>
import {
  getAbnormalBidsPage,
  getMaliciousBiddersPage,
  getRingBiddersPage,
  getRiskActivityUsersPage,
  updateUserRiskLevelBatch,
  applyUserStatusBatch,
} from "@/api/risk";

export default {
  name: "RiskDashboard",
  data() {
    return {
      activeTab: "abnormal",

      abnormalLoading: false,
      abnormalList: [],
      abnormalQuery: {
        goodsId: "",
        abnormalType: "",
      },
      abnormalPage: { current: 1, size: 10, total: 0 },

      maliciousLoading: false,
      maliciousList: [],
      maliciousQuery: {
        windowMinutes: 60,
      },

      ringLoading: false,
      ringList: [],
      ringQuery: {
        windowMinutes: 30,
        minBidCount: 3,
      },

      riskUsersLoading: false,
      riskUsersList: [],
      riskUsersPage: { current: 1, size: 10, total: 0 },
      riskUserSelection: [],
      riskLevelBatch: 1,
      freezeForm: {
        actionType: "freeze",
        remark: "",
      },
    };
  },
  mounted() {
    this.refreshAll();
  },
  methods: {
    refreshAll() {
      this.loadAbnormal();
      this.loadMalicious();
      this.loadRing();
      this.loadRiskUsers();
    },
    handleAbnormalPageChange(p) {
      this.abnormalPage.current = p;
      this.loadAbnormal();
    },
    async loadAbnormal() {
      this.abnormalLoading = true;
      try {
        const params = {
          current: this.abnormalPage.current,
          size: this.abnormalPage.size,
        };
        if (this.abnormalQuery.goodsId !== "") params.goodsId = Number(this.abnormalQuery.goodsId);
        if (
          this.abnormalQuery.abnormalType !== "" &&
          this.abnormalQuery.abnormalType !== null &&
          this.abnormalQuery.abnormalType !== undefined
        ) {
          params.abnormalType = this.abnormalQuery.abnormalType;
        }

        const res = await getAbnormalBidsPage(params);
        this.abnormalList = (res && res.list) || [];
        this.abnormalPage.total = (res && res.total) || 0;
      } catch (e) {
        this.$message.error(e.message || "加载异常出价失败");
      } finally {
        this.abnormalLoading = false;
      }
    },
    async loadMalicious() {
      this.maliciousLoading = true;
      try {
        const params = {
          current: 1,
          size: 10,
          windowMinutes: this.maliciousQuery.windowMinutes,
        };
        const res = await getMaliciousBiddersPage(params);
        this.maliciousList = (res && res.list) || [];
      } catch (e) {
        this.$message.error(e.message || "加载恶意参拍失败");
      } finally {
        this.maliciousLoading = false;
      }
    },
    async loadRing() {
      this.ringLoading = true;
      try {
        const params = {
          current: 1,
          size: 10,
          windowMinutes: this.ringQuery.windowMinutes,
          minBidCount: this.ringQuery.minBidCount,
        };
        const res = await getRingBiddersPage(params);
        this.ringList = (res && res.list) || [];
      } catch (e) {
        this.$message.error(e.message || "加载围标/刷价失败");
      } finally {
        this.ringLoading = false;
      }
    },
    handleRiskUsersPageChange(p) {
      this.riskUsersPage.current = p;
      this.loadRiskUsers();
    },
    onRiskUserSelectionChange(rows) {
      this.riskUserSelection = rows || [];
    },
    selectedRiskUserIds() {
      return (this.riskUserSelection || []).map((r) => Number(r.userId)).filter((id) => !Number.isNaN(id));
    },
    async loadRiskUsers() {
      this.riskUsersLoading = true;
      try {
        const params = {
          current: this.riskUsersPage.current,
          size: this.riskUsersPage.size,
        };
        const res = await getRiskActivityUsersPage(params);
        this.riskUsersList = (res && res.list) || [];
        this.riskUsersPage.total = (res && res.total) || 0;
      } catch (e) {
        this.$message.error(e.message || "加载有风险用户失败");
      } finally {
        this.riskUsersLoading = false;
      }
    },
    async submitRiskLevelBatch() {
      const ids = this.selectedRiskUserIds();
      if (!ids.length) return this.$message.warning("请先勾选用户");
      try {
        await updateUserRiskLevelBatch({ userIds: ids, riskLevel: this.riskLevelBatch });
        this.$message.success("风险等级已更新");
        if (this.$refs.riskUserTable) this.$refs.riskUserTable.clearSelection();
        await this.loadRiskUsers();
      } catch (e) {
        this.$message.error(e.message || "更新失败");
      }
    },
    async submitFreezeBatch() {
      const ids = this.selectedRiskUserIds();
      if (!ids.length) return this.$message.warning("请先勾选用户");
      if (!this.freezeForm.remark || !this.freezeForm.remark.trim()) {
        return this.$message.warning("请填写申请说明");
      }
      try {
        const res = await applyUserStatusBatch({
          userIds: ids,
          actionType: this.freezeForm.actionType,
          remark: this.freezeForm.remark.trim(),
        });
        const n = (res && res.count) || ids.length;
        this.$message.success(`已提交 ${n} 条申请，等待管理员复核`);
        this.freezeForm.remark = "";
        if (this.$refs.riskUserTable) this.$refs.riskUserTable.clearSelection();
      } catch (e) {
        this.$message.error(e.message || "提交失败");
      }
    },
    formatTime(v) {
      if (!v) return "-";
      const d = new Date(v);
      return isNaN(d.getTime()) ? v : d.toLocaleString("zh-CN", { hour12: false });
    },
    formatRiskRuleType(v) {
      const map = {
        1: "秒速出价",
        2: "超大额跳价",
        3: "倒计时疯狂加价",
        4: "频繁顶价",
        5: "围标嫌疑",
      };
      if (v == null || v === 0) return "-";
      return map[v] || "未知";
    },
    formatUserRiskLevel(v) {
      const map = { 0: "正常", 1: "低风险", 2: "中风险", 3: "高风险" };
      if (v == null) return "-";
      const n = Number(v);
      return map[n] !== undefined ? map[n] : String(v);
    },
  },
};
</script>

<style scoped>
.risk-dashboard-page {
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
.section-row {
  margin-top: 4px;
}
.section-title {
  margin: 8px 0 0 0;
  font-size: 14px;
  font-weight: 600;
}
.pagination-wrap {
  margin-top: 14px;
  text-align: right;
}
.hint-muted {
  color: #909399;
  font-size: 13px;
  margin: 0;
  line-height: 1.5;
}
.batch-risk-actions {
  margin-top: 24px;
  padding-top: 16px;
  border-top: 1px solid #ebeef5;
}
</style>

