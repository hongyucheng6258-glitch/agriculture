<template>
  <div class="water-quality">
    <!-- 搜索栏 -->
    <el-card shadow="never" class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="网箱">
          <el-select v-model="searchForm.cageId" placeholder="请选择网箱" clearable filterable @change="handleSearch">
            <el-option
              v-for="item in cageList"
              :key="item.id"
              :label="item.cageCode"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
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
        <el-table-column prop="cageId" label="网箱ID" width="80" align="center" />
        <el-table-column prop="waterTemp" label="水温(℃)" width="100" align="center" />
        <el-table-column prop="salinity" label="盐度(‰)" width="100" align="center" />
        <el-table-column prop="dissolvedOxygen" label="溶解氧(mg/L)" width="120" align="center" />
        <el-table-column prop="ph" label="pH值" width="80" align="center" />
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

    <!-- 趋势图表 -->
    <el-card shadow="never" class="chart-card">
      <div slot="header" class="card-header">
        <span>水质趋势图</span>
        <div class="chart-controls">
          <el-select v-model="trendIndicator" placeholder="选择指标" size="small" style="width: 140px;" @change="loadTrend">
            <el-option label="水温" value="waterTemp" />
            <el-option label="盐度" value="salinity" />
            <el-option label="溶解氧" value="dissolvedOxygen" />
            <el-option label="pH值" value="ph" />
          </el-select>
          <el-select v-model="trendCageId" placeholder="选择网箱" size="small" clearable filterable style="width: 140px;" @change="loadTrend">
            <el-option
              v-for="item in cageList"
              :key="item.id"
              :label="item.cageCode"
              :value="item.id"
            />
          </el-select>
        </div>
      </div>
      <div ref="trendChart" class="trend-chart"></div>
    </el-card>

    <!-- 新增对话框 -->
    <el-dialog title="新增水质数据" :visible.sync="dialogVisible" width="560px" @close="resetForm">
      <el-form ref="qualityForm" :model="qualityForm" :rules="qualityRules" label-width="100px">
        <el-form-item label="网箱" prop="cageId">
          <el-select v-model="qualityForm.cageId" placeholder="请选择网箱" filterable style="width: 100%;">
            <el-option
              v-for="item in cageList"
              :key="item.id"
              :label="item.cageCode"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="水温(℃)" prop="waterTemp">
          <el-input-number v-model="qualityForm.waterTemp" :precision="1" :step="0.1" :min="-10" :max="50" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="盐度(‰)" prop="salinity">
          <el-input-number v-model="qualityForm.salinity" :precision="1" :step="0.1" :min="0" :max="50" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="溶解氧(mg/L)" prop="dissolvedOxygen">
          <el-input-number v-model="qualityForm.dissolvedOxygen" :precision="2" :step="0.1" :min="0" :max="20" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="pH值" prop="ph">
          <el-input-number v-model="qualityForm.ph" :precision="2" :step="0.1" :min="0" :max="14" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="记录时间" prop="recordTime">
          <el-date-picker
            v-model="qualityForm.recordTime"
            type="datetime"
            placeholder="选择记录时间"
            value-format="yyyy-MM-dd HH:mm:ss"
            style="width: 100%;"
          />
        </el-form-item>
        <el-form-item label="数据来源" prop="dataSource">
          <el-select v-model="qualityForm.dataSource" placeholder="请选择数据来源" style="width: 100%;">
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
import { getList, add, deleteRecord, getTrend } from '@/api/waterQuality';
import { mapGetters } from 'vuex';
import Pagination from '@/components/Pagination';
import * as echarts from 'echarts';

