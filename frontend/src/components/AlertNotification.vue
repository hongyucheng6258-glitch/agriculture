<template>
  <div class="alert-notification">
    <el-dialog
      title="告警通知"
      :visible.sync="dialogVisible"
      width="500px"
      :before-close="handleClose"
    >
      <div class="alert-content">
        <el-alert
          v-for="alert in alertList"
          :key="alert.id"
          :title="alert.title || alert.message"
          :type="getAlertType(alert.level)"
          :description="alert.description || alert.content"
          show-icon
          :closable="false"
          class="alert-item"
        >
          <template slot="title">
            <div class="alert-title-row">
              <span class="alert-title">{{ alert.title || alert.message }}</span>
              <el-button
                v-if="alert.status === 0"
                type="text"
                size="mini"
                @click="handleAlertItem(alert)"
              >处理</el-button>
            </div>
          </template>
        </el-alert>

        <el-empty v-if="!alertList || alertList.length === 0" description="暂无告警信息" />
      </div>

      <span slot="footer" class="dialog-footer">
        <el-button @click="handleClose">关闭</el-button>
        <el-button type="primary" @click="goToAlertList">查看全部告警</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { getUnhandled, handleAlert } from '@/api/alert';

export default {
  name: 'AlertNotification',
  props: {
    visible: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      dialogVisible: false,
      alertList: []
    };
  },
  watch: {
    visible(val) {
      this.dialogVisible = val;
      if (val) {
        this.fetchAlerts();
      }
    },
    dialogVisible(val) {
      this.$emit('update:visible', val);
    }
  },
  methods: {
    async fetchAlerts() {
      try {
        const res = await getUnhandled({ pageSize: 10 });
        this.alertList = res.data || res.records || [];
      } catch (error) {
        console.error('获取告警列表失败:', error);
      }
    },
    getAlertType(level) {
      const typeMap = {
        1: 'warning',
        2: 'error',
        3: 'info'
      };
      return typeMap[level] || 'warning';
    },
    async handleAlertItem(alert) {
      try {
        await handleAlert({ id: alert.id, handleResult: '已处理' });
        this.$message.success('告警已处理');
        this.fetchAlerts();
        this.$emit('handled');
      } catch (error) {
        console.error('处理告警失败:', error);
      }
    },
    handleClose() {
      this.dialogVisible = false;
    },
    goToAlertList() {
      this.dialogVisible = false;
      this.$router.push('/alert/list');
    }
  }
};
</script>

<style scoped>
.alert-content {
  max-height: 400px;
  overflow-y: auto;
}

.alert-item {
  margin-bottom: 10px;
}

.alert-title-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.alert-title {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
}
</style>
