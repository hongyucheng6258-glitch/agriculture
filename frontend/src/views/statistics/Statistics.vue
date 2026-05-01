<template>
  <div class="statistics-page">
    <!-- 日期选择 -->
    <el-card shadow="never" class="search-card">
      <el-form :inline="true" class="search-form">
        <el-form-item label="统计日期">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="yyyy-MM-dd"
            @change="handleDateChange"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" @click="fetchData">查询统计</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 统计内容 -->
    <el-row :gutter="16">
      <!-- 投喂量统计 -->
      <el-col :span="12">
        <el-card shadow="never" class="chart-card">
          <div slot="header" class="card-header">
            <span>投喂量统计</span>
          </div>
          <div ref="feedingChart" class="chart-container" />
          <el-table :data="feedingSummary" stripe border size="small" style="margin-top: 16px;">
            <el-table-column prop="cageCode" label="网箱" min-width="100" />
            <el-table-column prop="amount" label="投喂总量(kg)" width="140" align="center">
              <template slot-scope="scope">
                {{ typeof scope.row.amount === 'number' ? scope.row.amount.toFixed(1) : scope.row.amount }}
              </template>
            </el-table-column>
            <el-table-column prop="feedCount" label="投喂次数" width="100" align="center" />
          </el-table>
        </el-card>
      </el-col>

      <!-- 病害次数统计 -->
      <el-col :span="12">
        <el-card shadow="never" class="chart-card">
          <div slot="header" class="card-header">
            <span>病害次数统计</span>
          </div>
          <div ref="diseaseChart" class="chart-container" />
          <el-table :data="diseaseSummary" stripe border size="small" style="margin-top: 16px;">
            <el-table-column prop="severity" label="严重程度" min-width="100" />
            <el-table-column prop="count" label="次数" width="100" align="center" />
            <el-table-column prop="percentage" label="占比" width="100" align="center" />
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import * as echarts from 'echarts';
import { getFeedingSummary, getDiseaseSummary } from '@/api/statistics';
import { getRecentDays } from '@/utils/date';

export default {
  name: 'Statistics',
  data() {
    const recent = getRecentDays(30);
    return {
      dateRange: [recent.start, recent.end],
      feedingSummary: [],
      diseaseSummary: [],
      feedingChartInstance: null,
      diseaseChartInstance: null
    };
  },
  mounted() {
    this.initCharts();
    this.fetchData();
    window.addEventListener('resize', this.handleResize);
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.handleResize);
    if (this.feedingChartInstance) {
      this.feedingChartInstance.dispose();
      this.feedingChartInstance = null;
    }
    if (this.diseaseChartInstance) {
      this.diseaseChartInstance.dispose();
      this.diseaseChartInstance = null;
    }
  },
  methods: {
    initCharts() {
      this.feedingChartInstance = echarts.init(this.$refs.feedingChart);
      this.diseaseChartInstance = echarts.init(this.$refs.diseaseChart);
    },
    handleResize() {
      if (this.feedingChartInstance) {
        this.feedingChartInstance.resize();
      }
      if (this.diseaseChartInstance) {
        this.diseaseChartInstance.resize();
      }
    },
    handleDateChange() {
      this.fetchData();
    },
    async fetchData() {
      const params = {
        startTime: this.dateRange ? this.dateRange[0] : undefined,
        endTime: this.dateRange ? this.dateRange[1] : undefined
      };
      try {
        const [feedingRes, diseaseRes] = await Promise.all([
          getFeedingSummary(params),
          getDiseaseSummary(params)
        ]);
        const feedingData = feedingRes.data || feedingRes || {};
        const diseaseData = diseaseRes.data || diseaseRes || {};

        // 后端返回 {byCageType: [...], byFeedType: [...], totalAmount: ...}
        this.feedingSummary = feedingData.byCageType || feedingData.records || feedingData.list || [];
        // 后端返回 {bySeverity: [...], byDiseaseName: [...], totalCount: ...}
        this.diseaseSummary = diseaseData.bySeverity || diseaseData.records || diseaseData.list || [];

        this.renderFeedingChart(this.feedingSummary);
        this.renderDiseaseChart(this.diseaseSummary);
      } catch (error) {
        console.error('获取统计数据失败:', error);
      }
    },
    renderFeedingChart(data) {
      if (!this.feedingChartInstance) return;
      const cageNames = data.map(item => item.cageCode || item.cageName || item.cageId || '未知');
      const amounts = data.map(item => item.amount || item.totalAmount || 0);

      const option = {
        tooltip: {
          trigger: 'axis',
          axisPointer: { type: 'shadow' }
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          data: cageNames,
          axisLabel: { rotate: 30 }
        },
        yAxis: {
          type: 'value',
          name: '投喂量(kg)'
        },
        series: [{
          name: '投喂量',
          type: 'bar',
          data: amounts,
          itemStyle: {
            color: '#409EFF'
          },
          barWidth: '40%'
        }]
      };
      this.feedingChartInstance.setOption(option, true);
    },
    renderDiseaseChart(data) {
      if (!this.diseaseChartInstance) return;
      const pieData = data.map(item => ({
        name: item.severity || '未知',
        value: item.count || 0
      }));

      const colorMap = {
        '轻度': '#67C23A',
        '中度': '#E6A23C',
        '重度': '#F56C6C'
      };

      const option = {
        tooltip: {
          trigger: 'item',
          formatter: '{a} <br/>{b}: {c}次 ({d}%)'
        },
        legend: {
          orient: 'vertical',
          left: 'left',
          top: 'middle'
        },
        series: [{
          name: '病害次数',
          type: 'pie',
          radius: ['40%', '70%'],
          center: ['55%', '50%'],
          avoidLabelOverlap: false,
          itemStyle: {
            borderRadius: 4,
            borderColor: '#fff',
            borderWidth: 2
          },
          label: {
            show: true,
            formatter: '{b}: {c}次'
          },
          emphasis: {
            label: {
              show: true,
              fontSize: 16,
              fontWeight: 'bold'
            }
          },
          data: pieData.map(item => ({
            ...item,
            itemStyle: { color: colorMap[item.name] || '#909399' }
          }))
        }]
      };
      this.diseaseChartInstance.setOption(option, true);
    }
  }
};
</script>

<style scoped>
.statistics-page {
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

.chart-card {
  min-height: 480px;
}

.chart-card >>> .el-card__body {
  padding: 16px 20px;
}

.card-header {
  font-size: 16px;
  font-weight: 600;
}

.chart-container {
  width: 100%;
  height: 320px;
}
</style>
