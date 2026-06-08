import React from 'react';
import { FaCircleNotch } from 'react-icons/fa';

/**
 * Primary button system cho toàn bộ admin.
 *
 * Variants:
 *   primary  — đen gradient, text trắng (main CTA)
 *   secondary — trắng + border, text slate (secondary action)
 *   danger   — đỏ, text trắng (destructive action)
 *   ghost    — transparent, text slate (muted action)
 *
 * Sizes: sm | md (default)
 */

const VARIANTS = {
    primary:
        'bg-gradient-to-b from-[#2D3139] to-[#1A1D23] hover:from-[#1A1D23] hover:to-[#000000] text-white shadow-sm',
    secondary:
        'bg-white border border-slate-200 text-slate-700 hover:bg-slate-50',
    danger:
        'bg-red-500 hover:bg-red-600 text-white shadow-sm',
    ghost:
        'text-slate-600 hover:bg-slate-100',
};

const SIZES = {
    sm: 'px-3 py-1.5 text-xs gap-1.5',
    md: 'px-4 py-2.5 text-sm gap-2',
};

const AdminButton = ({
    variant = 'secondary',
    size = 'md',
    icon: Icon,
    loading = false,
    disabled = false,
    children,
    className = '',
    ...props
}) => {
    return (
        <button
            disabled={disabled || loading}
            className={`
                inline-flex items-center justify-center font-bold rounded-md
                transition-all active:scale-[0.98]
                disabled:opacity-50 disabled:cursor-not-allowed disabled:active:scale-100
                ${VARIANTS[variant]}
                ${SIZES[size]}
                ${className}
            `}
            {...props}
        >
            {loading ? (
                <FaCircleNotch className="animate-spin shrink-0" />
            ) : Icon ? (
                <Icon className="shrink-0" />
            ) : null}
            {children}
        </button>
    );
};

export default AdminButton;
