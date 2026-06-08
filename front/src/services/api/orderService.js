import axiosClient from './axiosClient';

export const orderService = {
    /**
     * Create user order
     */
    createOrder: (data) => {
        return axiosClient.post('/orders', data);
    },

    /**
     * Initialize payment for an order
     */
    initPayment: (orderId) => {
        return axiosClient.post(`/orders/${orderId}/payment`);
    }
};
