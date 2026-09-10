package com.juan.tfgplatform.service;

import java.math.BigDecimal;

public interface AiGradingService {
    AiGradingResult gradeImage(String imagePath, String enunciado, BigDecimal puntuacionMaxima);
    AiGradingResult gradeText(String respuestaTexto, String enunciado, BigDecimal puntuacionMaxima);
}
