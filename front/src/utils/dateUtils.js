export const toBackendISO = (localDateTimeString) => {
    if (!localDateTimeString) return null;
    // Đảm bảo parse chuỗi local thành giờ VN (+07:00) rồi mới chuyển sang UTC (Z)
    const date = new Date(`${localDateTimeString}:00+07:00`);
    return !isNaN(date.getTime()) ? date.toISOString() : null;
};

export const formatToVN = (isoString, includeTime = true) => {
    if (!isoString) return '';
    try {
        const date = new Date(isoString);
        if (isNaN(date.getTime())) return isoString;

        const options = {
            timeZone: 'Asia/Ho_Chi_Minh',
            day: '2-digit',
            month: '2-digit',
            year: 'numeric',
            hour12: false
        };

        if (includeTime) {
            options.hour = '2-digit';
            options.minute = '2-digit';
        }

        return new Intl.DateTimeFormat('vi-VN', options).format(date);
    } catch (e) {
        console.error("Error formatting date:", e);
        return isoString;
    }
};

export const toDateTimeLocalValue = (isoString) => {
    if (!isoString) return '';
    try {
        const date = new Date(isoString);
        if (isNaN(date.getTime())) return '';

        const parts = new Intl.DateTimeFormat('en-US', {
            timeZone: 'Asia/Ho_Chi_Minh',
            year: 'numeric',
            month: '2-digit',
            day: '2-digit',
            hour: '2-digit',
            minute: '2-digit',
            hour12: false
        }).formatToParts(date);

        const getValue = (type) => parts.find(p => p.type === type).value;

        return `${getValue('year')}-${getValue('month')}-${getValue('day')}T${getValue('hour')}:${getValue('minute')}`;
    } catch (e) {
        return '';
    }
};
