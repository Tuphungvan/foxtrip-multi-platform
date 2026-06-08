import React, { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { tourService } from '../../services/api/tourService';
import { useCartStore } from '../../store/useCartStore';
import { useAuthStore } from '../../store/useAuthStore';
import { useUIStore } from '../../store/useUIStore';
import { FaStar, FaClock, FaMapMarkerAlt, FaCheckCircle, FaChevronRight, FaPlus, FaMinus, FaShoppingCart, FaUserTie, FaCheck, FaInfoCircle, FaCalendarAlt, FaUsers, FaEnvelope, FaPhone } from 'react-icons/fa';
import { toast } from 'react-hot-toast';
import { PROVINCES, TOUR_CATEGORIES } from '../../utils/constants';
import { formatToVN } from '../../utils/dateUtils';

const TourDetailPage = () => {
    const { slug } = useParams();
    const [tour, setTour] = useState(null);
    const [reviews, setReviews] = useState([]);
    const [isLoading, setIsLoading] = useState(true);
    const [quantity, setQuantity] = useState(1);
    const [selectedAddons, setSelectedAddons] = useState([]);
    const addItem = useCartStore(state => state.addItem);
    const { isAuthenticated } = useAuthStore();
    const { openAuthModal } = useUIStore();

    useEffect(() => {
        fetchTourDetail();
    }, [slug]);

    const fetchTourDetail = async () => {
        setIsLoading(true);
        try {
            const res = await tourService.getTourDetail(slug);
            const tourData = res.data; // Accessing .data based on axiosClient returning response.data
            setTour(tourData);

            // Fetch reviews if tour exists
            if (tourData && tourData.id) {
                const reviewsRes = await tourService.getTourReviews(tourData.id, { size: 10 });
                // ApiResponse<PageData<TourReviewDTO>> -> PageData has 'items'
                setReviews(reviewsRes.data.items || []);
            }
        } catch (error) {
            console.error('Error fetching tour detail:', error);
            toast.error('Không thể tải thông tin tour');
        } finally {
            setIsLoading(false);
        }
    };

    const handleAddtoCart = () => {
        if (!tour) return;
        addItem(tour, quantity, selectedAddons, isAuthenticated);
        toast.success(`Đã thêm ${quantity} tour vào giỏ hàng`);
    };

    if (isLoading) return (
        <div className="min-h-screen flex items-center justify-center bg-white">
            <div className="animate-spin rounded-full h-12 w-12 border-4 border-slate-100 border-t-[#129AF2]"></div>
        </div>
    );

    if (!tour) return <div className="min-h-screen flex items-center justify-center text-2xl font-bold bg-white text-slate-800">Tour không tồn tại</div>;

    const formatPrice = (price) => {
        return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(price);
    };

    let durationStr = 'N/A';
    if (tour.startDate && tour.endDate) {
        const start = new Date(tour.startDate);
        const end = new Date(tour.endDate);
        const diffTime = Math.abs(end - start);
        const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
        const nights = Math.max(0, diffDays - 1);
        durationStr = `${diffDays} ngày ${nights} đêm`;
    }

    const allImages = [];
    if (tour.thumbnailUrl) allImages.push(tour.thumbnailUrl);
    if (tour.itineraries) {
        tour.itineraries.forEach(item => {
            if (item.locationImageUrl && !allImages.includes(item.locationImageUrl)) {
                allImages.push(item.locationImageUrl);
            }
        });
    }

    const groupedItineraries = [];
    if (tour.itineraries && tour.itineraries.length > 0) {
        const dayMap = {};
        tour.itineraries.forEach(item => {
            if (!dayMap[item.dayNumber]) dayMap[item.dayNumber] = [];
            dayMap[item.dayNumber].push(item);
        });
        Object.keys(dayMap).sort((a, b) => a - b).forEach(dayNum => {
            groupedItineraries.push({
                dayNumber: dayNum,
                activities: dayMap[dayNum].sort((a, b) => a.position - b.position)
            });
        });
    }

    return (
        <div className="w-full bg-white pb-20">
            {/* Breadcrumb Area */}
            <div className="max-w-6xl mx-auto px-4 md:px-6 pt-6">
                <div className="flex items-center gap-2 text-xs text-slate-400 font-bold mb-4">
                    <Link to="/" className="hover:text-[#129AF2] transition-colors">Trang chủ</Link>
                    <FaChevronRight className="text-[8px]" />
                    <span className="text-slate-800">{tour.name}</span>
                </div>

                <h1 className="text-2xl md:text-3xl font-bold text-slate-800 tracking-tight leading-tight mb-4">
                    {tour.name}
                </h1>

                <div className="flex flex-wrap items-center gap-6 mb-4 text-sm font-bold text-slate-800">
                    <div
                        className="flex items-center gap-1.5 cursor-pointer hover:opacity-80 transition-opacity"
                        onClick={() => document.getElementById('reviews-section')?.scrollIntoView({ behavior: 'smooth' })}
                    >
                        <FaStar className="text-yellow-400" />
                        <span className="font-bold">{tour.averageRating?.toFixed(1) || '5.0'}</span>
                        <span className="text-slate-500 font-medium">({tour.reviewCount || 0})</span>
                    </div>
                    <div className="flex items-center gap-1.5">
                        <span className="font-medium">{PROVINCES[tour.province] || tour.province}</span>
                    </div>
                    <div className="flex items-center gap-1.5">
                        <span className="font-medium">{TOUR_CATEGORIES[tour.category] || tour.category}</span>
                    </div>
                </div>

                {/* Info Tags */}
                <div className="flex flex-wrap gap-2 mb-6">
                    <div className="px-3 py-1.5 bg-slate-50 rounded text-[13px] font-medium text-slate-600">
                        Thời gian {durationStr}
                    </div>
                    <div className="px-3 py-1.5 bg-slate-50 rounded text-[13px] font-medium text-slate-600">
                        Khởi hành {tour.startDate ? formatToVN(tour.startDate, false) : 'Sắp tới'}
                    </div>
                    <div className="px-3 py-1.5 bg-slate-50 rounded text-[13px] font-medium text-slate-600">
                        Kết thúc {tour.endDate ? formatToVN(tour.endDate, false) : 'Sắp tới'}
                    </div>
                    {tour.guide && (
                        <div className="px-3 py-1.5 bg-slate-50 rounded text-[13px] font-medium text-slate-600">
                            Có hướng dẫn viên
                        </div>
                    )}
                </div>

                {/* Gallery */}
                <div className="relative rounded-2xl overflow-hidden h-[50vh] min-h-[400px] flex gap-2">
                    <div className="w-full md:w-2/3 h-full">
                        <img
                            src={allImages[0] || 'https://images.unsplash.com/photo-1528127269322-539801943592?q=80&w=2070'}
                            className="w-full h-full object-cover"
                            alt="Main"
                        />
                    </div>
                    <div className="hidden md:grid w-1/3 h-full grid-cols-1 grid-rows-2 gap-2">
                        {[1, 2].map(idx => (
                            <div key={idx} className="h-full w-full overflow-hidden">
                                <img
                                    src={allImages[idx] || allImages[0]}
                                    className="w-full h-full object-cover"
                                    alt={`Gallery ${idx}`}
                                />
                            </div>
                        ))}
                    </div>
                </div>
            </div>

            <div className="max-w-6xl mx-auto px-4 md:px-6 py-10 grid grid-cols-1 lg:grid-cols-3 gap-12">
                {/* Left Column */}
                <div className="lg:col-span-2 space-y-12">

                    {/* Removed Info Cards Grid */}

                    <div className="space-y-4">
                        <h2 className="text-xl font-bold text-slate-800 flex items-center gap-3">
                            <span className="w-1.5 h-6 bg-[#129AF2] rounded-full"></span>
                            Những điều cần biết
                        </h2>
                        <div className="text-slate-600 font-medium leading-relaxed whitespace-pre-wrap text-[15px]">
                            {tour.description}
                        </div>
                    </div>

                    <div className="space-y-8">
                        <h2 className="text-xl font-bold text-slate-800 flex items-center gap-3">
                            <span className="w-1.5 h-6 bg-[#129AF2] rounded-full"></span>
                            Lịch trình chi tiết
                        </h2>
                        <div className="space-y-0">
                            {groupedItineraries.map((day, idx) => (
                                <div key={idx} className="relative pl-8 pb-10 last:pb-0">
                                    <div className="absolute left-0 top-1 w-3 h-3 rounded-full bg-[#129AF2]"></div>
                                    <div className="space-y-6">
                                        <div>
                                            <h3 className="text-base font-bold text-slate-800">Ngày {day.dayNumber}</h3>
                                        </div>
                                        <div className="space-y-8">
                                            {day.activities.map((act, aIdx) => (
                                                <div key={aIdx} className="flex flex-col gap-4">
                                                    <div className="flex-1">
                                                        <p className="text-slate-800 font-medium text-[15px] leading-relaxed">
                                                            {act.activity}
                                                        </p>
                                                    </div>
                                                    {act.locationImageUrl && (
                                                        <div className="rounded-xl overflow-hidden">
                                                            <img src={act.locationImageUrl} alt={act.locationName} className="w-full h-72 object-cover" />
                                                        </div>
                                                    )}
                                                </div>
                                            ))}
                                        </div>
                                    </div>
                                </div>
                            ))}
                        </div>
                    </div>

                    {/* Guide Info */}
                    {tour.guide && (
                        <div className="space-y-6">
                            <h2 className="text-xl font-bold text-slate-800 flex items-center gap-3">
                                <span className="w-1.5 h-6 bg-[#129AF2] rounded-full"></span>
                                Hướng dẫn viên
                            </h2>
                            <div className="p-4 rounded-xl flex items-center gap-4 max-w-md" style={{ backgroundColor: '#FFF0E5' }}>
                                {tour.guide.avatarUrl ? (
                                    <img
                                        src={tour.guide.avatarUrl}
                                        className="w-12 h-12 rounded-full object-cover shrink-0"
                                        alt={tour.guide.username}
                                    />
                                ) : (
                                    <div className="w-12 h-12 rounded-full bg-[#129AF2]/10 flex items-center justify-center text-[#129AF2] text-xl font-bold shrink-0">
                                        {tour.guide.username?.charAt(0).toUpperCase()}
                                    </div>
                                )}
                                <div className="min-w-0">
                                    <h4 className="text-sm font-bold text-slate-800 truncate">{tour.guide.username}</h4>
                                    <div className="flex flex-col gap-0.5 mt-0.5">
                                        <p className="text-[12px] text-slate-500 font-medium truncate flex items-center gap-1.5">
                                            <FaEnvelope className="text-slate-400 text-[10px]" /> {tour.guide.email}
                                        </p>
                                        <p className="text-[12px] text-slate-500 font-medium truncate flex items-center gap-1.5">
                                            <FaPhone className="text-slate-400 text-[10px]" /> {tour.guide.phoneNumber}
                                        </p>
                                    </div>
                                </div>
                            </div>
                        </div>
                    )}

                    {/* Reviews */}
                    <div id="reviews-section" className="space-y-8 scroll-mt-24">
                        <h2 className="text-xl font-bold text-slate-800 flex items-center gap-3">
                            <span className="w-1.5 h-6 bg-[#129AF2] rounded-full"></span>
                            Đánh giá
                        </h2>

                        {/* Review Summary */}
                        <div className="flex items-center gap-4 py-2">
                            <img
                                src={tour.averageRating >= 4 ? "/images/fantastic.png" : "/images/good.png"}
                                className="w-12 h-12 object-contain"
                                alt="rating-icon"
                            />
                            <div className="flex items-center gap-3">
                                <div className="flex items-baseline gap-0.5">
                                    <span className="text-[40px] font-black text-[#6366f1] leading-none">{tour.averageRating?.toFixed(1) || '5.0'}</span>
                                    <span className="text-slate-400 font-bold text-lg">/ 5</span>
                                </div>
                                <div className="flex flex-col">
                                    <span className="text-base font-bold text-[#6366f1] leading-tight">
                                        {tour.averageRating >= 4 ? 'Tuyệt vời' : 'Tốt'}
                                    </span>
                                    <span className="text-slate-400 font-medium text-[13px]">{tour.reviewCount || 0} đánh giá</span>
                                </div>
                            </div>
                        </div>

                        {reviews.length > 0 ? (
                            <div className="space-y-0">
                                {reviews.map((rev) => (
                                    <div key={rev.reviewId} className="py-8 border-b border-slate-100 last:border-0 space-y-4">
                                        <div className="flex items-start justify-between">
                                            <div className="flex items-center gap-3">
                                                {rev.user?.avatarUrl ? (
                                                    <img src={rev.user.avatarUrl} className="w-12 h-12 rounded-full object-cover" />
                                                ) : (
                                                    <div className="w-12 h-12 rounded-full bg-slate-100 flex items-center justify-center text-slate-400 font-bold">
                                                        {rev.user?.username?.charAt(0).toUpperCase() || 'U'}
                                                    </div>
                                                )}
                                                <div>
                                                    <p className="font-bold text-slate-800 text-[15px]">{rev.user?.username || 'Người dùng FoxTrip'}</p>
                                                    <p className="text-[12px] font-medium text-slate-400">{formatToVN(rev.createdAt, false).split(' ')[0]}</p>
                                                </div>
                                            </div>
                                            <div className="flex items-center gap-2">
                                                <span className="text-[14px] font-bold text-[#6366f1]">
                                                    {rev.rating >= 4 ? 'Tuyệt vời' : 'Tốt'}
                                                </span>
                                                <span className="px-2 py-0.5 rounded bg-[#6366f1] text-white text-[12px] font-bold">
                                                    {rev.rating.toFixed(1)}
                                                </span>
                                            </div>
                                        </div>

                                        <p className="text-[14px] text-slate-600 leading-relaxed font-medium">
                                            {rev.content}
                                        </p>
                                    </div>
                                ))}
                            </div>
                        ) : (
                            <div className="py-10 text-center space-y-2">
                                <FaStar className="text-slate-100 text-4xl mx-auto" />
                                <p className="text-slate-400 font-medium italic">Chưa có đánh giá nào cho tour này</p>
                            </div>
                        )}
                    </div>
                </div>

                {/* Right Column: Sidebar */}
                <div className="lg:col-span-1">
                    <div className="sticky top-24">
                        <div className="bg-white p-6 rounded-[24px] border border-slate-100 shadow-[0_10px_40px_rgba(0,0,0,0.04)] space-y-6">
                            <div className="flex flex-col gap-1">
                                <div className="flex items-end gap-1.5">
                                    <span className="text-3xl font-bold text-[#FF5B00]">{formatPrice(tour.finalPrice)}</span>
                                    <span className="text-[13px] text-slate-500 font-medium mb-1.5">/ khách</span>
                                </div>
                                {tour.discount > 0 && (
                                    <div className="flex items-center gap-2">
                                        <span className="text-sm text-slate-300 line-through font-medium">{formatPrice(tour.price)}</span>
                                        <span className="px-1.5 py-0.5 rounded bg-orange-50 text-[#FF5B00] text-[10px] font-bold">-{tour.discount}%</span>
                                    </div>
                                )}
                            </div>

                            <div className="space-y-4">
                                <div className="flex items-center justify-between text-[13px] text-slate-800 font-medium">
                                    <span className="text-slate-500">Số lượng người tham gia</span>
                                    <span className="text-slate-400">Còn {tour.availableSlots || 0} / {tour.slots || 0} chỗ</span>
                                </div>
                                <div className="flex items-center justify-between bg-slate-50 p-1.5 rounded-xl border border-slate-100">
                                    <button
                                        onClick={() => setQuantity(prev => Math.max(1, prev - 1))}
                                        className="w-10 h-10 rounded-lg bg-white flex items-center justify-center text-slate-600 hover:text-[#129AF2] shadow-sm transition-all"
                                    >
                                        <FaMinus className="text-[10px]" />
                                    </button>
                                    <span className="text-lg font-bold text-slate-800">{quantity}</span>
                                    <button
                                        onClick={() => setQuantity(prev => prev + 1)}
                                        className="w-10 h-10 rounded-lg bg-white flex items-center justify-center text-slate-600 hover:text-[#129AF2] shadow-sm transition-all"
                                    >
                                        <FaPlus className="text-[10px]" />
                                    </button>
                                </div>
                            </div>

                            <div className="space-y-3">
                                {isAuthenticated ? (
                                    <button
                                        onClick={handleAddtoCart}
                                        className="w-full bg-[#129AF2] hover:bg-[#0f84cf] text-white py-3.5 rounded-xl font-bold text-sm transition-all flex items-center justify-center gap-2 group"
                                    >
                                        <FaShoppingCart className="text-sm" />
                                        Thêm vào giỏ hàng
                                    </button>
                                ) : (
                                    <button
                                        onClick={() => openAuthModal('login')}
                                        className="w-full bg-slate-900 hover:bg-black text-white py-3.5 rounded-xl font-bold text-sm transition-all flex items-center justify-center gap-2"
                                    >
                                        Đăng nhập để đặt chỗ
                                    </button>
                                )}
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default TourDetailPage;
