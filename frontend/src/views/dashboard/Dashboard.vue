<template>
  <div class="dashboard">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stat-row">
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover" class="stat-card stat-card-1">
          <div class="stat-content">
            <div class="stat-info">
              <div class="stat-label">总网箱数</div>
              <div class="stat-value">{{ dashboard.cageCount || 0 }}</div>
            </div>
            <div class="stat-icon-wrapper">
              <i class="el-icon-box stat-icon"></i>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover" class="stat-card stat-card-2">
          <div class="stat-content">
            <div class="stat-info">
              <div class="stat-label">在职人员</div>
              <div class="stat-value">{{ dashboard.staffCount || 0 }}</div>
            </div>
            <div class="stat-icon-wrapper">
              <i class="el-icon-user stat-icon"></i>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover" class="stat-card stat-card-3">
          <div class="stat-content">
            <div class="stat-info">
              <div class="stat-label">未处理预警</div>
              <div class="stat-value alert-value">{{ dashboard.alertCount || 0 }}</div>
            </div>
            <div class="stat-icon-wrapper">
              <i class="el-icon-warning stat-icon"></i>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover" class="stat-card stat-card-4">
          <div class="stat-content">
            <div class="stat-info">
              <div class="stat-label">饲料库存总量</div>
              <div class="stat-value">{{ dashboard.feedStock || 0 }}<span class="stat-unit">kg</span></div>
            </div>
            <div class="stat-icon-wrapper">
              <i class="el-icon-goods stat-icon"></i>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 快捷操作 -->
    <el-row :gutter="20" class="action-row">
      <el-col :span="24">
        <el-card shadow="hover" class="dark-card">
          <div slot="header" class="card-header">
            <span class="card-header-title">快捷操作</span>
          </div>
          <div class="action-buttons">
            <el-button class="tech-btn" icon="el-icon-box" @click="$router.push('/cage')">网箱管理</el-button>
            <el-button class="tech-btn" icon="el-icon-water-cup" @click="$router.push('/water-quality')">水质录入</el-button>
            <el-button class="tech-btn" icon="el-icon-food" @click="$router.push('/feeding')">投喂记录</el-button>
            <el-button class="tech-btn" icon="el-icon-bell" @click="$router.push('/alert/list')">预警查看</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="chart-row">
      <el-col :xs="24" :md="12">
        <el-card shadow="hover" class="dark-card">
          <div slot="header" class="card-header">
            <span class="card-header-title">近7天投喂量趋势</span>
          </div>
          <div ref="feedingTrendChart" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :md="12">
        <el-card shadow="hover" class="dark-card">
          <div slot="header" class="card-header">
            <span class="card-header-title">预警分布</span>
          </div>
          <div ref="alertChart" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="info-row">
      <!-- 最近预警 -->
      <el-col :xs="24" :md="14">
        <el-card shadow="hover" class="dark-card">
          <div slot="header" class="card-header">
            <span class="card-header-title">最近预警</span>
            <el-button type="text" class="card-header-link" @click="$router.push('/alert/list')">查看全部</el-button>
          </div>
          <el-table :data="recentAlerts" stripe style="width: 100%" v-loading="alertsLoading">
            <el-table-column label="预警类型" min-width="100">
              <template slot-scope="scope">
                {{ scope.row.alertType || '-' }}
              </template>
            </el-table-column>
            <el-table-column label="网箱编号" min-width="100">
              <template slot-scope="scope">
                {{ scope.row.cageCode || getCageCode(scope.row.cageId) || '系统级' }}
              </template>
            </el-table-column>
            <el-table-column label="预警内容" min-width="180" show-overflow-tooltip>
              <template slot-scope="scope">
                {{ scope.row.indicatorLabel || scope.row.alertContent || '-' }}
                {{ scope.row.currentValue != null ? '，当前值: ' + scope.row.currentValue : '' }}
                {{ scope.row.thresholdValue != null ? '，阈值: ' + scope.row.thresholdValue : '' }}
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="触发时间" min-width="160" />
            <el-table-column label="状态" width="80" align="center">
              <template slot-scope="scope">
                <el-tag :type="scope.row.isHandled ? 'success' : 'danger'" size="small">
                  {{ scope.row.isHandled ? '已处理' : '未处理' }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
          <div v-if="!alertsLoading && recentAlerts.length === 0" class="empty-text">
            暂无未处理预警
          </div>
        </el-card>
      </el-col>

      <!-- 今日投喂汇总 -->
      <el-col :xs="24" :md="10">
        <el-card shadow="hover" class="dark-card">
          <div slot="header" class="card-header">
            <span class="card-header-title">今日投喂汇总</span>
            <el-button type="text" class="card-header-link" @click="$router.push('/feeding')">查看详情</el-button>
          </div>
          <div v-loading="feedingLoading" class="feeding-summary">
            <div v-for="item in feedingSummary" :key="item.cageCode" class="feeding-item">
              <div class="feeding-cage">
                <i class="el-icon-box"></i>
                <span>{{ item.cageCode || item.cageName || '未知网箱' }}</span>
              </div>
              <div class="feeding-info">
                <span class="feeding-amount">{{ typeof item.totalAmount === 'number' ? item.totalAmount.toFixed(1) : item.totalAmount || 0 }} kg</span>
                <span class="feeding-time">{{ item.feedingTime || item.lastTime || '-' }}</span>
              </div>
            </div>
            <div v-if="!feedingLoading && feedingSummary.length === 0" class="empty-text">
              今日暂无投喂记录
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { getDashboard } from '@/api/statistics';
import { getUnhandled } from '@/api/alert';
import { mapGetters } from 'vuex';
import * as echarts from 'echarts';

export default {
  name: 'Dashboard',
  computed: {
    ...mapGetters(['cageList'])
  },
  data() {
    return {
      dashboard: {},
      recentAlerts: [],
      feedingSummary: [],
      alertsLoading: false,
      feedingLoading: false,
      feedingTrendChart: null,
      alertChart: null
    };
  },
  created() {
    this.loadDashboard();
    this.loadRecentAlerts();
  },
  mounted() {
    this.$nextTick(() => {
      this.initCharts();
    });
  },
  beforeDestroy() {
    if (this.feedingTrendChart) {
      this.feedingTrendChart.dispose();
    }
    if (this.alertChart) {
      this.alertChart.dispose();
    }
  },
  methods: {
    getCageCode(cageId) {
      if (!cageId || !this.cageList) return '';
      const cage = this.cageList.find(c => c.id === cageId);
      return cage ? cage.cageCode : '';
    },
    async loadDashboard() {
      try {
        const res = await getDashboard();
        const data = res.data || res;
        this.dashboard = {
          cageCount: data.cageCount || 0,
          staffCount: data.staffCount || 0,
          alertCount: data.alertCount || 0,
          feedStock: data.feedStock || 0
        };
        this.feedingSummary = data.feedingSummary || data.todayFeedings || [];
        
        if (this.$refs.feedingTrendChart && data.feedingTrend) {
          this.updateFeedingTrendChart(data.feedingTrend);
        }
        if (this.$refs.alertChart && data.alertStats) {
          this.updateAlertChart(data.alertStats);
        }
      } catch (error) {
        console.error('获取仪表盘数据失败:', error);
      }
    },
    async loadRecentAlerts() {
      this.alertsLoading = true;
      try {
        const res = await getUnhandled();
        const allAlerts = Array.isArray(res.data) ? res.data : 
                          (Array.isArray(res.data?.records) ? res.data.records : 
                          (Array.isArray(res.data?.list) ? res.data.list : []));
        this.recentAlerts = allAlerts.slice(0, 5);
      } catch (error) {
        console.error('获取最近预警失败:', error);
      } finally {
        this.alertsLoading = false;
      }
    },
    initCharts() {
      if (this.$refs.feedingTrendChart) {
        this.feedingTrendChart = echarts.init(this.$refs.feedingTrendChart);
      }
      if (this.$refs.alertChart) {
        this.alertChart = echarts.init(this.$refs.alertChart);
      }
      
      window.addEventListener('resize', this.handleResize);
    },
    updateFeedingTrendChart(feedingTrend) {
      if (!this.feedingTrendChart) return;
      
      const dates = feedingTrend.map(item => item.date);
      const amounts = feedingTrend.map(item => item.amount);
      
      const option = {
        tooltip: {
          trigger: 'axis'
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
          name: 'kg'
        },
        series: [
          {
            name: '投喂量',
            type: 'line',
            stack: 'Total',
            smooth: true,
            data: amounts,
            areaStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: 'rgba(255,105,180,0.3)' },
                { offset: 1, color: 'rgba(255,105,180,0.05)' }
              ])
            },
            lineStyle: {
              color: '#ff69b4',
              width: 2
            },
            itemStyle: {
              color: '#ff69b4'
            }
          }
        ]
      };
      
      this.feedingTrendChart.setOption(option);
    },
    updateAlertChart(alertStats) {
      if (!this.alertChart) return;
      
      const colors = ['#ff69b4', '#ff85c0', '#ffa8d1', '#ffcde0'];
      
      const option = {
        tooltip: {
          trigger: 'item'
        },
        legend: {
          orient: 'vertical',
          right: '10%',
          top: 'center'
        },
        series: [
          {
            name: '预警分布',
            type: 'pie',
            radius: ['40%', '70%'],
            center: ['35%', '50%'],
            avoidLabelOverlap: false,
            itemStyle: {
              borderRadius: 10,
              borderColor: '#fff',
              borderWidth: 2
            },
            label: {
              show: false,
              position: 'center'
            },
            emphasis: {
              label: {
                show: true,
                fontSize: 20,
                fontWeight: 'bold'
              }
            },
            labelLine: {
              show: false
            },
            data: alertStats.map((item, index) => ({
              name: item.name,
              value: item.value,
              itemStyle: { color: colors[index % colors.length] }
            }))
          }
        ]
      };
      
      this.alertChart.setOption(option);
    },
    handleResize() {
      if (this.feedingTrendChart) {
        this.feedingTrendChart.resize();
      }
      if (this.alertChart) {
        this.alertChart.resize();
      }
    }
  }
};
</script>

