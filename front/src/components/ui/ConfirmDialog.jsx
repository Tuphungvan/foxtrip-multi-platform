import { FaExclamationTriangle, FaTimes } from 'react-icons/fa';

const ConfirmDialog = ({ isOpen, onClose, onConfirm, title, message, confirmText = 'Xác nhận', cancelText = 'Hủy', type = 'danger' }) => {
    if (!isOpen) return null;

    const typeStyles = {
        danger: {
            icon: 'text-red-500',
            iconBg: 'bg-red-50',
            button: 'bg-red-600 hover:bg-red-700 text-white',
            border: 'border-red-100'
        },
        warning: {
            icon: 'text-amber-500',
            iconBg: 'bg-amber-50',
            button: 'bg-amber-600 hover:bg-amber-700 text-white',
            border: 'border-amber-100'
        },
        info: {
            icon: 'text-blue-500',
            iconBg: 'bg-blue-50',
            button: 'bg-blue-600 hover:bg-blue-700 text-white',
            border: 'border-blue-100'
        }
    };

    const styles = typeStyles[type] || typeStyles.danger;

    return (
        <div className="fixed inset-0 z-[200] flex items-center justify-center p-4 bg-slate-900/50 backdrop-blur-sm animate-fadeIn">
            <div className="bg-white rounded-2xl shadow-2xl w-full max-w-md overflow-hidden border border-slate-200 animate-scaleIn">
                {/* Header */}
                <div className={`px-6 py-4 border-b ${styles.border} bg-slate-50/50 flex items-center justify-between`}>
                    <div className="flex items-center gap-3">
                        <div className={`w-10 h-10 rounded-xl ${styles.iconBg} flex items-center justify-center`}>
                            <FaExclamationTriangle className={`text-lg ${styles.icon}`} />
                        </div>
                        <h3 className="text-lg font-bold text-slate-900">{title}</h3>
                    </div>
                    <button 
                        onClick={onClose}
                        className="p-2 hover:bg-slate-100 rounded-lg transition-colors text-slate-400 hover:text-slate-600"
                    >
                        <FaTimes />
                    </button>
                </div>

                {/* Body */}
                <div className="px-6 py-6">
                    <p className="text-sm text-slate-600 leading-relaxed whitespace-pre-line">
                        {message}
                    </p>
                </div>

                {/* Footer */}
                <div className="px-6 py-4 bg-slate-50/50 border-t border-slate-100 flex items-center justify-end gap-3">
                    <button
                        onClick={onClose}
                        className="px-4 py-2 bg-white border border-slate-200 text-slate-700 rounded-lg text-sm font-semibold hover:bg-slate-50 transition-all shadow-sm"
                    >
                        {cancelText}
                    </button>
                    <button
                        onClick={() => {
                            onConfirm();
                            onClose();
                        }}
                        className={`px-4 py-2 rounded-lg text-sm font-semibold transition-all shadow-sm ${styles.button}`}
                    >
                        {confirmText}
                    </button>
                </div>
            </div>

            <style jsx>{`
                @keyframes fadeIn {
                    from { opacity: 0; }
                    to { opacity: 1; }
                }
                @keyframes scaleIn {
                    from { 
                        opacity: 0;
                        transform: scale(0.95);
                    }
                    to { 
                        opacity: 1;
                        transform: scale(1);
                    }
                }
                .animate-fadeIn {
                    animation: fadeIn 0.2s ease-out;
                }
                .animate-scaleIn {
                    animation: scaleIn 0.2s ease-out;
                }
            `}</style>
        </div>
    );
};

export default ConfirmDialog;
