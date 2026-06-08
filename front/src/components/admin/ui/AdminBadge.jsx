import React from 'react';

/**
 * Badge/chip chuẩn cho toàn bộ admin.
 * Radius: rounded (4px) — không dùng rounded-full.
 *
 * Variants:
 *   success  — xanh lá (trạng thái tốt)
 *   danger   — đỏ (bị khóa, lỗi, đã xóa)
 *   warning  — vàng (chờ xử lý)
 *   neutral  — xám (mặc định)
 *   info     — xanh dương (thông tin)
 *   dark     — tối (nhấn mạnh)
 */

const VARIANTS = {
    success: 'bg-emerald-50 text-emerald-700 border-emerald-200',
    danger: 'bg-red-50 text-red-700 border-red-200',
    warning: 'bg-amber-50 text-amber-700 border-amber-200',
    neutral: 'bg-slate-100 text-slate-600 border-slate-200',
    info: 'bg-blue-50 text-blue-700 border-blue-200',
    dark: 'bg-slate-900 text-white border-slate-900',
};

const AdminBadge = ({ variant = 'neutral', children, className = '' }) => {
    return (
        <span
            className={`
                inline-flex items-center gap-1
                px-2 py-0.5
                text-[10px] font-bold uppercase tracking-wider
                rounded border
                ${VARIANTS[variant] || VARIANTS.neutral}
                ${className}
            `}
        >
            {children}
        </span>
    );
};

export default AdminBadge;
