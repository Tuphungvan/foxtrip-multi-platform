package haui.foxtrip.user.service;

import haui.foxtrip.common.dto.PageData;
import haui.foxtrip.user.service.dto.request.*;
import haui.foxtrip.user.service.dto.response.*;
import java.util.UUID;
import org.springframework.data.domain.Pageable;

public interface UserService {

    UserDetailResponse getMe();

    void updateUserProfile(UpdateUserProfileRequest request);

    void updateStaffProfile(UpdateStaffProfileRequest request);

    PageData<UserDetailResponse> searchUsers(String email, Pageable pageable);

    void lockUser(UUID userId);

    void unlockUser(UUID userId);

    String createGuide(CreateGuideRequest request);

    String createAdmin(CreateAdminRequest request);
}
