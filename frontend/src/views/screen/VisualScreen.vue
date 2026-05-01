<template>
  <div class="visual-screen">
    <!-- 标题栏 -->
    <div class="screen-header">
      <div class="header-left">
        <span class="header-decoration"></span>
      </div>
      <div class="header-title">智慧渔业数据大屏</div>
      <div class="header-right">
        <span class="current-time">{{ currentTime }}</span>
        <el-button type="text" class="back-btn" @click="$router.push('/dashboard')">
          <i class="el-icon-back"></i> 返回系统
        </el-button>
      </div>
    </div>

    <!-- 主体内容区域 -->
    <div class="screen-body">
      <!-- 上半部分 -->
      <div class="screen-row">
        <!-- 左侧：溶解氧/pH实时数据卡片 -->
        <div class="screen-col col-left">
          <div class="panel">
            <div class="panel-title">各网箱溶解氧实时数据</div>
            <div class="stat-cards">
              <div
                v-for="(item, index) in doCards"
                :key="'do-' + index"
                class="mini-card"
              >
                <div class="mini-card-label">{{ item.label }}</div>
                <div class="mini-card-value" :style="{ color: item.color }">
                  {{ item.value }}
                </div>
                <div class="mini-card-unit">mg/L</div>
              </div>
            </div>
          </div>
          <div class="panel" style="margin-top: 12px;">
            <div class="panel-title">各网箱pH实时数据</div>
            <div class="stat-cards">
              <div
                v-for="(item, index) in phCards"
                :key="'ph-' + index"
                class="mini-card"
              >
                <div class="mini-card-label">{{ item.label }}</div>
                <div class="mini-card-value" :style="{ color: item.color }">
                  {{ item.value }}
                </div>
                <div class="mini-card-unit">pH</div>
              </div>
            </div>
          </div>
        </div>

        <!-- 中间：溶解氧/pH变化趋势折线图 -->
        <div class="screen-col col-center">
          <div class="panel">
            <div class="panel-title">
              溶解氧/pH变化趋势
              <div class="toggle-group">
                <el-button
                  :type="trendDays === 7 ? 'primary' : ''"
                  size="mini"
                  class="toggle-btn"
                  @click="changeTrendDays(7)"
                >近7天</el-button>
                <el-button
                  :type="trendDays === 30 ? 'primary' : ''"
                  size="mini"
                  class="toggle-btn"
                  @click="changeTrendDays(30)"
                >近30天</el-button>
              </div>
            </div>
            <div ref="trendChart" class="chart-area"></div>
          </div>
        </div>

        <!-- 右侧：投喂量统计柱状图 -->
        <div class="screen-col col-right">
          <div class="panel">
            <div class="panel-title">投喂量统计</div>
            <div ref="feedingChart" class="chart-area"></div>
          </div>
        </div>
      </div>

      <!-- 下半部分 -->
      <div class="screen-row">
        <!-- 左侧：水温/盐度实时数据卡片 -->
        <div class="screen-col col-left">
          <div class="panel">
            <div class="panel-title">水温/盐度实时数据</div>
            <div class="stat-cards">
              <div
                v-for="(item, index) in envCards"
                :key="'env-' + index"
                class="mini-card"
              >
                <div class="mini-card-label">{{ item.label }}</div>
                <div class="mini-card-value" :style="{ color: item.color }">
                  {{ item.value }}
                </div>
                <div class="mini-card-unit">{{ item.unit }}</div>
              </div>
            </div>
          </div>
        </div>

        <!-- 中间：各网箱pH值对比柱状图 -->
        <div class="screen-col col-center">
          <div class="panel">
            <div class="panel-title">各网箱pH值对比</div>
            <div ref="phCompareChart" class="chart-area"></div>
          </div>
        </div>

        <!-- 右侧：病害发生次数统计饼图 -->
        <div class="screen-col col-right">
          <div class="panel">
            <div class="panel-title">病害发生次数统计</div>
            <div ref="diseaseChart" class="chart-area"></div>
          </div>
        </div>
      </div>
    </div>

    <!-- 底部预警信息滚动条 -->
    <div class="screen-footer">
      <div class="alert-bar">
        <span class="alert-bar-icon">
          <i class="el-icon-warning"></i> 预警信息
        </span>
        <div class="alert-scroll-wrapper">
          <div class="alert-scroll-content" :class="{ 'scroll-animate': alertList.length > 0 }">
            <span
              v-for="(alert, index) in alertList"
              :key="index"
              class="alert-item"
            >
              【{{ alert.alertType || '预警' }}】{{ alert.indicatorLabel || alert.alertContent || alert.cageCode || '暂无预警信息' }}
              <span v-if="alert.currentValue != null" style="color:#ff4757;margin-left:4px;">
                当前值: {{ alert.currentValue }}{{ alert.thresholdValue != null ? '，阈值: ' + alert.thresholdValue : '' }}
              </span>
              <span class="alert-sep">|</span>
            </span>
            <span v-if="alertList.length === 0" class="alert-item">暂无预警信息</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import * as echarts from 'echarts';
