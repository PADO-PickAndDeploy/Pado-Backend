package org.pado.api.controller;

import java.util.Map;

import org.pado.api.core.security.userdetails.CustomUserDetails;
import org.pado.api.dto.request.FindUsernameRequest;
import org.pado.api.dto.request.SigninRequest;
import org.pado.api.dto.request.SignupRequest;
import org.pado.api.dto.response.DefaultResponse;
import org.pado.api.dto.response.FindUsernameResponse;
import org.pado.api.dto.response.SigninResponse;
import org.pado.api.dto.response.SignupResponse;
import org.pado.api.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

@RestController
@RequiredArgsConstructor
@Tag(name = "Auth", description = "인증 관련 API")
public class AuthController {
    private final AuthService authService;

    @Operation(summary = "회원가입", description = "유저 정보를 기반으로 회원가입을 진행합니다.")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "회원가입 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = SignupResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 (유효성 검증 실패, 중복된 이메일 등)",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DefaultResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "409",
            description = "이미 존재하는 사용자",
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
    @PostMapping("/signup")
    public ResponseEntity<SignupResponse> signup(@Valid @RequestBody SignupRequest request) {
        return ResponseEntity.ok(authService.signup(request));
    }
    
    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인을 시도합니다.")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "로그인 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = SigninResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 (필수 필드 누락 등)",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DefaultResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "인증 실패 (잘못된 이메일 또는 비밀번호)",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DefaultResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "존재하지 않는 사용자",
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
    @PostMapping("/signin")
    public ResponseEntity<SigninResponse> signin(@Valid @RequestBody SigninRequest request) {
        return ResponseEntity.ok(authService.signin(request));
    }

    @Operation(summary = "로그아웃", description = "로그아웃 처리를 수행합니다.")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "로그아웃 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DefaultResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 (유효하지 않은 토큰 등)",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DefaultResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "인증되지 않은 사용자 (토큰 만료 또는 무효)",
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
    @PostMapping("/signout")
    public ResponseEntity<DefaultResponse> signout(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody Map<String, String> request) {
        
        String refreshToken = request.get("refreshToken");
        
        return ResponseEntity.ok(authService.signout(userDetails, refreshToken));
    }


    // @Operation(summary = "아이디 찾기", description = "등록한 이메일 주소로 마스킹된 아이디를 조회합니다.")
    // @ApiResponses({
    //     @ApiResponse(
    //         responseCode = "200",
    //         description = "요청 처리 완료 (계정 존재 여부와 관계없이 항상 200 반환)",
    //         content = @Content(
    //             mediaType = "application/json",
    //             schema = @Schema(implementation = FindUsernameResponse.class)
    //         )
    //     ),
    //     @ApiResponse(
    //         responseCode = "400",
    //         description = "잘못된 요청 (이메일 형식 오류, 필수 필드 누락)",
    //         content = @Content(
    //             mediaType = "application/json",
    //             schema = @Schema(implementation = DefaultResponse.class)
    //         )
    //     ),
    //     @ApiResponse(
    //         responseCode = "500",
    //         description = "서버 내부 오류",
    //         content = @Content(
    //             mediaType = "application/json",
    //             schema = @Schema(implementation = DefaultResponse.class)
    //         )
    //     )
    // })
    // @PostMapping("/finduser")
    // public ResponseEntity<FindUsernameResponse> findUser(@Valid @RequestBody FindUsernameRequest request) {
    //     return ResponseEntity.ok(authService.findUsernameByEmail(request));
    // }
    
}
