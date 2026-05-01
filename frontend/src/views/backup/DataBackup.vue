<template>
  <div class="data-backup">
    <!-- 数据库备份 -->
    <el-row :gutter="20">
      <el-col :span="24">
        <el-card shadow="never" class="backup-card">
          <div slot="header" class="card-header">
            <i class="el-icon-coin" style="color: #409EFF; margin-right: 8px;"></i>
            <span>数据库备份</span>
          </div>
          <div class="backup-content">
            <p class="backup-desc">
              对系统数据库进行完整备份，备份文件将自动下载到本地。建议定期进行数据库备份，
              以防止数据丢失。备份操作不会影响系统正常运行。
            </p>
            <div class="backup-actions">
              <el-button
                type="primary"
                icon="el-icon-download"
                :loading="backupLoading"
                @click="handleBackup"
              >
                立即备份
              </el-button>
              <span v-if="lastBackupTime" class="last-backup-time">
                <i class="el-icon-time"></i>
                上次备份时间：{{ lastBackupTime }}
              </span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 数据导出 -->
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="24">
        <el-card shadow="never" class="backup-card">
          <div slot="header" class="card-header">
            <i class="el-icon-document" style="color: #67C23A; margin-right: 8px;"></i>
            <span>数据导出</span>
          </div>
          <div class="export-content">
            <p class="export-desc">
              选择需要导出的数据表，系统将生成对应的Excel文件并自动下载。
            </p>
            <el-form :inline="true" class="export-form">
              <el-form-item label="选择数据表">
                <el-select
                  v-model="exportTable"
                  placeholder="请选择要导出的数据表"
                  style="width: 240px;"
                >
                  <el-option
                    v-for="item in tableOptions"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value"
                  />
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-tooltip :content="!exportTable ? '请先选择要导出的数据表' : '点击导出数据'" placement="top">
                  <el-button
                    type="success"
                    icon="el-icon-download"
                    :loading="exportLoading"
                    :disabled="!exportTable"
                    @click="handleExport"
                  >
                    导出
                  </el-button>
                </el-tooltip>
              </el-form-item>
            </el-form>
            <p v-if="!exportTable" class="export-hint" style="color: #909399; font-size: 13px; margin-top: 10px;">
              <i class="el-icon-info"></i> 请先从下拉菜单中选择要导出的数据表
            </p>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { exportData, backupDatabase } from '@/api/backup';

export default {
  name: 'DataBackup',
  data() {
    return {
      backupLoading: false,
      exportLoading: false,
      exportTable: '',
      lastBackupTime: '',
      tableOptions: [
        { label: '网箱数据', value: 'cage' },
        { label: '水质数据', value: 'water_quality' },
        { label: '气象数据', value: 'weather' },
        { label: '投喂记录', value: 'feeding' },
        { label: '病害记录', value: 'disease' },
        { label: '人员数据', value: 'staff' },
        { label: '预警记录', value: 'alert' },
        { label: '溯源数据', value: 'trace' },
        { label: '饲料库存', value: 'feed_stock' }
      ]
    };
  },
  methods: {
    async handleBackup() {
      this.backupLoading = true;
      try {
        const res = await backupDatabase();
        if (res.code === 200) {
          this.lastBackupTime = this.formatNow();
          this.$message.success(res.message || '数据库备份成功');
        } else {
          this.$message.error(res.message || '数据库备份失败');
        }
      } catch (error) {
        console.error('数据库备份失败:', error);
        this.$message.error('数据库备份失败，请稍后重试');
      } finally {
        this.backupLoading = false;
      }
    },
    async handleExport() {
      if (!this.exportTable) {
        this.$message.warning('请先选择要导出的数据表');
        return;
      }
      this.exportLoading = true;
      try {
        const res = await exportData({ table: this.exportTable });
        if (res.code === 200) {
          const tableName = this.tableOptions.find(t => t.value === this.exportTable)?.label || this.exportTable;
          // 将JSON数据转换为CSV并下载
          this.downloadCSV(res.data, tableName);
          this.$message.success('数据导出成功');
        } else {
          this.$message.error(res.message || '数据导出失败');
        }
      } catch (error) {
        console.error('数据导出失败:', error);
        this.$message.error('数据导出失败，请稍后重试');
      } finally {
        this.exportLoading = false;
      }
    },
    downloadCSV(data, fileName) {
      if (!Array.isArray(data) || data.length === 0) {
        this.$message.warning('没有数据可导出');
        return;
      }
      // 获取表头
      const headers = Object.keys(data[0]);
      // 生成CSV内容（BOM头确保中文正确显示）
      let csv = '\uFEFF' + headers.join(',') + '\n';
      data.forEach(row => {
        const values = headers.map(h => {
          let val = row[h] == null ? '' : String(row[h]);
          // 转义包含逗号或换行的值
          if (val.includes(',') || val.includes('\n') || val.includes('"')) {
            val = '"' + val.replace(/"/g, '""') + '"';
          }
          return val;
        });
        csv += values.join(',') + '\n';
      });
      const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = `${fileName}.csv`;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);
    },
    formatNow() {
      const d = new Date();
      const pad = n => String(n).padStart(2, '0');
      return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`;
    }
  }
};
</script>

<style scoped>
.data-backup {
  padding: 0;
}

.backup-card >>> .el-card__header {
  padding: 14px 20px;
}

.card-header {
  display: flex;
  align-items: center;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.backup-content,
.export-content {
  padding: 8px 0;
}

.backup-desc,
.export-desc {
  font-size: 14px;
  color: #606266;
  line-height: 1.8;
  margin-bottom: 20px;
}

.backup-actions {
  display: flex;
  align-items: center;
  gap: 24px;
}

.last-backup-time {
  font-size: 13px;
  color: #909399;
  display: flex;
  align-items: center;
  gap: 4px;
}

.export-form {
  display: flex;
  align-items: center;
}
</style>
