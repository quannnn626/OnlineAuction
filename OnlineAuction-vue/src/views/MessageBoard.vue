<template>
  <div class="message-board-page">
    <div class="page-header">
      <h2>留言板</h2>
      <el-button type="primary" icon="el-icon-edit" @click="handleAddMessage">
        发布留言
      </el-button>
    </div>

    <!-- 留言列表 -->
    <div class="message-list">
      <el-card
        v-for="message in messageList"
        :key="message.id"
        class="message-card"
        shadow="hover"
      >
        <div class="message-header">
          <div class="user-info">
            <el-avatar :src="message.userAvatar || defaultAvatar" :size="40">
              {{ message.userName ? message.userName.charAt(0) : 'U' }}
            </el-avatar>
            <div class="user-details">
              <div class="user-name">{{ message.userName || '匿名用户' }}</div>
              <div class="message-time">{{ formatDateTime(message.createTime) }}</div>
            </div>
          </div>
          <div class="message-actions" v-if="isMyMessage(message)">
            <el-button
              type="text"
              icon="el-icon-edit"
              @click="handleEditMessage(message)"
            >
              编辑
            </el-button>
            <el-button
              type="text"
              icon="el-icon-delete"
              @click="handleDeleteMessage(message)"
            >
              删除
            </el-button>
          </div>
        </div>
        <div class="message-content">
          <p>{{ message.messageContent }}</p>
        </div>
        <!-- 管理员回复 -->
        <div v-if="message.replyContent" class="message-reply">
          <div class="reply-header">
            <i class="el-icon-service"></i>
            <span>管理员回复</span>
            <span class="reply-time">{{ formatDateTime(message.replyTime) }}</span>
          </div>
          <div class="reply-content">
            {{ message.replyContent }}
          </div>
        </div>
      </el-card>

      <!-- 空状态 -->
      <el-empty
        v-if="!loading && messageList.length === 0"
        description="暂无留言"
      ></el-empty>
    </div>

    <!-- 分页 -->
    <div class="pagination-section" v-if="messageList.length > 0">
      <el-pagination
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="pagination.current"
        :page-sizes="[10, 20, 50]"
        :page-size="pagination.size"
        layout="total, sizes, prev, pager, next, jumper"
        :total="pagination.total"
        background
      >
      </el-pagination>
    </div>

    <!-- 发布/编辑留言对话框 -->
    <el-dialog
      :title="dialogTitle"
      :visible.sync="dialogVisible"
      width="600px"
      @close="handleDialogClose"
    >
      <el-form
        ref="messageForm"
        :model="formData"
        :rules="formRules"
        label-width="80px"
      >
        <el-form-item label="留言内容" prop="messageContent">
          <el-input
            v-model="formData.messageContent"
            type="textarea"
            :rows="6"
            placeholder="请输入留言内容（最多500字）"
            maxlength="500"
            show-word-limit
          ></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">
          确定
        </el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  getMessagePage,
  addMessage,
  updateMessage,
  deleteMessage,
} from "@/api/message";

