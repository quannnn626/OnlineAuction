<template>
  <div class="seller-goods-add-page">
    <div class="back-section">
      <el-button icon="el-icon-arrow-left" @click="handleBack">返回到我的商品</el-button>
    </div>
    <div class="page-header">
      <h2>申请上架商品</h2>
      <p class="page-desc">填写商品信息并提交，管理员审核通过后将展示在拍卖商品列表</p>
    </div>
    <div class="form-section">
      <el-form
        ref="form"
        :model="formData"
        :rules="formRules"
        label-width="120px"
      >
        <el-form-item label="商品名称" prop="goodsName">
          <el-input
            v-model="formData.goodsName"
            placeholder="请输入商品名称"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="商品分类" prop="categoryIds">
          <el-popover
            placement="bottom-start"
            width="400"
            trigger="click"
            v-model="categoryTreeVisible"
          >
            <el-tree
              :data="categoryTreeData"
              :props="categoryTreeProps"
              show-checkbox
              node-key="id"
              @check="handleCategoryTreeCheck"
              :check-strictly="false"
              ref="categoryTree"
              style="max-height: 300px; overflow-y: auto"
            >
              <span class="custom-tree-node" slot-scope="{ node, data }">
                <el-tag
                  :type="getLevelTagType(data.level)"
                  size="mini"
                  style="margin-right: 8px"
                >
                  {{ getLevelText(data.level) }}
                </el-tag>
                <span>{{ data.categoryName }}</span>
              </span>
            </el-tree>
            <el-input
              slot="reference"
              v-model="selectedCategoryNames"
              placeholder="请选择商品分类（点击展开树形选择）"
              readonly
              style="cursor: pointer"
            >
              <i slot="prefix" class="el-input__icon el-icon-arrow-down" :class="{ 'is-reverse': categoryTreeVisible }"></i>
            </el-input>
          </el-popover>
        </el-form-item>
        <el-form-item label="商品介绍" prop="goodsDesc">
          <el-input
            type="textarea"
            v-model="formData.goodsDesc"
            placeholder="请输入商品介绍"
            :rows="4"
            maxlength="1000"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="商品图片/视频" prop="fileIds">
          <el-upload
            ref="upload"
            :auto-upload="true"
            :action="uploadAction"
            :on-success="handleUploadSuccess"
            :on-error="handleUploadError"
            :before-upload="beforeUpload"
            :on-remove="handleUploadRemove"
            :file-list="fileList"
            list-type="picture-card"
            :limit="20"
            multiple
            accept="image/*,video/*"
            name="files"
          >
            <i class="el-icon-plus"></i>
            <div slot="tip" class="el-upload__tip">
              支持上传图片和视频，最多20个文件，单文件最大100MB
            </div>
          </el-upload>
        </el-form-item>
        <el-form-item label="起拍价" prop="basePrice">
          <el-input-number
            v-model="formData.basePrice"
            :min="0"
            :precision="2"
            :step="0.01"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="最小加价" prop="addPrice">
          <el-input-number
            v-model="formData.addPrice"
            :min="0"
            :precision="2"
            :step="0.01"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="保留价" prop="reservePrice">
          <el-input-number
            v-model="formData.reservePrice"
            :min="0"
            :precision="2"
            :step="0.01"
            style="width: 100%"
          />
          <span class="form-tip">（选填，仅您可见，拍卖未达此价可流拍）</span>
        </el-form-item>
        <el-form-item label="拍卖开始时间" prop="startTime">
          <el-date-picker
            v-model="formData.startTime"
            type="datetime"
            placeholder="选择开始时间"
            value-format="yyyy-MM-dd HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="拍卖结束时间" prop="endTime">
          <el-date-picker
            v-model="formData.endTime"
            type="datetime"
            placeholder="选择结束时间"
            value-format="yyyy-MM-dd HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="submitLoading">提交申请</el-button>
          <el-button @click="handleBack">取消</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script>
import { addGoods } from "@/api/goods";
import { getCategoryTreeForHome } from "@/api/category";

