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
          <el-form-item label="买家ID">
            <el-input v-model="abnormalQuery.buyerId" placeholder="可选" style="width:140px" />
          </el-form-item>
          <el-form-item label="异常类型">
            <el-select v-model="abnormalQuery.abnormalType" placeholder="选择" style="width:160px">
              <el-option label="恶意出价（1）" :value="1"></el-option>
              <el-option label="机器人（2）" :value="2"></el-option>
            </el-select>
          </el-form-item>
          <el-button type="primary" size="small" @click="loadAbnormal">查询</el-button>
        </el-form>

        <el-table v-loading="abnormalLoading" :data="abnormalList" stripe style="margin-top:10px">
          <el-table-column prop="id" label="记录ID" width="90"></el-table-column>
          <el-table-column prop="goodsName" label="商品" min-width="160"></el-table-column>
          <el-table-column prop="buyerName" label="买家" min-width="120"></el-table-column>
          <el-table-column prop="bidPrice" label="出价" min-width="110"></el-table-column>
          <el-table-column prop="abnormalType" label="异常类型" width="100"></el-table-column>
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
              <el-form-item label="商品ID">
                <el-input v-model="maliciousQuery.goodsId" placeholder="可选" style="width:140px" />
              </el-form-item>
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
              <el-form-item label="商品ID">
                <el-input v-model="ringQuery.goodsId" placeholder="可选" style="width:140px" />
              </el-form-item>
              <el-form-item label="窗口(分钟)">
                <el-input-number v-model="ringQuery.windowMinutes" :min="1" :max="1440" />
              </el-form-item>
              <el-form-item label="阈值(次)">
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
        <el-row :gutter="20">
          <el-col :span="12">
            <h3 class="section-title">用户风险等级标记</h3>
            <el-form :model="riskLevelForm" label-width="120">
              <el-form-item label="用户ID">
                <el-input v-model.number="riskLevelForm.userId" style="width:240px" />
              </el-form-item>
              <el-form-item label="风险等级">
                <el-select v-model="riskLevelForm.riskLevel" style="width:240px">
                  <el-option label="0=正常" :value="0"></el-option>
                  <el-option label="1=低风险" :value="1"></el-option>
                  <el-option label="2=中风险" :value="2"></el-option>
                  <el-option label="3=高风险" :value="3"></el-option>
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="submitRiskLevel">提交</el-button>
              </el-form-item>
            </el-form>
          </el-col>

          <el-col :span="12">
            <h3 class="section-title">账户冻结 / 解封（申请）</h3>
            <el-form :model="riskActionForm" label-width="120">
              <el-form-item label="目标用户ID">
                <el-input v-model.number="riskActionForm.targetUserId" style="width:240px" />
              </el-form-item>
              <el-form-item label="操作类型">
                <el-select v-model="riskActionForm.actionType" style="width:240px">
                  <el-option label="冻结账号（freeze）" value="freeze"></el-option>
                  <el-option label="解封账号（unfreeze）" value="unfreeze"></el-option>
                </el-select>
              </el-form-item>
              <el-form-item label="申请说明">
                <el-input
                  v-model="riskActionForm.remark"
                  type="textarea"
                  :rows="4"
                  style="width:240px"
                  maxlength="1000"
                  show-word-limit
                />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="submitRiskAction">提交申请</el-button>
              </el-form-item>
            </el-form>
          </el-col>
        </el-row>
      </el-tab-pane>

      <el-tab-pane label="日志审计" name="logs">
        <el-form :inline="true" class="toolbar">
          <el-form-item label="模块">
            <el-input v-model="logQuery.operModule" placeholder="可选（如：risk）" style="width:200px" />
          </el-form-item>
          <el-form-item label="关键词">
            <el-input v-model="logQuery.keyword" placeholder="可选" style="width:200px" />
          </el-form-item>
          <el-form-item label="开始时间">
            <el-input v-model="logQuery.startTime" placeholder="YYYY-MM-DD HH:mm:ss" style="width:220px" />
          </el-form-item>
          <el-form-item label="结束时间">
            <el-input v-model="logQuery.endTime" placeholder="YYYY-MM-DD HH:mm:ss" style="width:220px" />
          </el-form-item>
          <el-button type="primary" size="small" @click="loadLogs">查询</el-button>
        </el-form>

        <el-table v-loading="logsLoading" :data="logsList" stripe style="margin-top:10px">
          <el-table-column prop="id" label="ID" width="90"></el-table-column>
          <el-table-column prop="operUserId" label="操作人ID" width="110"></el-table-column>
          <el-table-column prop="operModule" label="模块" width="120"></el-table-column>
          <el-table-column prop="operType" label="类型" width="140"></el-table-column>
          <el-table-column prop="operIp" label="IP" width="120"></el-table-column>
          <el-table-column prop="operContent" label="内容" min-width="220"></el-table-column>
          <el-table-column prop="createTime" label="时间" min-width="170">
            <template slot-scope="scope">{{ formatTime(scope.row.createTime) }}</template>
          </el-table-column>
        </el-table>

        <div class="pagination-wrap" v-if="logPage.total > 0">
          <el-pagination
            :current-page="logPage.current"
            :page-size="logPage.size"
            :total="logPage.total"
            layout="total, prev, pager, next"
            @current-change="handleLogPageChange"
          />
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
  updateUserRiskLevel,
  applyUserStatus,
  getOperLogsPage,
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
        buyerId: "",
        abnormalType: 1,
      },
      abnormalPage: { current: 1, size: 10, total: 0 },

      maliciousLoading: false,
      maliciousList: [],
      maliciousQuery: {
        goodsId: "",
        windowMinutes: 60,
      },

      ringLoading: false,
      ringList: [],
      ringQuery: {
        goodsId: "",
        windowMinutes: 30,
        minBidCount: 5,
      },

      riskLevelForm: {
        userId: null,
        riskLevel: 1,
      },

      riskActionForm: {
        targetUserId: null,
        actionType: "freeze",
        remark: "",
      },

      logsLoading: false,
      logsList: [],
      logQuery: {
        operModule: "risk",
        keyword: "",
        startTime: "",
        endTime: "",
      },
      logPage: { current: 1, size: 10, total: 0 },
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
      this.loadLogs();
    },
    handleAbnormalPageChange(p) {
      this.abnormalPage.current = p;
      this.loadAbnormal();
    },
    handleLogPageChange(p) {
      this.logPage.current = p;
      this.loadLogs();
    },
    async loadAbnormal() {
      this.abnormalLoading = true;
      try {
        const params = {
          current: this.abnormalPage.current,
          size: this.abnormalPage.size,
          abnormalType: this.abnormalQuery.abnormalType,
        };
        if (this.abnormalQuery.goodsId !== "") params.goodsId = Number(this.abnormalQuery.goodsId);
        if (this.abnormalQuery.buyerId !== "") params.buyerId = Number(this.abnormalQuery.buyerId);

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
        if (this.maliciousQuery.goodsId !== "") params.goodsId = Number(this.maliciousQuery.goodsId);
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
        if (this.ringQuery.goodsId !== "") params.goodsId = Number(this.ringQuery.goodsId);
        const res = await getRingBiddersPage(params);
        this.ringList = (res && res.list) || [];
      } catch (e) {
        this.$message.error(e.message || "加载围标/刷价失败");
      } finally {
        this.ringLoading = false;
      }
    },
    async submitRiskLevel() {
      if (!this.riskLevelForm.userId) return this.$message.warning("请填写用户ID");
      try {
        await updateUserRiskLevel(this.riskLevelForm.userId, { riskLevel: this.riskLevelForm.riskLevel });
        this.$message.success("风险等级已更新");
        this.loadLogs();
      } catch (e) {
        this.$message.error(e.message || "更新失败");
      }
    },
    async submitRiskAction() {
      if (!this.riskActionForm.targetUserId) return this.$message.warning("请填写目标用户ID");
      if (!this.riskActionForm.remark || !this.riskActionForm.remark.trim()) return this.$message.warning("请填写申请说明");
      try {
        await applyUserStatus({
          targetUserId: this.riskActionForm.targetUserId,
          actionType: this.riskActionForm.actionType,
          remark: this.riskActionForm.remark,
        });
        this.$message.success("申请已提交，等待管理员复核");
        this.riskActionForm.remark = "";
        this.loadLogs();
      } catch (e) {
        this.$message.error(e.message || "提交失败");
      }
    },
    async loadLogs() {
      this.logsLoading = true;
      try {
        const params = {
          current: this.logPage.current,
          size: this.logPage.size,
          operModule: this.logQuery.operModule,
          keyword: this.logQuery.keyword,
          startTime: this.logQuery.startTime,
          endTime: this.logQuery.endTime,
        };
        const res = await getOperLogsPage(params);
        this.logsList = (res && res.list) || [];
        this.logPage.total = (res && res.total) || 0;
      } catch (e) {
        this.$message.error(e.message || "加载日志失败");
      } finally {
        this.logsLoading = false;
      }
    },
    formatTime(v) {
      if (!v) return "-";
      const d = new Date(v);
      return isNaN(d.getTime()) ? v : d.toLocaleString("zh-CN", { hour12: false });
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
</style>

