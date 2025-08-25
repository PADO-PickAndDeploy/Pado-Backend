package org.pado.api.service;

import org.pado.api.core.security.jwt.JwtUtil;
import org.pado.api.core.security.jwt.TokenBlacklistService;
import org.pado.api.core.security.userdetails.CustomUserDetails;
import org.pado.api.core.exception.CustomException;
import org.pado.api.core.exception.ErrorCode;
import org.pado.api.domain.user.User;
import org.pado.api.domain.user.UserRepository;
import org.pado.api.dto.response.DefaultResponse;
import org.pado.api.dto.response.FindUsernameResponse;
import org.pado.api.dto.request.FindUsernameRequest;
import org.pado.api.dto.request.SigninRequest;
import org.pado.api.dto.response.SigninResponse;
import org.pado.api.dto.request.SignupRequest;
import org.pado.api.dto.response.SignupResponse;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final TokenBlacklistService tokenBlacklistService;
    
    /** 
     * 회원가입
     * @param request 회원가입 요청 DTO
     * @return 회원가입 응답 DTO (JWT 토큰 포함)
     */
    @Transactional
    public SignupResponse signup(SignupRequest request) {
        // 이메일 중복 체크
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new CustomException(ErrorCode.USER_ALREADY_EXISTS, 
                "이메일 '" + request.getEmail() + "'은 이미 사용 중입니다.");
        }

        User newUser = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        User savedUser = userRepository.save(newUser);

        // JWT 토큰 생성
        String accessToken = jwtUtil.generateAccessToken(savedUser.getId().toString());
        String refreshToken = jwtUtil.generateRefreshToken(savedUser.getId().toString());
        
        return new SignupResponse(
                savedUser.getId(),
                "회원 가입이 성공적으로 완료되었습니다.",
                accessToken,
                refreshToken
        );
    }

    /**
     * 로그인 - 보안 로깅 적용
     * @param request 로그인 요청 DTO
     * @return 로그인 응답 DTO (JWT 토큰 포함)
     */
    @Transactional(readOnly = true)
    public SigninResponse signin(SigninRequest request) {
        User user = userRepository.findByName(request.getUserName())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_AUTHENTICATION_FAILED, 
                    "사용자명 또는 비밀번호가 올바르지 않습니다."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.USER_AUTHENTICATION_FAILED, 
                "사용자명 또는 비밀번호가 올바르지 않습니다.");
        }

        // Spring Security 인증 처리 (아이디 기반)
        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getName(), request.getPassword())
            );
        } catch (Exception e) {
            log.warn("Spring Security 인증 실패: {}", e.getMessage());
            throw new CustomException(ErrorCode.USER_AUTHENTICATION_FAILED, 
                "인증 처리 중 오류가 발생했습니다.");
        }
        
        // 사용자 ID로 JWT 토큰 생성
        String accessToken = jwtUtil.generateAccessToken(user.getId().toString());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId().toString());
        
        return new SigninResponse(accessToken, refreshToken);
    }

    /**
     * 로그아웃 (Refresh Token 블랙리스트 방식)
     * @param userDetails 현재 인증된 사용자 정보
     * @param refreshToken 무효화할 refresh Token
     * @return 로그아웃 응답 DTO
     */
    /*  TODO : UX 해결 어떻게 할 것인가? 
     * prod에서 access Token의 TTL이 매우 짧음. 현재 방식은 access Token 만료되면
     * 사용자는 그때마다 로그인을 해야하는데 TTL도 매우 짧아서 로그인도 계속 해줘야함.
     * refresh라는 명시적 API를 만들어주던지, 다른 방식을 찾든지 해결이 필요
     */
    @Transactional
    public DefaultResponse signout(CustomUserDetails userDetails, String refreshToken) {
        try {
            log.info("로그아웃 요청: 사용자ID={}", userDetails.getId());
            
            // refresh Token 유효성 검사
            if (refreshToken == null || refreshToken.trim().isEmpty()) {
                throw new CustomException(ErrorCode.INVALID_REQUEST, 
                    "Refresh Token이 필요합니다.");
            }
            
            // Refresh Token인지 검증
            if (!jwtUtil.isRefreshToken(refreshToken)) {
                throw new CustomException(ErrorCode.JWT_TOKEN_INVALID, 
                    "유효하지 않은 Refresh Token입니다.");
            }
            
            // 토큰의 사용자 ID와 현재 사용자 ID가 일치하는지 확인
            try {
                Long tokenUserId = jwtUtil.extractUserId(refreshToken);
                if (!tokenUserId.equals(userDetails.getId())) {
                    throw new CustomException(ErrorCode.FORBIDDEN, 
                        "토큰 소유자가 일치하지 않습니다.");
                }
            } catch (Exception e) {
                if (e instanceof CustomException) {
                    throw e;
                }
                throw new CustomException(ErrorCode.JWT_TOKEN_INVALID, 
                    "토큰 검증 중 오류가 발생했습니다.");
            }
            
            // refresh Token을 블랙리스트에 추가 (Redis/NoOp 구현체가 자동으로 선택됨)
            tokenBlacklistService.blacklistToken(refreshToken);
            
            log.info("로그아웃 완료: 사용자ID={}", userDetails.getId());
            return new DefaultResponse("로그아웃이 완료되었습니다");
            
        } catch (CustomException e) {
            // CustomException은 그대로 다시 던지기
            throw e;
        } catch (Exception e) {
            log.error("로그아웃 중 예상치 못한 예외 발생: {}", e.getMessage(), e);
            // 로그아웃은 보안상 실패해도 성공으로 처리하는 경우가 많음
            return new DefaultResponse("로그아웃이 완료되었습니다");
        }
    }

    // /**
    //  * 이메일로 아이디 찾기
    //  * @param request 아이디 찾기 요청 DTO
    //  * @return 아이디 찾기 응답 DTO (마스킹된 아이디)
    //  */
    // @Transactional(readOnly = true)
    // public FindUsernameResponse findUsernameByEmail(FindUsernameRequest request){

    // }
}

