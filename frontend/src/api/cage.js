import request from './request';

// 获取网箱分页列表
export function getCageList(data) {
  return request({
    url: '/cage/list',
    method: 'post',
    data
  });
}

// 根据ID获取网箱详情
export function getCageById(id) {
  return request({
    url: `/cage/${id}`,
    method: 'get'
  });
}

// 获取所有网箱（不分页）
export function getCageAll() {
  return request({
    url: '/cage/all',
    method: 'get'
  });
}

// 新增网箱
export function addCage(data) {
  return request({
    url: '/cage',
    method: 'post',
    data
  });
}

// 更新网箱
export function updateCage(data) {
  return request({
    url: `/cage/${data.id}`,
    method: 'put',
    data
  });
}

// 删除网箱
export function deleteCage(id) {
  return request({
    url: `/cage/${id}`,
    method: 'delete'
  });
}
