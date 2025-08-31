package org.pado.api.service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import org.pado.api.core.exception.CustomException;
import org.pado.api.core.exception.ErrorCode;
import org.pado.api.core.security.userdetails.CustomUserDetails;
import org.pado.api.core.vault.service.CredentialVaultService;
import org.pado.api.domain.credential.Credential;
import org.pado.api.domain.credential.CredentialRepository;
import org.pado.api.domain.credential.CredentialType;
import org.pado.api.dto.request.CredentialRegisterRequest;
import org.pado.api.dto.response.CredentialResponse;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CredentialService {
    
    private final CredentialRepository credentialRepository;
    private final CredentialVaultService credentialVaultService;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    
    
    @Transactional
    public CredentialResponse createCredential(CredentialRegisterRequest request, CustomUserDetails authenticatedUser) {
        log.info("Creating credential for user: {}, name: {}", authenticatedUser.getId(), request.getName());

        // 중복 이름 검증
        if (credentialRepository.existsByNameAndUser(request.getName(), authenticatedUser.getUser())) {
            log.warn("Duplicate credential name detected: {} for user: {}", request.getName(), authenticatedUser.getId());
            throw new CustomException(ErrorCode.CREDENTIAL_NAME_DUPLICATE);
        }

        CredentialType credentialType;
        try {
            credentialType = CredentialType.valueOf(request.getType());
        } catch (IllegalArgumentException e) {
            log.error("Invalid CredentialType: {}", request.getType());
            throw new CustomException(ErrorCode.CREDENTIAL_TYPE_INVALID);
        }
        // 크리덴셜 엔티티 생성 (vaultKey는 제거 - Vault에서 경로로 관리)
        Credential credential = Credential.builder()
                .name(request.getName())
                .type(credentialType)
                .description(request.getDescription())
                .user(authenticatedUser.getUser())
                .build();

        // DB에 크리덴셜 메타데이터 저장
        Credential savedCredential = credentialRepository.save(credential);

        try {
            // Vault에 실제 credentialData 저장
            credentialVaultService.storeCredentialData(authenticatedUser.getUser(), savedCredential, request.getData());
            log.info("Successfully stored credential data in Vault for credential: {}", savedCredential.getId());
        } catch (Exception e) {
            // Vault 저장 실패시 DB 롤백을 위해 RuntimeException 던지기
            log.error("Failed to store credential data in Vault for credential: {}", savedCredential.getId(), e);
            throw new CustomException(ErrorCode.VAULT_OPERATION_FAILED, 
                "크리덴셜 데이터 저장에 실패했습니다.", e);
        }
        
        return new CredentialResponse(
                savedCredential.getId(),
                savedCredential.getName(),
                savedCredential.getType(),
                savedCredential.getDescription(),
                "크리덴셜 등록 완료",
                savedCredential.getCreatedAt().format(formatter)
        );
    }

    @Transactional(readOnly = true)
    public List<CredentialResponse> getAllCredentials(CustomUserDetails authenticatedUser) {

        return credentialRepository.findByUser(authenticatedUser.getUser()).stream()
                .map(c -> new CredentialResponse(
                        c.getId(),
                        c.getName(),
                        c.getType(),
                        c.getDescription(),
                        "크리덴셜 조회 완료",
                        c.getCreatedAt().format(formatter)))
                .collect(Collectors.toList());
    }
    

    /**
     * 소유권 검증
     */
    private void validateOwnership(Credential credential, Long userId) {
        if (!credential.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN, 
                "해당 크리덴셜에 접근할 권한이 없습니다.");
        }
    }
}
