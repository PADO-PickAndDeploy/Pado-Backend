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
import org.pado.api.domain.user.User;
import org.pado.api.dto.request.CredentialRegisterRequest;
import org.pado.api.dto.response.CredentialDeleteResponse;
import org.pado.api.dto.response.CredentialDetailResponse;
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

        // 크리덴셜 엔티티 생성 (vaultKey는 제거 - Vault에서 경로로 관리)
        Credential credential = Credential.builder()
                .name(request.getName())
                .type(request.getType())
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
        log.info("Retrieving all credentials for user: {}", authenticatedUser.getId());
        
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

    @Transactional(readOnly = true)
    public CredentialDetailResponse getCredential(CustomUserDetails authenticatedUser, Long credentialId){
        log.info("Getting credential detail: {} for user: {}", credentialId, authenticatedUser.getId());

        // 크리덴셜 존재 여부
        Credential credential = credentialRepository.findById(credentialId)
            .orElseThrow(() -> new CustomException(ErrorCode.CREDENTIAL_NOT_FOUND));
        // 사용자 소유권 확인
        if (!credential.getUser().getId().equals(authenticatedUser.getUser().getId())) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }
        // Vault에서 실제 데이터 조회
        String credentialData;
        try {
            credentialData = credentialVaultService.getCredentialData(authenticatedUser.getUser(), credential);
            log.debug("Successfully retrieved credential data from Vault for credential: {}", credentialId);
        } catch (Exception e) {
            log.error("Failed to retrieve credential data from Vault for credential: {}", credentialId, e);
            throw new CustomException(ErrorCode.VAULT_OPERATION_FAILED, 
                "크리덴셜 데이터 조회에 실패했습니다.", e);
        }

        return new CredentialDetailResponse(
                credential.getId(),
                credential.getName(),
                credential.getType(),
                credential.getDescription(),
                credentialData,
                "크리덴셜 상세 조회 완료",
                credential.getCreatedAt().format(formatter),
                credential.getUpdatedAt().format(formatter)
        );
    }

    @Transactional
    public CredentialDeleteResponse deleteCredential(CustomUserDetails authenticatedUser, Long credentialId){
        log.info("Deleting credential: {} for user: {}", credentialId, authenticatedUser.getId());

        User user = authenticatedUser.getUser();
        Credential credential;

        try {
            credential = credentialRepository.findByIdAndUserId(credentialId, user.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.CREDENTIAL_NOT_FOUND));

            // 추후 개발 예정 부분 (크리덴셜을 사용하는 프로젝트 상태 확인)
            // List<Project> projectsUsingCredential = findProjectsUsingCredential(credentialId);
            // for (Project project : projectsUsingCredential) {
            //     if (project.getStatus() != Status.DRAFT && project.getStatus() != Status.STOP) {
            //         throw new CustomException(ErrorCode.CREDENTIAL_DELETION_NOT_ALLOWED, 
            //             "실행 중이거나 배포된 프로젝트에서 사용 중인 크리덴셜입니다.");
            //     }
            // }

            // Vault에서 크리덴셜 삭제
            try {
                credentialVaultService.deleteCredentialData(user, credential);
                log.info("Successfully deleted credential data from Vault for credential: {}", credentialId);
            } catch (Exception e) {
                log.error("Failed to delete credential data from Vault for credential: {}", credentialId, e);
                throw new CustomException(ErrorCode.VAULT_OPERATION_FAILED,
                    "Vault에서 크리덴셜 데이터 삭제에 실패했습니다.", e);
            }

            // DB에서 크리덴셜 삭제
            credentialRepository.delete(credential);

        } catch (CustomException e) {
            log.warn("Credential deletion failed for user: {}, credential ID: {}", user.getId(), credentialId);
            throw e;
        } catch (Exception e) {
            log.error("Error occurred while deleting credential for user: {}", user.getId(), e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "크리덴셜 삭제 중 오류가 발생했습니다.");
        }
        
        return new CredentialDeleteResponse("크리덴셜이 성공적으로 삭제되었습니다.");
    } 

    

    /**
     * 소유권 검증
     * 재사용성 : 여러곳에서 소유권 검증이 필요할 때
     */
    private void validateOwnership(Credential credential, Long userId) {
        if (!credential.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN, 
                "해당 크리덴셜에 접근할 권한이 없습니다.");
        }
    }
}