import { getDashboard, getFeedingSummary, getDiseaseSummary } from '@/api/statistics';
import { getTrend } from '@/api/waterQuality';
import { getUnhandled } from '@/api/alert';
import { formatDate } from '@/utils/date';

export default {
  name: 'VisualScreen',
  data() {
    return {
      currentTime: '',
      timeTimer: null,
      refreshTimer: null,
      trendDays: 7,
      dashboardData: {},
      feedingSummary: [],
      diseaseSummary: [],
      waterTrend: [],
      alertList: [],
      doCards: [],
      phCards: [],
      envCards: [],
      trendChartInstance: null,
      feedingChartInstance: null,
      phCompareChartInstance: null,
      diseaseChartInstance: null
    };
  },
  mounted() {
    this.updateTime();
    this.timeTimer = setInterval(this.updateTime, 1000);
    this.initCharts();
    this.fetchAllData();
    this.refreshTimer = setInterval(this.fetchAllData, 60000);
    window.addEventListener('resize', this.handleResize);
  },
  beforeDestroy() {
    if (this.timeTimer) clearInterval(this.timeTimer);
    if (this.refreshTimer) clearInterval(this.refreshTimer);
    window.removeEventListener('resize', this.handleResize);
    this.disposeCharts();
  },
  methods: {
    updateTime() {
      this.currentTime = formatDate(new Date(), 'YYYY-MM-DD HH:mm:ss');
    },
    initCharts() {
      this.trendChartInstance = echarts.init(this.$refs.trendChart);
      this.feedingChartInstance = echarts.init(this.$refs.feedingChart);
      this.phCompareChartInstance = echarts.init(this.$refs.phCompareChart);
      this.diseaseChartInstance = echarts.init(this.$refs.diseaseChart);
    },
    disposeCharts() {
      if (this.trendChartInstance) { this.trendChartInstance.dispose(); this.trendChartInstance = null; }
      if (this.feedingChartInstance) { this.feedingChartInstance.dispose(); this.feedingChartInstance = null; }
      if (this.phCompareChartInstance) { this.phCompareChartInstance.dispose(); this.phCompareChartInstance = null; }
      if (this.diseaseChartInstance) { this.diseaseChartInstance.dispose(); this.diseaseChartInstance = null; }
    },
    handleResize() {
      if (this.trendChartInstance) this.trendChartInstance.resize();
      if (this.feedingChartInstance) this.feedingChartInstance.resize();
      if (this.phCompareChartInstance) this.phCompareChartInstance.resize();
      if (this.diseaseChartInstance) this.diseaseChartInstance.resize();
    },
    changeTrendDays(days) {
      this.trendDays = days;
      this.fetchWaterTrend();
    },
    async fetchAllData() {
      await Promise.all([
        this.fetchDashboard(),
        this.fetchFeedingSummary(),
        this.fetchDiseaseSummary(),
        this.fetchWaterTrend(),
        this.fetchAlerts()
      ]);
    },
    async fetchDashboard() {
      try {
        const res = await getDashboard();
        const data = res.data || res;
        this.dashboardData = data;
        const cages = data.cages || data.cageList || [];
        if (cages.length > 0) {
          this.doCards = cages.map(c => ({
            label: c.cageCode || c.name || '网箱',
            value: c.dissolvedOxygen != null ? c.dissolvedOxygen.toFixed(1) : '--',
            color: this.getDoColor(c.dissolvedOxygen)
          }));
          this.phCards = cages.map(c => ({
            label: c.cageCode || c.name || '网箱',
            value: c.ph != null ? c.ph.toFixed(1) : '--',
            color: this.getPhColor(c.ph)
          }));
        } else {
          this.doCards = [
            { label: '溶解氧', value: data.avgDo != null ? data.avgDo.toFixed(1) : '--', color: '#ff69b4' },
            { label: '溶解氧', value: data.maxDo != null ? data.maxDo.toFixed(1) : '--', color: '#ff85c0' },
            { label: '溶解氧', value: data.minDo != null ? data.minDo.toFixed(1) : '--', color: '#ffa8d1' },
            { label: '溶解氧', value: data.avgDo != null ? data.avgDo.toFixed(1) : '--', color: '#ffcde0' }
          ];
          this.phCards = [
            { label: 'pH均值', value: data.avgPh != null ? data.avgPh.toFixed(1) : '--', color: '#ff69b4' },
            { label: 'pH最高', value: data.maxPh != null ? data.maxPh.toFixed(1) : '--', color: '#ff85c0' },
            { label: 'pH最低', value: data.minPh != null ? data.minPh.toFixed(1) : '--', color: '#ffa8d1' },
            { label: 'pH均值', value: data.avgPh != null ? data.avgPh.toFixed(1) : '--', color: '#ffcde0' }
          ];
        }
        this.envCards = [
          { label: '气温', value: data.temperature != null ? data.temperature.toFixed(1) : '--', unit: '°C', color: '#ff69b4' },
          { label: '风速', value: data.windSpeed != null ? data.windSpeed.toFixed(1) : '--', unit: 'm/s', color: '#ff85c0' },
          { label: '天气', value: data.weather || '晴', unit: '', color: '#ffa8d1' },
          { label: '总网箱', value: data.cageCount || 0, unit: '个', color: '#ffcde0' }
        ];
        this.renderPhCompareChart(); // 渲染pH对比图表
      } catch (error) {
        console.error('获取仪表盘数据失败:', error);
      }
    },
    async fetchFeedingSummary() {
      try {
        const res = await getFeedingSummary({});
        const data = res.data || res;
        this.feedingSummary = Array.isArray(data) ? data : (data.byCageType || data.records || data.list || []);
        this.renderFeedingChart();
      } catch (error) {
        console.error('获取投喂统计失败:', error);
      }
    },
    async fetchDiseaseSummary() {
      try {
        const res = await getDiseaseSummary({});
        const data = res.data || res;
        this.diseaseSummary = Array.isArray(data) ? data : (data.bySeverity || data.records || data.list || []);
        this.renderDiseaseChart();
      } catch (error) {
        console.error('获取病害统计失败:', error);
      }
    },
    async fetchWaterTrend() {
      try {
        const res = await getTrend({ days: this.trendDays });
        const data = res.data || res;
        this.waterTrend = data.records || data.list || data || [];
        this.renderTrendChart();
      } catch (error) {
        console.error('获取水质趋势失败:', error);
      }
    },
    async fetchAlerts() {
      try {
        const res = await getUnhandled({});
        const data = res.data || res;
        this.alertList = Array.isArray(data) ? data : (data.records || data.list || []);
      } catch (error) {
        console.error('获取预警信息失败:', error);
      }
    },
    getDoColor(val) {
      if (val == null) return '#909399';
      if (val < 4) return '#ff4757';
      if (val < 6) return '#ffa502';
      return '#ff69b4';
    },
    getPhColor(val) {
      if (val == null) return '#909399';
      if (val < 6.5 || val > 9) return '#ff4757';
      if (val < 7 || val > 8.5) return '#ffa502';
      return '#ff69b4';
    },
    renderTrendChart() {
      if (!this.trendChartInstance || !Array.isArray(this.waterTrend)) return;
      const dates = this.waterTrend.map(item => item.recordTime || item.date || item.createTime || '');
      const doValues = this.waterTrend.map(item => item.dissolvedOxygen != null ? item.dissolvedOxygen : null);
      const phValues = this.waterTrend.map(item => item.ph != null ? item.ph : null);

      const option = {
        tooltip: {
          trigger: 'axis',
          axisPointer: { type: 'cross' },
          backgroundColor: 'rgba(102, 126, 234, 0.95)',
          borderColor: '#a78bfa',
          textStyle: { color: '#ffffff', fontWeight: 600 }
        },
        legend: {
          data: ['溶解氧', 'pH'],
          textStyle: { color: '#667eea', fontWeight: 600 },
          top: 5
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          top: 40,
          containLabel: true
        },
        xAxis: {
          type: 'category',
          data: dates,
          axisLine: { lineStyle: { color: 'rgba(102, 126, 234, 0.35)' } },
          axisLabel: { color: '#667eea', fontSize: 12, fontWeight: 600 }
        },
        yAxis: [
          {
            type: 'value',
            name: '溶解氧(mg/L)',
            nameTextStyle: { color: '#667eea', fontWeight: 600 },
            axisLine: { lineStyle: { color: 'rgba(102, 126, 234, 0.35)' } },
            splitLine: { lineStyle: { color: 'rgba(102, 126, 234, 0.15)' } },
            axisLabel: { color: '#667eea', fontWeight: 600 }
          },
          {
            type: 'value',
            name: 'pH',
            nameTextStyle: { color: '#a78bfa', fontWeight: 600 },
            axisLine: { lineStyle: { color: 'rgba(102, 126, 234, 0.35)' } },
            splitLine: { show: false },
            axisLabel: { color: '#a78bfa', fontWeight: 600 }
          }
        ],
        series: [
          {
            name: '溶解氧',
            type: 'line',
            yAxisIndex: 0,
            data: doValues,
            smooth: true,
            symbol: 'circle',
            symbolSize: 8,
            lineStyle: { color: '#667eea', width: 3 },
            itemStyle: { color: '#667eea', borderColor: '#ffffff', borderWidth: 2 },
            areaStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: 'rgba(102, 126, 234, 0.35)' },
                { offset: 1, color: 'rgba(102, 126, 234, 0.02)' }
              ])
            },
            emphasis: {
              itemStyle: {
                shadowBlur: 15,
                shadowColor: 'rgba(102, 126, 234, 0.4)'
              }
            }
          },
          {
            name: 'pH',
            type: 'line',
            yAxisIndex: 1,
            data: phValues,
            smooth: true,
            symbol: 'diamond',
            symbolSize: 8,
            lineStyle: { color: '#a78bfa', width: 3 },
            itemStyle: { color: '#a78bfa', borderColor: '#ffffff', borderWidth: 2 },
            areaStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: 'rgba(167, 139, 250, 0.35)' },
                { offset: 1, color: 'rgba(167, 139, 250, 0.02)' }
              ])
            },
            emphasis: {
              itemStyle: {
                shadowBlur: 15,
                shadowColor: 'rgba(167, 139, 250, 0.4)'
              }
            }
          }
        ]
      };
      this.trendChartInstance.setOption(option, true);
    },
    renderFeedingChart() {
      if (!this.feedingChartInstance) return;
      const names = this.feedingSummary.map(item => item.cageName || item.cageCode || '未知');
      const amounts = this.feedingSummary.map(item => item.amount || item.totalAmount || 0);

      const option = {
        tooltip: {
          trigger: 'axis',
          axisPointer: { type: 'shadow' },
          backgroundColor: 'rgba(16, 185, 129, 0.95)',
          borderColor: '#34d399',
          textStyle: { color: '#ffffff', fontWeight: 600 }
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          top: 20,
          containLabel: true
        },
        xAxis: {
          type: 'category',
          data: names,
          axisLine: { lineStyle: { color: 'rgba(16, 185, 129, 0.35)' } },
          axisLabel: { color: '#10b981', fontSize: 12, fontWeight: 600, rotate: names.length > 6 ? 30 : 0 }
        },
        yAxis: {
          type: 'value',
          name: '投喂量(kg)',
          nameTextStyle: { color: '#10b981', fontWeight: 600 },
          axisLine: { lineStyle: { color: 'rgba(16, 185, 129, 0.35)' } },
          splitLine: { lineStyle: { color: 'rgba(16, 185, 129, 0.15)' } },
          axisLabel: { color: '#10b981', fontWeight: 600 }
        },
        series: [{
          name: '投喂量',
          type: 'bar',
          data: amounts,
          barWidth: '45%',
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: '#10b981' },
              { offset: 1, color: '#34d399' }
            ]),
            borderRadius: [10, 10, 0, 0]
          },
          emphasis: {
            itemStyle: {
              shadowBlur: 20,
              shadowColor: 'rgba(0, 0, 0, 0.15)'
            }
          }
        }]
      };
      this.feedingChartInstance.setOption(option, true);
    },
    renderPhCompareChart() {
      if (!this.phCompareChartInstance) return;
      const phData = this.phCards.length > 0 ? this.phCards : [];
      const names = phData.map(item => item.label);
      const values = phData.map(item => parseFloat(item.value) || 0);

      const option = {
        tooltip: {
          trigger: 'axis',
          axisPointer: { type: 'shadow' },
          backgroundColor: 'rgba(102, 126, 234, 0.95)',
          borderColor: '#a78bfa',
          textStyle: { color: '#ffffff', fontWeight: 600 },
          formatter: function(params) {
            return params[0].name + '<br/>pH值: ' + params[0].value;
          }
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          top: 20,
          containLabel: true
        },
        xAxis: {
          type: 'category',
          data: names,
          axisLine: { lineStyle: { color: 'rgba(102, 126, 234, 0.35)' } },
          axisLabel: { color: '#667eea', fontSize: 12, fontWeight: 600 }
        },
        yAxis: {
          type: 'value',
          name: 'pH值',
          nameTextStyle: { color: '#667eea', fontWeight: 600 },
          min: 6,
          max: 10,
          axisLine: { lineStyle: { color: 'rgba(102, 126, 234, 0.35)' } },
          splitLine: { lineStyle: { color: 'rgba(102, 126, 234, 0.15)' } },
          axisLabel: { color: '#667eea', fontWeight: 600 }
        },
        series: [{
          name: 'pH值',
          type: 'bar',
          data: values.map((v, i) => ({
            value: v,
            itemStyle: {
              color: (() => {
                const colors = [
                  { type: 'linear', x: 0, y: 0, x2: 0, y2: 1, colorStops: [
                    { offset: 0, color: '#ff69b4' },
                    { offset: 1, color: '#ff85c0' }
                  ]},
                  { type: 'linear', x: 0, y: 0, x2: 0, y2: 1, colorStops: [
                    { offset: 0, color: '#a78bfa' },
                    { offset: 1, color: '#c4b5fd' }
                  ]},
                  { type: 'linear', x: 0, y: 0, x2: 0, y2: 1, colorStops: [
                    { offset: 0, color: '#667eea' },
                    { offset: 1, color: '#818cf8' }
                  ]},
                  { type: 'linear', x: 0, y: 0, x2: 0, y2: 1, colorStops: [
                    { offset: 0, color: '#10b981' },
                    { offset: 1, color: '#34d399' }
                  ]},
                  { type: 'linear', x: 0, y: 0, x2: 0, y2: 1, colorStops: [
                    { offset: 0, color: '#f59e0b' },
                    { offset: 1, color: '#fbbf24' }
                  ]},
                  { type: 'linear', x: 0, y: 0, x2: 0, y2: 1, colorStops: [
                    { offset: 0, color: '#fb7185' },
                    { offset: 1, color: '#fca5a5' }
                  ]}
                ];
                return colors[i % colors.length];
              })()
            }
          })),
          barWidth: '45%',
          itemStyle: { borderRadius: [10, 10, 0, 0] },
          label: {
            show: true,
            position: 'top',
            color: '#667eea',
            fontSize: 13,
            fontWeight: 700
          },
          emphasis: {
            itemStyle: {
              shadowBlur: 20,
              shadowColor: 'rgba(0, 0, 0, 0.15)'
            }
          }
        }]
      };
      this.phCompareChartInstance.setOption(option, true);
    },
    renderDiseaseChart() {
      if (!this.diseaseChartInstance) return;
      const colorMap = {
        '轻度': '#fb7185',
        '中度': '#f59e0b',
        '重度': '#ef4444'
      };
      const pieData = this.diseaseSummary.map(item => ({
        name: item.severity || '未知',
        value: item.count || 0,
        itemStyle: {
          color: colorMap[item.severity] || '#a78bfa'
        }
      }));

      const option = {
        tooltip: {
          trigger: 'item',
          formatter: '{a} <br/>{b}: {c}次 ({d}%)',
          backgroundColor: 'rgba(245, 158, 11, 0.95)',
          borderColor: '#fbbf24',
          textStyle: { color: '#ffffff', fontWeight: 600 }
        },
        legend: {
          orient: 'vertical',
          left: 'left',
          top: 'middle',
          textStyle: { color: '#f59e0b', fontWeight: 600 }
        },
        series: [{
          name: '病害次数',
          type: 'pie',
          radius: ['40%', '70%'],
          center: ['55%', '50%'],
          avoidLabelOverlap: false,
          itemStyle: {
            borderRadius: 10,
            borderColor: '#fffbeb',
            borderWidth: 3
          },
          label: {
            show: true,
            formatter: '{b}: {c}次',
            color: '#f59e0b',
            fontWeight: 600
          },
          emphasis: {
            label: {
              show: true,
              fontSize: 16,
              fontWeight: 'bold'
            },
            itemStyle: {
              shadowBlur: 20,
              shadowColor: 'rgba(0, 0, 0, 0.15)'
            }
          },
          data: pieData
        }]
      };
      this.diseaseChartInstance.setOption(option, true);
    }
  }
};
</script>

