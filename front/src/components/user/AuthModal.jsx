import React, { useState } from 'react';
import { GoogleLogin } from '@react-oauth/google';
import { FaTimes, FaExclamationCircle } from 'react-icons/fa';
import { useAuthStore } from '../../store/useAuthStore';
import { useUIStore } from '../../store/useUIStore';
import { authService } from '../../services/api/authService';
import toast from 'react-hot-toast';

const AuthModal = () => {
    const { isAuthModalOpen, closeAuthModal, authModalTab, openAuthModal } = useUIStore();
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState(null);
    const setAuth = useAuthStore((state) => state.setAuth);

    if (!isAuthModalOpen) return null;

    const handleSuccess = async (credentialResponse) => {
        setIsLoading(true);
        setError(null);
        try {
            const idToken = credentialResponse.credential;
            const res = await authService.loginWithGoogle(idToken);
            const data = res.data;

            if (data.role === 'ADMIN' || data.role === 'SUPER_ADMIN') {
                setError('Tài khoản Quản trị viên không được phép đăng nhập tại đây.');
                setIsLoading(false);
                return;
            }

            setAuth({
                accessToken: data.accessToken,
                refreshToken: data.refreshToken,
                expiresIn: data.expiresIn,
                role: data.role,
                user: data.user
            });

            toast.success('Đăng nhập thành công!');
            closeAuthModal();

        } catch (err) {
            console.error('Google Login Error:', err);
            setError(err?.message || 'Đã xảy ra lỗi khi đăng nhập bằng Google.');
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div className="fixed inset-0 z-[110] flex items-center justify-center p-4">
            <div 
                className="absolute inset-0 bg-slate-900/60 backdrop-blur-sm animate-fade-in"
                onClick={closeAuthModal}
            ></div>
            
            <div className="relative w-full max-w-md bg-white rounded-[32px] shadow-2xl overflow-hidden animate-zoom-in">
                <button 
                    onClick={closeAuthModal}
                    className="absolute top-6 right-6 w-10 h-10 rounded-full bg-slate-50 flex items-center justify-center text-slate-400 hover:text-slate-600 hover:bg-slate-100 transition-all z-10"
                >
                    <FaTimes />
                </button>

                <div className="p-10 pt-12 space-y-8">
                    <div className="text-center space-y-2">
                        <h2 className="text-3xl font-bold text-slate-800 tracking-tight">
                            {authModalTab === 'login' ? 'Chào mừng trở lại' : 'Đăng ký thành viên'}
                        </h2>
                        <p className="text-slate-500 font-medium text-sm">
                            Hành trình khám phá Việt Nam của bạn bắt đầu từ đây
                        </p>
                    </div>

                    <div className="flex p-1 bg-slate-100 rounded-2xl">
                        <button 
                            onClick={() => openAuthModal('login')}
                            className={`flex-1 py-2.5 rounded-xl text-sm font-bold transition-all ${
                                authModalTab === 'login' ? 'bg-white text-[#129AF2] shadow-sm' : 'text-slate-500 hover:text-slate-700'
                            }`}
                        >
                            Đăng nhập
                        </button>
                        <button 
                            onClick={() => openAuthModal('register')}
                            className={`flex-1 py-2.5 rounded-xl text-sm font-bold transition-all ${
                                authModalTab === 'register' ? 'bg-white text-[#129AF2] shadow-sm' : 'text-slate-500 hover:text-slate-700'
                            }`}
                        >
                            Đăng ký
                        </button>
                    </div>

                    {error && (
                        <div className="bg-red-50 border-l-4 border-red-500 p-4 rounded-xl flex gap-3 animate-shake">
                            <FaExclamationCircle className="text-red-500 mt-0.5 shrink-0" />
                            <p className="text-xs text-red-700 font-bold leading-relaxed">{error}</p>
                        </div>
                    )}

                    <div className="space-y-6">
                        <div className="relative flex items-center justify-center py-4">
                            <div className="absolute inset-0 flex items-center">
                                <div className="w-full border-t border-slate-100"></div>
                            </div>
                            <span className="relative px-4 bg-white text-[10px] font-bold text-slate-400 uppercase tracking-widest">
                                Tiếp tục với Google
                            </span>
                        </div>

                        <div className="flex justify-center">
                            {isLoading ? (
                                <div className="flex flex-col items-center gap-3">
                                    <div className="animate-spin rounded-full h-8 w-8 border-4 border-slate-100 border-t-[#129AF2]"></div>
                                    <p className="text-[10px] font-bold text-[#129AF2] uppercase tracking-widest">Đang xử lý...</p>
                                </div>
                            ) : (
                                <div className="w-full">
                                    <GoogleLogin
                                        onSuccess={handleSuccess}
                                        onError={() => setError('Đăng nhập Google thất bại')}
                                        useOneTap
                                        theme="filled_blue"
                                        shape="pill"
                                        width="100%"
                                    />
                                </div>
                            )}
                        </div>
                    </div>

                    <p className="text-center text-[11px] text-slate-400 font-medium leading-relaxed">
                        Bằng việc tiếp tục, bạn đồng ý với <a href="#" className="text-[#129AF2] font-bold">Điều khoản</a> và <a href="#" className="text-[#129AF2] font-bold">Chính sách bảo mật</a> của FoxTrip.
                    </p>
                </div>
            </div>
        </div>
    );
};

export default AuthModal;
