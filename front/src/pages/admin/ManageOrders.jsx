import React, { useState, useEffect, useRef } from 'react';
import {
    FaSearch, FaFilter, FaSpinner,
    FaEye, FaUndo, FaCheckCircle, FaMoneyBillWave
} from 'react-icons/fa';
import { getAdminOrders, createRefund, updateRefund, getOrderDetail, rejectCancellation } from '../../services/api/adminApi';
import { formatToVN } from '../../utils/dateUtils';
import toast from 'react-hot-toast';
import Pagination from '../../components/ui/Pagination';
import AdminButton from '../../components/admin/ui/AdminButton';
import AdminInput from '../../components/admin/ui/AdminInput';
import AdminBadge from '../../components/admin/ui/AdminBadge';
import AdminCard from '../../components/admin/ui/AdminCard';
import AdminModal from '../../components/admin/ui/AdminModal';
import AdminConfirmModal from '../../components/admin/ui/AdminConfirmModal';
import useOutsideClick from '../../hooks/useOutsideClick';

const formatCurrency = (val) => {
    if (val === undefined || val === null) return '0 ₫';
    return val.toString().replace(/\B(?=(\d{3})+(?!\d))/g, '.') + ' ₫';
};

// Status badge mapping
const getStatusBadge = (status) => {
    const map = {
        'PAID': { label: 'Đã thanh toán', variant: 'success' },
        'COMPLETED': { label: 'Hoàn thành', variant: 'neutral' },
        'CANCEL_REQUESTED': { label: 'Yêu cầu hủy', variant: 'warning' },
        'REFUND_PENDING': { label: 'Chờ CK hoàn tiền', variant: 'warning' },
        'REFUNDED': { label: 'Đã hoàn tiền', variant: 'info' },
        'PENDING': { label: 'Chờ thanh toán', variant: 'neutral' },
        'EXPIRED': { label: 'Hết hạn', variant: 'danger' },
    };
    const s = map[status] || { label: status, variant: 'neutral' };
    return <AdminBadge variant={s.variant}>{s.label}</AdminBadge>;
};

const inputClass = "w-full px-4 py-2.5 bg-white border border-slate-200 text-slate-800 text-sm rounded-md focus:outline-none focus:border-[#129AF2] focus:ring-2 focus:ring-[#129AF2]/10 transition-all";

