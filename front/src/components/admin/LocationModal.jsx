import { useState, useEffect } from 'react';
import {
    uploadImageToCloudinary, uploadImageUrlToCloudinary, createAdminLocation, updateAdminLocation,
    goongAutoComplete, goongPlaceDetail
} from '../../services/api/adminApi';
import {
    FaTimes, FaSpinner, FaCloudUploadAlt, FaCheck, FaSearch, FaMapMarkerAlt
} from 'react-icons/fa';
import { PROVINCES, LOCATION_TYPES } from '../../utils/constants';
import toast from 'react-hot-toast';
import AdminButton from './ui/AdminButton';

const LocationModal = ({ isOpen, onClose, onSuccess, editingLocation, hideAdFields = false }) => {
    const [lat, setLat] = useState('');
    const [lng, setLng] = useState('');

    // Image handling - 2 modes
    const [imageMode, setImageMode] = useState('upload'); // 'upload' | 'url'
    const [imageFile, setImageFile] = useState(null);
    const [imageUrl, setImageUrl] = useState('');
    const [imagePreview, setImagePreview] = useState(null);

    const [customName, setCustomName] = useState('');
    const [customAddress, setCustomAddress] = useState('');
    const [selectedType, setSelectedType] = useState('ATTRACTION');
    const [selectedProvince, setSelectedProvince] = useState('HA_NOI');
    const [contactPhone, setContactPhone] = useState('');

    // Advertisement fields
    const [isAdvertisement, setIsAdvertisement] = useState(false);
    const [priority, setPriority] = useState(1);
    const [featuredStartAt, setFeaturedStartAt] = useState('');
    const [featuredEndAt, setFeaturedEndAt] = useState('');

    const [isCreating, setIsCreating] = useState(false);
    const [isUploading, setIsUploading] = useState(false);

    // Goong Search State
    const [goongKeyword, setGoongKeyword] = useState('');
    const [goongResults, setGoongResults] = useState([]);
    const [isSearchingGoong, setIsSearchingGoong] = useState(false);
    const [showGoongDropdown, setShowGoongDropdown] = useState(false);
    const [mapboxPlaceId, setMapboxPlaceId] = useState('');

    useEffect(() => {
        if (!isOpen) {
            setImageFile(null); setImageUrl(''); setImagePreview(null); setImageMode('upload');
            setCustomName(''); setCustomAddress(''); setLat(''); setLng(''); setContactPhone('');
            setIsAdvertisement(false); setPriority(1); setFeaturedStartAt(''); setFeaturedEndAt('');
            setGoongKeyword(''); setGoongResults([]); setShowGoongDropdown(false); setMapboxPlaceId('');
        } else if (editingLocation) {
            setCustomName(editingLocation.name);
            setCustomAddress(editingLocation.address);
            setLat(editingLocation.lat);
            setLng(editingLocation.lng);
            setSelectedType(editingLocation.type);
            setSelectedProvince(editingLocation.province);
            setContactPhone(editingLocation.contactPhone || '');
            setImagePreview(editingLocation.imageUrl);
            setImageUrl(editingLocation.imageUrl);
            setMapboxPlaceId(editingLocation.mapboxPlaceId || '');

            // Check if it's advertisement
            const isAd = editingLocation.priority != null && editingLocation.priority > 0;
            setIsAdvertisement(isAd);
            setPriority(isAd ? editingLocation.priority : 1);

            const formatToLocalDateTime = (dateString) => {
                if (!dateString) return '';
                const d = new Date(dateString);
                const pad = (n) => n.toString().padStart(2, '0');
                return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`;
            };

            setFeaturedStartAt(formatToLocalDateTime(editingLocation.featuredStartAt));
            setFeaturedEndAt(formatToLocalDateTime(editingLocation.featuredEndAt));
        }
    }, [isOpen, editingLocation]);

    // Goong Autocomplete Effect
    useEffect(() => {
        if (!goongKeyword.trim() || !isOpen) {
            setGoongResults([]);
            setShowGoongDropdown(false);
            return;
        }

        const timer = setTimeout(async () => {
            setIsSearchingGoong(true);
            try {
                const res = await goongAutoComplete(goongKeyword);
                if (res?.data?.predictions) {
                    setGoongResults(res.data.predictions);
                    setShowGoongDropdown(true);
                }
            } catch (error) {
                console.error('Goong search error:', error);
            } finally {
                setIsSearchingGoong(false);
            }
        }, 500);

        return () => clearTimeout(timer);
    }, [goongKeyword, isOpen]);

    const handleSelectGoongPlace = async (prediction) => {
        setGoongKeyword(prediction.description);
        setShowGoongDropdown(false);
        setIsSearchingGoong(true);

        try {
            const res = await goongPlaceDetail(prediction.place_id);
            const result = res?.data?.result;
            if (result) {
                setCustomName(result.name || '');
                setCustomAddress(result.formatted_address || '');
                setMapboxPlaceId(result.place_id || '');
                
                if (result.geometry?.location) {
                    setLat(result.geometry.location.lat.toString());
                    setLng(result.geometry.location.lng.toString());
                }

                if (result.formatted_phone_number) {
                    setContactPhone(result.formatted_phone_number.replace(/\s+/g, ''));
                }

                // Xử lý ảnh từ Goong: Upload lên Cloudinary để lưu trữ vĩnh viễn
                if (result.first_photo_url) {
                    setIsUploading(true);
                    try {
                        const cloudinaryUrl = await uploadImageUrlToCloudinary(result.first_photo_url);
                        setImageUrl(cloudinaryUrl);
                        setImagePreview(cloudinaryUrl);
                        setImageMode('url');
                        toast.success('Đã tải và tối ưu ảnh từ Goong!');
                    } catch (uploadErr) {
                        console.error('Lỗi upload ảnh từ Goong:', uploadErr);
                        toast.error('Không thể tải ảnh từ Goong, vui lòng upload thủ công');
                    } finally {
                        setIsUploading(false);
                    }
                }

                toast.success('Đã tải thông tin từ Goong Map!');
            }
        } catch (error) {
            toast.error('Lỗi lấy chi tiết địa điểm từ Goong');
        } finally {
            setIsSearchingGoong(false);
        }
    };



    const handleCoordinateInput = (val) => {
        // Try parsing google maps url like http.../@16.0,108.0...
        const mapUrlRegex = /@(-?\d+\.\d+),(-?\d+\.\d+)/;
        const urlMatch = val.match(mapUrlRegex);
        if (urlMatch) {
            setLat(urlMatch[1]);
            setLng(urlMatch[2]);
            return;
        }
        // Try parsing direct lat,lng format e.g. "16.123, 108.456"
        const coordRegex = /(-?\d+\.\d+),\s*(-?\d+\.\d+)/;
        const coordMatch = val.match(coordRegex);
        if (coordMatch) {
            setLat(coordMatch[1]);
            setLng(coordMatch[2]);
            return;
        }
    };

    const handleImageChange = async (e) => {
        const file = e.target.files[0];
        if (file) {
            setImageFile(file);
            setImagePreview(URL.createObjectURL(file));
            setImageUrl(''); // Clear URL when file is selected

            // Upload ngay lập tức
            setIsUploading(true);
            try {
                toast.loading('Đang tải ảnh lên...', { id: 'upload-image' });
                const uploadedUrl = await uploadImageToCloudinary(file, 'foxtrip/locations');
                setImageUrl(uploadedUrl);
                setImagePreview(uploadedUrl);
                setImageFile(null); // Clear file sau khi upload thành công
                toast.success('Tải ảnh lên thành công!', { id: 'upload-image' });
            } catch (error) {
                toast.error('Lỗi tải ảnh: ' + error.message, { id: 'upload-image' });
                // Giữ lại preview local nếu upload thất bại
            } finally {
                setIsUploading(false);
            }
        }
    };

    const handleImageUrlChange = async (url) => {
        setImageUrl(url);
        setImagePreview(url);
        setImageFile(null); // Clear file when URL is entered

        if (url.trim()) {
            // Upload URL ảnh lên Cloudinary
            setIsUploading(true);
            try {
                toast.loading('Đang tải ảnh từ URL...', { id: 'upload-url' });
                const uploadedUrl = await uploadImageUrlToCloudinary(url, 'foxtrip/locations');
                setImageUrl(uploadedUrl);
                setImagePreview(uploadedUrl);
                toast.success('Tải ảnh từ URL thành công!', { id: 'upload-url' });
            } catch (error) {
                toast.error('Lỗi tải ảnh từ URL: ' + error.message, { id: 'upload-url' });
                // Giữ lại URL gốc nếu upload thất bại
            } finally {
                setIsUploading(false);
            }
        }
    };

    const handleSubmitLocation = async () => {
        // Validate image - bây giờ imageUrl đã có sẵn từ upload trước đó
        let finalImageUrl = imageUrl || editingLocation?.imageUrl || '';

        if (!finalImageUrl) {
            toast.error('Vui lòng chọn ảnh đại diện.');
            return;
        }

        // Strict Validation
        const finalName = customName.trim();
        if (!finalName) {
            toast.error('Vui lòng nhập Tên địa điểm.');
            return;
        }

        // Validate address
        if (!customAddress.trim()) {
            toast.error('Vui lòng nhập Địa chỉ.');
            return;
        }

        const parseLat = parseFloat(lat);
        const parseLng = parseFloat(lng);
        if (isNaN(parseLat) || isNaN(parseLng)) {
            toast.error('Vui lòng nhập đúng số Toạ độ.');
            return;
        }

        setIsCreating(true);
        try {
            const payload = {
                name: finalName,
                province: selectedProvince,
                address: customAddress.trim(),
                imageUrl: finalImageUrl,
                type: selectedType,
                lng: parseLng,
                lat: parseLat,
                contactPhone: contactPhone || null,
                mapboxPlaceId: mapboxPlaceId || null,
                // Advertisement fields
                priority: (!hideAdFields && isAdvertisement) ? priority : 0,
                featuredStartAt: (!hideAdFields && isAdvertisement && featuredStartAt) ? new Date(featuredStartAt).toISOString() : null,
                featuredEndAt: (!hideAdFields && isAdvertisement && featuredEndAt) ? new Date(featuredEndAt).toISOString() : null,
            };

            let res;
            if (editingLocation) {
                res = await updateAdminLocation(editingLocation.id, payload);
                console.log('Update location response:', res);
                toast.success('Cập nhật địa điểm thành công!');
            } else {
                res = await createAdminLocation(payload);
                console.log('Create location response:', res);
                toast.success('Tạo địa điểm mới thành công!');
            }
            // res structure after interceptor: { data: LocationResponseDTO, message: string }
            console.log('Calling onSuccess with:', res.data);
            onSuccess(res.data); // Pass the LocationResponseDTO
            onClose();
        } catch (err) {
            toast.error(err.message || 'Có lỗi xảy ra khi lưu địa điểm.');
        } finally {
            setIsCreating(false);
        }
    };

    if (!isOpen) return null;

    return (
        <div className="fixed inset-0 z-[100] flex items-center justify-center p-4 bg-slate-900/40 backdrop-blur-sm">
            <div className="bg-white rounded-[24px] shadow-2xl w-full max-w-md overflow-hidden flex flex-col max-h-[85vh] border border-slate-100">

                {/* Header */}
                <div className="px-6 py-5 border-b border-slate-100 flex justify-between items-center bg-white shrink-0">
                    <h3 className="text-lg font-bold text-slate-900">
                        {editingLocation ? 'Cập nhật Địa Điểm' : 'Thêm địa điểm'}
                    </h3>
                    <button onClick={onClose} className="p-2 bg-slate-50 hover:bg-slate-100 text-slate-500 rounded-full transition-colors">
                        <FaTimes className="text-sm" />
                    </button>
                </div>

                {/* Body */}
                <div className="p-6 overflow-y-auto flex-1 custom-scrollbar bg-slate-50/50">
                    <div className="space-y-4">
                        {/* Goong Map Search */}
                        <div className="relative mb-2">
                            <label className="block text-xs font-bold text-blue-800 mb-1.5">Tìm địa điểm mới (Goong Map)</label>
                            <div className="relative">
                                <input
                                    type="text"
                                    placeholder="Nhập tên địa điểm cần tìm..."
                                    value={goongKeyword}
                                    onChange={(e) => setGoongKeyword(e.target.value)}
                                    autoComplete="off"
                                    className="w-full pl-9 pr-10 py-2.5 text-sm bg-blue-50 border border-blue-200 rounded-xl outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 transition-all font-medium text-blue-900"
                                />
                                <FaSearch className="absolute left-3.5 top-1/2 -translate-y-1/2 text-blue-400" />
                                <div className="absolute right-3 top-1/2 -translate-y-1/2 flex items-center gap-2">
                                    {isSearchingGoong ? (
                                        <FaSpinner className="text-blue-400 animate-spin" />
                                    ) : goongKeyword && (
                                        <button 
                                            onClick={() => setGoongKeyword('')}
                                            className="p-1 hover:bg-blue-100 rounded-full text-blue-400 transition-colors"
                                        >
                                            <FaTimes size={12} />
                                        </button>
                                    )}
                                </div>
                            </div>

                            {showGoongDropdown && goongResults.length > 0 && (
                                <div className="absolute top-full left-0 z-[110] w-full mt-1 bg-white border border-slate-200 shadow-xl rounded-xl overflow-hidden max-h-60 overflow-y-auto">
                                    {goongResults.map((result) => (
                                        <div
                                            key={result.place_id}
                                            onClick={() => handleSelectGoongPlace(result)}
                                            className="px-4 py-3 border-b border-slate-50 last:border-0 hover:bg-blue-50 cursor-pointer transition-colors group"
                                        >
                                            <div className="flex items-start gap-3">
                                                <FaMapMarkerAlt className="mt-1 text-blue-400 group-hover:text-blue-600 shrink-0" />
                                                <div className="min-w-0 flex-1">
                                                    <div className="text-sm font-bold text-slate-800 truncate">{result.structured_formatting.main_text}</div>
                                                    <div className="text-[11px] text-slate-500 truncate">{result.structured_formatting.secondary_text}</div>
                                                </div>
                                            </div>
                                        </div>
                                    ))}
                                </div>
                            )}
                        </div>

                        {/* Location Context Header */}
                        <div className="p-3 bg-slate-100 border border-slate-200 rounded-xl flex items-start gap-3 relative mb-2">
                            <div className="w-1.5 h-1.5 rounded-full bg-emerald-500 mt-1.5 animate-pulse shrink-0"></div>
                            <div className="min-w-0 flex-1">
                                <div className="text-xs font-bold text-slate-700">Dữ liệu hiện tại</div>
                                <div className="text-[10px] text-slate-500 font-mono mt-0.5">
                                    {lat && lng ? `${parseFloat(lat).toFixed(6)}, ${parseFloat(lng).toFixed(6)}` : 'Chưa có toạ độ'}
                                    {mapboxPlaceId && ` | ID: ${mapboxPlaceId.substring(0, 8)}...`}
                                </div>
                            </div>
                        </div>

                        {/* Coordinates Input */}
                        <div className="p-3 border border-dashed border-emerald-300 bg-emerald-50 rounded-xl mb-4">
                            <label className="block text-[11px] font-bold text-emerald-800 mb-1.5 leading-snug">Toạ độ (Lat, Lng) <span className="text-red-500">*</span></label>
                            <p className="text-[10px] text-emerald-600 mb-2">Mẹo: Mở Google Maps, chuột phải vào địa điểm chọn mục toạ độ (VD: 16.05, 108.20). Hoặc copy Link dán vào đây, hệ thống tự trích xuất!</p>
                            <input
                                type="text"
                                placeholder="Dán Toạ độ hoặc Link Google Maps vào đây..."
                                onChange={(e) => handleCoordinateInput(e.target.value)}
                                className="w-full px-3 py-2 text-xs bg-white border border-emerald-200 rounded-md outline-none focus:border-emerald-500 shadow-sm text-slate-800 mb-2 placeholder:italic"
                            />
                            <div className="grid grid-cols-2 gap-2">
                                <div><span className="text-[10px] text-slate-500 font-semibold">Vĩ độ (Lat)</span><input type="number" step="any" required value={lat} onChange={(e) => setLat(e.target.value)} className="w-full px-2 py-1.5 text-xs bg-white border border-slate-200 rounded-md outline-none" /></div>
                                <div><span className="text-[10px] text-slate-500 font-semibold">Kinh độ (Lng)</span><input type="number" step="any" required value={lng} onChange={(e) => setLng(e.target.value)} className="w-full px-2 py-1.5 text-xs bg-white border border-slate-200 rounded-md outline-none" /></div>
                            </div>
                        </div>

                        <div className="space-y-4">
                            <div>
                                <label className="block text-xs font-semibold text-slate-600 mb-1.5">Tên địa điểm <span className="text-red-500">*</span></label>
                                <input type="text" placeholder="Nhập tên chính xác..." value={customName} onChange={(e) => setCustomName(e.target.value)} className="w-full px-3 py-2 text-sm bg-white border border-slate-200 rounded-lg outline-none focus:border-blue-400 shadow-sm text-slate-800" />
                            </div>

                            <div>
                                <label className="block text-xs font-semibold text-slate-600 mb-1.5">Địa chỉ <span className="text-red-500">*</span></label>
                                <textarea
                                    rows="2"
                                    placeholder="Nhập địa chỉ chi tiết (VD: 123 Đường ABC, Phường XYZ, Quận 1)"
                                    value={customAddress}
                                    onChange={(e) => setCustomAddress(e.target.value)}
                                    className="w-full px-3 py-2 text-sm bg-white border border-slate-200 rounded-lg outline-none focus:border-blue-400 shadow-sm text-slate-800 resize-none"
                                />
                            </div>

                            <div className="grid grid-cols-2 gap-4">
                                <div className="col-span-2">
                                    <label className="block text-xs font-semibold text-slate-600 mb-1.5">Tỉnh/Đại diện <span className="text-red-500">*</span></label>
                                    <select value={selectedProvince} onChange={(e) => setSelectedProvince(e.target.value)} className="w-full px-3 py-2 text-sm bg-white border border-slate-200 rounded-lg outline-none focus:border-blue-400 shadow-sm text-slate-800">
                                        {Object.entries(PROVINCES).map(([key, val]) => (
                                            <option key={key} value={key}>{val}</option>
                                        ))}
                                    </select>
                                </div>
                                <div>
                                    <label className="block text-xs font-semibold text-slate-600 mb-1.5">Loại địa điểm <span className="text-red-500">*</span></label>
                                    <select value={selectedType} onChange={(e) => setSelectedType(e.target.value)} className="w-full px-3 py-2 text-sm bg-white border border-slate-200 rounded-lg outline-none focus:border-blue-400 shadow-sm text-slate-800">
                                        {Object.entries(LOCATION_TYPES).map(([key, val]) => (
                                            <option key={key} value={key}>{val}</option>
                                        ))}
                                    </select>
                                </div>
                                <div>
                                    <label className="block text-xs font-semibold text-slate-600 mb-1.5">Số điện thoại (Tuỳ chọn)</label>
                                    <input type="text" placeholder="Ví dụ: 0912..." value={contactPhone} onChange={(e) => setContactPhone(e.target.value)} className="w-full px-3 py-2 text-sm bg-white border border-slate-200 rounded-lg outline-none focus:border-blue-400 shadow-sm text-slate-800" />
                                </div>
                            </div>

                            {/* Image Section with Tabs */}
                            <div>
                                <label className="block text-xs font-semibold text-slate-600 mb-2">
                                    Ảnh Đại Diện <span className="text-red-500">*</span>
                                </label>

                                {/* Tab Switcher */}
                                <div className="flex gap-2 p-1 bg-slate-100 rounded-lg mb-3">
                                    <button
                                        type="button"
                                        onClick={() => setImageMode('upload')}
                                        className={`flex-1 py-2 px-3 rounded-md text-xs font-bold transition-all ${imageMode === 'upload'
                                                ? 'bg-white text-slate-900 shadow-sm'
                                                : 'text-slate-500 hover:text-slate-700'
                                            }`}
                                    >
                                        Upload File
                                    </button>
                                    <button
                                        type="button"
                                        onClick={() => setImageMode('url')}
                                        className={`flex-1 py-2 px-3 rounded-md text-xs font-bold transition-all ${imageMode === 'url'
                                                ? 'bg-white text-slate-900 shadow-sm'
                                                : 'text-slate-500 hover:text-slate-700'
                                            }`}
                                    >
                                        Link URL
                                    </button>
                                </div>

                                {/* Upload Mode */}
                                {imageMode === 'upload' && (
                                    <div className="border border-dashed border-slate-300 rounded-xl p-4 relative cursor-pointer min-h-[120px] bg-white hover:border-blue-400 hover:bg-blue-50/50 transition-colors">
                                        <input
                                            type="file"
                                            accept="image/*"
                                            onChange={handleImageChange}
                                            className="absolute inset-0 w-full h-full opacity-0 cursor-pointer z-10"
                                            disabled={isUploading}
                                        />
                                        {isUploading ? (
                                            <div className="text-center pointer-events-none">
                                                <FaSpinner className="text-2xl text-blue-500 mx-auto mb-2 animate-spin" />
                                                <div className="text-xs font-semibold text-blue-600">Đang tải ảnh lên...</div>
                                            </div>
                                        ) : imagePreview && imageMode === 'upload' ? (
                                            <div className="absolute inset-0 p-1">
                                                <img src={imagePreview} className="w-full h-full object-cover rounded-lg" alt="Preview" />
                                            </div>
                                        ) : (
                                            <div className="text-center pointer-events-none">
                                                <FaCloudUploadAlt className="text-2xl text-slate-400 mx-auto mb-2" />
                                                <div className="text-xs font-semibold text-slate-500">Nhấp để tải ảnh lên</div>
                                                <div className="text-[10px] text-slate-400 mt-1">JPG, PNG (Max 5MB)</div>
                                            </div>
                                        )}
                                    </div>
                                )}

                                {/* URL Mode */}
                                {imageMode === 'url' && (
                                    <div className="space-y-2">
                                        <input
                                            type="url"
                                            placeholder="https://example.com/image.jpg"
                                            value={imageUrl}
                                            onChange={(e) => handleImageUrlChange(e.target.value)}
                                            className="w-full px-3 py-2 text-sm bg-white border border-slate-200 rounded-lg outline-none focus:border-blue-400 shadow-sm"
                                            disabled={isUploading}
                                        />
                                        {isUploading && (
                                            <div className="flex items-center gap-2 text-xs text-blue-600 p-2 bg-blue-50 rounded-lg">
                                                <FaSpinner className="animate-spin" />
                                                <span>Đang tải ảnh từ URL lên Cloudinary...</span>
                                            </div>
                                        )}
                                        {imagePreview && imageMode === 'url' && !isUploading && (
                                            <div className="border border-slate-200 rounded-lg p-2 bg-slate-50">
                                                <img
                                                    src={imagePreview}
                                                    className="w-full h-32 object-cover rounded"
                                                    alt="Preview"
                                                    onError={() => {
                                                        setImagePreview(null);
                                                        toast.error('URL ảnh không hợp lệ');
                                                    }}
                                                />
                                            </div>
                                        )}
                                    </div>
                                )}
                            </div>

                            {/* Advertisement Section */}
                            {!hideAdFields && (
                                <div className="space-y-3">
                                    <div className="flex items-center gap-2 p-3 bg-blue-50 border border-[#129AF2]/20 rounded-lg">
                                        <input
                                            type="checkbox"
                                            id="isAd"
                                            checked={isAdvertisement}
                                            onChange={(e) => setIsAdvertisement(e.target.checked)}
                                            className="w-4 h-4 text-[#129AF2] rounded focus:ring-[#129AF2]"
                                        />
                                        <label htmlFor="isAd" className="text-sm font-semibold text-slate-900 cursor-pointer">
                                            Đánh dấu là Địa điểm Quảng cáo
                                        </label>
                                    </div>

                                    {isAdvertisement && (
                                        <div className="space-y-3 p-4 bg-blue-50/50 border border-[#129AF2]/20 rounded-lg">
                                            <h4 className="text-xs font-bold text-slate-800 uppercase tracking-wider mb-3 border-b border-[#129AF2]/20 pb-2">
                                                Cài đặt Quảng cáo
                                            </h4>

                                            <div>
                                                <label className="block text-xs font-semibold text-slate-600 mb-1.5">
                                                    Độ ưu tiên (Priority)
                                                </label>
                                                <select
                                                    value={priority}
                                                    onChange={(e) => setPriority(parseInt(e.target.value))}
                                                    className="w-full px-3 py-2 text-sm bg-white border border-slate-200 rounded-lg outline-none focus:border-[#129AF2] shadow-sm"
                                                >
                                                    <option value={1}>1 - Thấp</option>
                                                    <option value={2}>2 - Trung bình</option>
                                                    <option value={3}>3 - Cao</option>
                                                </select>
                                                <p className="text-[10px] text-slate-500 mt-1">
                                                    Số càng lớn, địa điểm càng được hiển thị trước
                                                </p>
                                            </div>

                                            <div className="grid grid-cols-2 gap-3">
                                                <div>
                                                    <label className="block text-xs font-semibold text-slate-600 mb-1.5">
                                                        Bắt đầu hiển thị
                                                    </label>
                                                    <input
                                                        type="datetime-local"
                                                        value={featuredStartAt}
                                                        onChange={(e) => setFeaturedStartAt(e.target.value)}
                                                        className="w-full px-3 py-2 text-xs bg-white border border-slate-200 rounded-lg outline-none focus:border-[#129AF2] shadow-sm"
                                                    />
                                                </div>
                                                <div>
                                                    <label className="block text-xs font-semibold text-slate-600 mb-1.5">
                                                        Kết thúc hiển thị
                                                    </label>
                                                    <input
                                                        type="datetime-local"
                                                        value={featuredEndAt}
                                                        onChange={(e) => setFeaturedEndAt(e.target.value)}
                                                        className="w-full px-3 py-2 text-xs bg-white border border-slate-200 rounded-lg outline-none focus:border-[#129AF2] shadow-sm"
                                                    />
                                                </div>
                                            </div>
                                        </div>
                                    )}
                                </div>
                            )}
                        </div>
                    </div>
                </div>

                {/* Footer */}
                <div className="px-6 py-4 border-t border-slate-100 bg-white shrink-0 flex justify-end gap-3">
                    <AdminButton
                        variant="ghost"
                        onClick={onClose}
                    >
                        Đóng
                    </AdminButton>
                    <AdminButton
                        variant="primary"
                        onClick={handleSubmitLocation} 
                        disabled={isCreating}
                        loading={isCreating}
                        icon={FaCheck}
                    >
                        {editingLocation ? 'Cập Nhật' : 'Lưu Địa Điểm'}
                    </AdminButton>
                </div>
            </div>
        </div>
    );
};

export default LocationModal;