<style scoped>
.visual-screen {
  width: 100vw;
  height: 100vh;
  background: linear-gradient(135deg, #fff0f5 0%, #ffe4ec 25%, #f0f9ff 50%, #fff0fb 75%, #fef0f4 100%);
  color: #2d3436;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  font-family: 'Microsoft YaHei', 'PingFang SC', sans-serif;
  position: relative;
}

.visual-screen::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image: radial-gradient(circle at 20% 20%, rgba(255, 105, 180, 0.06) 0%, transparent 50%),
    radial-gradient(circle at 80% 80%, rgba(102, 126, 234, 0.05) 0%, transparent 50%),
    radial-gradient(circle at 40% 80%, rgba(16, 185, 129, 0.04) 0%, transparent 50%);
  pointer-events: none;
}

.screen-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 64px;
  padding: 0 32px;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.85) 0%, rgba(255, 255, 255, 0.6) 100%);
  backdrop-filter: blur(12px);
  border-bottom: 2px solid rgba(255, 105, 180, 0.25);
  box-shadow: 0 2px 20px rgba(255, 105, 180, 0.08);
  flex-shrink: 0;
  position: relative;
  z-index: 10;
}

.screen-header::before {
  content: '';
  position: absolute;
  top: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 240px;
  height: 4px;
  background: linear-gradient(90deg, transparent, #ff69b4, #ff85c0, #a78bfa, transparent);
  border-radius: 0 0 4px 4px;
}

.header-left {
  flex: 1;
  display: flex;
  align-items: center;
}

.header-decoration {
  display: inline-block;
  width: 160px;
  height: 4px;
  background: linear-gradient(90deg, transparent, #ff69b4, #ff85c0, transparent);
  border-radius: 2px;
}

.header-title {
  font-size: 30px;
  font-weight: 900;
  background: linear-gradient(135deg, #ff69b4, #ff85c0, #a78bfa);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: 8px;
  text-shadow: 0 4px 20px rgba(255, 105, 180, 0.2);
}

.header-right {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 24px;
}

.current-time {
  font-size: 15px;
  color: #a78bfa;
  font-family: 'Courier New', monospace;
  font-weight: 700;
  padding: 6px 16px;
  background: rgba(167, 139, 250, 0.1);
  border-radius: 10px;
  border: 1px solid rgba(167, 139, 250, 0.2);
}

.back-btn {
  color: #667eea;
  font-size: 14px;
  font-weight: 700;
  padding: 8px 20px;
  background: rgba(102, 126, 234, 0.08);
  border-radius: 10px;
  border: 1px solid rgba(102, 126, 234, 0.2);
  transition: all 0.3s ease;
}

.back-btn:hover {
  background: rgba(102, 126, 234, 0.15);
  color: #667eea;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.15);
}

.screen-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 16px;
  gap: 16px;
  overflow: hidden;
  position: relative;
  z-index: 5;
}

.screen-row {
  flex: 1;
  display: grid;
  grid-template-columns: 1fr 2fr 1fr;
  gap: 16px;
  min-height: 0;
}

.screen-col {
  display: flex;
  flex-direction: column;
  min-height: 0;
  overflow: hidden;
}

/* 左侧列 - 粉色系 */
.screen-col.col-left:first-child .panel:first-child {
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.95) 0%, rgba(255, 240, 245, 0.85) 100%);
  border: 2px solid rgba(255, 105, 180, 0.25);
  box-shadow: 0 8px 30px rgba(255, 105, 180, 0.12);
}

