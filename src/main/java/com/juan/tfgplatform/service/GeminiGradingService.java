package com.juan.tfgplatform.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Implementación de fallback del servicio de corrección por IA.
 * Se activa cuando no se ha configurado ai.openai.key en application.properties.
 * Devuelve puntuación 0 e indica al alumno que la corrección será manual.
 */
@Service
@ConditionalOnMissingBean(OpenAiGradingService.class)
public class GeminiGradingService implements AiGradingService {

    private static final AiGradingResult NO_DISPONIBLE = new AiGradingResult(
            BigDecimal.ZERO,
            "Corrección automática no disponible. El profesor revisará esta respuesta manualmente."
    );

    @Override
    public AiGradingResult gradeText(String respuestaTexto, String enunciado, BigDecimal puntuacionMaxima) {
        return NO_DISPONIBLE;
    }

    @Override
    public AiGradingResult gradeImage(String imagePath, String enunciado, BigDecimal puntuacionMaxima) {
        return NO_DISPONIBLE;
    }
}
