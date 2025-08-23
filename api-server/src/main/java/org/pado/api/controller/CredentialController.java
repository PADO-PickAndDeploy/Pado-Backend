package org.pado.api.controller;

import java.util.List;

import org.pado.api.core.security.userdetails.CustomUserDetails;
import org.pado.api.dto.request.CredentialRegisterRequest;
import org.pado.api.dto.response.CredentialDeleteResponse;
import org.pado.api.dto.response.CredentialDetailResponse;
import org.pado.api.dto.response.CredentialResponse;
import org.pado.api.dto.response.DefaultResponse;
import org.pado.api.service.CredentialService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/credentials")
@RequiredArgsConstructor
@Tag(name = "Credential", description = "자격 증명 관련 API")
@SecurityRequirement(name = "bearerAuth")
public class CredentialController {

    private final CredentialService credentialService;

    // 크리덴셜 등록
    @PostMapping
    @Operation(
        summary = "크리덴셜 등록", 
        description = "새로운 크리덴셜을 등록합니다. 외부 API를 통해 유효성을 검증한 후 Vault에 안전하게 저장됩니다.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "크리덴셜 등록 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CredentialResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "요청 데이터 오류 (필수 필드 누락, 잘못된 형식, 필드 길이 초과, 유효하지 않은 크리덴셜)",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DefaultResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "인증 오류",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DefaultResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "409",
            description = "중복된 크리덴셜 이름",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DefaultResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "서버 내부 오류 (Vault 암호화 실패, DB 연결 오류)",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DefaultResponse.class)
            )
        )
    })
    public ResponseEntity<CredentialResponse> createCredential(
            @RequestBody CredentialRegisterRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
                return ResponseEntity.ok(credentialService.createCredential(request, userDetails));
    }

    // 크리덴셜 전체 조회
    @Operation(
        summary = "크리덴셜 목록 조회", 
        description = "현재 사용자가 등록한 모든 크리덴셜의 메타데이터를 조회합니다. (실제 크리덴셜 데이터는 포함되지 않음)",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "크리덴셜 목록 조회 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CredentialResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "인증 오류 (로그인 필요)",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DefaultResponse.class)
            )
        )
    })
    public ResponseEntity<List<CredentialResponse>> getAllCredentials(
        @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(credentialService.getAllCredentials(userDetails));
    }

    // 크리덴셜 개별 조회
    @Operation(
        summary = "크리덴셜 개별 조회", 
        description = "특정 크리덴셜의 상세 정보를 조회합니다. Vault에서 실제 크리덴셜 데이터를 복호화하여 반환합니다.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "크리덴셜 상세 조회 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CredentialDetailResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 (존재하지 않거나 유효하지 않은 credential ID, Vault 데이터 조회 실패)",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DefaultResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "인증 오류 (로그인 필요)",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DefaultResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "접근 권한 없음 (타 사용자 크리덴셜 접근 불가)",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DefaultResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "리소스를 찾을 수 없음 (크리덴셜 없음, Vault 데이터 없음)",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DefaultResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "서버 내부 오류 (Vault 서비스 내부 오류, 복호화 실패, DB 오류)",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DefaultResponse.class)
            )
        )
    })
    @GetMapping("/{credentialId}")
    public ResponseEntity<CredentialDetailResponse> getCredential(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long credentialId) {
        return ResponseEntity.ok(credentialService.getCredential(userDetails, credentialId));
    }
    
    // 크리덴셜 삭제
    @Operation(
        summary = "크리덴셜 삭제", 
        description = "크리덴셜을 삭제합니다. Vault와 DB에서 모두 제거됩니다.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "204",
            description = "크리덴셜 삭제 성공"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 (컴포넌트에서 사용 중인 크리덴셜 삭제 시도)",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DefaultResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "인증 오류 (로그인 필요)",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DefaultResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "접근 권한 없음",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DefaultResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "크리덴셜을 찾을 수 없음",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DefaultResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "409",
            description = "사용 중인 크리덴셜 삭제 시도",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DefaultResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "서버 내부 오류 (Vault 삭제 실패 또는 서비스 내부 오류)",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DefaultResponse.class)
            )
        )
    })
    @DeleteMapping("/{credentialId}")
    public ResponseEntity<CredentialDeleteResponse> deleteCredential(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long credentialId) {
        return ResponseEntity.ok(credentialService.deleteCredential(userDetails.getId(), credentialId));
    }
}