.screen-col.col-left:first-child .panel:last-child {
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.95) 0%, rgba(255, 232, 255, 0.85) 100%);
  border: 2px solid rgba(167, 139, 250, 0.25);
  box-shadow: 0 8px 30px rgba(167, 139, 250, 0.12);
}

.screen-col.col-left:first-child .panel:last-child .panel-title {
  color: #a78bfa;
  border-bottom-color: rgba(167, 139, 250, 0.2);
}

.screen-col.col-left:first-child .panel:last-child .mini-card {
  background: linear-gradient(135deg, rgba(167, 139, 250, 0.1), rgba(196, 181, 253, 0.06));
  border-color: rgba(167, 139, 250, 0.2);
}

.screen-col.col-left:first-child .panel:last-child .mini-card-label {
  color: #a78bfa;
}

.screen-col.col-left:first-child .panel:last-child .mini-card-unit {
  color: rgba(167, 139, 250, 0.7);
}

/* 中间列 - 蓝紫色系 */
.screen-col.col-center .panel {
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.95) 0%, rgba(240, 249, 255, 0.85) 100%);
  border: 2px solid rgba(102, 126, 234, 0.22);
  box-shadow: 0 8px 30px rgba(102, 126, 234, 0.1);
}

.screen-col.col-center .panel-title {
  color: #667eea;
  border-bottom-color: rgba(102, 126, 234, 0.18);
}

