import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import { useCartStore } from './useCartStore';

export const useAuthStore = create(
  persist(
    (set) => ({
      accessToken: null,
      refreshToken: null,
      expiresIn: null,
      role: null,
      user: null,
      isAuthenticated: false,

      setUser: (user) => set({ user }),

      setAuth: (data) => {
        // Block GUIDE role on Web
        if (data.role === 'GUIDE') {
          throw new Error('Tài khoản GUIDE không được phép truy cập trên nền tảng Web.');
        }

        set({
          accessToken: data.accessToken,
          refreshToken: data.refreshToken,
          expiresIn: data.expiresIn,
          role: data.role,
          isAuthenticated: true,
          user: data.user,
        });

        // Fetch cart from backend after login
        if (data.role === 'USER') {
          useCartStore.getState().fetchCart();
        }
      },

      logout: async () => {
        const { accessToken, refreshToken, clearAuth } = useAuthStore.getState();
        
        if (accessToken && refreshToken) {
          try {
            await fetch(`${import.meta.env.VITE_API_BASE_URL}/auth/logout`, {
              method: 'POST',
              headers: {
                'Content-Type': 'application/json',
              },
              body: JSON.stringify({ refreshToken }),
            });
          } catch (error) {
            console.error('Logout API call failed:', error);
          }
        }
        
        clearAuth();
      },

      clearAuth: () => {
        set({
          accessToken: null,
          refreshToken: null,
          expiresIn: null,
          role: null,
          user: null,
          isAuthenticated: false,
        });
        // ONLY clear local cart state, don't call backend clear
        useCartStore.getState().clearCart(false);
      },
    }),
    {
      name: 'foxtrip-auth',
    }
  )
);
