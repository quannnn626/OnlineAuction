<template>
  <div class="message-center-page">
    <div class="page-header">
      <h2>消息中心</h2>
      <p class="page-desc">
        客服咨询：普通用户/卖方可就某商品向客服发起咨询；管理沟通：管理员/超管可与内部角色对话
      </p>
    </div>

    <div class="content-layout">
      <!-- 左侧：会话列表 -->
      <div class="session-list">
        <div class="session-list-header">
          <span>我的会话</span>
          <el-button
            v-if="isAdminOrSuperAdmin"
            type="text"
            size="small"
            @click="adminSessionVisible = true"
          >
            <i class="el-icon-plus"></i> 发起管理沟通
          </el-button>
          <el-button
            type="text"
            size="small"
            @click="loadSessions"
            :loading="sessionsLoading"
          >
            <i class="el-icon-refresh"></i> 刷新
          </el-button>
        </div>
        <div v-loading="sessionsLoading" class="session-items">
          <div
            v-for="s in sessionList"
            :key="s.id"
            class="session-item"
            :class="{ active: currentSessionId === s.id }"
            @click="selectSession(s)"
          >
            <div class="session-goods">
              {{ s.goodsName || (s.sessionType === 2 ? "管理沟通" : "商品") }}
            </div>
            <div class="session-meta">
              <template v-if="s.sessionType === 2">
                <span
                  >对方:
                  {{
                    (s.userId === currentUserId ? s.serviceName : s.userName) ||
                    "-"
                  }}</span
                >
              </template>
              <template v-else>
                <span v-if="s.serviceName">客服: {{ s.serviceName }}</span>
                <span v-else class="text-warning">待分配</span>
              </template>
              <el-tag v-if="s.sessionStatus === 1" type="info" size="mini"
                >已关闭</el-tag
              >
            </div>
          </div>
          <el-empty
            v-if="!sessionsLoading && sessionList.length === 0"
            description="暂无会话"
          ></el-empty>
        </div>
      </div>

      <!-- 右侧：消息区域 -->
      <div class="message-area">
        <template v-if="currentSessionId">
          <div class="message-header">
            <span>{{
              currentSessionDetail.goodsName ||
              (currentSessionDetail.sessionType === 2 ? "管理沟通" : "会话")
            }}</span>
            <el-button
              v-if="isCustomerService && currentSessionDetail.sessionType === 1 && currentSessionDetail.userId"
              type="text"
              size="small"
              @click="goToUserOrders"
            >
              查看用户订单
            </el-button>
            <el-button
              v-if="canCloseSession"
              type="text"
              size="small"
              @click="handleCloseSession"
            >
              关闭会话
            </el-button>
          </div>
          <div class="message-list" ref="messageListRef">
            <div
              v-for="m in messageList"
              :key="m.id"
              class="message-item"
              :class="{ 'is-mine': m.senderId === currentUserId }"
            >
              <div class="msg-meta">
                <span class="sender">{{ m.senderName }}</span>
                <span class="time">{{ formatTime(m.createTime) }}</span>
              </div>
              <div class="msg-body">
                <template v-if="m.contentType === 1">
                  <p class="msg-text">{{ m.content }}</p>
                </template>
                <template v-else-if="m.contentType === 2">
                  <p class="msg-order">订单信息: {{ m.content }}</p>
                </template>
                <template v-else-if="m.contentType === 3">
                  <template v-if="m.filePath">
                    <img
                      v-if="m.fileType === 'image'"
                      :src="getFileUrl(m.filePath)"
                      class="msg-file"
                    />
                    <video
                      v-else-if="m.fileType === 'video'"
                      :src="getFileUrl(m.filePath)"
                      controls
                      class="msg-video"
                    ></video>
                    <a v-else :href="getFileUrl(m.filePath)" target="_blank" rel="noopener">{{
                      m.fileName || "附件"
                    }}</a>
                  </template>
                  <span v-else class="msg-file-missing">[附件]</span>
                </template>
              </div>
            </div>
          </div>
          <div
            v-if="currentSessionDetail.sessionStatus !== 1"
            class="message-input"
          >
            <el-input
              v-model="inputContent"
              type="textarea"
              :rows="3"
              placeholder="输入消息内容..."
              maxlength="500"
              show-word-limit
              @keydown.enter.ctrl="sendText"
            ></el-input>
            <div class="input-actions">
              <el-upload
                :action="uploadUrl"
                name="files"
                :show-file-list="false"
                :with-credentials="true"
                :on-success="handleFileSuccess"
                :on-error="handleFileError"
                :before-upload="beforeFileUpload"
              >
                <el-button size="small" icon="el-icon-paperclip">
                  发送图片/视频
                </el-button>
              </el-upload>
              <el-button
                type="primary"
                size="small"
                @click="sendText"
                :loading="sendLoading"
              >
                发送
              </el-button>
            </div>
          </div>
        </template>
        <div v-else class="message-empty">
          <el-empty :description="emptyHint"></el-empty>
        </div>
      </div>
    </div>

    <el-dialog
      title="发起管理沟通"
      :visible.sync="adminSessionVisible"
      width="400px"
    >
      <p class="dialog-tip">
        与卖方、客服、拍卖师、财务、运营建立对话（不可与普通用户沟通）
      </p>
      <el-form label-width="100px">
        <el-form-item label="目标用户ID">
          <el-input
            v-model.number="adminTargetUserId"
            placeholder="输入目标用户的ID"
            type="number"
          ></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="adminSessionVisible = false">取消</el-button>
        <el-button
          type="primary"
          @click="handleCreateAdminSession"
          :loading="adminSessionLoading"
          >确定</el-button
        >
      </span>
    </el-dialog>
  </div>
