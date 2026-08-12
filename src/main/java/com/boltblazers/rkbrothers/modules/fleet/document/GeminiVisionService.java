package com.boltblazers.rkbrothers.modules.fleet.document;

import com.boltblazers.rkbrothers.modules.fleet.document.dto.AiParseResultDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * Best-effort document field extraction via Gemini Vision. Every failure
 * mode here — missing API key, network error, malformed response — falls
 * back to an empty result rather than throwing, so document upload never
 * blocks on AI availability.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiVisionService {

    private static final String API_URL_TEMPLATE =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=%s";

    private static final String PROMPT = """
            This is a vehicle document image. Extract the following \
            information and respond ONLY in this exact JSON format with no \
            other text:
            {
              "documentType": "insurance|gate_pass|puc|fitness|tax|state_permit|other",
              "documentNo": "document number or null",
              "vehicleNo": "vehicle registration number or null",
              "issuedDate": "YYYY-MM-DD or null",
              "expiryDate": "YYYY-MM-DD or null",
              "confidence": "high|medium|low"
            }
            If you cannot read a field clearly, use null. documentType must \
            be exactly one of the given values.""";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${gemini.api.key:}")
    private String apiKey;

    public AiParseResultDto parseDocument(MultipartFile file) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("GEMINI_API_KEY not set — skipping AI document parsing, admin will fill fields manually");
            return AiParseResultDto.empty();
        }

        try {
            String base64Data = Base64.getEncoder().encodeToString(file.getBytes());
            String mimeType = file.getContentType() != null ? file.getContentType() : "image/jpeg";

            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(Map.of(
                            "parts", List.of(
                                    Map.of("inline_data", Map.of("mime_type", mimeType, "data", base64Data)),
                                    Map.of("text", PROMPT)
                            )
                    ))
            );

            String url = API_URL_TEMPLATE.formatted(apiKey);
            String rawResponse = restTemplate.postForObject(url, requestBody, String.class);

            return parseGeminiResponse(rawResponse);
        } catch (Exception e) {
            log.warn("Gemini document parsing failed, falling back to manual entry: {}", e.getMessage());
            return AiParseResultDto.empty();
        }
    }

    private AiParseResultDto parseGeminiResponse(String rawResponse) {
        try {
            JsonNode root = objectMapper.readTree(rawResponse);
            String generatedText = root.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();
            String cleanedJson = stripMarkdownFences(generatedText);

            JsonNode parsed = objectMapper.readTree(cleanedJson);

            return new AiParseResultDto(
                    textOrNull(parsed, "documentType"),
                    textOrNull(parsed, "documentNo"),
                    textOrNull(parsed, "vehicleNo"),
                    dateOrNull(parsed, "issuedDate"),
                    dateOrNull(parsed, "expiryDate"),
                    textOrNull(parsed, "confidence") != null ? textOrNull(parsed, "confidence") : "low",
                    rawResponse
            );
        } catch (Exception e) {
            log.warn("Could not parse Gemini response as expected JSON: {}", e.getMessage());
            return new AiParseResultDto(null, null, null, null, null, "low", rawResponse);
        }
    }

    private String stripMarkdownFences(String text) {
        String trimmed = text.trim();
        if (trimmed.startsWith("```")) {
            trimmed = trimmed.replaceFirst("^```[a-zA-Z]*\\s*", "");
            int lastFence = trimmed.lastIndexOf("```");
            if (lastFence >= 0) {
                trimmed = trimmed.substring(0, lastFence);
            }
        }
        return trimmed.trim();
    }

    private String textOrNull(JsonNode node, String field) {
        JsonNode value = node.path(field);
        if (value.isMissingNode() || value.isNull()) {
            return null;
        }
        String text = value.asText();
        return (text.isBlank() || "null".equalsIgnoreCase(text)) ? null : text;
    }

    private LocalDate dateOrNull(JsonNode node, String field) {
        String text = textOrNull(node, field);
        if (text == null) {
            return null;
        }
        try {
            return LocalDate.parse(text);
        } catch (Exception e) {
            return null;
        }
    }
}
