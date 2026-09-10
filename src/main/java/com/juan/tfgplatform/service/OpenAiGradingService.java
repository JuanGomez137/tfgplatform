package com.juan.tfgplatform.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Base64;

/**
 * Implementación del servicio de corrección con IA usando cualquier API compatible
 * con OpenAI (GitHub Models, OpenAI, Azure OpenAI...).
 *
 * Se activa cuando se configura ai.openai.key en application.properties.
 *
 * GitHub Models (recomendado con GitHub Copilot):
 *   ai.openai.endpoint = https://models.inference.ai.azure.com
 *   ai.openai.key      = tu_github_personal_access_token
 *   ai.openai.model    = gpt-4o-mini
 *
 * OpenAI directo:
 *   ai.openai.endpoint = https://api.openai.com/v1
 *   ai.openai.key      = sk-...
 *   ai.openai.model    = gpt-4o-mini
 */
@Service
@ConditionalOnProperty(name = "ai.openai.key")
public class OpenAiGradingService implements AiGradingService {

    @Value("${ai.openai.endpoint:https://models.inference.ai.azure.com}")
    private String endpoint;

    @Value("${ai.openai.key}")
    private String apiKey;

    @Value("${ai.openai.model:gpt-4o-mini}")
    private String model;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    private final ObjectMapper mapper = new ObjectMapper();

    // -----------------------------------------------------------------------
    // CORRECCIÓN DE TEXTO
    // -----------------------------------------------------------------------

    @Override
    public AiGradingResult gradeText(String respuestaTexto, String enunciado, BigDecimal puntuacionMaxima) {
        try {
            String systemPrompt = buildSystemPrompt(puntuacionMaxima);
            String userPrompt = "Enunciado:\n" + enunciado
                    + "\n\nRespuesta del alumno:\n" + respuestaTexto;

            String bodyJson = buildTextBody(systemPrompt, userPrompt);
            String responseText = callApi(bodyJson);
            return parseResponse(responseText, puntuacionMaxima);

        } catch (Exception e) {
            System.err.println("[OpenAiGradingService] Error al corregir texto: " + e.getMessage());
            return fallback("Error al conectar con la IA: " + e.getMessage());
        }
    }

    // -----------------------------------------------------------------------
    // CORRECCIÓN DE IMAGEN (vision)
    // -----------------------------------------------------------------------

