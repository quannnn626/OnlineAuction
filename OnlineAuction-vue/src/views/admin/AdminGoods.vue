<template>
  <div class="admin-goods-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>拍卖商品管理</h2>
      <div class="header-actions">
        <el-button type="primary" icon="el-icon-plus" @click="handleAdd">
          添加商品
        </el-button>
      </div>
    </div>

    <!-- 搜索和筛选区域 -->
    <div class="filter-section">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-input
            v-model="searchForm.goodsName"
            placeholder="搜索商品名称"
            clearable
            @keyup.enter.native="handleSearch"
          >
            <el-button
              slot="append"
              icon="el-icon-search"
              @click="handleSearch"
            ></el-button>
          </el-input>
        </el-col>
        <el-col :span="4">
          <el-select
            v-model="searchForm.auditStatus"
            placeholder="审核状态"
            clearable
            @change="handleSearch"
          >
            <el-option label="全部" value=""></el-option>
            <el-option label="待审核" value="0"></el-option>
            <el-option label="审核通过" value="1"></el-option>
            <el-option label="审核驳回" value="2"></el-option>
            <el-option label="已下架" value="3"></el-option>
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-select
            v-model="searchForm.goodsStatus"
            placeholder="商品状态"
            clearable
            @change="handleSearch"
          >
            <el-option label="全部" value=""></el-option>
            <el-option label="未开始" value="0"></el-option>
            <el-option label="竞拍中" value="1"></el-option>
            <el-option label="已成交" value="2"></el-option>
            <el-option label="已流拍" value="3"></el-option>
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-button type="info" icon="el-icon-refresh" @click="handleReset"
            >重置</el-button
          >
        </el-col>
      </el-row>
    </div>

    <!-- 商品表格 -->
    <div class="table-section">
      <el-table
        v-loading="loading"
        :data="tableData"
        stripe
        style="width: 100%"
      >
        <el-table-column type="selection" width="55"></el-table-column>
        <el-table-column label="商品图片" width="120">
          <template slot-scope="scope">
            <el-image
              :src="getGoodsImage(scope.row.goodsImg)"
              :preview-src-list="getGoodsImages(scope.row.goodsImg)"
              style="width: 80px; height: 80px; object-fit: cover"
              fit="cover"
            >
              <div slot="error" class="image-slot">
                <i class="el-icon-picture-outline"></i>
              </div>
            </el-image>
          </template>
        </el-table-column>
        <el-table-column prop="goodsName" label="商品名称" min-width="150">
          <template slot-scope="scope">
            <div class="goods-name">{{ scope.row.goodsName }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="goodsDesc" label="商品介绍" min-width="200">
          <template slot-scope="scope">
            <div class="goods-desc">
              {{ scope.row.goodsDesc || "暂无介绍" }}
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="basePrice" label="起拍价" width="100">
          <template slot-scope="scope">
            <span class="price">¥{{ scope.row.basePrice }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="auditStatus" label="审核状态" width="100">
          <template slot-scope="scope">
            <el-tag :type="getAuditStatusType(scope.row.auditStatus)">
              {{ getAuditStatusText(scope.row.auditStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="goodsStatus" label="商品状态" width="100">
          <template slot-scope="scope">
            <el-tag :type="getGoodsStatusType(scope.row.goodsStatus)">
              {{ getGoodsStatusText(scope.row.goodsStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="startTime" label="拍卖时间" min-width="180">
          <template slot-scope="scope">
            <div>{{ formatDateTime(scope.row.startTime) }}</div>
            <div>至 {{ formatDateTime(scope.row.endTime) }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160">
          <template slot-scope="scope">
            {{ formatDateTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template slot-scope="scope">
            <el-button
              size="mini"
              type="primary"
              icon="el-icon-view"
              @click="handleView(scope.row)"
            >
              查看
            </el-button>
            <el-button
              v-if="scope.row.auditStatus === 0"
              size="mini"
              type="success"
              icon="el-icon-check"
              @click="handleAudit(scope.row, 1)"
            >
              通过
            </el-button>
            <el-button
              v-if="scope.row.auditStatus === 0"
              size="mini"
              type="warning"
              icon="el-icon-close"
              @click="handleAudit(scope.row, 2)"
            >
              驳回
            </el-button>
            <el-button
              size="mini"
              type="primary"
              icon="el-icon-edit"
              @click="handleEdit(scope.row)"
            >
              编辑
            </el-button>
            <el-button
              size="mini"
              type="danger"
              icon="el-icon-delete"
              @click="handleDelete(scope.row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 分页 -->
    <div class="pagination-section">
      <el-pagination
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="pagination.current"
        :page-sizes="[10, 20, 50, 100]"
        :page-size="pagination.size"
        layout="total, sizes, prev, pager, next, jumper"
        :total="pagination.total"
      >
      </el-pagination>
    </div>

    <!-- 添加/编辑对话框 -->
    <el-dialog
      :title="dialogTitle"
      :visible.sync="dialogVisible"
      width="800px"
      :close-on-click-modal="false"
    >
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
          >
          </el-input>
        </el-form-item>
        <el-form-item label="商品分类" prop="categoryIds">
          <el-select
            v-model="formData.categoryIds"
            multiple
            placeholder="请选择商品分类"
            style="width: 100%"
          >
            <el-option
              v-for="category in categoryList"
              :key="category.id"
              :label="category.categoryName"
              :value="category.id"
            >
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="商品介绍" prop="goodsDesc">
          <el-input
            type="textarea"
            v-model="formData.goodsDesc"
            placeholder="请输入商品介绍"
            :rows="4"
            maxlength="1000"
            show-word-limit
          >
          </el-input>
        </el-form-item>
        <el-form-item label="商品图片" prop="goodsImg">
          <el-upload
            ref="upload"
            :action="uploadAction"
            :on-success="handleUploadSuccess"
            :on-remove="handleUploadRemove"
            :file-list="fileList"
            list-type="picture-card"
            :limit="10"
            multiple
          >
            <i class="el-icon-plus"></i>
            <div slot="tip" class="el-upload__tip">
              最多上传10张图片，单张最大5MB
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
          >
          </el-input-number>
        </el-form-item>
        <el-form-item label="最小加价" prop="addPrice">
          <el-input-number
            v-model="formData.addPrice"
            :min="0"
            :precision="2"
            :step="0.01"
            style="width: 100%"
          >
          </el-input-number>
        </el-form-item>
        <el-form-item label="保留价" prop="reservePrice">
          <el-input-number
            v-model="formData.reservePrice"
            :min="0"
            :precision="2"
            :step="0.01"
            style="width: 100%"
          >
          </el-input-number>
        </el-form-item>
        <el-form-item label="拍卖开始时间" prop="startTime">
          <el-date-picker
            v-model="formData.startTime"
            type="datetime"
            placeholder="选择开始时间"
            style="width: 100%"
          >
          </el-date-picker>
        </el-form-item>
        <el-form-item label="拍卖结束时间" prop="endTime">
          <el-date-picker
            v-model="formData.endTime"
            type="datetime"
            placeholder="选择结束时间"
            style="width: 100%"
          >
          </el-date-picker>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading"
          >确定</el-button
        >
      </div>
    </el-dialog>

    <!-- 审核驳回对话框 -->
    <el-dialog
      title="审核驳回"
      :visible.sync="auditDialogVisible"
      width="400px"
    >
      <el-form ref="auditForm" :model="auditData" label-width="80px">
        <el-form-item label="驳回原因" prop="remark">
          <el-input
            type="textarea"
            v-model="auditData.remark"
            placeholder="请输入驳回原因"
            :rows="3"
            maxlength="200"
            show-word-limit
          >
          </el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="auditDialogVisible = false">取消</el-button>
        <el-button
          type="primary"
          @click="handleAuditSubmit"
          :loading="auditLoading"
          >确定</el-button
        >
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  getGoodsPage,
  addGoods,
  updateGoods,
  deleteGoods,
  auditGoods,
} from "@/api/goods";
import { getCategoryList } from "@/api/category";

export default {
  name: "AdminGoods",
  data() {
    return {
      loading: false,
      submitLoading: false,
      auditLoading: false,
      tableData: [],
      categoryList: [],
      searchForm: {
        goodsName: "",
        auditStatus: "",
        goodsStatus: "",
      },
      pagination: {
        current: 1,
        size: 10,
        total: 0,
      },
      dialogVisible: false,
      dialogTitle: "添加商品",
      formData: {
        id: null,
        goodsName: "",
        categoryIds: [],
        goodsDesc: "",
        goodsImg: "",
        basePrice: 0,
        addPrice: 0,
        reservePrice: 0,
        startTime: null,
        endTime: null,
      },
      fileList: [],
      uploadAction: "/api/upload/image", // 上传接口
      formRules: {
        goodsName: [
          { required: true, message: "请输入商品名称", trigger: "blur" },
          {
            min: 1,
            max: 100,
            message: "商品名称长度在 1 到 100 个字符",
            trigger: "blur",
          },
        ],
        categoryIds: [
          { required: true, message: "请选择商品分类", trigger: "change" },
        ],
        goodsDesc: [
          { required: true, message: "请输入商品介绍", trigger: "blur" },
          {
            min: 1,
            max: 1000,
            message: "商品介绍长度在 1 到 1000 个字符",
            trigger: "blur",
          },
        ],
        basePrice: [
          { required: true, message: "请输入起拍价", trigger: "blur" },
          {
            type: "number",
            min: 0,
            message: "起拍价必须大于等于0",
            trigger: "blur",
          },
        ],
        addPrice: [
          { required: true, message: "请输入最小加价", trigger: "blur" },
          {
            type: "number",
            min: 0,
            message: "最小加价必须大于等于0",
            trigger: "blur",
          },
        ],
        reservePrice: [
          {
            type: "number",
            min: 0,
            message: "保留价必须大于等于0",
            trigger: "blur",
          },
        ],
        startTime: [
          { required: true, message: "请选择拍卖开始时间", trigger: "change" },
        ],
        endTime: [
          { required: true, message: "请选择拍卖结束时间", trigger: "change" },
        ],
      },
      auditDialogVisible: false,
      auditData: {
        goodsId: null,
        status: 2,
        remark: "",
      },
    };
  },
  mounted() {
    this.loadData();
    this.loadCategories();
  },
  methods: {
    // 加载数据
    async loadData() {
      this.loading = true;
      try {
        const params = {
          current: this.pagination.current,
          size: this.pagination.size,
          goodsName: this.searchForm.goodsName || undefined,
          auditStatus: this.searchForm.auditStatus || undefined,
          goodsStatus: this.searchForm.goodsStatus || undefined,
        };
        const result = await getGoodsPage(params);
        this.tableData = result.list || [];
        this.pagination.total = result.total || 0;
      } catch (error) {
        console.error("加载商品列表失败:", error);
        this.$message.error("加载失败");
      } finally {
        this.loading = false;
      }
    },
    // 加载分类列表
    async loadCategories() {
      try {
        const result = await getCategoryList();
        this.categoryList = result || [];
      } catch (error) {
        console.error("加载分类列表失败:", error);
      }
    },
    // 搜索
    handleSearch() {
      this.pagination.current = 1;
      this.loadData();
    },
    // 重置
    handleReset() {
      this.searchForm.goodsName = "";
      this.searchForm.auditStatus = "";
      this.searchForm.goodsStatus = "";
      this.pagination.current = 1;
      this.loadData();
    },
    // 添加
    handleAdd() {
      this.dialogTitle = "添加商品";
      this.formData = {
        id: null,
        goodsName: "",
        categoryIds: [],
        goodsDesc: "",
        goodsImg: "",
        basePrice: 0,
        addPrice: 0,
        reservePrice: 0,
        startTime: null,
        endTime: null,
      };
      this.fileList = [];
      this.dialogVisible = true;
    },
    // 编辑
    handleEdit(goods) {
      this.dialogTitle = "编辑商品";
      this.formData = {
        ...goods,
        categoryIds: goods.categoryId ? goods.categoryId.split(",") : [],
      };
      this.fileList = this.getFileListFromImages(goods.goodsImg);
      this.dialogVisible = true;
    },
    // 查看
    handleView(goods) {
      this.$message.info(`查看商品: ${goods.goodsName}`);
      // TODO: 跳转到商品详情页面
    },
    // 审核通过
    async handleAudit(goods, status) {
      if (status === 2) {
        // 驳回，需要输入原因
        this.auditData.goodsId = goods.id;
        this.auditData.status = status;
        this.auditData.remark = "";
        this.auditDialogVisible = true;
      } else {
        // 通过
        await this.performAudit(goods.id, status, "");
      }
    },
    // 执行审核
    async performAudit(goodsId, status, remark) {
      try {
        await auditGoods({ goodsId, status, remark });
        this.$message.success(status === 1 ? "审核通过" : "审核驳回");
        this.loadData();
      } catch (error) {
        console.error("审核失败:", error);
        this.$message.error("审核失败");
      }
    },
    // 审核提交
    async handleAuditSubmit() {
      this.auditLoading = true;
      try {
        await this.performAudit(
          this.auditData.goodsId,
          this.auditData.status,
          this.auditData.remark
        );
        this.auditDialogVisible = false;
      } catch (error) {
        // 错误已在 performAudit 中处理
      } finally {
        this.auditLoading = false;
      }
    },
    // 删除
    async handleDelete(goods) {
      try {
        await this.$confirm(`确定删除商品 "${goods.goodsName}" 吗？`, "提示", {
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          type: "warning",
        });
        await deleteGoods(goods.id);
        this.$message.success("删除成功");
        this.loadData();
      } catch (error) {
        if (error !== "cancel") {
          console.error("删除商品失败:", error);
          this.$message.error("删除失败");
        }
      }
    },
    // 分页大小变化
    handleSizeChange(size) {
      this.pagination.size = size;
      this.pagination.current = 1;
      this.loadData();
    },
    // 当前页变化
    handleCurrentChange(current) {
      this.pagination.current = current;
      this.loadData();
    },
    // 提交表单
    async handleSubmit() {
      this.$refs.form.validate(async (valid) => {
        if (!valid) return;
        this.submitLoading = true;
        try {
          const data = {
            ...this.formData,
            categoryId: this.formData.categoryIds.join(","), // 多个分类用逗号分隔
          };
          if (this.formData.id) {
            await updateGoods(data);
            this.$message.success("编辑成功");
          } else {
            await addGoods(data);
            this.$message.success("添加成功");
          }
          this.dialogVisible = false;
          this.loadData();
        } catch (error) {
          console.error("提交失败:", error);
          this.$message.error("提交失败");
        } finally {
          this.submitLoading = false;
        }
      });
    },
    // 上传成功
    handleUploadSuccess(response, file, fileList) {
      if (response.code === 200) {
        this.fileList = fileList;
        this.formData.goodsImg = fileList.map((f) => f.response.data).join(",");
      } else {
        this.$message.error("上传失败");
      }
    },
    // 上传移除
    handleUploadRemove(file, fileList) {
      this.fileList = fileList;
      this.formData.goodsImg = fileList
        .map((f) => (f.response ? f.response.data : f.url))
        .join(",");
    },
    // 获取文件列表
    getFileListFromImages(images) {
      if (!images) return [];
      return images.split(",").map((url, index) => ({
        name: `image${index + 1}`,
        url: url,
      }));
    },
    // 获取商品图片
    getGoodsImage(goodsImg) {
      if (!goodsImg) return "/images/no-image.svg";
      return goodsImg.split(",")[0];
    },
    // 获取商品图片列表
    getGoodsImages(goodsImg) {
      if (!goodsImg) return [];
      return goodsImg.split(",");
    },
    // 获取审核状态类型
    getAuditStatusType(status) {
      const typeMap = {
        0: "warning",
        1: "success",
        2: "danger",
        3: "info",
      };
      return typeMap[status] || "info";
    },
    // 获取审核状态文本
    getAuditStatusText(status) {
      const textMap = {
        0: "待审核",
        1: "审核通过",
        2: "审核驳回",
        3: "已下架",
      };
      return textMap[status] || "未知";
    },
    // 获取商品状态类型
    getGoodsStatusType(status) {
      const typeMap = {
        0: "info",
        1: "success",
        2: "warning",
        3: "danger",
      };
      return typeMap[status] || "info";
    },
    // 获取商品状态文本
    getGoodsStatusText(status) {
      const textMap = {
        0: "未开始",
        1: "竞拍中",
        2: "已成交",
        3: "已流拍",
      };
      return textMap[status] || "未知";
    },
    // 格式化日期时间
    formatDateTime(timeStr) {
      if (!timeStr) return "";
      const date = new Date(timeStr);
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
.admin-goods-page {
  padding: 20px;
  background: #f5f5f5;
  min-height: 100vh;
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

.filter-section {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.table-section {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.goods-name {
  font-weight: bold;
  color: #333;
  margin-bottom: 4px;
}

.goods-desc {
  color: #666;
  font-size: 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.price {
  color: #e74c3c;
  font-weight: bold;
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

.image-slot {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 80px;
  height: 80px;
  background: #f5f5f5;
  color: #909399;
  font-size: 24px;
}
</style>
