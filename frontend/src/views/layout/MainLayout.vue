<template>
  <el-container class="main-layout">
    <!-- 左侧边栏 -->
    <el-aside :width="isCollapse ? '64px' : '220px'" class="sidebar">
      <div class="logo-container">
        <i class="el-icon-s-opportunity logo-icon"></i>
        <span v-show="!isCollapse" class="logo-text">智慧渔业</span>
      </div>
      <el-scrollbar class="menu-scrollbar">
        <el-menu
          :default-active="activeMenu"
          :collapse="isCollapse"
          :collapse-transition="false"
          background-color="transparent"
          text-color="#636e72"
          active-text-color="#ff69b4"
          router
        >
          <!-- 系统概览 -->
          <div v-show="!isCollapse" class="menu-group-title">系统概览</div>
          <el-menu-item index="/dashboard">
            <i class="el-icon-s-home"></i>
            <span slot="title">首页仪表盘</span>
          </el-menu-item>
          <el-menu-item index="/ai-assistant">
            <i class="el-icon-magic-stick"></i>
            <span slot="title">智慧助手</span>
          </el-menu-item>

          <!-- 环境监测 -->
          <div v-show="!isCollapse" class="menu-group-title">环境监测</div>
          <el-menu-item index="/water-quality">
            <i class="el-icon-water-cup"></i>
            <span slot="title">水质数据</span>
          </el-menu-item>
          <el-menu-item index="/weather">
            <i class="el-icon-cloudy"></i>
            <span slot="title">气象数据</span>
          </el-menu-item>
          <el-menu-item index="/simulate" v-if="isAdmin">
            <i class="el-icon-cpu"></i>
            <span slot="title">数据模拟</span>
          </el-menu-item>

          <!-- 生产管理 -->
          <div v-show="!isCollapse" class="menu-group-title">生产管理</div>
          <el-menu-item index="/cage">
            <i class="el-icon-box"></i>
            <span slot="title">网箱管理</span>
          </el-menu-item>
          <el-menu-item index="/feeding">
            <i class="el-icon-food"></i>
            <span slot="title">投喂管理</span>
          </el-menu-item>
          <el-menu-item index="/disease">
            <i class="el-icon-warning-outline"></i>
            <span slot="title">病害管理</span>
          </el-menu-item>
          <el-menu-item index="/staff">
            <i class="el-icon-user"></i>
            <span slot="title">人员管理</span>
          </el-menu-item>
          <el-menu-item index="/stock">
            <i class="el-icon-goods"></i>
            <span slot="title">饲料库存</span>
          </el-menu-item>

          <!-- 智能预警 -->
          <div v-show="!isCollapse" class="menu-group-title">智能预警</div>
          <el-menu-item index="/alert/list">
            <i class="el-icon-bell"></i>
            <span slot="title">预警记录</span>
          </el-menu-item>
          <el-menu-item index="/alert/threshold" v-if="isAdmin">
            <i class="el-icon-setting"></i>
            <span slot="title">阈值设置</span>
          </el-menu-item>
          <el-menu-item index="/statistics">
            <i class="el-icon-data-analysis"></i>
            <span slot="title">数据统计</span>
          </el-menu-item>
          <el-menu-item index="/trace" v-if="isAdmin">
            <i class="el-icon-link"></i>
            <span slot="title">溯源管理</span>
          </el-menu-item>
          <el-menu-item index="/trace/query">
            <i class="el-icon-search"></i>
            <span slot="title">溯源查询</span>
          </el-menu-item>

          <!-- 系统工具 -->
          <div v-show="!isCollapse" class="menu-group-title">系统工具</div>
          <el-menu-item index="/screen">
            <i class="el-icon-monitor"></i>
            <span slot="title">可视化大屏</span>
          </el-menu-item>
          <el-menu-item index="/backup" v-if="isAdmin">
            <i class="el-icon-upload2"></i>
            <span slot="title">数据备份</span>
          </el-menu-item>
          <el-menu-item index="/user/manage" v-if="isAdmin">
            <i class="el-icon-user-solid"></i>
            <span slot="title">用户管理</span>
          </el-menu-item>
        </el-menu>
      </el-scrollbar>
    </el-aside>

    <el-container class="main-container">
      <!-- 顶部导航栏 -->
      <el-header class="header" height="56px">
        <div class="header-left">
          <i
            :class="isCollapse ? 'el-icon-s-unfold' : 'el-icon-s-fold'"
            class="hamburger"
            @click="toggleCollapse"
          ></i>
          <span class="system-title">智慧渔业管理系统</span>
        </div>
        <div class="header-right">
          <!-- 告警铃铛 -->
          <el-badge :value="unhandledAlertCount" :hidden="unhandledAlertCount === 0" class="alert-badge" @click.native="$router.push('/alert/list')">
            <i class="el-icon-bell alert-bell"></i>
          </el-badge>

          <!-- 用户下拉菜单 -->
          <el-dropdown trigger="click" @command="handleCommand">
            <span class="user-dropdown">
              <i class="el-icon-user-solid"></i>
              <span class="username">{{ user.username || '管理员' }}</span>
              <i class="el-icon-arrow-down"></i>
            </span>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item icon="el-icon-user" command="userInfo">用户信息</el-dropdown-item>
              <el-dropdown-item icon="el-icon-edit" command="changePassword">修改密码</el-dropdown-item>
              <el-dropdown-item icon="el-icon-switch-button" divided command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 主内容区域 -->
      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>

    <!-- 修改密码对话框 -->
    <el-dialog title="修改密码" :visible.sync="passwordDialogVisible" width="420px" append-to-body>
      <el-form ref="passwordForm" :model="passwordForm" :rules="passwordRules" label-width="90px">
        <el-form-item label="原密码" prop="oldPassword">
          <el-input v-model="passwordForm.oldPassword" type="password" placeholder="请输入原密码" show-password />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="passwordForm.newPassword" type="password" placeholder="请输入新密码" show-password />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="passwordForm.confirmPassword" type="password" placeholder="请再次输入新密码" show-password />
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="passwordDialogVisible = false">取 消</el-button>
        <el-button type="primary" :loading="passwordLoading" @click="submitPassword">确 定</el-button>
      </span>
    </el-dialog>
  </el-container>