    @Override
    public AiGradingResult gradeImage(String imagePath, String enunciado, BigDecimal puntuacionMaxima) {
        try {
            // Leer imagen y convertir a base64
            byte[] imageBytes = Files.readAllBytes(Paths.get(imagePath));
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);
            String mimeType = detectMimeType(imagePath);
            String dataUrl = "data:" + mimeType + ";base64," + base64Image;

            String systemPrompt = buildSystemPrompt(puntuacionMaxima);
            String bodyJson = buildVisionBody(systemPrompt, enunciado, dataUrl);
            String responseText = callApi(bodyJson);
            return parseResponse(responseText, puntuacionMaxima);

        } catch (Exception e) {
            System.err.println("[OpenAiGradingService] Error al corregir imagen: " + e.getMessage());
            return fallback("Error al procesar la imagen con la IA: " + e.getMessage());
        }
    }

    // -----------------------------------------------------------------------
    // CONSTRUCCIÓN DEL PROMPT
    // -----------------------------------------------------------------------

    private String buildSystemPrompt(BigDecimal maxScore) {
        return "Eres un corrector automático de prácticas universitarias de ingeniería.\n\n"
                + "PROCESO DE CORRECCIÓN (sigue estos pasos en orden):\n"
                + "1. Lee el enunciado y la rúbrica de corrección (si la hay).\n"
                + "2. Examina la respuesta del alumno e identifica cada error.\n"
                + "3. Si la rúbrica especifica penalizaciones concretas (p.ej. '-2 puntos por elemento faltante', "
                + "'-0.5 por dato incorrecto'), lista CADA penalización en el array 'penalties'. "
                + "No calcules el score tú mismo; el sistema lo calculará restando las penalizaciones de "
                + maxScore.toPlainString() + ".\n"
                + "4. Si NO hay rúbrica, evalúa con criterio técnico razonado y lista las penalizaciones igualmente.\n"
                + "5. Cada penalización debe ser un número positivo (el sistema lo restará).\n\n"
                + "FORMATO: responde ÚNICAMENTE con JSON válido, sin bloques de código ni texto adicional:\n"
                + "{\"penalties\": [{\"descripcion\": \"<error>\", \"puntos\": <número positivo>}, ...], "
                + "\"feedback\": \"<2-3 frases en español indicando qué errores se detectaron y la penalización de cada uno>\"}";
    }

    // -----------------------------------------------------------------------
    // CONSTRUCCIÓN DEL CUERPO JSON DE LA PETICIÓN
    // -----------------------------------------------------------------------

    private String buildTextBody(String systemPrompt, String userPrompt) throws Exception {
        String escapedSystem = escapeJson(systemPrompt);
        String escapedUser = escapeJson(userPrompt);

        return "{"
                + "\"model\": \"" + model + "\","
                + "\"temperature\": 0.1,"
                + "\"max_tokens\": 400,"
                + "\"messages\": ["
                +   "{\"role\": \"system\", \"content\": \"" + escapedSystem + "\"},"
                +   "{\"role\": \"user\",   \"content\": \"" + escapedUser + "\"}"
                + "]"
                + "}";
    }

    private String buildVisionBody(String systemPrompt, String enunciado, String dataUrl) throws Exception {
        String escapedSystem = escapeJson(systemPrompt);
        String escapedEnunciado = escapeJson("Enunciado:\n" + enunciado);
        String escapedDataUrl = escapeJson(dataUrl);

        return "{"
                + "\"model\": \"" + model + "\","
                + "\"temperature\": 0.1,"
                + "\"max_tokens\": 400,"
                + "\"messages\": ["
                +   "{\"role\": \"system\", \"content\": \"" + escapedSystem + "\"},"
                +   "{\"role\": \"user\", \"content\": ["
                +     "{\"type\": \"text\", \"text\": \"" + escapedEnunciado + "\"},"
                +     "{\"type\": \"image_url\", \"image_url\": {\"url\": \"" + escapedDataUrl + "\"}}"
                +   "]}"
                + "]"
                + "}";
    }

    // -----------------------------------------------------------------------
    // LLAMADA HTTP
    // -----------------------------------------------------------------------

    private String callApi(String bodyJson) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint + "/chat/completions"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .timeout(Duration.ofSeconds(60))
                .POST(HttpRequest.BodyPublishers.ofString(bodyJson))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("API devolvió HTTP " + response.statusCode() + ": " + response.body());
        }

        // Extraer el content del primer choice
        JsonNode root = mapper.readTree(response.body());
        return root.path("choices").get(0).path("message").path("content").asText();
    }

    // -----------------------------------------------------------------------
    // PARSEO DE LA RESPUESTA
    // -----------------------------------------------------------------------

    private AiGradingResult parseResponse(String content, BigDecimal maxScore) {
        try {
            // El modelo a veces envuelve el JSON en ```json ... ```
            String cleaned = content.trim();
            if (cleaned.startsWith("```")) {
                cleaned = cleaned.replaceAll("(?s)^```[a-z]*\\n?", "").replaceAll("```$", "").trim();
            }

            JsonNode node = mapper.readTree(cleaned);
            String feedback = node.get("feedback").asText();

            BigDecimal score;
            if (node.has("penalties")) {
                // Calculamos el score en Java restando penalizaciones — evita errores aritméticos de la IA
                BigDecimal totalPenalty = BigDecimal.ZERO;
                for (JsonNode p : node.get("penalties")) {
                    BigDecimal puntos = new BigDecimal(p.get("puntos").asText());
                    totalPenalty = totalPenalty.add(puntos.abs());
                }
                score = maxScore.subtract(totalPenalty);
            } else {
                // Fallback: el modelo devolvió formato antiguo con "score"
                score = new BigDecimal(node.get("score").asText());
            }

            score = score.min(maxScore).max(BigDecimal.ZERO)
                    .setScale(2, java.math.RoundingMode.HALF_UP);
            return new AiGradingResult(score, feedback);

        } catch (Exception e) {
            System.err.println("[OpenAiGradingService] Error al parsear respuesta IA: " + content);
            return fallback("La IA devolvió un formato inesperado. El profesor revisará esta respuesta.");
        }
    }

    // -----------------------------------------------------------------------
    // UTILIDADES
    // -----------------------------------------------------------------------

    private String detectMimeType(String path) {
        String lower = path.toLowerCase();
        if (lower.endsWith(".png"))  return "image/png";
        if (lower.endsWith(".gif"))  return "image/gif";
        if (lower.endsWith(".webp")) return "image/webp";
        return "image/jpeg";
    }

    /** Escapa caracteres especiales JSON en cadenas que se insertan manualmente. */
    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private AiGradingResult fallback(String reason) {
        return new AiGradingResult(
                BigDecimal.ZERO,
                "⚠ Corrección automática no disponible (" + reason
                        + "). El profesor revisará esta respuesta manualmente."
        );
    }
}
