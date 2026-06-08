package haui.foxtrip.location.service.goong;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoongApiService {

    @Value("${foxtrip.goong.api-key}")
    private String apiKey;

    @Value("${foxtrip.goong.api-url}")
    private String apiUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public GoongDtos.AutoCompleteResponse autoComplete(String input) {
        try {
            // Mã hóa theo chuẩn application/x-www-form-urlencoded (dấu cách sẽ thành dấu +)
            String encodedInput = URLEncoder.encode(input, StandardCharsets.UTF_8);
            
            // Xây dựng URL thủ công
            String urlString = String.format("%s/Place/AutoComplete?api_key=%s&input=%s", 
                    apiUrl, apiKey, encodedInput);
            
            // URI.create sẽ giữ nguyên chuỗi đã encode, không mã hóa lại
            URI uri = URI.create(urlString);
            
            
            return restTemplate.getForObject(uri, GoongDtos.AutoCompleteResponse.class);
        } catch (Exception e) {
            log.error("Error calling Goong AutoComplete API", e);
            return null;
        }
    }


    public GoongDtos.PlaceDetailResponse getPlaceDetail(String placeId) {
        try {
            String urlString = String.format("%s/Place/Detail?api_key=%s&place_id=%s", 
                    apiUrl, apiKey, placeId);
            URI uri = URI.create(urlString);
            
            GoongDtos.PlaceDetailResponse response = restTemplate.getForObject(uri, GoongDtos.PlaceDetailResponse.class);
            if (response != null && response.getResult() != null && response.getResult().getPhotos() != null && !response.getResult().getPhotos().isEmpty()) {
                String photoRef = response.getResult().getPhotos().get(0).getPhoto_reference();
                String photoUrl = apiUrl + "/Place/Photo?maxwidth=800&photoreference=" + photoRef + "&api_key=" + apiKey;
                response.getResult().setFirst_photo_url(photoUrl);
            }
            return response;
        } catch (Exception e) {
            log.error("Error calling Goong Place Detail API", e);
            return null;
        }
    }
}
