import request from './request';

// 获取投喂记录分页列表
export function getList(data) {
  return request({
    url: '/feeding/list',
    method: 'post',
    data
  });
}

// 根据ID获取投喂记录详情
export function getById(id) {
  return request({
    url: `/feeding/${id}`,
    method: 'get'
  });
}

// 新增投喂记录
export function add(data) {
  return request({
    url: '/feeding',
    method: 'post',
    data
  });
}

// 更新投喂记录
export function update(data) {
  return request({
    url: `/feeding/${data.id}`,
    method: 'put',
    data
  });
}

// 删除投喂记录
export function deleteRecord(id) {
  return request({
    url: `/feeding/${id}`,
    method: 'delete'
  });
}