export default {
  name: "SellerGoodsAdd",
  data() {
    return {
      categoryTreeData: [],
      categoryTreeVisible: false,
      categoryTreeProps: {
        children: "children",
        label: "categoryName",
      },
      categoryMap: {},
      selectedCategoryNames: "",
      formData: {
        goodsName: "",
        categoryIds: [],
        goodsDesc: "",
        fileIds: [],
        basePrice: 0,
        addPrice: 0,
        reservePrice: 0,
        startTime: null,
        endTime: null,
      },
      fileList: [],
      uploadAction: "/api/OnlineAuction/auctionFile/upload",
      submitLoading: false,
      formRules: {
        goodsName: [
          { required: true, message: "请输入商品名称", trigger: "blur" },
          { min: 1, max: 100, message: "商品名称长度在 1 到 100 个字符", trigger: "blur" },
        ],
        categoryIds: [{ required: true, message: "请选择商品分类", trigger: "change" }],
        goodsDesc: [
          { required: true, message: "请输入商品介绍", trigger: "blur" },
          { min: 1, max: 1000, message: "商品介绍长度在 1 到 1000 个字符", trigger: "blur" },
        ],
        basePrice: [
          { required: true, message: "请输入起拍价", trigger: "blur" },
          { type: "number", min: 0, message: "起拍价必须大于等于0", trigger: "blur" },
        ],
        addPrice: [
          { required: true, message: "请输入最小加价", trigger: "blur" },
          { type: "number", min: 0, message: "最小加价必须大于等于0", trigger: "blur" },
        ],
        startTime: [{ required: true, message: "请选择拍卖开始时间", trigger: "change" }],
        endTime: [{ required: true, message: "请选择拍卖结束时间", trigger: "change" }],
      },
    };
  },
  mounted() {
    this.loadCategories();
  },
  methods: {
    async loadCategories() {
      try {
        const result = await getCategoryTreeForHome(false);
        this.categoryTreeData = result || [];
        this.buildCategoryMap(this.categoryTreeData);
      } catch (e) {
        this.$message.error("加载分类失败");
      }
    },
    buildCategoryMap(treeData) {
      const flatten = (nodes) => {
        if (!nodes || !Array.isArray(nodes)) return;
        nodes.forEach((node) => {
          this.categoryMap[node.id] = node.categoryName;
          if (node.children && node.children.length > 0) flatten(node.children);
        });
      };
      flatten(treeData);
    },
    handleCategoryTreeCheck(data, checkedInfo) {
      this.formData.categoryIds = (checkedInfo.checkedKeys || []).map((id) => Number(id));
      this.updateSelectedCategoryNames();
    },
    updateSelectedCategoryNames() {
      if (!this.formData.categoryIds || this.formData.categoryIds.length === 0) {
        this.selectedCategoryNames = "";
        return;
      }
      this.selectedCategoryNames = this.formData.categoryIds
        .map((id) => this.categoryMap[id])
        .filter((n) => n)
        .join("、");
    },
    getLevelTagType(level) {
      return { 1: "primary", 2: "success", 3: "warning" }[level] || "info";
    },
    getLevelText(level) {
      return { 1: "一级", 2: "二级", 3: "三级" }[level] || "未知";
    },
    beforeUpload(file) {
      const isImage = file.type && file.type.startsWith("image/");
      const isVideo = file.type && file.type.startsWith("video/");
      const ext = (file.name || "").split(".").pop().toLowerCase();
      const ok = isImage || isVideo || ["jpg", "jpeg", "png", "gif", "webp", "mp4", "webm"].includes(ext);
      if (!ok) {
        this.$message.error(`不支持的文件类型：${file.name}`);
        return false;
      }
      if (file.size / 1024 / 1024 >= 100) {
        this.$message.error(`文件过大（>100MB）：${file.name}`);
        return false;
      }
      return true;
    },
    handleUploadError(err, file) {
      this.$message.error(`${file.name || "文件"} 上传失败`);
    },
    handleUploadSuccess(response, file, fileList) {
      if (response.code === 200 && response.data) {
        this.fileList = fileList;
        this.syncFileIdsFromList();
      } else {
        this.$message.error("上传失败");
      }
    },
    handleUploadRemove(file, fileList) {
      this.fileList = fileList;
      this.syncFileIdsFromList();
    },
    syncFileIdsFromList() {
      const ids = [];
      (this.fileList || []).forEach((f) => {
        if (f.response && f.response.data) {
          const arr = Array.isArray(f.response.data) ? f.response.data : [f.response.data];
          arr.forEach((d) => {
            if (d && d.id && !ids.includes(d.id)) ids.push(d.id);
          });
        } else if (f.id && !ids.includes(f.id)) ids.push(f.id);
      });
      this.formData.fileIds = ids;
    },
    async handleSubmit() {
      this.$refs.form.validate(async (valid) => {
        if (!valid) return;
        const uploading = (this.fileList || []).some((f) => f.status === "uploading");
        if (uploading) {
          this.$message.warning("文件上传中，请稍候再提交");
          return;
        }
        this.submitLoading = true;
        try {
          const data = {
            ...this.formData,
            categoryId: (this.formData.categoryIds || []).join(","),
            fileIds: this.formData.fileIds || [],
          };
          await addGoods(data);
          this.$message.success("提交成功，请等待管理员审核");
          this.$router.push("/my-goods");
        } catch (e) {
          this.$message.error(e.message || "提交失败");
        } finally {
          this.submitLoading = false;
        }
      });
    },
    handleBack() {
      this.$router.push("/my-goods");
    },
  },
};
</script>

<style scoped>
.seller-goods-add-page {
  padding: 20px;
  background: #f5f5f5;
  min-height: 100vh;
}
.back-section {
  margin-bottom: 16px;
}
.page-header {
  margin-bottom: 24px;
  background: #fff;
  padding: 24px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.08);
}
.page-header h2 {
  margin: 0 0 8px 0;
  color: #303133;
}
.page-desc {
  margin: 0;
  color: #909399;
  font-size: 14px;
}
.form-section {
  background: #fff;
  padding: 24px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.08);
  max-width: 700px;
}
.form-tip {
  margin-left: 8px;
  font-size: 12px;
  color: #909399;
}
.custom-tree-node {
  display: flex;
  align-items: center;
}
</style>