</template>

<script>
import {
  getOrCreateSession,
  getOrCreateAdminSession,
  getSessions,
  getSessionDetail,
  getSessionMessages,
  sendMessage,
  closeSession,
} from "@/api/messageCenter";

export default {
  name: "MessageCenter",
  computed: {
    canCloseSession() {
      const d = this.currentSessionDetail;
      const uid = this.currentUserId;
      return (
        uid &&
        d &&
        d.sessionStatus !== 1 &&
        (d.userId === uid || d.serviceId === uid)
      );
    },
    emptyHint() {
      if (this.isAdminOrSuperAdmin) {
        return "请从左侧选择会话，或点击「发起管理沟通」与后台用户对话";
      }
      return "请从左侧选择会话，或从商品详情页点击「咨询客服」发起咨询";
    },
    isCustomerService() {
      try {
        const user = JSON.parse(localStorage.getItem("userInfo") || "{}");
        const roles = user.userRole ? String(user.userRole).split(",").map((r) => r.trim()) : [];
        return roles.includes("6");
      } catch (e) {
        return false;
      }
    },
  },
  data() {
    return {
      sessionList: [],
      sessionsLoading: false,
      currentSessionId: null,
      currentSessionDetail: {},
      messageList: [],
      messageLoading: false,
      inputContent: "",
      sendLoading: false,
      currentUserId: null,
      isAdminOrSuperAdmin: false,
      adminSessionVisible: false,
      adminTargetUserId: null,
      adminSessionLoading: false,
      uploadUrl: "/api/OnlineAuction/auctionFile/upload?fileCategory=message",
      pollTimer: null,
    };
  },
  mounted() {
    const user = JSON.parse(localStorage.getItem("userInfo") || "{}");
    this.currentUserId = user.id || user.userId;
    this.isAdminOrSuperAdmin =
      user.userRole &&
      (String(user.userRole).includes("3") ||
        String(user.userRole).includes("4"));
    this.loadSessions();
    const goodsId = this.$route.query.goodsId;
    if (goodsId) {
      getOrCreateSession(Number(goodsId))
        .then((session) => {
          this.loadSessions();
          if (session && session.id) {
            this.selectSession(session);
          }
        })
        .catch(() => {});
    }
  },
  beforeDestroy() {
    if (this.pollTimer) clearInterval(this.pollTimer);
  },
  methods: {
    async loadSessions() {
      this.sessionsLoading = true;
      try {
        const res = await getSessions({ current: 1, size: 50 });
        this.sessionList = res && res.list ? res.list : [];
      } catch (e) {
        this.sessionList = [];
      } finally {
        this.sessionsLoading = false;
      }
    },
    async selectSession(s) {
      this.currentSessionId = s.id;
      this.currentSessionDetail = s;
      try {
        const detail = await getSessionDetail(s.id);
        this.currentSessionDetail = detail || s;
      } catch (e) {}
      this.loadMessages();
      if (this.pollTimer) clearInterval(this.pollTimer);
      this.pollTimer = setInterval(() => this.loadMessages(), 5000);
    },
    async loadMessages() {
      if (!this.currentSessionId) return;
      this.messageLoading = true;
      try {
        const list = await getSessionMessages(this.currentSessionId);
        this.messageList = list || [];
        this.$nextTick(() => this.scrollToBottom());
      } catch (e) {
        this.messageList = [];
      } finally {
        this.messageLoading = false;
      }
    },
    scrollToBottom() {
      const el = this.$refs.messageListRef;
      if (el) el.scrollTop = el.scrollHeight;
    },
    async sendText() {
      const content = (this.inputContent || "").trim();
      if (!content) return;
      this.sendLoading = true;
      try {
        await sendMessage({
          sessionId: this.currentSessionId,
          contentType: 1,
          content,
        });
        this.inputContent = "";
        this.loadMessages();
      } catch (e) {
        this.$message.error(e.message || "发送失败");
      } finally {
        this.sendLoading = false;
      }
    },
    handleFileSuccess(res, file) {
      if (!res || res.code !== 200 || !res.data) {
        this.$message.error(res?.message || "上传失败");
        return;
      }
      const fileId = Array.isArray(res.data) ? res.data[0]?.id : res.data?.id;
      if (!fileId) {
        this.$message.error("上传成功但未获取到文件ID");
        return;
      }
      sendMessage({
        sessionId: this.currentSessionId,
        contentType: 3,
        fileId,
        content: "",
      })
        .then(() => {
          this.loadMessages();
          this.$message.success("附件已发送");
        })
        .catch((e) => {
          this.$message.error(e?.message || "发送附件失败");
        });
    },
    handleFileError(err, file, fileList) {
      this.$message.error(file?.name ? `上传 ${file.name} 失败` : "上传失败");
    },
    beforeFileUpload(file) {
      const isImage = file.type.startsWith("image/");
      const isVideo = file.type.startsWith("video/");
      if (!isImage && !isVideo) {
        this.$message.error("仅支持图片或视频");
        return false;
      }
      if (file.size > 20 * 1024 * 1024) {
        this.$message.error("文件大小不能超过 20MB");
        return false;
      }
      return true;
    },
    goToUserOrders() {
      const d = this.currentSessionDetail;
      if (!d || !d.userId) return;
      const userName = d.userName && d.userName !== "-" ? encodeURIComponent(d.userName) : "";
      this.$router.push({
        path: "/message/service-orders",
        query: { userId: d.userId, userName },
      });
    },
    async handleCreateAdminSession() {
      const targetId = this.adminTargetUserId;
      if (!targetId) {
        this.$message.warning("请输入目标用户ID");
        return;
      }
      this.adminSessionLoading = true;
      try {
        const session = await getOrCreateAdminSession(targetId);
        this.adminSessionVisible = false;
        this.adminTargetUserId = null;
        this.loadSessions();
        if (session && session.id) {
          this.selectSession(session);
        }
        this.$message.success("已发起沟通");
      } catch (e) {
        this.$message.error(e.message || "发起失败");
      } finally {
        this.adminSessionLoading = false;
      }
    },
    async handleCloseSession() {
      try {
        await this.$confirm("确定关闭该会话？", "提示", { type: "warning" });
        await closeSession(this.currentSessionId);
        this.$message.success("已关闭");
        this.currentSessionId = null;
        this.loadSessions();
      } catch (e) {}
    },
    formatTime(t) {
      if (!t) return "";
      const d = new Date(t);
      return isNaN(d.getTime())
        ? t
        : d.toLocaleString("zh-CN", { hour12: false });
    },
    getFileUrl(path) {
      if (!path) return "";
      if (path.startsWith("http")) return path;
      if (path.startsWith("/upload")) return path;
      return (process.env.VUE_APP_BASE_API || "") + (path.startsWith("/") ? path : "/" + path);
    },
  },
};
</script>

