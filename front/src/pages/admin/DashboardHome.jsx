import { useState, useEffect, useMemo, useRef } from 'react';
import {
    FaChartLine, FaDownload, FaUserEdit, FaCalendarAlt, FaArrowUp, FaArrowDown,
    FaShoppingBag, FaAd, FaWallet, FaSpinner, FaFilePdf, FaHistory, FaCheckCircle, FaPrint
} from 'react-icons/fa';
import {
    LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer,
    BarChart, Bar, Cell, LabelList
} from 'recharts';
import { toPng } from 'html-to-image';
import { useAuthStore } from '../../store/useAuthStore';
import ProfileModal from '../../components/admin/ProfileModal';
import AdminButton from '../../components/admin/ui/AdminButton';
import AdminCard from '../../components/admin/ui/AdminCard';
import ReportTemplate from './ReportTemplate';
import {
    getRevenueReport,
    getLatestRevenueReport,
    getTourOccupancy,
    getDailyBookings,
    getComprehensiveReport
} from '../../services/api/adminApi';
import { exportComprehensiveReportToPdf } from '../../utils/PdfExportService';
import toast from 'react-hot-toast';

const DashboardHome = () => {
    const { user } = useAuthStore();
    const [isProfileModalOpen, setIsProfileModalOpen] = useState(false);
    const lineChartRef = useRef(null);
    const barChartRef = useRef(null);
    const reportRef = useRef(null);

    // Stats & Chart State
    const [monthData, setMonthData] = useState(null);
    const [latest12MonthsData, setLatest12MonthsData] = useState([]);
    const [isLoading, setIsLoading] = useState(true);
    const [isExporting, setIsExporting] = useState(false);
    const [reportData, setReportData] = useState(null);

    // Config filters
    const now = new Date();
    const [selectedDate, setSelectedDate] = useState({
        day: now.getDate(),
        month: now.getMonth() + 1,
        year: now.getFullYear()
    });
    const [occupancyData, setOccupancyData] = useState([]);
    const [dailyData, setDailyData] = useState([]);
    const [isExtraLoading, setIsExtraLoading] = useState(false);

    // Toggle categories visibility in chart
    const [visibleSeries, setVisibleSeries] = useState({
        tour: true,
        addon: true,
        ads: true
    });

    const handleImageError = (e) => {
        e.target.src = `https://ui-avatars.com/api/?name=${user?.username}&background=f1f5f9&color=64748b`;
    };

    const fetchDashboardData = async () => {
        setIsLoading(true);
        try {
            const [monthRes, latestRes, occRes] = await Promise.all([
                getRevenueReport(selectedDate.month, selectedDate.year),
                getLatestRevenueReport(),
                getTourOccupancy(selectedDate.month, selectedDate.year)
            ]);
            setMonthData(monthRes.data);
            setLatest12MonthsData(latestRes.data || []);
            setOccupancyData(occRes.data || []);
        } catch (error) {
            console.error("Lỗi khi tải dữ liệu dashboard:", error);
            toast.error("Không thể tải dữ liệu báo cáo.");
        } finally {
            setIsLoading(false);
        }
    };

    const fetchDailyData = async () => {
        setIsExtraLoading(true);
        try {
            const dateStr = `${selectedDate.year}-${String(selectedDate.month).padStart(2, '0')}-${String(selectedDate.day).padStart(2, '0')}`;
            const dailyRes = await getDailyBookings(dateStr);
            setDailyData(dailyRes.data || []);
        } catch (error) {
            console.error("Lỗi khi tải thống kê ngày:", error);
        } finally {
            setIsExtraLoading(false);
        }
    };

    useEffect(() => {
        fetchDashboardData();
    }, [selectedDate.month, selectedDate.year]);

    useEffect(() => {
        fetchDailyData();
    }, [selectedDate]);

    // Formatters
    const formatCurrency = (value) => {
        return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value || 0);
    };

    const formatCompactNumber = (number) => {
        if (number >= 1000000) return (number / 1000000).toFixed(1) + 'M';
        if (number >= 1000) return (number / 1000).toFixed(1) + 'K';
        return number;
    };

    const monthlyBarData = useMemo(() => {
        if (!monthData) return [];
        return [
            { name: 'Doanh thu Tour', value: monthData.revenueTour, color: '#1e293b' },
            { name: 'Dịch vụ đi kèm', value: monthData.revenueAddon, color: '#129AF2' },
            { name: 'Quảng cáo', value: monthData.revenueAds, color: '#94a3b8' }
        ];
    }, [monthData]);

    const handleExportComprehensivePdf = async () => {
        setIsExporting(true);
        const toastId = toast.loading("Đang khởi tạo báo cáo...");

        try {
            // 1. Fetch latest data for report
            const res = await getComprehensiveReport(selectedDate.month, selectedDate.year);
            const data = res.data;

            // 2. Capture charts for the template
            const chartImages = [];
            if (barChartRef.current) {
                const img1 = await toPng(barChartRef.current, { backgroundColor: '#fff', pixelRatio: 2 });
                chartImages.push(img1);
            }
            if (lineChartRef.current) {
                const img2 = await toPng(lineChartRef.current, { backgroundColor: '#fff', pixelRatio: 2 });
                chartImages.push(img2);
            }

            // 3. Set data to render the off-screen template
            setReportData({ data, chartImages });

            // 4. Wait for React to render the template (next tick)
            setTimeout(async () => {
                if (reportRef.current) {
                    try {
                        const pageElements = reportRef.current.querySelectorAll('.report-page');
                        const pageImages = [];

                        for (const el of pageElements) {
                            const img = await toPng(el, {
                                backgroundColor: '#fff',
                                pixelRatio: 3,
                                width: 800
                            });
                            pageImages.push(img);
                        }

                        // 5. Trigger PDF generation
                        exportComprehensiveReportToPdf({
                            reportImages: pageImages,
                            period: selectedDate.month
                                ? `Tháng ${selectedDate.month}/${selectedDate.year}`
                                : `Cả năm ${selectedDate.year}`
                        });

                        toast.success("Đã xuất báo cáo siêu cấp thành công!", { id: toastId });
                    } catch (err) {
                        console.error("Lỗi chụp ảnh báo cáo:", err);
                        toast.error("Lỗi khi xử lý hình ảnh báo cáo.", { id: toastId });
                    } finally {
                        setIsExporting(false);
                        setReportData(null);
                    }
                }
            }, 1500);

        } catch (error) {
            console.error("Lỗi xuất PDF:", error);
            toast.error("Không thể tạo báo cáo PDF tổng hợp.", { id: toastId });
            setIsExporting(false);
        }
    };

    const chartData = useMemo(() => {
        return latest12MonthsData.map(d => ({
            name: `T${d.month}/${d.year.toString().slice(-2)}`,
            tour: d.revenueTour,
            addon: d.revenueAddon,
            ads: d.revenueAds,
            total: d.totalRevenue
        }));
    }, [latest12MonthsData]);

    return (
        <div className="grid grid-cols-1 xl:grid-cols-12 gap-6 pb-10 font-sans relative">

            {/* HIDDEN REPORT TEMPLATE FOR CAPTURE */}
            {reportData && (
                <div className="fixed left-[-9999px] top-0 z-[-1]">
                    <div ref={reportRef}>
                        <ReportTemplate
                            data={reportData.data}
                            chartImages={reportData.chartImages}
                            period={selectedDate.month
                                ? `Tháng ${selectedDate.month}/${selectedDate.year}`
                                : `Cả năm ${selectedDate.year}`}
                            user={user}
                        />
                    </div>
                </div>
            )}

            {/* Left Area (Main) */}
            <div className="xl:col-span-8 space-y-6">

                {/* Welcome & Filter Card */}
                <div className="bg-white rounded-xl p-8 border border-slate-100 shadow-[0_15px_50px_rgba(0,0,0,0.08)]">
                    <div className="flex flex-col md:flex-row justify-between items-start md:items-center mb-8 gap-4">
                        <div>
                            <h1 className="text-2xl font-bold text-slate-800">Xin chào, {user?.username}</h1>
                            <div className="flex gap-3 mt-4">
                                <div className="flex flex-col gap-1">
                                    <label className="text-[10px] font-bold text-slate-400 uppercase ml-1">Ngày</label>
                                    <select
                                        className="text-xs font-bold bg-slate-50 border border-slate-100 rounded-lg px-3 py-2 outline-none focus:border-[#B48279] shadow-sm"
                                        value={selectedDate.day}
                                        onChange={(e) => setSelectedDate(prev => ({ ...prev, day: parseInt(e.target.value) }))}
                                    >
                                        {Array.from({ length: 31 }, (_, i) => (
                                            <option key={i + 1} value={i + 1}>{i + 1}</option>
                                        ))}
                                    </select>
                                </div>
                                <div className="flex flex-col gap-1">
                                    <label className="text-[10px] font-bold text-slate-400 uppercase ml-1">Tháng</label>
                                    <select
                                        className="text-xs font-bold bg-slate-50 border border-slate-100 rounded-lg px-3 py-2 outline-none focus:border-[#B48279] shadow-sm"
                                        value={selectedDate.month}
                                        onChange={(e) => setSelectedDate(prev => ({ ...prev, month: e.target.value === '' ? '' : parseInt(e.target.value) }))}
                                    >
                                        <option value="">Cả năm</option>
                                        {Array.from({ length: 12 }, (_, i) => (
                                            <option key={i + 1} value={i + 1}>Tháng {i + 1}</option>
                                        ))}
                                    </select>
                                </div>
                                <div className="flex flex-col gap-1">
                                    <label className="text-[10px] font-bold text-slate-400 uppercase ml-1">Năm</label>
                                    <select
                                        className="text-xs font-bold bg-slate-50 border border-slate-100 rounded-lg px-3 py-2 outline-none focus:border-[#B48279] shadow-sm"
                                        value={selectedDate.year}
                                        onChange={(e) => setSelectedDate(prev => ({ ...prev, year: parseInt(e.target.value) }))}
                                    >
                                        {[2024, 2025, 2026].map(y => <option key={y} value={y}>Năm {y}</option>)}
                                    </select>
                                </div>
                            </div>
                        </div>

                        <AdminButton
                            variant="primary"
                            icon={isExporting ? FaSpinner : FaFilePdf}
                            onClick={handleExportComprehensivePdf}
                            disabled={isExporting}
                            className={isExporting ? 'animate-pulse' : ''}
                        >
                            {isExporting ? 'ĐANG XUẤT...' : 'XUẤT BÁO CÁO TỔNG HỢP'}
                        </AdminButton>
                    </div>

                    {/* Stats Row */}
                    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
                        <div className="p-5 bg-slate-900 border border-slate-900 rounded-lg relative overflow-hidden shadow-lg">
                            <div className="text-[10px] font-bold uppercase tracking-widest text-slate-400 mb-1">Tổng doanh thu</div>
                            <div className="text-lg font-bold text-white mb-2">{formatCurrency(monthData?.totalRevenue)}</div>
                            <div className={`text-[10px] font-bold flex items-center gap-1 ${monthData?.totalGrowth >= 0 ? 'text-emerald-400' : 'text-rose-400'}`}>
                                {monthData?.totalGrowth >= 0 ? '▲' : '▼'} {Math.abs(monthData?.totalGrowth || 0).toFixed(1)}%
                            </div>
                        </div>

                        <div className="p-5 bg-white border border-slate-100 rounded-lg shadow-sm">
                            <div className="flex items-center gap-3 mb-3">
                                <div className="w-8 h-8 rounded-lg bg-slate-100 text-slate-800 flex items-center justify-center text-sm"><FaShoppingBag /></div>
                                <div className="text-[10px] font-bold text-slate-400 uppercase tracking-wider">Doanh thu Tour</div>
                            </div>
                            <div className="text-lg font-bold text-slate-900 leading-none mb-2">{formatCompactNumber(monthData?.revenueTour)}</div>
                            <div className={`text-[9px] font-bold flex items-center gap-1 ${monthData?.tourGrowth >= 0 ? 'text-emerald-500' : 'text-rose-500'}`}>
                                {monthData?.tourGrowth >= 0 ? '▲' : '▼'} {Math.abs(monthData?.tourGrowth || 0).toFixed(1)}%
                            </div>
                        </div>

                        <div className="p-5 bg-white border border-slate-100 rounded-lg shadow-sm">
                            <div className="flex items-center gap-3 mb-3">
                                <div className="w-8 h-8 rounded-lg bg-blue-50 text-[#129AF2] flex items-center justify-center text-sm"><FaWallet /></div>
                                <div className="text-[10px] font-bold text-slate-400 uppercase tracking-wider">Add-on</div>
                            </div>
                            <div className="text-lg font-bold text-slate-900 leading-none mb-2">{formatCompactNumber(monthData?.revenueAddon)}</div>
                            <div className={`text-[9px] font-bold flex items-center gap-1 ${monthData?.addonGrowth >= 0 ? 'text-emerald-500' : 'text-rose-500'}`}>
                                {monthData?.addonGrowth >= 0 ? '▲' : '▼'} {Math.abs(monthData?.addonGrowth || 0).toFixed(1)}%
                            </div>
                        </div>

                        <div className="p-5 bg-white border border-slate-100 rounded-lg shadow-sm">
                            <div className="flex items-center gap-3 mb-3">
                                <div className="w-8 h-8 rounded-lg bg-slate-50 text-slate-400 flex items-center justify-center text-sm"><FaAd /></div>
                                <div className="text-[10px] font-bold text-slate-400 uppercase tracking-wider">Quảng cáo</div>
                            </div>
                            <div className="text-lg font-bold text-slate-900 leading-none mb-2">{formatCompactNumber(monthData?.revenueAds)}</div>
                            <div className={`text-[9px] font-bold flex items-center gap-1 ${monthData?.adsGrowth >= 0 ? 'text-emerald-500' : 'text-rose-500'}`}>
                                {monthData?.adsGrowth >= 0 ? '▲' : '▼'} {Math.abs(monthData?.adsGrowth || 0).toFixed(1)}%
                            </div>
                        </div>
                    </div>
                </div>

                {/* Monthly Revenue Breakdown Chart */}
                <div className="bg-white rounded-xl p-6 border border-slate-100 shadow-[0_15px_50px_rgba(0,0,0,0.08)]">
                    <div className="flex justify-between items-center mb-6">
                        <div>
                            <h2 className="text-lg font-bold text-slate-800">Cơ cấu doanh thu Tháng {selectedDate.month}</h2>
                            <p className="text-[10px] text-slate-400 font-bold uppercase tracking-wider mt-1">Phân tích chi tiết 3 loại doanh thu chính</p>
                        </div>
                    </div>

                    <div ref={barChartRef} className="h-[300px] w-full bg-white">
                        <ResponsiveContainer width="100%" height="100%">
                            <BarChart data={monthlyBarData} margin={{ top: 20, right: 30, left: 0, bottom: 5 }}>
                                <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#f1f5f9" />
                                <XAxis dataKey="name" tick={{ fontSize: 11, fontWeight: 700, fill: '#64748b' }} axisLine={false} tickLine={false} />
                                <YAxis tick={{ fontSize: 10, fontWeight: 700, fill: '#94a3b8' }} axisLine={false} tickLine={false} tickFormatter={(val) => formatCompactNumber(val)} />
                                <Tooltip
                                    cursor={{ fill: '#f8fafc' }}
                                    contentStyle={{ borderRadius: '12px', border: 'none', boxShadow: '0 10px 15px -3px rgba(0,0,0,0.1)', fontSize: '11px', fontWeight: '700' }}
                                    formatter={(value) => formatCurrency(value)}
                                />
                                <Bar dataKey="value" barSize={60} radius={[6, 6, 0, 0]}>
                                    {monthlyBarData.map((entry, index) => (
                                        <Cell key={`cell-${index}`} fill={entry.color} />
                                    ))}
                                    <LabelList
                                        dataKey="value"
                                        position="top"
                                        formatter={(val) => formatCompactNumber(val)}
                                        style={{ fontSize: 11, fontWeight: '800', fill: '#475569' }}
                                    />
                                </Bar>
                            </BarChart>
                        </ResponsiveContainer>
                    </div>
                </div>

                {/* Revenue Chart Section */}
                <div className="bg-white rounded-xl p-6 border border-slate-100 shadow-[0_15px_50px_rgba(0,0,0,0.08)]">
                    <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center mb-6 gap-4">
                        <div>
                            <h2 className="text-lg font-bold text-slate-800">Xu hướng doanh thu 12 tháng gần đây</h2>
                            <p className="text-[10px] text-slate-400 font-bold uppercase tracking-wider mt-1">Lịch sử tăng trưởng hệ thống</p>
                        </div>
                    </div>

                    <div className="flex flex-wrap gap-4 mb-8">
                        <label className="flex items-center gap-2 cursor-pointer group">
                            <input
                                type="checkbox"
                                checked={visibleSeries.tour}
                                onChange={() => setVisibleSeries(prev => ({ ...prev, tour: !prev.tour }))}
                                className="w-4 h-4 rounded-full accent-slate-800"
                            />
                            <span className="text-[11px] font-bold text-slate-500 group-hover:text-slate-800">Doanh thu Tour</span>
                            <div className="w-8 h-0.5 bg-slate-800"></div>
                        </label>
                        <label className="flex items-center gap-2 cursor-pointer group">
                            <input
                                type="checkbox"
                                checked={visibleSeries.addon}
                                onChange={() => setVisibleSeries(prev => ({ ...prev, addon: !prev.addon }))}
                                className="w-4 h-4 rounded-full accent-[#129AF2]"
                            />
                            <span className="text-[11px] font-bold text-slate-500 group-hover:text-slate-800">Dịch vụ đi kèm</span>
                            <div className="w-8 h-0.5 bg-[#129AF2]"></div>
                        </label>
                        <label className="flex items-center gap-2 cursor-pointer group">
                            <input
                                type="checkbox"
                                checked={visibleSeries.ads}
                                onChange={() => setVisibleSeries(prev => ({ ...prev, ads: !prev.ads }))}
                                className="w-4 h-4 rounded-full accent-slate-400"
                            />
                            <span className="text-[11px] font-bold text-slate-500 group-hover:text-slate-800">Quảng cáo</span>
                            <div className="w-8 h-0.5 bg-slate-400"></div>
                        </label>
                    </div>

                    <div className="h-[300px] w-full relative" ref={lineChartRef} id="revenue-chart-section">
                        {isLoading && (
                            <div className="absolute inset-0 z-10 bg-white/60 backdrop-blur-[1px] flex items-center justify-center">
                                <FaSpinner className="animate-spin text-2xl text-[#129AF2]" />
                            </div>
                        )}
                        <ResponsiveContainer width="100%" height="100%">
                            <LineChart data={chartData} margin={{ top: 10, right: 10, left: 0, bottom: 0 }}>
                                <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#f1f5f9" />
                                <XAxis
                                    dataKey="name"
                                    axisLine={false}
                                    tickLine={false}
                                    tick={{ fontSize: 10, fontWeight: 700, fill: '#94a3b8' }}
                                    padding={{ left: 20, right: 20 }}
                                />
                                <YAxis
                                    axisLine={false}
                                    tickLine={false}
                                    tick={{ fontSize: 10, fontWeight: 700, fill: '#94a3b8' }}
                                    tickFormatter={(val) => formatCompactNumber(val)}
                                />
                                <Tooltip
                                    contentStyle={{ borderRadius: '12px', border: 'none', boxShadow: '0 10px 15px -3px rgba(0,0,0,0.1)', fontSize: '11px', fontWeight: '700' }}
                                    formatter={(value) => formatCurrency(value)}
                                />
                                {visibleSeries.tour && (
                                    <Line
                                        type="monotone"
                                        dataKey="tour"
                                        stroke="#1e293b"
                                        strokeWidth={3}
                                        dot={{ r: 4, strokeWidth: 2, fill: '#fff' }}
                                        activeDot={{ r: 6, strokeWidth: 0 }}
                                        animationDuration={1500}
                                    />
                                )}
                                {visibleSeries.addon && (
                                    <Line
                                        type="monotone"
                                        dataKey="addon"
                                        stroke="#129AF2"
                                        strokeWidth={3}
                                        dot={{ r: 4, strokeWidth: 2, fill: '#fff' }}
                                        activeDot={{ r: 6, strokeWidth: 0 }}
                                        animationDuration={1500}
                                    />
                                )}
                                {visibleSeries.ads && (
                                    <Line
                                        type="monotone"
                                        dataKey="ads"
                                        stroke="#94a3b8"
                                        strokeWidth={3}
                                        dot={{ r: 4, strokeWidth: 2, fill: '#fff' }}
                                        activeDot={{ r: 6, strokeWidth: 0 }}
                                        animationDuration={1500}
                                    />
                                )}
                            </LineChart>
                        </ResponsiveContainer>
                    </div>
                </div>

                {/* Detailed Occupancy Statistics Table */}
                <div className="bg-white rounded-xl p-6 border border-slate-100 shadow-[0_15px_50px_rgba(0,0,0,0.08)]">
                    <div className="flex justify-between items-center mb-6">
                        <div>
                            <h2 className="text-lg font-bold text-slate-800">Tỉ lệ lấp đầy Tour (Kỳ báo cáo)</h2>
                            <p className="text-[10px] text-slate-400 font-bold uppercase tracking-wider mt-1">Chi tiết hiệu suất dựa trên các Tour đã Hoàn thành</p>
                        </div>
                    </div>

                    <div className="overflow-x-auto">
                        <table className="w-full text-left border-collapse">
                            <thead>
                                <tr className="border-b border-slate-50">
                                    <th className="py-4 px-2 text-[10px] font-bold text-slate-400 uppercase tracking-widest">Tên Tour</th>
                                    <th className="py-4 px-2 text-[10px] font-bold text-slate-400 uppercase tracking-widest text-center">Số lượt</th>
                                    <th className="py-4 px-2 text-[10px] font-bold text-slate-400 uppercase tracking-widest text-center">Tổng chỗ</th>
                                    <th className="py-4 px-2 text-[10px] font-bold text-slate-400 uppercase tracking-widest text-center">Đã đặt</th>
                                    <th className="py-4 px-2 text-[10px] font-bold text-slate-400 uppercase tracking-widest text-right">Lấp đầy</th>
                                </tr>
                            </thead>
                            <tbody className="divide-y divide-slate-50">
                                {isLoading ? (
                                    <tr>
                                        <td colSpan="5" className="py-10 text-center">
                                            <FaSpinner className="animate-spin inline-block text-slate-200 text-xl" />
                                        </td>
                                    </tr>
                                ) : occupancyData.length === 0 ? (
                                    <tr>
                                        <td colSpan="5" className="py-10 text-center text-slate-400 text-sm italic">Chưa có dữ liệu thống kê trong kỳ này.</td>
                                    </tr>
                                ) : (
                                    occupancyData.map((item) => (
                                        <tr key={item.tourId} className="hover:bg-slate-50 transition-colors">
                                            <td className="py-4 px-2">
                                                <div className="text-xs font-bold text-slate-700 line-clamp-1">{item.tourName}</div>
                                                <div className="text-[9px] text-slate-400 font-mono mt-0.5">{item.tourId.substring(0, 8)}</div>
                                            </td>
                                            <td className="py-4 px-2 text-center text-xs font-bold text-slate-600">{item.runCount}</td>
                                            <td className="py-4 px-2 text-center text-xs font-bold text-slate-600">{item.totalSlots}</td>
                                            <td className="py-4 px-2 text-center text-xs font-bold text-slate-900">{item.bookedSlots}</td>
                                            <td className="py-4 px-2 text-right">
                                                <div className="flex items-center justify-end gap-2">
                                                    <div className="w-16 bg-slate-100 h-1.5 rounded-full overflow-hidden">
                                                        <div
                                                            className="h-full bg-slate-800 rounded-full"
                                                            style={{ width: `${Math.min(item.averageOccupancy, 100)}%` }}
                                                        ></div>
                                                    </div>
                                                    <span className="text-xs font-bold text-slate-800">{item.averageOccupancy}%</span>
                                                </div>
                                            </td>
                                        </tr>
                                    ))
                                )}
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>

            {/* Right Sidebar Area */}
            <div className="xl:col-span-4 space-y-6">

                {/* Profile Card */}
                <div className="bg-slate-50 rounded-xl p-8 text-center border border-slate-100 shadow-[0_15px_50px_rgba(0,0,0,0.08)] flex flex-col items-center">
                    <div className="relative mb-5">
                        <div className="w-24 h-24 rounded-full p-1 bg-white border border-slate-200 shadow-sm">
                            <img
                                src={user?.avatarUrl || `https://ui-avatars.com/api/?name=${user?.username}&background=f1f5f9&color=64748b`}
                                onError={handleImageError}
                                className="w-full h-full rounded-full object-cover"
                            />
                        </div>
                        <span className="absolute bottom-1 right-1 w-4 h-4 bg-emerald-500 border-2 border-white rounded-full"></span>
                    </div>
                    <div className="text-[10px] font-bold text-slate-400 uppercase tracking-widest mb-1">Họ và tên</div>
                    <h3 className="font-bold text-slate-900 text-lg mb-1">{user?.username}</h3>
                    <p className="text-xs text-slate-500 mb-6 lowercase">{user?.email}</p>

                    <AdminButton
                        variant="secondary"
                        onClick={() => setIsProfileModalOpen(true)}
                        className="w-full justify-center mt-6"
                        icon={FaUserEdit}
                    >
                        SỬA THÔNG TIN
                    </AdminButton>
                </div>

                {/* Today's Bookings Card */}
                <div className="bg-white rounded-xl p-6 border border-slate-100 shadow-[0_15px_50px_rgba(0,0,0,0.08)]">
                    <div className="flex justify-between items-center mb-6">
                        <div className="flex items-center gap-2">
                            <div className="w-8 h-8 rounded-lg bg-indigo-50 text-indigo-500 flex items-center justify-center text-sm"><FaHistory /></div>
                            <h3 className="font-bold text-slate-800 text-sm">Đặt chỗ ngày chọn</h3>
                        </div>
                    </div>

                    <div className="space-y-4">
                        {isExtraLoading ? (
                            <div className="py-4 text-center"><FaSpinner className="animate-spin inline-block text-slate-200" /></div>
                        ) : dailyData.length === 0 ? (
                            <div className="py-6 text-center text-slate-400 text-xs italic">Không có đơn hàng nào.</div>
                        ) : (
                            dailyData.map((item) => (
                                <div key={item.tourId} className="flex items-center gap-3 p-3 rounded-lg border border-slate-50 hover:border-slate-100 transition-all">
                                    <div className="flex-1 min-w-0">
                                        <div className="text-[11px] font-bold text-slate-800 truncate">{item.tourName}</div>
                                        <div className="flex items-center gap-2 mt-1">
                                            <span className="text-[9px] font-bold text-[#129AF2] bg-blue-50 px-1.5 py-0.5 rounded uppercase">{item.bookingCount} đơn</span>
                                            <span className="text-[9px] font-bold text-slate-400">|</span>
                                            <span className="text-[9px] font-bold text-slate-900">{item.totalQuantity} chỗ</span>
                                        </div>
                                    </div>
                                    <div className="w-6 h-6 rounded-full bg-emerald-50 text-emerald-500 flex items-center justify-center text-[10px]">
                                        <FaCheckCircle />
                                    </div>
                                </div>
                            ))
                        )}
                    </div>

                    <p className="mt-6 text-[10px] text-slate-400 text-center leading-relaxed">
                        Dữ liệu dựa trên bộ lọc Ngày/Tháng/Năm phía trên.
                    </p>
                </div>

            </div>

            <ProfileModal
                isOpen={isProfileModalOpen}
                onClose={() => setIsProfileModalOpen(false)}
            />
        </div>
    );
};

export default DashboardHome;
