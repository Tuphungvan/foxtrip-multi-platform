import axiosClient from './axiosClient';

export const chatbotService = {
    /**
     * Send message to chatbot
     */
    chat: (message, history = []) => {
        return axiosClient.post('/chatbot/chat', { message, history });
    }
};
