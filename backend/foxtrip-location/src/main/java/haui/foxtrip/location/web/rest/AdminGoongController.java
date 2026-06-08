package haui.foxtrip.location.web.rest;

import haui.foxtrip.common.response.ApiResponse;
import haui.foxtrip.location.service.goong.GoongApiService;
import haui.foxtrip.location.service.goong.GoongDtos;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/goong")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ADMIN', 'SUPER_ADMIN')")
public class AdminGoongController {

    private final GoongApiService goongApiService;

    @GetMapping("/autocomplete")
    public ApiResponse<GoongDtos.AutoCompleteResponse> autoComplete(@RequestParam("input") String input) {
        return ApiResponse.success("Success", goongApiService.autoComplete(input));
    }

    @GetMapping("/detail")
    public ApiResponse<GoongDtos.PlaceDetailResponse> getDetail(@RequestParam("place_id") String placeId) {
        return ApiResponse.success("Success", goongApiService.getPlaceDetail(placeId));
    }
}
