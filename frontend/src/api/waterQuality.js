import request from './request';

// 获取水质数据分页列表
export function getList(data) {
  return request({
    url: '/water-quality/list',
    method: 'post',
    data
  });
}

// 根据ID获取水质数据详情
export function getById(id) {
  return request({
    url: `/water-quality/${id}`,
    method: 'get'
  });
}

// 新增水质数据
export function add(data) {
  return request({
    url: '/water-quality',
    method: 'post',
    data
  });
}

// 批量新增水质数据
export function batchAdd(data) {
  return request({
    url: '/water-quality/batch',
    method: 'post',
    data
  });
}

// 更新水质数据
export function update(data) {
  return request({
    url: `/water-quality/${data.id}`,
    method: 'put',
    data
  });
}

// 删除水质数据
export function deleteRecord(id) {
  return request({
    url: `/water-quality/${id}`,
    method: 'delete'
  });
}

// 获取水质趋势数据
export function getTrend(params) {
  return request({
    url: '/water-quality/trend',
    method: 'get',
    params
  });
}
