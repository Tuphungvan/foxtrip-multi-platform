import axiosClient from './axiosClient';

export const getAdminMe = () => {
    return axiosClient.get('/admin/me');
};

export const updateAdminProfile = (payload) => {
    return axiosClient.patch('/admin/me/profile', payload);
};

export const getRevenueReport = (month, year) => {
    return axiosClient.get('/admin/reports/revenue', { params: { month, year } });
};

export const getLatestRevenueReport = () => {
    return axiosClient.get('/admin/reports/revenue/latest');
};

export const getTourOccupancy = (month, year) => {
    return axiosClient.get('/admin/statistics/occupancy', { params: { month, year } });
};

export const getDailyBookings = (date) => {
    return axiosClient.get('/admin/statistics/daily', { params: { date } });
};

export const getComprehensiveReport = (month, year) => {
    return axiosClient.get('/admin/statistics/comprehensive', { params: { month, year } });
};

export const uploadImageToCloudinary = async (file, targetFolder = 'foxtrip/locations') => {
    console.log('🔄 Starting Cloudinary upload for file:', file.name, 'to folder:', targetFolder);

    // 1. Lấy Signature từ backend
    const apiRes = await axiosClient.get(`/cloudinary/signature?folder=${targetFolder}`);
    console.log('✅ Got signature response:', apiRes);

    // apiRes chính là object ApiResponse { data, message, errors }
    const signatureData = apiRes.data;

    if (!signatureData) throw new Error("Không lấy được signature từ server.");
    const { signature, timestamp, apiKey, folder } = signatureData;
    console.log('📝 Signature data:', { signature: signature?.substring(0, 10) + '...', timestamp, apiKey, folder });

    // 2. Tải ảnh lên Cloudinary bằng Signed Upload
    const cloudName = import.meta.env.VITE_CLOUDINARY_CLOUD_NAME;
    if (!cloudName) throw new Error("VITE_CLOUDINARY_CLOUD_NAME is missing in .env");
    console.log('☁️ Uploading to Cloudinary cloud:', cloudName);

    const formData = new FormData();
    formData.append("file", file);
    formData.append("api_key", apiKey);
    formData.append("timestamp", timestamp);
    formData.append("signature", signature);
    formData.append("folder", folder);

    console.log('📤 Uploading to Cloudinary with formData keys:', Array.from(formData.keys()));

    const res = await fetch(`https://api.cloudinary.com/v1_1/${cloudName}/image/upload`, {
        method: "POST",
        body: formData
    });

    console.log('📥 Cloudinary response status:', res.status, res.statusText);

    if (!res.ok) {
        const errorText = await res.text();
        console.error('❌ Cloudinary upload failed:', errorText);
        throw new Error("Tải ảnh lên Cloudinary thất bại: " + errorText);
    }

    const data = await res.json();
    console.log('✅ Cloudinary upload success:', data.secure_url);
    return data.secure_url;
};

export const uploadImageUrlToCloudinary = async (imageUrl, targetFolder = 'foxtrip/locations') => {
    console.log('🔄 Starting Cloudinary upload from URL:', imageUrl, 'to folder:', targetFolder);

    // 1. Lấy Signature từ backend
    const apiRes = await axiosClient.get(`/cloudinary/signature?folder=${targetFolder}`);
    console.log('✅ Got signature response:', apiRes);

    const signatureData = apiRes.data;

    if (!signatureData) throw new Error("Không lấy được signature từ server.");
    const { signature, timestamp, apiKey, folder } = signatureData;
    console.log('📝 Signature data:', { signature: signature?.substring(0, 10) + '...', timestamp, apiKey, folder });

    // 2. Tải ảnh từ URL lên Cloudinary
    const cloudName = import.meta.env.VITE_CLOUDINARY_CLOUD_NAME;
    if (!cloudName) throw new Error("VITE_CLOUDINARY_CLOUD_NAME is missing in .env");
    console.log('☁️ Uploading URL to Cloudinary cloud:', cloudName);

    const formData = new FormData();
    formData.append("file", imageUrl); // Cloudinary hỗ trợ upload từ URL
    formData.append("api_key", apiKey);
    formData.append("timestamp", timestamp);
    formData.append("signature", signature);
    formData.append("folder", folder);

    console.log('📤 Uploading URL to Cloudinary with formData keys:', Array.from(formData.keys()));

    const res = await fetch(`https://api.cloudinary.com/v1_1/${cloudName}/image/upload`, {
        method: "POST",
        body: formData
    });

    console.log('📥 Cloudinary response status:', res.status, res.statusText);

    if (!res.ok) {
        const errorText = await res.text();
        console.error('❌ Cloudinary URL upload failed:', errorText);
        throw new Error("Tải ảnh từ URL lên Cloudinary thất bại: " + errorText);
    }

    const data = await res.json();
    console.log('✅ Cloudinary URL upload success:', data.secure_url);
    return data.secure_url;
};