export default {
  name: 'WaterQuality',
  components: { Pagination },
  data() {
    return {
      loading: false,
      tableData: [],
      total: 0,
      searchForm: {
        cageId: ''
      },
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
      qualityForm: {
        cageId: '',
        waterTemp: null,
        salinity: null,
        dissolvedOxygen: null,
        ph: null,
        recordTime: '',
        dataSource: '手动录入'
      },
      qualityRules: {
        cageId: [{ required: true, message: '请选择网箱', trigger: 'change' }],
        waterTemp: [{ required: true, message: '请输入水温', trigger: 'blur' }],
        dissolvedOxygen: [{ required: true, message: '请输入溶解氧', trigger: 'blur' }],
        ph: [{ required: true, message: '请输入pH值', trigger: 'blur' }],
        recordTime: [{ required: true, message: '请选择记录时间', trigger: 'change' }]
      },
      trendIndicator: 'waterTemp',
      trendCageId: '',
      chart: null
    };
  },
  computed: {
    ...mapGetters(['cageList']),
    indicatorLabel() {
      const map = {
        waterTemp: '水温(℃)',
        salinity: '盐度(‰)',
        dissolvedOxygen: '溶解氧(mg/L)',
        ph: 'pH值'
      };
      return map[this.trendIndicator] || '';
    }
  },
  created() {
    this.getList();
  },
  mounted() {
    this.initChart();
    window.addEventListener('resize', this.handleResize);
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.handleResize);
    if (this.chart) {
      this.chart.dispose();
      this.chart = null;
    }
  },
  methods: {
    async getList() {
      this.loading = true;
      try {
        const params = {
          page: this.listQuery.page,
          size: this.listQuery.limit,
          cageId: this.searchForm.cageId || undefined,
          startTime: this.dateRange ? this.dateRange[0] : undefined,
          endTime: this.dateRange ? this.dateRange[1] : undefined
        };
        const res = await getList(params);
        const data = res.data || res;
        this.tableData = data.records || data.list || [];
        this.total = data.total || 0;
      } catch (error) {
        console.error('获取水质数据失败:', error);
      } finally {
        this.loading = false;
      }
    },
    handleSearch() {
      this.listQuery.page = 1;
      this.getList();
    },
    handleReset() {
      this.searchForm = { cageId: '' };
      this.dateRange = null;
      this.listQuery.page = 1;
      this.getList();
    },
    handleAdd() {
      this.qualityForm = {
        cageId: '',
        waterTemp: null,
        salinity: null,
        dissolvedOxygen: null,
        ph: null,
        recordTime: '',
        dataSource: '手动录入'
      };
      this.dialogVisible = true;
    },
    async handleDelete(row) {
      try {
        await this.$confirm('确定要删除该条水质数据吗？', '提示', {
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
      this.$refs.qualityForm.validate(async (valid) => {
        if (!valid) return;
        this.submitLoading = true;
        try {
          await add(this.qualityForm);
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
      if (this.$refs.qualityForm) {
        this.$refs.qualityForm.resetFields();
      }
    },
    initChart() {
      if (this.$refs.trendChart) {
        this.chart = echarts.init(this.$refs.trendChart);
        this.loadTrend();
      }
    },
    async loadTrend() {
      if (!this.chart) return;
      try {
        const params = {
          indicator: this.trendIndicator,
          cageId: this.trendCageId || undefined
        };
        const res = await getTrend(params);
        // 后端返回的是 List<WaterQuality> 对象列表，需要转换为图表需要的格式
        const records = res.data || res || [];
        const isArray = Array.isArray(records);
        const dates = isArray
          ? records.map(r => r.recordTime ? r.recordTime.substring(0, 16) : '')
          : (records.dates || records.times || []);
        const values = isArray
          ? records.map(r => r[this.trendIndicator] != null ? r[this.trendIndicator] : 0)
          : (records.values || []);

        this.chart.setOption({
          tooltip: {
            trigger: 'axis',
            formatter: '{b}<br/>' + this.indicatorLabel + ': {c}'
          },
          grid: {
            left: '3%',
            right: '4%',
            bottom: '3%',
            containLabel: true
          },
          xAxis: {
            type: 'category',
            boundaryGap: false,
            data: dates
          },
          yAxis: {
            type: 'value',
            name: this.indicatorLabel
          },
          series: [
            {
              name: this.indicatorLabel,
              type: 'line',
              data: values,
              smooth: true,
              areaStyle: {
                color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                  { offset: 0, color: 'rgba(64, 158, 255, 0.3)' },
                  { offset: 1, color: 'rgba(64, 158, 255, 0.02)' }
                ])
              },
              lineStyle: { color: '#409EFF', width: 2 },
              itemStyle: { color: '#409EFF' }
            }
          ]
        });
      } catch (error) {
        console.error('获取趋势数据失败:', error);
      }
    },
    handleResize() {
      if (this.chart) {
        this.chart.resize();
      }
    }
  }
};
</script>

<style scoped>
.water-quality {
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

.table-card {
  margin-bottom: 16px;
}

.table-card >>> .el-card__body {
  padding: 16px 20px;
}

.chart-card {
  margin-bottom: 16px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-weight: 600;
}

.chart-controls {
  display: flex;
  gap: 10px;
}

.trend-chart {
  width: 100%;
  height: 350px;
}
</style>
