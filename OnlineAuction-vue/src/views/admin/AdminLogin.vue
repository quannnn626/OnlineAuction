<template>
  <div class="login-container admin-login">
    <div class="login-box">
      <div class="login-header">
        <h2>在线拍卖系统</h2>
        <p>后台管理登录</p>
      </div>
      <el-form
        ref="loginForm"
        :model="loginForm"
        :rules="loginRules"
        class="login-form"
      >
        <el-form-item prop="userName">
          <el-input
            v-model="loginForm.userName"
            placeholder="请输入管理账号"
            prefix-icon="el-icon-user"
            @keyup.enter.native="handleLogin"
          ></el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            prefix-icon="el-icon-lock"
            @keyup.enter.native="handleLogin"
            show-password
          ></el-input>
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            :loading="loading"
            @click="handleLogin"
            class="login-button"
          >
            {{ loading ? "登录中..." : "登录" }}
          </el-button>
        </el-form-item>
      </el-form>
      <div class="login-footer">
        <p>
          <router-link to="/login">前台用户登录</router-link>
        </p>
      </div>
    </div>
  </div>
</template>

<script>
import { adminLogin } from "@/api/auth";

export default {
  name: "AdminLogin",
  data() {
    return {
      loginForm: {
        userName: "",
        password: "",
      },
      loginRules: {
        userName: [
          { required: true, message: "请输入用户名", trigger: "blur" },
        ],
        password: [
          { required: true, message: "请输入密码", trigger: "blur" },
          { min: 6, message: "密码长度不能少于6位", trigger: "blur" },
        ],
      },
      loading: false,
    };
  },
  methods: {
    handleLogin() {
      this.$refs.loginForm.validate((valid) => {
        if (valid) {
          this.loading = true;
          adminLogin(this.loginForm.userName, this.loginForm.password)
            .then((response) => {
              this.loading = false;
              if (response && response.user) {
                localStorage.setItem("userInfo", JSON.stringify(response.user));
                localStorage.setItem("userId", response.user.id);
                localStorage.setItem("userName", response.user.userName);
                localStorage.setItem("userRole", response.user.userRole);
                localStorage.setItem("isAdmin", response.user.isAdmin);
                localStorage.setItem("isSuperAdmin", response.user.isSuperAdmin);
                localStorage.setItem("isBuyer", response.user.isBuyer);
                localStorage.setItem("isSeller", response.user.isSeller);

                this.$message.success("登录成功");
                this.$router.push("/admin/profile");
              }
            })
            .catch(() => {
              this.loading = false;
            });
        }
      });
    },
  },
};
</script>

<style scoped>
.admin-login.login-container {
  background: linear-gradient(135deg, #1e3a5f 0%, #0f172a 100%);
}

.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
}

.login-box {
  width: 400px;
  padding: 40px;
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.login-header h2 {
  color: #333;
  margin-bottom: 10px;
  font-size: 28px;
}

.login-header p {
  color: #999;
  font-size: 14px;
}

.login-form {
  margin-top: 30px;
}

.login-button {
  width: 100%;
  height: 45px;
  font-size: 16px;
}

.login-footer {
  text-align: center;
  margin-top: 20px;
  font-size: 14px;
  color: #999;
}

.login-footer a {
  color: #1e3a5f;
  text-decoration: none;
}

.login-footer a:hover {
  text-decoration: underline;
}
</style>
