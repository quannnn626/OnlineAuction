<template>
  <div class="apply-seller-page">
    <div class="page-header">
      <h2>申请成为卖家</h2>
      <p class="page-desc">上传资质证明材料，提交后由管理员审核。审核通过后，您即可上架商品参与拍卖。</p>
    </div>
    <el-card shadow="never" class="content-card">
      <div class="status-section">
        <p>
          当前状态：<strong>{{ sellerAuditStatusText }}</strong>
          <span v-if="sellerAuditStatus === 3 && sellerAuditRemark" class="reject-remark">
            驳回原因：{{ sellerAuditRemark }}
          </span>
        </p>
      </div>
      <div v-if="!isSellerUser" class="form-section">
        <p class="tip-text">请上传身份证、营业执照等资质证明材料（支持图片或视频）。</p>
        <el-upload
          :action="uploadAction"
          :file-list="certificateFileList"
          list-type="picture-card"
          name="files"
          :with-credentials="true"
          :on-success="handleUploadSuccess"
          :on-error="handleUploadError"
          :before-upload="beforeUpload"
          :on-remove="handleFileRemove"
          :disabled="sellerAuditStatus === 1"
        >
          <i class="el-icon-plus"></i>
          <div slot="tip" class="el-upload__tip">
            支持图片或视频，单个文件不超过 20MB。待审核期间不可修改。
          </div>
        </el-upload>
        <div class="submit-section">
          <el-button
            type="primary"
            @click="handleSubmit"
            :loading="submitLoading"
            :disabled="sellerAuditStatus === 1 || !canSubmit"
          >
            提交申请
          </el-button>
        </div>
      </div>
      <div v-else class="already-seller">
        <el-result icon="success" title="您已是卖家" subTitle="您已具备卖方资质，可前往「我的商品」上架商品。">
          <template slot="extra">
            <el-button type="primary" @click="$router.push('/my-goods')">去上架商品</el-button>
          </template>
        </el-result>
      </div>
    </el-card>
  </div>
</template>

<script>
import { getCurrentUser } from "@/api/auth";
import { applySeller } from "@/api/user";

export default {
  name: "ApplySeller",
  data() {
    return {
      uploadAction: "/api/OnlineAuction/auctionFile/upload?fileCategory=seller-certificate",
      sellerAuditStatus: null,
      sellerAuditRemark: "",
      certificateFileIds: [],
      certificateFileList: [],
      submitLoading: false,
      isSellerUser: false,
    };
  },
  computed: {
    sellerAuditStatusText() {
      const s = this.sellerAuditStatus;
      if (s === 1) return "待审核";
      if (s === 2) return "审核通过（您已具备卖方资质）";
      if (s === 3) return "审核驳回";
      return "未提交";
    },
    canSubmit() {
      return (
        (this.sellerAuditStatus === null || this.sellerAuditStatus === 0 || this.sellerAuditStatus === 3) &&
        this.certificateFileIds.length > 0
      );
    },
  },
  mounted() {
    this.loadUserInfo();
  },
  methods: {
    async loadUserInfo() {
      try {
        const userInfo = await getCurrentUser();
        if (userInfo) {
          this.sellerAuditStatus = userInfo.sellerAuditStatus;
          this.sellerAuditRemark = userInfo.sellerAuditRemark || "";
          const roles = userInfo.userRole ? String(userInfo.userRole).split(",").map((r) => r.trim()) : [];
          this.isSellerUser = roles.includes("2");
          if (userInfo.sellerCertificateFiles) {
            const ids = userInfo.sellerCertificateFiles.split(",").map((s) => s.trim()).filter(Boolean);
            this.certificateFileIds = ids.map(Number);
            this.certificateFileList = ids.map((id) => ({
              name: `资质文件${id}`,
              url: "",
              id: Number(id),
            }));
          }
        }
      } catch (e) {}
    },
    beforeUpload(file) {
      const isAllowed = file.type.startsWith("image/") || file.type.startsWith("video/");
      if (!isAllowed) {
        this.$message.error("仅支持上传图片或视频作为资质材料");
        return false;
      }
      const isLt20M = file.size / 1024 / 1024 < 20;
      if (!isLt20M) {
        this.$message.error("单个文件大小不能超过 20MB");
        return false;
      }
      return true;
    },
    handleUploadSuccess(response, file, fileList) {
      if (!response || response.code !== 200 || !response.data) {
        this.$message.error(response?.message || "上传失败");
        return;
      }
      const added = Array.isArray(response.data) ? response.data : [response.data];
      added.forEach((f) => {
        if (f && f.id && !this.certificateFileIds.includes(f.id)) {
          this.certificateFileIds.push(f.id);
          this.certificateFileList.push({
            name: f.fileName || f.filePath || `文件${f.id}`,
            url: f.filePath && f.filePath.startsWith("/") ? this.getFileUrl(f.filePath) : f.filePath,
            id: f.id,
          });
        }
      });
    },
    getFileUrl(path) {
      if (!path) return "";
      if (path.startsWith("http")) return path;
      const base = process.env.NODE_ENV === "production" ? "" : "";
      return `${base}${path.startsWith("/") ? path : "/" + path}`;
    },
    handleUploadError() {
      this.$message.error("上传失败，请重试");
    },
    handleFileRemove(file, fileList) {
      const id = file.id || (file.response && file.response.data && (Array.isArray(file.response.data) ? file.response.data[0] : file.response.data).id);
      this.certificateFileList = fileList;
      if (id) {
        this.certificateFileIds = this.certificateFileIds.filter((x) => x !== id);
      }
    },
    async handleSubmit() {
      if (!this.canSubmit) return;
      if (!this.certificateFileIds.length) {
        this.$message.warning("请先上传至少一份资质材料");
        return;
      }
      const userInfo = JSON.parse(localStorage.getItem("userInfo") || "{}");
      const userId = userInfo.id || userInfo.userId;
      if (!userId) {
        this.$message.error("未获取到当前用户ID，请重新登录");
        return;
      }
      this.submitLoading = true;
      try {
        await applySeller(userId, this.certificateFileIds.join(","));
        this.$message.success("申请已提交，请等待管理员审核");
        this.sellerAuditStatus = 1;
        this.sellerAuditRemark = "";
      } catch (e) {
        this.$message.error(e.message || "申请失败");
      } finally {
        this.submitLoading = false;
      }
    },
  },
};
</script>

<style scoped>
.apply-seller-page { padding: 20px; }
.page-header { margin-bottom: 20px; }
.page-header h2 { margin: 0 0 8px 0; font-size: 20px; }
.page-desc { color: #606266; font-size: 14px; margin: 0; }
.content-card { max-width: 800px; }
.status-section { margin-bottom: 16px; }
.reject-remark { color: #f56c6c; margin-left: 8px; }
.tip-text { color: #909399; margin-bottom: 12px; font-size: 14px; }
.submit-section { margin-top: 20px; }
.already-seller { padding: 20px 0; }
</style>