/* 右侧列 - 青绿色系 */
.screen-col.col-right .panel:first-child {
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.95) 0%, rgba(240, 253, 250, 0.85) 100%);
  border: 2px solid rgba(16, 185, 129, 0.22);
  box-shadow: 0 8px 30px rgba(16, 185, 129, 0.1);
}

.screen-col.col-right .panel:first-child .panel-title {
  color: #10b981;
  border-bottom-color: rgba(16, 185, 129, 0.18);
}

.screen-col.col-right .panel:last-child {
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.95) 0%, rgba(255, 251, 235, 0.85) 100%);
  border: 2px solid rgba(251, 191, 36, 0.22);
  box-shadow: 0 8px 30px rgba(251, 191, 36, 0.1);
}

.screen-col.col-right .panel:last-child .panel-title {
  color: #f59e0b;
  border-bottom-color: rgba(251, 191, 36, 0.18);
}

.panel {
  flex: 1;
  backdrop-filter: blur(12px);
  border-radius: 20px;
  padding: 18px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  transition: all 0.4s ease;
}

.panel:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.08);
}

.panel-title {
  font-size: 16px;
  font-weight: 800;
  color: #ff69b4;
  margin-bottom: 14px;
  padding-bottom: 10px;
  border-bottom: 2px solid rgba(255, 105, 180, 0.15);
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-shrink: 0;
  letter-spacing: 1px;
}

