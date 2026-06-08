import axiosClient from './axiosClient';

export const userService = {
    getMe: () => {
        return axiosClient.get('/users/me');
    },
    updateProfile: (data) => {
        return axiosClient.patch('/users/me/profile', data);
    }
};
