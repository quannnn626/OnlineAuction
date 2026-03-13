<template>
  <div class="my-goods-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>我的商品</h2>
      <div class="header-actions">
        <el-button type="primary" icon="el-icon-plus" @click="handleAdd">
          商品申请
        </el-button>
      </div>
    </div>

    <!-- 商品表格 -->
    <div class="table-section">
      <el-table
        v-loading="loading"
        :data="goodsList"
        stripe
        style="width: 100%">
        <el-table-column type="selection" width="55"></el-table-column>
        <el-table-column label="商品图片" width="120">
          <template slot-scope="scope">
            <el-image
              :src="getGoodsImageUrl(scope.row)"
              :preview-src-list="[getGoodsImageUrl(scope.row)]"
              style="width: 80px; height: 80px; object-fit: cover;"
              fit="cover">
              <div slot="error" class="image-slot">
                <i class="el-icon-picture-outline"></i>
              </div>
            </el-image>
          </template>
        </el-table-column>
        <el-table-column prop="goodsName" label="商品名称" min-width="200">
          <template slot-scope="scope">
            <div class="goods-name">{{ scope.row.goodsName }}</div>
            <div class="goods-desc">{{ scope.row.goodsDesc || '暂无描述' }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="basePrice" label="起拍价" width="120">
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
            <el-tag :type="getStatusType(scope.row.goodsStatus)">
              {{ getStatusText(scope.row.goodsStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="startTime" label="拍卖时间" min-width="180">
          <template slot-scope="scope">
            <div>{{ formatTime(scope.row.startTime) }}</div>
            <div>至 {{ formatTime(scope.row.endTime) }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160">
          <template slot-scope="scope">
            {{ formatTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template slot-scope="scope">
            <el-button
              size="mini"
              type="primary"
              icon="el-icon-edit"
              @click="handleEdit(scope.row)"
              :disabled="scope.row.auditStatus === 1 && (scope.row.goodsStatus === 1 || scope.row.goodsStatus === 0)">
              编辑
            </el-button>
            <el-button
              v-if="scope.row.auditStatus === 2 || scope.row.auditStatus === 3 || scope.row.goodsStatus === 3"
              size="mini"
              type="success"
              icon="el-icon-upload2"
              @click="handleReapply(scope.row)">
              重新申请上架
            </el-button>
            <el-button
              v-if="scope.row.auditStatus === 0"
              size="mini"
              type="info"
              disabled>
              审核中
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
        :total="pagination.total">
      </el-pagination>
    </div>
  </div>
</template>

<script>
import { getMyGoodsList, deleteGoods, reapplyGoods } from '@/api/goods'

export default {
  name: 'MyGoods',
  data() {
    return {
      goodsList: [],
      loading: false,
      pagination: {
        current: 1,
        size: 10,
        total: 0
      }
    }
  },
  created() {
    this.loadData()
  },
  methods: {
    async loadData() {
      this.loading = true
      try {
        const params = {
          current: this.pagination.current,
          size: this.pagination.size
        }
        const result = await getMyGoodsList(params)
        // 后端 PageHelper 返回 list，MyBatis-Plus 返回 records
        this.goodsList = result.list || result.records || []
        this.pagination.total = result.total || 0
      } catch (error) {
        this.$message.error('加载失败，请重试')
      } finally {
        this.loading = false
      }
    },
    handleAdd() {
      this.$router.push('/seller/goods/add')
    },
    handleEdit(goods) {
      this.$message.info(`编辑商品: ${goods.goodsName}`)
      // TODO: 跳转到编辑商品页面
    },
    async handleDelete(goods) {
      try {
        await this.$confirm(`确定下架商品 "${goods.goodsName}" 吗？`, '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        await deleteGoods(goods.id)
        this.$message.success('下架成功')
        this.loadData()
      } catch (error) {
        if (error !== 'cancel') {
          this.$message.error('下架失败，请重试')
        }
      }
    },
    handleSizeChange(size) {
      this.pagination.size = size
      this.pagination.current = 1
      this.loadData()
    },
    handleCurrentChange(current) {
      this.pagination.current = current
      this.loadData()
    },
    getGoodsImageUrl(goods) {
      const files = goods.files || []
      const firstImg = files.find((f) => f && f.filePath && /\.(jpg|jpeg|png|gif|webp|bmp|svg)/i.test(f.filePath))
      if (firstImg && firstImg.filePath) {
        const p = firstImg.filePath
        return /^https?:\/\//i.test(p) ? p : (p.startsWith('/') ? p : '/' + p)
      }
      if (goods.goodsImg) {
        const imgs = String(goods.goodsImg).split(',')
        const url = imgs[0] && imgs[0].trim()
        if (url) return url.startsWith('/') ? url : '/' + url
      }
      return '/images/no-image.svg'
    },
    getStatusType(status) {
      const typeMap = {
        0: 'info',
        1: 'success',
        2: 'warning',
        3: 'danger'
      }
      return typeMap[status] || 'info'
    },
    getStatusText(status) {
      const textMap = {
        0: '未开始',
        1: '竞拍中',
        2: '已成交',
        3: '已流拍'
      }
      return textMap[status] || '未知'
    },
    getAuditStatusType(status) {
      const typeMap = {
        0: 'warning', // 待审核
        1: 'success', // 审核通过
        2: 'danger',  // 审核驳回
        3: 'info'     // 已下架
      }
      return typeMap[status] || 'info'
    },
    getAuditStatusText(status) {
      const textMap = {
        0: '待审核',
        1: '审核通过',
        2: '审核驳回',
        3: '已下架'
      }
      return textMap[status] || '未知'
    },
    async handleReapply(goods) {
      try {
        await this.$confirm(`确定重新申请上架商品 "${goods.goodsName}" 吗？`, '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        await reapplyGoods(goods.id)
        this.$message.success('重新申请上架成功，等待管理员审核')
        this.loadData()
      } catch (error) {
        if (error !== 'cancel') {
          this.$message.error('重新申请上架失败，请重试')
        }
      }
    },
    formatTime(timeStr) {
      if (!timeStr) return ''
      const date = new Date(timeStr)
      const year = date.getFullYear()
      const month = (date.getMonth() + 1).toString().padStart(2, '0')
      const day = date.getDate().toString().padStart(2, '0')
      const hours = date.getHours().toString().padStart(2, '0')
      const minutes = date.getMinutes().toString().padStart(2, '0')
      return `${year}-${month}-${day} ${hours}:${minutes}`
    }
  }
}
</script>

<style scoped>
.my-goods-page {
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