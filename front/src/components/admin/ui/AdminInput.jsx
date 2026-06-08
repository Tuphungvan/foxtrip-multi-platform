import React from 'react';

/**
 * Input chuẩn cho toàn bộ admin.
 * Focus state luôn dùng accent blue (#129AF2).
 *
 * Props:
 *   icon       — React-icons component hiển thị bên trái
 *   wrapperClassName — class cho div bọc ngoài
 */

const AdminInput = ({
    icon: Icon,
    className = '',
    wrapperClassName = '',
    ...props
}) => {
    return (
        <div className={`relative ${wrapperClassName}`}>
            {Icon && (
                <Icon className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400 text-sm pointer-events-none z-10" />
            )}
            <input
                className={`
                    w-full bg-white border border-slate-200 text-slate-800 text-sm
                    rounded-md transition-all placeholder:text-slate-400
                    focus:outline-none focus:border-[#129AF2] focus:ring-2 focus:ring-[#129AF2]/10
                    ${Icon ? 'pl-9' : 'pl-4'} pr-4 py-2.5
                    ${className}
                `}
                {...props}
            />
        </div>
    );
};

export default AdminInput;
