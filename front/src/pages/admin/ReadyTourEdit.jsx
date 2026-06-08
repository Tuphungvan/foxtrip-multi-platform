import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { FaChevronLeft, FaSave, FaSpinner, FaImage, FaVideo, FaUserTie, FaPercent } from 'react-icons/fa';
import { getTourDetail, updateTourBase, getGuideSuggestions } from '../../services/api/adminApi';
import toast from 'react-hot-toast';
import AdminButton from '../../components/admin/ui/AdminButton';
import AdminCard from '../../components/admin/ui/AdminCard';

const ReadyTourEdit = () => {
    const { tourId } = useParams();
    const navigate = useNavigate();
    const [isLoading, setIsLoading] = useState(true);
    const [isSaving, setIsSaving] = useState(false);
    const [guides, setGuides] = useState([]);
    
    const [formData, setFormData] = useState({
        guideId: '',
        discount: 0,
        thumbnailUrl: '',
        shortId: ''
    });

    useEffect(() => {
        const fetchData = async () => {
            try {
                const [tourRes, guideRes] = await Promise.all([
                    getTourDetail(tourId),
                    getGuideSuggestions(tourId)
                ]);
                
                const tour = tourRes?.data || tourRes;
                setFormData({
                    guideId: tour.guideId || '',
                    discount: tour.discount || 0,
                    thumbnailUrl: tour.thumbnailUrl || '',
                    shortId: tour.shortId || ''
                });
                
                setGuides(guideRes?.data || []);
            } catch (err) {
                toast.error('Lỗi khi tải thông tin: ' + err.message);
                navigate('/admin/manage-tours');
            } finally {
                setIsLoading(false);
            }
        };
        fetchData();
    }, [tourId, navigate]);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setIsSaving(true);
        
        const submitData = {
            ...formData,
            guideId: formData.guideId || null,
            discount: Number(formData.discount)
        };

        try {
            await updateTourBase(tourId, submitData);
            toast.success('Cập nhật tour thành công!');
            navigate('/admin/manage-tours');
        } catch (err) {
            toast.error('Lỗi cập nhật: ' + (err.response?.data?.message || err.message));
        } finally {
            setIsSaving(false);
        }
    };

    if (isLoading) {
        return (
            <div className="flex items-center justify-center min-h-[400px]">
                <FaSpinner className="animate-spin text-3xl text-slate-800" />
            </div>
        );
    }

    const inputClass = "w-full px-4 py-2.5 bg-white border border-slate-200 text-slate-800 text-sm rounded-md focus:outline-none focus:border-[#129AF2] focus:ring-2 focus:ring-[#129AF2]/10 transition-all";

    return (
        <div className="max-w-4xl mx-auto space-y-6 pb-10">

            {/* Page Header */}
            <div className="flex items-center gap-3">
                <button
                    onClick={() => navigate('/admin/manage-tours')}
                    className="p-2 hover:bg-slate-100 rounded-md transition-colors"
                >
                    <FaChevronLeft className="text-slate-600" />
                </button>
                <div>
                    <h2 className="text-2xl font-bold text-slate-900">Chỉnh Sửa Nhanh Tour</h2>
                    <p className="text-slate-500 text-sm mt-0.5">Tour đã sẵn sàng, bạn chỉ có thể cập nhật các thông tin vận hành.</p>
                </div>
            </div>

            <form onSubmit={handleSubmit}>
                <AdminCard noPadding>
                    <div className="p-8 space-y-8">
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-8">

                            {/* Hướng dẫn viên */}
                            <div className="space-y-2">
                                <label className="flex items-center gap-2 text-xs font-bold text-slate-500 uppercase tracking-wider">
                                    <FaUserTie /> Hướng dẫn viên
                                </label>
                                <select
                                    value={formData.guideId}
                                    onChange={e => setFormData({ ...formData, guideId: e.target.value })}
                                    className={inputClass}
                                >
                                    <option value="">Chưa chọn hướng dẫn viên</option>
                                    {guides.map(guide => (
                                        <option key={guide.id} value={guide.id}>
                                            {guide.username} ({guide.email}){guide.phoneNumber ? ` - ${guide.phoneNumber}` : ''}
                                        </option>
                                    ))}
                                </select>
                            </div>

                            {/* Giảm giá */}
                            <div className="space-y-2">
                                <label className="flex items-center gap-2 text-xs font-bold text-slate-500 uppercase tracking-wider">
                                    <FaPercent /> Giảm giá (%)
                                </label>
                                <input
                                    type="number"
                                    min="0"
                                    max="100"
                                    value={formData.discount}
                                    onChange={e => setFormData({ ...formData, discount: e.target.value })}
                                    className={inputClass}
                                />
                            </div>

                            {/* Ảnh bìa */}
                            <div className="md:col-span-2 space-y-2">
                                <label className="flex items-center gap-2 text-xs font-bold text-slate-500 uppercase tracking-wider">
                                    <FaImage /> URL Ảnh bìa
                                </label>
                                <input
                                    type="text"
                                    value={formData.thumbnailUrl}
                                    onChange={e => setFormData({ ...formData, thumbnailUrl: e.target.value })}
                                    className={inputClass}
                                    placeholder="https://..."
                                />
                            </div>

                            {/* Video Short */}
                            <div className="md:col-span-2 space-y-2">
                                <label className="flex items-center gap-2 text-xs font-bold text-slate-500 uppercase tracking-wider">
                                    <FaVideo /> Mã Video (Short ID)
                                </label>
                                <input
                                    type="text"
                                    value={formData.shortId}
                                    onChange={e => setFormData({ ...formData, shortId: e.target.value })}
                                    className={inputClass}
                                    placeholder="Mã video youtube short..."
                                />
                            </div>
                        </div>

                        {/* Footer actions */}
                        <div className="pt-6 border-t border-slate-100 flex justify-end gap-3">
                            <AdminButton
                                type="button"
                                variant="ghost"
                                onClick={() => navigate('/admin/manage-tours')}
                            >
                                Hủy bỏ
                            </AdminButton>
                            <AdminButton
                                type="submit"
                                variant="primary"
                                icon={isSaving ? undefined : FaSave}
                                loading={isSaving}
                            >
                                Lưu thay đổi
                            </AdminButton>
                        </div>
                    </div>
                </AdminCard>
            </form>
        </div>
    );
};

export default ReadyTourEdit;
