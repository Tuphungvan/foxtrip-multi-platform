import { useState, useEffect } from 'react';
import { updateAdminProfile, uploadImageToCloudinary, uploadImageUrlToCloudinary } from '../../services/api/adminApi';
import { FaTimes, FaSpinner, FaUserEdit, FaCamera, FaLock, FaEnvelope, FaUserShield, FaMapMarkerAlt, FaCheckCircle, FaUser, FaCloudUploadAlt, FaLink } from 'react-icons/fa';
import { useAuthStore } from '../../store/useAuthStore';
import { PROVINCES } from '../../utils/constants';
import toast from 'react-hot-toast';
import AdminButton from './ui/AdminButton';

const ProfileModal = ({ isOpen, onClose, onSuccess }) => {
    const { user, setUser } = useAuthStore();
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [isUploading, setIsUploading] = useState(false);
    const [imageMode, setImageMode] = useState('upload'); // 'upload' | 'url'
    const [imagePreview, setImagePreview] = useState(null);
    
    const [formData, setFormData] = useState({
        phoneNumber: '',
        password: '',
        avatarUrl: ''
    });

    useEffect(() => {
        if (isOpen && user) {
            setFormData({
                phoneNumber: user.phoneNumber || '',
                password: '',
                avatarUrl: user.avatarUrl || ''
            });
            setImagePreview(user.avatarUrl || null);
        }
    }, [isOpen, user]);

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
    };

    const handleImageError = (e) => {
        e.target.src = `https://ui-avatars.com/api/?name=${user?.username}&background=f1f5f9&color=64748b`;
    };

    // Upload File ngay lập tức
    const handleFileChange = async (e) => {
        const file = e.target.files[0];
        if (file) {
            setImagePreview(URL.createObjectURL(file));
            setIsUploading(true);
            try {
                toast.loading('Đang tải ảnh lên...', { id: 'upload-avatar' });
                const uploadedUrl = await uploadImageToCloudinary(file, 'foxtrip/avatars');
                setFormData(prev => ({ ...prev, avatarUrl: uploadedUrl }));
                setImagePreview(uploadedUrl);
                toast.success('Tải ảnh lên thành công!', { id: 'upload-avatar' });
            } catch (error) {
                toast.error('Lỗi tải ảnh: ' + error.message, { id: 'upload-avatar' });
            } finally {
                setIsUploading(false);
            }
        }
    };

    // Chuyển đổi link URL sang Cloudinary ngay lập tức
    const handleUrlUpload = async () => {
        if (!formData.avatarUrl || formData.avatarUrl.includes('cloudinary.com')) return;

        setIsUploading(true);
        try {
            toast.loading('Đang chuẩn hóa ảnh từ URL...', { id: 'upload-url' });
            const uploadedUrl = await uploadImageUrlToCloudinary(formData.avatarUrl, 'foxtrip/avatars');
            setFormData(prev => ({ ...prev, avatarUrl: uploadedUrl }));
            setImagePreview(uploadedUrl);
            toast.success('Chuẩn hóa ảnh thành công!', { id: 'upload-url' });
        } catch (error) {
            toast.error('Lỗi: ' + error.message, { id: 'upload-url' });
        } finally {
            setIsUploading(false);
        }
    };

    const validateForm = () => {
        const { phoneNumber, password } = formData;
        
        if (!phoneNumber) {
            toast.error('Vui lòng nhập số điện thoại');
            return false;
        }

        if (!/^[0-9]{10}$/.test(phoneNumber)) {
            toast.error('Số điện thoại phải là 10 chữ số');
            return false;
        }

        if (password && password.length < 8) {
            toast.error('Mật khẩu mới phải có ít nhất 8 ký tự');
            return false;
        }

        if (isUploading) {
            toast.error('Vui lòng đợi quá trình tải ảnh hoàn tất');
            return false;
        }

        return true;
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!validateForm()) return;

        setIsSubmitting(true);
        try {
            const payload = { 
                phoneNumber: formData.phoneNumber,
                avatarUrl: formData.avatarUrl
            };
            if (formData.password.trim()) {
                payload.password = formData.password.trim();
            }

            const res = await updateAdminProfile(payload);
            
            toast.success(res.message || 'Cập nhật hồ sơ thành công!');
            
            if (user) {
                setUser({ 
                    ...user, 
                    phoneNumber: formData.phoneNumber, 
                    avatarUrl: formData.avatarUrl 
                });
            }

            onSuccess?.();
            onClose();
        } catch (error) {
            toast.error(error.message || 'Có lỗi xảy ra khi cập nhật hồ sơ');
        } finally {
            setIsSubmitting(false);
        }
    };

    if (!isOpen) return null;

    return (
        <div className="fixed inset-0 z-[100] flex items-center justify-center p-4 bg-slate-900/40 backdrop-blur-sm">
            <div className="bg-white rounded-[24px] shadow-2xl w-full max-w-2xl overflow-hidden flex flex-col border border-slate-100 animate-in fade-in zoom-in duration-200">
                
                {/* Header */}
                <div className="px-6 py-5 border-b border-slate-100 flex justify-between items-center bg-white shrink-0">
                    <div className="flex items-center gap-3">
                        <div className="w-10 h-10 rounded-xl bg-blue-50 text-[#129AF2] flex items-center justify-center text-lg">
                            <FaUserEdit />
                        </div>
                        <div>
                            <h3 className="text-lg font-bold text-slate-900 leading-none">Thông Tin Cá Nhân</h3>
                            <p className="text-[10px] text-slate-400 mt-1 uppercase font-bold tracking-wider">Cập nhật hồ sơ hệ thống</p>
                        </div>
                    </div>
                    <button onClick={onClose} className="p-2 bg-slate-50 hover:bg-slate-100 text-slate-500 rounded-full transition-colors">
                        <FaTimes className="text-sm" />
                    </button>
                </div>

                <form onSubmit={handleSubmit} className="overflow-y-auto custom-scrollbar max-h-[85vh]">
                    <div className="p-6 bg-slate-50/30">
                        {/* Top Info Section */}
                        <div className="flex flex-col md:flex-row gap-8 items-start mb-8 p-6 bg-white rounded-[20px] border border-slate-100 shadow-sm relative overflow-hidden">
                            <div className="absolute top-0 right-0 p-4">
                                {user?.isVerified ? (
                                    <span className="flex items-center gap-1.5 px-2.5 py-1 bg-emerald-50 text-emerald-600 rounded-full text-[10px] font-bold border border-emerald-100 shadow-sm">
                                        <FaCheckCircle className="text-[12px]" /> ĐÃ XÁC MINH
                                    </span>
                                ) : (
                                    <span className="px-2.5 py-1 bg-slate-100 text-slate-400 rounded-full text-[10px] font-bold border border-slate-200">
                                        CHƯA XÁC MINH
                                    </span>
                                )}
                            </div>

                            <div className="mx-auto md:mx-0 shrink-0">
                                <div className="w-32 h-32 rounded-[32px] border-4 border-slate-50 shadow-md overflow-hidden bg-slate-200">
                                    <img 
                                        src={imagePreview || `https://ui-avatars.com/api/?name=${user?.username}&background=f1f5f9&color=64748b`} 
                                        alt="Avatar" 
                                        onError={handleImageError}
                                        className="w-full h-full object-cover transition-all"
                                    />
                                </div>
                                {isUploading && (
                                    <div className="mt-2 text-[10px] text-blue-500 font-bold flex items-center justify-center gap-1">
                                        <FaSpinner className="animate-spin" /> ĐANG TẢI LÊN...
                                    </div>
                                )}
                            </div>

                            <div className="flex-1 space-y-4 w-full">
                                <div>
                                    <div className="text-[10px] font-bold text-slate-400 uppercase tracking-widest mb-1 items-center flex gap-1.5 line-clamp-1"><FaUser className="text-[8px]" /> Họ và tên</div>
                                    <div className="text-lg font-bold text-slate-900">{user?.username}</div>
                                </div>
                                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                                    <div className="p-3 bg-slate-50 rounded-xl border border-slate-100">
                                        <div className="text-[10px] font-bold text-slate-400 uppercase tracking-widest mb-1 flex items-center gap-1.5"><FaUserShield className="text-[8px]" /> Chức vụ</div>
                                        <div className="text-xs font-bold text-slate-700">{user?.role}</div>
                                    </div>
                                    <div className="p-3 bg-slate-50 rounded-xl border border-slate-100">
                                        <div className="text-[10px] font-bold text-slate-400 uppercase tracking-widest mb-1 flex items-center gap-1.5"><FaMapMarkerAlt className="text-[8px]" /> Khu vực</div>
                                        <div className="text-xs font-bold text-slate-700">{PROVINCES[user?.province] || user?.province || 'N/A'}</div>
                                    </div>
                                </div>
                            </div>
                        </div>

                        {/* Form Fields & Avatar Upload Tabs */}
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                            {/* Left Side: Avatar Upload Mechanism */}
                            <div className="space-y-5">
                                <h4 className="text-[11px] font-bold text-slate-900 uppercase tracking-widest border-l-4 border-slate-300 pl-3">Đổi ảnh đại diện</h4>
                                
                                {/* Tab Switcher */}
                                <div className="flex gap-2 p-1 bg-white border border-slate-200 rounded-xl">
                                    <button
                                        type="button"
                                        onClick={() => setImageMode('upload')}
                                        className={`flex-1 py-2 px-3 rounded-lg text-[11px] font-bold transition-all flex items-center justify-center gap-2 ${imageMode === 'upload'
                                                ? 'bg-slate-900 text-white shadow-md'
                                                : 'text-slate-500 hover:text-slate-700'
                                            }`}
                                    >
                                        <FaCloudUploadAlt /> Tải File
                                    </button>
                                    <button
                                        type="button"
                                        onClick={() => setImageMode('url')}
                                        className={`flex-1 py-2 px-3 rounded-lg text-[11px] font-bold transition-all flex items-center justify-center gap-2 ${imageMode === 'url'
                                                ? 'bg-slate-900 text-white shadow-md'
                                                : 'text-slate-500 hover:text-slate-700'
                                            }`}
                                    >
                                        <FaLink /> Link URL
                                    </button>
                                </div>

                                {imageMode === 'upload' ? (
                                    <div className="border-2 border-dashed border-slate-200 rounded-2xl p-8 relative cursor-pointer group bg-white hover:border-[#129AF2] hover:bg-blue-50/20 transition-all text-center">
                                        <input
                                            type="file"
                                            accept="image/*"
                                            onChange={handleFileChange}
                                            className="absolute inset-0 w-full h-full opacity-0 cursor-pointer z-10"
                                            disabled={isUploading}
                                        />
                                        <FaCamera className="text-3xl text-slate-300 group-hover:text-[#129AF2] mx-auto mb-3 transition-colors" />
                                        <div className="text-[11px] font-bold text-slate-500 group-hover:text-slate-700">Nhấp vào đây để chọn ảnh</div>
                                        <div className="text-[9px] text-slate-400 mt-1 uppercase tracking-tighter">PNG, JPG, WEBP (Max 5MB)</div>
                                    </div>
                                ) : (
                                    <div className="space-y-3">
                                        <div className="relative">
                                            <input
                                                type="text"
                                                name="avatarUrl"
                                                value={formData.avatarUrl}
                                                onChange={handleInputChange}
                                                placeholder="Nhập đường dẫn ảnh (Google, Facebook...)"
                                                className="w-full px-4 py-3 text-sm bg-white border border-slate-200 rounded-xl focus:ring-4 focus:ring-[#129AF2]/20 focus:border-[#129AF2] outline-none transition-all text-slate-700 shadow-sm"
                                            />
                                            <button
                                                type="button"
                                                onClick={handleUrlUpload}
                                                disabled={isUploading || !formData.avatarUrl || formData.avatarUrl.includes('cloudinary.com')}
                                                className="absolute right-2 top-1/2 -translate-y-1/2 px-3 py-1.5 bg-slate-100 hover:bg-slate-200 text-slate-600 rounded-lg text-[10px] font-bold transition-all disabled:opacity-0"
                                            >
                                                Tải lên
                                            </button>
                                        </div>
                                        <p className="text-[10px] text-slate-400 px-1 italic">Mẹo: Hệ thống tự động chuyển ảnh ngoại về Cloudinary để bảo quản vĩnh viễn.</p>
                                    </div>
                                )}
                            </div>

                            {/* Right Side: Editable Info */}
                            <div className="space-y-5">
                                <h4 className="text-[11px] font-bold text-slate-900 uppercase tracking-widest border-l-4 border-[#129AF2] pl-3">Thông tin chỉnh sửa</h4>
                                <div>
                                    <label className="block text-[10px] font-bold text-slate-500 uppercase tracking-wider mb-2">Số điện thoại liên hệ <span className="text-red-500">*</span></label>
                                    <input
                                        type="tel"
                                        name="phoneNumber"
                                        value={formData.phoneNumber}
                                        onChange={handleInputChange}
                                        placeholder="0912xxxxxx"
                                        className="w-full px-4 py-3 text-sm bg-white border border-slate-200 rounded-xl focus:ring-4 focus:ring-[#129AF2]/20 focus:border-[#129AF2] outline-none transition-all text-slate-700 shadow-sm"
                                        required
                                    />
                                </div>

                                <div>
                                    <div className="flex items-center justify-between mb-2">
                                        <label className="block text-[10px] font-bold text-slate-500 uppercase tracking-wider">Mật khẩu mới</label>
                                        <span className="text-[9px] text-slate-400 font-bold bg-slate-100 px-2 py-0.5 rounded uppercase tracking-tighter italic">Bảo mật</span>
                                    </div>
                                    <div className="relative">
                                        <FaLock className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
                                        <input
                                            type="password"
                                            name="password"
                                            value={formData.password}
                                            onChange={handleInputChange}
                                            placeholder="••••••••"
                                            className="w-full px-4 py-3 pl-10 text-sm bg-white border border-slate-200 rounded-xl focus:ring-4 focus:ring-[#129AF2]/20 focus:border-[#129AF2] outline-none transition-all text-slate-700 shadow-sm"
                                        />
                                    </div>
                                    <p className="text-[10px] text-slate-400 mt-2 px-1 italic">* Để trống nếu bạn muốn giữ mật khẩu hiện tại.</p>
                                </div>
                            </div>
                        </div>

                         {/* Read Only Email Section at bottom */}
                         <div className="mt-8 pt-6 border-t border-slate-100">
                            <div className="flex flex-col sm:flex-row items-center justify-between gap-4 p-4 bg-white rounded-2xl border border-slate-100 shadow-sm">
                                <div className="flex items-center gap-4">
                                    <div className="w-10 h-10 rounded-full bg-blue-50 text-blue-500 flex items-center justify-center">
                                        <FaEnvelope />
                                    </div>
                                    <div>
                                        <div className="text-[10px] font-bold text-slate-400 uppercase tracking-wider">Email định danh tài khoản</div>
                                        <div className="text-sm font-bold text-slate-700">{user?.email}</div>
                                    </div>
                                </div>
                                <div className="px-4 py-2 bg-slate-100 text-slate-400 text-[10px] font-bold rounded-xl flex items-center gap-2">
                                    <FaLock className="text-[8px]" /> FIXED
                                </div>
                            </div>
                        </div>
                    </div>

                    {/* Footer */}
                    <div className="px-8 py-6 border-t border-slate-100 bg-white flex justify-end items-center gap-4">
                        <AdminButton
                            variant="ghost"
                            onClick={onClose}
                        >
                            Đóng
                        </AdminButton>
                        <AdminButton
                            type="submit"
                            variant="primary"
                            disabled={isSubmitting || isUploading}
                            loading={isSubmitting}
                            icon={FaUserEdit}
                        >
                            CẬP NHẬT TRẠNG THÁI
                        </AdminButton>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default ProfileModal;
