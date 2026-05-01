import axios from 'axios';
import qs from 'qs';
import { Message } from 'element-ui';
import router from '@/router';

const service = axios.create({
  baseURL: '/api',
  timeout: 60000,
  paramsSerializer: function(params) {
    return qs.stringify(params, { skipNulls: true });
  }
});

// 请求拦截器
service.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token');
    if (token && token !== 'undefined' && token !== 'null') {
      config.headers['Authorization'] = 'Bearer ' + token;
    }
    return config;
  },
  error => {
    console.error('请求错误:', error);
    return Promise.reject(error);
  }
);

// 响应拦截器
service.interceptors.response.use(
  response => {
    const res = response.data;

    // 如果返回的状态码不是200，说明接口有问题
    if (res.code && res.code !== 200) {
      Message({
        message: res.message || '请求失败',
        type: 'error',
        duration: 3000
      });

      // 401: Token 过期或未登录
      if (res.code === 401) {
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        router.push('/login');
        Message({
          message: '登录已过期，请重新登录',
          type: 'warning',
          duration: 3000
        });
      }

      return Promise.reject(new Error(res.message || '请求失败'));
    }

    return res;
  },
  error => {
    console.error('响应错误:', error);

    if (error.response) {
      const status = error.response.status;

      switch (status) {
        case 401:
          localStorage.removeItem('token');
          localStorage.removeItem('user');
          router.push('/login');
          Message({
            message: '登录已过期，请重新登录',
            type: 'warning',
            duration: 3000
          });
          break;
        case 403:
          Message({
            message: '没有权限访问该资源',
            type: 'error',
            duration: 3000
          });
          break;
        case 404:
          Message({
            message: '请求的资源不存在',
            type: 'error',
            duration: 3000
          });
          break;
        case 500:
          Message({
            message: '服务器内部错误',
            type: 'error',
            duration: 3000
          });
          break;
        default:
          Message({
            message: error.response.data?.message || '请求失败',
            type: 'error',
            duration: 3000
          });
      }
    } else if (error.message.includes('timeout')) {
      Message({
        message: '请求超时，请稍后重试',
        type: 'error',
        duration: 3000
      });
    } else {
      Message({
        message: '网络连接异常，请检查网络',
        type: 'error',
        duration: 3000
      });
    }

    return Promise.reject(error);
  }
);

export default service;
