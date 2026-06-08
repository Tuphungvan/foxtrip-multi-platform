import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useCartStore } from '../../store/useCartStore';
import { useAuthStore } from '../../store/useAuthStore';
import { FaTrash, FaPlus, FaMinus, FaChevronRight, FaShoppingCart, FaArrowLeft, FaClock, FaCheckCircle, FaCheck } from 'react-icons/fa';

const CartPage = () => {
    const { items, removeItem, updateQuantity } = useCartStore();
    const { isAuthenticated } = useAuthStore();
    const navigate = useNavigate();
    const [selectedItemId, setSelectedItemId] = useState(null);

    useEffect(() => {
        if (items.length > 0 && !selectedItemId) {
            setSelectedItemId(items[0].tourId);
        }
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    const formatPrice = (price) => {
        return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(price);
    };

    const selectedItem = items.find(item => item.tourId === selectedItemId);
    const getSelectedTotal = () => {
        if (!selectedItem) return 0;
        const tour = selectedItem.tour;
        const price = tour.price || 0;
        const discount = tour.discount || 0;
        // Ưu tiên dùng finalPrice nếu có (từ Detail), nếu không (từ Cart) thì tự tính theo discount
        const finalPrice = tour.finalPrice !== undefined && tour.finalPrice !== null
            ? tour.finalPrice
            : price * (1 - discount / 100);
            
        return finalPrice * selectedItem.quantity;
    };

    const getSelectedSubtotal = () => {
        if (!selectedItem) return 0;
        return (selectedItem.tour.price || 0) * selectedItem.quantity;
    };

    const getSelectedDiscount = () => {
        return getSelectedSubtotal() - getSelectedTotal();
    };

    if (items.length === 0) {
        return (
            <div className="min-h-[60vh] flex flex-col items-center justify-center p-8 text-center space-y-6 animate-fade-in">
                <div className="w-24 h-24 bg-slate-100 rounded-[24px] flex items-center justify-center text-slate-300 transform -rotate-12">
                    <FaShoppingCart className="text-4xl" />
                </div>
                <div className="space-y-2">
                    <h2 className="text-3xl font-bold text-slate-800 tracking-tight">Giỏ hàng của bạn đang trống</h2>
                    <p className="text-slate-500 font-medium max-w-sm mx-auto text-sm">Hãy khám phá những hành trình tuyệt vời và thêm chúng vào giỏ hàng của bạn ngay hôm nay.</p>
                </div>
                <Link to="/" className="px-8 py-3 bg-[#129AF2] text-white font-bold rounded-xl hover:bg-[#0f84cf] transition-all shadow-[0_5px_15px_rgba(18,154,242,0.3)] text-sm">
                    Khám phá Tour ngay
                </Link>
            </div>
        );
    }

    const handleCheckout = () => {
        if (!selectedItemId) return;
        navigate('/checkout', { state: { selectedItemId } });
    };

    return (
        <div className="max-w-6xl mx-auto px-4 md:px-6 py-12 sm:py-16">
            <div className="flex flex-col md:flex-row items-baseline justify-between gap-4 mb-10 border-b border-slate-100 pb-6">
                <div className="flex items-baseline gap-2">
                    <h1 className="text-xl font-bold text-slate-800">Giỏ hàng</h1>
                    <span className="text-slate-600 text-[13px] font-medium">({items.length} tour)</span>
                    <span className="text-slate-400 text-[13px] font-medium ml-2">— Hãy chọn 1 tour để thanh toán</span>
                </div>
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
                {/* List Items */}
                <div className="lg:col-span-2 space-y-6">
                    {items.map((item) => {
                        const isSelected = selectedItemId === item.tourId;
                        return (
                            <div 
                                key={item.tourId} 
                                onClick={() => setSelectedItemId(isSelected ? null : item.tourId)}
                                className={`bg-white p-4 rounded-2xl border transition-all cursor-pointer flex gap-5 ${
                                    isSelected 
                                        ? 'border-[#129AF2] ring-1 ring-[#129AF2]' 
                                        : 'border-slate-100 hover:border-slate-200 shadow-sm'
                                }`}
                            >
                                {/* Thumbnail */}
                                <div className="w-32 h-24 rounded-xl overflow-hidden bg-slate-50 flex-shrink-0">
                                    <img 
                                        src={item.tour.thumbnailUrl || 'https://images.unsplash.com/photo-1528127269322-539801943592?q=80&w=2070&auto=format&fit=crop'} 
                                        alt={item.tour.name}
                                        className="w-full h-full object-cover"
                                    />
                                </div>

                                {/* Info */}
                                <div className="flex-1 min-w-0 flex flex-col justify-between">
                                    <div className="flex justify-between items-start gap-4">
                                        <h3 
                                            onClick={(e) => {
                                                e.stopPropagation();
                                                navigate(`/tour/${item.tour.slug}`);
                                            }}
                                            className="text-sm font-bold text-slate-800 hover:text-[#129AF2] transition-colors line-clamp-1 cursor-pointer"
                                        >
                                            {item.tour.name}
                                        </h3>
                                        <button 
                                            onClick={(e) => {
                                                e.stopPropagation();
                                                removeItem(item.tourId, isAuthenticated);
                                            }}
                                            className="text-slate-300 hover:text-red-500 transition-colors p-1"
                                        >
                                            <FaTrash className="text-xs" />
                                        </button>
                                    </div>
                                    
                                    <div className="flex flex-col gap-1">
                                        <div className="flex items-center gap-1.5 text-[11px] font-medium text-slate-500">
                                            <FaClock className="text-[#129AF2] text-[10px]" />
                                            <span>
                                                {item.tour.startDate ? new Date(item.tour.startDate).toLocaleDateString('vi-VN') : 'N/A'} 
                                                {item.tour.endDate ? ` — ${new Date(item.tour.endDate).toLocaleDateString('vi-VN')}` : ''}
                                            </span>
                                        </div>
                                    </div>

                                    <div className="flex items-center justify-between pt-2">
                                        {/* Quantity Selector */}
                                        <div className="flex items-center gap-3 bg-slate-50 p-0.5 rounded-lg border border-slate-100" onClick={e => e.stopPropagation()}>
                                            <button 
                                                onClick={() => updateQuantity(item.tourId, item.quantity - 1, isAuthenticated)}
                                                className="w-6 h-6 rounded-md bg-white flex items-center justify-center text-slate-500 hover:text-[#129AF2] shadow-sm transition-all"
                                            >
                                                <FaMinus className="text-[8px]" />
                                            </button>
                                            <span className="text-xs font-bold text-slate-700 w-3 text-center">{item.quantity}</span>
                                            <button 
                                                onClick={() => updateQuantity(item.tourId, item.quantity + 1, isAuthenticated)}
                                                className="w-6 h-6 rounded-md bg-white flex items-center justify-center text-slate-500 hover:text-[#129AF2] shadow-sm transition-all"
                                            >
                                                <FaPlus className="text-[8px]" />
                                            </button>
                                        </div>

                                        <div className="text-right">
                                            <div className="flex flex-col items-end">
                                                <p className="text-sm font-bold text-[#129AF2]">
                                                    {formatPrice((item.tour.finalPrice !== undefined && item.tour.finalPrice !== null 
                                                        ? item.tour.finalPrice 
                                                        : (item.tour.price || 0) * (1 - (item.tour.discount || 0) / 100)) * item.quantity)}
                                                </p>
                                                {item.tour.discount > 0 && (
                                                    <p className="text-[10px] text-slate-300 line-through font-bold">
                                                        {formatPrice((item.tour.price || 0) * item.quantity)}
                                                    </p>
                                                )}
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        );
                    })}
                </div>

                {/* Summary Card */}
                <div className="lg:col-span-1">
                    <div className="sticky top-24 space-y-6">
                        <div className="bg-white p-6 rounded-2xl border border-slate-100 shadow-xl space-y-5">
                            <h2 className="text-lg font-bold text-slate-800">Tóm tắt thanh toán</h2>
                            
                            <div className="space-y-3 border-b border-slate-50 pb-5 text-[13px]">
                                <div className="flex justify-between font-medium text-slate-500">
                                    <span>Tạm tính</span>
                                    <span>{formatPrice(getSelectedSubtotal())}</span>
                                </div>
                                <div className="flex justify-between font-medium text-slate-500">
                                    <span>Giảm giá</span>
                                    <span className="text-green-500">-{formatPrice(getSelectedDiscount())}</span>
                                </div>
                            </div>

                            <div className="flex flex-col gap-0.5">
                                <p className="text-[10px] font-bold text-slate-400 uppercase tracking-widest">Tổng thanh toán</p>
                                <p className="text-2xl font-bold text-[#129AF2]">{formatPrice(getSelectedTotal())}</p>
                            </div>

                            <button 
                                onClick={handleCheckout}
                                disabled={!selectedItemId}
                                className={`w-full py-3.5 rounded-xl font-bold text-sm transition-all flex items-center justify-center gap-2 group ${
                                    selectedItemId 
                                        ? 'bg-[#129AF2] text-white hover:bg-[#0f84cf] shadow-[0_5px_15px_rgba(18,154,242,0.3)]' 
                                        : 'bg-slate-100 text-slate-400 cursor-not-allowed'
                                }`}
                            >
                                Tiến hành đặt hàng
                                <FaChevronRight className="group-hover:translate-x-1 transition-transform text-[10px]" />
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default CartPage;

