import axiosClient from './axiosClient';

export const tourService = {
    /**
     * Search tours with filters
     */
    searchTours: (params) => {
        // Clean params: remove empty strings or null/undefined
        const cleanParams = Object.entries(params).reduce((acc, [key, value]) => {
            if (value !== '' && value !== null && value !== undefined) {
                acc[key] = value;
            }
            return acc;
        }, {});
        return axiosClient.get('/tours/search', { params: cleanParams });
    },

    /**
     * Get upcoming tours
     */
    getUpcomingTours: (limit = 10) => {
        return axiosClient.get('/tours/upcoming', { params: { limit } });
    },

    /**
     * Get discounted tours
     */
    getDiscountedTours: (limit = 10) => {
        return axiosClient.get('/tours/discounted', { params: { limit } });
    },

    /**
     * Get tour detail by slug
     */
    getTourDetail: (slug) => {
        return axiosClient.get(`/tours/${slug}`);
    },

    /**
     * Get tour reviews
     */
    getTourReviews: (tourId, params) => {
        return axiosClient.get(`/tours/${tourId}/reviews`, { params });
    }
};