<style scoped>
.dashboard {
  padding: 0;
}

.stat-row {
  margin-bottom: 24px;
}

.stat-card {
  margin-bottom: 10px;
  background: #ffffff !important;
  border: 1px solid rgba(255, 105, 180, 0.1) !important;
  border-radius: 16px !important;
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 30px rgba(255, 105, 180, 0.15);
  border-color: rgba(255, 105, 180, 0.2) !important;
}

.stat-card >>> .el-card__body {
  padding: 24px;
  background: transparent !important;
}

.stat-card-1::before,
.stat-card-2::before,
.stat-card-3::before,
.stat-card-4::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 4px;
  background: linear-gradient(90deg, #ff69b4, #ff85c0);
}

.stat-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.stat-info {
  flex: 1;
}

.stat-label {
  font-size: 14px;
  color: #b2bec3;
  margin-bottom: 10px;
  font-weight: 500;
}

.stat-value {
  font-size: 32px;
  font-weight: 800;
  color: #2d3436;
  line-height: 1.2;
}

.stat-unit {
  font-size: 16px;
  font-weight: 500;
  color: #b2bec3;
  margin-left: 6px;
}

.alert-value {
  color: #ff4757;
}

.stat-icon-wrapper {
  width: 70px;
  height: 70px;
  border-radius: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, rgba(255, 105, 180, 0.1), rgba(255, 133, 192, 0.1));
}

