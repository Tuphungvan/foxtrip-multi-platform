import React from 'react';
import { Link } from 'react-router-dom';
import { FaStar, FaMapMarkerAlt, FaCalendarAlt } from 'react-icons/fa';
import { PROVINCES } from '../../utils/constants';
import { formatToVN } from '../../utils/dateUtils';

const TourCard = ({ tour }) => {
    const {
        name,
        slug,
        thumbnailUrl,
        finalPrice,
        price,
        discount,
        averageRating,
        province,
        startDate
    } = tour;

    const formatPrice = (price) => {
        return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(price);
    };

    return (
        <Link
            to={`/tour/${slug}`}
            className="group flex flex-col bg-white rounded-2xl overflow-hidden border border-slate-100 hover:shadow-[0_12px_32px_rgba(0,0,0,0.08)] hover:-translate-y-1 transition-all duration-300"
        >
            {/* Image Section */}
            <div className="relative aspect-[4/3] overflow-hidden">
                <img
                    src={thumbnailUrl || 'https://images.unsplash.com/photo-1528127269322-539801943592?q=80&w=2070&auto=format&fit=crop'}
                    alt={name}
                    className="w-full h-full object-cover transition-transform duration-700 group-hover:scale-110"
                />

                {/* Discount Badge */}
                {discount > 0 && (
                    <div className="absolute top-3 left-3 bg-[#FF5722] text-white text-[10px] font-bold px-2 py-1 rounded-lg shadow-lg">
                        -{discount}%
                    </div>
                )}

                {/* Rating Badge Overlay (Optional) */}
                <div className="absolute bottom-3 left-3 flex items-center gap-1.5 px-2 py-1 bg-black/40 backdrop-blur-md rounded-lg text-white text-[10px] font-bold">
                    <FaStar className="text-yellow-400" />
                    <span>{averageRating?.toFixed(1) || '5.0'}</span>
                </div>
            </div>

            {/* Content Section */}
            <div className="p-4 flex flex-col flex-grow">
                <div className="flex items-center gap-2 text-[10px] font-bold text-slate-400 tracking-wider mb-2">
                    <span className="flex items-center gap-1">
                        <FaMapMarkerAlt className="text-[#129AF2]" />
                        {PROVINCES[province] || province || 'Việt Nam'}
                    </span>
                    <span>•</span>
                    <span className="flex items-center gap-1">
                        <FaCalendarAlt className="text-[#129AF2]" />
                        {startDate ? formatToVN(startDate, false) : 'Sắp diễn ra'}
                    </span>
                </div>

                <h3 className="text-[15px] font-bold text-slate-800 line-clamp-2 leading-snug transition-colors mb-4 flex-grow min-h-[40px]">
                    {name}
                </h3>

                <div className="flex flex-col border-t border-slate-50 pt-3">
                    {discount > 0 && (
                        <span className="text-[11px] text-slate-400 line-through font-bold">
                            {formatPrice(price)}
                        </span>
                    )}
                    <div className="flex items-baseline justify-between">
                        <div className="flex items-baseline gap-1">
                            <span className="text-lg font-bold text-[#FF5722]">
                                {formatPrice(finalPrice)}
                            </span>
                            <span className="text-[10px] text-slate-400 font-bold">/ khách</span>
                        </div>

                        <div className="w-8 h-8 rounded-full bg-slate-50 flex items-center justify-center group-hover:bg-[#129AF2] group-hover:text-white transition-all text-slate-300">
                            <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="3" d="M9 5l7 7-7 7" />
                            </svg>
                        </div>
                    </div>
                </div>
            </div>
        </Link>
    );
};

export default TourCard;

