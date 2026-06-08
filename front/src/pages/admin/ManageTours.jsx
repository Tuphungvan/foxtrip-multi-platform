import React, { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import {
    FaPlus, FaFilter, FaSearch, FaSpinner,
    FaRegEdit, FaTrash, FaProjectDiagram, FaCalendarAlt, FaPlay, FaStopCircle, FaRedoAlt, FaUserTie
} from 'react-icons/fa';
import { getAdminTours, terminateTour, restartTour, getGuideSuggestions } from '../../services/api/adminApi';
import { formatToVN, toBackendISO } from '../../utils/dateUtils';
import { PROVINCES, TOUR_CATEGORIES } from '../../utils/constants';
import toast from 'react-hot-toast';
import { deleteTour, restoreTour, getTourDetail } from '../../services/api/adminApi';
import Pagination from '../../components/ui/Pagination';
import AdminButton from '../../components/admin/ui/AdminButton';
import AdminInput from '../../components/admin/ui/AdminInput';
import AdminBadge from '../../components/admin/ui/AdminBadge';
import AdminCard from '../../components/admin/ui/AdminCard';
import AdminModal from '../../components/admin/ui/AdminModal';
import AdminConfirmModal from '../../components/admin/ui/AdminConfirmModal';
import useOutsideClick from '../../hooks/useOutsideClick';


const formatCurrency = (val) => {
    if (!val) return '0 ₫';
    return val.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ".") + ' ₫';
};

