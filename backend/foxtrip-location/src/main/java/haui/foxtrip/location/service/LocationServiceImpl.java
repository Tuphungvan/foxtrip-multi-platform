package haui.foxtrip.location.service;

import haui.foxtrip.common.error.BusinessException;
import haui.foxtrip.location.domain.Location;
import haui.foxtrip.location.service.dto.response.LocationResponse;

import java.util.UUID;
import haui.foxtrip.location.repository.LocationRepository;
import haui.foxtrip.location.service.mapper.LocationMapper;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@SuppressWarnings("unused")
@RequiredArgsConstructor
public class LocationServiceImpl implements LocationService {

    private final LocationMapper locationMapper;
    private final LocationRepository locationRepository;

    @Override
    @Transactional(readOnly = true)
    public LocationResponse getLocationDetail(UUID locationId) {
        Location location = locationRepository.findByIdAndDeletedAtIsNull(locationId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND.value(), "Không tìm thấy location"));
        return locationMapper.toDto(location);
    }
}
