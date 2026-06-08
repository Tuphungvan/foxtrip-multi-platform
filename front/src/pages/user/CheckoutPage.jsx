import React, { useState, useEffect } from 'react';
import { useNavigate, Navigate, useLocation } from 'react-router-dom';
import { useCartStore } from '../../store/useCartStore';
import { useAuthStore } from '../../store/useAuthStore';
import { orderService } from '../../services/api/orderService';
import { userService } from '../../services/api/userService';
import { FaUser, FaPhone, FaEnvelope, FaLock, FaChevronRight, FaArrowLeft, FaCheckCircle, FaCheck, FaMinus, FaPlus } from 'react-icons/fa';
import { toast } from 'react-hot-toast';

const CheckoutPage = () => {
    const { items, removeItem } = useCartStore();
    const { isAuthenticated, user } = useAuthStore();
    const navigate = useNavigate();
    const location = useLocation();
    const selectedItemId = location.state?.selectedItemId;

    // Find the item to checkout
    const item = items.find(it => it.tourId === selectedItemId) || items[0];

    const [formData, setFormData] = useState({
        customerName: user?.username || '',
        customerPhone: '',
        customerEmail: user?.email || '',
    });
    const [selectedAddons, setSelectedAddons] = useState([]);
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [isLoadingProfile, setIsLoadingProfile] = useState(true);

    useEffect(() => {
        if (isAuthenticated) {
            fetchUserProfile();
        } else {
            setIsLoadingProfile(false);
        }
    }, [isAuthenticated]);

    const fetchUserProfile = async () => {
        try {
            const res = await userService.getMe();
            const profile = res.data;
            setFormData(prev => ({
                ...prev,
                customerName: profile.username || prev.customerName,
                customerPhone: profile.phoneNumber || prev.customerPhone,
                customerEmail: profile.email || prev.customerEmail,
            }));
        } catch (error) {
            console.error('Error fetching user profile:', error);
        } finally {
            setIsLoadingProfile(false);
        }
    };

    if (items.length === 0) {
        return <Navigate to="/cart" />;
    }

    if (!isAuthenticated) {
        return <Navigate to="/auth" />;
    }

    if (isLoadingProfile) {
        return (
            <div className="min-h-screen flex items-center justify-center bg-white">
                <div className="animate-spin rounded-full h-12 w-12 border-4 border-slate-100 border-t-[#129AF2]"></div>
            </div>
        );
    }

    const formatPrice = (price) => {
        return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(price);
    };

    const availableAddons = item.addons || item.tour.addons || [];

    const toggleAddon = (id) => {
        setSelectedAddons(prev => {
            const exists = prev.find(a => a.id === id);
            if (exists) {
                return prev.filter(a => a.id !== id);
            } else {
                return [...prev, { id, quantity: 1 }];
            }
        });
    };

    const updateAddonQuantity = (id, newQuantity) => {
        if (newQuantity < 1) return;
        if (newQuantity > item.quantity) {
            toast.error(`Số lượng không được vượt quá số lượng vé (${item.quantity})`);
            return;
        }
        setSelectedAddons(prev =>
            prev.map(a => a.id === id ? { ...a, quantity: newQuantity } : a)
        );
    };

    const addonsTotal = selectedAddons.reduce((sum, sa) => {
        const addon = availableAddons.find(a => a.id === sa.id);
        return sum + (addon?.price || 0) * sa.quantity;
    }, 0);
    const tourPrice = item.tour.finalPrice !== undefined && item.tour.finalPrice !== null
        ? item.tour.finalPrice
        : (item.tour.price || 0) * (1 - (item.tour.discount || 0) / 100);
    const grandTotal = (tourPrice * item.quantity) + addonsTotal;

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();

        if (!formData.customerName || !formData.customerPhone) {
            toast.error('Vui lòng nhập đầy đủ thông tin');
            return;
        }

        if (formData.customerPhone.length < 10) {
            toast.error('Số điện thoại không hợp lệ');
            return;
        }

        setIsSubmitting(true);
        try {
            const payload = {
                fromCart: true,
                customerName: formData.customerName,
                customerPhone: formData.customerPhone,
                customerEmail: formData.customerEmail,
                tourId: item.tourId,
                quantity: item.quantity,
                addons: {
                    items: selectedAddons.map(sa => ({ tourAddonId: sa.id, quantity: sa.quantity }))
                }
            };

            const orderRes = await orderService.createOrder(payload);
            const orderData = orderRes.data;

            // Step 2: Init Payment
            const paymentRes = await orderService.initPayment(orderData.orderId);
            const paymentUrl = paymentRes.data.paymentUrl;

            toast.success('Đặt hàng thành công! Đang chuyển đến trang thanh toán...');
            removeItem(item.tourId, isAuthenticated);
            window.location.href = paymentUrl;

        } catch (error) {
            console.error('Checkout error:', error);
            toast.error(error?.message || 'Đã xảy ra lỗi khi thanh toán');
        } finally {
            setIsSubmitting(false);
        }
    };
    return (
        <div className="max-w-6xl mx-auto px-4 md:px-6 py-12 sm:py-16">
            <div className="mb-10 border-b-2 border-slate-200 pb-6">
                <h1 className="text-2xl font-bold text-slate-800">Thanh toán</h1>
                <p className="text-slate-400 text-sm mt-1">Vui lòng xác nhận thông tin và lựa chọn dịch vụ bổ sung</p>
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-5 gap-12 items-start">
                {/* Form Section */}
                <div className="lg:col-span-3 space-y-10">
                    <div className="space-y-6">
                        <div className="flex items-center gap-3">
                            <span className="w-1 h-5 bg-[#129AF2] rounded-full"></span>
                            <h2 className="text-lg font-bold text-slate-800">Thông tin liên hệ</h2>
                        </div>
                        <div className="bg-white p-6 rounded-2xl border-2 border-[#129AF2] shadow-sm">
                            <form onSubmit={handleSubmit} className="space-y-4 max-w-sm">
                            <div className="space-y-1.5">
                                <label className="text-[12px] font-bold text-slate-900">Họ và tên</label>
                                <input
                                    type="text"
                                    name="customerName"
                                    value={formData.customerName}
                                    onChange={handleInputChange}
                                    placeholder="Nguyễn Văn A"
                                    className="w-full px-4 py-2.5 rounded-xl bg-white border border-slate-200 focus:border-[#129AF2] transition-all outline-none font-medium text-slate-700 text-sm shadow-sm"
                                    required
                                />
                            </div>
                            <div className="space-y-1.5">
                                <label className="text-[12px] font-bold text-slate-900">Số điện thoại</label>
                                <input
                                    type="tel"
                                    name="customerPhone"
                                    value={formData.customerPhone}
                                    onChange={handleInputChange}
                                    placeholder="0987xxxxxx"
                                    className="w-full px-4 py-2.5 rounded-xl bg-white border border-slate-200 focus:border-[#129AF2] transition-all outline-none font-medium text-slate-700 text-sm shadow-sm"
                                    required
                                />
                            </div>
                            <div className="space-y-1.5">
                                <label className="text-[12px] font-bold text-slate-900">Địa chỉ Email (Nhận vé)</label>
                                <input
                                    type="email"
                                    name="customerEmail"
                                    value={formData.customerEmail}
                                    readOnly
                                    disabled
                                    className="w-full px-4 py-2.5 rounded-xl bg-slate-50 border border-slate-200 text-slate-400 cursor-not-allowed outline-none font-medium text-sm"
                                />
                            </div>
                        </form>
                        </div>
                    </div>

                    {/* Addons Section */}
                    {availableAddons.length > 0 && (
                        <div className="space-y-6 pt-6 border-t-2 border-slate-200">
                            <div className="flex items-center gap-3">
                                <span className="w-1 h-5 bg-[#7C3AED] rounded-full"></span>
                                <h2 className="text-lg font-bold text-slate-800">Dịch vụ bổ sung</h2>
                            </div>
                            <div className="space-y-4">
                                {availableAddons.map(addon => {
                                    const selected = selectedAddons.find(sa => sa.id === addon.id);
                                    const isSelected = !!selected;

                                    return (
                                        <div
                                            key={addon.id}
                                            className={`flex flex-col gap-2 p-4 rounded-xl border transition-all bg-white group ${isSelected ? 'border-[#7C3AED] bg-purple-50/30' : 'border-slate-100 hover:border-slate-200'
                                                }`}
                                        >
                                            <div className="flex items-center justify-between gap-4">
                                                <div
                                                    className="flex items-center gap-3 flex-1 min-w-0 cursor-pointer"
                                                    onClick={() => toggleAddon(addon.id)}
                                                >
                                                    <div className={`w-4 h-4 rounded border flex items-center justify-center transition-all ${isSelected ? 'bg-[#7C3AED] border-[#7C3AED]' : 'border-slate-300 bg-white'
                                                        }`}>
                                                        {isSelected && <FaCheck className="text-white text-[8px]" />}
                                                    </div>
                                                    <div className="flex items-center gap-2 flex-1">
                                                        <p className="font-bold text-slate-800 text-sm">{addon.name}</p>
                                                        <span className={`text-sm font-bold transition-colors ${isSelected ? 'text-[#7C3AED]' : 'text-slate-600'}`}>
                                                            {formatPrice(addon.price)}
                                                        </span>
                                                    </div>
                                                </div>

                                                <div className="flex items-center gap-4">
                                                    {isSelected && (
                                                        <div className="flex items-center gap-2 bg-white px-2 py-1 rounded-lg border border-slate-200 shadow-sm" onClick={(e) => e.stopPropagation()}>
                                                            <button
                                                                type="button"
                                                                onClick={() => updateAddonQuantity(addon.id, selected.quantity - 1)}
                                                                className="w-6 h-6 flex items-center justify-center text-slate-400 hover:text-[#7C3AED] transition-colors"
                                                            >
                                                                <FaMinus className="text-[8px]" />
                                                            </button>
                                                            <span className="text-xs font-bold text-slate-800 w-4 text-center">{selected.quantity}</span>
                                                            <button
                                                                type="button"
                                                                onClick={() => updateAddonQuantity(addon.id, selected.quantity + 1)}
                                                                className="w-6 h-6 flex items-center justify-center text-slate-400 hover:text-[#7C3AED] transition-colors"
                                                            >
                                                                <FaPlus className="text-[8px]" />
                                                            </button>
                                                        </div>
                                                    )}
                                                    <button
                                                        type="button"
                                                        onClick={(e) => {
                                                            e.stopPropagation();
                                                            const el = document.getElementById(`addon-desc-${addon.id}`);
                                                            el.classList.toggle('hidden');
                                                        }}
                                                        className="text-[11px] font-bold text-[#7C3AED] hover:underline"
                                                    >
                                                        Xem chi tiết
                                                    </button>
                                                </div>
                                            </div>
                                            <p id={`addon-desc-${addon.id}`} className="hidden text-xs text-slate-500 font-medium leading-relaxed pt-2 border-t-2 border-slate-100">
                                                {addon.description}
                                            </p>
                                        </div>
                                    );
                                })}
                            </div>
                        </div>
                    )}
                </div>

                {/* Summary Section */}
                <div className="lg:col-span-2">
                    <div className="sticky top-24 space-y-6">
                        <div className="bg-white p-8 rounded-[32px] border-2 border-slate-200 shadow-xl space-y-8 relative overflow-hidden">
                            <div className="absolute top-0 right-0 w-32 h-32 bg-[#129AF2]/5 rounded-full -translate-y-1/2 translate-x-1/2"></div>

                            <h2 className="text-xl font-bold text-slate-800 tracking-tight relative z-10">Tóm tắt đơn hàng</h2>

                            <div className="space-y-6 relative z-10">
                                <div className="flex gap-4">
                                    <div className="w-20 h-20 rounded-2xl overflow-hidden bg-slate-100 flex-shrink-0 shadow-sm">
                                        <img src={item.tour.thumbnailUrl} alt={item.tour.name} className="w-full h-full object-cover" />
                                    </div>
                                    <div className="flex flex-col justify-center gap-2 min-w-0">
                                        <h4 className="font-bold text-slate-800 text-sm line-clamp-2 leading-snug">{item.tour.name}</h4>
                                        <div className="flex flex-col gap-1.5">
                                            <p className="text-[12px] text-slate-600">SL: {item.quantity}</p>
                                            <div className="flex flex-col">
                                                <p className="text-sm font-bold text-[#129AF2]">
                                                    {formatPrice((item.tour.finalPrice !== undefined && item.tour.finalPrice !== null
                                                        ? item.tour.finalPrice
                                                        : (item.tour.price || 0) * (1 - (item.tour.discount || 0) / 100)) * item.quantity)}
                                                </p>
                                                {item.tour.discount > 0 && (
                                                    <p className="text-[10px] text-slate-300 line-through">
                                                        {formatPrice((item.tour.price || 0) * item.quantity)}
                                                    </p>
                                                )}
                                            </div>
                                        </div>
                                    </div>
                                </div>

                                {selectedAddons.length > 0 && (
                                    <div className="space-y-2 pt-4 border-t-2 border-slate-100">
                                        {selectedAddons.map(sa => {
                                            const addon = availableAddons.find(a => a.id === sa.id);
                                            if (!addon) return null;
                                            return (
                                                <div key={sa.id} className="flex justify-between items-center text-sm font-medium">
                                                    <span className="text-slate-600">{addon.name} (x{sa.quantity})</span>
                                                    <span className="text-slate-800">{formatPrice(addon.price * sa.quantity)}</span>
                                                </div>
                                            );
                                        })}
                                    </div>
                                )}
                            </div>

                            <div className="space-y-4 pt-6 border-t-2 border-slate-200 relative z-10">
                                <div className="flex justify-between font-bold text-slate-800 text-lg pt-1">
                                    <span>Tổng cộng</span>
                                    <span className="text-[#129AF2] font-bold">{formatPrice(grandTotal)}</span>
                                </div>
                            </div>

                            <button
                                onClick={handleSubmit}
                                disabled={isSubmitting}
                                className={`w-full py-3.5 rounded-xl font-bold text-sm transition-all flex items-center justify-center gap-3 group relative overflow-hidden ${isSubmitting
                                    ? 'bg-slate-100 text-slate-400 cursor-not-allowed'
                                    : 'bg-[#129AF2] text-white hover:bg-[#0f84cf] shadow-[0_10px_20px_rgba(18,154,242,0.2)]'
                                    }`}
                            >
                                {isSubmitting ? (
                                    <>
                                        <div className="animate-spin rounded-full h-4 w-4 border-2 border-slate-300 border-t-slate-500"></div>
                                        Đang xử lý...
                                    </>
                                ) : (
                                    <>
                                        Thanh toán ngay
                                        <FaChevronRight className="group-hover:translate-x-1 transition-transform text-[10px]" />
                                    </>
                                )}
                            </button>

                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default CheckoutPage;

