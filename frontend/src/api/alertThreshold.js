import request from './request';

// 获取告警阈值列表
export function getList(params) {
  return request({
    url: '/alert-threshold/list',
    method: 'get',
    params
  });
}

// 更新告警阈值
export function update(data) {
  return request({
    url: `/alert-threshold/${data.id}`,
    method: 'put',
    data
  });
}

// 重新检查指定阈值的预警
export function recheckThreshold(id) {
  return request({
    url: `/alert-threshold/${id}/recheck`,
    method: 'post'
  });
}

// 重新检查所有预警
export function recheckAll() {
  return request({
    url: '/alert-threshold/recheck-all',
    method: 'post'
  });
}

// 初始化默认告警阈值
export function initDefault() {
  return request({
    url: '/alert-threshold/init',
    method: 'post'
  });
}
