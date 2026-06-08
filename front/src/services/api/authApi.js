import axiosClient from './axiosClient';

export const authApi = {
  login: (data) => {
    return axiosClient.post('/auth/login', data);
  },

  loginGoogle: (data) => {
    return axiosClient.post('/auth/login/google', data);
  },

  refresh: (data) => {
    return axiosClient.post('/auth/refresh', data);
  },

  logout: (data) => {
    return axiosClient.post('/auth/logout', data);
  },
};
