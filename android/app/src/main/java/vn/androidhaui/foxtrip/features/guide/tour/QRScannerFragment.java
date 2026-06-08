package vn.androidhaui.foxtrip.features.guide.tour;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import vn.androidhaui.foxtrip.databinding.FragmentQrScannerBinding;
import vn.androidhaui.foxtrip.utils.QRUtils;

public class QRScannerFragment extends Fragment {
    private static final int CAMERA_PERMISSION_CODE = 100;
    private FragmentQrScannerBinding binding;
    private GuideTourViewModel viewModel;
    private String tourId;

    public static QRScannerFragment newInstance(String tourId) {
        QRScannerFragment fragment = new QRScannerFragment();
        Bundle args = new Bundle();
        args.putString("tourId", tourId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            tourId = getArguments().getString("tourId");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentQrScannerBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(GuideTourViewModel.class);

        // Check camera permission
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        } else {
            startScanning();
        }

        binding.btnClose.setOnClickListener(v -> getParentFragmentManager().popBackStack());
        binding.btnManual.setOnClickListener(v -> showManualEntryDialog());
        binding.btnGallery.setOnClickListener(v -> openGallery());

        viewModel.getCheckInSuccess().observe(getViewLifecycleOwner(), success -> {
            if (Boolean.TRUE.equals(success)) {
                Toast.makeText(getContext(), "Check-in thành công!", Toast.LENGTH_SHORT).show();
                viewModel.resetCheckInStatus();
                getParentFragmentManager().popBackStack();
            }
        });

        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
                viewModel.clearError();
                binding.barcodeScanner.resume();
            }
        });
    }

    private void openGallery() {
        android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_PICK);
        intent.setType("image/*");
        galleryLauncher.launch(intent);
    }

    private final androidx.activity.result.ActivityResultLauncher<android.content.Intent> galleryLauncher = 
        registerForActivityResult(new androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null) {
                android.net.Uri imageUri = result.getData().getData();
                try {
                    android.graphics.Bitmap bitmap = android.provider.MediaStore.Images.Media.getBitmap(requireContext().getContentResolver(), imageUri);
                    String payload = QRUtils.decodeQRCode(bitmap);
                    if (payload != null && payload.contains("|")) {
                        String orderCode = payload.split("\\|")[0];
                        viewModel.checkIn(tourId, orderCode);
                    } else {
                        Toast.makeText(requireContext(), "Không tìm thấy mã QR hợp lệ trong ảnh", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(requireContext(), "Lỗi khi đọc ảnh", Toast.LENGTH_SHORT).show();
                }
            }
        });

    private void startScanning() {
        binding.barcodeScanner.decodeContinuous(new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult result) {
                if (result != null && result.getText() != null) {
                    String payload = result.getText();
                    if (payload.contains("|")) {
                        binding.barcodeScanner.pause();
                        String orderCode = payload.split("\\|")[0];
                        viewModel.checkIn(tourId, orderCode);
                    } else {
                        Toast.makeText(requireContext(), "Mã QR không hợp lệ", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startScanning();
            } else {
                Toast.makeText(requireContext(), "Cần cấp quyền camera để quét QR", Toast.LENGTH_SHORT).show();
                getParentFragmentManager().popBackStack();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        binding.barcodeScanner.resume();
    }

    @Override
    public void onPause() {
        super.onPause();
        binding.barcodeScanner.pause();
    }

    private void showManualEntryDialog() {
        android.widget.EditText input = new android.widget.EditText(requireContext());
        input.setHint("Nhập mã đơn hàng (VD: FT-...)");
        
        // Thêm padding cho EditText trông đẹp hơn
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        input.setPadding(padding, padding, padding, padding);

        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Nhập mã thủ công")
                .setMessage("Trong trường hợp không thể quét QR, vui lòng nhập mã đơn hàng của khách:")
                .setView(input)
                .setPositiveButton("Xác nhận", (dialog, which) -> {
                    String code = input.getText().toString().trim();
                    if (!code.isEmpty()) {
                        binding.barcodeScanner.pause();
                        viewModel.checkIn(tourId, code);
                    } else {
                        Toast.makeText(requireContext(), "Vui lòng nhập mã đơn hàng", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