export default {
  name: "MessageBoard",
  data() {
    return {
      loading: false,
      submitLoading: false,
      messageList: [],
      pagination: {
        current: 1,
        size: 10,
        total: 0,
      },
      dialogVisible: false,
      dialogTitle: "发布留言",
      isEdit: false,
      formData: {
        id: null,
        messageContent: "",
      },
      formRules: {
        messageContent: [
          { required: true, message: "请输入留言内容", trigger: "blur" },
          { min: 1, max: 500, message: "留言内容长度在 1 到 500 个字符", trigger: "blur" },
        ],
      },
      defaultAvatar: "",
      currentUserId: null,
    };
  },
  mounted() {
    this.loadCurrentUser();
    this.loadMessages();
  },
  methods: {
    // 加载当前用户信息
    loadCurrentUser() {
      const userInfo = localStorage.getItem("userInfo");
      if (userInfo) {
        const user = JSON.parse(userInfo);
        this.currentUserId = user.id || user.userId || null;
      }
    },
    async loadMessages() {
      this.loading = true;
      try {
        const res = await getMessagePage({
          current: this.pagination.current,
          size: this.pagination.size,
        });
        this.messageList = (res && res.list) ? res.list : [];
        this.pagination.total = (res && res.total) ? res.total : 0;
      } catch (e) {
        this.messageList = [];
        this.pagination.total = 0;
      } finally {
        this.loading = false;
      }
    },
    // 判断是否是自己的留言
    isMyMessage(message) {
      return message.userId === this.currentUserId;
    },
    // 发布留言
    handleAddMessage() {
      this.dialogTitle = "发布留言";
      this.isEdit = false;
      this.formData = {
        id: null,
        messageContent: "",
      };
      this.dialogVisible = true;
    },
    // 编辑留言
    handleEditMessage(message) {
      this.dialogTitle = "编辑留言";
      this.isEdit = true;
      this.formData = {
        id: message.id,
        messageContent: message.messageContent,
      };
      this.dialogVisible = true;
    },
    // 删除留言
    handleDeleteMessage(message) {
      this.$confirm(`确定要删除这条留言吗？`, "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
      })
        .then(async () => {
          await deleteMessage(message.id);
          this.$message.success("删除成功");
          this.loadMessages();
        })
        .catch(() => {
          // 用户取消
        });
    },
    // 提交表单
    handleSubmit() {
      this.$refs.messageForm.validate((valid) => {
        if (!valid) return;
        this.submitLoading = true;
        const req = this.isEdit
          ? updateMessage(this.formData.id, { messageContent: this.formData.messageContent })
          : addMessage({ messageContent: this.formData.messageContent });
        req
          .then(() => {
            this.$message.success(this.isEdit ? "修改成功" : "发布成功");
            this.dialogVisible = false;
            this.loadMessages();
          })
          .catch(() => {})
          .finally(() => {
            this.submitLoading = false;
          });
      });
    },
    // 对话框关闭
    handleDialogClose() {
      if (this.$refs.messageForm) {
        this.$refs.messageForm.resetFields();
      }
      this.formData = {
        id: null,
        messageContent: "",
      };
      this.isEdit = false;
    },
    // 分页大小变化
    handleSizeChange(size) {
      this.pagination.size = size;
      this.pagination.current = 1;
      this.loadMessages();
    },
    // 当前页变化
    handleCurrentChange(current) {
      this.pagination.current = current;
      this.loadMessages();
    },
    // 格式化日期时间
    formatDateTime(timeStr) {
      if (!timeStr) return "";
      const date = new Date(timeStr);
      if (isNaN(date.getTime())) return timeStr;
      const year = date.getFullYear();
      const month = (date.getMonth() + 1).toString().padStart(2, "0");
      const day = date.getDate().toString().padStart(2, "0");
      const hours = date.getHours().toString().padStart(2, "0");
      const minutes = date.getMinutes().toString().padStart(2, "0");
      return `${year}-${month}-${day} ${hours}:${minutes}`;
    },
  },
};
</script>

<style scoped>
.message-board-page {
  padding: 20px;
  background: #f5f5f5;
  min-height: calc(100vh - 60px);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.page-header h2 {
  margin: 0;
  color: #333;
}

.message-list {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.message-card {
  margin-bottom: 20px;
}

.message-card:last-child {
  margin-bottom: 0;
}

.message-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 15px;
}

.user-info {
  display: flex;
  align-items: center;
}

.user-details {
  margin-left: 12px;
}

.user-name {
  font-weight: 500;
  color: #333;
  margin-bottom: 4px;
}

.message-time {
  font-size: 12px;
  color: #999;
}

.message-actions {
  display: flex;
  gap: 10px;
}

.message-content {
  color: #666;
  line-height: 1.6;
  margin-bottom: 10px;
}

.message-content p {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-word;
}

.message-reply {
  margin-top: 15px;
  padding: 15px;
  background: #f8f9fa;
  border-left: 3px solid #409eff;
  border-radius: 4px;
}

.reply-header {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
  font-size: 14px;
  color: #409eff;
  font-weight: 500;
}

.reply-header i {
  margin-right: 6px;
}

.reply-time {
  margin-left: auto;
  font-size: 12px;
  color: #999;
  font-weight: normal;
}

.reply-content {
  color: #666;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
}

.pagination-section {
  display: flex;
  justify-content: center;
  margin-top: 20px;
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}
</style>
