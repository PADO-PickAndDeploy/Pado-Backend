package org.pado.api.core.vault.config;

import java.util.Optional;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.vault.authentication.AppRoleAuthentication;
import org.springframework.vault.authentication.AppRoleAuthenticationOptions;
import org.springframework.vault.authentication.AppRoleAuthenticationOptions.RoleId;
import org.springframework.vault.authentication.AppRoleAuthenticationOptions.SecretId;
import org.springframework.vault.client.VaultEndpoint;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class VaultConfig {
    
    @Value("${spring.cloud.vault.uri:}")
    private String vaultUri;
    
    @Value("${spring.cloud.vault.app-role.role-id:}")
    private String roleId;

    @Value("${spring.cloud.vault.app-role.secret-id:}")
    private String secretId;

    /**
     * VaultTemplate 빈 생성 (Vault 활성화시에만)
     */
    @Bean
    @ConditionalOnProperty(name = "spring.cloud.vault.enabled", havingValue = "true")
    public VaultTemplate vaultTemplate() {
        log.info("Creating VaultTemplate with URI: {}", maskUri(vaultUri));
        
        try {
            VaultEndpoint endpoint = VaultEndpoint.from(java.net.URI.create(vaultUri));
            
            AppRoleAuthenticationOptions options = AppRoleAuthenticationOptions.builder()
                    .roleId(RoleId.provided(roleId))
                    .secretId(SecretId.provided(secretId))
                    .build();
            
            RestTemplate restTemplate = new RestTemplate();
            AppRoleAuthentication auth = new AppRoleAuthentication(options, restTemplate);
            
            VaultTemplate template = new VaultTemplate(endpoint, auth);
            log.info("VaultTemplate created successfully with AppRole authentication");
            return template;
            
        } catch (Exception e) {
            log.error("Failed to create VaultTemplate: {}", e.getMessage());
            throw new RuntimeException("Vault configuration failed", e);
        }
    }

    
    /**
     * Optional<VaultTemplate> 빈 생성 
     * Vault 비활성화시에는 Optional.empty() 반환
     */
    @Bean
    public Optional<VaultTemplate> optionalVaultTemplate(
            @Value("${spring.cloud.vault.enabled:false}") boolean vaultEnabled) {
        
        if (!vaultEnabled) {
            log.info("Vault is disabled - creating empty Optional<VaultTemplate>");
            return Optional.empty();
        }
        
        try {
            // 이미 존재하는 VaultTemplate 빈을 재사용하려고 시도
            VaultTemplate template = vaultTemplate();
            return Optional.of(template);
        } catch (Exception e) {
            log.warn("Failed to create VaultTemplate, returning empty Optional: {}", e.getMessage());
            return Optional.empty();
        }
    }
    
    private String maskUri(String uri) {
        if (uri == null || uri.isEmpty()) {
            return "not configured";
        }
        return uri.replaceAll("://[^@]+@", "://***:***@");
    }
}