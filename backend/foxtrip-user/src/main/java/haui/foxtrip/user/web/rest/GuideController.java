package haui.foxtrip.user.web.rest;

import haui.foxtrip.common.response.ApiResponse;
import haui.foxtrip.user.service.UserService;
import haui.foxtrip.user.service.dto.request.UpdateStaffProfileRequest;
import haui.foxtrip.user.service.dto.response.UserDetailResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/guide")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('GUIDE')")
public class GuideController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDetailResponse>> getMe() {
        UserDetailResponse data = userService.getMe();
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin người dùng thành công", data));
    }

    @PatchMapping("/me/profile")
    public ResponseEntity<ApiResponse<Void>> updateProfile(@Valid @RequestBody UpdateStaffProfileRequest request) {
        userService.updateStaffProfile(request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật thông tin thành công"));
    }
}
