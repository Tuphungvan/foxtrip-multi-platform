import React from 'react';

const ReportTemplate = ({ data, period, user, chartImages }) => {
    const { revenueReport, occupancyReport, adLocationCount } = data;
    const formatCurrency = (val) => new Intl.NumberFormat('vi-VN').format(val) + ' đ';

    const mapRole = (role) => {
        if (role === 'SUPER_ADMIN') return 'Quản trị viên cấp cao';
        if (role === 'ADMIN') return 'Quản trị viên';
        return role;
    };

    const PageWrapper = ({ children, pageNum, totalPages }) => (
        <div className="report-page bg-white w-[800px] min-h-[1130px] p-10 border border-black flex flex-col mb-10 text-[13px] font-sans">
            <div className="flex-1">
                {children}
            </div>
            <div className="mt-4 pt-2 border-t border-black flex justify-between items-center font-bold">
                <div>BÁO CÁO KINH DOANH THÁNG</div>
                <div>TRANG {pageNum} / {totalPages}</div>
            </div>
        </div>
    );

    return (
        <div id="report-template-container">
            {/* TRANG 1 */}
            <PageWrapper pageNum={1} totalPages={3}>
                <div className="flex justify-between items-start mb-8">
                    <div className="text-left text-[12px] leading-relaxed">
                        <div className="font-bold uppercase text-[13px]">Công ty Cổ phần Foxtrip</div>
                        <div>Địa chỉ: Trịnh Văn Bô, Nam Từ Liêm, Hà Nội</div>
                        <div>SĐT: 0859 605 024</div>
                        <div>Email: foxtripgroup@gmail.com</div>
                    </div>
                    <div className="text-right text-[12px] leading-relaxed">
                        <div className="font-bold uppercase text-[13px] text-slate-700">Bộ phận Quản lý Hệ thống</div>
                        <div className="italic text-slate-500 mt-1">Ngày xuất báo cáo: {new Date().getDate()}/{new Date().getMonth() + 1}/{new Date().getFullYear()}</div>
                    </div>
                </div>

                <div className="text-center pb-4 mb-6 border-b border-black">
                    <h1 className="font-bold uppercase text-[16px]">BÁO CÁO KINH DOANH {period ? period.toUpperCase() : ''}</h1>
                </div>

                <div className="grid grid-cols-2 border border-black mb-6">
                    <div className="border-r border-black p-3 space-y-1">
                        <p>Người lập: {user?.username || 'Phùng Văn Tú'}</p>
                        <p>Email: {user?.email || 'admin@foxtrip.vn'}</p>
                        <p>Chức vụ: {mapRole(user?.role)}</p>
                    </div>
                    <div className="p-3 space-y-1 text-right">
                        <p>Kỳ báo cáo: {period}</p>
                        <p>Ngày tạo: {new Date().toLocaleString('vi-VN')}</p>
                    </div>
                </div>

                <div className="mb-6">
                    <h2 className="font-bold mb-2 uppercase">1. Thống kê doanh thu</h2>
                    <table className="w-full border-collapse border border-black">
                        <thead>
                            <tr className="bg-gray-50 font-bold">
                                <th className="border border-black p-2 text-left">Hạng mục</th>
                                <th className="border border-black p-2 text-right">Số tiền</th>
                                <th className="border border-black p-2 text-center">Tăng trưởng</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr>
                                <td className="border border-black p-2">Doanh thu Tour du lịch</td>
                                <td className="border border-black p-2 text-right">{formatCurrency(revenueReport.revenueTour)}</td>
                                <td className="border border-black p-2 text-center">{revenueReport.tourGrowth}%</td>
                            </tr>
                            <tr>
                                <td className="border border-black p-2">Doanh thu dịch vụ đi kèm</td>
                                <td className="border border-black p-2 text-right">{formatCurrency(revenueReport.revenueAddon)}</td>
                                <td className="border border-black p-2 text-center">{revenueReport.addonGrowth}%</td>
                            </tr>
                            <tr>
                                <td className="border border-black p-2">Doanh thu quảng cáo</td>
                                <td className="border border-black p-2 text-right">{formatCurrency(revenueReport.revenueAds)}</td>
                                <td className="border border-black p-2 text-center">{revenueReport.adsGrowth}%</td>
                            </tr>
                            <tr className="font-bold">
                                <td className="border border-black p-2">TỔNG CỘNG</td>
                                <td className="border border-black p-2 text-right">{formatCurrency(revenueReport.totalRevenue)}</td>
                                <td className="border border-black p-2 text-center">{revenueReport.totalGrowth}%</td>
                            </tr>
                        </tbody>
                    </table>
                </div>

                <div className="border border-black p-4">
                    <h2 className="font-bold mb-1 uppercase">2. Thống kê địa điểm quảng cáo</h2>
                    <p>Số lượng địa điểm thực hiện quảng cáo thuê vị trí: {adLocationCount} địa điểm.</p>
                </div>
            </PageWrapper>

            {/* TRANG 2 */}
            <PageWrapper pageNum={2} totalPages={3}>
                <h2 className="font-bold mb-4 uppercase">3. Biểu đồ phân tích</h2>
                <div className="space-y-6">
                    {chartImages.map((img, idx) => (
                        <div key={idx} className="border border-black p-2">
                            <img src={img} alt="chart" className="w-full grayscale" />
                            <p className="text-center mt-2 font-bold uppercase">Biểu đồ {idx + 1}</p>
                        </div>
                    ))}
                </div>
            </PageWrapper>

            {/* TRANG 3 */}
            <PageWrapper pageNum={3} totalPages={3}>
                <div className="mb-8">
                    <h2 className="font-bold mb-2 uppercase">4. Tỉ lệ lấp đầy Tour</h2>
                    <table className="w-full border-collapse border border-black text-[12px]">
                        <thead>
                            <tr className="bg-gray-50 font-bold">
                                <th className="border border-black p-2 text-left">Tên Tour</th>
                                <th className="border border-black p-2 text-center">Lượt</th>
                                <th className="border border-black p-2 text-center">Đã đặt / Tổng</th>
                                <th className="border border-black p-2 text-right">Lấp đầy (%)</th>
                            </tr>
                        </thead>
                        <tbody>
                            {occupancyReport.map((item, idx) => (
                                <tr key={idx}>
                                    <td className="border border-black p-2">{item.tourName}</td>
                                    <td className="border border-black p-2 text-center">{item.runCount}</td>
                                    <td className="border border-black p-2 text-center">{item.bookedSlots} / {item.totalSlots}</td>
                                    <td className="border border-black p-2 text-right font-bold">{item.averageOccupancy}%</td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>

                <div className="mt-auto pt-10 grid grid-cols-2 text-center font-bold">
                    <div>
                        <p className="mb-20 uppercase">Người lập báo cáo</p>
                        <p>{user?.username || 'Phùng Văn Tú'}</p>
                    </div>
                    <div>
                        <p className="mb-20 uppercase">Xác nhận hệ thống</p>
                        <p>FOXTRIP SYSTEM</p>
                    </div>
                </div>
            </PageWrapper>
        </div>
    );
};

export default ReportTemplate;
