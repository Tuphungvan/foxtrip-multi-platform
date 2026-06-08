import { create } from 'zustand';

export const useUIStore = create((set) => ({
    isChatbotOpen: false,
    isAuthModalOpen: false,
    authModalTab: 'login', // 'login' or 'register'

    openChatbot: () => set({ isChatbotOpen: true }),
    closeChatbot: () => set({ isChatbotOpen: false }),
    toggleChatbot: () => set((state) => ({ isChatbotOpen: !state.isChatbotOpen })),

    openAuthModal: (tab = 'login') => set({ isAuthModalOpen: true, authModalTab: tab }),
    closeAuthModal: () => set({ isAuthModalOpen: false }),
}));
