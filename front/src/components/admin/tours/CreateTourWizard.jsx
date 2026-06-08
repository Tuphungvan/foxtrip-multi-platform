import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import {
    createTourBase, updateTourBase, updateTourItineraries, updateTourAddons,
    setTourGuide, searchLocations, getGuideSuggestions, activateTour,
    uploadImageToCloudinary, uploadImageUrlToCloudinary, getTourDetail
} from '../../../services/api/adminApi';
import LocationModal from '../LocationModal';
import {
    FaTimesCircle, FaSearch, FaSpinner, FaPlus, FaCheck,
    FaArrowRight, FaTrashAlt, FaTrash, FaUserCircle, FaPhoneAlt, FaCheckCircle,
    FaMapPin, FaCalendarDay, FaProjectDiagram, FaImages, FaCloudUploadAlt
} from 'react-icons/fa';
import { toBackendISO } from '../../../utils/dateUtils';
import { PROVINCES, TOUR_CATEGORIES } from '../../../utils/constants';
import toast from 'react-hot-toast';


const formatCurrency = (value) => {
    if (!value) return '';
    const num = value.toString().replace(/\D/g, '');
    return num.replace(/\B(?=(\d{3})+(?!\d))/g, '.');
};

const LocationPicker = ({ value, onChange }) => {
    const [keyword, setKeyword] = useState('');
    const [results, setResults] = useState([]);
    const [isSearching, setIsSearching] = useState(false);
    const [showDropdown, setShowDropdown] = useState(false);
    const [isAddModalOpen, setIsAddModalOpen] = useState(false);

    React.useEffect(() => {
        if (!keyword.trim()) { setResults([]); return; }
        const timer = setTimeout(async () => {
            setIsSearching(true);
            try {
                const res = await searchLocations(keyword);
                // API trả về PageData { items, totalPages, ... }
                setResults(Array.isArray(res?.data?.items) ? res.data.items : []);
                setShowDropdown(true);
            } catch (err) {
                console.error('Search error:', err);
            } finally {
                setIsSearching(false);
            }
        }, 500);
        return () => clearTimeout(timer);
    }, [keyword]);

    return (
        <div className="relative flex items-center gap-2 w-full">
            {value.locationName ? (
                <div className="flex-1 flex items-center justify-between px-3 py-1.5 bg-blue-50 border border-blue-100 rounded-md text-[13px] text-blue-900 shadow-sm">
                    <span className="font-semibold flex items-center truncate max-w-[200px]">
                        <FaMapPin className="mr-1.5 text-blue-500" /> {value.locationName}
                    </span>
                    <button type="button" onClick={() => onChange({ locationId: '', locationName: '' })} className="text-blue-400 hover:text-blue-600 transition-colors ml-2">
                        <FaTimesCircle />
                    </button>
                </div>
            ) : (
                <div className="flex-1 relative flex items-center">
                    <input
                        type="text" placeholder="Tìm địa điểm đã lưu..." value={keyword}
                        onChange={(e) => setKeyword(e.target.value)}
                        onFocus={() => { if (results.length > 0) setShowDropdown(true); }}
                        className="w-full pl-8 pr-3 py-1.5 text-[13px] bg-white text-slate-800 border border-slate-200 rounded-md outline-none focus:border-blue-400 focus:ring-1 focus:ring-blue-400 transition-all font-medium placeholder:text-slate-400 shadow-sm"
                    />
                    <FaSearch className="absolute left-2.5 text-slate-400 text-xs" />
                    {isSearching && <FaSpinner className="absolute right-2.5 text-slate-300 animate-spin text-xs" />}

                    {showDropdown && results.length > 0 && (
                        <div className="absolute top-full left-0 z-30 w-full mt-1 bg-white border border-slate-100 shadow-xl rounded-md max-h-48 overflow-y-auto custom-scrollbar">
                            {results.map(loc => {
                                const isDeleted = !!loc.deletedAt;
                                return (
                                    <div
                                        key={loc.id}
                                        onClick={() => {
                                            if (!isDeleted) {
                                                onChange({ locationId: loc.id, locationName: loc.name });
                                                setShowDropdown(false);
                                                setKeyword('');
                                            }
                                        }}
                                        className={`p-2 border-b border-slate-50 last:border-0 flex items-center gap-2 ${isDeleted
                                                ? 'bg-red-50/50 cursor-not-allowed opacity-60'
                                                : 'hover:bg-slate-50 cursor-pointer'
                                            }`}
                                    >
                                        <div className="w-6 h-6 rounded bg-slate-100 overflow-hidden shrink-0">
                                            <img src={loc.imageUrl} className="w-full h-full object-cover" />
                                        </div>
                                        <div className="min-w-0 flex-1">
                                            <div className="flex items-center gap-1.5">
                                                <div className="text-[11px] font-bold text-slate-700 truncate">{loc.name}</div>
                                                {isDeleted && (
                                                    <span className="px-1 py-0.5 bg-red-100 text-red-600 border border-red-200 rounded text-[8px] font-bold shrink-0">
                                                        ĐÃ XÓA
                                                    </span>
                                                )}
                                            </div>
                                            <div className="text-[9px] text-slate-400 truncate">{PROVINCES[loc.province] || loc.province}</div>
                                        </div>
                                    </div>
                                );
                            })}
                        </div>
                    )}
                </div>
            )}

            {!value.locationName && (
                <button
                    onClick={() => setIsAddModalOpen(true)} type="button"
                    className="p-1.5 bg-white text-slate-500 hover:bg-slate-50 hover:text-slate-800 border border-slate-200 rounded-md text-xs transition-colors shrink-0 shadow-sm"
                >
                    <FaPlus />
                </button>
            )}

            <LocationModal
                isOpen={isAddModalOpen}
                hideAdFields={true}
                onClose={() => setIsAddModalOpen(false)}
                onSuccess={(newLoc) => onChange({ locationId: newLoc.id, locationName: newLoc.name })}
            />
        </div>
    );
};

