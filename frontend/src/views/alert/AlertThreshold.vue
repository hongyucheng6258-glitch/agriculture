<template>
  <div class="alert-threshold">
    <!-- 操作栏 -->
    <el-card shadow="never" class="search-card">
      <el-form :inline="true" class="search-form">
        <el-form-item>
          <el-button type="primary" icon="el-icon-refresh" @click="handleInitDefault">初始化默认阈值</el-button>
        </el-form-item>
        <el-form-item>
          <el-button type="danger" icon="el-icon-warning-outline" :loading="recheckAllLoading" @click="handleRecheckAll">全部重新检查预警</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 数据表格 -->
    <el-card shadow="never" class="table-card">
      <el-table :data="tableData" stripe border v-loading="loading" style="width: 100%">
        <el-table-column type="index" label="编号" width="60" align="center" />
        <el-table-column prop="indicatorName" label="指标名称" min-width="140" show-overflow-tooltip />
        <el-table-column prop="indicatorLabel" label="指标标签" min-width="120" show-overflow-tooltip />
        <el-table-column prop="minValue" label="最小阈值" width="120" align="center" />
        <el-table-column prop="maxValue" label="最大阈值" width="120" align="center" />
        <el-table-column prop="unit" label="单位" width="80" align="center" />
        <el-table-column label="是否启用" width="100" align="center">
          <template slot-scope="scope">
            <el-switch
              :value="Number(scope.row.isEnabled)"
              :active-value="1"
              :inactive-value="0"
              @change="handleSwitchChange(scope.row, $event)"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template slot-scope="scope">
            <el-button type="text" icon="el-icon-edit" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button type="text" icon="el-icon-refresh-left" :loading="scope.row.recheckLoading" @click="handleRecheckThreshold(scope.row)">重新检查</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 编辑对话框 -->
    <el-dialog title="编辑预警阈值" :visible.sync="dialogVisible" width="520px" @close="resetForm">
      <el-form ref="thresholdForm" :model="thresholdForm" :rules="thresholdRules" label-width="100px">
        <el-form-item label="指标名称">
          <el-input :value="thresholdForm.indicatorName" disabled />
        </el-form-item>
        <el-form-item label="最小阈值" prop="minValue">
          <el-input-number v-model="thresholdForm.minValue" :precision="2" :step="0.1" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="最大阈值" prop="maxValue">
          <el-input-number v-model="thresholdForm.maxValue" :precision="2" :step="0.1" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="单位" prop="unit">
          <el-input v-model="thresholdForm.unit" placeholder="请输入单位" />
        </el-form-item>
        <el-form-item label="是否启用" prop="isEnabled">
          <el-switch v-model="thresholdForm.isEnabled" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="thresholdForm.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitForm">确 定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { getList, update, initDefault, recheckThreshold, recheckAll } from '@/api/alertThreshold';

export default {
  name: 'AlertThreshold',
  data() {
    return {
      loading: false,
      recheckAllLoading: false,
      tableData: [],
      dialogVisible: false,
      submitLoading: false,
      thresholdForm: {
        id: null,
        indicatorName: '',
        indicatorLabel: '',
        minValue: 0,
        maxValue: 0,
        unit: '',
        isEnabled: 1,
        remark: ''
      },
      thresholdRules: {
        minValue: [{ required: true, message: '请输入最小阈值', trigger: 'blur' }],
        maxValue: [{ required: true, message: '请输入最大阈值', trigger: 'blur' }],
        unit: [{ required: true, message: '请输入单位', trigger: 'blur' }]
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    async getList() {
      this.loading = true;
      try {
        const res = await getList();
        const data = res.data || res;
        this.tableData = data.records || data.list || data || [];
      } catch (error) {
        console.error('获取预警阈值列表失败:', error);
      } finally {
        this.loading = false;
      }
    },
    handleEdit(row) {
      this.thresholdForm = {
        id: row.id,
        indicatorName: row.indicatorName,
        indicatorLabel: row.indicatorLabel,
        minValue: row.minValue,
        maxValue: row.maxValue,
        unit: row.unit,
        isEnabled: row.isEnabled,
        remark: row.remark
      };
      this.dialogVisible = true;
    },
    async handleSwitchChange(row, val) {
      try {
        const newValue = Number(val);
        await update({
          id: row.id,
          isEnabled: newValue
        });
        this.$message.success('更新成功');
        this.getList();
      } catch (error) {
        console.error('更新失败:', error);
      }
    },
    async handleInitDefault() {
      try {
        await this.$confirm('确定要初始化为默认阈值吗？此操作将覆盖当前所有阈值设置。', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        });
        await initDefault();
        this.$message.success('初始化默认阈值成功');
        this.getList();
      } catch (error) {
        if (error !== 'cancel') {
          console.error('初始化默认阈值失败:', error);
        }
      }
    },
    async handleRecheckAll() {
      try {
        await this.$confirm('确定要重新检查所有预警吗？此操作将检查最近的水质和饲料数据。', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'info'
        });
        this.recheckAllLoading = true;
        const res = await recheckAll();
        const data = res.data || res;
        const newAlerts = data.newAlerts || 0;
        this.$message.success(`重新检查完成，新增 ${newAlerts} 条预警`);
      } catch (error) {
        if (error !== 'cancel') {
          console.error('重新检查预警失败:', error);
        }
      } finally {
        this.recheckAllLoading = false;
      }
    },
    async handleRecheckThreshold(row) {
      try {
        this.$set(row, 'recheckLoading', true);
        const res = await recheckThreshold(row.id);
        const data = res.data || res;
        const newAlerts = data.newAlerts || 0;
        this.$message.success(`重新检查完成，新增 ${newAlerts} 条预警`);
      } catch (error) {
        console.error('重新检查预警失败:', error);
      } finally {
        this.$set(row, 'recheckLoading', false);
      }
    },
    submitForm() {
      this.$refs.thresholdForm.validate(async (valid) => {
        if (!valid) return;
        this.submitLoading = true;
        try {
          await update(this.thresholdForm);
          this.$message.success('更新成功');
          this.dialogVisible = false;
          this.getList();
        } catch (error) {
          console.error('更新失败:', error);
        } finally {
          this.submitLoading = false;
        }
      });
    },
    resetForm() {
      if (this.$refs.thresholdForm) {
        this.$refs.thresholdForm.resetFields();
      }
    }
  }
};
</script>

<style scoped>
.alert-threshold {
  padding: 0;
}

.search-card {
  margin-bottom: 16px;
}

.search-card >>> .el-card__body {
  padding: 18px 20px 0;
}

.search-form {
  display: flex;
  flex-wrap: wrap;
}

.table-card >>> .el-card__body {
  padding: 16px 20px;
}
</style>