<style scoped>
.message-center-page {
  padding: 20px;
  min-height: calc(100vh - 120px);
}
.page-header {
  margin-bottom: 16px;
}
.page-desc {
  color: #909399;
  font-size: 13px;
  margin: 4px 0 0;
}
.content-layout {
  display: flex;
  gap: 16px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  overflow: hidden;
  min-height: 500px;
}
.dialog-tip {
  color: #909399;
  font-size: 12px;
  margin-bottom: 16px;
}
.session-list {
  width: 280px;
  border-right: 1px solid #ebeef5;
  display: flex;
  flex-direction: column;
}
.session-list-header {
  padding: 12px 16px;
  border-bottom: 1px solid #ebeef5;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.session-items {
  flex: 1;
  overflow-y: auto;
}
.session-item {
  padding: 12px 16px;
  border-bottom: 1px solid #f5f7fa;
  cursor: pointer;
}
.session-item:hover {
  background: #f5f7fa;
}
.session-item.active {
  background: #ecf5ff;
  border-left: 3px solid #409eff;
}
.session-goods {
  font-weight: 500;
  color: #303133;
}
.session-meta {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
.text-warning {
  color: #e6a23c;
}
.message-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}
.message-header {
  padding: 12px 16px;
  border-bottom: 1px solid #ebeef5;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  max-height: 360px;
}
.message-item {
  margin-bottom: 16px;
}
.message-item.is-mine .msg-body {
  background: #ecf5ff;
  margin-left: 40px;
  margin-right: 0;
}
.msg-meta {
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}
.msg-body {
  background: #f5f7fa;
  padding: 10px 12px;
  border-radius: 8px;
  margin-right: 40px;
  display: inline-block;
}
.msg-text,
.msg-order {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-word;
}
.msg-file {
  max-width: 200px;
  max-height: 200px;
  border-radius: 4px;
}
.msg-video {
  max-width: 300px;
  max-height: 200px;
  border-radius: 4px;
}
.msg-file-missing {
  color: #909399;
  font-size: 12px;
}
.message-input {
  padding: 12px 16px;
  border-top: 1px solid #ebeef5;
}
.input-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 8px;
}
.message-empty {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