const ManageOrders = () => {
    const [orders, setOrders] = useState([]);
    const [isLoading, setIsLoading] = useState(true);
    const [keyword, setKeyword] = useState('');
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(1);
    const [totalItems, setTotalItems] = useState(0);

    const [filterStatus, setFilterStatus] = useState('');
    const [showFilters, setShowFilters] = useState(false);
    const filterRef = useRef(null);
    useOutsideClick(filterRef, () => setShowFilters(false));

    const [selectedOrder, setSelectedOrder] = useState(null);
    const [showDetailModal, setShowDetailModal] = useState(false);
    const [showRefundModal, setShowRefundModal] = useState(false);
    const [showConfirmRefundModal, setShowConfirmRefundModal] = useState(false);
    const [showRejectModal, setShowRejectModal] = useState(false);

    const [isActionLoading, setIsActionLoading] = useState(false);
    const [refundAmount, setRefundAmount] = useState('');
    const [refundReason, setRefundReason] = useState('');
    const [rejectReason, setRejectReason] = useState('');

    const fetchOrders = async () => {
        setIsLoading(true);
        try {
            const params = { page, size: 20 };
            if (filterStatus) params.status = filterStatus;
            if (keyword) {
                if (keyword.includes('@')) { params.customerEmail = keyword; }
                else { params.orderCode = keyword; }
            }
            const res = await getAdminOrders(params);
            const data = res?.data;
            if (data?.items) {
                setOrders(data.items);
                setPage(data.page || 0);
                setTotalPages(data.totalPages || 1);
                setTotalItems(data.totalItems || 0);
            } else { setOrders([]); }
        } catch (error) {
            console.error('Loi fetch orders:', error);
            toast.error('Khong the tai danh sach don hang');
        } finally {
            setIsLoading(false);
        }
    };

    useEffect(() => {
        const timer = setTimeout(() => { fetchOrders(); }, 500);
        return () => clearTimeout(timer);
    }, [keyword, page, filterStatus]);

    const handleSearch = (e) => { e.preventDefault(); setPage(0); fetchOrders(); };

    const handleOpenDetail = async (orderId) => {
        setIsActionLoading(true);
        try {
            const res = await getOrderDetail(orderId);
            setSelectedOrder(res?.data);
            setShowDetailModal(true);
        } catch (error) {
            toast.error('Loi tai chi tiet don hang');
        } finally {
            setIsActionLoading(false);
        }
    };

    const handleOpenRefund = (order) => {
        setSelectedOrder(order);
        const suggestedAmount = order.totalAmount ? (order.totalAmount / 2) : 0;
        setRefundAmount(suggestedAmount);
        setRefundReason(`Hoan tien 50% theo quy dinh phi huy tour. 50% con lai (${formatCurrency(suggestedAmount)}) duoc tinh vao doanh thu phi huy.`);
        setShowRefundModal(true);
    };

    const handleCreateRefund = async () => {
        if (!refundAmount || refundAmount <= 0) { toast.error('So tien hoan phai lon hon 0'); return; }
        setIsActionLoading(true);
        try {
            await createRefund(selectedOrder.orderId, { amount: parseFloat(refundAmount), reason: refundReason });
            toast.success('Tao yeu cau hoan tien thanh cong!');
            setShowRefundModal(false);
            fetchOrders();
        } catch (error) {
            toast.error(error?.response?.data?.message || 'Loi tao yeu cau hoan tien');
        } finally {
            setIsActionLoading(false);
        }
    };

    const handleConfirmRefund = async () => {
        setIsActionLoading(true);
        try {
            const detailRes = await getOrderDetail(selectedOrder.orderId);
            if (!detailRes?.data?.refundId) {
                toast.error('Khong tim thay ma yeu cau hoan tien. Vui long lien he Dev.');
                setIsActionLoading(false);
                return;
            }
            await updateRefund(detailRes.data.refundId, { status: 'COMPLETED', note: 'Admin xac nhan da chuyen khoan thanh cong' });
            toast.success('Da xac nhan hoan tien thanh cong!');
            setShowConfirmRefundModal(false);
            fetchOrders();
        } catch (error) {
            toast.error(error?.response?.data?.message || 'Loi xac nhan hoan tien');
        } finally {
            setIsActionLoading(false);
        }
    };

    const handleOpenReject = (order) => {
        setSelectedOrder(order);
        setRejectReason('');
        setShowRejectModal(true);
    };

    const handleRejectCancellation = async () => {
        if (!rejectReason.trim()) { toast.error('Vui lòng nhập lý do từ chối'); return; }
        setIsActionLoading(true);
        try {
            await rejectCancellation(selectedOrder.orderId, rejectReason);
            toast.success('Đã từ chối yêu cầu hủy và gử thông báo tới khách hàng.');
            setShowRejectModal(false);
            fetchOrders();
        } catch (error) {
            toast.error(error?.response?.data?.message || 'Lỗi khi từ chối yêu cầu hủy');
        } finally {
            setIsActionLoading(false);
        }
    };

    return (
        <div className="space-y-6 pb-10">

            {/* Page Header */}
            <div>
                <h2 className="text-2xl font-bold text-slate-900">Quản Lý Đơn Hàng &amp; Hoàn Tiền</h2>
                <p className="text-slate-500 text-sm mt-1">Quản lý vé đặt tour và xử lý các yêu cầu hủy vé của khách hàng.</p>
            </div>

            <AdminCard noPadding>
                {/* Toolbar */}
                <div className="p-4 border-b border-slate-100 flex items-center gap-3">
                    <form onSubmit={handleSearch} className="flex-1 max-w-sm">
                        <AdminInput
                            icon={FaSearch}
                            type="text"
                            placeholder="Tim theo Ma don (FT...) hoac Email..."
                            value={keyword}
                            onChange={(e) => setKeyword(e.target.value)}
                        />
                    </form>

                    {/* Filter dropdown */}
                    <div className="relative" ref={filterRef}>
                        <AdminButton
                            variant="secondary"
                            icon={FaFilter}
                            onClick={() => setShowFilters(!showFilters)}
                            className={showFilters ? 'border-[#129AF2] text-[#129AF2]' : ''}
                        >
                            Bộ Lọc {filterStatus && <span className="inline-block w-1.5 h-1.5 rounded-full bg-[#129AF2] ml-1" />}
                        </AdminButton>

                        {showFilters && (
                            <div className="absolute top-full right-0 mt-2 w-60 bg-white border border-slate-200 shadow-lg rounded-lg p-4 z-50">
                                <h4 className="text-[10px] font-bold text-slate-400 uppercase tracking-widest mb-3">Trạng thái</h4>
                                <select
                                    value={filterStatus}
                                    onChange={e => { setFilterStatus(e.target.value); setPage(0); }}
                                    className={inputClass}
                                >
                                    <option value="">Tất cả</option>
                                    <option value="PENDING">Chờ thanh toán</option>
                                    <option value="PAID">Đã thanh toán</option>
                                    <option value="COMPLETED">Đã hoàn thành</option>
                                    <option value="CANCEL_REQUESTED">Khách yêu cầu hủy</option>
                                    <option value="REFUND_PENDING">Chờ chuyển khoản hoàn tiền</option>
                                    <option value="REFUNDED">Đã hoàn tiền</option>
                                    <option value="EXPIRED">Hết hạn</option>
                                </select>
                                {filterStatus && (
                                    <button
                                        onClick={() => { setFilterStatus(''); setPage(0); }}
                                        className="mt-3 w-full text-xs font-bold text-slate-500 hover:text-slate-800 py-1.5 border border-slate-200 rounded-md hover:bg-slate-50 transition-colors"
                                    >
                                        Xóa bộ lọc
                                    </button>
                                )}
                            </div>
                        )}
                    </div>
                </div>

                {/* Table */}
                <div className="overflow-x-auto">
                    <table className="w-full text-left border-collapse">
                        <thead>
                            <tr className="bg-slate-50/60">
                                <th className="py-3.5 px-5 text-[10px] font-bold text-slate-400 uppercase tracking-wider border-b border-slate-100">Mã Đơn</th>
                                <th className="py-3.5 px-5 text-[10px] font-bold text-slate-400 uppercase tracking-wider border-b border-slate-100">Khách hàng</th>
                                <th className="py-3.5 px-5 text-[10px] font-bold text-slate-400 uppercase tracking-wider border-b border-slate-100">Thông tin Tour</th>
                                <th className="py-3.5 px-5 text-[10px] font-bold text-slate-400 uppercase tracking-wider border-b border-slate-100">Số lượng &amp; Giá</th>
                                <th className="py-3.5 px-5 text-[10px] font-bold text-slate-400 uppercase tracking-wider border-b border-slate-100 text-center">Trạng Thái</th>
                                <th className="py-3.5 px-5 text-[10px] font-bold text-slate-400 uppercase tracking-wider border-b border-slate-100 text-center">Thao Tác</th>
                            </tr>
                        </thead>
                        <tbody className="divide-y divide-slate-100 text-sm">
                            {isLoading ? (
                                <tr><td colSpan="6" className="py-16 text-center text-slate-400">
                                    <FaSpinner className="animate-spin text-2xl mx-auto mb-2 text-slate-300" />
                                    <p className="text-xs">Đang tải dữ liệu...</p>
                                </td></tr>
                            ) : orders.length === 0 ? (
                                <tr><td colSpan="6" className="py-20 text-center">
                                    <div className="w-14 h-14 bg-slate-50 border border-slate-100 rounded-lg flex items-center justify-center mx-auto mb-4">
                                        <FaMoneyBillWave className="text-xl text-slate-300" />
                                    </div>
                                    <p className="text-sm font-bold text-slate-800 mb-1">Chưa có Đơn hàng nào</p>
                                    <p className="text-xs text-slate-400">Thử thay đổi bộ lọc hoặc tìm kiếm lại.</p>
                                </td></tr>
                            ) : (
                                orders.map((order) => (
                                    <tr key={order.orderId} className="hover:bg-slate-50/60 transition-colors group">
                                        <td className="py-4 px-5 font-bold text-slate-800 text-xs">{order.orderCode}</td>
                                        <td className="py-4 px-5">
                                            <div className="font-bold text-slate-800 text-xs mb-0.5">{order.customerName}</div>
                                            <div className="text-[11px] text-slate-500">{order.customerEmail}</div>
                                            <div className="text-[11px] text-slate-500">{order.customerPhone}</div>
                                        </td>
                                        <td className="py-4 px-5">
                                            <div className="font-bold text-slate-700 text-xs line-clamp-2">{order.tourNameAtTime}</div>
                                            <div className="text-[10px] text-slate-400 mt-0.5">Tạo: {formatToVN(order.createdAt)}</div>
                                        </td>
                                        <td className="py-4 px-5">
                                            <div className="text-xs font-bold text-[#129AF2]">{formatCurrency(order.totalAmount)}</div>
                                            <div className="text-[11px] text-slate-500">x{order.quantity} khách</div>
                                        </td>
                                        <td className="py-4 px-5 text-center">{getStatusBadge(order.status)}</td>
                                        <td className="py-4 px-5 text-center">
                                            <div className="flex items-center justify-center gap-1.5">
                                                <button
                                                    onClick={() => handleOpenDetail(order.orderId)}
                                                    className="p-1.5 text-[#129AF2] hover:bg-blue-50 rounded-md transition-all"
                                                    title="Xem chi tiet"
                                                >
                                                    <FaEye />
                                                </button>
                                                {order.status === 'CANCEL_REQUESTED' && (
                                                    <>
                                                        <button
                                                            onClick={() => handleOpenRefund(order)}
                                                            className="p-1.5 text-amber-500 hover:bg-amber-50 rounded-md transition-all"
                                                            title="Chap nhan & Hoan tien"
                                                        >
                                                            <FaUndo />
                                                        </button>
                                                        <button
                                                            onClick={() => handleOpenReject(order)}
                                                            className="p-1.5 text-red-500 hover:bg-red-50 rounded-md transition-all"
                                                            title="Tu choi yeu cau huy"
                                                        >
                                                            <FaCheckCircle />
                                                        </button>
                                                    </>
                                                )}
                                                {order.status === 'REFUND_PENDING' && (
                                                    <button
                                                        onClick={() => { setSelectedOrder(order); setShowConfirmRefundModal(true); }}
                                                        className="p-1.5 text-orange-500 hover:bg-orange-50 rounded-md transition-all"
                                                        title="Xac nhan da chuyen khoan hoan tien"
                                                    >
                                                        <FaCheckCircle />
                                                    </button>
                                                )}
                                            </div>
                                        </td>
                                    </tr>
                                ))
                            )}
                        </tbody>
                    </table>
                </div>

                {/* Pagination footer */}
                <div className="p-4 border-t border-slate-100 bg-slate-50/40 flex flex-col md:flex-row md:items-center justify-between gap-4">
                    <p className="text-xs font-bold text-slate-400">
                        Hiển thị <span className="text-slate-800">{orders.length}</span> trên <span className="text-slate-800">{totalItems}</span> đơn
                    </p>
                    <Pagination currentPage={page} totalPages={totalPages} onPageChange={setPage} themeColor="#1e293b" />
                </div>
            </AdminCard>

            {/* Modal: Chi tiet don hang */}
            <AdminModal
                isOpen={showDetailModal}
                onClose={() => setShowDetailModal(false)}
                title="Chi tiết Đơn hàng"
                size="lg"
            >
                {selectedOrder && (
                    <div className="p-6 text-sm text-slate-700 space-y-6">
                        {/* Header của Đơn */}
                        <div className="flex items-center justify-between pb-4 border-b border-slate-100">
                            <div>
                                <p className="text-[11px] text-slate-400 uppercase tracking-wider mb-1">Mã đơn hàng</p>
                                <p className="font-bold text-lg text-slate-800">{selectedOrder.orderCode}</p>
                            </div>
                            <div className="text-right">
                                {getStatusBadge(selectedOrder.status)}
                                <p className="text-xs text-slate-500 mt-1.5">{formatToVN(selectedOrder.createdAt)}</p>
                            </div>
                        </div>

                        <div className="grid grid-cols-1 md:grid-cols-2 gap-x-8 gap-y-6">
                            {/* Khách hàng */}
                            <div>
                                <p className="text-xs font-bold text-slate-400 uppercase mb-3">Khách hàng</p>
                                <table className="w-full text-sm">
                                    <tbody>
                                        <tr>
                                            <td className="text-slate-500 py-1 pr-3 align-top whitespace-nowrap">Họ tên:</td>
                                            <td className="font-medium py-1">{selectedOrder.customerName}</td>
                                        </tr>
                                        <tr>
                                            <td className="text-slate-500 py-1 pr-3 align-top whitespace-nowrap">Điện thoại:</td>
                                            <td className="font-medium py-1">{selectedOrder.customerPhone}</td>
                                        </tr>
                                        <tr>
                                            <td className="text-slate-500 py-1 pr-3 align-top whitespace-nowrap">Email:</td>
                                            <td className="font-medium py-1">{selectedOrder.customerEmail}</td>
                                        </tr>
                                    </tbody>
                                </table>
                            </div>

                            {/* Thanh toán */}
                            <div>
                                <p className="text-xs font-bold text-slate-400 uppercase mb-3">Thanh toán</p>
                                <table className="w-full text-sm">
                                    <tbody>
                                        <tr>
                                            <td className="text-slate-500 py-1 pr-3 align-top whitespace-nowrap">Tổng tiền:</td>
                                            <td className="font-bold text-[#129AF2] py-1">{formatCurrency(selectedOrder.totalAmount)}</td>
                                        </tr>
                                        <tr>
                                            <td className="text-slate-500 py-1 pr-3 align-top whitespace-nowrap">Ngày TT:</td>
                                            <td className="font-medium py-1">{selectedOrder.paidAt ? formatToVN(selectedOrder.paidAt) : 'Chưa thanh toán'}</td>
                                        </tr>
                                        {selectedOrder.expiresAt && !selectedOrder.paidAt && selectedOrder.status === 'PENDING' && (
                                            <tr>
                                                <td className="text-slate-500 py-1 pr-3 align-top whitespace-nowrap">Hạn TT:</td>
                                                <td className="font-medium text-red-500 py-1">{formatToVN(selectedOrder.expiresAt)}</td>
                                            </tr>
                                        )}
                                    </tbody>
                                </table>
                            </div>
                        </div>

                        {/* Thông tin Tour */}
                        <div>
                            <p className="text-xs font-bold text-slate-400 uppercase mb-3 pt-4 border-t border-slate-100">Chi tiết Tour</p>
                            <div className="space-y-3">
                                <p className="font-bold text-base text-slate-800">{selectedOrder.tour?.tourNameAtTime || selectedOrder.tourNameAtTime}</p>
                                <div className="grid grid-cols-3 gap-4">
                                    <div>
                                        <p className="text-[11px] text-slate-500 mb-0.5">Khởi hành</p>
                                        <p className="font-medium">{selectedOrder.tour?.startDateAtTime ? formatToVN(selectedOrder.tour.startDateAtTime, false) : '---'}</p>
                                    </div>
                                    <div>
                                        <p className="text-[11px] text-slate-500 mb-0.5">Kết thúc</p>
                                        <p className="font-medium">{selectedOrder.tour?.endDateAtTime ? formatToVN(selectedOrder.tour.endDateAtTime, false) : '---'}</p>
                                    </div>
                                    <div>
                                        <p className="text-[11px] text-slate-500 mb-0.5">Số lượng vé</p>
                                        <p className="font-medium">{selectedOrder.quantity}</p>
                                    </div>
                                </div>
                            </div>
                        </div>

                        {/* Dịch vụ đi kèm */}
                        {selectedOrder.addons?.length > 0 && (
                            <div>
                                <p className="text-xs font-bold text-slate-400 uppercase mb-3 pt-4 border-t border-slate-100">Dịch vụ đi kèm</p>
                                <ul className="space-y-2">
                                    {selectedOrder.addons.map(ad => (
                                        <li key={ad.tourAddonId || ad.nameAtTime} className="flex justify-between items-center text-sm">
                                            <span>
                                                {ad.addonName || ad.nameAtTime} <span className="text-slate-400 text-xs ml-1">(x{ad.quantity})</span>
                                            </span>
                                            <span className="font-medium">{formatCurrency((ad.priceAtTime || ad.unitPriceAtTime) * ad.quantity)}</span>
                                        </li>
                                    ))}
                                </ul>
                            </div>
                        )}

                        {selectedOrder.statusNote && (
                            <div className="mt-2 p-3 bg-slate-50 border-l-2 border-amber-400">
                                <p className="text-slate-500 text-xs mb-1">Ghi chú hệ thống</p>
                                <p className="text-slate-700 italic">{selectedOrder.statusNote}</p>
                            </div>
                        )}

                        <div className="pt-4 mt-2 flex justify-end">
                            <AdminButton variant="secondary" onClick={() => setShowDetailModal(false)} className="px-6">
                                Đóng
                            </AdminButton>
                        </div>
                    </div>
                )}
            </AdminModal>

            {/* Modal: Xu ly hoan tien */}
            <AdminModal
                isOpen={showRefundModal}
                onClose={() => setShowRefundModal(false)}
                title="Xu ly Hoan tien"
                size="md"
            >
                <div className="p-6 space-y-4">
                    <p className="text-xs text-slate-500">Mã đơn: <span className="font-bold text-slate-800">{selectedOrder?.orderCode}</span></p>
                    <div>
                        <label className="block text-[10px] font-bold text-slate-400 uppercase tracking-wider mb-2">Số tiền hoàn (VND)</label>
                        <input type="number" value={refundAmount} onChange={e => setRefundAmount(e.target.value)} className={inputClass} />
                        <p className="text-[10px] text-slate-400 mt-1">Tổng tiền đơn hàng: {formatCurrency(selectedOrder?.totalAmount)}</p>
                    </div>
                    <div>
                        <label className="block text-[10px] font-bold text-slate-400 uppercase tracking-wider mb-2">Lý do</label>
                        <textarea rows="3" value={refundReason} onChange={e => setRefundReason(e.target.value)} className={inputClass + ' resize-none'} />
                    </div>
                    <div className="flex gap-3 pt-2">
                        <AdminButton variant="secondary" className="flex-1 justify-center" onClick={() => setShowRefundModal(false)}>Đóng</AdminButton>
                        <AdminButton variant="primary" loading={isActionLoading} className="flex-1 justify-center" onClick={handleCreateRefund}>
                            Tạo Lệnh Hoàn Tiền
                        </AdminButton>
                    </div>
                </div>
            </AdminModal>

            {/* Modal: Tu choi yeu cau huy */}
            <AdminModal
                isOpen={showRejectModal}
                onClose={() => setShowRejectModal(false)}
                title="Tu choi yeu cau huy"
                size="md"
            >
                <div className="p-6 space-y-4">
                    <p className="text-sm text-slate-500">Vui lòng nhập lý do từ chối. Lý do này sẽ được gử tới email của khách hàng để thông báo.</p>
                    <div>
                        <label className="block text-[10px] font-bold text-slate-400 uppercase tracking-wider mb-2">Lý do từ chối</label>
                        <textarea
                            value={rejectReason}
                            onChange={(e) => setRejectReason(e.target.value)}
                            className={inputClass + ' resize-none min-h-[100px]'}
                            placeholder="VD: Tour đã bắt đầu hoặc quá thời hạn hủy quy định..."
                        />
                    </div>
                    <div className="flex gap-3">
                        <AdminButton variant="secondary" className="flex-1 justify-center" onClick={() => setShowRejectModal(false)}>Đóng</AdminButton>
                        <AdminButton variant="danger" loading={isActionLoading} className="flex-1 justify-center" onClick={handleRejectCancellation}>
                            Xác nhận từ chối
                        </AdminButton>
                    </div>
                </div>
            </AdminModal>

            {/* Confirm Refund — dung AdminConfirmModal */}
            <AdminConfirmModal
                isOpen={showConfirmRefundModal}
                onClose={() => setShowConfirmRefundModal(false)}
                onConfirm={handleConfirmRefund}
                variant="warning"
                title="Xác nhận chuyển khoản?"
                message={`Bạn xác nhận đã hoàn tất chuyển khoản trả tiền cho đơn hàng ${selectedOrder?.orderCode}? Đơn sẽ chuyển sang trạng thái Đã Hoàn Tiền.`}
                confirmLabel="Đã chuyển khoản"
                cancelLabel="Hủy"
                isLoading={isActionLoading}
            />
        </div>
    );
};

export default ManageOrders;
