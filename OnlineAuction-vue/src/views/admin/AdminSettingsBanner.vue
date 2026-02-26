<template>
  <div class="admin-settings-banner-page">
    <div class="page-header">
      <h2>首页轮播图管理</h2>
      <div class="header-actions" v-if="canManage">
        <el-button type="primary" icon="el-icon-plus" @click="handleAdd">
          新增轮播图
        </el-button>
      </div>
    </div>

    <div class="table-section">
      <el-table v-loading="loading" :data="tableData" stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="70"></el-table-column>
        <el-table-column label="轮播图" width="160">
          <template slot-scope="scope">
            <el-image
              v-if="scope.row.bannerImg"
              :src="getImageUrl(scope.row.bannerImg)"
              fit="cover"
              style="width: 120px; height: 60px; border-radius: 4px"
              :preview-src-list="[getImageUrl(scope.row.bannerImg)]"
            >
            </el-image>
            <span v-else class="no-image">暂无图片</span>
          </template>
        </el-table-column>
        <el-table-column prop="goodsId" label="关联商品ID" width="100">
          <template slot-scope="scope">
            {{ scope.row.goodsId || "-" }}
          </template>
        </el-table-column>
        <el-table-column prop="bannerSort" label="排序" width="80"></el-table-column>
        <el-table-column prop="bannerStatus" label="状态" width="100">
          <template slot-scope="scope">
            <el-tag :type="scope.row.bannerStatus === 1 ? 'success' : 'info'">
              {{ scope.row.bannerStatus === 1 ? "已启用" : "未启用" }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160">
          <template slot-scope="scope">
            {{ formatDateTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template slot-scope="scope">
            <el-button
              v-if="canManage"
              size="mini"
              type="primary"
              icon="el-icon-edit"
              @click="handleEdit(scope.row)"
            >
              编辑
            </el-button>
            <el-button
              v-if="canManage"
              size="mini"
              :type="scope.row.bannerStatus === 1 ? 'warning' : 'success'"
              :icon="scope.row.bannerStatus === 1 ? 'el-icon-turn-off' : 'el-icon-open'"
              @click="handleToggleStatus(scope.row)"
            >
              {{ scope.row.bannerStatus === 1 ? "停用" : "启用" }}
            </el-button>
            <el-button
              v-if="canDelete"
              size="mini"
              type="danger"
              icon="el-icon-delete"
              @click="handleDelete(scope.row)"
            >
              删除
            </el-button>
            <span v-if="!canManage && !canDelete" class="view-only">仅可查看</span>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-empty v-if="!loading && tableData.length === 0" description="暂无轮播图"></el-empty>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      :title="dialogTitle"
      :visible.sync="dialogVisible"
      width="560px"
      @close="handleDialogClose"
    >
      <el-form
        :model="formData"
        :rules="formRules"
        ref="bannerForm"
        label-width="100px"
      >
        <el-form-item label="轮播图" prop="bannerImg">
          <el-upload
            class="banner-uploader"
            :action="uploadAction"
            :show-file-list="false"
            :with-credentials="true"
            :on-success="handleUploadSuccess"
            :before-upload="beforeUpload"
            accept="image/*"
            name="files"
          >
            <img v-if="formData.bannerImg" :src="getImageUrl(formData.bannerImg)" class="banner-preview" />
            <i v-else class="el-icon-plus banner-uploader-icon"></i>
          </el-upload>
          <div class="upload-tip">建议尺寸 1920x400，支持 jpg/png，单文件最大 10MB</div>
        </el-form-item>
        <el-form-item label="关联商品ID" prop="goodsId">
          <el-input-number
            v-model="formData.goodsId"
            :min="0"
            placeholder="可选，点击轮播图跳转的商品ID，0 表示不关联"
            style="width: 100%"
          ></el-input-number>
        </el-form-item>
        <el-form-item label="排序值" prop="bannerSort">
          <el-input-number
            v-model="formData.bannerSort"
            :min="0"
            placeholder="数字越小越靠前"
            style="width: 100%"
          ></el-input-number>
        </el-form-item>
        <el-form-item label="状态" prop="bannerStatus">
          <el-radio-group v-model="formData.bannerStatus">
            <el-radio :label="0">未启用</el-radio>
            <el-radio :label="1">已启用</el-radio>
          </el-radio-group>
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
  getAdminBannerList,
  getBannerById,
  addBanner,
  updateBanner,
  deleteBanner,
} from "@/api/banner";

export default {
  name: "AdminSettingsBanner",
  data() {
    return {
      loading: false,
      submitLoading: false,
      tableData: [],
      dialogVisible: false,
      dialogTitle: "新增轮播图",
      isEdit: false,
      formData: {
        id: null,
        bannerImg: "",
        goodsId: null,
        bannerSort: 0,
        bannerStatus: 1,
      },
      formRules: {
        bannerImg: [
          { required: true, message: "请上传轮播图", trigger: "change" },
        ],
      },
      uploadAction: "/api/OnlineAuction/auctionFile/upload?fileCategory=banner",
    };
  },
  computed: {
    canManage() {
      const user = this.getUserInfo();
      if (!user) return false;
      const roles = String(user.userRole || "").split(",").map((r) => r.trim());
      return user.isAdmin || user.isSuperAdmin || roles.includes("8");
    },
    canDelete() {
      const user = this.getUserInfo();
      if (!user) return false;
      return user.isAdmin || user.isSuperAdmin;
    },
  },
  mounted() {
    this.loadData();
  },
  methods: {
    getUserInfo() {
      try {
        const raw = localStorage.getItem("userInfo");
        return raw ? JSON.parse(raw) : null;
      } catch (e) {
        return null;
      }
    },
    async loadData() {
      this.loading = true;
      try {
        const res = await getAdminBannerList();
        this.tableData = Array.isArray(res) ? res : [];
      } catch (error) {
        this.$message.error(error.message || "加载失败");
        this.tableData = [];
      } finally {
        this.loading = false;
      }
    },
    handleAdd() {
      this.dialogTitle = "新增轮播图";
      this.isEdit = false;
      this.formData = {
        id: null,
        bannerImg: "",
        goodsId: null,
        bannerSort: this.tableData.length,
        bannerStatus: 1,
      };
      this.dialogVisible = true;
    },
    async handleEdit(row) {
      try {
        const res = await getBannerById(row.id);
        this.dialogTitle = "编辑轮播图";
        this.isEdit = true;
        this.formData = {
          id: res.id,
          bannerImg: res.bannerImg || "",
          goodsId: res.goodsId || null,
          bannerSort: res.bannerSort ?? 0,
          bannerStatus: res.bannerStatus ?? 1,
        };
        this.dialogVisible = true;
      } catch (error) {
        this.$message.error(error.message || "获取详情失败");
      }
    },
    handleSubmit() {
      this.$refs.bannerForm.validate(async (valid) => {
        if (!valid) return;
        this.submitLoading = true;
        try {
          const data = { ...this.formData };
          if (data.goodsId === 0) data.goodsId = null;
          if (this.formData.id) {
            await updateBanner(this.formData.id, data);
            this.$message.success("更新成功");
          } else {
            await addBanner(data);
            this.$message.success("新增成功");
          }
          this.dialogVisible = false;
          this.loadData();
        } catch (error) {
          this.$message.error(error.message || "操作失败");
        } finally {
          this.submitLoading = false;
        }
      });
    },
    handleDialogClose() {
      this.$refs.bannerForm && this.$refs.bannerForm.resetFields();
    },
    handleUploadSuccess(res, file, fileList) {
      const list = (res && res.data) ? res.data : (Array.isArray(res) ? res : []);
      if (list.length > 0 && list[0].filePath) {
        this.formData.bannerImg = list[0].filePath;
      }
      this.$refs.bannerForm && this.$refs.bannerForm.validateField("bannerImg");
    },
    beforeUpload(file) {
      const isImage = file.type.startsWith("image/");
      const isLt10M = file.size / 1024 / 1024 < 10;
      if (!isImage) {
        this.$message.error("只能上传图片");
        return false;
      }
      if (!isLt10M) {
        this.$message.error("图片大小不能超过 10MB");
        return false;
      }
      return true;
    },
    handleToggleStatus(row) {
      const newStatus = row.bannerStatus === 1 ? 0 : 1;
      const action = newStatus === 1 ? "启用" : "停用";
      this.$confirm(`确定要${action}该轮播图吗？`, "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "info",
      })
        .then(async () => {
          try {
            await updateBanner(row.id, { ...row, bannerStatus: newStatus });
            this.$message.success(`${action}成功`);
            this.loadData();
          } catch (error) {
            this.$message.error(error.message || `${action}失败`);
          }
        })
        .catch(() => {});
    },
    handleDelete(row) {
      this.$confirm("确定要删除该轮播图吗？", "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
      })
        .then(async () => {
          try {
            await deleteBanner(row.id);
            this.$message.success("删除成功");
            this.loadData();
          } catch (error) {
            this.$message.error(error.message || "删除失败");
          }
        })
        .catch(() => {});
    },
    getImageUrl(path) {
      if (!path) return "";
      if (path.startsWith("http") || path.startsWith("/")) return path;
      return "/" + path.replace(/^\//, "");
    },
    formatDateTime(val) {
      if (!val) return "-";
      const d = new Date(val);
      if (isNaN(d.getTime())) return val;
      const y = d.getFullYear();
      const m = String(d.getMonth() + 1).padStart(2, "0");
      const day = String(d.getDate()).padStart(2, "0");
      const h = String(d.getHours()).padStart(2, "0");
      const min = String(d.getMinutes()).padStart(2, "0");
      return `${y}-${m}-${day} ${h}:${min}`;
    },
  },
};
</script>

<style scoped>
.admin-settings-banner-page {
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
  font-weight: 500;
  color: #303133;
}

.table-section {
  background: #fff;
  padding: 20px;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.no-image {
  color: #909399;
  font-size: 12px;
}

.view-only {
  color: #909399;
  font-size: 12px;
}

.banner-uploader {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  overflow: hidden;
}

.banner-uploader:hover {
  border-color: #409eff;
}

.banner-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 178px;
  height: 100px;
  line-height: 100px;
  text-align: center;
  display: block;
  background: #fafafa;
}

.banner-preview {
  width: 178px;
  height: 100px;
  display: block;
  object-fit: cover;
}

.upload-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 8px;
}
</style>
