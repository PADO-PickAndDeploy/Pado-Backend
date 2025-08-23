package org.pado.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CredentialRegisterRequest {
    @Schema(description = "크레덴셜 이름", example = "AWS IAM Key")
    private String name;

    @Schema(description = "크레덴셜 설명", example = "IAM 역할을 위한 인증키입니다.")
    private String description;

    @Schema(description = "크레덴셜 타입", example = "AWS")
    private String type;

    @Schema(description = "크레덴셜 실제 데이터(ID / Token)", example = "AKIAIOSFODNN7EXAMPLE/secret")
    private String data;
}