const CreateTourWizard = () => {
    const navigate = useNavigate();
    const { tourId } = useParams();
    const [step, setStep] = useState(1);
    const [createdTourId, setCreatedTourId] = useState(null);
    const [tourStatus, setTourStatus] = useState(null);
    const [loading, setLoading] = useState(false);
    const [baseInfo, setBaseInfo] = useState({ name: '', description: '', province: 'HA_NOI', category: 'NATURE', slots: 20, startDate: '', endDate: '', price: 0, discount: 0, thumbnailUrl: '', shortId: '' });

    // Image handling
    const [imageMode, setImageMode] = useState('upload');
    const [imageFile, setImageFile] = useState(null);
    const [imageUrl, setImageUrl] = useState('');
    const [imagePreview, setImagePreview] = useState(null);
    const [uploadingImage, setUploadingImage] = useState(false);
    const [days, setDays] = useState([{ dayLocalId: Date.now(), activities: [{ actLocalId: Date.now() + 1, locationId: '', locationName: '', activity: '' }] }]);
    const [addons, setAddons] = useState([]);
    const [guides, setGuides] = useState([]);
    const [selectedGuideId, setSelectedGuideId] = useState('');

    // Mapping setupStep to Wizard Steps
    const mapSetupStepToStep = (setupStep) => {
        if (!setupStep) return 1;
        switch (setupStep) {
            case 'BASIC_DONE': return 2;
            case 'ITINERARY_DONE': return 3;
            case 'ADDON_DONE': return 4;
            case 'READY': return 1; // Finished setup, start at step 1 for editing
            default: return 1;
        }
    };

    // Load tour data if editing/resuming
    useEffect(() => {
        const loadTour = async () => {
            if (!tourId) return;

            setLoading(true);
            try {
                const res = await getTourDetail(tourId);
                const tour = res?.data || res;
                if (tour) {
                    setCreatedTourId(tour.id);
                    setTourStatus(tour.status);
                    setBaseInfo({
                        name: tour.name || '',
                        description: tour.description || '',
                        province: tour.province || 'HA_NOI',
                        category: tour.category || 'NATURE',
                        slots: tour.slots || 20,
                        startDate: tour.startDate ? tour.startDate.substring(0, 16) : '',
                        endDate: tour.endDate ? tour.endDate.substring(0, 16) : '',
                        price: tour.price || 0,
                        discount: tour.discount || 0,
                        thumbnailUrl: tour.thumbnailUrl || '',
                        shortId: tour.shortId || ''
                    });

                    if (tour.thumbnailUrl) {
                        setImageUrl(tour.thumbnailUrl);
                        setImagePreview(tour.thumbnailUrl);
                        setImageMode('url');
                    }

                    // Map itineraries to days structure
                    if (tour.itineraries && tour.itineraries.length > 0) {
                        const dayMap = {};
                        tour.itineraries.forEach(item => {
                            if (!dayMap[item.dayNumber]) dayMap[item.dayNumber] = [];
                            dayMap[item.dayNumber].push({
                                actLocalId: Math.random(),
                                locationId: item.location?.id || item.locationId || '',
                                locationName: item.location?.name || item.locationName || '',
                                activity: item.activity || ''
                            });
                        });
                        const mappedDays = Object.keys(dayMap).sort((a, b) => a - b).map(dayNum => ({
                            dayLocalId: Math.random(),
                            activities: dayMap[dayNum]
                        }));
                        setDays(mappedDays);
                    }

                    // Map addons
                    if (tour.addons && tour.addons.length > 0) {
                        setAddons(tour.addons.map(ad => ({ ...ad, localId: Math.random() })));
                    }

                    // Determine current step
                    const targetStep = mapSetupStepToStep(tour.setupStep);
                    setStep(targetStep);

                    // If moving to personnel step, fetch guides
                    if (targetStep === 4) {
                        fetchGuides(tour.id);
                        if (tour.guideId) setSelectedGuideId(tour.guideId);
                    }
                }
            } catch (err) {
                toast.error("Không thể tải thông tin tour: " + err.message);
            } finally {
                setLoading(false);
            }
        };
        loadTour();
    }, [tourId]);

    const extractErrorMsg = (e, defaultMsg) => {
        let msg = e.message || defaultMsg;
        // Xử lý list validation method errors của Spring Boot
        if (e.errors && Array.isArray(e.errors) && e.errors.length > 0) {
            const mapped = e.errors.map(err => typeof err === 'object' ? (err.defaultMessage || err.message || err.msg || JSON.stringify(err)) : err);
            msg = mapped.join("; ");
        } else if (e.data && typeof e.data === 'object' && !Array.isArray(e.data)) {
            // Xử lý dạng field : errorMessage trong object data
            const values = Object.values(e.data);
            if (values.length > 0 && typeof values[0] === 'string') {
                msg = values.join("; ");
            }
        }
        return msg;
    };

    const handleImageChange = async (e) => {
        const file = e.target.files[0];
        if (file) {
            setImageFile(file);
            setImagePreview(URL.createObjectURL(file));
            setImageUrl('');
            setBaseInfo({ ...baseInfo, thumbnailUrl: '' }); // Clear old URL

            // Upload ngay lập tức
            setUploadingImage(true);
            try {
                const uploadedUrl = await uploadImageToCloudinary(file, 'foxtrip/tours');
                setImageUrl(uploadedUrl);
                setImagePreview(uploadedUrl);
                setBaseInfo({ ...baseInfo, thumbnailUrl: uploadedUrl });
                setImageFile(null); // Clear file sau khi upload thành công
                toast.success('Tải ảnh lên thành công!');
            } catch (err) {
                toast.error("Lỗi tải ảnh: " + err.message);
                // Giữ lại preview local nếu upload thất bại
            } finally {
                setUploadingImage(false);
            }
        }
    };

    const handleImageUrlChange = async (url) => {
        setImageUrl(url);
        setImagePreview(url);
        setImageFile(null);
        setBaseInfo({ ...baseInfo, thumbnailUrl: url });

        if (url.trim()) {
            // Upload URL ảnh lên Cloudinary
            setUploadingImage(true);
            try {
                const uploadedUrl = await uploadImageUrlToCloudinary(url, 'foxtrip/tours');
                setImageUrl(uploadedUrl);
                setImagePreview(uploadedUrl);
                setBaseInfo({ ...baseInfo, thumbnailUrl: uploadedUrl });
                toast.success('Tải ảnh từ URL thành công!');
            } catch (err) {
                toast.error("Lỗi tải ảnh từ URL: " + err.message);
                // Giữ lại URL gốc nếu upload thất bại
            } finally {
                setUploadingImage(false);
            }
        }
    };

    const handleSubmitBase = async (e) => {
        e.preventDefault();

        // Validation ảnh - bây giờ imageUrl đã có sẵn từ upload trước đó
        let finalImageUrl = baseInfo.thumbnailUrl || imageUrl || '';

        if (!finalImageUrl) {
            toast.error('Vui lòng chọn ảnh bìa cho tour!');
            return;
        }

        // Validation shortId (YouTube Shorts URL)
        if (!baseInfo.shortId || baseInfo.shortId.trim() === '') {
            toast.error('Vui lòng nhập link YouTube Shorts!');
            return;
        }

        if (!baseInfo.startDate || !baseInfo.endDate) {
            toast.error('Vui lòng chọn thời gian bắt đầu và kết thúc!');
            return;
        }

        // Kiểm tra ngày kết thúc phải sau ngày bắt đầu
        if (new Date(baseInfo.endDate) <= new Date(baseInfo.startDate)) {
            toast.error('Ngày kết thúc phải sau ngày bắt đầu!');
            return;
        }

        setLoading(true);
        try {
            let payload = {
                ...baseInfo,
                thumbnailUrl: finalImageUrl,
                startDate: toBackendISO(baseInfo.startDate),
                endDate: toBackendISO(baseInfo.endDate)
            };

            // Backend validation: Không được truyền các trường cốt lõi nếu tour ACTIVE
            if (tourStatus === 'ACTIVE') {
                delete payload.name;
                delete payload.province;
                delete payload.category;
                delete payload.slots;
                delete payload.startDate;
                delete payload.endDate;
                delete payload.price;
                delete payload.description;
            }

            const isEdit = !!(createdTourId || tourId);
            const res = isEdit
                ? await updateTourBase(createdTourId || tourId, payload)
                : await createTourBase(payload);

            const tourData = res?.data || res;
            setCreatedTourId(tourData?.id);
            toast.success(isEdit ? 'Cập nhật thông tin tour thành công!' : 'Khởi tạo tour thành công!');
            setStep(2);
        } catch (e) {
            toast.error(extractErrorMsg(e, "Không thể lưu thông tin tour cơ bản."));
        } finally {
            setLoading(false);
        }
    };

    const handleAddDay = () => setDays([...days, { dayLocalId: Date.now(), activities: [] }]);
    const handleAddActivity = (dIdx) => { const n = [...days]; n[dIdx].activities.push({ actLocalId: Date.now(), locationId: '', locationName: '', activity: '' }); setDays(n); };
    const handleUpdateActivity = (dIdx, aIdx, field, val) => { const n = [...days]; n[dIdx].activities[aIdx][field] = val; setDays(n); };
    const handleRemoveActivity = (dIdx, aIdx) => { const n = [...days]; n[dIdx].activities.splice(aIdx, 1); setDays(n); };
    const handleRemoveDay = (dIdx) => { const n = [...days]; n.splice(dIdx, 1); setDays(n); };

    const handleAddAddon = () => setAddons([...addons, { localId: Date.now(), name: '', price: 0, description: '', isActive: true }]);
    const handleUpdateAddon = (index, field, value) => { const newAddons = [...addons]; newAddons[index][field] = value; setAddons(newAddons); };
    const handleRemoveAddon = (index) => { const newAddons = [...addons]; newAddons.splice(index, 1); setAddons(newAddons); };

    const handleSubmitItinerary = async () => {
        // Validation
        if (days.length === 0) {
            toast.error('Vui lòng thêm ít nhất một ngày trong lịch trình!');
            return;
        }

        // Kiểm tra mỗi ngày phải có ít nhất 1 hoạt động
        const emptyDays = days.filter(day => day.activities.length === 0);
        if (emptyDays.length > 0) {
            toast.error('Mỗi ngày phải có ít nhất một hoạt động!');
            return;
        }

        // Kiểm tra tất cả hoạt động phải có mô tả
        const hasEmptyActivity = days.some(day =>
            day.activities.some(act => !act.activity || act.activity.trim() === '')
        );
        if (hasEmptyActivity) {
            toast.error('Vui lòng điền mô tả cho tất cả các hoạt động!');
            return;
        }

        setLoading(true);
        try {
            // Cấu trúc lại mảng Itinerary tránh xung đột ngầm từ DB
            const flatItineraries = [];
            days.forEach((day, dIdx) => {
                day.activities.forEach((act, aIdx) => {
                    flatItineraries.push({
                        dayNumber: dIdx + 1,
                        position: aIdx + 1,
                        locationId: act.locationId || null,
                        activity: act.activity
                    });
                });
            });
            await updateTourItineraries(createdTourId, flatItineraries);
            toast.success('Lưu lịch trình thành công!');
            setStep(3);
        } catch (e) {
            toast.error(extractErrorMsg(e, "Không thể lưu lịch trình."));
        } finally {
            setLoading(false);
        }
    };

    const fetchGuides = async (tourId) => {
        try {
            const res = await getGuideSuggestions(tourId);
            setGuides(res?.data || res || []);
        } catch (e) {
            console.error("Lỗi khi tải danh sách hướng dẫn viên", e);
        }
    };

    const handleSubmitAddons = async () => {
        // Validation: Kiểm tra tất cả addon phải có tên và mô tả
        if (addons.length > 0) {
            const hasEmptyName = addons.some(ad => !ad.name || ad.name.trim() === '');
            if (hasEmptyName) {
                toast.error("Vui lòng điền tên cho tất cả các dịch vụ!");
                return;
            }

            const hasEmptyDesc = addons.some(ad => !ad.description || ad.description.trim() === '');
            if (hasEmptyDesc) {
                toast.error("Quy định: Mô tả của dịch vụ không được để rỗng!");
                return;
            }

            const hasInvalidPrice = addons.some(ad => !ad.price || ad.price < 0);
            if (hasInvalidPrice) {
                toast.error("Giá dịch vụ phải lớn hơn hoặc bằng 0!");
                return;
            }
        }

        setLoading(true);
        try {
            // Đảm bảo tất cả addon có field isActive
            const formattedAddons = addons.map(ad => ({
                ...ad,
                isActive: ad.isActive !== undefined ? ad.isActive : true,
                price: Number(ad.price) || 0
            }));

            await updateTourAddons(createdTourId, formattedAddons);
            toast.success('Lưu dịch vụ đính kèm thành công!');
            await fetchGuides(createdTourId);
            setStep(4);
        } catch (e) {
            toast.error(extractErrorMsg(e, "Không thể lưu dịch vụ đính kèm."));
        } finally {
            setLoading(false);
        }
    };

    const handleSubmitGuide = async () => {
        setLoading(true);
        try {
            await setTourGuide(createdTourId, selectedGuideId);
            toast.success('Gán hướng dẫn viên và kích hoạt tour thành công!');
            setStep(5);
        } catch (e) {
            // Race Condition Bẫy
            if (e.message && e.message.includes("409")) {
                toast.error("Guide đã được phân công cho Tour khác trùng lịch. Đang Refresh...");
                await fetchGuides(createdTourId);
                setSelectedGuideId('');
            } else {
                toast.error(extractErrorMsg(e, "Không thể phân công hướng dẫn viên."));
            }
        } finally {
            setLoading(false);
        }
    };

    const stepsArray = ["Thông tin", "Lịch trình", "Dịch vụ", "Nhân sự", "Hoàn tất"];
    const isRestricted = ['ACTIVE', 'ONGOING', 'COMPLETED'].includes(tourStatus);
    const isReadOnly = ['ONGOING', 'COMPLETED'].includes(tourStatus);

    return (
        <div className="max-w-4xl mx-auto space-y-5 pb-10">
            <div>
                <h2 className="text-xl font-bold text-slate-900 tracking-tight">
                    {tourId ? (isReadOnly ? 'Xem Chi Tiết Tour' : 'Chỉnh Sửa Tour') : 'Tạo Tour Mới'}
                </h2>
                <p className="text-[13px] text-slate-500 mt-1">
                    {isReadOnly ? 'Tour đang diễn ra hoặc đã kết thúc, bạn chỉ có thể xem thông tin.' : 'Điền các thông tin cơ bản và lập lịch trình cho tour.'}
                </p>
            </div>

            {loading && tourId && step === 1 && (
                <div className="bg-white/50 backdrop-blur-sm fixed inset-0 z-[200] flex flex-col items-center justify-center">
                    <FaSpinner className="text-4xl text-blue-500 animate-spin mb-4" />
                    <p className="text-slate-600 font-medium animate-pulse">Đang đồng bộ hóa dữ liệu...</p>
                </div>
            )}

            {/* Elegant Railway Stepper */}
            <div className="bg-white rounded-xl border border-slate-200 py-6 px-10 shadow-sm relative overflow-hidden">
                <div className="relative z-10 w-full max-w-2xl mx-auto flex justify-between">
                    {/* Background Track */}
                    <div className="absolute left-[5%] top-1/2 -translate-y-1/2 w-[90%] h-[3px] bg-slate-100 rounded-full -z-10"></div>
                    {/* Filled Track */}
                    <div
                        className="absolute left-[5%] top-1/2 -translate-y-1/2 h-[3px] bg-blue-500 rounded-full -z-10 transition-all duration-500 ease-in-out"
                        style={{ width: `${((step - 1) / (stepsArray.length - 1)) * 90}%` }}
                    ></div>

                    {stepsArray.map((st, idx) => {
                        const sNum = idx + 1;
                        const isActive = step === sNum;
                        const isCompleted = step > sNum;
                        return (
                            <div key={idx} className="flex flex-col items-center bg-white px-2">
                                <div className={`w-7 h-7 rounded-full flex items-center justify-center font-bold text-[10px] transition-all duration-300 relative
                                    ${isActive ? 'bg-blue-600 text-white shadow-[0_0_0_4px_rgba(59,130,246,0.15)] scale-110' :
                                        isCompleted ? 'bg-blue-100 text-blue-600 border border-blue-200' : 'bg-slate-50 text-slate-400 border border-slate-200'}`}>
                                    {isCompleted ? <FaCheck className="text-[10px]" /> : sNum}
                                </div>
                                <div className={`text-[10px] font-semibold mt-2.5 whitespace-nowrap hidden sm:block delay-100 transition-colors
                                    ${isActive ? 'text-blue-600' : isCompleted ? 'text-slate-600' : 'text-slate-400'}`}>
                                    {st.toUpperCase()}
                                </div>
                            </div>
                        );
                    })}
                </div>
            </div>

            <div className="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
                {step === 1 && (
                    <form onSubmit={handleSubmitBase} className="p-6 space-y-4">
                        <h3 className="font-bold text-slate-800 text-base mb-4 border-b border-slate-100 pb-3">Thông tin cơ bản</h3>
                        <div className="space-y-4">
                            {/* Tên Tour */}
                            <div>
                                <label className="block text-xs font-semibold text-slate-600 mb-1.5">Tên Tour <span className="text-red-500">*</span></label>
                                <input
                                    required
                                    type="text"
                                    disabled={isRestricted}
                                    value={baseInfo.name}
                                    onChange={e => setBaseInfo({ ...baseInfo, name: e.target.value })}
                                    className={`w-full px-3 py-2 text-sm text-slate-800 border rounded-lg outline-none transition-all shadow-sm ${isRestricted ? 'bg-slate-50 border-slate-200 cursor-not-allowed' : 'bg-white border-slate-200 focus:border-[#B48279] focus:ring-2 focus:ring-[#B48279]/20 font-medium'}`}
                                    placeholder="VD: Khám phá Vịnh Hạ Long 3N2Đ"
                                />
                            </div>

                            {/* Tỉnh Thành */}
                            <div>
                                <label className="block text-xs font-semibold text-slate-600 mb-1.5">Tỉnh Thành / Khu Vực <span className="text-red-500">*</span></label>
                                <select
                                    disabled={isRestricted}
                                    value={baseInfo.province}
                                    onChange={e => setBaseInfo({ ...baseInfo, province: e.target.value })}
                                    className={`w-full px-3 py-2 text-sm text-slate-700 border rounded-lg outline-none shadow-sm ${isRestricted ? 'bg-slate-50 border-slate-200 cursor-not-allowed' : 'bg-white border-slate-200 focus:border-[#B48279] focus:ring-2 focus:ring-[#B48279]/20'}`}
                                >
                                    {Object.entries(PROVINCES).map(([key, value]) => (
                                        <option key={key} value={key}>{value}</option>
                                    ))}
                                </select>
                            </div>

                            {/* Link YouTube Shorts */}
                            <div>
                                <label className="block text-xs font-semibold text-slate-600 mb-1.5">
                                    Link Video YouTube Shorts <span className="text-red-500">*</span>
                                </label>
                                <input
                                    required
                                    type="url"
                                    disabled={isReadOnly}
                                    value={baseInfo.shortId}
                                    onChange={e => setBaseInfo({ ...baseInfo, shortId: e.target.value })}
                                    className={`w-full px-3 py-2 text-sm text-slate-700 border rounded-lg outline-none transition-all shadow-sm ${isReadOnly ? 'bg-slate-50 border-slate-200 cursor-not-allowed' : 'bg-white border-slate-200 focus:border-[#B48279] focus:ring-2 focus:ring-[#B48279]/20 font-medium'}`}
                                    placeholder="https://youtube.com/shorts/abc123"
                                />
                                <p className="text-[10px] text-slate-500 mt-1">💡 Dán link YouTube Shorts để quảng bá tour</p>
                            </div>

                            <div className="md:col-span-12">
                                <label className="block text-xs font-semibold text-slate-600 mb-2">
                                    Ảnh Bìa (Thumbnail) <span className="text-red-500">*</span>
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
                                        Tải lên tệp
                                    </button>
                                    <button
                                        type="button"
                                        onClick={() => setImageMode('url')}
                                        className={`flex-1 py-2 px-3 rounded-md text-xs font-bold transition-all ${imageMode === 'url'
                                                ? 'bg-white text-slate-900 shadow-sm'
                                                : 'text-slate-500 hover:text-slate-700'
                                            }`}
                                    >
                                        Đường dẫn URL
                                    </button>
                                </div>

                                {/* Upload Mode */}
                                {imageMode === 'upload' && (
                                    <div className="border border-dashed border-slate-300 rounded-xl p-4 relative cursor-pointer min-h-[180px] bg-white hover:border-blue-400 hover:bg-blue-50/50 transition-colors">
                                        <input
                                            type="file"
                                            accept="image/*"
                                            onChange={handleImageChange}
                                            className="absolute inset-0 w-full h-full opacity-0 cursor-pointer z-10"
                                        />
                                        {imagePreview && imageMode === 'upload' ? (
                                            <div className="relative h-40">
                                                <img src={imagePreview} className="w-full h-full object-cover rounded-lg" alt="Preview" />
                                                <button
                                                    type="button"
                                                    onClick={(e) => {
                                                        e.preventDefault();
                                                        e.stopPropagation();
                                                        setImageFile(null);
                                                        setImagePreview(null);
                                                        setBaseInfo({ ...baseInfo, thumbnailUrl: '' });
                                                    }}
                                                    className="absolute top-2 right-2 bg-red-500 hover:bg-red-600 text-white rounded-full p-2 shadow-lg transition-transform hover:scale-110 z-20"
                                                >
                                                    <FaTrashAlt className="text-xs" />
                                                </button>
                                            </div>
                                        ) : (
                                            <div className="text-center pointer-events-none py-8">
                                                <FaCloudUploadAlt className="text-3xl text-slate-400 mx-auto mb-3" />
                                                <div className="text-sm font-semibold text-slate-700 mb-1">Nhấp để tải ảnh lên</div>
                                                <div className="text-xs text-slate-400">JPG, PNG (Max 5MB)</div>
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
                                        />
                                        {imagePreview && imageMode === 'url' && (
                                            <div className="border border-slate-200 rounded-lg p-2 bg-slate-50 relative">
                                                <img
                                                    src={imagePreview}
                                                    className="w-full h-40 object-cover rounded"
                                                    alt="Preview"
                                                    onError={() => {
                                                        setImagePreview(null);
                                                        toast.error('URL ảnh không hợp lệ');
                                                    }}
                                                />
                                                <button
                                                    type="button"
                                                    onClick={(e) => {
                                                        e.preventDefault();
                                                        setImageUrl('');
                                                        setImagePreview(null);
                                                        setBaseInfo({ ...baseInfo, thumbnailUrl: '' });
                                                    }}
                                                    className="absolute top-4 right-4 bg-red-500 hover:bg-red-600 text-white rounded-full p-2 shadow-lg transition-transform hover:scale-110"
                                                >
                                                    <FaTrashAlt className="text-xs" />
                                                </button>
                                            </div>
                                        )}
                                    </div>
                                )}

                                {uploadingImage && (
                                    <div className="mt-2 flex items-center gap-2 text-xs text-blue-600">
                                        <FaSpinner className="animate-spin" />
                                        <span>Đang tải ảnh lên Cloudinary...</span>
                                    </div>
                                )}
                            </div>

                            <div className="md:col-span-12">
                                <label className="block text-[11px] font-bold text-slate-500 uppercase tracking-widest mb-1.5">Mô Tả Tour</label>
                                <textarea required disabled={isRestricted} rows="3" value={baseInfo.description} onChange={e => setBaseInfo({ ...baseInfo, description: e.target.value })} className={`w-full px-3 py-2 text-[13px] text-slate-900 border rounded-md outline-none transition-shadow shadow-sm ${isRestricted ? 'bg-slate-50 border-slate-200 cursor-not-allowed' : 'bg-white border-slate-300 focus:border-blue-500 focus:ring-1 focus:ring-blue-500'}`} placeholder="Nhập mô tả tổng quan về chuyến đi..."></textarea>
                            </div>

                            <div className="md:col-span-4 relative">
                                <label className="block text-[11px] font-bold text-slate-500 uppercase tracking-widest mb-1.5">Danh mục</label>
                                <div className="relative">
                                    <select disabled={isRestricted} value={baseInfo.category} onChange={e => setBaseInfo({ ...baseInfo, category: e.target.value })} className={`appearance-none w-full px-3 py-2 pr-8 text-[13px] text-slate-700 border rounded-md outline-none shadow-sm font-medium ${isRestricted ? 'bg-slate-50 border-slate-200 cursor-not-allowed' : 'bg-white border-slate-300 focus:border-blue-500 focus:ring-1 focus:ring-blue-500'}`}>
                                        {Object.entries(TOUR_CATEGORIES).map(([key, label]) => (
                                            <option key={key} value={key}>{label}</option>
                                        ))}
                                    </select>
                                    <div className="pointer-events-none absolute inset-y-0 right-0 flex items-center px-3 text-slate-500">
                                        <svg className="w-3 h-3 fill-current" viewBox="0 0 20 20"><path d="M5.293 7.293a1 1 0 011.414 0L10 10.586l3.293-3.293a1 1 0 111.414 1.414l-4 4a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414z" /></svg>
                                    </div>
                                </div>
                            </div>
                            <div className="md:col-span-4">
                                <label className="block text-[11px] font-bold text-slate-500 uppercase tracking-widest mb-1.5">Số Khách Tối Đa</label>
                                <input required disabled={isRestricted} type="number" min="1" value={baseInfo.slots} onChange={e => setBaseInfo({ ...baseInfo, slots: e.target.value })} className={`w-full px-3 py-2 text-[13px] text-slate-900 border rounded-md outline-none shadow-sm ${isRestricted ? 'bg-slate-50 border-slate-200 cursor-not-allowed' : 'bg-white border-slate-300 focus:border-blue-500 focus:ring-1 focus:ring-blue-500'}`} />
                            </div>
                            <div className="md:col-span-4 grid grid-cols-2 gap-3">
                                <div>
                                    <label className="block text-[11px] font-bold text-slate-500 uppercase tracking-widest mb-1.5">Giá (VND)</label>
                                    <input required disabled={isRestricted} type="text" value={formatCurrency(baseInfo.price)} onChange={e => setBaseInfo({ ...baseInfo, price: e.target.value.replace(/\D/g, '') })} className={`w-full px-3 py-2 text-[13px] border rounded-md outline-none shadow-sm font-semibold ${isRestricted ? 'bg-slate-50 border-slate-200 text-slate-500 cursor-not-allowed' : 'bg-white text-blue-700 border-slate-300 focus:border-blue-500 focus:ring-1 focus:ring-blue-500'}`} />
                                </div>
                                <div>
                                    <label className="block text-[11px] font-bold text-slate-500 uppercase tracking-widest mb-1.5">Giảm giá (%)</label>
                                    <input required disabled={isReadOnly} type="number" min="0" max="100" value={baseInfo.discount} onChange={e => setBaseInfo({ ...baseInfo, discount: e.target.value ? Number(e.target.value) : 0 })} className={`w-full px-3 py-2 text-[13px] text-slate-900 border rounded-md outline-none shadow-sm font-medium ${isReadOnly ? 'bg-slate-50 border-slate-200 cursor-not-allowed' : 'bg-white border-slate-300 focus:border-blue-500 focus:ring-1 focus:ring-blue-500'}`} />
                                </div>
                            </div>

                            <div className="md:col-span-6">
                                <label className="block text-[11px] font-bold text-slate-500 uppercase tracking-widest mb-1.5">Thời gian bắt đầu</label>
                                <input required disabled={isRestricted} type="datetime-local" value={baseInfo.startDate} onChange={e => setBaseInfo({ ...baseInfo, startDate: e.target.value })} className={`w-full px-3 py-2 text-[13px] text-slate-700 border rounded-md outline-none shadow-sm ${isRestricted ? 'bg-slate-50 border-slate-200 cursor-not-allowed' : 'bg-white border-slate-300 focus:border-blue-500 focus:ring-1 focus:ring-blue-500'}`} />
                            </div>
                            <div className="md:col-span-6">
                                <label className="block text-[11px] font-bold text-slate-500 uppercase tracking-widest mb-1.5">Thời gian kết thúc</label>
                                <input required disabled={isRestricted} type="datetime-local" value={baseInfo.endDate} onChange={e => setBaseInfo({ ...baseInfo, endDate: e.target.value })} className={`w-full px-3 py-2 text-[13px] text-slate-700 border rounded-md outline-none shadow-sm ${isRestricted ? 'bg-slate-50 border-slate-200 cursor-not-allowed' : 'bg-white border-slate-300 focus:border-blue-500 focus:ring-1 focus:ring-blue-500'}`} />
                            </div>
                        </div>

                        <div className="flex justify-end pt-4 mt-2">
                            {isReadOnly ? (
                                <button type="button" onClick={() => setStep(2)} className="px-5 py-2 bg-slate-600 hover:bg-slate-700 text-white rounded-md font-semibold text-[13px] transition-colors shadow-sm flex items-center gap-2">
                                    Xem Lịch Trình <FaArrowRight className="text-[10px]" />
                                </button>
                            ) : (
                                <button type="submit" disabled={loading} className="px-5 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-md font-semibold text-[13px] transition-colors shadow-sm flex items-center gap-2">
                                    {loading ? <FaSpinner className="animate-spin" /> : "Lưu Thông Tin"} <FaArrowRight className="text-[10px]" />
                                </button>
                            )}
                        </div>
                    </form>
                )}

                {step === 2 && (
                    <div className="p-6">
                        <div className="mb-6 flex flex-col sm:flex-row sm:justify-between sm:items-end gap-3 border-b border-slate-100 pb-2">
                            <h3 className="font-bold text-slate-800 text-[15px]">Lập Lịch Trình</h3>
                            {!isRestricted && (
                                <button onClick={handleAddDay} className="px-3 py-1.5 bg-white border border-slate-200 text-slate-700 hover:bg-slate-50 hover:border-slate-300 rounded-md text-[11px] uppercase tracking-wider font-bold transition-all shadow-sm flex items-center gap-2">
                                    <FaCalendarDay className="text-slate-400" /> Thêm Ngày Mới
                                </button>
                            )}
                        </div>

                        <div className="pl-2">
                            {days.map((day, dIdx) => (
                                <div key={day.dayLocalId} className="relative border-l-2 border-slate-200 ml-3 pl-6 pb-8 last:pb-4 group/day">
                                    <div className="absolute -left-[9px] top-0.5 w-4 h-4 rounded-full bg-slate-800 ring-4 ring-white flex items-center justify-center"></div>

                                    <div className="flex items-center justify-between mb-4 -mt-0.5">
                                        <h4 className="text-[13px] font-bold text-slate-800 tracking-wide">NGÀY {dIdx + 1}</h4>
                                        <div className="opacity-0 group-hover/day:opacity-100 transition-opacity flex gap-2">
                                            {!isRestricted && <button onClick={() => handleRemoveDay(dIdx)} className="text-[9px] uppercase font-bold text-red-500 hover:text-red-700 hover:bg-red-50 px-2 py-1 rounded transition-colors">Xóa</button>}
                                            {!isRestricted && (
                                                <button onClick={() => handleAddActivity(dIdx)} className="text-[9px] uppercase font-bold px-2 py-1 bg-slate-100 text-slate-700 hover:bg-slate-200 rounded transition-colors hidden sm:inline-block">
                                                    <FaPlus className="inline mr-1" /> Thêm Hoạt Động
                                                </button>
                                            )}
                                        </div>
                                    </div>

                                    <div className="space-y-3">
                                        {day.activities.length === 0 && <div className="text-[11px] font-medium text-slate-400 italic">Chưa có hoạt động...</div>}
                                        {day.activities.map((act, aIdx) => (
                                            <div key={act.actLocalId} className="relative bg-white border border-slate-200 rounded-lg p-3 shadow-sm hover:border-blue-300 transition-all flex flex-col md:flex-row items-stretch md:items-center gap-3">
                                                <div className="w-5 h-5 rounded bg-slate-100 text-slate-500 flex items-center justify-center font-bold text-[9px] shrink-0 border border-slate-200">{aIdx + 1}</div>
                                                <div className="flex-1 grid grid-cols-1 md:grid-cols-12 gap-3 min-w-0">
                                                    <div className="md:col-span-6 flex items-center">
                                                        <input
                                                            required disabled={isRestricted} type="text" placeholder="Mô tả hoạt động..."
                                                            value={act.activity} onChange={e => handleUpdateActivity(dIdx, aIdx, 'activity', e.target.value)}
                                                            className={`w-full text-[13px] font-semibold border-none outline-none focus:ring-0 p-0 placeholder:font-normal placeholder:text-slate-400 bg-transparent ${isRestricted ? 'text-slate-500 cursor-not-allowed' : 'text-slate-800'}`}
                                                        />
                                                    </div>
                                                    <div className="md:col-span-6 md:border-l border-slate-100 md:pl-4">
                                                        <LocationPicker
                                                            disabled={isRestricted}
                                                            value={{ locationId: act.locationId, locationName: act.locationName }}
                                                            onChange={(val) => {
                                                                if (!isRestricted) {
                                                                    handleUpdateActivity(dIdx, aIdx, 'locationId', val.locationId);
                                                                    handleUpdateActivity(dIdx, aIdx, 'locationName', val.locationName);
                                                                }
                                                            }}
                                                        />
                                                    </div>
                                                </div>
                                                {!isRestricted && (
                                                    <button onClick={() => handleRemoveActivity(dIdx, aIdx)} className="text-slate-400 hover:text-red-500 p-1.5 rounded hover:bg-red-50 transition-colors shrink-0 outline-none flex self-end md:self-auto">
                                                        <FaTrashAlt className="text-[11px]" />
                                                    </button>
                                                )}
                                            </div>
                                        ))}
                                        {!isRestricted && (
                                            <button onClick={() => handleAddActivity(dIdx)} className="w-full py-2 mt-2 border border-dashed border-slate-300 rounded-lg text-slate-500 text-[11px] font-semibold hover:bg-slate-50 hover:text-slate-700 transition-colors sm:hidden">
                                                + Thêm Hoạt Động
                                            </button>
                                        )}
                                    </div>
                                </div>
                            ))}
                        </div>

                        <div className="flex justify-end pt-4 mt-2">
                            {isRestricted ? (
                                <button onClick={() => setStep(3)} className="px-5 py-2 bg-slate-600 hover:bg-slate-700 text-white rounded-md font-semibold text-[13px] transition-colors shadow-sm flex items-center gap-2">
                                    Xem Dịch Vụ <FaArrowRight className="text-[10px]" />
                                </button>
                            ) : (
                                <button onClick={handleSubmitItinerary} disabled={loading} className="px-5 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-md font-semibold text-[13px] transition-colors shadow-sm flex items-center gap-2">
                                    {loading ? <FaSpinner className="animate-spin" /> : "Lưu Lịch Trình"} <FaArrowRight className="text-[10px]" />
                                </button>
                            )}
                        </div>
                    </div>
                )}

                {step === 3 && (
                    <div className="p-6 space-y-5">
                        <h3 className="font-bold text-slate-800 text-[15px] border-b border-slate-100 pb-2">Dịch Vụ Đính Kèm</h3>
                        <div className="space-y-3">
                            {addons.map((ad, idx) => (
                                <div key={ad.localId} className="flex flex-col md:flex-row gap-3 p-3 border border-slate-200 rounded-lg items-center relative pr-10 bg-white hover:border-blue-200 shadow-sm transition-colors">
                                    <div className="w-full md:w-1/3"><input required disabled={isRestricted} type="text" placeholder="Tên dịch vụ" value={ad.name} onChange={e => handleUpdateAddon(idx, 'name', e.target.value)} className={`w-full text-slate-900 border border-slate-200 rounded-md px-3 py-1.5 outline-none text-[13px] font-semibold shadow-sm ${isRestricted ? 'bg-slate-50 cursor-not-allowed' : 'bg-white focus:border-blue-400'}`} /></div>
                                    <div className="flex-1 w-full"><input required disabled={isRestricted} type="text" placeholder="Chi tiết dịch vụ..." value={ad.description} onChange={e => handleUpdateAddon(idx, 'description', e.target.value)} className={`w-full text-slate-700 border border-slate-200 rounded-md px-3 py-1.5 outline-none text-[13px] shadow-sm ${isRestricted ? 'bg-slate-50 cursor-not-allowed' : 'bg-white focus:border-blue-400'}`} /></div>
                                    <div className="w-full md:w-40 relative">
                                        <span className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400 text-[11px] font-bold">₫</span>
                                        <input required disabled={isRestricted} type="text" placeholder="Giá tiền" value={formatCurrency(ad.price)} onChange={e => handleUpdateAddon(idx, 'price', e.target.value.replace(/\D/g, ''))} className={`w-full border border-slate-200 rounded-md pl-6 pr-3 py-1.5 outline-none font-semibold text-[13px] text-right shadow-sm ${isRestricted ? 'bg-slate-50 text-slate-400 cursor-not-allowed' : 'bg-white text-slate-900 focus:border-blue-400'}`} /></div>
                                    {!isRestricted && <button onClick={() => handleRemoveAddon(idx)} className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-red-500 p-1.5"><FaTrash className="text-xs" /></button>}
                                </div>
                            ))}
                            {addons.length === 0 && <div className="text-[12px] font-medium text-slate-400 p-6 border border-dashed border-slate-300 rounded-lg text-center bg-slate-50/50">Danh sách trống. Thêm dịch vụ/yêu cầu đính kèm.</div>}
                        </div>
                        {!isRestricted && <button onClick={handleAddAddon} className="px-4 py-2 border border-slate-200 text-slate-600 rounded-md text-[11px] uppercase tracking-wider font-bold hover:bg-slate-50 transition-all shadow-sm"><FaPlus className="inline mr-1.5 text-[10px]" /> Thêm Dịch Vụ</button>}

                        <div className="flex justify-between items-center pt-4 mt-2">
                             <button onClick={() => setStep(4)} className="text-[12px] text-slate-500 hover:text-slate-800 font-semibold transition-colors py-1.5 underline underline-offset-2">Bỏ qua</button>
                             {isRestricted ? (
                                 <button onClick={() => setStep(4)} className="px-5 py-2 bg-slate-600 hover:bg-slate-700 text-white rounded-md font-semibold text-[13px] transition-colors shadow-sm flex items-center gap-2">Chọn Nhân Sự <FaArrowRight className="text-[10px]" /></button>
                             ) : (
                                 <button onClick={handleSubmitAddons} disabled={loading} className="px-5 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-md font-semibold text-[13px] transition-colors shadow-sm flex items-center gap-2">Xác Nhận <FaArrowRight className="text-[10px]" /></button>
                             )}
                        </div>
                    </div>
                )}

                {step === 4 && (
                    <div className="p-8 space-y-6 text-center">
                        <FaUserCircle className="text-4xl text-slate-300 mx-auto mb-2" />
                        <div>
                            <h3 className="text-[17px] font-bold text-slate-900 mb-1">Phân Công Hướng Dẫn Viên</h3>
                            <p className="text-[12px] text-slate-500 max-w-sm mx-auto">Chọn một hướng dẫn viên phù hợp để dẫn dắt và theo dõi tour này.</p>
                        </div>
                        <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 max-w-xl mx-auto text-left mt-2">
                            {guides.map(g => (
                                <div key={g.id} 
                                     onClick={() => { if (!isReadOnly) setSelectedGuideId(g.id); }} 
                                     className={`p-3 rounded-lg border transition-all flex gap-3 items-center ${isReadOnly ? 'cursor-not-allowed opacity-80' : 'cursor-pointer'} ${selectedGuideId === g.id ? 'border-blue-500 bg-blue-50 ring-1 ring-blue-500 shadow-sm' : 'border-slate-200 bg-white hover:border-slate-300 shadow-[0_1px_2px_rgba(0,0,0,0.01)]'}`}>
                                    <div className="w-9 h-9 rounded-full bg-blue-50 flex items-center justify-center border border-blue-100 shrink-0">
                                        <FaUserCircle className="text-blue-400 text-xl" />
                                    </div>
                                    <div className="min-w-0 flex-1">
                                        <div className="font-bold text-[13px] text-slate-900 truncate">{g.username}</div>
                                        <div className="text-[10px] text-slate-500 mt-0.5 truncate">{g.email}</div>
                                        <div className="text-[10px] text-slate-400 mt-0.5 flex items-center gap-1.5">
                                            <FaPhoneAlt className="text-[9px]" /> {g.phoneNumber}
                                        </div>
                                    </div>
                                    {selectedGuideId === g.id && <FaCheckCircle className="text-blue-500 shrink-0 text-base" />}
                                </div>
                            ))}
                        </div>
                        <div className="flex justify-between items-center pt-6 mt-2 max-w-xl mx-auto">
                            <button onClick={() => setStep(5)} className="text-[12px] text-slate-500 hover:text-slate-800 font-semibold py-1.5 underline underline-offset-2 transition-colors">Phân công sau</button>
                            <button onClick={handleSubmitGuide} disabled={loading || !selectedGuideId} className="px-5 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-md font-semibold text-[13px] transition-colors shadow-sm flex items-center gap-2 disabled:opacity-50 disabled:cursor-not-allowed">Kích Hoạt Tour <FaArrowRight className="text-[10px]" /></button>
                        </div>
                    </div>
                )}

                {step === 5 && (
                    <div className="text-center p-12 py-16 space-y-4">
                        <div className="w-16 h-16 bg-blue-50 border border-blue-100 rounded-2xl flex items-center justify-center text-2xl text-blue-600 mx-auto mb-3 shadow-[0_4px_15px_rgba(59,130,246,0.15)] transform rotate-3">
                            <FaProjectDiagram />
                        </div>
                        <h2 className="text-[20px] font-bold text-slate-900 tracking-tight">Khởi Tạo Thành Công</h2>
                        <p className="text-[13px] text-slate-500 max-w-xs mx-auto leading-relaxed">Tour đã được tạo thành công và kích hoạt trên hệ thống.</p>
                        <div className="pt-5">
                            <button onClick={() => navigate('/admin/manage-tours')} className="px-6 py-2.5 bg-slate-900 hover:bg-slate-800 text-white rounded-lg font-semibold text-[13px] shadow-sm transition-colors mx-auto flex items-center gap-2">
                                Vào Bảng Điều Hành <FaArrowRight className="text-[10px] opacity-70" />
                            </button>
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
};

export default CreateTourWizard;