.stat-cards {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  flex: 1;
  overflow-y: auto;
}

.mini-card {
  background: linear-gradient(135deg, rgba(255, 105, 180, 0.1), rgba(255, 133, 192, 0.06));
  border: 2px solid rgba(255, 105, 180, 0.18);
  border-radius: 16px;
  padding: 14px 10px;
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  transition: all 0.35s ease;
  position: relative;
  overflow: hidden;
}

.mini-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.4), transparent);
  transition: left 0.6s ease;
}

.mini-card:hover::before {
  left: 100%;
}

.mini-card:hover {
  transform: translateY(-3px) scale(1.02);
  box-shadow: 0 8px 25px rgba(255, 105, 180, 0.2);
}

.mini-card-label {
  font-size: 13px;
  color: #ff85c0;
  margin-bottom: 6px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  font-weight: 700;
}

.mini-card-value {
  font-size: 24px;
  font-weight: 900;
  font-family: 'Courier New', monospace;
}

.mini-card-unit {
  font-size: 12px;
  color: rgba(255, 133, 192, 0.7);
  margin-top: 3px;
  font-weight: 600;
}

.toggle-group {
  display: flex;
  gap: 8px;
}

.toggle-btn {
  background: rgba(102, 126, 234, 0.1) !important;
  border-color: rgba(102, 126, 234, 0.3) !important;
  color: #667eea !important;
  padding: 6px 16px !important;
  font-size: 13px !important;
  border-radius: 10px !important;
  font-weight: 700 !important;
  transition: all 0.3s ease !important;
}

