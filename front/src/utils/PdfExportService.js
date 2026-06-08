import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';

/**
 * Utility service to export data to PDF with professional styling.
 * Addresses "split objects across pages" and "single page fit" requirements.
 */
export const exportToPdf = ({ title, filename, headers, data, orientation = 'p' }) => {
    // orientation 'p' for portrait, 'l' for landscape
    const doc = new jsPDF(orientation, 'pt', 'a4');
    
    // Add custom font/branding if needed, here we use default
    const titleText = title || 'FoxTrip Report';
    const dateText = `Ngày xuất: ${new Date().toLocaleString('vi-VN')}`;
    
    // Header
    doc.setFontSize(20);
    doc.setTextColor(180, 130, 121); // Primary color #B48279
    doc.text(titleText, 40, 50);
    
    doc.setFontSize(10);
    doc.setTextColor(100);
    doc.text(dateText, 40, 70);
    
    // Line separator
    doc.setDrawColor(200);
    doc.line(40, 80, orientation === 'p' ? 555 : 800, 80);

    // AutoTable configuration
    autoTable(doc, {
        head: [headers],
        body: data,
        startY: 100,
        theme: 'striped',
        headStyles: { 
            fillColor: [180, 130, 121], 
            textColor: [255, 255, 255],
            fontSize: 10,
            halign: 'center'
        },
        bodyStyles: { 
            fontSize: 9,
            halign: 'center'
        },
        alternateRowStyles: {
            fillColor: [250, 248, 247]
        },
        margin: { top: 100, bottom: 40, left: 40, right: 40 },
        pageBreak: 'avoid', // Crucial: avoid splitting a single row across pages
        didDrawPage: (data) => {
            // Footer
            const str = "Trang " + doc.internal.getNumberOfPages();
            doc.setFontSize(9);
            doc.setTextColor(150);
            const pageSize = doc.internal.pageSize;
            const pageHeight = pageSize.height ? pageSize.height : pageSize.getHeight();
            doc.text(str, data.settings.margin.left, pageHeight - 20);
            doc.text("Hệ thống quản lý FoxTrip", pageSize.width - 150, pageHeight - 20);
        }
    });

    doc.save(`${filename || 'report'}.pdf`);
};

export const exportOccupancyToPdf = (data) => {
    const headers = ['Tour ID', 'Tên Tour', 'Số lượt tổ chức', 'Tổng chỗ', 'Đã đặt', 'Tỉ lệ lấp đầy'];
    const tableData = data.map(item => [
        item.tourId.substring(0, 8),
        item.tourName,
        item.runCount,
        item.totalSlots,
        item.bookedSlots,
        `${item.averageOccupancy}%`
    ]);
    
    exportToPdf({
        title: 'Báo cáo Tỉ lệ lấp đầy Tour (4 tháng)',
        filename: `Bao-cao-lap-day-${new Date().toISOString().split('T')[0]}`,
        headers,
        data: tableData,
        orientation: 'l' // Landscape for wide tables
    });
};

export const exportDailyBookingsToPdf = (data, date) => {
    const headers = ['ID Tour', 'Tên Tour', 'Số đơn hàng', 'Số chỗ đã đặt'];
    const tableData = data.map(item => [
        item.tourId.substring(0, 8),
        item.tourName,
        item.bookingCount,
        item.totalQuantity
    ]);
    
    exportToPdf({
        title: `Báo cáo Đặt chỗ ngày ${date}`,
        filename: `Dat-cho-ngay-${date}`,
        headers,
        data: tableData
    });
};

export const exportComprehensiveReportToPdf = ({ reportImages, period }) => {
    const doc = new jsPDF('p', 'pt', 'a4');
    const pageWidth = doc.internal.pageSize.getWidth();
    const pageHeight = doc.internal.pageSize.getHeight();
    
    reportImages.forEach((imgData, index) => {
        if (index > 0) {
            doc.addPage();
        }
        // Each image is a full A4 page from our template
        doc.addImage(imgData, 'PNG', 0, 0, pageWidth, pageHeight, undefined, 'FAST');
    });

    doc.save(`Bao_cao_tong_hop_${period.replace(/[\/ ]/g, '_')}.pdf`);
};
