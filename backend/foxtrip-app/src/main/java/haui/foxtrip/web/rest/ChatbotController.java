package haui.foxtrip.web.rest;

import haui.foxtrip.service.ChatbotService;
import haui.foxtrip.web.rest.dto.chatbot.ChatbotDTOs.ChatRequest;
import haui.foxtrip.web.rest.dto.chatbot.ChatbotDTOs.ChatResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
public class ChatbotController {

    private final ChatbotService chatbotService;

    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        return ResponseEntity.ok(chatbotService.getChatResponse(request.getMessage(), request.getHistory()));
    }
}
