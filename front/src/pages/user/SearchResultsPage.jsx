import React, { useState, useEffect } from 'react';
import { useSearchParams, Link } from 'react-router-dom';
import { tourService } from '../../services/api/tourService';
import TourCard from '../../components/user/TourCard';
import { FaSearch, FaMapMarkerAlt, FaCalendarAlt, FaMoneyBillWave, FaFilter } from 'react-icons/fa';
import { TOUR_CATEGORIES, PROVINCES } from '../../utils/constants';
import Pagination from '../../components/ui/Pagination';

const SearchResultsPage = () => {
    const [searchParams, setSearchParams] = useSearchParams();
    const [results, setResults] = useState([]);
    const [isSearching, setIsSearching] = useState(true);
    const [totalItems, setTotalItems] = useState(0);
    const [totalPages, setTotalPages] = useState(0);

    const pageSize = 12;

    const q = searchParams.get('q') || '';
    const province = searchParams.get('province') || '';
    const category = searchParams.get('category') || '';
    const priceFrom = searchParams.get('priceFrom') || '';
    const priceTo = searchParams.get('priceTo') || '';
    const startDate = searchParams.get('startDate') || '';
    const endDate = searchParams.get('endDate') || '';
    const page = parseInt(searchParams.get('page') || '0');

    useEffect(() => {
        fetchResults();
        window.scrollTo(0, 0);
    }, [searchParams]);

    const fetchResults = async () => {
        setIsSearching(true);
        try {
            const params = {
                q,
                province,
                category,
                priceFrom,
                priceTo,
                startDate: startDate ? new Date(startDate).toISOString() : null,
                endDate: endDate ? new Date(endDate).toISOString() : null,
                page,
                size: pageSize
            };
            const res = await tourService.searchTours(params);
            const data = res.data;
            const items = data?.items || [];
            
            setResults(items);
            setTotalItems(data?.totalItems || 0);
            setTotalPages(data?.totalPages || 0);
        } catch (error) {
            console.error('Search error:', error);
        } finally {
            setIsSearching(false);
        }
    };

    const getSearchLabel = () => {
        if (q) return `Kết quả tìm kiếm cho "${q}"`;
        if (category) return `Kết quả tìm kiếm theo danh mục`;
        if (province) return `Tour tại ${PROVINCES[province] || province}`;
        return 'Tất cả tour';
    };

    return (
        <div className="w-full min-h-screen flex flex-col bg-[#F5F5F5]">
            {/* Header Section - White Background */}
            <div className="bg-white pt-8 pb-6 border-b border-slate-100">
                <div className="max-w-[1200px] mx-auto px-4 md:px-6">
                    <div className="flex flex-col md:flex-row md:items-end justify-between gap-4">
                        <div className="space-y-0.5">
                            <h1 className="text-2xl md:text-3xl font-bold text-slate-800 tracking-tight">
                                {getSearchLabel()}
                            </h1>
                            <p className="text-slate-500 font-bold text-sm">
                                {isSearching ? 'Đang tìm kiếm...' : `Tìm thấy ${totalItems} kết quả phù hợp`}
                            </p>
                        </div>

                        {/* Optional: Show active filters here */}
                        <div className="flex flex-wrap gap-2">
                            {province && (
                                <span className="px-3 py-1.5 bg-slate-50 text-slate-600 rounded-full text-xs font-bold border border-slate-100 flex items-center gap-2">
                                    <FaMapMarkerAlt className="text-[#129AF2]" /> {PROVINCES[province] || province}
                                </span>
                            )}
                            {category && (
                                <span className="px-3 py-1.5 bg-slate-50 text-slate-600 rounded-full text-xs font-bold border border-slate-100 flex items-center gap-2">
                                    <FaFilter className="text-[#129AF2]" /> {TOUR_CATEGORIES[category] || category}
                                </span>
                            )}
                        </div>
                    </div>
                </div>
            </div>

            {/* Results Section - Gray Background */}
            <div className="flex-1 py-12">
                <div className="max-w-[1200px] mx-auto px-4 md:px-6">
                    {isSearching ? (
                        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
                            {[1, 2, 3, 4, 5, 6, 7, 8].map(i => (
                                <div key={i} className="aspect-[4/5] bg-white rounded-2xl animate-pulse shadow-sm"></div>
                            ))}
                        </div>
                    ) : results.length > 0 ? (
                        <div className="space-y-12">
                            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
                                {results.map(tour => (
                                    <TourCard key={tour.id} tour={tour} />
                                ))}
                            </div>

                            {/* Pagination */}
                            {totalPages > 1 && (
                                <div className="flex justify-center pt-8 border-t border-slate-200">
                                    <Pagination 
                                        currentPage={page} 
                                        totalPages={totalPages} 
                                        onPageChange={(p) => {
                                            const newParams = new URLSearchParams(searchParams);
                                            newParams.set('page', p);
                                            setSearchParams(newParams);
                                            window.scrollTo({ top: 0, behavior: 'smooth' });
                                        }} 
                                        themeColor="#129AF2"
                                    />
                                </div>
                            )}
                        </div>
                    ) : (
                        <div className="py-20 flex flex-col items-center justify-center text-center">
                            <div className="w-20 h-20 bg-slate-100 rounded-full flex items-center justify-center mb-6">
                                <FaSearch className="text-3xl text-slate-300" />
                            </div>
                            <h3 className="text-xl font-bold text-slate-800 mb-2">Không tìm thấy kết quả nào</h3>
                            <p className="text-slate-500 font-bold max-w-md">
                                Thử thay đổi từ khóa hoặc bộ lọc để tìm thấy những chuyến đi tuyệt vời khác nhé.
                            </p>
                            <Link
                                to="/"
                                className="mt-8 px-8 py-3 bg-[#129AF2] text-white rounded-xl font-bold hover:bg-[#0f87d4] transition-all shadow-lg shadow-[#129AF2]/20"
                            >
                                Quay về trang chủ
                            </Link>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
};

export default SearchResultsPage;
