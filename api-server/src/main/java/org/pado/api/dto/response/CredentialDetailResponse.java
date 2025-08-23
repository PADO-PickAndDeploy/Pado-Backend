package org.pado.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CredentialDetailResponse {
    @Schema(description = "크레덴셜 ID", example = "1")
    private Long id;

    @Schema(description = "크레덴셜 이름", example = "AWS IAM Key")
    private String name;

    @Schema(description = "크레덴셜 타입", example = "AWS")
    private String type;

    @Schema(description = "크레덴셜 설명", example = "IAM 역할을 위한 인증키입니다.")
    private String description;

    @Schema(description = "실제 크리덴셜 데이터", example = "{\"accessKey\":\"AKIA...\", \"secretKey\":\"...\"}")
    private String data;

    @Schema(description = "처리 메시지", example = "등록 완료")
    private String message;

    @Schema(description = "생성 일시", example = "2025-01-15T15:45:00")
    private String createdAt;

    @Schema(description = "수정 일시", example = "2025-01-15T15:45:00")
    private String updatedAt;
}
