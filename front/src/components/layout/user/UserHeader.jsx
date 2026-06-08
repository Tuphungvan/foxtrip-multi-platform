import React from 'react';
import { Link, useLocation } from 'react-router-dom';
import { useAuthStore } from '../../../store/useAuthStore';
import { useCartStore } from '../../../store/useCartStore';
import { useUIStore } from '../../../store/useUIStore';
import { userService } from '../../../services/api/userService';
import { FaShoppingCart, FaUser, FaGlobe, FaSignOutAlt, FaChevronDown } from 'react-icons/fa';

const UserHeader = () => {
    const { isAuthenticated, user, logout, setUser } = useAuthStore();
    const { openAuthModal, openChatbot } = useUIStore();
    const cartCount = useCartStore(state => state.getTotalItems());
    const location = useLocation();

    const handleLogout = async () => {
        await logout();
    };

    React.useEffect(() => {
        const fetchProfile = async () => {
            if (isAuthenticated && !user) {
                try {
                    const res = await userService.getMe();
                    setUser(res.data);
                } catch (error) {
                    console.error("Error fetching user profile:", error);
                }
            }
        };
        fetchProfile();
    }, [isAuthenticated, user, setUser]);

    const scrollToApp = (e) => {
        e.preventDefault();
        const element = document.getElementById('app-download');
        if (element) {
            element.scrollIntoView({ behavior: 'smooth' });
        }
    };

    return (
        <header className="fixed top-0 left-0 right-0 z-50 bg-white border-b border-slate-200">
            {/* Top Row */}
            <div className="max-w-[1200px] mx-auto w-full px-4 md:px-6 h-16 flex items-center justify-between">
                {/* Logo */}
                <Link to="/" className="flex items-center gap-2 group">
                    <img
                        src="/images/logo.png"
                        alt="FoxTrip Logo"
                        className="h-8 w-8 object-contain"
                    />
                    <span className="text-3xl font-bold tracking-tight text-[#129AF2]">
                        FOXTRIP
                    </span>
                </Link>

                {/* Right Side */}
                <div className="flex items-center gap-6">
                    <div className="hidden lg:flex items-center gap-4 text-xs font-bold text-slate-600">
                        <div className="flex items-center gap-1 cursor-pointer text-[#129AF2]">
                            <FaGlobe />
                            <span>VI | VND</span>
                        </div>
                        <a href="#app-download" onClick={scrollToApp} className="hover:text-[#129AF2]">Vào ứng dụng</a>
                        <button onClick={openChatbot} className="hover:text-[#129AF2]">Trợ giúp</button>
                    </div>

                    {isAuthenticated ? (
                        <div className="flex items-center gap-3">
                            <Link to="/cart" className="relative p-2 text-slate-600 hover:text-[#129AF2]">
                                <FaShoppingCart className="text-lg" />
                                {cartCount > 0 && (
                                    <span className="absolute top-0 right-0 bg-[#FF5722] text-white text-[9px] font-bold px-1.5 py-0.5 rounded-full">
                                        {cartCount}
                                    </span>
                                )}
                            </Link>

                            <div className="group relative">
                                <button className="flex items-center gap-2 py-1.5 px-3 rounded-full border border-slate-100 hover:bg-slate-50 transition-all hover:border-slate-200">
                                    <div className="w-7 h-7 rounded-full bg-[#129AF2] text-white flex items-center justify-center text-[10px] font-bold shadow-sm overflow-hidden">
                                        {user?.avatarUrl ? (
                                            <img src={user.avatarUrl} alt={user.username} className="w-full h-full object-cover" />
                                        ) : (
                                            user?.username?.charAt(0).toUpperCase() || <FaUser />
                                        )}
                                    </div>
                                    <span className="text-xs font-bold text-slate-700 truncate max-w-[120px]">
                                        {user?.username || 'Tài khoản'}
                                    </span>
                                    <FaChevronDown className="text-[10px] text-slate-400 group-hover:rotate-180 transition-transform" />
                                </button>

                                <div className="absolute right-0 mt-2 w-64 bg-white rounded-2xl shadow-2xl border border-slate-100 py-3 opacity-0 invisible group-hover:opacity-100 group-hover:visible transition-all transform origin-top-right group-hover:translate-y-0 translate-y-2 z-[60]">
                                    {/* User Info Header */}
                                    <div className="px-5 pb-3 mb-2 border-b border-slate-50 flex items-center gap-3">
                                        <div className="w-10 h-10 rounded-full bg-slate-100 flex items-center justify-center text-[#129AF2] text-sm font-bold border border-slate-50 overflow-hidden">
                                            {user?.avatarUrl ? (
                                                <img src={user.avatarUrl} alt={user.username} className="w-full h-full object-cover" />
                                            ) : (
                                                user?.username?.charAt(0).toUpperCase() || <FaUser />
                                            )}
                                        </div>
                                        <div className="min-w-0">
                                            {user?.username && (
                                                <p className="text-sm font-bold text-slate-800 truncate">
                                                    {user.username}
                                                </p>
                                            )}
                                            {user?.email && (
                                                <p className="text-[11px] text-slate-500 truncate font-medium">
                                                    {user.email}
                                                </p>
                                            )}
                                            {user?.phoneNumber && (
                                                <p className="text-[11px] text-slate-500 truncate font-medium">
                                                    {user.phoneNumber}
                                                </p>
                                            )}
                                        </div>
                                    </div>

                                    {/* Footer Action */}
                                    <div className="pt-2 border-t border-slate-50 px-2">
                                        <button
                                            onClick={handleLogout}
                                            className="w-full flex items-center gap-3 px-3 py-2.5 rounded-xl text-xs font-bold text-red-500 hover:bg-red-50 transition-all"
                                        >
                                            <div className="w-8 h-8 rounded-lg bg-red-50/50 flex items-center justify-center">
                                                <FaSignOutAlt className="rotate-180" />
                                            </div>
                                            Đăng xuất
                                        </button>
                                    </div>
                                </div>
                            </div>
                        </div>
                    ) : (
                        <div className="flex items-center gap-3">
                            <button
                                onClick={() => openAuthModal('register')}
                                className="text-xs font-bold text-slate-600 hover:text-[#129AF2]"
                            >
                                Đăng ký
                            </button>
                            <button
                                onClick={() => openAuthModal('login')}
                                className="px-5 py-2 bg-[#129AF2] text-white rounded-full text-xs font-bold hover:bg-[#0f87d4] transition-all shadow-md shadow-[#129AF2]/20"
                            >
                                Đăng nhập
                            </button>
                        </div>
                    )}
                </div>
            </div>

            {/* Bottom Row */}
            <div className="border-t border-slate-200">
                <div className="max-w-[1200px] mx-auto w-full px-4 md:px-6 h-10 flex items-center gap-8">
                    {['Điều khoản sử dụng', 'Về chúng tôi', 'Đối tác'].map((link) => (
                        <Link
                            key={link}
                            to="#"
                            className="text-xs font-bold text-slate-600 hover:text-[#129AF2] transition-colors"
                        >
                            {link}
                        </Link>
                    ))}
                </div>
            </div>
        </header>
    );
};

export default UserHeader;