import request from './request';

// 用户登录
export function login(data) {
  return request({
    url: '/auth/login',
    method: 'post',
    data
  });
}

// 用户登出
export function logout() {
  return request({
    url: '/auth/logout',
    method: 'post'
  });
}

// 获取当前用户信息
export function getUserInfo() {
  return request({
    url: '/auth/info',
    method: 'get'
  });
}

// 修改密码
export function changePassword(data) {
  return request({
    url: '/auth/password',
    method: 'put',
    data
  });
}

// 修改用户信息
export function updateUserInfo(data) {
  return request({
    url: '/auth/info',
    method: 'put',
    data
  });
}