</template>

<script>
import { mapGetters } from 'vuex';
import { changePassword, logout } from '@/api/auth';

export default {
  name: 'MainLayout',
  data() {
    const validateConfirmPassword = (rule, value, callback) => {
      if (value !== this.passwordForm.newPassword) {
        callback(new Error('两次输入的密码不一致'));
      } else {
        callback();
      }
    };
    return {
      isCollapse: false,
      passwordDialogVisible: false,
      passwordLoading: false,
      passwordForm: {
        oldPassword: '',
        newPassword: '',
        confirmPassword: ''
      },
      passwordRules: {
        oldPassword: [
          { required: true, message: '请输入原密码', trigger: 'blur' }
        ],
        newPassword: [
          { required: true, message: '请输入新密码', trigger: 'blur' },
          { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
        ],
        confirmPassword: [
          { required: true, message: '请再次输入新密码', trigger: 'blur' },
          { validator: validateConfirmPassword, trigger: 'blur' }
        ]
      },
      alertTimer: null
    };
  },
  computed: {
    ...mapGetters(['user', 'unhandledAlertCount']),
    activeMenu() {
      return this.$route.path;
    },
    isAdmin() {
      return this.user.role === 'admin';
    }
  },
  created() {
    this.$store.dispatch('fetchAlertCount');
    this.$store.dispatch('fetchCageList');
    this.startPolling();
    this.checkUserInfo();
  },
  beforeDestroy() {
    this.stopPolling();
  },
  methods: {
    async checkUserInfo() {
      // 检查用户信息是否完整
      if (!this.user || !this.user.role) {
        try {
          const { getUserInfo } = await import('@/api/auth');
          const res = await getUserInfo();
          if (res && res.data) {
            const userInfo = res.data;
            localStorage.setItem('user', JSON.stringify(userInfo));
            this.$store.commit('SET_USER', userInfo);
          }
        } catch (e) {
          console.error('获取用户信息失败:', e);
        }
      }
    },
    toggleCollapse() {
      this.isCollapse = !this.isCollapse;
    },
    handleCommand(command) {
      if (command === 'logout') {
        this.handleLogout();
      } else if (command === 'changePassword') {
        this.passwordDialogVisible = true;
        this.passwordForm = { oldPassword: '', newPassword: '', confirmPassword: '' };
      } else if (command === 'userInfo') {
        this.$router.push('/user/info').catch(() => {});
      }
    },
    async handleLogout() {
      try {
        await this.$confirm('确定要退出登录吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        });
        await logout();
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        this.$store.commit('SET_USER', {});
        if (this.$route.path !== '/login') {
          this.$router.push('/login').catch(() => {});
        }
      } catch (e) {
        // 用户点击取消或请求失败，什么都不做
        if (e !== 'cancel') {
          console.error('退出登录失败:', e);
        }
      }
    },
    async submitPassword() {
      this.$refs.passwordForm.validate(async (valid) => {
        if (!valid) return;
        this.passwordLoading = true;
        try {
          await changePassword({
            oldPassword: this.passwordForm.oldPassword,
            newPassword: this.passwordForm.newPassword
          });
          this.$message.success('密码修改成功，请重新登录');
          this.passwordDialogVisible = false;
          localStorage.removeItem('token');
          localStorage.removeItem('user');
          this.$router.push('/login');
        } catch (error) {
          console.error('修改密码失败:', error);
        } finally {
          this.passwordLoading = false;
        }
      });
    },
    startPolling() {
      this.alertTimer = setInterval(() => {
        this.$store.dispatch('fetchAlertCount');
      }, 30000);
    },
    stopPolling() {
      if (this.alertTimer) {
        clearInterval(this.alertTimer);
        this.alertTimer = null;
      }
    }
  }
};
</script>

<style scoped>
.main-layout {
  height: 100vh;
  overflow: hidden;
}

/* ==================== 侧边栏 ==================== */
.sidebar {
  background: linear-gradient(180deg, #fff0f5 0%, #ffe8f0 100%);
  transition: width 0.3s ease;
  overflow: hidden;
  box-shadow: 2px 0 20px rgba(255, 105, 180, 0.12);
  position: relative;
  z-index: 20;
  border-right: 1px solid rgba(255, 105, 180, 0.15);
}

/* Logo区域 */
.logo-container {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #ff69b4 0%, #ff85c0 100%);
  flex-shrink: 0;
  box-shadow: 0 4px 15px rgba(255, 105, 180, 0.2);
}

.logo-icon {
  font-size: 28px;
  color: #ffffff;
}

.logo-text {
  font-size: 16px;
  color: #ffffff;
  font-weight: 700;
  margin-left: 8px;
  white-space: nowrap;
  letter-spacing: 1px;
  text-shadow: 0 2px 8px rgba(255, 255, 255, 0.3);
}

/* 菜单滚动条 */
.menu-scrollbar {
  height: calc(100vh - 60px);
}

/* 菜单基础样式 */
.el-menu {
  border-right: none !important;
  background: transparent !important;
  padding: 8px 0;
}

/* 菜单分组标题 */
.menu-group-title {
  height: 36px;
  line-height: 36px;
  padding: 0 24px;
  font-size: 11px;
  color: #b2bec3;
  font-weight: 700;
  letter-spacing: 1.5px;
  text-transform: uppercase;
  user-select: none;
  white-space: nowrap;
  overflow: hidden;
  margin-top: 8px;
}

/* 菜单项 */
.sidebar .el-menu-item {
  height: 44px;
  line-height: 44px;
  margin: 4px 10px;
  border-radius: 12px;
  padding-left: 20px !important;
  font-size: 14px;
  color: #636e72 !important;
  background: transparent !important;
  transition: all 0.3s ease;
  font-weight: 500;
}

.sidebar .el-menu-item i {
  color: #636e72;
  margin-right: 12px;
  font-size: 16px;
  transition: all 0.3s ease;
}

/* 菜单hover */
.sidebar .el-menu-item:hover {
  background: rgba(255, 105, 180, 0.1) !important;
  color: #ff69b4 !important;
  transform: translateX(4px);
}

.sidebar .el-menu-item:hover i {
  color: #ff69b4;
}

/* 菜单active */
.sidebar .el-menu-item.is-active {
  background: linear-gradient(135deg, rgba(255, 105, 180, 0.18) 0%, rgba(255, 133, 192, 0.12) 100%) !important;
  color: #ff69b4 !important;
  font-weight: 700;
  box-shadow: 0 4px 15px rgba(255, 105, 180, 0.15);
}

.sidebar .el-menu-item.is-active i {
  color: #ff69b4;
}

/* 折叠状态菜单 */
.sidebar .el-menu--collapse .el-menu-item {
  margin: 4px 6px;
  padding: 0 !important;
  text-align: center;
}

.sidebar .el-menu--collapse .el-menu-item i {
  margin-right: 0;
}

/* ==================== 顶栏 ==================== */
.main-container {
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: linear-gradient(135deg, #ffffff 0%, #fffafc 100%);
  padding: 0 24px;
  z-index: 10;
  box-shadow: 0 2px 12px rgba(255, 105, 180, 0.08);
  border-bottom: 1px solid rgba(255, 105, 180, 0.1);
  flex-shrink: 0;
}

.header-left {
  display: flex;
  align-items: center;
}

.hamburger {
  font-size: 20px;
  cursor: pointer;
  color: #636e72;
  margin-right: 16px;
  transition: all 0.3s ease;
  padding: 8px;
  border-radius: 8px;
}

.hamburger:hover {
  color: #ff69b4;
  background: rgba(255, 105, 180, 0.08);
  transform: rotate(90deg);
}

.system-title {
  font-size: 16px;
  font-weight: 700;
  color: #2d3436;
  letter-spacing: 0.5px;
  background: linear-gradient(135deg, #ff69b4, #ff85c0);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

/* 铃铛 */
.alert-badge {
  line-height: 1;
  cursor: pointer;
  transition: transform 0.3s ease;
}

.alert-badge:hover {
  transform: scale(1.15);
}

.alert-bell {
  font-size: 20px;
  cursor: pointer;
  color: #636e72;
  transition: color 0.3s ease;
}

.alert-bell:hover {
  color: #ff69b4;
}

/* 用户下拉 */
.user-dropdown {
  display: flex;
  align-items: center;
  cursor: pointer;
  color: #636e72;
  font-size: 14px;
  transition: all 0.3s ease;
  padding: 8px 12px;
  border-radius: 10px;
  background: rgba(255, 105, 180, 0.05);
}

.user-dropdown:hover {
  color: #ff69b4;
  background: rgba(255, 105, 180, 0.1);
}

.username {
  margin: 0 8px;
  font-weight: 600;
}

/* ==================== 主内容区 ==================== */
.main-content {
  background: linear-gradient(135deg, #fff5f8 0%, #fffafc 100%);
  overflow-y: auto;
  padding: 24px;
  position: relative;
}

/* 装饰性背景 */
.main-content::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image: 
    radial-gradient(circle at 20% 20%, rgba(255, 105, 180, 0.03) 0%, transparent 50%),
    radial-gradient(circle at 80% 80%, rgba(255, 133, 192, 0.03) 0%, transparent 50%);
  pointer-events: none;
  z-index: 0;
}

.main-content > * {
  position: relative;
  z-index: 1;
}

/* ==================== 全局滚动条美化 ==================== */
.main-content::-webkit-scrollbar {
  width: 8px;
}

.main-content::-webkit-scrollbar-track {
  background: #fff5f8;
  border-radius: 4px;
}

.main-content::-webkit-scrollbar-thumb {
  background: linear-gradient(180deg, #ff69b4, #ff85c0);
  border-radius: 4px;
  opacity: 0.6;
}

.main-content::-webkit-scrollbar-thumb:hover {
  background: linear-gradient(180deg, #ff5a9e, #ff7ab5);
  opacity: 0.8;
}

.menu-scrollbar >>> .el-scrollbar__wrap {
  overflow-x: hidden;
}

.menu-scrollbar >>> .el-scrollbar__bar.is-vertical {
  width: 6px;
}

.menu-scrollbar >>> .el-scrollbar__thumb {
  background: linear-gradient(180deg, #ff69b4, #ff85c0);
  border-radius: 3px;
  opacity: 0.5;
}
</style>
