package org.pado.api.core.vault.service;

import java.util.Map;

import org.pado.api.core.exception.CustomException;
import org.pado.api.core.exception.ErrorCode;
import org.pado.api.domain.credential.Credential;
import org.pado.api.domain.user.User;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
/**
 * 크리덴셜 전용 Vault 서비스
 */
public class CredentialVaultService {
    
    private final VaultService vaultService;
    private static final String VAULT_BASE_PATH = "secret/pado";

    /**
     * 크리덴셜 민감 데이터를 Vault에 저장
     * 경로: secret/pado/{userId}/{credentialType}/{credentialId}
     */
    public void storeCredentialData(User user, Credential credential, String credentialData) {
        validateInputs(user, credential, credentialData);
        
        String path = buildVaultPath(user.getId(), credential.getType(), credential.getId());
        
        Map<String, Object> vaultData = Map.of(
            "credentialData", credentialData,
            "createdAt", credential.getCreatedAt().toString()
        );
        
        try {
            // VaultService에 위임 (마스킹도 VaultService에서 처리)
            vaultService.writeSecret(path, vaultData);
            log.info("Stored credential data for user: {} credential: {}", user.getId(), credential.getId());
        } catch (Exception e) {
            log.error("Failed to store credential data for user: {} credential: {}", 
                user.getId(), credential.getId());
            throw e; // VaultService에서 이미 CustomException으로 변환됨
        }
    }

    /**
     * Vault에서 크리덴셜 민감 데이터 조회
     */
    public String getCredentialData(User user, Credential credential) {
        validateInputs(user, credential);
        
        String path = buildVaultPath(user.getId(), credential.getType(), credential.getId());
        
        try {
            Map<String, Object> data = vaultService.readSecret(path);
            if (data == null) {
                log.warn("No credential data found for user: {} credential: {}", 
                    user.getId(), credential.getId());
                throw new CustomException(ErrorCode.VAULT_SECRET_NOT_FOUND, 
                    "크리덴셜 데이터를 찾을 수 없습니다.");
            }
            
            return (String) data.get("credentialData");
            
        } catch (CustomException e) {
            // 이미 CustomException인 경우 그대로 전파
            throw e;
        } catch (Exception e) {
            log.error("Failed to read credential data for user: {} credential: {}", 
                user.getId(), credential.getId());
            throw new CustomException(ErrorCode.VAULT_OPERATION_FAILED, 
                "크리덴셜 데이터 조회에 실패했습니다.", e);
        }
    }

    /**
     * Vault에서 크리덴셜 삭제
     */
    public void deleteCredentialData(User user, Credential credential) {
        validateInputs(user, credential);
        
        String path = buildVaultPath(user.getId(), credential.getType(), credential.getId());
        
        try {
            vaultService.deleteSecret(path);
            log.info("Deleted credential data for user: {} credential: {}", 
                user.getId(), credential.getId());
        } catch (Exception e) {
            log.error("Failed to delete credential data for user: {} credential: {}", 
                user.getId(), credential.getId());
            throw e; // VaultService에서 이미 CustomException으로 변환됨
        }
    }

    /**
     * Vault 경로 생성
     */
    private String buildVaultPath(Long userId, String credentialType, Long credentialId) {
        return String.format("%s/%d/%s/%d",
            VAULT_BASE_PATH,
            userId,
            credentialType,
            credentialId
        );
    }
    
    /**
     * 입력값 유효성 검증
     */
    private void validateInputs(User user, Credential credential) {
        if (user == null || user.getId() == null) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "사용자 정보가 유효하지 않습니다.");
        }
        
        if (credential == null || credential.getId() == null) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "크리덴셜 정보가 유효하지 않습니다.");
        }
        
        if (credential.getType() == null || credential.getType().trim().isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "크리덴셜 타입이 유효하지 않습니다.");
        }
    }
    
    /**
     * 입력값 유효성 검증 (credentialData 포함)
     */
    private void validateInputs(User user, Credential credential, String credentialData) {
        validateInputs(user, credential);
        
        if (credentialData == null || credentialData.trim().isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "크리덴셜 데이터가 비어있습니다.");
        }
    }
}