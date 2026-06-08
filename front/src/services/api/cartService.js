import axiosClient from './axiosClient';

export const cartService = {
    getCart: () => {
        return axiosClient.get('/cart');
    },
    upsertItem: (tourId, quantity) => {
        return axiosClient.put('/cart/item', { tourId, quantity });
    },
    clearCart: () => {
        return axiosClient.delete('/cart');
    }
};
