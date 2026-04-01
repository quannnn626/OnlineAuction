<template>
  <div class="admin-notification-inbox-page">
    <div class="page-header">
      <h2>管理沟通通知 - 收件箱</h2>
      <el-button icon="el-icon-refresh" size="small" @click="loadData">刷新</el-button>
    </div>

    <el-alert
      v-if="!canViewInbox"
      type="warning"
      :closable="false"
      show-icon
      title="当前账号无权限查看收件箱"
      style="margin-bottom: 16px"
    />

    <el-card v-if="canViewInbox" shadow="never" class="card-box">
      <el-table v-loading="loading" :data="tableData" stripe style="width: 100%">
        <el-table-column prop="notificationId" label="通知ID" width="90" />

        <el-table-column prop="title" label="标题" min-width="260" show-overflow-tooltip>
          <template slot-scope="scope">
            <span :title="scope.row.title">{{ scope.row.title }}</span>
          </template>
        </el-table-column>

        <el-table-column label="类型" width="160">
          <template slot-scope="scope">
            {{ getTypeText(scope.row.noticeType) }}
          </template>
        </el-table-column>

        <el-table-column label="是否需要确认收到" width="180">
          <template slot-scope="scope">
            <el-tag size="mini" :type="scope.row.needConfirm === 1 ? 'warning' : ''">
              {{ scope.row.needConfirm === 1 ? "是" : "否" }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="状态" width="180">
          <template slot-scope="scope">
            <div>
              <div>已读：{{ scope.row.isRead === 1 ? "是" : "否" }}</div>
              <div v-if="scope.row.needConfirm === 1">
                确认收到：{{ scope.row.isConfirmed === 1 ? "是" : "否" }}
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="createTime" label="发布时间" width="180" />

        <el-table-column label="操作" width="220" fixed="right">
          <template slot-scope="scope">
            <el-button type="text" size="mini" @click="openDetail(scope.row)">查看详情</el-button>

            <el-button
              v-if="scope.row.needConfirm === 1"
              type="primary"
              size="mini"
              :disabled="scope.row.isConfirmed === 1"
              @click="handleConfirm(scope.row.notificationId)"
            >
              确认收到
            </el-button>

            <el-button
              v-else
              type="text"
              size="mini"
              :disabled="scope.row.isRead === 1"
              @click="handleConfirm(scope.row.notificationId)"
            >
              {{ scope.row.isRead === 1 ? "已读" : "标记已读" }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-section">
        <el-pagination
          background
          :current-page="pagination.current"
          :page-sizes="[10, 20, 50]"
          :page-size="pagination.size"
          layout="total, sizes, prev, pager, next"
          :total="pagination.total"
          @current-change="handleCurrentChange"
          @size-change="handleSizeChange"
        />
      </div>
    </el-card>

    <el-dialog
      title="通知详情"
      :visible.sync="detailDialogVisible"
      width="720px"
      :close-on-click-modal="false"
    >
      <div v-if="currentDetail" class="detail-wrap">
        <div class="detail-row">
          <span class="label">通知ID：</span>
          <span>{{ currentDetail.notificationId }}</span>
        </div>
        <div class="detail-row">
          <span class="label">标题：</span>
          <span>{{ currentDetail.title || "-" }}</span>
        </div>
        <div class="detail-row">
          <span class="label">类型：</span>
          <span>{{ getTypeText(currentDetail.noticeType) }}</span>
        </div>
        <div class="detail-row">
          <span class="label">发布时间：</span>
          <span>{{ currentDetail.createTime || "-" }}</span>
        </div>
        <div class="detail-row">
          <span class="label">是否需确认：</span>
          <span>{{ currentDetail.needConfirm === 1 ? "是" : "否" }}</span>
        </div>
        <div class="detail-row">
          <span class="label">状态：</span>
          <span>
            已读：{{ currentDetail.isRead === 1 ? "是" : "否" }}
            <template v-if="currentDetail.needConfirm === 1">
              ，确认收到：{{ currentDetail.isConfirmed === 1 ? "是" : "否" }}
            </template>
          </span>
        </div>
        <div class="detail-row content-row">
          <span class="label">内容：</span>
          <div class="content-box">{{ currentDetail.content || "-" }}</div>
        </div>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="detailDialogVisible = false">关闭</el-button>
        <el-button
          v-if="currentDetail"
          type="primary"
          :disabled="
            currentDetail.needConfirm === 1
              ? currentDetail.isConfirmed === 1
              : currentDetail.isRead === 1
          "
          @click="handleConfirmFromDetail"
        >
          {{
            currentDetail && currentDetail.needConfirm === 1
              ? currentDetail.isConfirmed === 1
                ? "已确认收到"
                : "确认收到"
              : currentDetail && currentDetail.isRead === 1
              ? "已读"
              : "标记已读"
          }}
        </el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { confirmNotification, getInboxNotifications } from "@/api/notification";

export default {
  name: "AdminNotificationsInbox",
  data() {
    return {
      canViewInbox: false,
      loading: false,
      tableData: [],
      pagination: { current: 1, size: 10, total: 0 },
      detailDialogVisible: false,
      currentDetail: null,
    };
  },
  mounted() {
    try {
      const user = JSON.parse(localStorage.getItem("userInfo") || "{}");
      const roles = user.userRole ? String(user.userRole).split(",").map((r) => r.trim()) : [];
      this.canViewInbox = roles.some((r) => ["5", "6", "7", "8", "9", "10"].includes(r));
    } catch (e) {
      this.canViewInbox = false;
    }
    this.loadData();
  },
  methods: {
    getTypeText(type) {
      const map = {
        1: "普通通知（只读）",
        2: "重要通知",
        3: "沟通通知",
        4: "风险告知",
        5: "订单/交易提醒",
      };
      return map[type] || "未知";
    },
    async loadData() {
      if (!this.canViewInbox) return;
      this.loading = true;
      try {
        const res = await getInboxNotifications(this.pagination.current, this.pagination.size);
        this.tableData = res.list || [];
        this.pagination.total = res.total || 0;
      } catch (e) {
        this.$message.error("加载收件箱失败");
      } finally {
        this.loading = false;
      }
    },
    handleCurrentChange(val) {
      this.pagination.current = val;
      this.loadData();
    },
    handleSizeChange(val) {
      this.pagination.size = val;
      this.pagination.current = 1;
      this.loadData();
    },
    async handleConfirm(notificationId) {
      try {
        await confirmNotification(notificationId);
        this.$message.success("操作成功");
        await this.loadData();
        if (this.currentDetail && this.currentDetail.notificationId === notificationId) {
          const latest = this.tableData.find((r) => r.notificationId === notificationId);
          if (latest) this.currentDetail = { ...latest };
        }
      } catch (e) {
        this.$message.error("操作失败");
      }
    },
    openDetail(row) {
      this.currentDetail = { ...row };
      this.detailDialogVisible = true;
    },
    async handleConfirmFromDetail() {
      if (!this.currentDetail) return;
      await this.handleConfirm(this.currentDetail.notificationId);
    },
  },
};
</script>

<style scoped>
.admin-notification-inbox-page {
  padding: 16px;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.card-box {
  width: 100%;
}

.pagination-section {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.detail-wrap {
  font-size: 14px;
  line-height: 1.8;
}

.detail-row {
  margin-bottom: 10px;
  display: flex;
  align-items: flex-start;
}

.detail-row .label {
  width: 100px;
  color: #666;
  flex-shrink: 0;
}

.content-row .content-box {
  white-space: pre-wrap;
  word-break: break-word;
  background: #f8f9fb;
  border-radius: 4px;
  padding: 10px 12px;
  width: 100%;
}
</style>

