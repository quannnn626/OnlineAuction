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
        <el-table-column label="操作" width="330" fixed="right">
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
              v-if="canReapply(scope.row)"
              size="mini"
              type="success"
              icon="el-icon-refresh"
              @click="handleReapply(scope.row)"
            >
              重新申请
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
              v-if="canAuctioneerManage && scope.row.goodsStatus === 1"
              size="mini"
              type="warning"
              icon="el-icon-time"
              @click="handleExtendTime(scope.row)"
            >
              拍卖延时
            </el-button>
            <el-button
              v-if="canAuctioneerManage && scope.row.goodsStatus === 1"
              size="mini"
              type="info"
              icon="el-icon-warning-outline"
              @click="handleMarkNoSale(scope.row)"
            >
              流拍
            </el-button>
            <el-button
              v-if="canAuctioneerManage"
              size="mini"
              icon="el-icon-document"
              @click="openRecordsDialog(scope.row)"
            >
              竞拍记录
            </el-button>
            <el-button
              v-if="canOffline(scope.row)"
              size="mini"
              type="warning"
              icon="el-icon-bottom"
              @click="handleOffline(scope.row)"
            >
              下架
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
              :key="dialogVisible ? 'tree-' + formData.id : 'tree-new'"
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
              <i
                slot="prefix"
                class="el-input__icon el-icon-arrow-down"
                :class="{ 'is-reverse': categoryTreeVisible }"
              ></i>
            </el-input>
          </el-popover>
          <div
            v-if="formData.categoryIds && formData.categoryIds.length > 0"
            style="margin-top: 8px"
          >
            <el-tag
              v-for="id in formData.categoryIds"
              :key="id"
              closable
              @close="handleRemoveCategory(id)"
              style="margin-right: 8px; margin-bottom: 4px"
            >
              {{ getCategoryNameById(id) }}
            </el-tag>
          </div>
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
            :data="{ fileCategory: 'goods' }"
          >
            <i class="el-icon-plus"></i>
            <div slot="tip" class="el-upload__tip">
              支持上传图片和视频，最多20个文件，单文件最大100MB
            </div>
          </el-upload>
          <div v-if="fileList && fileList.length" class="upload-status-list">
            <div v-for="f in fileList" :key="f.uid || f.id || f.name" class="upload-status-item">
              <span class="upload-file-name">{{ f.name }}</span>
              <el-tag size="mini" :type="getUploadStatusType(f.status)">
                {{ getUploadStatusText(f.status) }}
              </el-tag>
            </div>
          </div>
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
        <el-form-item label="保证金（参与竞拍需冻结）" prop="depositRequired">
          <el-input-number
            v-model="formData.depositRequired"
            :min="0"
            :precision="2"
            :step="1"
            style="width: 100%"
            placeholder="加价时冻结该金额"
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

    <!-- 竞拍记录（拍卖师可标记异常出价） -->
    <el-dialog
      title="竞拍记录"
      :visible.sync="recordsDialogVisible"
      width="700px"
    >
      <p v-if="recordsGoodsName" class="records-goods-name">{{ recordsGoodsName }}</p>
      <el-table v-loading="recordsLoading" :data="recordsList" stripe size="small">
        <el-table-column prop="bidPrice" label="出价" width="100">
          <template slot-scope="scope">¥{{ scope.row.bidPrice }}</template>
        </el-table-column>
        <el-table-column prop="buyerName" label="买家" width="100"></el-table-column>
        <el-table-column prop="bidTime" label="出价时间" width="160">
          <template slot-scope="scope">{{ scope.row.bidTime ? new Date(scope.row.bidTime).toLocaleString() : "-" }}</template>
        </el-table-column>
        <el-table-column label="异常标记" width="180">
          <template slot-scope="scope">
            <el-select
              :value="scope.row.abnormalType != null ? scope.row.abnormalType : 0"
              size="mini"
              @change="(v) => changeRecordAbnormal(scope.row, v)"
              style="width: 120px"
            >
              <el-option label="正常" :value="0"></el-option>
              <el-option label="恶意出价" :value="1"></el-option>
              <el-option label="机器人" :value="2"></el-option>
            </el-select>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="recordsPagination.total > 0" class="records-pagination">
        <el-pagination
          small
          :current-page="recordsPagination.current"
          :page-size="recordsPagination.size"
          :total="recordsPagination.total"
          layout="total, prev, pager, next"
          @current-change="(p) => { recordsPagination.current = p; loadRecords(); }"
        />
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
  reapplyGoods,
  updateShelfStatus,
  extendAuctionTime,
  markNoSale,
} from "@/api/goods";
import { getCategoryTree } from "@/api/category";
import {
  getAdminBidRecordsByGoodsPage,
  markRecordAbnormal,
} from "@/api/record";

