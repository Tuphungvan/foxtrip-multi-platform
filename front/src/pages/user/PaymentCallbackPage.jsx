import React, { useEffect, useState } from 'react';
import { useSearchParams, Link } from 'react-router-dom';
import { FaCheckCircle, FaTimesCircle, FaCalendarCheck, FaCreditCard, FaArrowRight } from 'react-icons/fa';
import { useCartStore } from '../../store/useCartStore';

const PaymentCallbackPage = () => {
    const [searchParams] = useSearchParams();
    const [status, setStatus] = useState('processing');
    const clearCart = useCartStore(state => state.clearCart);

    const responseCode = searchParams.get('vnp_ResponseCode');
    const txnRef = searchParams.get('vnp_TxnRef');
    const amount = searchParams.get('vnp_Amount');
    const orderInfo = searchParams.get('vnp_OrderInfo');

    useEffect(() => {
        if (responseCode === '00') {
            setStatus('success');
            // Thanh toán thành công thì clear giỏ hàng local
            clearCart(false);
        } else {
            setStatus('failed');
        }
    }, [responseCode, clearCart]);

    const formatVnpAmount = (amt) => {
        if (!amt) return '0 VND';
        const value = parseInt(amt) / 100;
        return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value);
    };

    return (
        <div className="min-h-[80vh] flex items-center justify-center p-4">
            <div className="max-w-md w-full bg-white rounded-3xl border-2 border-slate-200 shadow-sm overflow-hidden">
                <div className="p-10 text-center space-y-8">
                    {status === 'success' ? (
                        <>
                            <div className="w-16 h-16 bg-green-50 rounded-full flex items-center justify-center mx-auto">
                                <FaCheckCircle className="text-3xl text-green-500" />
                            </div>
                            <div className="space-y-2">
                                <h1 className="text-xl font-bold text-slate-800">Thanh toán thành công!</h1>
                                <p className="text-slate-400 text-sm">Cảm ơn bạn đã tin tưởng và lựa chọn Foxtrip.</p>
                            </div>
                        </>
                    ) : (
                        <>
                            <div className="w-16 h-16 bg-red-50 rounded-full flex items-center justify-center mx-auto">
                                <FaTimesCircle className="text-3xl text-red-500" />
                            </div>
                            <div className="space-y-2">
                                <h1 className="text-xl font-bold text-slate-800">Thanh toán thất bại</h1>
                                <p className="text-slate-400 text-sm">Đã có lỗi xảy ra trong quá trình thanh toán.</p>
                            </div>
                        </>
                    )}

                    <div className="bg-slate-50 rounded-2xl p-6 text-left space-y-4">
                        <div className="flex justify-between items-center">
                            <span className="text-[12px] text-slate-600">Mã giao dịch</span>
                            <span className="text-sm font-bold text-slate-800">{txnRef || 'N/A'}</span>
                        </div>
                        <div className="flex justify-between items-center">
                            <span className="text-[12px] text-slate-600">Số tiền</span>
                            <span className="text-sm font-bold text-[#129AF2]">{formatVnpAmount(amount)}</span>
                        </div>
                        <div className="flex justify-between items-center">
                            <span className="text-[12px] text-slate-600">Trạng thái</span>
                            <span className={`text-sm font-bold ${status === 'success' ? 'text-green-500' : 'text-red-500'}`}>
                                {status === 'success' ? 'Thành công' : 'Thất bại'}
                            </span>
                        </div>
                    </div>

                    <div className="pt-4 flex flex-col gap-4">
                        {status === 'success' ? (
                            <Link
                                to="/"
                                className="w-full bg-[#129AF2] text-white py-3.5 rounded-xl font-bold text-sm flex items-center justify-center gap-2 hover:bg-[#0f84cf] transition-all shadow-sm"
                            >
                                Tiếp tục khám phá
                            </Link>
                        ) : (
                            <>
                                <Link
                                    to="/cart"
                                    className="w-full bg-slate-800 text-white py-3.5 rounded-xl font-bold text-sm flex items-center justify-center gap-2 hover:bg-slate-900 transition-all shadow-sm"
                                >
                                    Thử thanh toán lại
                                </Link>
                                <Link
                                    to="/"
                                    className="text-slate-400 font-bold text-[11px] uppercase tracking-widest hover:text-[#129AF2] transition-colors text-center"
                                >
                                    Quay về trang chủ
                                </Link>
                            </>
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
};

export default PaymentCallbackPage;
