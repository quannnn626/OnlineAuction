<template>
  <div class="admin-notification-send-page">
    <div class="page-header">
      <h2>管理沟通通知 - 发起通知</h2>
    </div>

    <el-alert
      v-if="!canSend"
      type="warning"
      :closable="false"
      show-icon
      title="当前账号无权限发起通知（仅管理员/超级管理员）"
      style="margin-bottom: 16px"
    />

    <el-card v-if="canSend" shadow="never" class="card-box">
      <el-form :model="form" label-width="110" class="send-form">
        <el-form-item label="选择用户">
          <el-select
            v-model="form.receiverIds"
            multiple
            filterable
            remote
            reserve-keyword
            placeholder="搜索内部岗位用户（不包含卖家）"
            class="w-520"
            :loading="userSearchLoading"
            :remote-method="handleUserSearch"
          >
            <el-option
              v-for="u in userOptions"
              :key="u.userId"
              :label="`${u.userName}${u.nickName ? '(' + u.nickName + ')' : ''}`"
              :value="u.userId"
            />
          </el-select>
          <div style="margin-top: 8px; color: #666; font-size: 12px">
            已选择：{{ form.receiverIds.length }} 人
          </div>
        </el-form-item>

        <el-form-item label="通知标题">
          <el-input v-model="form.title" placeholder="请输入通知标题" class="w-520" />
        </el-form-item>

        <el-form-item label="通知内容">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="4"
            placeholder="请输入通知内容"
            class="w-520"
          />
        </el-form-item>

        <el-form-item label="通知类型">
          <el-select v-model="form.noticeType" placeholder="请选择通知类型" class="w-240">
            <el-option label="1 - 普通通知（只读）" :value="1"></el-option>
            <el-option label="2 - 重要通知（需确认已读/确认收到）" :value="2"></el-option>
            <el-option label="3 - 沟通通知" :value="3"></el-option>
            <el-option label="4 - 风险告知" :value="4"></el-option>
            <el-option label="5 - 订单/交易提醒" :value="5"></el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="是否需要回复/确认收到">
          <el-checkbox v-model="form.needConfirm">需要“确认收到”</el-checkbox>
          <div style="margin-top: 6px; color: #666; font-size: 12px">
            勾选后，接收方在收件箱会出现“确认收到”按钮。
          </div>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" icon="el-icon-position" :loading="sending" @click="handleSend">
            发送通知
          </el-button>
          <el-button style="margin-left: 12px" icon="el-icon-refresh" @click="handleReset">
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card v-if="canSend" shadow="never" class="card-box" style="margin-top: 16px">
      <div class="section-title">已发出的通知（统计）</div>
      <el-table v-loading="sentLoading" :data="sentTableData" stripe style="width: 100%">
        <el-table-column prop="id" label="通知ID" width="90" />
        <el-table-column prop="noticeTitle" label="标题" min-width="220">
          <template slot-scope="scope">
            <span :title="scope.row.noticeTitle">{{ scope.row.noticeTitle }}</span>
          </template>
        </el-table-column>
        <el-table-column label="类型" width="160">
          <template slot-scope="scope">
            {{ getTypeText(scope.row.noticeType) }}
          </template>
        </el-table-column>
        <el-table-column label="需确认" width="120">
          <template slot-scope="scope">
            <el-tag size="mini" :type="scope.row.needConfirm === 1 ? 'warning' : ''">
              {{ scope.row.needConfirm === 1 ? "是" : "否" }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="接收人" width="120">
          <template slot-scope="scope">
            {{ scope.row.totalReceivers || 0 }}
          </template>
        </el-table-column>
        <el-table-column label="已读" width="120">
          <template slot-scope="scope">
            {{ scope.row.readCount || 0 }}
          </template>
        </el-table-column>
        <el-table-column label="确认收到" width="140">
          <template slot-scope="scope">
            {{ scope.row.confirmedCount || 0 }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template slot-scope="scope">
            <el-button type="text" size="mini" @click="openTargetsDialog(scope.row)">
              查看接收详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-section">
        <el-pagination
          background
          :current-page="sentPagination.current"
          :page-sizes="[10, 20, 50]"
          :page-size="sentPagination.size"
          layout="total, sizes, prev, pager, next"
          :total="sentPagination.total"
          @current-change="handleSentCurrentChange"
          @size-change="handleSentSizeChange"
        />
      </div>
    </el-card>

    <el-dialog
      title="接收详情"
      :visible.sync="targetsDialogVisible"
      width="880px"
      :close-on-click-modal="false"
    >
      <div v-if="targetsDialogHeader" class="targets-header">
        <div><strong>通知ID：</strong>{{ targetsDialogHeader.id }}</div>
        <div><strong>标题：</strong>{{ targetsDialogHeader.noticeTitle }}</div>
        <div>
          <strong>统计：</strong>
          接收 {{ targetsDialogHeader.totalReceivers || 0 }} 人，
          已读 {{ targetsDialogHeader.readCount || 0 }} 人，
          确认收到 {{ targetsDialogHeader.confirmedCount || 0 }} 人
        </div>
      </div>

      <el-table v-loading="targetsLoading" :data="targetsTableData" stripe style="width: 100%">
        <el-table-column prop="receiverId" label="用户ID" width="90" />
        <el-table-column prop="userName" label="用户名" min-width="150" />
        <el-table-column prop="nickName" label="昵称" min-width="140">
          <template slot-scope="scope">{{ scope.row.nickName || "-" }}</template>
        </el-table-column>
        <el-table-column prop="userRole" label="角色" min-width="100">
          <template slot-scope="scope">{{ scope.row.userRole || "-" }}</template>
        </el-table-column>
        <el-table-column label="已读" width="90">
          <template slot-scope="scope">
            <el-tag size="mini" :type="scope.row.isRead === 1 ? 'success' : 'info'">
              {{ scope.row.isRead === 1 ? "是" : "否" }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="确认收到" width="110">
          <template slot-scope="scope">
            <el-tag size="mini" :type="scope.row.isConfirmed === 1 ? 'success' : 'info'">
              {{ scope.row.isConfirmed === 1 ? "是" : "否" }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="readTime" label="已读时间" width="170">
          <template slot-scope="scope">{{ scope.row.readTime || "-" }}</template>
        </el-table-column>
        <el-table-column prop="confirmTime" label="确认时间" width="170">
          <template slot-scope="scope">{{ scope.row.confirmTime || "-" }}</template>
        </el-table-column>
      </el-table>
      <span slot="footer" class="dialog-footer">
        <el-button @click="targetsDialogVisible = false">关闭</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import {
  getSentNotificationTargets,
  getSentNotifications,
  searchStaffUsers,
  sendNotification,
} from "@/api/notification";

export default {
  name: "AdminNotificationsSend",
  data() {
    return {
      canSend: false,
      sending: false,
      userSearchLoading: false,
      userOptions: [],
      form: {
        receiverIds: [],
        title: "",
        content: "",
        noticeType: 2,
        needConfirm: true,
      },
      sentLoading: false,
      sentTableData: [],
      sentPagination: { current: 1, size: 10, total: 0 },
      targetsDialogVisible: false,
      targetsLoading: false,
      targetsDialogHeader: null,
      targetsTableData: [],
    };
  },
  mounted() {
    try {
      const user = JSON.parse(localStorage.getItem("userInfo") || "{}");
      const roles = user.userRole ? String(user.userRole).split(",").map((r) => r.trim()) : [];
      this.canSend = roles.some((r) => ["3", "4"].includes(r));
    } catch (e) {
      this.canSend = false;
    }

    // 拉取“已发统计”
    if (this.canSend) {
      this.loadSentData();
    }
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
    async handleUserSearch(query) {
      const kw = (query || "").trim();
      if (!kw) {
        this.userOptions = [];
        return;
      }
      this.userSearchLoading = true;
      try {
        const list = await searchStaffUsers(kw, 10);
        this.userOptions = Array.isArray(list) ? list : [];
      } catch (e) {
        this.userOptions = [];
      } finally {
        this.userSearchLoading = false;
      }
    },
    async handleSend() {
      if (!this.canSend) return;
      if (!this.form.receiverIds || this.form.receiverIds.length === 0) {
        this.$message.warning("请选择接收用户");
        return;
      }
      if (!this.form.title || !this.form.title.trim()) {
        this.$message.warning("请输入通知标题");
        return;
      }
      if (!this.form.content || !this.form.content.trim()) {
        this.$message.warning("请输入通知内容");
        return;
      }

      this.sending = true;
      try {
        await sendNotification({
          receiverIds: this.form.receiverIds,
          title: this.form.title,
          content: this.form.content,
          noticeType: this.form.noticeType,
          needConfirm: this.form.needConfirm ? 1 : 0,
        });
        this.$message.success("发送成功");
        this.handleReset();
        this.sentPagination.current = 1;
        await this.loadSentData();
      } catch (e) {
        // message 会由拦截器处理，这里兜底
        this.$message.error("发送失败");
      } finally {
        this.sending = false;
      }
    },
    handleReset() {
      this.form.receiverIds = [];
      this.form.title = "";
      this.form.content = "";
      this.form.noticeType = 2;
      this.form.needConfirm = true;
    },
    async loadSentData() {
      this.sentLoading = true;
      try {
        const res = await getSentNotifications(this.sentPagination.current, this.sentPagination.size);
        this.sentTableData = res.list || [];
        this.sentPagination.total = res.total || 0;
      } catch (e) {
        this.sentTableData = [];
      } finally {
        this.sentLoading = false;
      }
    },
    handleSentCurrentChange(val) {
      this.sentPagination.current = val;
      this.loadSentData();
    },
    handleSentSizeChange(val) {
      this.sentPagination.size = val;
      this.sentPagination.current = 1;
      this.loadSentData();
    },
    async openTargetsDialog(row) {
      this.targetsDialogVisible = true;
      this.targetsDialogHeader = { ...row };
      this.targetsTableData = [];
      this.targetsLoading = true;
      try {
        const res = await getSentNotificationTargets(row.id);
        this.targetsTableData = Array.isArray(res) ? res : [];
      } catch (e) {
        this.targetsTableData = [];
      } finally {
        this.targetsLoading = false;
      }
    },
  },
};
</script>

<style scoped>
.admin-notification-send-page {
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

.send-form .w-520 {
  width: 520px;
}

.send-form .w-240 {
  width: 240px;
}

.section-title {
  font-weight: 600;
  margin-bottom: 12px;
}

.pagination-section {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.targets-header {
  margin-bottom: 12px;
  line-height: 1.8;
  color: #333;
}
</style>

