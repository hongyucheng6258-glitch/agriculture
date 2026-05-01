<template>
  <div class="alert-list">
    <!-- 搜索栏 -->
    <el-card shadow="never" class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="网箱">
          <el-select v-model="searchForm.cageId" placeholder="请选择网箱" clearable @change="handleSearch">
            <el-option v-for="item in cageList" :key="item.id" :label="item.cageCode" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="处理状态">
          <el-select v-model="searchForm.isHandled" placeholder="请选择处理状态" clearable @change="handleSearch">
            <el-option label="未处理" :value="0" />
            <el-option label="已处理" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item label="预警级别">
          <el-select v-model="searchForm.alertLevel" placeholder="请选择预警级别" clearable @change="handleSearch">
            <el-option label="一般" value="一般" />
            <el-option label="严重" value="严重" />
            <el-option label="紧急" value="紧急" />
          </el-select>
        </el-form-item>
        <el-form-item label="预警日期">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="yyyy-MM-dd"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" @click="handleSearch">搜索</el-button>
          <el-button icon="el-icon-refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 数据表格 -->
    <el-card shadow="never" class="table-card">
      <el-table :data="tableData" stripe border v-loading="loading" style="width: 100%">
        <el-table-column type="index" label="编号" width="60" align="center" />
        <el-table-column label="网箱编号" min-width="100" show-overflow-tooltip>
          <template slot-scope="scope">
            {{ getCageCode(scope.row.cageId) }}
          </template>
        </el-table-column>
        <el-table-column prop="indicatorLabel" label="指标名称" min-width="120" show-overflow-tooltip />
        <el-table-column prop="currentValue" label="当前值" width="100" align="center" />
        <el-table-column prop="thresholdValue" label="阈值" width="100" align="center" />
        <el-table-column label="预警类型" width="110" align="center">
          <template slot-scope="scope">
            <el-tag :type="getAlertTypeTag(scope.row.alertType)" size="small">
              {{ scope.row.alertType }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="预警级别" width="100" align="center">
          <template slot-scope="scope">
            <el-tag :type="getAlertLevelTag(scope.row.alertLevel)" size="small">
              {{ scope.row.alertLevel }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="是否处理" width="100" align="center">
          <template slot-scope="scope">
            <el-tag :type="scope.row.isHandled ? 'success' : 'danger'" size="small">
              {{ scope.row.isHandled ? '已处理' : '未处理' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="触发时间" min-width="160" show-overflow-tooltip />
            <el-table-column prop="handleRemark" label="处理意见" min-width="150" show-overflow-tooltip>
              <template slot-scope="scope">
                <span v-if="scope.row.handleRemark">{{ scope.row.handleRemark }}</span>
                <span v-else style="color: #b2bec3;">-</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100" align="center" fixed="right">
              <template slot-scope="scope">
                <el-button
                  type="text"
                  icon="el-icon-s-tools"
                  :disabled="!!scope.row.isHandled"
                  @click="handleProcess(scope.row)"
                >处理</el-button>
              </template>
            </el-table-column>
      </el-table>
      <pagination
        :total="total"
        :page.sync="listQuery.page"
        :limit.sync="listQuery.limit"
        @pagination="getList"
      />
    </el-card>

    <!-- 处理对话框 -->
    <el-dialog title="处理预警" :visible.sync="handleDialogVisible" width="520px" @close="resetHandleForm">
      <el-form ref="handleForm" :model="handleForm" :rules="handleRules" label-width="100px">
        <el-form-item label="预警信息">
          <el-input :value="handleForm.alertInfo" disabled type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="处理人" prop="handler">
          <el-input v-model="handleForm.handler" placeholder="请输入处理人" />
        </el-form-item>
        <el-form-item label="处理时间" prop="handleTime">
          <el-date-picker
            v-model="handleForm.handleTime"
            type="datetime"
            placeholder="处理时间"
            value-format="yyyy-MM-dd HH:mm:ss"
            style="width: 100%;"
          />
        </el-form-item>
        <el-form-item label="处理备注" prop="handleRemark">
          <el-input v-model="handleForm.handleRemark" type="textarea" :rows="3" placeholder="请输入处理备注" />
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="handleDialogVisible = false">取 消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitHandleForm">确 定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { getList, handleAlert } from '@/api/alert';
import { mapGetters } from 'vuex';
import Pagination from '@/components/Pagination';

export default {
  name: 'AlertList',
  components: { Pagination },
  data() {
    return {
      loading: false,
      tableData: [],
      total: 0,
      searchForm: {
        cageId: '',
        isHandled: '',
        alertLevel: '',
        dateRange: null
      },
      listQuery: {
        page: 1,
        limit: 10
      },
      submitLoading: false,
      handleDialogVisible: false,
      handleForm: {
        id: null,
        alertInfo: '',
        handler: '',
        handleTime: '',
        handleRemark: ''
      },
      handleRules: {
        handler: [{ required: true, message: '请输入处理人', trigger: 'blur' }]
      }
    };
  },
  computed: {
    ...mapGetters(['cageList'])
  },
  created() {
    this.getList();
    this.$store.dispatch('fetchCageList');
  },
  watch: {},
  methods: {
    async getList() {
      this.loading = true;
      try {
        const params = {
          page: this.listQuery.page,
          size: this.listQuery.limit,
          cageId: this.searchForm.cageId || undefined,
          isHandled: this.searchForm.isHandled === '' ? undefined : this.searchForm.isHandled,
          alertLevel: this.searchForm.alertLevel || undefined,
          startTime: this.searchForm.dateRange ? this.searchForm.dateRange[0] : undefined,
          endTime: this.searchForm.dateRange ? this.searchForm.dateRange[1] : undefined
        };
        const res = await getList(params);
        const data = res.data || res;
        this.tableData = data.records || data.list || [];
        this.total = data.total || 0;
      } catch (error) {
        console.error('获取预警记录列表失败:', error);
      } finally {
        this.loading = false;
      }
    },
    handleSearch() {
      this.listQuery.page = 1;
      this.getList();
    },
    handleReset() {
      this.searchForm = { cageId: '', isHandled: '', alertLevel: '', dateRange: null };
      this.listQuery.page = 1;
      this.getList();
    },
    getCageCode(cageId) {
      if (!cageId) return '-';
      const cage = this.cageList.find(c => c.id === cageId);
      return cage ? cage.cageCode : cageId;
    },
    handleProcess(row) {
      this.handleForm = {
        id: row.id,
        alertInfo: `${row.indicatorLabel || row.indicatorName || ''} - ${row.alertType} (当前值: ${row.currentValue}, 阈值: ${row.thresholdValue})`,
        handler: '',
        handleTime: this.formatNow(),
        handleRemark: ''
      };
      this.handleDialogVisible = true;
    },
    formatNow() {
      const d = new Date();
      const pad = n => String(n).padStart(2, '0');
      return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`;
    },
    submitHandleForm() {
      this.$refs.handleForm.validate(async (valid) => {
        if (!valid) return;
        this.submitLoading = true;
        try {
          await handleAlert(this.handleForm);
          this.$message.success('处理成功');
          this.handleDialogVisible = false;
          this.getList();
          this.$store.dispatch('fetchAlertCount');
        } catch (error) {
          console.error('处理失败:', error);
        } finally {
          this.submitLoading = false;
        }
      });
    },
    resetHandleForm() {
      if (this.$refs.handleForm) {
        this.$refs.handleForm.resetFields();
      }
    },
    getAlertTypeTag(type) {
      const map = { '低于下限': 'warning', '高于上限': 'danger' };
      return map[type] || 'info';
    },
    getAlertLevelTag(level) {
      const map = { '一般': 'info', '严重': 'warning', '紧急': 'danger' };
      return map[level] || 'info';
    }
  }
};
</script>

<style scoped>
.alert-list {
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
