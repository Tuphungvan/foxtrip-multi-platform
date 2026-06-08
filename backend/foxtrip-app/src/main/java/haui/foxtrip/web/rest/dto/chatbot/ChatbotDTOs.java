package haui.foxtrip.web.rest.dto.chatbot;

import haui.foxtrip.location.service.dto.response.LocationResponse;
import haui.foxtrip.tour.service.dto.response.TourCardResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
public class ChatbotDTOs {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatRequest {
        private String message;
        private List<Message> history;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Message {
            private String role; // "user" or "assistant"
            private String content;
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatResponse {
        private String message;
        private List<TourCardResponse> tours;
        private List<LocationResponse> locations;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GroqRequest {
        private String model;
        private List<Message> messages;
        private double temperature;

        @Data
        @AllArgsConstructor
        @NoArgsConstructor
        public static class Message {
            private String role;
            private String content;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GroqResponse {
        private List<Choice> choices;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Choice {
            private Message message;
        }

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Message {
            private String content;
        }
    }
}
