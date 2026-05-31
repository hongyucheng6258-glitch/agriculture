import request from './request';

// 获取仪表盘统计数据
export function getDashboard() {
  return request({
    url: '/statistics/dashboard',
    method: 'get'
  });
}

// 获取投喂汇总统计
export function getFeedingSummary(params) {
  return request({
    url: '/statistics/feeding-summary',
    method: 'get',
    params
  });
}

// 获取病害汇总统计
export function getDiseaseSummary(params) {
  return request({
    url: '/statistics/disease-summary',
    method: 'get',
    params
  });
}
