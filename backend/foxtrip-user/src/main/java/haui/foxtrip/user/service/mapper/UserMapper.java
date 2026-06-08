package haui.foxtrip.user.service.mapper;

import haui.foxtrip.user.domain.User;
import haui.foxtrip.user.service.dto.request.CreateAdminRequest;
import haui.foxtrip.user.service.dto.request.CreateGuideRequest;
import haui.foxtrip.user.service.dto.request.UpdateStaffProfileRequest;
import haui.foxtrip.user.service.dto.request.UpdateUserProfileRequest;
import haui.foxtrip.user.service.dto.response.UserDetailResponse;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    UserDetailResponse toUserDetailResDTO(User user);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateUserFromDTO(UpdateUserProfileRequest dto, @MappingTarget User user);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateStaffFromDTO(UpdateStaffProfileRequest dto, @MappingTarget User user);

    User toUserFromCreateGuideDTO(CreateGuideRequest dto);

    User toUserFromCreateAdminDTO(CreateAdminRequest dto);
}
