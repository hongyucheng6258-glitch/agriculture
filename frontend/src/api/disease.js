import request from './request';

// 获取病害记录分页列表
export function getList(data) {
  return request({
    url: '/disease/list',
    method: 'post',
    data
  });
}

// 根据ID获取病害记录详情
export function getById(id) {
  return request({
    url: `/disease/${id}`,
    method: 'get'
  });
}

// 新增病害记录
export function add(data) {
  return request({
    url: '/disease',
    method: 'post',
    data
  });
}

// 更新病害记录
export function update(data) {
  return request({
    url: `/disease/${data.id}`,
    method: 'put',
    data
  });
}

// 处理病害
export function handleDisease(data) {
  return request({
    url: `/disease/${data.id}/handle`,
    method: 'put',
    data
  });
}
