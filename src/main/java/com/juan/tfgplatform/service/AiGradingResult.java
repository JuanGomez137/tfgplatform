package com.juan.tfgplatform.service;

import java.math.BigDecimal;

public class AiGradingResult {
    private final BigDecimal score;
    private final String feedback;

    public AiGradingResult(BigDecimal score, String feedback) {
        this.score = score;
        this.feedback = feedback;
    }

    public BigDecimal getScore() { return score; }
    public String getFeedback() { return feedback; }
}
