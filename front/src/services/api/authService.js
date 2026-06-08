import axiosClient from './axiosClient';

export const authService = {
  /**
   * Đăng nhập bằng Google
   * @param {string} idToken 
   * @returns {Promise}
   */
  loginWithGoogle: async (idToken) => {
    return axiosClient.post('/auth/login/google', { idToken });
  },

  /**
   * Refresh token
   * @param {string} refreshToken 
   * @returns {Promise}
   */
  refreshToken: async (refreshToken) => {
    return axiosClient.post('/auth/refresh', { refreshToken });
  },

  /**
   * Đăng xuất
   * @param {string} refreshToken 
   * @returns {Promise}
   */
  logout: async (refreshToken) => {
    return axiosClient.post('/auth/logout', { refreshToken });
  },
};
