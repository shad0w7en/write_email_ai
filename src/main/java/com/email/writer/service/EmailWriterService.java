package com.email.writer.service;

import com.email.writer.model.EmailModel;
import com.fasterxml.jackson.databind.JsonNode;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
public class EmailWriterService {


    @Value("${gemini.base.url}")
    private String GEMINI_URI;

    @Value("${gemini.key}")
    private String API_KEY;

   // private final WebClient.Builder webclintBuilder;

    @Autowired
    private WebClient.Builder webclintBuilder;

//    public EmailWriterService(WebClient.Builder webclintBuilder) {
//        this.webclintBuilder = webclintBuilder;
//    }

    private JsonNode getAiResponse(String textToSend) {
        WebClient webClient = webclintBuilder.baseUrl(GEMINI_URI+API_KEY).build();

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of(
                                "parts", List.of(
                                        Map.of("text", textToSend)
                                )
                        )
                )
        );

        return webClient.post()
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .onStatus(
                        HttpStatusCode::is4xxClientError,
                        clientResponse -> clientResponse.bodyToMono(String.class).map(RuntimeException::new)
                )
                .onStatus(
                        HttpStatusCode::is5xxServerError,
                        clientResponse -> clientResponse.bodyToMono(String.class).map(RuntimeException::new)
                )
                .bodyToMono(JsonNode.class)
                .block();
    }

    private String getContext(EmailModel emailModel) {
        return """
           Please write a response in %s tone for this original email:
           %s
           """.formatted(emailModel.getTone(), emailModel.getEmailContent());
    }

    public String getEmailResponse(EmailModel emailModel) {
        String context = getContext(emailModel);
        JsonNode getResponse = getAiResponse(context);



        JsonNode candidates = getResponse.path("candidates");
        if (candidates.isMissingNode() || candidates.isEmpty()) {
            throw new RuntimeException("No candidates found in AI response");
        }

        JsonNode content = candidates.get(0).path("content");
        JsonNode parts = content.path("parts");
        if (parts.isMissingNode() || parts.isEmpty()) {
            throw new RuntimeException("No parts found in AI response content");
        }

        JsonNode textNode = parts.get(0).path("text");
        if (textNode.isMissingNode()) {
            throw new RuntimeException("Text is missing in AI response parts");
        }

        return textNode.asText();
    }
}
