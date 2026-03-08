package com.sakollife.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class QuizSubmitResponse {
    private UUID attemptId;
    private int attemptNumber; // 0 for guest
    private double[] studentVector; // [R, I, A, S, E, C]
    private List<MajorResultResponse> results; // ranked, filtered to >= 50%
}

