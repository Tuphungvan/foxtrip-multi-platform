import React, { useRef } from 'react';
import { FaTrash, FaExclamationTriangle, FaSpinner } from 'react-icons/fa';
import useOutsideClick from '../../../hooks/useOutsideClick';

/**
 * Confirm dialog chuẩn cho các destructive / warning actions.
 *
 * Variants:
 *   danger  — đỏ, dùng cho xóa / khóa vĩnh viễn
 *   warning — vàng/đen, dùng cho hành động có thể hoàn tác
 *
 * Behaviors:
 *   - Click overlay → đóng (onClose)
 *   - Outside click → đóng
 */

const VARIANTS = {
    danger: {
        Icon: FaTrash,
        iconBg: 'bg-red-50 text-red-500',
        confirmClass: 'bg-red-500 hover:bg-red-600 text-white',
    },
    warning: {
        Icon: FaExclamationTriangle,
        iconBg: 'bg-amber-50 text-amber-500',
        confirmClass: 'bg-gradient-to-b from-[#2D3139] to-[#1A1D23] hover:from-[#1A1D23] hover:to-[#000000] text-white',
    },
};

const AdminConfirmModal = ({
    isOpen,
    onClose,
    onConfirm,
    title,
    message,
    variant = 'danger',
    confirmLabel = 'Xác nhận',
    cancelLabel = 'Hủy bỏ',
    isLoading = false,
}) => {
    const contentRef = useRef(null);
    useOutsideClick(contentRef, isOpen ? onClose : () => {});

    if (!isOpen) return null;

    const v = VARIANTS[variant] || VARIANTS.danger;
    const { Icon } = v;

    return (
        <div className="fixed inset-0 z-[100] flex items-center justify-center p-4 bg-slate-900/50 backdrop-blur-sm">
            <div
                ref={contentRef}
                className="bg-white rounded-lg shadow-xl w-full max-w-sm border border-slate-100"
            >
                <div className="p-6 text-center">
                    <div className={`w-12 h-12 rounded-md flex items-center justify-center mx-auto mb-4 ${v.iconBg}`}>
                        <Icon className="text-xl" />
                    </div>
                    <h3 className="text-base font-bold text-slate-900 mb-2">{title}</h3>
                    <p className="text-sm text-slate-500 leading-relaxed mb-6">{message}</p>
                    <div className="flex gap-3">
                        <button
                            onClick={onClose}
                            disabled={isLoading}
                            className="flex-1 px-4 py-2.5 bg-slate-100 hover:bg-slate-200 text-slate-700 font-bold rounded-md transition-colors text-sm disabled:opacity-50"
                        >
                            {cancelLabel}
                        </button>
                        <button
                            onClick={onConfirm}
                            disabled={isLoading}
                            className={`flex-1 px-4 py-2.5 font-bold rounded-md transition-all text-sm flex items-center justify-center gap-2 disabled:opacity-50 ${v.confirmClass}`}
                        >
                            {isLoading ? <FaSpinner className="animate-spin" /> : confirmLabel}
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default AdminConfirmModal;
