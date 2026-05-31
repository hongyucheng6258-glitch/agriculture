import request from './request';

// 获取饲料库存列表
export function getList(params) {
  return request({
    url: '/feed-stock/list',
    method: 'get',
    params
  });
}

// 新增饲料库存记录
export function add(data) {
  return request({
    url: '/feed-stock',
    method: 'post',
    data
  });
}

// 更新饲料库存记录
export function update(data) {
  return request({
    url: `/feed-stock/${data.id}`,
    method: 'put',
    data
  });
}

// 饲料入库
export function restock(data) {
  return request({
    url: `/feed-stock/${data.id}/restock`,
    method: 'put',
    data
  });
}