export default {
  name: "AdminGoods",
  data() {
    return {
      loading: false,
      submitLoading: false,
      auditLoading: false,
      tableData: [],
      categoryTreeData: [],
      categoryTreeVisible: false,
      categoryTreeProps: {
        children: "children",
        label: "categoryName",
      },
      categoryMap: {}, // 用于快速查找分类名称
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
        fileIds: [],
        basePrice: 0,
        addPrice: 0,
        depositRequired: 0,
        reservePrice: 0,
        startTime: null,
        endTime: null,
      },
      selectedCategoryNames: "", // 已选择分类的名称显示
      fileList: [],
      uploadAction: "/api/OnlineAuction/auctionFile/upload", // 文件上传接口
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
        depositRequired: [
          { type: "number", min: 0, message: "保证金不能为负", trigger: "blur" },
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
      recordsDialogVisible: false,
      recordsLoading: false,
      recordsList: [],
      recordsGoodsName: "",
      recordsGoodsId: null,
      recordsPagination: { current: 1, size: 10, total: 0 },
    };
  },
  computed: {
    canAuctioneerManage() {
      try {
        const user = JSON.parse(localStorage.getItem("userInfo") || "{}");
        const roles = user.userRole ? String(user.userRole).split(",").map((r) => r.trim()) : [];
        return roles.some((r) => ["3", "4", "5"].includes(r));
      } catch (e) {
        return false;
      }
    },
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
        this.$message.error("加载失败");
      } finally {
        this.loading = false;
      }
    },
    // 加载分类树
    async loadCategories() {
      try {
        const result = await getCategoryTree(true); // 获取所有分类，包括禁用的
        this.categoryTreeData = result || [];
        // 构建分类映射表，方便快速查找分类名称
        this.buildCategoryMap(this.categoryTreeData);
      } catch (error) {
        this.$message.error("加载分类列表失败");
      }
    },
    // 构建分类映射表（扁平化树结构）
    buildCategoryMap(treeData) {
      const map = {};
      const flatten = (nodes) => {
        if (!nodes || !Array.isArray(nodes)) return;
        nodes.forEach((node) => {
          map[node.id] = node.categoryName;
          if (node.children && node.children.length > 0) {
            flatten(node.children);
          }
        });
      };
      flatten(treeData);
      this.categoryMap = map;
    },
    // 处理树节点选择
    handleCategoryTreeCheck(data, checkedInfo) {
      // 获取所有选中的节点（包括半选中的父节点，但我们只要完全选中的）
      const checkedKeys = checkedInfo.checkedKeys || [];
      // 确保所有ID都是数字类型
      this.formData.categoryIds = checkedKeys.map((id) => Number(id));
      // 更新显示的分类名称
      this.updateSelectedCategoryNames();
    },
    // 更新已选择的分类名称显示
    updateSelectedCategoryNames() {
      if (!this.formData.categoryIds || this.formData.categoryIds.length === 0) {
        this.selectedCategoryNames = "";
        return;
      }
      const names = this.formData.categoryIds
        .map((id) => this.getCategoryNameById(id))
        .filter((name) => name)
        .join("、");
      this.selectedCategoryNames = names || "";
    },
    // 根据ID获取分类名称
    getCategoryNameById(id) {
      return this.categoryMap[id] || `分类ID: ${id}`;
    },
    // 移除分类
    handleRemoveCategory(id) {
      const index = this.formData.categoryIds.indexOf(id);
      if (index > -1) {
        this.formData.categoryIds.splice(index, 1);
        // 同步更新树的选中状态
        this.$nextTick(() => {
          if (this.$refs.categoryTree) {
            this.$refs.categoryTree.setCheckedKeys(this.formData.categoryIds);
          }
          this.updateSelectedCategoryNames();
        });
      }
    },
    // 获取层级标签类型
    getLevelTagType(level) {
      const typeMap = {
        1: "primary",
        2: "success",
        3: "warning",
      };
      return typeMap[level] || "info";
    },
    // 获取层级文本
    getLevelText(level) {
      const textMap = {
        1: "一级",
        2: "二级",
        3: "三级",
      };
      return textMap[level] || "未知";
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
        depositRequired: 0,
        reservePrice: 0,
        startTime: null,
        endTime: null,
      };
      this.selectedCategoryNames = "";
      this.fileList = [];
      // 重置树的选中状态
      this.$nextTick(() => {
        if (this.$refs.categoryTree) {
          this.$refs.categoryTree.setCheckedKeys([]);
        }
      });
      this.dialogVisible = true;
    },
    // 编辑
    handleEdit(goods) {
      this.dialogTitle = "编辑商品";
      const categoryIds = goods.categoryId
        ? goods.categoryId
            .split(",")
            .map((id) => parseInt(id))
            .filter((id) => !isNaN(id))
        : [];
      this.formData = {
        ...goods,
        categoryIds: categoryIds,
        fileIds: goods.files ? goods.files.map((f) => f.id) : [],
        depositRequired: goods.depositRequired != null ? Number(goods.depositRequired) : 0,
      };
      this.fileList = this.getFileListFromImages(goods.files);
      // 更新选中分类名称显示
      this.updateSelectedCategoryNames();
      // 同步更新树的选中状态
      this.$nextTick(() => {
        if (this.$refs.categoryTree) {
          this.$refs.categoryTree.setCheckedKeys(categoryIds);
        }
      });
      this.dialogVisible = true;
    },
    // 查看
    handleView(goods) {
      // 跳转到商品详情页面
      this.$router.push({ 
        path: "/goods-detail", 
        query: { id: goods.id } 
      });
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
    // 重新申请（流拍/驳回/已下架）
    async handleReapply(goods) {
      try {
        await this.$confirm(
          `确定将商品 "${goods.goodsName}" 重新申请上架吗？`,
          "提示",
          {
            confirmButtonText: "确定",
            cancelButtonText: "取消",
            type: "warning",
          }
        );
        await reapplyGoods(goods.id);
        this.$message.success("重新申请成功，等待审核");
        this.loadData();
      } catch (error) {
        if (error !== "cancel") {
          this.$message.error("重新申请失败");
        }
      }
    },
    // 拍卖师：拍卖延时
    async handleExtendTime(goods) {
      try {
        const minutes = await this.$prompt("延长分钟数（1~1440）", "拍卖延时", {
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          inputValue: "5",
          inputValidator: (v) => {
            const n = parseInt(v, 10);
            if (isNaN(n) || n < 1 || n > 1440) return "请输入 1~1440 的整数";
            return true;
          },
        }).then(({ value }) => parseInt(value, 10)).catch(() => null);
        if (minutes == null) return;
        await extendAuctionTime(goods.id, minutes);
        this.$message.success("已延长 " + minutes + " 分钟");
        this.loadData();
      } catch (e) {
        this.$message.error(e.message || "操作失败");
      }
    },
    // 拍卖师：流拍
    async handleMarkNoSale(goods) {
      try {
        await this.$confirm(`确定将 "${goods.goodsName}" 标记为流拍吗？`, "流拍", {
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          type: "warning",
        });
        await markNoSale(goods.id);
        this.$message.success("已标记流拍");
        this.loadData();
      } catch (e) {
        if (e !== "cancel") this.$message.error(e.message || "操作失败");
      }
    },
    // 竞拍记录弹窗
    async openRecordsDialog(goods) {
      this.recordsGoodsId = goods.id;
      this.recordsGoodsName = goods.goodsName;
      this.recordsDialogVisible = true;
      this.recordsPagination.current = 1;
      await this.loadRecords();
    },
    async loadRecords() {
      if (!this.recordsGoodsId) return;
      this.recordsLoading = true;
      try {
        const res = await getAdminBidRecordsByGoodsPage(this.recordsGoodsId, {
          current: this.recordsPagination.current,
          size: this.recordsPagination.size,
        });
        this.recordsList = res.list || res.records || [];
        this.recordsPagination.total = res.total || 0;
      } catch (e) {
        this.recordsList = [];
      } finally {
        this.recordsLoading = false;
      }
    },
    getAbnormalText(type) {
      const m = { 0: "正常", 1: "恶意出价", 2: "机器人" };
      return m[type] || "正常";
    },
    async changeRecordAbnormal(record, type) {
      try {
        await markRecordAbnormal(record.id, type);
        this.$message.success("已更新");
        record.abnormalType = type;
      } catch (e) {
        this.$message.error(e.message || "更新失败");
      }
    },
    // 下架（仅已上架商品）
    async handleOffline(goods) {
      try {
        await this.$confirm(`确定下架商品 "${goods.goodsName}" 吗？`, "提示", {
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          type: "warning",
        });
        await updateShelfStatus(goods.id, 0);
        this.$message.success("下架成功");
        this.loadData();
      } catch (error) {
        if (error !== "cancel") {
          this.$message.error("下架失败");
        }
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
        const uploading = (this.fileList || []).some((f) => f.status === "uploading");
        if (uploading) {
          this.$message.warning("文件还在上传中，请稍后再提交");
          return;
        }
        const failed = (this.fileList || []).some((f) => f.status === "fail");
        if (failed) {
          this.$message.error("存在上传失败文件，请重新上传后再提交");
          return;
        }
        this.submitLoading = true;
        try {
          const data = {
            ...this.formData,
            categoryId: this.formData.categoryIds.join(","), // 多个分类用逗号分隔
            fileIds: this.formData.fileIds || [], // 确保传递文件ID列表
          };
          if (this.formData.id) {
            await updateGoods(data);
            this.$message.success("修改成功");
          } else {
            await addGoods(data);
            this.$message.success("添加成功");
          }
          this.dialogVisible = false;
          this.loadData();
        } catch (error) {
          this.$message.error("提交失败");
        } finally {
          this.submitLoading = false;
        }
      });
    },
    beforeUpload(file) {
      const isImage = file.type && file.type.startsWith("image/");
      const isVideo = file.type && file.type.startsWith("video/");
      const fileName = (file.name || "").toLowerCase();
      const ext = fileName.includes(".") ? fileName.substring(fileName.lastIndexOf(".") + 1) : "";
      const imageExt = ["jpg", "jpeg", "png", "gif", "webp", "bmp"];
      const videoExt = ["mp4", "webm", "ogg", "mov", "m4v", "avi"];
      const typeOk = isImage || isVideo || imageExt.includes(ext) || videoExt.includes(ext);
      if (!typeOk) {
        this.$message.error(`不支持的文件类型：${file.name}`);
        return false;
      }
      const isLt100MB = file.size / 1024 / 1024 < 100;
      if (!isLt100MB) {
        this.$message.error(`文件过大（>${100}MB）：${file.name}`);
        return false;
      }
      return true;
    },
    handleUploadError(error, file) {
      const name = file && file.name ? file.name : "文件";
      this.$message.error(`${name} 上传失败，请检查格式或大小限制`);
    },
    // 上传成功
    handleUploadSuccess(response, file, fileList) {
      if (response.code === 200 && response.data) {
        this.fileList = fileList;
        // 提取文件路径和文件ID
        const filePaths = [];
        const fileIds = [];
        
        // response.data 是文件数组（后端返回的是 List<AuctionFile>）
        if (Array.isArray(response.data)) {
          response.data.forEach((fileData) => {
            if (fileData.filePath) {
              filePaths.push(fileData.filePath);
            }
            if (fileData.id) {
              fileIds.push(fileData.id);
            }
          });
        } else {
          // 兼容处理：如果返回的是单个文件对象
          if (response.data.filePath) {
            filePaths.push(response.data.filePath);
          }
          if (response.data.id) {
            fileIds.push(response.data.id);
          }
        }
        
        // 更新所有文件的路径和ID（合并所有已上传的文件和已有文件）
        fileList.forEach((f) => {
          // 处理新上传的文件（有response字段）
          if (f.response && f.response.data) {
            if (Array.isArray(f.response.data)) {
              f.response.data.forEach((fileData) => {
                if (fileData.filePath && !filePaths.includes(fileData.filePath)) {
                  filePaths.push(fileData.filePath);
                }
                if (fileData.id && !fileIds.includes(fileData.id)) {
                  fileIds.push(fileData.id);
                }
              });
            } else {
              if (f.response.data.filePath && !filePaths.includes(f.response.data.filePath)) {
                filePaths.push(f.response.data.filePath);
              }
              if (f.response.data.id && !fileIds.includes(f.response.data.id)) {
                fileIds.push(f.response.data.id);
              }
            }
          } else if (f.id) {
            // 处理已有文件（从服务器加载的文件，有id字段）
            if (f.url && !filePaths.includes(f.url)) {
              filePaths.push(f.url);
            }
            if (!fileIds.includes(f.id)) {
              fileIds.push(f.id);
            }
          } else if (f.url && !f.response) {
            // 处理已有文件（只有url，没有response）
            if (!filePaths.includes(f.url)) {
              filePaths.push(f.url);
            }
          }
        });
        
        this.formData.goodsImg = filePaths.join(",");
        this.formData.fileIds = fileIds;
      } else {
        this.$message.error(`上传失败: ${response.msg || "未知错误"}`);
      }
    },
    // 上传移除
    handleUploadRemove(file, fileList) {
      this.fileList = fileList;
      // 更新文件路径和文件ID
      const filePaths = [];
      const fileIds = [];
      
      fileList.forEach((f) => {
        // 处理新上传的文件（有response字段）
        if (f.response && f.response.data) {
          // 如果返回的是数组（多个文件）
          if (Array.isArray(f.response.data)) {
            f.response.data.forEach((fileData) => {
              if (fileData.filePath) {
                filePaths.push(fileData.filePath);
              }
              if (fileData.id) {
                fileIds.push(fileData.id);
              }
            });
          } else {
            // 如果返回的是单个文件对象
            if (f.response.data.filePath) {
              filePaths.push(f.response.data.filePath);
            }
            if (f.response.data.id) {
              fileIds.push(f.response.data.id);
            }
          }
        } else if (f.id) {
          // 处理已有文件（从服务器加载的文件，有id字段）
          if (f.url) {
            filePaths.push(f.url);
          }
          fileIds.push(f.id);
        } else if (f.url && !f.response) {
          // 处理已有文件（只有url，没有response）
          filePaths.push(f.url);
        }
      });
      
      this.formData.goodsImg = filePaths.join(",");
      this.formData.fileIds = fileIds;
    },
    // 获取文件列表
    getFileListFromImages(files) {
      if (!files || !Array.isArray(files)) return [];
      return files.map((file) => ({
        id: file.id, // 保留文件ID，用于编辑时识别已有文件
        name: file.fileName,
        url: file.filePath,
        response: { data: file },
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
    getUploadStatusText(status) {
      const map = {
        ready: "待上传",
        uploading: "上传中",
        success: "成功",
        fail: "失败",
      };
      return map[status] || "未知";
    },
    getUploadStatusType(status) {
      const map = {
        ready: "info",
        uploading: "warning",
        success: "success",
        fail: "danger",
      };
      return map[status] || "info";
    },
    canReapply(row) {
      if (!row) return false;
      return row.auditStatus === 2 || row.auditStatus === 3 || row.goodsStatus === 3;
    },
    canOffline(row) {
      if (!row) return false;
      return row.shelfStatus === 1;
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

/* 分类树选择器样式 */
.custom-tree-node {
  flex: 1;
  display: flex;
  align-items: center;
  font-size: 14px;
  padding-right: 8px;
}

.custom-tree-node span {
  flex: 1;
}

.el-input__icon.is-reverse {
  transform: rotate(180deg);
}

/* 树形选择器滚动条样式 */
.el-tree {
  background: transparent;
}

.el-tree-node__content {
  height: 32px;
  line-height: 32px;
}

.el-tree-node__content:hover {
  background-color: #f5f7fa;
}

.upload-status-list {
  margin-top: 8px;
  max-height: 140px;
  overflow-y: auto;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 8px;
  background: #fafafa;
}

.upload-status-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  margin-bottom: 6px;
}

.upload-status-item:last-child {
  margin-bottom: 0;
}

.upload-file-name {
  max-width: 460px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: #606266;
  font-size: 12px;
}
</style>
