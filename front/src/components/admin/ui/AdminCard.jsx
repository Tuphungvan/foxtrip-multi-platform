import React from 'react';

/**
 * Card container chuẩn cho toàn bộ admin.
 * Radius: rounded-lg (8px). Shadow nhẹ + border mỏng.
 *
 * Chỉ dùng card khi:
 *   - Nhóm nội dung độc lập (form, stats block, table)
 *   - Cần phân tách rõ với background page
 *
 * KHÔNG dùng card:
 *   - Bọc toàn bộ page layout
 *   - Lồng card trong card với radius khác nhau
 */

const AdminCard = ({ children, className = '', noPadding = false, ...props }) => {
    return (
        <div
            className={`
                bg-white rounded-lg border border-slate-100 shadow-sm
                ${!noPadding ? '' : ''}
                ${className}
            `}
            {...props}
        >
            {children}
        </div>
    );
};

export default AdminCard;
