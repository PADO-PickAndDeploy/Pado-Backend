package org.pado.api.controller;

import org.pado.api.service.ComponentService;
import org.pado.api.core.security.userdetails.CustomUserDetails;
import org.pado.api.dto.request.ComponentCreateRequest;
import org.pado.api.dto.request.ComponentSettingRequest;
import org.pado.api.dto.request.ConnectionCreateRequest;
import org.pado.api.dto.response.ComponentCreateResponse;
import org.pado.api.dto.response.ComponentDeleteResponse;
import org.pado.api.dto.response.ComponentListResponse;
import org.pado.api.dto.response.ComponentSettingResponse;
import org.pado.api.dto.response.ConnectionCreateResponse;
import org.pado.api.dto.response.ConnectionDeleteResponse;
import org.pado.api.dto.response.DefaultResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@Tag(name = "Component", description = "컴포넌트 관련 API")
public class ComponentController {
    private final ComponentService componentService;

    @Operation(summary = "컴포넌트 목록 조회", description = "전체 컴포넌트의 목록을 조회합니다.")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "컴포넌트 목록 조회 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ComponentListResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DefaultResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "서버 내부 오류",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DefaultResponse.class)
            )
        )
    })
    @GetMapping("/components")
    public ResponseEntity<ComponentListResponse> getComponentList() {
        return ResponseEntity.ok(componentService.getComponentList());
    }

    @Operation(summary = "컴포넌트 생성", description = "새로운 컴포넌트를 생성합니다.")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "컴포넌트 생성 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ComponentCreateResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DefaultResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "인증 실패",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DefaultResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "프로젝트 또는 컴포넌트가 존재하지 않음",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DefaultResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "서버 내부 오류",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DefaultResponse.class)
            )
        )
    })
    @PostMapping("/projects/{projectId}/components")
    public ResponseEntity<ComponentCreateResponse> createComponent(@PathVariable Long projectId, @Valid @RequestBody ComponentCreateRequest request, @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(componentService.createComponent(projectId, request, userDetails));
    }

    @Operation(summary = "컴포넌트 설정 변경", description = "컴포넌트의 설정을 변경합니다.")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "컴포넌트 설정 변경 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ComponentSettingResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DefaultResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "인증 실패",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DefaultResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "프로젝트 또는 컴포넌트가 존재하지 않음",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DefaultResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "서버 내부 오류",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DefaultResponse.class)
            )
        )
    })
    @PostMapping("/projects/{projectId}/components/{componentId}/setting")
    public ResponseEntity<ComponentSettingResponse> setComponentSetting(@PathVariable Long projectId, @PathVariable Long componentId, @RequestBody @Valid ComponentSettingRequest request, @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(componentService.setComponentSetting(projectId, componentId, request, userDetails));
    }
    

    @Operation(summary = "컴포넌트 삭제", description = "컴포넌트를 삭제합니다.")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "컴포넌트 삭제 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ComponentDeleteResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DefaultResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "인증 실패",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DefaultResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "프로젝트 또는 컴포넌트가 존재하지 않음",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DefaultResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "서버 내부 오류",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DefaultResponse.class)
            )
        )
    })
    @DeleteMapping("/projects/{projectId}/components/{componentId}")
    public ResponseEntity<ComponentDeleteResponse> deleteComponent(@PathVariable Long projectId, @PathVariable Long componentId, @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(componentService.deleteComponent(projectId, componentId, userDetails));
    }

    @Operation(summary = "컴포넌트 연결", description = "컴포넌트 간 연결을 수립합니다.")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "컴포넌트 연결 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ConnectionCreateResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DefaultResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "인증 실패",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DefaultResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "프로젝트 또는 컴포넌트가 존재하지 않음",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DefaultResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "서버 내부 오류",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DefaultResponse.class)
            )
        )
    })
    @PostMapping("/projects/{projectId}/components/{componentId}/connections")
    public ResponseEntity<ConnectionCreateResponse> createConnection(@PathVariable Long projectId, @PathVariable Long componentId, @RequestBody @Valid ConnectionCreateRequest request, @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(componentService.createConnection(projectId, componentId, request, userDetails));
    }

    @Operation(summary = "컴포넌트 연결 삭제", description = "컴포넌트 간 연결을 삭제합니다.")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "컴포넌트 연결 삭제 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ConnectionDeleteResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DefaultResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "인증 실패",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DefaultResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "프로젝트 또는 컴포넌트가 존재하지 않음",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DefaultResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "서버 내부 오류",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DefaultResponse.class)
            )
        )
    })
    @DeleteMapping("/projects/{projectId}/components/{sourceComponentId}/connections/{connectionId}")
    public ResponseEntity<ConnectionDeleteResponse> deleteConnection(@PathVariable Long projectId, @PathVariable Long sourceComponentId, @PathVariable Long connectionId, @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(componentService.deleteConnection(projectId, sourceComponentId, connectionId, userDetails));
    }
}