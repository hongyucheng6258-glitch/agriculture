import request from '@/api/request';

export function getUserList(params) {
  return request({
    url: '/users',
    method: 'get',
    params
  });
}

export function getUser(id) {
  return request({
    url: `/users/${id}`,
    method: 'get'
  });
}

export function addUser(data) {
  return request({
    url: '/users',
    method: 'post',
    data
  });
}

export function updateUser(id, data) {
  return request({
    url: `/users/${id}`,
    method: 'put',
    data
  });
}

export function deleteUser(id) {
  return request({
    url: `/users/${id}`,
    method: 'delete'
  });
}

export function toggleUserEnabled(id) {
  return request({
    url: `/users/${id}/toggle`,
    method: 'put'
  });
}

export function resetUserPassword(id, newPassword) {
  return request({
    url: `/users/${id}/password`,
    method: 'put',
    data: { newPassword }
  });
}
