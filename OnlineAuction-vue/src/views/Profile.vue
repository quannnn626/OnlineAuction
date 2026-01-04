<template>
  <div class="profile-page">
    <el-tabs v-model="activeTab" @tab-click="handleTabClick">
      <el-tab-pane label="个人信息" name="info">
        <div class="profile-content">
          <h3>个人信息</h3>
          <p>个人信息页面开发中...</p>
        </div>
      </el-tab-pane>
      <el-tab-pane label="订单管理" name="order">
        <router-view />
      </el-tab-pane>
      <el-tab-pane label="保证金管理" name="deposit">
        <router-view />
      </el-tab-pane>
      <el-tab-pane label="消息中心" name="message">
        <router-view />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script>
export default {
  name: 'Profile',
  data() {
    return {
      activeTab: 'info'
    }
  },
  mounted() {
    this.updateActiveTab()
  },
  watch: {
    $route() {
      this.updateActiveTab()
    }
  },
  methods: {
    updateActiveTab() {
      const routeName = this.$route.name
      if (routeName === 'ProfileOrder') {
        this.activeTab = 'order'
      } else if (routeName === 'ProfileDeposit') {
        this.activeTab = 'deposit'
      } else if (routeName === 'ProfileMessage') {
        this.activeTab = 'message'
      } else {
        this.activeTab = 'info'
      }
    },
    handleTabClick(tab) {
      const routeMap = {
        'order': '/profile/order',
        'deposit': '/profile/deposit',
        'message': '/profile/message'
      }
      if (routeMap[tab.name]) {
        this.$router.push(routeMap[tab.name])
      }
    }
  }
}
</script>

