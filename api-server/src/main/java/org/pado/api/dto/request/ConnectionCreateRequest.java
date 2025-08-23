package org.pado.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class ConnectionCreateRequest {
    @NotBlank(message = "대상 컴포넌트 ID는 필수입니다.")
    @Schema(description = "대상 컴포넌트 ID", example = "2")
    private Long targetComponentId;

    @NotBlank(message = "연결 유형은 필수입니다.")
    @Schema(description = "연결 유형", example = "TCP")
    private String connectionType; // e.g., "TCP"(Default), "UDP"
}
