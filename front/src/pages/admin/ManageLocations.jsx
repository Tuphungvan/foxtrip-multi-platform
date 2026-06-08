import { useState, useEffect, useRef } from 'react';
import { getAdminLocations, deleteAdminLocation, restoreAdminLocation } from '../../services/api/adminApi';
import LocationModal from '../../components/admin/LocationModal';
import ConfirmDialog from '../../components/ui/ConfirmDialog';
import Pagination from '../../components/ui/Pagination';
import { FaPlus, FaSpinner, FaMapMarkerAlt, FaRegEdit, FaUndo, FaLock, FaUnlock, FaFilter, FaSearch } from 'react-icons/fa';
import { PROVINCES, LOCATION_TYPES } from '../../utils/constants';
import toast from 'react-hot-toast';
import AdminButton from '../../components/admin/ui/AdminButton';
import AdminInput from '../../components/admin/ui/AdminInput';
import AdminBadge from '../../components/admin/ui/AdminBadge';
import AdminCard from '../../components/admin/ui/AdminCard';

const ManageLocations = () => {
    const [keyword, setKeyword] = useState('');
    const [debouncedKeyword, setDebouncedKeyword] = useState('');

    // Debounce search input
    useEffect(() => {
        const timer = setTimeout(() => {
            setDebouncedKeyword(keyword);
            setPage(0); // Reset về trang đầu khi tìm kiếm
        }, 500);
        return () => clearTimeout(timer);
    }, [keyword]);

    const [locations, setLocations] = useState([]);
    const [statusFilter, setStatusFilter] = useState('ACTIVE');
    const [isLoading, setIsLoading] = useState(false);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(1);
    const [totalItems, setTotalItems] = useState(0);
    const [editingLoc, setEditingLoc] = useState(null);

    const [confirmDialog, setConfirmDialog] = useState({
        isOpen: false, title: '', message: '', onConfirm: () => {}, type: 'danger'
    });

    const errorToastShown = useRef(false);

    const fetchLocations = async (targetPage = 0) => {
        setIsLoading(true);
        setLocations([]);
        try {
            const params = { page: targetPage, size: 10, keyword: debouncedKeyword };
            if (statusFilter) params.type = statusFilter;
            const res = await getAdminLocations(params);
            const data = res?.data;
            if (data) {
                setLocations(data.items || []);
                setTotalPages(data.totalPages || 1);
                setTotalItems(data.totalItems || 0);
                setPage(data.page || 0);
            }
            errorToastShown.current = false;
        } catch (error) {
            console.error('Lỗi fetch locations:', error);
            if (!errorToastShown.current) {
                toast.error('Không thể tải danh sách địa điểm');
                errorToastShown.current = true;
                setTimeout(() => { errorToastShown.current = false; }, 2000);
            }
        } finally {
            setIsLoading(false);
        }
    };

    useEffect(() => { fetchLocations(page); }, [debouncedKeyword, statusFilter, page]);

    const handleDelete = async (id, name) => {
        setConfirmDialog({
            isOpen: true,
            title: 'Xác nhận xóa địa điểm',
            message: `Bạn có chắc chắn muốn xóa "${name}"?\n\nĐịa điểm sẽ được chuyển sang trạng thái đã xóa và có thể khôi phục lại.`,
            type: 'danger',
            onConfirm: async () => {
                try {
                    await deleteAdminLocation(id);
                    toast.success('Xóa địa điểm thành công!');
                    fetchLocations(0);
                } catch (error) {
                    toast.error(error.message || 'Không thể xóa địa điểm này!');
                }
            }
        });
    };

    const handleRestore = async (id, name) => {
        setConfirmDialog({
            isOpen: true,
            title: 'Khôi phục địa điểm',
            message: `Bạn có muốn khôi phục địa điểm: "${name}"?`,
            type: 'warning',
            onConfirm: async () => {
                try {
                    await restoreAdminLocation(id);
                    toast.success('Khôi phục địa điểm thành công!');
                    fetchLocations(0);
                } catch (error) {
                    toast.error(error.message || 'Không thể khôi phục địa điểm này!');
                }
            }
        });
    };

    const getTypeBadge = (type) => {
        const map = {
            'HOTEL':      'info',
            'RESTAURANT': 'warning',
            'ATTRACTION': 'success',
        };
        return <AdminBadge variant={map[type] || 'neutral'}>{LOCATION_TYPES[type] || type}</AdminBadge>;
    };

    return (
        <div className="space-y-6">

            {/* Page Header */}
            <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
                <div>
                    <h2 className="text-2xl font-bold text-slate-900">Quản Lý Địa Điểm</h2>
                    <p className="text-slate-500 text-sm mt-1">Phân quyền, tra cứu dữ liệu địa lý (Khách sạn, Điểm tham quan,...)</p>
                </div>
                <AdminButton
                    variant="primary"
                    icon={FaPlus}
                    onClick={() => { setEditingLoc(null); setIsModalOpen(true); }}
                >
                    Thêm Địa Điểm
                </AdminButton>
            </div>

            {/* Main Card */}
            <AdminCard noPadding>

                {/* Toolbar */}
                <div className="p-4 border-b border-slate-100 flex flex-col sm:flex-row sm:items-center justify-between gap-3">
                    <div className="flex items-center gap-3 flex-wrap">
                        <AdminInput
                            icon={FaSearch}
                            placeholder="Tim kiem dia diem..."
                            value={keyword}
                            onChange={(e) => setKeyword(e.target.value)}
                            wrapperClassName="w-full sm:w-64"
                        />
                        <p className="text-xs text-slate-400 font-bold">
                            Tổng <span className="text-slate-700">{totalItems}</span> kết quả
                        </p>
                    </div>

                    {/* Filter tabs */}
                    <div className="flex items-center bg-slate-100 rounded-md p-0.5 gap-0.5">
                        <button
                            onClick={() => { setStatusFilter('ACTIVE'); setPage(0); }}
                            className={`px-3 py-1.5 text-xs font-bold rounded transition-all flex items-center gap-1.5 ${statusFilter === 'ACTIVE' ? 'bg-white text-slate-900 shadow-sm' : 'text-slate-500 hover:text-slate-700'}`}
                        >
                            <FaUnlock className="text-[10px]" /> Hoạt động
                        </button>
                        <button
                            onClick={() => { setStatusFilter('DELETED'); setPage(0); }}
                            className={`px-3 py-1.5 text-xs font-bold rounded transition-all flex items-center gap-1.5 ${statusFilter === 'DELETED' ? 'bg-white text-slate-900 shadow-sm' : 'text-slate-500 hover:text-slate-700'}`}
                        >
                            <FaLock className="text-[10px]" /> Đã xóa
                        </button>
                        <button
                            onClick={() => { setStatusFilter(null); setPage(0); }}
                            className={`px-3 py-1.5 text-xs font-bold rounded transition-all flex items-center gap-1.5 ${statusFilter === null ? 'bg-white text-slate-900 shadow-sm' : 'text-slate-500 hover:text-slate-700'}`}
                        >
                            <FaFilter className="text-[10px]" /> Tất cả
                        </button>
                    </div>
                </div>

                {/* Content */}
                <div className="min-h-[400px]">
                    {isLoading && locations.length === 0 ? (
                        <div className="py-28 text-center">
                            <FaSpinner className="animate-spin text-3xl mx-auto text-slate-300 mb-3" />
                            <p className="text-sm font-medium text-slate-400">Đang tải dữ liệu...</p>
                        </div>
                    ) : locations.length === 0 ? (
                        <div className="py-28 text-center">
                            <div className="w-14 h-14 bg-slate-50 border border-slate-100 rounded-lg flex items-center justify-center mx-auto mb-4">
                                <FaMapMarkerAlt className="text-2xl text-slate-300" />
                            </div>
                            <p className="text-sm font-bold text-slate-700">Không tìm thấy địa điểm nào</p>
                            <p className="text-xs text-slate-400 mt-1">Vui lòng thử điều chỉnh bộ lọc hoặc từ khóa tìm kiếm</p>
                        </div>
                    ) : (
                        <div className="divide-y divide-slate-100">
                            {locations.map((loc) => {
                                const isDeleted = !!loc.deletedAt;
                                return (
                                    <div key={loc.id} className={`p-4 hover:bg-slate-50/70 transition-colors group ${isDeleted ? 'opacity-60' : ''}`}>
                                        <div className="flex items-start gap-4">
                                            <div className="w-16 h-16 rounded-lg bg-slate-100 overflow-hidden shrink-0 border border-slate-200">
                                                <img
                                                    src={loc.imageUrl || 'https://placehold.co/200x200?text=Location'}
                                                    alt={loc.name}
                                                    className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
                                                />
                                            </div>
                                            <div className="flex-1 min-w-0">
                                                <div className="flex items-start justify-between gap-3">
                                                    <div className="min-w-0">
                                                        <div className="flex items-center gap-2 mb-1 flex-wrap">
                                                            <h4 className="font-bold text-slate-800 text-sm">{loc.name}</h4>
                                                            {isDeleted && <AdminBadge variant="danger">Đã xóa</AdminBadge>}
                                                        </div>
                                                        <div className="flex items-center gap-1.5 text-slate-500 mb-2">
                                                            <FaMapMarkerAlt className="text-[10px] shrink-0" />
                                                            <p className="text-xs truncate">{loc.address}</p>
                                                        </div>
                                                        <div className="flex items-center gap-2 flex-wrap">
                                                            <AdminBadge variant="neutral">{PROVINCES[loc.province] || loc.province}</AdminBadge>
                                                            {getTypeBadge(loc.type)}
                                                            {loc.priority > 0 && (
                                                                <AdminBadge variant="warning">QC P{loc.priority}</AdminBadge>
                                                            )}
                                                        </div>
                                                    </div>

                                                    <div className="flex items-center gap-1.5 shrink-0">
                                                        {!isDeleted ? (
                                                            <>
                                                                <button
                                                                    onClick={() => { setEditingLoc(loc); setIsModalOpen(true); }}
                                                                    className="p-2 text-[#129AF2] hover:bg-blue-50 rounded-md transition-all"
                                                                    title="Chỉnh sửa"
                                                                >
                                                                    <FaRegEdit />
                                                                </button>
                                                                <button
                                                                    onClick={() => handleDelete(loc.id, loc.name)}
                                                                    className="p-2 text-red-500 hover:bg-red-50 rounded-md transition-all"
                                                                    title="Xóa"
                                                                >
                                                                    <FaLock />
                                                                </button>
                                                            </>
                                                        ) : (
                                                            <AdminButton
                                                                variant="secondary"
                                                                size="sm"
                                                                icon={FaUndo}
                                                                onClick={() => handleRestore(loc.id, loc.name)}
                                                            >
                                                                Khôi phục
                                                            </AdminButton>
                                                        )}
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                );
                            })}
                        </div>
                    )}
                </div>

                {/* Pagination footer */}
                <div className="p-4 border-t border-slate-100 bg-slate-50/40 flex flex-col md:flex-row md:items-center justify-between gap-4">
                    <p className="text-xs font-bold text-slate-400">
                        Hiển thị <span className="text-slate-800">{locations.length}</span> trên <span className="text-slate-800">{totalItems}</span> địa điểm
                    </p>
                    <Pagination currentPage={page} totalPages={totalPages} onPageChange={setPage} themeColor="#1e293b" />
                </div>
            </AdminCard>

            <LocationModal
                isOpen={isModalOpen}
                editingLocation={editingLoc}
                onClose={() => { setIsModalOpen(false); setEditingLoc(null); }}
                onSuccess={() => fetchLocations(0)}
            />

            <ConfirmDialog
                isOpen={confirmDialog.isOpen}
                onClose={() => setConfirmDialog({ ...confirmDialog, isOpen: false })}
                onConfirm={confirmDialog.onConfirm}
                title={confirmDialog.title}
                message={confirmDialog.message}
                type={confirmDialog.type}
                confirmText="Đồng ý"
                cancelText="Hủy bỏ"
            />
        </div>
    );
};

export default ManageLocations;
