<template>
  <div class="goods-detail-page">
    <div class="container">
      <!-- 商品图片展示区 -->
      <div class="image-gallery">
        <div class="main-image">
          <img
            src="https://via.placeholder.com/600x400?text=商品主图"
            alt="商品主图"
            class="main-img"
          />
        </div>
        <div class="thumbnail-list">
          <img
            src="https://via.placeholder.com/150x100?text=图片1"
            alt="图片1"
            class="thumbnail"
          />
          <img
            src="https://via.placeholder.com/150x100?text=图片2"
            alt="图片2"
            class="thumbnail"
          />
          <img
            src="https://via.placeholder.com/150x100?text=图片3"
            alt="图片3"
            class="thumbnail"
          />
          <img
            src="https://via.placeholder.com/150x100?text=图片4"
            alt="图片4"
            class="thumbnail"
          />
        </div>
      </div>

      <!-- 商品信息区 -->
      <div class="product-info">
        <h1 class="product-title">商品名称</h1>
        <div class="price-section">
          <span class="current-price">¥ 999.00</span>
          <span class="original-price">¥ 1299.00</span>
        </div>
        <div class="product-description">
          <h3>商品描述</h3>
          <p>这里是商品的详细描述信息。商品质量上乘，性价比高，值得购买。</p>
        </div>
        <div class="product-details">
          <h3>商品详情</h3>
          <ul>
            <li>品牌：某某品牌</li>
            <li>型号：ABC-123</li>
            <li>颜色：黑色</li>
            <li>尺寸：中号</li>
          </ul>
        </div>
      </div>

      <!-- 商品视频区 -->
      <div class="video-section">
        <h3>商品视频</h3>
        <video controls class="product-video">
          <source
            src="https://www.w3schools.com/html/mov_bbb.mp4"
            type="video/mp4"
          />
          您的浏览器不支持视频播放。
        </video>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: "GoodsDetail",
};
</script>

<style scoped>
.goods-detail-page {
  padding: 20px;
  background-color: #f5f5f5;
  min-height: 100vh;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  background-color: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.image-gallery {
  display: flex;
  gap: 20px;
  margin-bottom: 30px;
}

.main-image {
  flex: 1;
}

.main-img {
  width: 100%;
  height: auto;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.thumbnail-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.thumbnail {
  width: 150px;
  height: 100px;
  object-fit: cover;
  border-radius: 4px;
  cursor: pointer;
  transition: transform 0.2s;
}

.thumbnail:hover {
  transform: scale(1.05);
}

.product-info {
  margin-bottom: 30px;
}

.product-title {
  font-size: 28px;
  margin-bottom: 20px;
  color: #333;
}

.price-section {
  margin-bottom: 20px;
}

.current-price {
  font-size: 24px;
  color: #e74c3c;
  font-weight: bold;
  margin-right: 10px;
}

.original-price {
  font-size: 18px;
  color: #999;
  text-decoration: line-through;
}

.product-description,
.product-details {
  margin-bottom: 20px;
}

.product-description h3,
.product-details h3 {
  font-size: 20px;
  margin-bottom: 10px;
  color: #333;
}

.product-description p {
  line-height: 1.6;
  color: #666;
}

.product-details ul {
  list-style: none;
  padding: 0;
}

.product-details li {
  padding: 5px 0;
  border-bottom: 1px solid #eee;
  color: #666;
}

.video-section {
  margin-top: 30px;
}

.video-section h3 {
  font-size: 20px;
  margin-bottom: 15px;
  color: #333;
}

.product-video {
  width: 100%;
  max-width: 600px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}
</style>