.stat-icon {
  font-size: 36px;
  color: #ff69b4;
  opacity: 0.9;
}

.action-row {
  margin-bottom: 24px;
}

.dark-card {
  background: #ffffff !important;
  border: 1px solid rgba(255, 105, 180, 0.1) !important;
  border-radius: 16px !important;
  transition: all 0.3s ease;
}

.dark-card:hover {
  border-color: rgba(255, 105, 180, 0.15) !important;
  box-shadow: 0 8px 25px rgba(255, 105, 180, 0.1);
}

.dark-card >>> .el-card__header {
  background: linear-gradient(135deg, #fffafc, #ffffff) !important;
  border-bottom-color: rgba(255, 105, 180, 0.08) !important;
  padding: 18px 24px;
}

.dark-card >>> .el-card__body {
  background: transparent !important;
  padding: 24px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-weight: 600;
}

.card-header-title {
  color: #2d3436;
  font-size: 16px;
  font-weight: 700;
}

.card-header-link {
  color: #ff69b4 !important;
  font-size: 14px;
  font-weight: 600;
}

.card-header-link:hover {
  color: #ff85c0 !important;
}

.action-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 14px;
}

.tech-btn {
  background: #fffafc !important;
  border: 2px solid rgba(255, 105, 180, 0.15) !important;
  color: #ff69b4 !important;
  border-radius: 12px !important;
  padding: 14px 28px !important;
  font-size: 14px;
  font-weight: 600;
  transition: all 0.3s ease;
}

