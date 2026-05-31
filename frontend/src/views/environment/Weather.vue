<template>
  <div class="weather">
    <!-- 搜索栏 -->
    <el-card shadow="never" class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="记录时间">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="yyyy-MM-dd"
            :picker-options="pickerOptions"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" @click="handleSearch">搜索</el-button>
          <el-button icon="el-icon-refresh" @click="handleReset">重置</el-button>
          <el-button type="success" icon="el-icon-plus" @click="handleAdd">新增</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 数据表格 -->
    <el-card shadow="never" class="table-card">
      <el-table :data="tableData" stripe border v-loading="loading" style="width: 100%">
        <el-table-column type="index" label="编号" width="60" align="center" />
        <el-table-column prop="temperature" label="气温(℃)" width="100" align="center" />
        <el-table-column prop="windSpeed" label="风速(m/s)" width="110" align="center" />
        <el-table-column prop="weatherDesc" label="天气描述" min-width="120" align="center">
          <template slot-scope="scope">
            <span>{{ getWeatherIcon(scope.row.weatherDesc) }} {{ scope.row.weatherDesc }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="recordTime" label="记录时间" min-width="160" />
        <el-table-column prop="dataSource" label="数据来源" width="100" align="center">
          <template slot-scope="scope">
            <el-tag :type="scope.row.dataSource === '手动录入' ? '' : 'success'" size="small">
              {{ scope.row.dataSource || '-' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="80" align="center" fixed="right">
          <template slot-scope="scope">
            <el-button type="text" icon="el-icon-delete" style="color: #F56C6C;" @click="handleDelete(scope.row)">删除</el-button>
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

    <!-- 新增对话框 -->
    <el-dialog title="新增气象数据" :visible.sync="dialogVisible" width="520px" @close="resetForm">
      <el-form ref="weatherForm" :model="weatherForm" :rules="weatherRules" label-width="100px">
        <el-form-item label="气温(℃)" prop="temperature">
          <el-input-number v-model="weatherForm.temperature" :precision="1" :step="0.5" :min="-40" :max="60" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="风速(m/s)" prop="windSpeed">
          <el-input-number v-model="weatherForm.windSpeed" :precision="1" :step="0.1" :min="0" :max="100" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="天气描述" prop="weatherDesc">
          <el-select v-model="weatherForm.weatherDesc" placeholder="请选择天气" style="width: 100%;">
            <el-option label="晴" value="晴" />
            <el-option label="多云" value="多云" />
            <el-option label="阴" value="阴" />
            <el-option label="小雨" value="小雨" />
            <el-option label="大雨" value="大雨" />
          </el-select>
        </el-form-item>
        <el-form-item label="记录时间" prop="recordTime">
          <el-date-picker
            v-model="weatherForm.recordTime"
            type="datetime"
            placeholder="选择记录时间"
            value-format="yyyy-MM-dd HH:mm:ss"
            style="width: 100%;"
          />
        </el-form-item>
        <el-form-item label="数据来源" prop="dataSource">
          <el-select v-model="weatherForm.dataSource" placeholder="请选择数据来源" style="width: 100%;">
            <el-option label="手动录入" value="手动录入" />
            <el-option label="传感器" value="传感器" />
            <el-option label="数据导入" value="数据导入" />
          </el-select>
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
import { getList, add, deleteRecord } from '@/api/weather';
import Pagination from '@/components/Pagination';

export default {
  name: 'Weather',
  components: { Pagination },
  data() {
    return {
      loading: false,
      tableData: [],
      total: 0,
      searchForm: {},
      dateRange: null,
      listQuery: {
        page: 1,
        limit: 10
      },
      pickerOptions: {
        shortcuts: [
          {
            text: '最近一周',
            onClick(picker) {
              const end = new Date();
              const start = new Date();
              start.setTime(start.getTime() - 7 * 24 * 3600 * 1000);
              picker.$emit('pick', [start, end]);
            }
          },
          {
            text: '最近一个月',
            onClick(picker) {
              const end = new Date();
              const start = new Date();
              start.setTime(start.getTime() - 30 * 24 * 3600 * 1000);
              picker.$emit('pick', [start, end]);
            }
          }
        ]
      },
      dialogVisible: false,
      submitLoading: false,
      weatherForm: {
        temperature: null,
        windSpeed: null,
        weatherDesc: '',
        recordTime: '',
        dataSource: '手动录入'
      },
      weatherRules: {
        temperature: [{ required: true, message: '请输入气温', trigger: 'blur' }],
        windSpeed: [{ required: true, message: '请输入风速', trigger: 'blur' }],
        weatherDesc: [{ required: true, message: '请选择天气描述', trigger: 'change' }],
        recordTime: [{ required: true, message: '请选择记录时间', trigger: 'change' }]
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
        const params = {
          page: this.listQuery.page,
          size: this.listQuery.limit,
          startTime: this.dateRange ? this.dateRange[0] : undefined,
          endTime: this.dateRange ? this.dateRange[1] : undefined
        };
        const res = await getList(params);
        const data = res.data || res;
        this.tableData = data.records || data.list || [];
        this.total = data.total || 0;
      } catch (error) {
        console.error('获取气象数据失败:', error);
      } finally {
        this.loading = false;
      }
    },
    handleSearch() {
      this.listQuery.page = 1;
      this.getList();
    },
    handleReset() {
      this.dateRange = null;
      this.listQuery.page = 1;
      this.getList();
    },
    handleAdd() {
      this.weatherForm = {
        temperature: null,
        windSpeed: null,
        weatherDesc: '',
        recordTime: '',
        dataSource: '手动录入'
      };
      this.dialogVisible = true;
    },
    async handleDelete(row) {
      try {
        await this.$confirm('确定要删除该条气象数据吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        });
        await deleteRecord(row.id);
        this.$message.success('删除成功');
        this.getList();
      } catch (error) {
        if (error !== 'cancel') {
          console.error('删除失败:', error);
        }
      }
    },
    submitForm() {
      this.$refs.weatherForm.validate(async (valid) => {
        if (!valid) return;
        this.submitLoading = true;
        try {
          await add(this.weatherForm);
          this.$message.success('新增成功');
          this.dialogVisible = false;
          this.getList();
        } catch (error) {
          console.error('新增失败:', error);
        } finally {
          this.submitLoading = false;
        }
      });
    },
    resetForm() {
      if (this.$refs.weatherForm) {
        this.$refs.weatherForm.resetFields();
      }
    },
    getWeatherIcon(desc) {
      const map = {
        '晴': '\u2600',
        '多云': '\u26C5',
        '阴': '\u2601',
        '小雨': '\uD83C\uDF27',
        '大雨': '\uD83C\uDF27'
      };
      return map[desc] || '';
    }
  }
};
</script>

<style scoped>
.weather {
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
