import React, { useState, useEffect } from 'react';
import { useNavigate, Link, useLocation, Outlet, useSearchParams } from 'react-router-dom';
import { useAuthStore } from '../../store/useAuthStore';
import {
    FaHome, FaUsers, FaSignOutAlt,
    FaBars, FaProjectDiagram, FaMapMarkerAlt, FaImages, FaStar, FaChartLine, FaCog
} from 'react-icons/fa';
import { getAdminMe } from '../../services/api/adminApi';

const AdminLayout = () => {
    const [isSidebarOpen, setIsSidebarOpen] = useState(true);
    const navigate = useNavigate();
    const location = useLocation();
    const { logout, user, setUser, isAuthenticated } = useAuthStore();

    useEffect(() => {
        const fetchUserData = async () => {
            if (isAuthenticated) {
                try {
                    const res = await getAdminMe();
                    setUser(res.data);
                } catch (error) {
                    console.error("Không thể lấy thông tin người dùng:", error);
                }
            }
        };
        fetchUserData();
    }, [isAuthenticated, setUser]);

    const handleLogout = async () => {
        await logout();
        navigate('/admin/login');
    };

    const sideMenu = [
        { to: '/admin/dashboard', icon: FaHome, label: 'Bảng điều khiển' },
        { to: '/admin/manage-tours', icon: FaProjectDiagram, label: 'Quản lý Tour' },
        { to: '/admin/orders', icon: FaChartLine, label: 'Đơn hàng' },
        { to: '/admin/locations', icon: FaMapMarkerAlt, label: 'Địa điểm' },
        { to: '/admin/users', icon: FaUsers, label: 'Người dùng' }
    ];

    return (
        <div
            className="flex h-screen font-sans overflow-hidden text-slate-800"
            style={{ background: 'radial-gradient(circle, #F0F7FF 0%, #FFFFFF 100%)' }}
        >
            {/* Mobile Overlay */}
            {!isSidebarOpen && (
                <div
                    className="fixed inset-0 bg-slate-900/40 backdrop-blur-sm z-30 lg:hidden transition-opacity"
                    onClick={() => setIsSidebarOpen(true)}
                ></div>
            )}

            {/* Sidebar */}
            <aside
                className={`fixed lg:static inset-y-0 left-0 z-40 w-[240px] bg-white flex flex-col transition-all duration-300 ease-in-out border-r border-slate-100 shadow-sm ${isSidebarOpen ? '-translate-x-full lg:translate-x-0 lg:w-[80px] xl:w-[240px]' : 'translate-x-0 lg:-translate-x-full'
                    }`}
            >
                {/* Logo Area */}
                <div
                    className="h-[76px] flex items-center px-6 border-b border-slate-50 cursor-pointer hover:bg-slate-50 transition-colors shrink-0"
                    onClick={() => {
                        if (location.pathname === '/admin/dashboard') {
                            window.location.reload();
                        } else {
                            navigate('/admin/dashboard');
                        }
                    }}
                >
                    <div className="flex items-center gap-3 w-full">
                        <div className="flex-shrink-0 flex items-center justify-center">
                            <img src="/images/admin_logo.png" alt="Logo" className="h-6 w-auto" />
                        </div>
                        <div className="whitespace-nowrap overflow-hidden transition-all duration-300 xl:block lg:hidden font-black text-2xl tracking-tighter uppercase">
                            <span className="text-slate-900">fox</span>
                            <span className="text-[#129AF2]">trip</span>
                        </div>
                    </div>
                </div>

                {/* Nav Links */}
                <nav className="flex-1 px-4 py-8 overflow-y-auto custom-scrollbar space-y-1.5 cursor-pointer">
                    {sideMenu.map((item, idx) => {
                        const isActive = item.to === '/admin/dashboard'
                            ? location.pathname === item.to
                            : location.pathname.startsWith(item.to);
                        const IconCmp = item.icon;

                        return (
                            <Link
                                key={idx}
                                to={item.to}
                                onClick={(e) => {
                                    if (location.pathname === item.to) {
                                        e.preventDefault();
                                        window.location.reload();
                                    }
                                }}
                                className={`group flex items-center gap-3.5 px-3 py-2.5 rounded-lg transition-all text-sm font-bold ${isActive
                                    ? 'bg-slate-900 text-white shadow-lg shadow-slate-200'
                                    : 'text-slate-500 hover:text-slate-900 hover:bg-slate-50'
                                    }`}
                            >
                                <div className={`w-5 flex justify-center text-[18px] transition-colors ${isActive ? 'text-white' : 'text-slate-400 group-hover:text-slate-700'}`}>
                                    <IconCmp />
                                </div>
                                <span className="xl:block lg:hidden whitespace-nowrap">{item.label}</span>
                            </Link>
                        );
                    })}
                </nav>

                {/* Bottom User Area */}
                <div className="p-4 border-t border-slate-50 shrink-0">
                    <button onClick={handleLogout} className="flex items-center gap-3 px-3 py-2 rounded-lg text-red-500 hover:text-red-700 hover:bg-red-50 text-sm font-bold w-full transition-colors">
                        <FaSignOutAlt className="inline mr-2 text-lg" /> <span className="xl:block lg:hidden text-left">Đăng xuất</span>
                    </button>
                </div>
            </aside>

            {/* Main Content Area */}
            <div className="flex-1 flex flex-col min-w-0 overflow-hidden relative">
                {/* Header */}
                <header className="h-[76px] flex items-center justify-between px-6 lg:px-10 z-20 shrink-0">
                    <div className="flex justify-between items-center w-full max-w-7xl mx-auto">
                        <div className="flex items-center gap-4 flex-1">
                            <button onClick={() => setIsSidebarOpen(prev => !prev)} className="p-2 -ml-2 rounded-lg text-slate-500 hover:bg-white hover:shadow-sm transition-all lg:hidden">
                                <FaBars />
                            </button>
                        </div>
                        <div className="flex items-center gap-4 shrink-0 px-2 lg:px-4">
                            {/* Header User/Notification Area Cleared */}
                        </div>
                    </div>
                </header>

                {/* Content */}
                <main className="flex-1 overflow-y-auto px-6 lg:px-10 pb-10 custom-scrollbar">
                    <div className="max-w-7xl mx-auto min-h-full">
                        <Outlet />
                    </div>
                </main>
            </div>
        </div>
    );
};

export default AdminLayout;
