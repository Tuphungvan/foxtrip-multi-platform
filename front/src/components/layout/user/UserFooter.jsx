import React from 'react';
import { Link } from 'react-router-dom';
import { FaFacebookF, FaInstagram, FaYoutube, FaTiktok } from 'react-icons/fa';
import { SiVisa, SiMastercard, SiApplepay, SiGooglepay } from 'react-icons/si';

const UserFooter = () => {
    return (
        <footer className="bg-white border-t border-slate-200 pt-12 pb-8">
            <div className="max-w-[1200px] mx-auto px-4 md:px-6">
                <div className="grid grid-cols-2 md:flex md:items-start mb-12">

                    <div className="space-y-4 md:mr-30">
                        <h4 className="text-sm font-bold text-black tracking-normal">Về Foxtrip</h4>
                        <ul className="space-y-2 text-slate-500 text-[12px]">
                            <li><Link to="/about" className="hover:text-slate-800">Về chúng tôi</Link></li>
                            <li><a href="#" className="hover:text-slate-800">Tuyển dụng</a></li>
                            <li><a href="#" className="hover:text-slate-800">Blog</a></li>
                        </ul>
                    </div>

                    <div className="space-y-4 md:mr-30">
                        <h4 className="text-sm font-bold text-black tracking-normal">Đối tác</h4>
                        <ul className="space-y-2 text-slate-500 text-[12px]">
                            <li><a href="#" className="hover:text-slate-800">Đăng ký nhà cung cấp</a></li>
                            <li><a href="#" className="hover:text-slate-800">Đối tác liên kết</a></li>
                        </ul>
                    </div>

                    <div className="space-y-4 md:mr-80">
                        <h4 className="text-sm font-bold text-black tracking-normal">Hỗ trợ</h4>
                        <ul className="space-y-2 text-slate-500 text-[12px]">
                            <li><a href="#" className="hover:text-slate-800">Trung tâm trợ giúp</a></li>
                            <li><a href="#" className="hover:text-slate-800">Điều khoản sử dụng</a></li>
                            <li><a href="#" className="hover:text-slate-800">Chính sách bảo mật</a></li>
                        </ul>
                    </div>

                    <div className="space-y-4">
                        <h4 className="text-sm font-bold text-black tracking-normal">Thanh toán</h4>
                        <div className="flex gap-2">
                            <div className="h-7 px-3 border border-slate-200 rounded flex items-center gap-2 text-[10px] font-bold text-slate-400">
                                <img src="/images/vnpay.png" alt="VNPAY" className="h-3 w-auto object-contain" />
                                VNPAY
                            </div>
                        </div>
                    </div>

                </div>
            </div>
            <div className="border-t border-slate-200 w-full pt-8">
                <div className="max-w-[1200px] mx-auto px-4 md:px-6 flex flex-col md:flex-row items-center justify-between gap-4">
                    <p className="text-slate-400 text-[12px]">
                        © 2026 foxtrip. Bảo lưu mọi quyền.
                    </p>
                    <div className="flex gap-4">
                        {[FaFacebookF, FaInstagram, FaYoutube, FaTiktok].map((Icon, idx) => (
                            <a key={idx} href="#" className="text-black hover:text-slate-600 transition-colors">
                                <Icon className="text-base" />
                            </a>
                        ))}
                    </div>
                </div>
            </div>
        </footer>
    );
};

export default UserFooter;
