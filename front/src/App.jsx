import React, { useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { GoogleOAuthProvider } from '@react-oauth/google';
import { Toaster } from 'react-hot-toast';
import AdminLoginPage from './pages/admin/AdminLoginPage';

// Admin Layout & Pages
import AdminLayout from './components/layout/AdminLayout';
import DashboardHome from './pages/admin/DashboardHome';
import ManageLocations from './pages/admin/ManageLocations';
import ManageTours from './pages/admin/ManageTours';
import ManageOrders from './pages/admin/ManageOrders';
import ManageUsers from './pages/admin/ManageUsers';
import ReadyTourEdit from './pages/admin/ReadyTourEdit';
import CreateTourWizard from './components/admin/tours/CreateTourWizard';

// User Layout & Pages
import UserLayout from './components/layout/user/UserLayout';
import HomePage from './pages/user/HomePage';
import SearchResultsPage from './pages/user/SearchResultsPage';

import TourDetailPage from './pages/user/TourDetailPage';
import CartPage from './pages/user/CartPage';
import CheckoutPage from './pages/user/CheckoutPage';
import PaymentCallbackPage from './pages/user/PaymentCallbackPage';

import { useAuthStore } from './store/useAuthStore';
import { useUIStore } from './store/useUIStore';

// Protected Route for Users
const ProtectedRoute = ({ children }) => {
  const { isAuthenticated } = useAuthStore();
  const { openAuthModal } = useUIStore();

  useEffect(() => {
    if (!isAuthenticated) {
      openAuthModal('login');
    }
  }, [isAuthenticated, openAuthModal]);

  if (!isAuthenticated) return <Navigate to="/" />;
  return children;
};

// AdminRoute component for ADMIN and SUPER_ADMIN roles
const AdminRoute = ({ children }) => {
  const { isAuthenticated, role } = useAuthStore();
  const isAdmin = role === 'ADMIN' || role === 'SUPER_ADMIN';

  if (!isAuthenticated) return <Navigate to="/admin/login" />;
  if (!isAdmin) return <Navigate to="/" />;

  return children;
};

function App() {
  const { isAuthenticated, role } = useAuthStore();
  const isAdmin = role === 'ADMIN' || role === 'SUPER_ADMIN';

  return (
    <GoogleOAuthProvider clientId={import.meta.env.VITE_GOOGLE_CLIENT_ID}>
      <Toaster
        position="top-right"
        toastOptions={{
          style: {
            fontFamily: 'Nunito, sans-serif',
            fontSize: '14px',
            fontWeight: '600',
            borderRadius: '12px',
            background: '#fff',
            color: '#334155',
            boxShadow: '0 4px 12px rgba(0, 0, 0, 0.05)',
            border: '1px solid #f1f5f9'
          },
          success: {
            iconTheme: {
              primary: '#129AF2',
              secondary: '#fff',
            },
          },
        }}
      />
      <Router>
        <Routes>
          {/* Admin Dashboard */}
          <Route
            path="/admin/*"
            element={
              <AdminRoute>
                <AdminLayout />
              </AdminRoute>
            }
          >
            <Route index element={<Navigate to="dashboard" />} />
            <Route path="dashboard" element={<DashboardHome />} />
            <Route path="locations" element={<ManageLocations />} />
            <Route path="manage-tours" element={<ManageTours />} />
            <Route path="orders" element={<ManageOrders />} />
            <Route path="users" element={<ManageUsers />} />
            <Route path="tours/create" element={<CreateTourWizard />} />
            <Route path="tours/edit/:tourId" element={<CreateTourWizard />} />
            <Route path="tours/ready-edit/:tourId" element={<ReadyTourEdit />} />
          </Route>

          {/* Admin Login */}
          <Route path="/admin/login" element={
            isAuthenticated && isAdmin ? <Navigate to="/admin/dashboard" /> : <AdminLoginPage />
          } />

          {/* User Flow */}
          <Route
            path="/*"
            element={
              isAuthenticated && isAdmin ? (
                <Navigate to="/admin/dashboard" replace />
              ) : (
                <UserLayout>
                  <Routes>
                    <Route path="/" element={<HomePage />} />
                    <Route path="/search" element={<SearchResultsPage />} />
                    <Route path="/tour/:slug" element={<TourDetailPage />} />
                    <Route path="/cart" element={<CartPage />} />
                    <Route path="/checkout" element={
                      <ProtectedRoute>
                        <CheckoutPage />
                      </ProtectedRoute>
                    } />
                    <Route path="/payment-callback" element={<PaymentCallbackPage />} />
                    <Route path="*" element={<Navigate to="/" />} />
                  </Routes>
                </UserLayout>
              )
            }
          />
        </Routes>
      </Router>
    </GoogleOAuthProvider>
  );
}

export default App;


