import React, { useState, useEffect, useRef } from 'react';
import { tourService } from '../../services/api/tourService';
import TourCard from '../../components/user/TourCard';
import {
    FaSearch, FaMapMarkerAlt, FaCalendarAlt, FaStar,
    FaArrowLeft, FaArrowRight, FaTree, FaSpa, FaLandmark, FaBorderAll,
    FaFilter, FaMoneyBillWave, FaTicketAlt, FaHotel, FaChevronLeft, FaChevronRight
} from 'react-icons/fa';
import {
    MdOutlineCardGiftcard, MdOutlineExplore,
    MdOutlineVerifiedUser, MdOutlineSupportAgent
} from 'react-icons/md';
import { PROVINCES, TOUR_CATEGORIES } from '../../utils/constants';
import { useNavigate } from 'react-router-dom';

const formatVND = (value) => {
    if (!value) return '';
    return value.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ".");
};

const parseVND = (value) => {
    return value.replace(/\./g, '');
};

const HomePage = () => {

    const navigate = useNavigate();
    const [discountedTours, setDiscountedTours] = useState([]);
    const [featuredTours, setFeaturedTours] = useState([]);
    const [isLoading, setIsLoading] = useState(true);

    // Slider state
    const [currentSlide, setCurrentSlide] = useState(1);
    const [isTransitioning, setIsTransitioning] = useState(true);

    const heroImages = [
        "/images/thum2.jpg",
        "/images/thum1.jpg",
        "/images/thum2.jpg",
        "/images/thum1.jpg"
    ];

    const timerRef = useRef(null);
    const isMoving = useRef(false);

    const startTimer = () => {
        stopTimer();
        timerRef.current = setInterval(() => {
            nextSlide();
        }, 6000);
    };

    const stopTimer = () => {
        if (timerRef.current) {
            clearInterval(timerRef.current);
            timerRef.current = null;
        }
    };

    useEffect(() => {
        startTimer();
        return () => stopTimer();
    }, []);

    const nextSlide = () => {
        if (isMoving.current) return;
        isMoving.current = true;
        setIsTransitioning(true);
        setCurrentSlide(prev => prev + 1);
        startTimer(); // Reset timer on manual or auto move
    };

    const prevSlide = () => {
        if (isMoving.current) return;
        isMoving.current = true;
        setIsTransitioning(true);
        setCurrentSlide(prev => prev - 1);
        startTimer(); // Reset timer on manual move
    };

    const handleTransitionEnd = () => {
        isMoving.current = false;
        if (currentSlide >= heroImages.length - 1) {
            setIsTransitioning(false);
            setCurrentSlide(1);
        } else if (currentSlide <= 0) {
            setIsTransitioning(false);
            setCurrentSlide(heroImages.length - 2);
        }
    };

    // Pagination for Featured Tours


    // Search & Filter State
    const [searchQuery, setSearchQuery] = useState('');
    const [filters, setFilters] = useState({
        province: '',
        category: '',
        priceFrom: '',
        priceTo: '',
        startDate: '',
        endDate: ''
    });

    useEffect(() => {
        fetchData();
    }, []);

    const fetchData = async () => {
        setIsLoading(true);
        try {
            const [discountedRes, upcomingRes] = await Promise.all([
                tourService.getDiscountedTours(12),
                tourService.getUpcomingTours(12)
            ]);
            setDiscountedTours(discountedRes.data || []);
            setFeaturedTours(upcomingRes.data || []);
        } catch (error) {
            console.error('Error fetching home data:', error);
        } finally {
            setIsLoading(false);
        }
    };

    const handleSearch = (e) => {
        if (e) e.preventDefault();

        const params = new URLSearchParams();
        if (searchQuery) params.append('q', searchQuery);
        if (filters.province) params.append('province', filters.province);
        if (filters.category) params.append('category', filters.category);
        if (filters.priceFrom) params.append('priceFrom', filters.priceFrom);
        if (filters.priceTo) params.append('priceTo', filters.priceTo);
        if (filters.startDate) params.append('startDate', filters.startDate);
        if (filters.endDate) {
            // Append time to endDate to ensure tours ending late in the day are included
            params.append('endDate', `${filters.endDate}T23:59:59`);
        }

        navigate(`/search?${params.toString()}`);
    };

    const handleCategoryClick = (categoryId) => {
        const categoryValue = categoryId === 'ALL' ? '' : categoryId;
        if (!categoryValue) {
            navigate('/search');
        } else {
            navigate(`/search?category=${categoryValue}`);
        }
    };



    const categories = [
        { id: 'CULTURE', name: TOUR_CATEGORIES.CULTURE, icon: <img src="/images/culture.png" className="w-12 h-12 object-contain" /> },
        { id: 'NATURE', name: TOUR_CATEGORIES.NATURE, icon: <img src="/images/natural.png" className="w-12 h-12 object-contain" /> },
        { id: 'SEA', name: TOUR_CATEGORIES.SEA, icon: <img src="/images/sea.png" className="w-12 h-12 object-contain" /> },
        { id: 'RELAX', name: TOUR_CATEGORIES.RELAX, icon: <img src="/images/relax.png" className="w-12 h-12 object-contain" /> },
    ];

    return (
        <div className="w-full flex flex-col bg-white font-sans">
            <section className="relative w-full h-[520px] overflow-hidden group">
                <div
                    onTransitionEnd={handleTransitionEnd}
                    className="flex h-full"
                    style={{
                        transform: `translateX(-${currentSlide * 100}%)`,
                        transition: isTransitioning
                            ? 'transform 1s ease-in-out'
                            : 'none'
                    }}
                >
                    {heroImages.map((img, idx) => (
                        <div key={idx} className="min-w-full h-full">
                            <img
                                src={img}
                                alt={`Hero Banner ${idx}`}
                                className="w-full h-full object-cover"
                            />
                        </div>
                    ))}
                </div>

                <div className="absolute inset-0 bg-black/20"></div>

                <div className="absolute inset-0 flex flex-col items-start justify-center text-left px-6 md:px-20 lg:px-40 z-10">
                    <h1 className="text-5xl md:text-6xl font-bold text-white mb-4 tracking-tight drop-shadow-lg">
                        Trải nghiệm của niềm vui
                    </h1>

                    <p className="text-white font-normal mb-10 drop-shadow-md text-lg opacity-90 max-w-xl">
                        Từ những chuyến nghỉ dưỡng gần nhà đến những cuộc phiêu lưu xa xôi, hãy tìm kiếm điều khiến bạn hạnh phúc mọi lúc, mọi nơi.
                    </p>

                    <form onSubmit={handleSearch} className="w-full max-w-3xl bg-white p-1 rounded-xl shadow-2xl flex items-center gap-1">
                        <div className="flex-1 flex items-center px-4 gap-3">
                            <FaSearch className="text-slate-300 text-lg" />

                            <input
                                type="text"
                                placeholder="Bạn muốn đi đâu chơi?"
                                className="w-full py-3.5 text-sm font-normal text-slate-800 placeholder:text-slate-400 border-none focus:ring-0"
                                value={searchQuery}
                                onChange={(e) => setSearchQuery(e.target.value)}
                            />
                        </div>

                        <button
                            type="submit"
                            className="bg-[#129AF2] text-white px-10 py-3.5 rounded-lg font-bold text-sm transition-all shadow-lg"
                        >
                            Tìm kiếm
                        </button>
                    </form>
                </div>

                <button
                    onClick={prevSlide}
                    className="absolute left-6 top-1/2 -translate-y-1/2 w-12 h-12 rounded-full bg-white/20 backdrop-blur-md text-white flex items-center justify-center opacity-0 group-hover:opacity-100 transition-all hover:bg-white hover:text-slate-800 z-20"
                >
                    <FaChevronLeft className="text-xl" />
                </button>

                <button
                    onClick={nextSlide}
                    className="absolute right-6 top-1/2 -translate-y-1/2 w-12 h-12 rounded-full bg-white/20 backdrop-blur-md text-white flex items-center justify-center opacity-0 group-hover:opacity-100 transition-all hover:bg-white hover:text-slate-800 z-20"
                >
                    <FaChevronRight className="text-xl" />
                </button>
            </section>

            <div className="max-w-[1200px] mx-auto w-full px-4 md:px-6">
                <section className="py-10">
                    <h2 className="text-2xl font-bold text-slate-800 tracking-tight mb-8">Bạn thích thể loại nào?</h2>
                    <div className="flex flex-wrap justify-center gap-20">
                        {categories.map((cat) => (
                            <button
                                key={cat.id}
                                onClick={() => handleCategoryClick(cat.id)}
                                className="flex flex-col items-center justify-center gap-2.5 p-5 w-40 h-32 rounded-[20px] bg-white border border-slate-300 hover:shadow-xl hover:shadow-slate-200 transition-all group"
                            >
                                <div className="w-10 h-10 flex items-center justify-center" style={{ color: cat.color }}>
                                    {cat.icon}
                                </div>

                                <span className="text-[13px] font-bold text-slate-700 text-center leading-tight">
                                    {cat.name}
                                </span>
                            </button>
                        ))}
                    </div>
                </section>


                {/* Discounted Tours */}
                <section className="py-10">
                    <h2 className="text-2xl font-bold text-slate-800 tracking-tight mb-8">Ưu đãi cho bạn</h2>

                    {isLoading ? (
                        <div className="grid grid-cols-2 md:grid-cols-4 gap-6">
                            {[1, 2, 3, 4].map(i => (
                                <div key={i} className="aspect-[4/5] bg-slate-50 rounded-2xl animate-pulse"></div>
                            ))}
                        </div>
                    ) : (
                        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
                            {discountedTours.map(tour => (
                                <TourCard key={tour.id} tour={tour} />
                            ))}
                        </div>
                    )}
                </section>

                {/* Đi đâu tiếp theo? Section - Complete Filters */}
                <section className="py-10">
                    <h2 className="text-2xl font-bold text-slate-800 tracking-tight mb-8">Đi đâu tiếp theo?</h2>
                    <div className="bg-white rounded-[40px] p-8 md:p-10 border border-slate-200 shadow-[0_10px_40px_rgba(0,0,0,0.04)]">
                        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
                            {/* Province */}
                            <div className="space-y-3">
                                <label className="text-[11px] font-bold text-slate-400 uppercase tracking-widest flex items-center gap-2">
                                    <FaMapMarkerAlt className="text-[#FF9E69]" /> Địa điểm
                                </label>
                                <div className="relative">
                                    <select
                                        className="w-full bg-slate-50 border-none rounded-2xl px-5 py-4 text-sm font-bold text-slate-700 focus:ring-2 focus:ring-[#129AF2]/20 transition-all appearance-none cursor-pointer"
                                        value={filters.province}
                                        onChange={(e) => setFilters(prev => ({ ...prev, province: e.target.value }))}
                                    >
                                        <option value="">Tất cả địa điểm</option>
                                        {Object.entries(PROVINCES).map(([key, value]) => (
                                            <option key={key} value={key}>{value}</option>
                                        ))}
                                    </select>
                                    <div className="absolute right-5 top-1/2 -translate-y-1/2 pointer-events-none text-slate-400">
                                        <FaChevronRight className="rotate-90 text-xs" />
                                    </div>
                                </div>
                            </div>

                            {/* Price From */}
                            <div className="space-y-3">
                                <label className="text-[11px] font-bold text-slate-400 uppercase tracking-widest flex items-center gap-2">
                                    <FaMoneyBillWave className="text-[#FF9E69]" /> Giá từ
                                </label>
                                <input
                                    type="text"
                                    placeholder="VND"
                                    className="w-full bg-slate-50 border-none rounded-2xl px-5 py-4 text-sm font-bold text-slate-700 focus:ring-2 focus:ring-[#129AF2]/20 transition-all"
                                    value={formatVND(filters.priceFrom)}
                                    onChange={(e) => {
                                        const rawValue = parseVND(e.target.value);
                                        if (/^\d*$/.test(rawValue)) {
                                            setFilters(prev => ({ ...prev, priceFrom: rawValue }));
                                        }
                                    }}
                                />
                            </div>

                            {/* Price To */}
                            <div className="space-y-3">
                                <label className="text-[11px] font-bold text-slate-400 uppercase tracking-widest flex items-center gap-2">
                                    <FaMoneyBillWave className="text-[#FF9E69]" /> Đến giá
                                </label>
                                <input
                                    type="text"
                                    placeholder="VND"
                                    className="w-full bg-slate-50 border-none rounded-2xl px-5 py-4 text-sm font-bold text-slate-700 focus:ring-2 focus:ring-[#129AF2]/20 transition-all"
                                    value={formatVND(filters.priceTo)}
                                    onChange={(e) => {
                                        const rawValue = parseVND(e.target.value);
                                        if (/^\d*$/.test(rawValue)) {
                                            setFilters(prev => ({ ...prev, priceTo: rawValue }));
                                        }
                                    }}
                                />
                            </div>

                            {/* Start Date */}
                            <div className="space-y-3">
                                <label className="text-[11px] font-bold text-slate-400 uppercase tracking-widest flex items-center gap-2">
                                    <FaCalendarAlt className="text-[#FF9E69]" /> Ngày bắt đầu
                                </label>
                                <input
                                    type="date"
                                    className="w-full bg-slate-50 border-none rounded-2xl px-5 py-4 text-sm font-bold text-slate-700 focus:ring-2 focus:ring-[#129AF2]/20 transition-all"
                                    value={filters.startDate}
                                    onChange={(e) => setFilters(prev => ({ ...prev, startDate: e.target.value }))}
                                />
                            </div>

                            {/* End Date */}
                            <div className="space-y-3">
                                <label className="text-[11px] font-bold text-slate-400 uppercase tracking-widest flex items-center gap-2">
                                    <FaCalendarAlt className="text-[#FF9E69]" /> Ngày kết thúc
                                </label>
                                <input
                                    type="date"
                                    className="w-full bg-slate-50 border-none rounded-2xl px-5 py-4 text-sm font-bold text-slate-700 focus:ring-2 focus:ring-[#129AF2]/20 transition-all"
                                    value={filters.endDate}
                                    onChange={(e) => setFilters(prev => ({ ...prev, endDate: e.target.value }))}
                                />
                            </div>

                            <div className="flex items-end">
                                <button
                                    onClick={handleSearch}
                                    className="w-full bg-[#129AF2] text-white py-4 rounded-2xl font-bold text-sm transition-all flex items-center justify-center gap-2 shadow-xl"
                                >
                                    Khám phá ngay
                                </button>
                            </div>
                        </div>
                    </div>
                </section>

                {/* Featured Tours */}
                <section className="py-10">
                    <h2 className="text-2xl font-bold text-slate-800 tracking-tight mb-8">Các tour nổi bật</h2>

                    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
                        {isLoading ? (
                            [1, 2, 3, 4, 5, 6, 7, 8].map(i => (
                                <div key={i} className="h-80 bg-slate-50 rounded-2xl animate-pulse"></div>
                            ))
                        ) : (
                            featuredTours.map(tour => (
                                <TourCard key={tour.id} tour={tour} />
                            ))
                        )}
                    </div>
                </section>
            </div>


            {/* App Download Section */}
            <section id="app-download" className="py-10 bg-white">
                <div className="max-w-[1200px] mx-auto px-4 md:px-6">
                    <h2 className="text-2xl font-bold text-slate-800 tracking-tight mb-8">Khám phá nhiều hơn nữa</h2>
                    <div className="w-full rounded-[48px] overflow-hidden shadow-2xl">
                        <img
                            src="/images/installapp.png"
                            alt="Install FoxTrip App"
                            className="w-full h-auto object-cover block"
                        />
                    </div>
                </div>
            </section>

            {/* Why Choose Us */}
            <section className="py-10 max-w-[1200px] mx-auto px-4 md:px-6">
                <h2 className="text-2xl font-bold text-slate-800 mb-8 text-center md:text-left">Vì sao bạn nên chọn foxtrip?</h2>
                <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-12">
                    {[
                        { title: 'Đa dạng lựa chọn', desc: 'Các tour du lịch phong phú với nhiều điểm tham quan thú vị không thể bỏ lỡ.', icon: "/images/possibilities.png" },
                        { title: 'Chơi vui, giá tốt', desc: 'Trải nghiệm chất lượng với mức giá tốt, nhiều ưu đãi thường xuyên.', icon: "/images/vouchers.png" },
                        { title: 'Dễ dàng và tiện lợi', desc: 'Thao tác nhanh gọn, tiện lợi trên thiết bị cầm tay. Mang lại cảm hứng vô tận cho khách hàng.', icon: "/images/phone.png" },
                        { title: 'Đáng tin cậy', desc: 'Tham khảo đánh giá chân thực. Dịch vụ hỗ trợ tận tình, đồng hành cùng bạn mọi lúc, mọi nơi.', icon: "/images/review.png" },
                    ].map((item, idx) => (
                        <div key={idx} className="space-y-4">
                            <div className="w-16 h-16 rounded-2xl bg-white flex items-center justify-center overflow-hidden">
                                <img src={item.icon} alt={item.title} className="w-full h-full object-contain" />
                            </div>
                            <h4 className="text-lg font-bold text-slate-800 tracking-tight">{item.title}</h4>
                            <p className="text-black text-sm font-normal leading-relaxed">{item.desc}</p>
                        </div>
                    ))}
                </div>
            </section>
        </div>
    );
};

export default HomePage;