.tech-btn:hover {
  background: rgba(255, 105, 180, 0.08) !important;
  border-color: rgba(255, 105, 180, 0.25) !important;
  color: #ff69b4 !important;
  transform: translateY(-2px);
  box-shadow: 0 6px 15px rgba(255, 105, 180, 0.15);
}

.tech-btn:focus {
  background: rgba(255, 105, 180, 0.08) !important;
  border-color: rgba(255, 105, 180, 0.25) !important;
  color: #ff69b4 !important;
}

.tech-btn i {
  color: #ff69b4;
  font-size: 16px;
}

.chart-row {
  margin-bottom: 24px;
}

.chart-container {
  height: 300px;
  width: 100%;
}

.info-row {
  margin-bottom: 24px;
}

.empty-text {
  text-align: center;
  color: #b2bec3;
  padding: 40px 0;
  font-size: 14px;
  font-weight: 500;
}

.feeding-summary {
  max-height: 320px;
  overflow-y: auto;
}

.feeding-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 0;
  border-bottom: 1px solid rgba(255, 105, 180, 0.06);
  transition: all 0.3s ease;
}

.feeding-item:hover {
  background: rgba(255, 105, 180, 0.03);
  padding-left: 10px;
  padding-right: 10px;
  margin-left: -10px;
  margin-right: -10px;
  border-radius: 8px;
}

.feeding-item:last-child {
  border-bottom: none;
}

.feeding-cage {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #2d3436;
  font-size: 15px;
  font-weight: 600;
}

.feeding-cage i {
  color: #ff69b4;
  opacity: 0.8;
  font-size: 18px;
}

.feeding-info {
  display: flex;
  align-items: center;
  gap: 20px;
}

.feeding-amount {
  font-weight: 700;
  color: #ff69b4;
  font-size: 16px;
}

.feeding-time {
  color: #b2bec3;
  font-size: 13px;
  font-weight: 500;
}

.dashboard >>> .el-table {
  border-radius: 12px;
  overflow: hidden;
}

.dashboard >>> .el-table th {
  background: linear-gradient(135deg, #fffafc, #ffffff) !important;
  color: #2d3436 !important;
  font-weight: 700;
}

.dashboard >>> .el-table tr {
  background: #ffffff !important;
}

.dashboard >>> .el-table--striped .el-table__body tr.el-table__row--striped td {
  background: #fffafc !important;
}

.dashboard >>> .el-table__body tr:hover > td {
  background: rgba(255, 105, 180, 0.04) !important;
}
</style>
