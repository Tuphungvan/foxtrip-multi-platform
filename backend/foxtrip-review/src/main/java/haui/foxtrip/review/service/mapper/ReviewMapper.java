package haui.foxtrip.review.service.mapper;

import haui.foxtrip.review.domain.Review;
import haui.foxtrip.review.service.dto.ReviewResDTO;
import haui.foxtrip.review.service.dto.TourReviewDTO;
import haui.foxtrip.user.domain.User;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ReviewMapper {

    @Mapping(target = "reviewId", source = "review.id")
    @Mapping(target = "rating", source = "review.rating")
    @Mapping(target = "content", source = "review.content")
    @Mapping(target = "createdAt", source = "review.createdAt")
    @Mapping(target = "editedAt", source = "review.editedAt")
    @Mapping(target = "user", source = "user")
    TourReviewDTO toTourReviewDTO(Review review, User user);

    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "avatarUrl", source = "user.avatarUrl")
    TourReviewDTO.UserInfoDTO toUserInfoDTO(User user);

    @Mapping(target = "reviewId", source = "id")
    ReviewResDTO toReviewResDTO(Review review);
}
