package com.sakollife.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class MajorResultResponse {
    private UUID majorId;
    private String code;
    private String nameEn;
    private String nameKh;
    private String descriptionEn;
    private String descriptionKh;
    private int rank;
    private double similarityScore;    // 0.0 to 1.0
    private int similarityPercentage;  // 0 to 100 (for display)
}