.toggle-btn:hover {
  background: rgba(102, 126, 234, 0.2) !important;
  border-color: rgba(102, 126, 234, 0.4) !important;
  transform: translateY(-1px);
}

.toggle-btn.is-primary {
  background: linear-gradient(135deg, #667eea, #a78bfa) !important;
  border-color: transparent !important;
  color: #ffffff !important;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.35);
}

.chart-area {
  flex: 1;
  min-height: 0;
}

.screen-footer {
  flex-shrink: 0;
  height: 44px;
  background: linear-gradient(90deg, rgba(255, 105, 180, 0.08), rgba(167, 139, 250, 0.06), rgba(102, 126, 234, 0.08));
  border-top: 2px solid rgba(255, 105, 180, 0.2);
  display: flex;
  align-items: center;
  position: relative;
  z-index: 10;
}

.alert-bar {
  display: flex;
  align-items: center;
  width: 100%;
  height: 100%;
  padding: 0 20px;
  gap: 20px;
}

.alert-bar-icon {
  flex-shrink: 0;
  color: #ff69b4;
  font-size: 14px;
  font-weight: 800;
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 4px 12px;
  background: rgba(255, 105, 180, 0.1);
  border-radius: 8px;
  border: 1px solid rgba(255, 105, 180, 0.2);
}

.alert-scroll-wrapper {
  flex: 1;
  overflow: hidden;
  height: 100%;
  display: flex;
  align-items: center;
}

.alert-scroll-content {
  display: flex;
  white-space: nowrap;
  animation: scrollAlert 30s linear infinite;
}

.alert-scroll-content.scroll-animate:hover {
  animation-play-state: paused;
}

.alert-item {
  font-size: 14px;
  color: #ff69b4;
  margin-right: 16px;
  font-weight: 700;
}

.alert-sep {
  color: rgba(167, 139, 250, 0.5);
  margin-left: 16px;
}

@keyframes scrollAlert {
  0% {
    transform: translateX(100%);
  }
  100% {
    transform: translateX(-100%);
  }
}
</style>
