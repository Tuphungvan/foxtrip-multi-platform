import React, { useEffect } from 'react';
import { useLocation } from 'react-router-dom';
import UserHeader from './UserHeader';
import UserFooter from './UserFooter';
import Chatbot from '../../user/Chatbot';
import AuthModal from '../../user/AuthModal';
import { useAuthStore } from '../../../store/useAuthStore';

const UserLayout = ({ children }) => {
    const { pathname } = useLocation();
    const { isAuthenticated } = useAuthStore();

    // Scroll to top on route change
    useEffect(() => {
        window.scrollTo(0, 0);
    }, [pathname]);

    return (
        <div className="flex flex-col min-h-screen bg-white selection:bg-[#129AF2] selection:text-white">
            <UserHeader />
            <main className="flex-grow pt-[104px]">
                {children}
            </main>
            <UserFooter />
            <Chatbot />
            <AuthModal />
        </div>
    );
};

export default UserLayout;

