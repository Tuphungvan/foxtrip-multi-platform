import React, { useEffect, useRef } from 'react';
import { FaTimes } from 'react-icons/fa';
import useOutsideClick from '../../../hooks/useOutsideClick';

/**
 * Modal chuẩn cho toàn bộ admin.
 *
 * Behaviors:
 *   - Click overlay → đóng (onClose)
 *   - ESC key → đóng
 *   - Scroll lock khi mở
 *
 * Size system:
 *   sm → max-w-sm  (384px)  — confirm đơn giản
 *   md → max-w-md  (448px)  — form chuẩn
 *   lg → max-w-2xl (672px)  — form phức tạp
 */

const SIZE_MAP = {
    sm: 'max-w-sm',
    md: 'max-w-md',
    lg: 'max-w-2xl',
};

const AdminModal = ({
    isOpen,
    onClose,
    title,
    icon: Icon,
    size = 'md',
    children,
    className = '',
}) => {
    const contentRef = useRef(null);
    useOutsideClick(contentRef, isOpen ? onClose : () => {});

    useEffect(() => {
        if (isOpen) {
            document.body.style.overflow = 'hidden';
        } else {
            document.body.style.overflow = '';
        }
        return () => { document.body.style.overflow = ''; };
    }, [isOpen]);

    useEffect(() => {
        const handleEsc = (e) => { if (e.key === 'Escape' && isOpen) onClose(); };
        document.addEventListener('keydown', handleEsc);
        return () => document.removeEventListener('keydown', handleEsc);
    }, [isOpen, onClose]);

    if (!isOpen) return null;

    return (
        <div className="fixed inset-0 z-[100] flex items-center justify-center p-4 bg-slate-900/50 backdrop-blur-sm">
            <div
                ref={contentRef}
                className={`
                    bg-white rounded-lg shadow-xl w-full flex flex-col
                    border border-slate-100
                    ${SIZE_MAP[size] || SIZE_MAP.md}
                    ${className}
                `}
            >
                {/* Header */}
                <div className="flex items-center justify-between px-6 py-4 border-b border-slate-100 shrink-0">
                    <div className="flex items-center gap-3">
                        {Icon && (
                            <div className="w-8 h-8 rounded-md bg-slate-100 text-slate-600 flex items-center justify-center text-sm shrink-0">
                                <Icon />
                            </div>
                        )}
                        <h3 className="text-base font-bold text-slate-900">{title}</h3>
                    </div>
                    <button
                        onClick={onClose}
                        className="p-1.5 rounded-md text-slate-400 hover:text-slate-700 hover:bg-slate-100 transition-colors"
                    >
                        <FaTimes className="text-sm" />
                    </button>
                </div>

                {/* Content */}
                <div className="flex-1 overflow-y-auto">
                    {children}
                </div>
            </div>
        </div>
    );
};

export default AdminModal;
