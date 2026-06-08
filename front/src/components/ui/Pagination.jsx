import React from 'react';
import { FaChevronLeft, FaChevronRight } from 'react-icons/fa';

/**
 * Pagination Component
 * @param {number} currentPage - 0-indexed current page
 * @param {number} totalPages - total number of pages
 * @param {function} onPageChange - callback when page changes
 * @param {string} themeColor - main color for active state (default: FoxTrip Brown)
 */
const Pagination = ({ currentPage, totalPages, onPageChange, themeColor = '#B48279' }) => {
    if (totalPages <= 1) return null;

    const renderPageNumbers = () => {
        const pages = [];
        const maxVisible = 5; // Số lượng nút số trang hiển thị tối đa
        
        let startPage = Math.max(0, currentPage - Math.floor(maxVisible / 2));
        let endPage = Math.min(totalPages - 1, startPage + maxVisible - 1);
        
        // Điều chỉnh startPage nếu endPage chạm giới hạn cuối
        if (endPage - startPage + 1 < maxVisible) {
            startPage = Math.max(0, endPage - maxVisible + 1);
        }

        // Trang đầu và dấu ... đầu
        if (startPage > 0) {
            pages.push(
                <button
                    key={0}
                    onClick={() => onPageChange(0)}
                    className="w-9 h-9 flex items-center justify-center rounded-xl text-xs font-bold transition-all hover:bg-slate-100 text-slate-600 border border-transparent"
                >
                    1
                </button>
            );
            if (startPage > 1) {
                pages.push(
                    <span key="dots-start" className="w-9 h-9 flex items-center justify-center text-slate-400 text-xs">...</span>
                );
            }
        }

        // Các trang ở giữa
        for (let i = startPage; i <= endPage; i++) {
            const isActive = i === currentPage;
            pages.push(
                <button
                    key={i}
                    onClick={() => onPageChange(i)}
                    className={`w-9 h-9 flex items-center justify-center rounded-xl text-xs font-bold transition-all border ${
                        isActive 
                            ? 'shadow-md scale-110' 
                            : 'hover:bg-slate-100 text-slate-600 border-transparent'
                    }`}
                    style={{
                        backgroundColor: isActive ? themeColor : 'transparent',
                        borderColor: isActive ? themeColor : 'transparent',
                        color: isActive ? '#fff' : ''
                    }}
                >
                    {i + 1}
                </button>
            );
        }

        // Dấu ... cuối và trang cuối
        if (endPage < totalPages - 1) {
            if (endPage < totalPages - 2) {
                pages.push(
                    <span key="dots-end" className="w-9 h-9 flex items-center justify-center text-slate-400 text-xs">...</span>
                );
            }
            pages.push(
                <button
                    key={totalPages - 1}
                    onClick={() => onPageChange(totalPages - 1)}
                    className="w-9 h-9 flex items-center justify-center rounded-xl text-xs font-bold transition-all hover:bg-slate-100 text-slate-600 border border-transparent"
                >
                    {totalPages}
                </button>
            );
        }

        return pages;
    };

    return (
        <div className="flex items-center gap-1.5">
            <button
                onClick={() => onPageChange(currentPage - 1)}
                disabled={currentPage === 0}
                className={`w-9 h-9 flex items-center justify-center rounded-xl border border-slate-200 transition-all ${
                    currentPage === 0 
                        ? 'opacity-40 cursor-not-allowed bg-slate-50' 
                        : 'hover:bg-white hover:border-slate-300 bg-white text-slate-600 shadow-sm active:scale-90'
                }`}
            >
                <FaChevronLeft className="text-[10px]" />
            </button>

            <div className="flex items-center gap-1 mx-1">
                {renderPageNumbers()}
            </div>

            <button
                onClick={() => onPageChange(currentPage + 1)}
                disabled={currentPage === totalPages - 1}
                className={`w-9 h-9 flex items-center justify-center rounded-xl border border-slate-200 transition-all ${
                    currentPage === totalPages - 1 
                        ? 'opacity-40 cursor-not-allowed bg-slate-50' 
                        : 'hover:bg-white hover:border-slate-300 bg-white text-slate-600 shadow-sm active:scale-90'
                }`}
            >
                <FaChevronRight className="text-[10px]" />
            </button>
        </div>
    );
};

export default Pagination;
