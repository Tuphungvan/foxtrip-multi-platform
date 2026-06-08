import { useState, useEffect } from 'react';
import { createGuideAccount, createAdminAccount } from '../../services/api/adminApi';
import { FaTimes, FaSpinner, FaUserCheck, FaUserShield, FaUserPlus } from 'react-icons/fa';
import { PROVINCES } from '../../utils/constants';
import { useAuthStore } from '../../store/useAuthStore';
import toast from 'react-hot-toast';
import AdminButton from './ui/AdminButton';

const UserModal = ({ isOpen, onClose, onSuccess }) => {
    const { role: currentUserRole } = useAuthStore();
    const [targetRole, setTargetRole] = useState('GUIDE'); // 'GUIDE' | 'ADMIN'
    const [isSubmitting, setIsSubmitting] = useState(false);
    
    const [formData, setFormData] = useState({
        email: '',
        username: '',
        phoneNumber: '',
        province: 'HA_NOI'
    });

    useEffect(() => {
        if (!isOpen) {
            setFormData({
                email: '',
                username: '',
                phoneNumber: '',
                province: 'HA_NOI'
            });
            setTargetRole('GUIDE');
        }
    }, [isOpen]);

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
    };

    const validateForm = () => {
        const { email, username, phoneNumber } = formData;
        
        if (!email || !username || !phoneNumber) {
            toast.error('Vui lòng điền đầy đủ thông tin');
            return false;
        }

        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailRegex.test(email)) {
            toast.error('Email không hợp lệ');
            return false;
        }

        if (username.length < 8) {
            toast.error('Username phải có ít nhất 8 ký tự');
            return false;
        }

        if (!/^[0-9]{10}$/.test(phoneNumber)) {
            toast.error('Số điện thoại phải là 10 chữ số');
            return false;
        }

        return true;
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!validateForm()) return;

        setIsSubmitting(true);
        try {
            let res;
            if (targetRole === 'GUIDE') {
                res = await createGuideAccount(formData);
            } else {
                // Admin creation doesn't need province in DTO according to CreateAdminRequest.java
                const { province, ...adminPayload } = formData;
                res = await createAdminAccount(adminPayload);
            }
            
            toast.success(res.message || 'Tạo tài khoản thành công! Mật khẩu đã được gửi đến email.');
            onSuccess();
            onClose();
        } catch (error) {
            toast.error(error.message || 'Có lỗi xảy ra khi tạo tài khoản');
        } finally {
            setIsSubmitting(false);
        }
    };

    if (!isOpen) return null;

    return (
        <div className="fixed inset-0 z-[100] flex items-center justify-center p-4 bg-slate-900/40 backdrop-blur-sm">
            <div className="bg-white rounded-[24px] shadow-2xl w-full max-w-md overflow-hidden flex flex-col border border-slate-100 animate-in fade-in zoom-in duration-200">
                
                {/* Header */}
                <div className="px-6 py-5 border-b border-slate-100 flex justify-between items-center bg-white shrink-0">
                    <div className="flex items-center gap-3">
                        <div className="w-10 h-10 rounded-xl bg-blue-50 flex items-center justify-center text-[#129AF2]">
                            <FaUserPlus />
                        </div>
                        <h3 className="text-lg font-bold text-slate-900">Thêm Thành Viên Mới</h3>
                    </div>
                    <button onClick={onClose} className="p-2 bg-slate-50 hover:bg-slate-100 text-slate-500 rounded-full transition-colors">
                        <FaTimes className="text-sm" />
                    </button>
                </div>

                <form onSubmit={handleSubmit}>
                    <div className="p-6 space-y-5 bg-slate-50/30">
                        {/* Role Selection (Only for SUPER_ADMIN) */}
                        {currentUserRole === 'SUPER_ADMIN' && (
                            <div className="grid grid-cols-2 gap-3 p-1 bg-slate-100 rounded-xl">
                                <button
                                    type="button"
                                    onClick={() => setTargetRole('GUIDE')}
                                    className={`flex items-center justify-center gap-2 py-2.5 rounded-lg text-sm font-bold transition-all ${
                                        targetRole === 'GUIDE'
                                            ? 'bg-white text-emerald-600 shadow-sm'
                                            : 'text-slate-500 hover:text-slate-700'
                                    }`}
                                >
                                    <FaUserCheck className="text-xs" /> Hướng Dẫn Viên
                                </button>
                                <button
                                    type="button"
                                    onClick={() => setTargetRole('ADMIN')}
                                    className={`flex items-center justify-center gap-2 py-2.5 rounded-lg text-sm font-bold transition-all ${
                                        targetRole === 'ADMIN'
                                            ? 'bg-white text-blue-600 shadow-sm'
                                            : 'text-slate-500 hover:text-slate-700'
                                    }`}
                                >
                                    <FaUserShield className="text-xs" /> Quản Trị Viên
                                </button>
                            </div>
                        )}

                        <div className="space-y-4">
                            <div>
                                <label className="block text-xs font-bold text-slate-500 uppercase tracking-wider mb-2">Username <span className="text-red-500">*</span></label>
                                <input
                                    type="text"
                                    name="username"
                                    value={formData.username}
                                    onChange={handleInputChange}
                                    placeholder="Ít nhất 8 ký tự..."
                                    className="w-full px-4 py-3 text-sm bg-white border border-slate-200 rounded-xl focus:ring-2 focus:ring-[#129AF2]/20 focus:border-[#129AF2] outline-none transition-all text-slate-700 shadow-sm"
                                    required
                                />
                            </div>

                            <div>
                                <label className="block text-xs font-bold text-slate-500 uppercase tracking-wider mb-2">Email cá nhân <span className="text-red-500">*</span></label>
                                <input
                                    type="email"
                                    name="email"
                                    value={formData.email}
                                    onChange={handleInputChange}
                                    placeholder="example@gmail.com"
                                    className="w-full px-4 py-3 text-sm bg-white border border-slate-200 rounded-xl focus:ring-2 focus:ring-[#129AF2]/20 focus:border-[#129AF2] outline-none transition-all text-slate-700 shadow-sm"
                                    required
                                />
                            </div>

                            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                                <div>
                                    <label className="block text-xs font-bold text-slate-500 uppercase tracking-wider mb-2">Số điện thoại <span className="text-red-500">*</span></label>
                                    <input
                                        type="tel"
                                        name="phoneNumber"
                                        value={formData.phoneNumber}
                                        onChange={handleInputChange}
                                        placeholder="0912xxxxxx"
                                        className="w-full px-4 py-3 text-sm bg-white border border-slate-200 rounded-xl focus:ring-2 focus:ring-[#129AF2]/20 focus:border-[#129AF2] outline-none transition-all text-slate-700 shadow-sm"
                                        required
                                    />
                                </div>

                                {targetRole === 'GUIDE' && (
                                    <div>
                                        <label className="block text-xs font-bold text-slate-500 uppercase tracking-wider mb-2">Khu vực <span className="text-red-500">*</span></label>
                                        <select
                                            name="province"
                                            value={formData.province}
                                            onChange={handleInputChange}
                                            className="w-full px-4 py-3 text-sm bg-white border border-slate-200 rounded-xl focus:ring-2 focus:ring-[#129AF2]/20 focus:border-[#129AF2] outline-none transition-all text-slate-700 shadow-sm appearance-none cursor-pointer"
                                            required
                                        >
                                            {Object.entries(PROVINCES).map(([key, val]) => (
                                                <option key={key} value={key}>{val}</option>
                                            ))}
                                        </select>
                                    </div>
                                )}
                            </div>
                        </div>

                        <div className="p-4 bg-amber-50 rounded-xl border border-amber-100">
                            <p className="text-[11px] text-amber-700 leading-relaxed font-medium">
                                <strong>Lưu ý:</strong> Mật khẩu sẽ được hệ thống tạo tự động và gửi trực tiếp đến Email đã đăng ký phía trên. Vui lòng yêu cầu thành viên kiểm tra hộp thư (bao gồm cả thư rác).
                            </p>
                        </div>
                    </div>

                    {/* Footer */}
                    <div className="px-6 py-5 border-t border-slate-100 bg-white flex justify-end gap-3">
                        <AdminButton
                            variant="ghost"
                            onClick={onClose}
                        >
                            Hủy bỏ
                        </AdminButton>
                        <AdminButton
                            type="submit"
                            variant="primary"
                            disabled={isSubmitting}
                            loading={isSubmitting}
                            icon={FaUserPlus}
                        >
                            Tạo Tài Khoản
                        </AdminButton>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default UserModal;
