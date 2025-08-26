package org.pado.api.controller;

import org.pado.api.core.security.userdetails.CustomUserDetails;
import org.pado.api.dto.response.DefaultResponse;
import org.pado.api.dto.response.DeployStartResponse;
import org.pado.api.dto.response.DeployStopResponse;
import org.pado.api.dto.response.ProjectCreateResponse;
import org.pado.api.service.DeployService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@Tag(name = "Deploy", description = "프로젝트 배포 관련 API")
public class DeployController {
    private final DeployService deployService;


    @Operation(summary = "프로젝트 배포", description = "새로운 프로젝트를 배포합니다.")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "프로젝트 배포 요청 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ProjectCreateResponse.class)
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
            responseCode = "500",
            description = "서버 내부 오류",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DefaultResponse.class)
            )
        )
    })
    @PostMapping("/projects/{projectId}/deploy/start")
    public ResponseEntity<DeployStartResponse> startProjectDeployment(@PathVariable Long projectId, @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(deployService.startDeployment(projectId, userDetails));
    }
    

    @Operation(summary = "프로젝트 배포 중지", description = "진행 중인 프로젝트 배포를 중지합니다.")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "프로젝트 배포 중지 요청 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ProjectCreateResponse.class)
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
            responseCode = "500",
            description = "서버 내부 오류",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DefaultResponse.class)
            )
        )
    })
    @PostMapping("/projects/{projectId}/deploy/stop")
    public ResponseEntity<DeployStopResponse> stopProjectDeployment(@PathVariable Long projectId, @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(deployService.stopDeployment(projectId, userDetails));
    }
}
