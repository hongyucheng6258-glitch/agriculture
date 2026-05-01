import Vue from 'vue';
import Vuex from 'vuex';
import { getCount } from '@/api/alert';
import { getCageAll } from '@/api/cage';

Vue.use(Vuex);

export default new Vuex.Store({
  state: {
    user: JSON.parse(localStorage.getItem('user') || '{}'),
    unhandledAlertCount: 0,
    alertList: [],
    cageList: []
  },

  mutations: {
    SET_USER(state, user) {
      state.user = user;
      localStorage.setItem('user', JSON.stringify(user));
    },

    SET_ALERT_COUNT(state, count) {
      state.unhandledAlertCount = count;
    },

    SET_ALERT_LIST(state, list) {
      state.alertList = list;
    },

    SET_CAGE_LIST(state, list) {
      state.cageList = list;
    }
  },

  actions: {
    async fetchAlertCount({ commit }) {
      try {
        const res = await getCount();
        commit('SET_ALERT_COUNT', res.data || 0);
      } catch (error) {
        console.error('获取告警数量失败:', error);
      }
    },

    async fetchCageList({ commit }) {
      try {
        const res = await getCageAll();
        commit('SET_CAGE_LIST', res.data || []);
      } catch (error) {
        console.error('获取网箱列表失败:', error);
      }
    }
  },

  getters: {
    user: state => state.user,
    unhandledAlertCount: state => state.unhandledAlertCount,
    alertList: state => state.alertList,
    cageList: state => state.cageList
  }
});
