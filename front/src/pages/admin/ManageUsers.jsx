import { useState, useEffect, useRef } from 'react';
import { getAdminUsers, lockUser, unlockUser } from '../../services/api/adminApi';
import UserModal from '../../components/admin/UserModal';
import ConfirmDialog from '../../components/ui/ConfirmDialog';
import Pagination from '../../components/ui/Pagination';
import {
    FaUsers, FaPlus, FaSpinner,
    FaEnvelope, FaPhoneAlt, FaMapMarkerAlt,
    FaLock, FaUnlock, FaSearch
} from 'react-icons/fa';
import { PROVINCES } from '../../utils/constants';
import toast from 'react-hot-toast';
import AdminButton from '../../components/admin/ui/AdminButton';
import AdminInput from '../../components/admin/ui/AdminInput';
import AdminBadge from '../../components/admin/ui/AdminBadge';
import AdminCard from '../../components/admin/ui/AdminCard';

const ManageUsers = () => {
    const [keyword, setKeyword] = useState('');
    const [users, setUsers] = useState([]);
    const [isLoading, setIsLoading] = useState(true);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(1);
    const [totalItems, setTotalItems] = useState(0);

    const [confirmDialog, setConfirmDialog] = useState({
        isOpen: false,
        title: '',
        message: '',
        onConfirm: () => {},
        type: 'danger'
    });

    const errorToastShown = useRef(false);

    const fetchUsers = async (pageNumber = 0) => {
        setIsLoading(true);
        try {
            const params = { page: pageNumber, size: 10, email: keyword };
            const res = await getAdminUsers(params);
            const data = res?.data;
            if (data) {
                setUsers(data.items || []);
                setPage(data.page || 0);
                setTotalPages(data.totalPages || 1);
                setTotalItems(data.totalItems || 0);
            }
            errorToastShown.current = false;
        } catch (error) {
            console.error('Lỗi fetch users:', error);
            if (!errorToastShown.current) {
                toast.error('Không thể tải danh sách người dùng');
                errorToastShown.current = true;
                setTimeout(() => { errorToastShown.current = false; }, 2000);
            }
        } finally {
            setIsLoading(false);
        }
    };

    useEffect(() => {
        const timer = setTimeout(() => { fetchUsers(page); }, 300);
        return () => clearTimeout(timer);
    }, [keyword, page]);

    const handleLockUnlock = (user) => {
        const isLocking = !user.deletedAt;
        setConfirmDialog({
            isOpen: true,
            title: isLocking ? 'Khóa người dùng' : 'Mở khóa người dùng',
            message: `Bạn có chắc chắn muốn ${isLocking ? 'khóa' : 'mở khóa'} tài khoản:\n\n"${user.email}"\n\n${isLocking ? 'Người dùng này sẽ không thể đăng nhập vào hệ thống.' : 'Người dùng này sẽ có thể đăng nhập lại bình thường.'}`,
            type: isLocking ? 'danger' : 'success',
            onConfirm: async () => {
                try {
                    if (isLocking) {
                        await lockUser(user.id);
                        toast.success('Khóa người dùng thành công');
                    } else {
                        await unlockUser(user.id);
                        toast.success('Mở khóa người dùng thành công');
                    }
                    fetchUsers(0);
                    setPage(0);
                } catch (error) {
                    toast.error(error.message || `Không thể ${isLocking ? 'khóa' : 'mở khóa'} người dùng này`);
                }
            }
        });
    };

    const getRoleBadge = (role) => {
        const map = {
            'SUPER_ADMIN': { label: 'Super Admin', variant: 'dark' },
            'ADMIN':       { label: 'Admin',       variant: 'info' },
            'GUIDE':       { label: 'Guide',       variant: 'success' },
            'USER':        { label: 'User',        variant: 'neutral' },
        };
        const r = map[role] || map['USER'];
        return <AdminBadge variant={r.variant}>{r.label}</AdminBadge>;
    };

    return (
        <div className="space-y-6">

            {/* Page Header */}
            <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
                <div>
                    <h2 className="text-2xl font-bold text-slate-900">Quản Lý Thành Viên</h2>
                    <p className="text-slate-500 text-sm mt-1">Quản lý tài khoản, phân quyền và trạng thái hoạt động của nhân sự &amp; khách hàng.</p>
                </div>
                <AdminButton
                    variant="primary"
                    icon={FaPlus}
                    onClick={() => setIsModalOpen(true)}
                >
                    Thêm Thành Viên
                </AdminButton>
            </div>

            {/* Main Card */}
            <AdminCard noPadding>

                {/* Toolbar */}
                <div className="p-4 border-b border-slate-100 flex flex-col sm:flex-row sm:items-center justify-between gap-3">
                    <AdminInput
                        icon={FaSearch}
                        placeholder="Tìm kiếm Email..."
                        value={keyword}
                        onChange={(e) => setKeyword(e.target.value)}
                        wrapperClassName="w-full sm:max-w-xs"
                    />
                    <div className="text-xs font-bold text-slate-400 shrink-0">
                        <FaUsers className="inline mr-1.5" />
                        Tổng cộng <span className="text-slate-800">{totalItems}</span> thành viên
                    </div>
                </div>

                {/* Table */}
                <div className="overflow-x-auto">
                    <table className="w-full text-left border-collapse">
                        <thead>
                            <tr className="bg-slate-50/60">
                                <th className="px-5 py-3.5 text-[10px] font-bold text-slate-400 uppercase tracking-wider border-b border-slate-100">Thành viên</th>
                                <th className="px-5 py-3.5 text-[10px] font-bold text-slate-400 uppercase tracking-wider border-b border-slate-100">Vai trò</th>
                                <th className="px-5 py-3.5 text-[10px] font-bold text-slate-400 uppercase tracking-wider border-b border-slate-100">Liên hệ</th>
                                <th className="px-5 py-3.5 text-[10px] font-bold text-slate-400 uppercase tracking-wider border-b border-slate-100">Vị trí</th>
                                <th className="px-5 py-3.5 text-[10px] font-bold text-slate-400 uppercase tracking-wider border-b border-slate-100 text-center">Trạng thái</th>
                                <th className="px-5 py-3.5 text-[10px] font-bold text-slate-400 uppercase tracking-wider border-b border-slate-100 text-right">Thao tác</th>
                            </tr>
                        </thead>
                        <tbody className="divide-y divide-slate-100">
                            {users.map((user) => (
                                <tr key={user.id} className="hover:bg-slate-50/50 transition-colors group">
                                    <td className="px-5 py-4">
                                        <div className="flex items-center gap-3">
                                            <div className="w-9 h-9 rounded-full bg-slate-100 border border-slate-200 overflow-hidden shrink-0">
                                                <img
                                                    src={user.avatarUrl || `https://ui-avatars.com/api/?name=${user.username}&background=f1f5f9&color=64748b`}
                                                    alt={user.username}
                                                    className="w-full h-full object-cover"
                                                />
                                            </div>
                                            <div className="min-w-0">
                                                <div className="text-sm font-bold text-slate-900 truncate">{user.username}</div>
                                                <div className="text-[10px] text-slate-400 font-mono">{user.id.substring(0, 8)}...</div>
                                            </div>
                                        </div>
                                    </td>
                                    <td className="px-5 py-4">{getRoleBadge(user.role)}</td>
                                    <td className="px-5 py-4">
                                        <div className="space-y-1">
                                            <div className="flex items-center gap-2 text-xs text-slate-600">
                                                <FaEnvelope className="text-slate-300 shrink-0" /> {user.email}
                                            </div>
                                            <div className="flex items-center gap-2 text-xs text-slate-600">
                                                <FaPhoneAlt className="text-slate-300 shrink-0" /> {user.phoneNumber}
                                            </div>
                                        </div>
                                    </td>
                                    <td className="px-5 py-4">
                                        <div className="flex items-center gap-2 text-xs text-slate-600">
                                            <FaMapMarkerAlt className="text-slate-400 shrink-0" />
                                            {PROVINCES[user.province] || user.province || 'N/A'}
                                        </div>
                                    </td>
                                    <td className="px-5 py-4 text-center">
                                        {user.deletedAt
                                            ? <AdminBadge variant="danger">Đã khóa</AdminBadge>
                                            : <AdminBadge variant="success">Hoạt động</AdminBadge>
                                        }
                                    </td>
                                    <td className="px-5 py-4 text-right">
                                        <button
                                            onClick={() => handleLockUnlock(user)}
                                            className={`p-2 rounded-md transition-all ${
                                                user.deletedAt
                                                    ? 'bg-emerald-50 text-emerald-600 hover:bg-emerald-100'
                                                    : 'bg-red-50 text-red-600 hover:bg-red-100'
                                            }`}
                                            title={user.deletedAt ? 'Mở khóa' : 'Khóa'}
                                        >
                                            {user.deletedAt ? <FaUnlock /> : <FaLock />}
                                        </button>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>

                {/* Empty state */}
                {!isLoading && users.length === 0 && (
                    <div className="py-20 text-center text-slate-500">
                        <div className="w-16 h-16 bg-slate-50 border border-slate-100 rounded-lg flex items-center justify-center mx-auto mb-4">
                            <FaUsers className="text-2xl text-slate-300" />
                        </div>
                        <h3 className="text-sm font-bold text-slate-900 mb-1">Không tìm thấy thành viên</h3>
                        <p className="text-xs text-slate-400">Thử thay đổi từ khóa tìm kiếm.</p>
                    </div>
                )}

                {/* Loading state */}
                {isLoading && (
                    <div className="py-12 text-center">
                        <FaSpinner className="animate-spin text-2xl mx-auto text-slate-300 mb-2" />
                        <p className="text-xs font-bold text-slate-400 uppercase tracking-widest">Đang tải dữ liệu...</p>
                    </div>
                )}

                {/* Pagination footer */}
                <div className="p-4 border-t border-slate-100 bg-slate-50/40 flex flex-col md:flex-row md:items-center justify-between gap-4">
                    <p className="text-xs font-bold text-slate-400">
                        Hiển thị <span className="text-slate-800">{users.length}</span> trên <span className="text-slate-800">{totalItems}</span> thành viên
                    </p>
                    <Pagination
                        currentPage={page}
                        totalPages={totalPages}
                        onPageChange={setPage}
                        themeColor="#1e293b"
                    />
                </div>
            </AdminCard>

            {/* Modals */}
            <UserModal
                isOpen={isModalOpen}
                onClose={() => setIsModalOpen(false)}
                onSuccess={() => { setPage(0); fetchUsers(0); }}
            />

            <ConfirmDialog
                isOpen={confirmDialog.isOpen}
                onClose={() => setConfirmDialog({ ...confirmDialog, isOpen: false })}
                onConfirm={confirmDialog.onConfirm}
                title={confirmDialog.title}
                message={confirmDialog.message}
                type={confirmDialog.type}
                confirmText="Xác nhận"
                cancelText="Hủy bỏ"
            />
        </div>
    );
};

export default ManageUsers;
