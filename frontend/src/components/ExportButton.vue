<template>
  <div class="export-button">
    <el-button
      type="success"
      icon="el-icon-download"
      :loading="loading"
      :disabled="disabled"
      @click="handleExport"
    >
      {{ buttonText }}
    </el-button>
  </div>
</template>

<script>
import { Message } from 'element-ui';

export default {
  name: 'ExportButton',
  props: {
    // 导出API函数
    exportFunction: {
      type: Function,
      required: true
    },
    // 导出参数
    exportParams: {
      type: Object,
      default() {
        return {};
      }
    },
    // 导出文件名（不含扩展名）
    fileName: {
      type: String,
      default: '导出数据'
    },
    // 按钮文字
    buttonText: {
      type: String,
      default: '导出'
    },
    // 是否禁用
    disabled: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      loading: false
    };
  },
  methods: {
    async handleExport() {
      this.loading = true;
      try {
        const res = await this.exportFunction(this.exportParams);

        // 处理Blob下载
        const blob = new Blob([res], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = `${this.fileName}.xlsx`;
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        window.URL.revokeObjectURL(url);

        this.$message.success('导出成功');
        this.$emit('success');
      } catch (error) {
        console.error('导出失败:', error);
        Message.error('导出失败，请稍后重试');
        this.$emit('fail', error);
      } finally {
        this.loading = false;
      }
    }
  }
};
</script>

<style scoped>
.export-button {
  display: inline-block;
}
</style>