const ManageTours = () => {
    const navigate = useNavigate();
    const [tours, setTours] = useState([]);
    const [isLoading, setIsLoading] = useState(true);
    const [keyword, setKeyword] = useState('');
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(1);
    const [totalItems, setTotalItems] = useState(0);

    // Filters
    const [showFilters, setShowFilters] = useState(false);
    const [filterStatus, setFilterStatus] = useState('');
    const [filterCategory, setFilterCategory] = useState('');
    const [filterType, setFilterType] = useState('ACTIVE');
    const filterRef = useRef(null);
    useOutsideClick(filterRef, () => setShowFilters(false));

    // Actions state
    const [selectedTour, setSelectedTour] = useState(null);
    const [showTerminateModal, setShowTerminateModal] = useState(false);
    const [showRestartModal, setShowRestartModal] = useState(false);
    const [showDeleteModal, setShowDeleteModal] = useState(false);
    const [isActionLoading, setIsActionLoading] = useState(false);
    const [guides, setGuides] = useState([]);

    // Restart Form State
    const [restartData, setRestartData] = useState({
        startDate: '',
        endDate: '',
        slots: 20,
        price: 0,
        discount: 0,
        guideId: '',
        thumbnailUrl: '',
        shortId: '',
        description: ''
    });

    const handleOpenRestart = async (tour) => {
        setIsActionLoading(true);
        setGuides([]);
        try {
            const [tourRes, guideRes] = await Promise.all([
                getTourDetail(tour.id),
                getGuideSuggestions(tour.id, tour.startDate, tour.endDate).catch(() => ({ data: [] }))
            ]);
            const fullTour = tourRes?.data || tourRes;

            setSelectedTour(fullTour);
            setGuides(guideRes?.data || []);
            setRestartData({
                startDate: fullTour.startDate ? fullTour.startDate.substring(0, 16) : '',
                endDate: fullTour.endDate ? fullTour.endDate.substring(0, 16) : '',
                slots: fullTour.slots || 20,
                price: fullTour.price || 0,
                discount: fullTour.discount || 0,
                guideId: fullTour.guideId || '',
                thumbnailUrl: fullTour.thumbnailUrl || '',
                shortId: fullTour.shortId || '',
                description: fullTour.description || '',
                itineraries: fullTour.itineraries || [],
                addons: fullTour.addons || []
            });
            setShowRestartModal(true);
        } catch (err) {
            toast.error('Không thể tải thông tin chi tiết tour');
        } finally {
            setIsActionLoading(false);
        }
    };

    const [isRefetchingGuides, setIsRefetchingGuides] = useState(false);
    const refetchGuides = async () => {
        if (!selectedTour || !restartData.startDate || !restartData.endDate) {
            toast.error('Vui lòng chọn ngày bắt đầu và kết thúc trước!');
            return;
        }
        setIsRefetchingGuides(true);
        setGuides([]);
        try {
            const res = await getGuideSuggestions(
                selectedTour.id,
                new Date(restartData.startDate).toISOString(),
                new Date(restartData.endDate).toISOString()
            );
            setGuides(res?.data || []);
            if ((res?.data || []).length === 0) {
                toast('Không có hướng dẫn viên nào rảnh trong khoảng thời gian này.');
            } else {
                toast.success(`Tìm được ${res.data.length} hướng dẫn viên rảnh lịch`);
            }
        } catch {
            toast.error('Lỗi khi tải danh sách hướng dẫn viên');
        } finally {
            setIsRefetchingGuides(false);
        }
    };

    const handleTerminate = async () => {
        if (!selectedTour) return;
        setIsActionLoading(true);
        try {
            await terminateTour(selectedTour.id);
            toast.success('Đã kết thúc tour thành công!');
            setShowTerminateModal(false);
            fetchTours();
        } catch (err) {
            toast.error('Lỗi khi kết thúc tour: ' + err.message);
        } finally {
            setIsActionLoading(false);
        }
    };

    const handleRestart = async () => {
        if (!selectedTour) return;
        if (!restartData.startDate || !restartData.endDate) {
            toast.error('Vui lòng chọn ngày mới!');
            return;
        }
        setIsActionLoading(true);
        try {
            await restartTour(selectedTour.id, {
                startDate: toBackendISO(restartData.startDate),
                endDate: toBackendISO(restartData.endDate),
                slots: restartData.slots,
                price: restartData.price,
                discount: restartData.discount,
                guideId: restartData.guideId || null,
                thumbnailUrl: restartData.thumbnailUrl,
                shortId: restartData.shortId,
                description: restartData.description,
                itineraries: restartData.itineraries?.map(it => ({
                    dayNumber: it.dayNumber,
                    position: it.position,
                    locationId: it.location?.id || it.locationId,
                    activity: it.activity
                })) || [],
                addons: restartData.addons?.map(ad => ({
                    name: ad.name,
                    price: ad.price,
                    description: ad.description,
                    isActive: ad.isActive
                })) || []
            });
            toast.success('Đã khởi động lại tour thành công!');
            setShowRestartModal(false);
            fetchTours();
        } catch (err) {
            toast.error('Lỗi khi khởi động lại: ' + err.message);
        } finally {
            setIsActionLoading(false);
        }
    };

    const handleDelete = async () => {
        if (!selectedTour) return;
        setIsActionLoading(true);
        try {
            await deleteTour(selectedTour.id);
            toast.success('Đã xóa tour thành công (Xóa mềm)');
            setShowDeleteModal(false);
            fetchTours();
        } catch (err) {
            toast.error('Lỗi khi xóa tour: ' + err.message);
        } finally {
            setIsActionLoading(false);
        }
    };

    const handleRestore = async (id) => {
        try {
            await restoreTour(id);
            toast.success('Đã khôi phục tour thành công!');
            fetchTours();
        } catch (err) {
            toast.error('Lỗi khi khôi phục tour: ' + err.message);
        }
    };

    const fetchTours = async () => {
        setIsLoading(true);
        try {
            const params = { page, size: 10, keyword, type: filterType };
            if (filterStatus) params.status = filterStatus;
            if (filterCategory) params.category = filterCategory;

            const res = await getAdminTours(params);
            const data = res?.data;
            if (data?.items) {
                setTours(data.items);
                setPage(data.page || 0);
                setTotalPages(data.totalPages || 1);
                setTotalItems(data.totalItems || 0);
            } else {
                const items = Array.isArray(data) ? data : [];
                setTours(items);
                setTotalPages(1);
                setTotalItems(items.length);
            }
        } catch (error) {
            console.error("Lỗi fetch tours:", error);
            // Fallback to empty if error (e.g. endpoint not added yet)
            setTours([]);
        } finally {
            setIsLoading(false);
        }
    };

    useEffect(() => {
        const timer = setTimeout(() => {
            fetchTours();
        }, 300);
        return () => clearTimeout(timer);
    }, [keyword, page, filterStatus, filterCategory, filterType]);

    const handleSearch = (e) => {
        e.preventDefault();
        setPage(0);
        fetchTours();
    };

    return (
        <div className="space-y-6 pb-10">
            <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
                <div>
                    <h2 className="text-2xl font-bold text-slate-900">Quản Lý Tour</h2>
                    <p className="text-slate-500 text-sm mt-1">Lên lịch trình, cập nhật thông tin và điều phối các chuyến đi.</p>
                </div>
                <AdminButton variant="primary" icon={FaPlus} onClick={() => navigate('/admin/tours/create')}>
                    Thêm Tour Mới
                </AdminButton>
            </div>

            <AdminCard noPadding>
                <div className="p-4 border-b border-slate-100 flex items-center gap-3">
                    <form onSubmit={handleSearch} className="flex-1 max-w-sm">
                        <AdminInput
                            icon={FaSearch}
                            type="text"
                            placeholder="Tìm kiếm tên tour..."
                            value={keyword}
                            onChange={(e) => setKeyword(e.target.value)}
                        />
                    </form>

                    <div className="relative" ref={filterRef}>
                        <AdminButton
                            variant="secondary"
                            icon={FaFilter}
                            onClick={() => setShowFilters(!showFilters)}
                            className={showFilters ? 'border-[#129AF2] text-[#129AF2]' : ''}
                        >
                            Bộ Lọc {(filterStatus || filterCategory) && <span className="inline-block w-1.5 h-1.5 rounded-full bg-[#129AF2] ml-1" />}
                        </AdminButton>

                        {showFilters && (
                            <div className="absolute top-full right-0 mt-2 w-64 bg-white border border-slate-200 shadow-lg rounded-lg p-4 z-50">
                                <h4 className="text-[10px] font-bold text-slate-400 uppercase tracking-widest mb-3">Lọc danh sách</h4>
                                <div className="space-y-3">
                                    <div>
                                        <label className="block text-[10px] font-bold text-slate-500 uppercase tracking-wider mb-1.5">Trạng thái</label>
                                        <select value={filterStatus} onChange={e => { setFilterStatus(e.target.value); setPage(0); }} className="w-full text-sm bg-white border border-slate-200 rounded-md px-3 py-2 outline-none focus:border-[#129AF2] transition-colors text-slate-700">
                                            <option value="">Tất cả</option>
                                            <option value="ACTIVE">Đang mở bán</option>
                                            <option value="ONGOING">Đang diễn ra</option>
                                            <option value="COMPLETED">Đã kết thúc</option>
                                            <option value="HIDDEN">Tạm ẩn</option>
                                        </select>
                                    </div>
                                    <div>
                                        <label className="block text-[10px] font-bold text-slate-500 uppercase tracking-wider mb-1.5">Loại</label>
                                        <select value={filterType} onChange={e => { setFilterType(e.target.value); setPage(0); }} className="w-full text-sm bg-white border border-slate-200 rounded-md px-3 py-2 outline-none focus:border-[#129AF2] transition-colors text-slate-700">
                                            <option value="ACTIVE">Đang hoạt động</option>
                                            <option value="DELETED">Đã xóa mềm</option>
                                        </select>
                                    </div>
                                    <div>
                                        <label className="block text-[10px] font-bold text-slate-500 uppercase tracking-wider mb-1.5">Phân loại</label>
                                        <select value={filterCategory} onChange={e => { setFilterCategory(e.target.value); setPage(0); }} className="w-full text-sm bg-white border border-slate-200 rounded-md px-3 py-2 outline-none focus:border-[#129AF2] transition-colors text-slate-700">
                                            <option value="">Tất cả</option>
                                            <option value="NATURE">Thiên nhiên</option>
                                            <option value="CULTURE">Văn hoá</option>
                                            <option value="SEA">Biển</option>
                                            <option value="RELAX">Nghỉ dưỡng</option>
                                        </select>
                                    </div>
                                </div>
                                {(filterStatus || filterCategory) && (
                                    <button
                                        onClick={() => { setFilterStatus(''); setFilterCategory(''); setPage(0); }}
                                        className="mt-3 w-full text-xs font-bold text-slate-500 hover:text-slate-800 py-1.5 border border-slate-200 rounded-md hover:bg-slate-50 transition-colors"
                                    >
                                        Xóa bộ lọc
                                    </button>
                                )}
                            </div>
                        )}
                    </div>
                </div>

                <div className="overflow-x-auto flex-1">
                    <table className="w-full text-left border-collapse">
                        <thead>
                            <tr className="bg-slate-50 text-slate-500 text-[11px] uppercase tracking-wider">
                                <th className="py-4 px-5 font-bold border-b border-slate-100 w-16">ID</th>
                                <th className="py-4 px-5 font-bold border-b border-slate-100 w-24">Ảnh Bìa</th>
                                <th className="py-4 px-5 font-bold border-b border-slate-100">Thông tin Tour</th>
                                <th className="py-4 px-5 font-bold border-b border-slate-100">Khu vực</th>
                                <th className="py-4 px-5 font-bold border-b border-slate-100">Khách/Giá</th>
                                <th className="py-4 px-5 font-bold border-b border-slate-100 text-center">Trạng Thái</th>
                                <th className="py-4 px-5 font-bold border-b border-slate-100 text-center">Thao tác</th>
                            </tr>
                        </thead>
                        <tbody className="divide-y divide-slate-100 text-sm">
                            {isLoading ? (
                                <tr>
                                    <td colSpan="7" className="py-12 text-center text-slate-400">
                                        <FaSpinner className="animate-spin text-3xl mx-auto mb-3" />
                                        <p>Đang tải dữ liệu...</p>
                                    </td>
                                </tr>
                            ) : tours.length === 0 ? (
                                <tr>
                                    <td colSpan="7" className="py-16 text-center text-slate-400">
                                        <div className="bg-slate-50 border border-slate-100 w-16 h-16 rounded-full flex items-center justify-center mx-auto mb-4">
                                            <FaProjectDiagram className="text-2xl text-slate-300" />
                                        </div>
                                        <h3 className="text-sm font-bold text-slate-800 mb-1">Chưa có Tour nào</h3>
                                        <p className="text-[12px] text-slate-500">Hãy nhấn "Thêm Tour Mới" để bắt đầu thiết kế hành trình.</p>
                                    </td>
                                </tr>
                            ) : (
                                tours.map((tour, index) => (
                                    <tr key={tour.id || index} className="hover:bg-slate-50/70 transition-colors group">
                                        <td className="py-4 px-5 text-slate-500 font-mono text-[11px]">#{index + 1}</td>
                                        <td className="py-4 px-5">
                                            <div className="w-16 h-12 rounded-lg bg-slate-100 overflow-hidden border border-slate-200 shadow-sm relative group">
                                                {tour.thumbnailUrl ? (
                                                    <img src={tour.thumbnailUrl} alt={tour.name} className="w-full h-full object-cover" />
                                                ) : (
                                                    <div className="w-full h-full flex items-center justify-center text-[9px] text-slate-400">No Img</div>
                                                )}
                                            </div>
                                        </td>
                                        <td className="py-4 px-5">
                                            <div className="font-bold text-slate-800 text-[13px] mb-1 line-clamp-1">{tour.name}</div>
                                            <div className="text-[11px] text-slate-500 flex items-center gap-2 mt-1">
                                                <span className="bg-slate-100 px-2 py-0.5 rounded text-slate-600 font-semibold">{TOUR_CATEGORIES[tour.category] || tour.category}</span>
                                                <span className="flex items-center gap-1 text-slate-400 bg-slate-50 px-2 py-0.5 rounded border border-slate-100">
                                                    <FaCalendarAlt className="text-[9px]" /> {formatToVN(tour.startDate, false)}
                                                </span>
                                            </div>
                                        </td>
                                        <td className="py-4 px-5 text-slate-600 text-[12px] font-medium">{PROVINCES[tour.province] || tour.province}</td>
                                        <td className="py-4 px-5">
                                            <div className="text-[12px] font-bold text-blue-600">{formatCurrency(tour.price)}</div>
                                            <div className="text-[11px] text-slate-500">{tour.slots} chỗ</div>
                                        </td>
                                        <td className="py-4 px-5 text-center">
                                            {tour.status === 'ACTIVE' ? <AdminBadge variant="success">Đang mở bán</AdminBadge> :
                                                tour.status === 'ONGOING' ? <AdminBadge variant="info">Đang diễn ra</AdminBadge> :
                                                    tour.status === 'COMPLETED' ? <AdminBadge variant="neutral">Hoàn thành</AdminBadge> :
                                                        tour.status === 'HIDDEN' ? <AdminBadge variant="warning">Tạm ẩn</AdminBadge> :
                                                            <AdminBadge variant="neutral">{tour.status}</AdminBadge>}
                                            {tour.setupStep && tour.setupStep !== 'READY' && (
                                                <div className="text-[9px] text-amber-500 font-bold mt-1 uppercase tracking-tight">Chưa hoàn tất</div>
                                            )}
                                        </td>
                                        <td className="py-4 px-5 text-center whitespace-nowrap">
                                            <div className="flex items-center justify-center gap-2">
                                                {/* Nút Continue/Edit chỉ hiện cho tour chưa xóa, và chưa ongoing/completed */}
                                                {!(tour.deletedAt || filterType === 'DELETED') && tour.status !== 'COMPLETED' && tour.status !== 'ONGOING' && (
                                                    tour.setupStep && tour.setupStep !== 'READY' ? (
                                                        <button
                                                            onClick={() => navigate(`/admin/tours/edit/${tour.id}`)}
                                                            className="p-1.5 bg-amber-50 text-amber-600 border border-amber-200 rounded-md hover:bg-amber-100 transition-all shadow-sm flex items-center gap-1 text-[11px] font-bold"
                                                            title="Tiếp tục cấu hình"
                                                        >
                                                            <FaPlay className="text-[10px]" /> Tiếp tục
                                                        </button>
                                                    ) : (
                                                        <button
                                                            onClick={() => navigate(`/admin/tours/ready-edit/${tour.id}`)}
                                                            className="p-1.5 text-blue-500 hover:text-white hover:bg-blue-500 rounded-md transition-all shadow-sm" title="Chỉnh sửa nhanh"
                                                        >
                                                            <FaRegEdit />
                                                        </button>
                                                    )
                                                )}

                                                {/* Nút Kết thúc cho tour đang active hoặc ongoing và CHƯA xóa */}
                                                {!(tour.deletedAt || filterType === 'DELETED') && (tour.status === 'ACTIVE' || tour.status === 'ONGOING') && (
                                                    <button
                                                        onClick={() => { setSelectedTour(tour); setShowTerminateModal(true); }}
                                                        className="p-1.5 text-orange-500 hover:text-white hover:bg-orange-500 rounded-md transition-all shadow-sm"
                                                        title="Kết thúc Tour sớm"
                                                    >
                                                        <FaStopCircle />
                                                    </button>
                                                )}

                                                {/* Nút Restart cho tour đã xong và CHƯA xóa */}
                                                {!(tour.deletedAt || filterType === 'DELETED') && tour.status === 'COMPLETED' && (
                                                    <button
                                                        onClick={() => handleOpenRestart(tour)}
                                                        className="p-1.5 text-emerald-500 hover:text-white hover:bg-emerald-500 rounded-md transition-all shadow-sm"
                                                        title="Tổ chức lại Tour"
                                                    >
                                                        <FaRedoAlt />
                                                    </button>
                                                )}

                                                {/* Nút Xóa / Khôi phục */}
                                                {(tour.deletedAt || filterType === 'DELETED') ? (
                                                    <button
                                                        onClick={() => handleRestore(tour.id)}
                                                        className="p-1.5 text-blue-500 hover:text-white hover:bg-blue-500 rounded-md transition-all shadow-sm"
                                                        title="Khôi phục"
                                                    >
                                                        <FaRedoAlt className="text-[10px]" />
                                                    </button>
                                                ) : (
                                                    <button
                                                        onClick={() => { setSelectedTour(tour); setShowDeleteModal(true); }}
                                                        className="p-1.5 text-red-300 hover:text-white hover:bg-red-500 rounded-md transition-all shadow-sm"
                                                        title="Xóa"
                                                    >
                                                        <FaTrash />
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

                {/* Pagination */}
                <div className="p-4 border-t border-slate-100 flex flex-col md:flex-row md:items-center justify-between gap-4 bg-slate-50/50">
                    <p className="text-xs font-bold text-slate-500">
                        Hiển thị <span className="text-slate-800">{tours.length}</span> trên <span className="text-slate-800">{totalItems}</span> tour
                    </p>
                    <Pagination
                        currentPage={page}
                        totalPages={totalPages}
                        onPageChange={setPage}
                        themeColor="#1e293b"
                    />
                </div>
            </AdminCard>

            {/* Modal Kết thúc Tour */}
            <AdminConfirmModal
                isOpen={showTerminateModal}
                onClose={() => setShowTerminateModal(false)}
                onConfirm={handleTerminate}
                variant="warning"
                title="Kết thúc Tour sớm?"
                message={`Bạn có chắc chắn muốn kết thúc tour "${selectedTour?.name}" sớm? Hệ thống sẽ tự động xử lý hoàn tiền hoặc hoàn thành các đơn hàng liên quan.`}
                confirmLabel="Xác nhận kết thúc"
                cancelLabel="Bỏ qua"
                isLoading={isActionLoading}
            />

            {/* Modal Xóa Tour */}
            <AdminConfirmModal
                isOpen={showDeleteModal}
                onClose={() => setShowDeleteModal(false)}
                onConfirm={handleDelete}
                variant="danger"
                title="Xác nhận xóa Tour?"
                message={`Tour "${selectedTour?.name}" sẽ được chuyển vào mục lưu trữ. Khách hàng sẽ không thể thấy tour này nữa.`}
                confirmLabel="Xác nhận xóa"
                cancelLabel="Đóng"
                isLoading={isActionLoading}
            />

            {/* Modal Khởi động lại Tour (Restart) */}
            <AdminModal
                isOpen={showRestartModal}
                onClose={() => setShowRestartModal(false)}
                title={`Tổ chức lại Tour — ${selectedTour?.name || ''}`}
                size="lg"
            >
                <div className="p-6 overflow-y-auto space-y-5">
                    <div className="grid grid-cols-2 gap-4">
                        {[
                            { label: 'Thời gian bắt đầu mới', key: 'startDate', type: 'datetime-local' },
                            { label: 'Thời gian kết thúc mới', key: 'endDate', type: 'datetime-local' },
                            { label: 'Giá gốc (₫)', key: 'price', type: 'number' },
                            { label: 'Giảm giá (%)', key: 'discount', type: 'number' },
                            { label: 'Số lượng khách', key: 'slots', type: 'number' },
                            { label: 'Mã Video (ShortID)', key: 'shortId', type: 'text' },
                        ].map(({ label, key, type }) => (
                            <div key={key}>
                                <label className="block text-[10px] font-bold text-slate-500 uppercase tracking-widest mb-1.5">{label}</label>
                                <input
                                    type={type}
                                    value={restartData[key]}
                                    onChange={e => setRestartData({ ...restartData, [key]: e.target.value })}
                                    className="w-full px-3 py-2 bg-white border border-slate-200 rounded-md text-sm outline-none focus:ring-2 focus:ring-[#129AF2]/20 focus:border-[#129AF2] transition-all"
                                />
                            </div>
                        ))}
                    </div>

                    {/* Guide Selector */}
                    <div>
                        <div className="flex items-center justify-between mb-2">
                            <p className="text-[10px] font-bold text-slate-400 uppercase tracking-widest flex items-center gap-1.5">
                                <FaUserTie /> Hướng dẫn viên
                            </p>
                            <button
                                type="button"
                                onClick={refetchGuides}
                                disabled={isRefetchingGuides}
                                className="flex items-center gap-1 text-[10px] font-bold text-[#129AF2] hover:opacity-80 disabled:opacity-50 transition-colors"
                            >
                                {isRefetchingGuides ? <FaSpinner className="animate-spin text-xs" /> : <FaRedoAlt className="text-xs" />}
                                Tìm lại theo ngày mới
                            </button>
                        </div>
                        <div className="border border-slate-200 rounded-md overflow-hidden">
                            {guides.length === 0 ? (
                                <div className="flex items-center gap-2 text-xs text-slate-400 italic p-3">
                                    <FaSpinner className="animate-spin text-xs" /> Đang tải danh sách hướng dẫn viên...
                                </div>
                            ) : (
                                <div className="divide-y divide-slate-100 max-h-44 overflow-y-auto">
                                    <label className={`flex items-center gap-3 px-4 py-2.5 cursor-pointer transition-colors ${!restartData.guideId ? 'bg-slate-900/5' : 'hover:bg-slate-50'}`}>
                                        <input type="radio" name="guide" value="" checked={!restartData.guideId} onChange={() => setRestartData({ ...restartData, guideId: '' })} className="accent-slate-900" />
                                        <div className="w-7 h-7 rounded-full bg-slate-100 border border-slate-200 flex items-center justify-center shrink-0">
                                            <FaUserTie className="text-slate-300 text-xs" />
                                        </div>
                                        <span className="text-xs text-slate-400 italic">Chưa chỉ định hướng dẫn viên</span>
                                    </label>
                                    {guides.map(guide => (
                                        <label key={guide.id} className={`flex items-center gap-3 px-4 py-2.5 cursor-pointer transition-colors ${restartData.guideId === guide.id ? 'bg-slate-50' : 'hover:bg-slate-50'}`}>
                                            <input type="radio" name="guide" value={guide.id} checked={restartData.guideId === guide.id} onChange={() => setRestartData({ ...restartData, guideId: guide.id })} className="accent-slate-900 shrink-0" />
                                            <img src={guide.avatarUrl || `https://ui-avatars.com/api/?name=${encodeURIComponent(guide.username || 'G')}&background=f1f5f9&color=64748b`} alt={guide.username} className="w-7 h-7 rounded-full object-cover border border-slate-200 shrink-0" />
                                            <div className="min-w-0 flex-1">
                                                <div className="text-xs font-bold text-slate-800 truncate">{guide.username}</div>
                                                <div className="text-[10px] text-slate-400 truncate">{guide.email}{guide.phoneNumber ? ` · ${guide.phoneNumber}` : ''}</div>
                                            </div>
                                            {restartData.guideId === guide.id && <span className="text-[9px] font-bold text-[#129AF2] bg-blue-50 px-1.5 py-0.5 rounded uppercase tracking-wide shrink-0">Đã chọn</span>}
                                        </label>
                                    ))}
                                </div>
                            )}
                        </div>
                    </div>

                    <div className="space-y-3">
                        <div>
                            <label className="block text-[10px] font-bold text-slate-500 uppercase tracking-widest mb-1.5">URL Ảnh bìa</label>
                            <input type="text" value={restartData.thumbnailUrl} onChange={e => setRestartData({ ...restartData, thumbnailUrl: e.target.value })} className="w-full px-3 py-2 bg-white border border-slate-200 rounded-md text-sm outline-none focus:ring-2 focus:ring-[#129AF2]/20 focus:border-[#129AF2] transition-all" />
                        </div>
                        <div>
                            <label className="block text-[10px] font-bold text-slate-500 uppercase tracking-widest mb-1.5">Mô tả tóm tắt</label>
                            <textarea rows="3" value={restartData.description} onChange={e => setRestartData({ ...restartData, description: e.target.value })} className="w-full px-3 py-2 bg-white border border-slate-200 rounded-md text-sm outline-none focus:ring-2 focus:ring-[#129AF2]/20 focus:border-[#129AF2] transition-all resize-none" />
                        </div>
                    </div>

                    <div className="flex gap-3 pt-2 border-t border-slate-100">
                        <AdminButton variant="ghost" className="flex-1 justify-center" onClick={() => setShowRestartModal(false)}>Đóng</AdminButton>
                        <AdminButton variant="primary" loading={isActionLoading} className="flex-1 justify-center" onClick={handleRestart}>Bắt đầu ngay</AdminButton>
                    </div>
                </div>
            </AdminModal>

        </div>
    );
};

export default ManageTours;
