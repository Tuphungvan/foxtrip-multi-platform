package haui.foxtrip.web.client;

import haui.foxtrip.config.GroqConfig;
import haui.foxtrip.web.rest.dto.chatbot.ChatbotDTOs.GroqRequest;
import haui.foxtrip.web.rest.dto.chatbot.ChatbotDTOs.GroqResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GroqClient {

    private final RestTemplate restTemplate;
    private final GroqConfig groqConfig;

    public String queryGroq(String prompt) {
        return queryGroq(null, prompt);
    }

    public String queryGroq(String systemPrompt, String userMessage) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(groqConfig.getApiKey());

            List<GroqRequest.Message> messages = new java.util.ArrayList<>();
            if (systemPrompt != null && !systemPrompt.isBlank()) {
                messages.add(new GroqRequest.Message("system", systemPrompt));
            }
            messages.add(new GroqRequest.Message("user", userMessage));

            GroqRequest request = GroqRequest.builder()
                    .model(groqConfig.getModel())
                    .messages(messages)
                    .temperature(0.2)  // Giảm xuống để AI bám sát dữ liệu hệ thống, ít hallucinate hơn
                    .build();

            HttpEntity<GroqRequest> entity = new HttpEntity<>(request, headers);

            GroqResponse response = restTemplate.postForObject(groqConfig.getApiUrl(), entity, GroqResponse.class);

            if (response != null && response.getChoices() != null && !response.getChoices().isEmpty()) {
                return response.getChoices().get(0).getMessage().getContent().trim();
            }
            return "Xin lỗi, tôi không thể trả lời lúc này.";
        } catch (Exception e) {
            log.error("Error querying Groq API: {}", e.getMessage());
            return "Đã có lỗi xảy ra khi kết nối với trí tuệ nhân tạo.";
        }
    }
}
