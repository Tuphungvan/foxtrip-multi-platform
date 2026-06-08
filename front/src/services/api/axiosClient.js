import axios from 'axios';
import { useAuthStore } from '../../store/useAuthStore';
import { normalizeApiError } from './normalizeApiError';
import { clearChatCache } from '../../utils/clearChatCache';

// NOTE: `localhost` chi dung khi chay tren chinh may dev.
// Neu mo web tren dien thoai trong cung mang LAN, hay doi sang IPv4 may tinh, VD:
// http://192.168.1.10:8080/api
// Khuyen nghi: VITE_API_BASE_URL khong co dau "/" cuoi URL.
const LOCAL_API_FALLBACK = '/api';
const normalizeBaseUrl = (url) => String(url || '').replace(/\/+$/, '');
const API_BASE_URL = normalizeBaseUrl(import.meta.env.VITE_API_BASE_URL || LOCAL_API_FALLBACK);

const axiosClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor
axiosClient.interceptors.request.use(
  (config) => {
    const token = useAuthStore.getState().accessToken;
    const isAuthPostRequest = config.url && config.url.includes('/auth') && config.method?.toUpperCase() === 'POST';

    if (token && !isAuthPostRequest) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Mảng chứa các request bị hoãn lại khi đang refresh token
let isRefreshing = false;
let failedQueue = [];

const processQueue = (error, token = null) => {
  failedQueue.forEach((prom) => {
    if (error) {
      prom.reject(error);
    } else {
      prom.resolve(token);
    }
  });

  failedQueue = [];
};

const redirectToLogin = () => {
  if (typeof window === 'undefined') {
    return;
  }

  if (window.location.pathname !== '/auth') {
    window.location.assign('/auth');
  }
};

// Response interceptor
axiosClient.interceptors.response.use(
  (response) => {
    return response.data;
  },
  async (error) => {
    const originalRequest = error?.config;
    if (!originalRequest) {
      return Promise.reject(normalizeApiError(error));
    }

    const requestUrl = String(originalRequest.url || '');

    // Tránh gọi refresh vòng lặp vô hạn nếu chính request refresh bị lỗi
    if (requestUrl.includes('/auth/refresh')) {
      useAuthStore.getState().clearAuth();
      clearChatCache();
      redirectToLogin();
      return Promise.reject(normalizeApiError(error));
    }

    if (error.response?.status === 401 && !originalRequest._retry) {
      if (isRefreshing) {
        return new Promise(function (resolve, reject) {
          failedQueue.push({ resolve, reject });
        })
          .then((token) => {
            originalRequest.headers = originalRequest.headers || {};
            originalRequest.headers.Authorization = `Bearer ${token}`;
            return axiosClient(originalRequest);
          })
          .catch((err) => {
            return Promise.reject(normalizeApiError(err));
          });
      }

      originalRequest._retry = true;
      isRefreshing = true;

      const refreshToken = useAuthStore.getState().refreshToken;
      if (!refreshToken) {
        useAuthStore.getState().clearAuth();
        clearChatCache();
        redirectToLogin();
        return Promise.reject(normalizeApiError(error));
      }

      try {
        const refreshResponse = await axios.post(
          `${axiosClient.defaults.baseURL}/auth/refresh`,
          { refreshToken: refreshToken },
          {
            headers: {
              'Content-Type': 'application/json',
            },
          }
        );

        const authData = refreshResponse?.data?.data;
        const nextAccessToken = authData?.accessToken;
        const nextRefreshToken = authData?.refreshToken || refreshToken;

        if (!nextAccessToken) {
          throw new Error('Refresh response does not contain accessToken.');
        }

        useAuthStore.getState().setAuth({
          accessToken: nextAccessToken,
          refreshToken: nextRefreshToken,
          expiresIn: authData?.expiresIn ?? null,
          role: authData?.role ?? null,
        });

        processQueue(null, nextAccessToken);
        originalRequest.headers = originalRequest.headers || {};
        originalRequest.headers.Authorization = `Bearer ${nextAccessToken}`;
        return axiosClient(originalRequest);
      } catch (refreshError) {
        useAuthStore.getState().clearAuth();
        clearChatCache();
        processQueue(refreshError, null);
        redirectToLogin();
        return Promise.reject(normalizeApiError(refreshError));
      } finally {
        isRefreshing = false;
      }
    }

    return Promise.reject(normalizeApiError(error));
  }
);

export default axiosClient;