export const searchLocations = (keyword) => {
    return axiosClient.get('/admin/locations', {
        params: { keyword }
    });
};

export const getAdminLocations = (params) => {
    return axiosClient.get('/admin/locations', { params });
};

export const createAdminLocation = (payload) => {
    // payload: { name, province, address, imageUrl, mapboxPlaceId, type, lat, lng }
    return axiosClient.post('/admin/locations', payload);
};

/**
 * Soft delete location (set deletedAt)
 */
export const deleteAdminLocation = (id) => {
    return axiosClient.delete(`/admin/locations/${id}`);
};

export const restoreAdminLocation = (id) => {
    return axiosClient.patch(`/admin/locations/${id}/restore`);
};

export const updateAdminLocation = (id, payload) => {
    return axiosClient.patch(`/admin/locations/${id}`, payload);
};

export const createTourBase = (payload) => {
    return axiosClient.post('/admin/tours', payload);
};

export const updateTourBase = (tourId, payload) => {
    return axiosClient.patch(`/admin/tours/${tourId}`, payload);
};

export const getAdminTours = (params) => {
    return axiosClient.get('/admin/tours', { params });
};

export const updateTourItineraries = (tourId, items) => {
    return axiosClient.put(`/admin/tours/${tourId}/itineraries`, { items });
};

export const updateTourAddons = (tourId, addons) => {
    return axiosClient.put(`/admin/tours/${tourId}/addons`, { addons });
};

export const getGuideSuggestions = (tourId, startDate, endDate) => {
    const params = {};
    if (startDate) params.startDate = startDate;
    if (endDate) params.endDate = endDate;
    return axiosClient.get(`/admin/tours/${tourId}/guide-suggestions`, { params });
};

export const setTourGuide = (tourId, guideId) => {
    return axiosClient.patch(`/admin/tours/${tourId}/guide`, null, {
        params: { guideId }
    });
};

export const getTourDetail = (tourId) => {
    return axiosClient.get(`/admin/tours/${tourId}`);
};

export const terminateTour = (tourId) => {
    return axiosClient.patch(`/admin/tours/${tourId}/terminate`);
};

export const restartTour = (tourId, payload) => {
    return axiosClient.patch(`/admin/tours/${tourId}/restart`, payload);
};

export const activateTour = (tourId) => {
    return axiosClient.patch(`/admin/tours/${tourId}/status`, { status: "ACTIVE" });
};

export const deleteTour = (tourId) => {
    return axiosClient.delete(`/admin/tours/${tourId}`);
};

export const restoreTour = (tourId) => {
    return axiosClient.patch(`/admin/tours/${tourId}/restore`);
};

export const getAdminUsers = (params) => {
    // params: { email, page, size, sort }
    return axiosClient.get('/admin/users', { params });
};

export const lockUser = (userId) => {
    return axiosClient.post(`/admin/users/${userId}/lock`);
};

export const unlockUser = (userId) => {
    return axiosClient.post(`/admin/users/${userId}/unlock`);
};

export const createGuideAccount = (payload) => {
    return axiosClient.post('/admin/users/guides', payload);
};

export const createAdminAccount = (payload) => {
    return axiosClient.post('/admin/users/admins', payload);
};

export const getAdminOrders = (params) => {
    return axiosClient.get('/admin/orders', { params });
};

export const createRefund = (orderId, payload) => {
    return axiosClient.post(`/admin/orders/${orderId}/refunds`, payload);
};

export const updateRefund = (refundId, payload) => {
    return axiosClient.patch(`/admin/refunds/${refundId}`, payload);
};

export const rejectCancellation = (orderId, reason) => {
    return axiosClient.post(`/admin/orders/${orderId}/reject-cancellation`, { reason });
};

export const getOrderDetail = (orderId) => {
    return axiosClient.get(`/orders/${orderId}`);
};

export const goongAutoComplete = (input) => {
    return axiosClient.get('/admin/goong/autocomplete', { params: { input } });
};

export const goongPlaceDetail = (placeId) => {
    return axiosClient.get('/admin/goong/detail', { params: { place_id: placeId } });
};
